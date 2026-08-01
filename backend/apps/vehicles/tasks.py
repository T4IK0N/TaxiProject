from datetime import timedelta

from celery import shared_task
from django.utils import timezone

from .models import VehicleDocument


@shared_task
def check_expiring_vehicle_documents():
    """
    Analogicznie do apps.drivers.tasks.check_expiring_driver_documents,
    ale dla dokumentow pojazdu: OC, AC, przeglad techniczny, zezwolenie.

    W przeciwienstwie do kierowcy, wygasniecie dokumentu pojazdu
    (np. OC) nie zawiesza automatycznie pojazdu w systemie - to swiadoma
    decyzja, bo np. przeglad techniczny czasem ma kilkudniowy bufor
    ustawowy. Dyspozytor dostaje alert i sam decyduje, ale pojazd
    NIE powinien dluzej byc przydzielany do nowych zmian (walidacja
    w apps.scheduling.validators powinna to wziac pod uwage w kolejnej
    iteracji - oznaczone jako TODO).
    """
    from apps.notifications.services import notify_vehicle_document_expiring

    today = timezone.now().date()
    upcoming = VehicleDocument.objects.filter(expires_at__gte=today).select_related("vehicle")

    notified = 0
    for doc in upcoming:
        alert_date = doc.expires_at - timedelta(days=doc.alert_threshold_days)
        if alert_date <= today:
            notify_vehicle_document_expiring(doc)
            notified += 1

    return {"checked": upcoming.count(), "notified": notified}

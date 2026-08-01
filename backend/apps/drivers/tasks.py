from datetime import timedelta

from celery import shared_task
from django.utils import timezone

from .models import Driver, DriverDocument


@shared_task
def check_expiring_driver_documents():
    from apps.notifications.services import notify_document_expiring, notify_driver_suspended

    today = timezone.now().date()

    # Dokumenty, ktore wchodza w prog alertu (ale jeszcze nie wygasly)
    upcoming = DriverDocument.objects.filter(expires_at__gte=today).select_related("driver")
    for doc in upcoming:
        alert_date = doc.expires_at - timedelta(days=doc.alert_threshold_days)
        if alert_date <= today:
            notify_document_expiring(doc)

    # Dokumenty juz przeterminowane -> zawieszenie kierowcy
    expired_driver_ids = (
        DriverDocument.objects.filter(expires_at__lt=today)
        .values_list("driver_id", flat=True)
        .distinct()
    )
    drivers_to_suspend = Driver.objects.filter(
        id__in=expired_driver_ids, status=Driver.Status.ACTIVE
    )
    for driver in drivers_to_suspend:
        driver.status = Driver.Status.SUSPENDED
        driver.save(update_fields=["status", "updated_at"])
        notify_driver_suspended(driver)

    return {
        "checked": upcoming.count(),
        "suspended": drivers_to_suspend.count(),
    }

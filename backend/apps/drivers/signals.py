from django.db.models.signals import post_save
from django.dispatch import receiver
from django.utils import timezone

from .models import Driver, DriverDocument


@receiver(post_save, sender=DriverDocument)
def suspend_driver_if_document_expired(sender, instance: DriverDocument, **kwargs):
    """
    Jesli ktos zapisze dokument z data wygasniecia w przeszlosci
    (np. zapomniano odnowic na czas), kierowca jest automatycznie
    zawieszany. To jest siec bezpieczenstwa - glowna logika sprawdzania
    terminow i tak dziala w cyklicznym zadaniu Celery (apps.drivers.tasks),
    ale ten sygnal reaguje natychmiast przy zapisie/edycji rekordu.
    """
    if instance.expires_at < timezone.now().date():
        driver = instance.driver
        if driver.status == Driver.Status.ACTIVE:
            driver.status = Driver.Status.SUSPENDED
            driver.save(update_fields=["status", "updated_at"])

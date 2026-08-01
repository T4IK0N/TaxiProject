import logging
from django.core.mail import send_mail
from django.conf import settings

logger = logging.getLogger("notifications")

def notify_document_expiring(document):
    """
    Wysyła e-mail z alertem o nadchodzącym wygaśnięciu dokumentu kierowcy.
    Wysyła do kierowcy (jeśli ma e-mail i włączone powiadomienia) oraz do dyspozytora.
    """
    driver = document.driver
    message = (
        f"Dokument '{document.get_doc_type_display()}' kierowcy "
        f"{driver.full_name} wygasa {document.expires_at} "
        f"(za {document.days_until_expiry} dni)."
    )
    logger.warning(message)

    # powiadomienie do kierowcy
    if getattr(driver, 'notifications_enabled', True) and getattr(driver, 'email', None):
        try:
            send_mail(
                subject="Przypomnienie o wygasającym dokumencie - Taxi App",
                message=message,
                from_email=settings.DEFAULT_FROM_EMAIL,
                recipient_list=[driver.email],
                fail_silently=True,  # False keidy debugowanie
            )
        except Exception as e:
            logger.error(f"Nie udało się wysłać e-maila do kierowcy {driver.email}: {e}")

    # powiadomienie do dyspozytora
    if hasattr(settings, 'DISPATCHER_EMAIL'):
        try:
            send_mail(
                subject=f"Alert: Wygasający dokument kierowcy {driver.full_name}",
                message=message,
                from_email=settings.DEFAULT_FROM_EMAIL,
                recipient_list=[settings.DISPATCHER_EMAIL],
                fail_silently=True,
            )
        except Exception as e:
            logger.error(f"Nie udało się wysłać e-maila do dyspozytora: {e}")


def notify_driver_suspended(driver):
    """
    Wysyła powiadomienie do dyspozytora o automatycznym zawieszeniu kierowcy.
    """
    message = (
        f"Kierowca {driver.full_name} został automatycznie zawieszony "
        f"z powodu przeterminowanego dokumentu."
    )
    logger.error(message)

    if hasattr(settings, 'DISPATCHER_EMAIL'):
        try:
            send_mail(
                subject=f"PILNE: Kierowca {driver.full_name} zawieszony!",
                message=message,
                from_email=settings.DEFAULT_FROM_EMAIL,
                recipient_list=[settings.DISPATCHER_EMAIL],
                fail_silently=True,
            )
        except Exception as e:
            logger.error(f"Nie udało się wysłać e-maila o zawieszeniu do dyspozytora: {e}")


def notify_vehicle_document_expiring(document):
    """
    Wysyła powiadomienie do dyspozytora/floty o wygasającym dokumencie pojazdu.
    """
    vehicle = document.vehicle
    message = (
        f"Dokument '{document.get_doc_type_display()}' pojazdu "
        f"{vehicle.plate_number} wygasa {document.expires_at}."
    )
    logger.warning(message)

    if hasattr(settings, 'DISPATCHER_EMAIL'):
        try:
            send_mail(
                subject=f"Alert floty: Wygasający dokument pojazdu {vehicle.plate_number}",
                message=message,
                from_email=settings.DEFAULT_FROM_EMAIL,
                recipient_list=[settings.DISPATCHER_EMAIL],
                fail_silently=True,
            )
        except Exception as e:
            logger.error(f"Nie udało się wysłać e-maila flotowego: {e}")
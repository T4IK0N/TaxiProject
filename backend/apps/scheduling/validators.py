from django.core.exceptions import ValidationError
from django.utils import timezone

from apps.vehicles.models import Vehicle


def validate_no_driver_overlap(shift):
    """Kierowca nie moze miec dwoch zmian nakladajacych sie czasowo."""
    from .models import Shift

    overlapping = Shift.objects.filter(
        driver=shift.driver,
        start_at__lt=shift.end_at,
        end_at__gt=shift.start_at,
    ).exclude(status=Shift.Status.CANCELLED)

    if shift.pk:
        overlapping = overlapping.exclude(pk=shift.pk)

    if overlapping.exists():
        raise ValidationError(
            f"Kierowca {shift.driver.full_name} ma juz zaplanowana zmiane "
            f"w tym przedziale czasowym."
        )


def validate_no_vehicle_overlap(shift):
    """Jedno auto nie moze byc przypisane do dwoch zmian naraz."""
    from .models import Shift

    overlapping = Shift.objects.filter(
        vehicle=shift.vehicle,
        start_at__lt=shift.end_at,
        end_at__gt=shift.start_at,
    ).exclude(status=Shift.Status.CANCELLED)

    if shift.pk:
        overlapping = overlapping.exclude(pk=shift.pk)

    if overlapping.exists():
        raise ValidationError(
            f"Pojazd {shift.vehicle.plate_number} jest juz przypisany "
            f"do innej zmiany w tym przedziale czasowym."
        )


def validate_vehicle_available(shift):
    """Auto nie moze byc w statusie 'w serwisie' lub 'wycofane'."""
    if shift.vehicle.status != Vehicle.Status.ACTIVE:
        raise ValidationError(
            f"Pojazd {shift.vehicle.plate_number} nie jest aktywny "
            f"(status: {shift.vehicle.get_status_display()})."
        )


def validate_driver_documents_valid(shift):
    """
    Kierowca nie moze byc wyznaczony na zmiane, jesli w trakcie jej
    trwania wygasa ktorykolwiek z jego wymaganych dokumentow
    (licencja, prawo jazdy, psychotesty, medycyna pracy).
    """
    expired_during_shift = shift.driver.documents.filter(
        expires_at__lt=shift.end_at.date()
    )
    if expired_during_shift.exists():
        doc = expired_during_shift.first()
        raise ValidationError(
            f"Kierowca {shift.driver.full_name} ma przeterminowany dokument: "
            f"{doc.get_doc_type_display()} (wygasl {doc.expires_at})."
        )


def validate_shift(shift, check_documents=True):
    """
    Glowna funkcja walidujaca - wywolywana explicit z serializera/widoku
    przed zapisem zmiany do bazy. Zbiera wszystkie bledy naraz,
    zamiast zwracac tylko pierwszy znaleziony.
    """
    errors = []
    validators = [
        validate_no_driver_overlap,
        validate_no_vehicle_overlap,
        validate_vehicle_available,
    ]
    if check_documents:
        validators.append(validate_driver_documents_valid)

    for validator in validators:
        try:
            validator(shift)
        except ValidationError as e:
            errors.append(e.message)

    if errors:
        raise ValidationError(errors)

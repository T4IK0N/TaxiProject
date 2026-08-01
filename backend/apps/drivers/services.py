from django.core.exceptions import ValidationError
from django.core.mail import send_mail
from django.db import transaction
from django.utils import timezone

from .models import Driver, EmailChangeRequest


def request_email_change(*, driver: Driver, new_email: str) -> EmailChangeRequest:
    """
    Tworzy zadanie zmiany e-mail i wysyla kod weryfikacyjny na NOWY
    adres (nie na stary - to jest klucz calego mechanizmu, patrz
    docstring EmailChangeRequest). Driver.email NIE jest tutaj
    modyfikowany - to dzieje sie tylko w confirm_email_change po
    poprawnym podaniu kodu.
    """
    if new_email.strip().lower() == driver.email.strip().lower():
        raise ValidationError("Podany adres e-mail jest taki sam jak obecny.")

    if Driver.objects.exclude(pk=driver.pk).filter(email__iexact=new_email).exists():
        # Zapobiega dwoch kierowcom posiadania tego samego adresu e-mail -
        # bez tego dwie osoby moglyby (przypadkiem albo celowo) ustawic
        # identyczny adres, co utrudnia komunikacje (np. powiadomienia
        # o wygasajacych dokumentach trafialyby z myla co do odbiorcy).
        raise ValidationError("Ten adres e-mail jest już używany przez innego kierowcę.")

    request = EmailChangeRequest.create_for(driver=driver, new_email=new_email)

    send_mail(
        subject="Kod weryfikacyjny - zmiana adresu e-mail",
        message=(
            f"Twoj kod weryfikacyjny do zmiany adresu e-mail to: {request.code}\n\n"
            f"Kod jest aktywny przez {EmailChangeRequest.EXPIRY_MINUTES} minut.\n"
            f"Jesli to nie Ty zlecles te zmiane, zignoruj ten e-mail."
        ),
        from_email=None,  # uzywa DEFAULT_FROM_EMAIL z ustawien
        recipient_list=[new_email],
    )

    return request


@transaction.atomic
def confirm_email_change(*, driver: Driver, code: str) -> Driver:
    """
    Weryfikuje kod i, jesli poprawny, NADPISUJE Driver.email nowym
    adresem. Bierzemy najnowsze, jeszcze wazne (niewygasniete,
    nieuzyte) zadanie tego kierowcy - jesli kierowca poprosil o kod
    wiele razy, najnowsze jest tym, co faktycznie zamierza ustawic
    (np. gdyby zmienil zdanie co do docelowego adresu miedzy prosbami).
    """
    pending_request = (
        EmailChangeRequest.objects.filter(driver=driver, used_at__isnull=True)
        .order_by("-created_at")
        .first()
    )

    if pending_request is None:
        raise ValidationError("Nie znaleziono aktywnego żądania zmiany e-maila.")

    if pending_request.is_expired:
        raise ValidationError("Kod weryfikacyjny wygasł. Poproś o nowy.")

    if pending_request.code != code.strip():
        raise ValidationError("Podany kod jest nieprawidłowy.")

    driver.email = pending_request.new_email
    driver.save(update_fields=["email", "updated_at"])

    pending_request.used_at = timezone.now()
    pending_request.save(update_fields=["used_at"])

    return driver

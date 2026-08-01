import random
import uuid
from datetime import timedelta

from django.conf import settings
from django.db import models
from django.utils import timezone


class Driver(models.Model):
    """
    Profil kierowcy. Polaczony jeden-do-jednego z User (kierowca
    moze, ale nie musi, miec dostep do logowania w systemie/aplikacji).
    """

    class Status(models.TextChoices):
        ACTIVE = "active", "Aktywny"
        SUSPENDED = "suspended", "Zawieszony"  # np. przez wygasniety dokument
        INACTIVE = "inactive", "Nieaktywny"  # np. urlop, zwolnienie lekarskie
        TERMINATED = "terminated", "Zwolniony"

    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    user = models.OneToOneField(
        settings.AUTH_USER_MODEL,
        on_delete=models.SET_NULL,
        null=True,
        blank=True,
        related_name="driver_profile",
    )

    first_name = models.CharField(max_length=100)
    last_name = models.CharField(max_length=100)
    phone_number = models.CharField(max_length=20)
    email = models.EmailField(blank=True)
    pesel = models.CharField(
        max_length=11, unique=True, help_text="Do umow i rozliczen z ZUS/US"
    )

    hired_at = models.DateField(help_text="Data zatrudnienia")
    status = models.CharField(
        max_length=20, choices=Status.choices, default=Status.ACTIVE
    )

    # Kierowca moze miec wlasne auto - ownerem moze byc tylko jeden Driver,
    # ale relacja jest zdefiniowana w apps.vehicles.Vehicle.owner_driver (FK)
    # zeby uniknac zaleznosci cyklicznej miedzy aplikacjami.

    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = "drivers"
        verbose_name = "Kierowca"
        verbose_name_plural = "Kierowcy"
        ordering = ["last_name", "first_name"]

    def __str__(self):
        return f"{self.first_name} {self.last_name}"

    @property
    def full_name(self):
        return f"{self.first_name} {self.last_name}"

    @property
    def has_expired_documents(self):
        return self.documents.filter(expires_at__lt=timezone.now().date()).exists()

    @property
    def documents_expiring_soon(self, days=14):
        threshold = timezone.now().date() + timedelta(days=days)
        return self.documents.filter(
            expires_at__lte=threshold, expires_at__gte=timezone.now().date()
        )


class DriverDocument(models.Model):
    """
    Uniwersalny model dla wszystkich dokumentow kierowcy wymagajacych
    sledzenia terminu waznosci: licencja taxi, prawo jazdy, psychotesty,
    medycyna pracy, badania lekarskie itd.

    Dodanie nowego typu dokumentu w przyszlosci = nowa wartosc w DocType,
    bez migracji schematu bazy.
    """

    class DocType(models.TextChoices):
        TAXI_LICENSE = "taxi_license", "Licencja na wykonywanie transportu taksowka"
        DRIVING_LICENSE = "driving_license", "Prawo jazdy"
        PSYCHOLOGICAL_TEST = "psychological_test", "Badania psychologiczne"
        OCCUPATIONAL_MEDICINE = "occupational_medicine", "Medycyna pracy"
        MEDICAL_EXAM = "medical_exam", "Badania lekarskie (ogolne)"
        CRIMINAL_RECORD = "criminal_record", "Zapytanie o niekaralnosc"
        OTHER = "other", "Inny dokument"

    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    driver = models.ForeignKey(
        Driver, on_delete=models.CASCADE, related_name="documents"
    )

    doc_type = models.CharField(max_length=30, choices=DocType.choices)
    document_number = models.CharField(max_length=100, blank=True)
    issuing_authority = models.CharField(
        max_length=200, blank=True, help_text="Np. nazwa urzedu, gminy, przychodni"
    )

    issued_at = models.DateField()
    expires_at = models.DateField(db_index=True)

    file = models.FileField(
        upload_to="driver_documents/%Y/%m/", blank=True, null=True
    )

    # Ile dni przed wygasnieciem system ma zaczac wysylac alerty.
    # Domyslnie 30, ale np. psychotesty/licencja moga wymagac dluzszego okresu
    # ze wzgledu na czas trwania procedury odnowienia.
    alert_threshold_days = models.PositiveSmallIntegerField(default=30)

    notes = models.TextField(blank=True)

    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = "driver_documents"
        verbose_name = "Dokument kierowcy"
        verbose_name_plural = "Dokumenty kierowcy"
        ordering = ["expires_at"]
        indexes = [
            models.Index(fields=["doc_type", "expires_at"]),
        ]

    def __str__(self):
        return f"{self.get_doc_type_display()} - {self.driver.full_name}"

    @property
    def is_expired(self):
        return self.expires_at < timezone.now().date()

    @property
    def days_until_expiry(self):
        return (self.expires_at - timezone.now().date()).days


class EmailChangeRequest(models.Model):
    """
    Zadanie zmiany adresu e-mail kierowcy, wymagajace weryfikacji
    kodem wyslanym na NOWY adres. Driver.email NIE jest nadpisywany
    od razu po zlozeniu zadania - dopiero po potwierdzeniu kodu
    (patrz apps.drivers.services.confirm_email_change). Dzieki temu:

    - stary adres pozostaje aktywny przez cala weryfikacje (np. gdyby
      byl uzywany do odzyskiwania dostepu)
    - nie da sie podstawic cudzego adresu e-mail bez dostepu do jego
      skrzynki, bo kod trafia tylko na NOWY adres, ktory chce sie ustawic

    Jeden kierowca moze miec wiele rekordow w historii (np. jesli
    poprzedni kod wygasl i poprosil o nowy) - nie ma unique constraint
    na driver, tylko logika w services ogranicza sie do najnowszego,
    nieuzytego, niewygasniete go zadania.
    """

    CODE_LENGTH = 6
    EXPIRY_MINUTES = 15

    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    driver = models.ForeignKey(
        Driver, on_delete=models.CASCADE, related_name="email_change_requests"
    )
    new_email = models.EmailField()
    code = models.CharField(max_length=CODE_LENGTH)
    expires_at = models.DateTimeField()
    used_at = models.DateTimeField(null=True, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = "email_change_requests"
        verbose_name = "Zadanie zmiany e-mail"
        verbose_name_plural = "Zadania zmiany e-mail"
        ordering = ["-created_at"]

    def __str__(self):
        return f"{self.driver.full_name} -> {self.new_email}"

    @staticmethod
    def generate_code() -> str:
        # Kod numeryczny (nie alfanumeryczny) - latwiejszy do przepisania
        # z e-maila na telefonie niz mieszanka liter/cyfr, a 6 cyfr daje
        # 1:1 000 000 szans na zgadniecie, co jest standardem branzowym
        # (np. kody 2FA SMS) dla kodu o krotkim czasie zycia (15 minut).
        return "".join(random.choices("0123456789", k=EmailChangeRequest.CODE_LENGTH))

    @classmethod
    def create_for(cls, driver, new_email):
        return cls.objects.create(
            driver=driver,
            new_email=new_email,
            code=cls.generate_code(),
            expires_at=timezone.now() + timedelta(minutes=cls.EXPIRY_MINUTES),
        )

    @property
    def is_expired(self):
        return timezone.now() > self.expires_at

    @property
    def is_used(self):
        return self.used_at is not None

    @property
    def is_valid(self):
        return not self.is_expired and not self.is_used

import uuid

from django.db import models
from django.utils import timezone


class Vehicle(models.Model):
    """
    Pojazd uzywany w firmie. Moze byc wlasnoscia firmy (FLEET) albo
    prywatnym autem kierowcy (PRIVATE) - w tym drugim przypadku
    owner_driver wskazuje na wlasciciela, a warunki rozliczenia
    (apps.billing) sa inne niz dla auta firmowego.
    """

    class OwnershipType(models.TextChoices):
        FLEET = "fleet", "Auto firmowe"
        PRIVATE = "private", "Auto prywatne kierowcy"

    class Status(models.TextChoices):
        ACTIVE = "active", "Aktywne"
        IN_SERVICE = "in_service", "W serwisie / naprawie"
        RETIRED = "retired", "Wycofane z uzytku"

    class FuelType(models.TextChoices):
        PETROL = "petrol", "Benzyna"
        DIESEL = "diesel", "Diesel"
        LPG = "lpg", "LPG"
        HYBRID = "hybrid", "Hybryda"
        ELECTRIC = "electric", "Elektryczny"

    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)

    plate_number = models.CharField(max_length=20, unique=True)
    brand = models.CharField(max_length=100)
    model = models.CharField(max_length=100)
    year = models.PositiveSmallIntegerField()
    fuel_type = models.CharField(max_length=20, choices=FuelType.choices)

    ownership_type = models.CharField(
        max_length=20, choices=OwnershipType.choices, default=OwnershipType.FLEET
    )
    # Wypelnione tylko gdy ownership_type == PRIVATE
    owner_driver = models.ForeignKey(
        "drivers.Driver",
        on_delete=models.SET_NULL,
        null=True,
        blank=True,
        related_name="owned_vehicles",
    )

    status = models.CharField(
        max_length=20, choices=Status.choices, default=Status.ACTIVE
    )

    # Aktualny przebieg - aktualizowany przy kazdym wpisie tankowania
    # i przy kazdym serwisie, zeby zawsze miec najnowsza wartosc bez
    # koniecznosci liczenia agregacji za kazdym razem.
    odometer_km = models.PositiveIntegerField(default=0)

    # Producent zazwyczaj podaje spalanie referencyjne - przydatne do
    # wykrywania anomalii (np. ktos podpisuje wiecej litrow niz realnie
    # zatankowal, albo auto ma usterke zwiekszajaca spalanie).
    manufacturer_avg_consumption = models.DecimalField(
        max_digits=5, decimal_places=2, null=True, blank=True,
        help_text="Srednie spalanie deklarowane przez producenta, l/100km",
    )

    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = "vehicles"
        verbose_name = "Pojazd"
        verbose_name_plural = "Pojazdy"
        ordering = ["plate_number"]

    def __str__(self):
        return f"{self.plate_number} ({self.brand} {self.model})"

    @property
    def is_fleet_vehicle(self):
        return self.ownership_type == self.OwnershipType.FLEET

    @property
    def is_available(self):
        return self.status == self.Status.ACTIVE


class VehicleDocument(models.Model):
    """
    Dokumenty pojazdu z terminem waznosci: OC, AC, przeglad techniczny.
    Ten sam wzorzec co DriverDocument - jedna tabela, typ jako enum,
    zamiast osobnych kolumn dla kazdego rodzaju dokumentu.
    """

    class DocType(models.TextChoices):
        OC_INSURANCE = "oc_insurance", "Ubezpieczenie OC"
        AC_INSURANCE = "ac_insurance", "Ubezpieczenie AC"
        TECHNICAL_INSPECTION = "technical_inspection", "Przeglad techniczny"
        TAXI_PERMIT = "taxi_permit", "Zezwolenie na taksowke (pojazd)"
        OTHER = "other", "Inny dokument"

    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    vehicle = models.ForeignKey(
        Vehicle, on_delete=models.CASCADE, related_name="documents"
    )

    doc_type = models.CharField(max_length=30, choices=DocType.choices)
    issued_at = models.DateField()
    expires_at = models.DateField(db_index=True)
    file = models.FileField(upload_to="vehicle_documents/%Y/%m/", blank=True, null=True)
    alert_threshold_days = models.PositiveSmallIntegerField(default=30)
    notes = models.TextField(blank=True)

    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = "vehicle_documents"
        verbose_name = "Dokument pojazdu"
        verbose_name_plural = "Dokumenty pojazdu"
        ordering = ["expires_at"]
        indexes = [models.Index(fields=["doc_type", "expires_at"])]

    def __str__(self):
        return f"{self.get_doc_type_display()} - {self.vehicle.plate_number}"

    @property
    def is_expired(self):
        return self.expires_at < timezone.now().date()


class MaintenanceLog(models.Model):
    """
    Historia serwisowa pojazdu - przeglady, naprawy, wymiany czesci.
    Wazne dla pelnego obrazu kosztow utrzymania auta (oprocz paliwa)
    i do planowania przyszlych serwisow na podstawie przebiegu.
    """

    class ServiceType(models.TextChoices):
        ROUTINE_SERVICE = "routine_service", "Przeglad okresowy"
        REPAIR = "repair", "Naprawa"
        TIRE_CHANGE = "tire_change", "Wymiana ogumienia"
        OIL_CHANGE = "oil_change", "Wymiana oleju"
        OTHER = "other", "Inne"

    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    vehicle = models.ForeignKey(
        Vehicle, on_delete=models.CASCADE, related_name="maintenance_logs"
    )

    service_type = models.CharField(max_length=30, choices=ServiceType.choices)
    description = models.TextField()
    service_date = models.DateField()
    odometer_km = models.PositiveIntegerField()
    cost = models.DecimalField(max_digits=10, decimal_places=2)
    workshop_name = models.CharField(max_length=200, blank=True)
    invoice_file = models.FileField(
        upload_to="maintenance_invoices/%Y/%m/", blank=True, null=True
    )

    # Kiedy/po jakim przebiegu nalezy zrobic to ponownie (np. nastepna wymiana oleju)
    next_service_due_km = models.PositiveIntegerField(null=True, blank=True)
    next_service_due_date = models.DateField(null=True, blank=True)

    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = "maintenance_logs"
        verbose_name = "Wpis serwisowy"
        verbose_name_plural = "Historia serwisowa"
        ordering = ["-service_date"]

    def __str__(self):
        return f"{self.get_service_type_display()} - {self.vehicle.plate_number} ({self.service_date})"

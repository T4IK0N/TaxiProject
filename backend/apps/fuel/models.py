import uuid

from django.core.exceptions import ValidationError
from django.db import models


class FuelLog(models.Model):
    """
    Wpis tankowania. Kazdy kierowca po zatankowaniu wpisuje dane tutaj
    (recznie albo przez zdjecie paragonu / OCR w przyszlosci) i system
    automatycznie wylicza:
    - calkowity koszt (litry * cena_za_litr)
    - spalanie na 100km (na podstawie roznicy przebiegu od poprzedniego
      tankowania TEGO SAMEGO pojazdu)

    Logika wyliczen jest w apps.fuel.services, nie w modelu - zeby
    metoda save() nie robila niejawnie dodatkowych zapytan do bazy
    przy kazdym wywolaniu (np. .save() wywolane przez Django admina
    lub fixture).
    """

    class FuelType(models.TextChoices):
        PETROL_95 = "petrol_95", "Benzyna PB95"
        PETROL_98 = "petrol_98", "Benzyna PB98"
        DIESEL = "diesel", "Diesel (ON)"
        LPG = "lpg", "LPG"
        ELECTRIC = "electric", "Ladowanie elektryczne (kWh)"

    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)

    driver = models.ForeignKey(
        "drivers.Driver", on_delete=models.CASCADE, related_name="fuel_logs"
    )
    vehicle = models.ForeignKey(
        "vehicles.Vehicle", on_delete=models.CASCADE, related_name="fuel_logs"
    )
    # Opcjonalne powiazanie z konkretna zmiana, podczas ktorej doszlo
    # do tankowania - przydatne do raportow "koszt paliwa per zmiana".
    shift = models.ForeignKey(
        "scheduling.Shift",
        on_delete=models.SET_NULL,
        null=True,
        blank=True,
        related_name="fuel_logs",
    )

    fuel_type = models.CharField(max_length=20, choices=FuelType.choices)
    liters = models.DecimalField(
        max_digits=7, decimal_places=2,
        help_text="Dla elektrykow: ilosc kWh (pole nazwane 'liters' dla uproszczenia schematu)",
    )
    price_per_liter = models.DecimalField(max_digits=6, decimal_places=3)
    total_cost = models.DecimalField(max_digits=10, decimal_places=2, editable=False)

    odometer_km = models.PositiveIntegerField(help_text="Przebieg w momencie tankowania")

    # Wyliczane automatycznie w apps.fuel.services na podstawie
    # poprzedniego tankowania tego samego pojazdu. Null przy pierwszym
    # tankowaniu danego pojazdu w systemie (brak punktu odniesienia).
    km_since_last_fillup = models.PositiveIntegerField(null=True, blank=True, editable=False)
    consumption_per_100km = models.DecimalField(
        max_digits=5, decimal_places=2, null=True, blank=True, editable=False
    )

    # Dane do faktury - stacja, numer faktury/paragonu
    station_name = models.CharField(max_length=200, blank=True)
    invoice_number = models.CharField(max_length=100, blank=True)
    receipt_file = models.FileField(
        upload_to="fuel_receipts/%Y/%m/", blank=True, null=True
    )

    fueled_at = models.DateTimeField(help_text="Data i godzina tankowania")
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        db_table = "fuel_logs"
        verbose_name = "Tankowanie"
        verbose_name_plural = "Tankowania"
        ordering = ["-fueled_at"]
        indexes = [
            models.Index(fields=["vehicle", "fueled_at"]),
            models.Index(fields=["driver", "fueled_at"]),
        ]

    def __str__(self):
        return f"{self.vehicle.plate_number} - {self.liters}l ({self.fueled_at:%d.%m.%Y})"

    def clean(self):
        if self.liters <= 0:
            raise ValidationError("Ilosc litrow musi byc wieksza od zera.")
        if self.price_per_liter <= 0:
            raise ValidationError("Cena za litr musi byc wieksza od zera.")

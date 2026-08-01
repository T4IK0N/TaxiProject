import uuid

from django.db import models


class EmploymentContract(models.Model):
    """
    Umowa kierowcy z firma. Typ rozliczenia jest inny w zaleznosci
    od tego czy kierowca jezdzi autem firmowym czy swoim wlasnym
    (zgodnie z wymaganiem - "warunki umowy sa inne").
    """

    class ContractType(models.TextChoices):
        EMPLOYMENT = "employment", "Umowa o prace"
        CONTRACT_OF_MANDATE = "mandate", "Umowa zlecenie"
        B2B = "b2b", "Wspolpraca B2B"

    class SettlementModel(models.TextChoices):
        FIXED_RATE = "fixed_rate", "Stawka stala (godzinowa/dzienna)"
        REVENUE_SHARE = "revenue_share", "Procent od przychodu z kursow"
        MIXED = "mixed", "Stawka stala + procent od przychodu"

    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    driver = models.OneToOneField(
        "drivers.Driver", on_delete=models.CASCADE, related_name="contract"
    )

    contract_type = models.CharField(max_length=20, choices=ContractType.choices)
    settlement_model = models.CharField(max_length=20, choices=SettlementModel.choices)

    # Stawka godzinowa/dzienna - uzywana przy FIXED_RATE i MIXED
    fixed_rate_amount = models.DecimalField(
        max_digits=8, decimal_places=2, null=True, blank=True
    )
    fixed_rate_period = models.CharField(
        max_length=10,
        choices=[("hourly", "za godzine"), ("daily", "za dzien")],
        null=True,
        blank=True,
    )

    # Procent od przychodu - uzywany przy REVENUE_SHARE i MIXED
    revenue_share_percent = models.DecimalField(
        max_digits=5, decimal_places=2, null=True, blank=True
    )

    # Jesli kierowca jezdzi wlasnym autem, firma zazwyczaj wyplaca
    # rekompensate za zuzycie pojazdu (oddzielnie od zwrotu kosztow paliwa,
    # ktory wynika z FuelLog) - np. ryczalt za km albo stala kwota miesieczna.
    private_vehicle_compensation = models.DecimalField(
        max_digits=8, decimal_places=2, null=True, blank=True,
        help_text="Rekompensata za uzywanie wlasnego pojazdu (np. zl/km lub ryczalt miesieczny)",
    )

    valid_from = models.DateField()
    valid_to = models.DateField(null=True, blank=True, help_text="Puste = umowa na czas nieokreslony")

    file = models.FileField(upload_to="contracts/%Y/%m/", blank=True, null=True)

    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = "employment_contracts"
        verbose_name = "Umowa"
        verbose_name_plural = "Umowy"

    def __str__(self):
        return f"Umowa {self.driver.full_name} ({self.get_contract_type_display()})"


class SettlementPeriod(models.Model):
    """
    Rozliczenie kierowcy za dany okres (np. miesiac) - agreguje
    przepracowane zmiany (Shift), koszty paliwa (FuelLog) i warunki
    umowy (EmploymentContract) w jedna kwote do wyplaty.

    Generowane przez Celery task na koniec okresu rozliczeniowego,
    ale wartosci sa zapisywane (nie tylko liczone "na zywo"), zeby
    raport z przeszlosci nie zmienial sie, jesli ktos potem edytuje
    stawke w umowie.
    """

    class Status(models.TextChoices):
        DRAFT = "draft", "Wersja roboca"
        APPROVED = "approved", "Zatwierdzone"
        PAID = "paid", "Wyplacone"

    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    driver = models.ForeignKey(
        "drivers.Driver", on_delete=models.CASCADE, related_name="settlement_periods"
    )

    period_start = models.DateField()
    period_end = models.DateField()

    total_hours_worked = models.DecimalField(max_digits=7, decimal_places=2, default=0)
    total_revenue = models.DecimalField(max_digits=10, decimal_places=2, default=0)
    total_fuel_cost = models.DecimalField(max_digits=10, decimal_places=2, default=0)
    base_pay = models.DecimalField(max_digits=10, decimal_places=2, default=0)
    vehicle_compensation = models.DecimalField(max_digits=10, decimal_places=2, default=0)
    final_amount = models.DecimalField(max_digits=10, decimal_places=2, default=0)

    status = models.CharField(max_length=20, choices=Status.choices, default=Status.DRAFT)

    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = "settlement_periods"
        verbose_name = "Rozliczenie"
        verbose_name_plural = "Rozliczenia"
        ordering = ["-period_start"]
        unique_together = [("driver", "period_start", "period_end")]

    def __str__(self):
        return f"Rozliczenie {self.driver.full_name}: {self.period_start} - {self.period_end}"

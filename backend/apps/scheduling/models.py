import uuid

from django.core.exceptions import ValidationError
from django.db import models


class Shift(models.Model):
    """
    Pojedyncza zmiana w grafiku: kierowca + pojazd + przedzial czasowy.
    NIE MA CZEGOS TAKIEGO JAK TYDZIEN W DJANGO!
    """

    class Status(models.TextChoices):
        PLANNED = "planned", "Zaplanowana"
        IN_PROGRESS = "in_progress", "W trakcie"
        COMPLETED = "completed", "Zakonczona"
        CANCELLED = "cancelled", "Odwolana"

    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)

    driver = models.ForeignKey(
        "drivers.Driver", on_delete=models.CASCADE, related_name="shifts"
    )
    vehicle = models.ForeignKey(
        "vehicles.Vehicle", on_delete=models.CASCADE, related_name="shifts"
    )

    start_at = models.DateTimeField(db_index=True)
    end_at = models.DateTimeField(db_index=True)

    status = models.CharField(
        max_length=20, choices=Status.choices, default=Status.PLANNED
    )

    # Przebieg na start/koniec zmiany - opcjonalne, ale przydatne do
    # raportowania "ile km przejechano na danej zmianie" niezaleznie
    # od tankowan (ktore moga nie pokrywac sie 1:1 ze zmianami).
    odometer_start_km = models.PositiveIntegerField(null=True, blank=True)
    odometer_end_km = models.PositiveIntegerField(null=True, blank=True)

    notes = models.TextField(blank=True)

    created_by = models.ForeignKey(
        "accounts.User",
        on_delete=models.SET_NULL,
        null=True,
        related_name="created_shifts",
        help_text="Dyspozytor, ktory utworzyl zmiane",
    )

    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = "shifts"
        verbose_name = "Zmiana"
        verbose_name_plural = "Zmiany (grafik)"
        ordering = ["start_at"]
        indexes = [
            models.Index(fields=["driver", "start_at"]),
            models.Index(fields=["vehicle", "start_at"]),
        ]

    def __str__(self):
        return f"{self.driver.full_name} / {self.vehicle.plate_number} ({self.start_at:%d.%m %H:%M}-{self.end_at:%H:%M})"

    def clean(self):
        """
        Walidacja podstawowa na poziomie modelu. Sprawdzanie kolizji
        grafiku (czy kierowca/auto nie ma juz innej zmiany w tym czasie,
        czy dokumenty kierowcy sa wazne) jest w apps.scheduling.validators,
        bo wymaga zapytan do bazy i jest wywolywane explicit w serializerze/
        widoku - nie chcemy tej logiki w clean(), zeby uniknac
        nieoczekiwanych zapytan przy kazdym .full_clean().
        """
        if self.end_at <= self.start_at:
            raise ValidationError("Koniec zmiany musi byc po jej rozpoczeciu.")

    @property
    def duration_hours(self):
        delta = self.end_at - self.start_at
        return round(delta.total_seconds() / 3600, 2)

    @property
    def km_driven(self):
        if self.odometer_start_km is not None and self.odometer_end_km is not None:
            return self.odometer_end_km - self.odometer_start_km
        return None

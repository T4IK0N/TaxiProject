from decimal import Decimal

from django.core.exceptions import ValidationError
from django.db import transaction

from apps.vehicles.models import Vehicle

from .models import FuelLog


@transaction.atomic
def create_fuel_log(*, driver, vehicle, fuel_type, liters, price_per_liter,
                     fueled_at, odometer_km=None, km_driven_since_last_fillup=None,
                     shift=None, station_name="", invoice_number="", receipt_file=None):
    """
    Tworzy wpis tankowania i wylicza:
    - total_cost
    - km_since_last_fillup i consumption_per_100km na podstawie
      poprzedniego tankowania TEGO SAMEGO pojazdu (nie kierowcy -
      bo spalanie jest cecha auta, nie kierowcy; ten sam samochod
      mogl byc tankowany przez kogos innego miedzy tymi wpisami)
    - aktualizuje Vehicle.odometer_km, jesli nowy przebieg jest wiekszy
      niz dotychczas zapisany

    Kierowca moze podac stan licznika w JEDEN z dwoch sposobow (dokladnie
    jeden z tych dwoch argumentow musi byc podany):
    - odometer_km: bezwzgledny stan licznika ("licznik pokazuje 46710")
    - km_driven_since_last_fillup: liczba km przejechanych OD OSTATNIEGO
      tankowania tego pojazdu ("przejechalem 350 km") - wygodniejsze dla
      kierowcy, ktory nie pamieta/nie chce sprawdzac dokladnego stanu
      licznika, tylko wie ile jezdzil. System sam przeliczy to na
      bezwzgledny odometer_km, dodajac do ostatniego zarejestrowanego
      przebiegu pojazdu (Vehicle.odometer_km - aktualny, zsynchronizowany
      stan, NIE przebieg z ostatniego FuelLog, bo te dwa moga sie roznic
      jesli miedzy tankowaniami byl np. wpis serwisowy z nowszym przebiegiem).

    Uzywamy jednej funkcji serwisowej (a nie logiki w widoku/serializerze)
    zeby ta sama walidacja/wyliczenia dzialaly identycznie niezaleznie
    od tego, czy wpis tworzony jest przez API, panel admina, czy
    przyszly import CSV.
    """
    if (odometer_km is None) == (km_driven_since_last_fillup is None):
        raise ValidationError(
            "Podaj dokladnie jedno z dwoch: stan licznika (odometer_km) "
            "albo liczbe przejechanych kilometrow od ostatniego tankowania "
            "(km_driven_since_last_fillup) - nie oba i nie zaden."
        )

    if km_driven_since_last_fillup is not None:
        if km_driven_since_last_fillup < 0:
            raise ValidationError("Liczba przejechanych kilometrow nie moze byc ujemna.")
        odometer_km = vehicle.odometer_km + km_driven_since_last_fillup

    if odometer_km < vehicle.odometer_km:
        raise ValidationError(
            f"Podany przebieg ({odometer_km} km) jest mniejszy niz "
            f"ostatni zarejestrowany przebieg pojazdu ({vehicle.odometer_km} km). "
            f"Sprawdz, czy nie pomylono pojazdu lub wartosci."
        )

    previous_log = (
        FuelLog.objects.filter(vehicle=vehicle, fueled_at__lt=fueled_at)
        .order_by("-fueled_at")
        .first()
    )

    km_since_last_fillup = None
    consumption_per_100km = None

    if previous_log is not None:
        km_since_last_fillup = odometer_km - previous_log.odometer_km
        if km_since_last_fillup > 0:
            consumption_per_100km = (
                Decimal(liters) / Decimal(km_since_last_fillup) * Decimal(100)
            ).quantize(Decimal("0.01"))

    fuel_log = FuelLog(
        driver=driver,
        vehicle=vehicle,
        shift=shift,
        fuel_type=fuel_type,
        liters=liters,
        price_per_liter=price_per_liter,
        total_cost=(Decimal(liters) * Decimal(price_per_liter)).quantize(Decimal("0.01")),
        odometer_km=odometer_km,
        km_since_last_fillup=km_since_last_fillup,
        consumption_per_100km=consumption_per_100km,
        station_name=station_name,
        invoice_number=invoice_number,
        receipt_file=receipt_file,
        fueled_at=fueled_at,
    )
    fuel_log.full_clean()
    fuel_log.save()

    # Synchronizacja aktualnego przebiegu pojazdu
    if odometer_km > vehicle.odometer_km:
        vehicle.odometer_km = odometer_km
        vehicle.save(update_fields=["odometer_km", "updated_at"])

    return fuel_log


def get_driver_fuel_summary(driver, date_from, date_to):
    """
    Podsumowanie tankowan kierowcy w danym okresie - do widoku
    'moje tankowania' i do raportow miesiecznych.
    """
    logs = FuelLog.objects.filter(
        driver=driver, fueled_at__date__gte=date_from, fueled_at__date__lte=date_to
    )
    total_liters = sum((log.liters for log in logs), Decimal("0"))
    total_cost = sum((log.total_cost for log in logs), Decimal("0"))

    return {
        "logs": logs,
        "total_liters": total_liters,
        "total_cost": total_cost,
        "fillup_count": logs.count(),
    }

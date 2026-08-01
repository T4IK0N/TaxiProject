from decimal import Decimal

from rest_framework import serializers

from .models import FuelLog


class FuelLogSerializer(serializers.ModelSerializer):
    """
    Serializer do ODCZYTU (GET) - zawiera pola wyliczane przez
    apps.fuel.services (total_cost, consumption_per_100km). Tworzenie
    nowych wpisow idzie przez FuelLogCreateSerializer, bo logika
    wyliczen wymaga dostepu do poprzedniego tankowania i nie nadaje
    sie do prostego ModelSerializer.create().
    """

    driver_name = serializers.CharField(source="driver.full_name", read_only=True)
    vehicle_plate = serializers.CharField(source="vehicle.plate_number", read_only=True)

    class Meta:
        model = FuelLog
        fields = [
            "id", "driver", "driver_name", "vehicle", "vehicle_plate", "shift",
            "fuel_type", "liters", "price_per_liter", "total_cost", "odometer_km",
            "km_since_last_fillup", "consumption_per_100km", "station_name",
            "invoice_number", "receipt_file", "fueled_at", "created_at",
        ]
        read_only_fields = [
            "total_cost", "km_since_last_fillup", "consumption_per_100km", "created_at",
        ]


class FuelLogCreateSerializer(serializers.Serializer):
    """
    Serializer wejsciowy do tworzenia tankowania. Nie jest ModelSerializer,
    bo faktyczne tworzenie obiektu i wyliczenia (total_cost, spalanie)
    dzieja sie w apps.fuel.services.create_fuel_log - tutaj tylko
    walidujemy ksztalt danych wejsciowych z requestu.

    Kierowca podaje stan licznika w JEDEN z dwoch sposobow:
    - odometer_km: bezwzgledny stan licznika ("licznik pokazuje 46710")
    - km_driven_since_last_fillup: liczba km przejechanych OD OSTATNIEGO
      tankowania ("przejechalem 350 km") - wygodniejsze, gdy kierowca
      nie chce sprawdzac/pamietac dokladnego stanu licznika. System
      (apps.fuel.services.create_fuel_log) sam przeliczy to na
      bezwzgledny odometer_km na podstawie Vehicle.odometer_km.

    Oba pola sa "required=False" na poziomie pojedynczego pola, ale
    validate() (walidacja na poziomie calego serializera, nie pola)
    wymusza, ze dokladnie jedno z nich musi byc podane - DRF nie ma
    wbudowanego mechanizmu "pole A albo pole B, ale nie oba", trzeba
    to sprawdzic recznie.
    """

    vehicle = serializers.UUIDField()
    shift = serializers.UUIDField(required=False, allow_null=True)
    fuel_type = serializers.ChoiceField(choices=FuelLog.FuelType.choices)
    liters = serializers.DecimalField(max_digits=7, decimal_places=2, min_value=Decimal("0.01"))
    price_per_liter = serializers.DecimalField(max_digits=6, decimal_places=3, min_value=Decimal("0.001"))
    odometer_km = serializers.IntegerField(min_value=0, required=False, allow_null=True)
    km_driven_since_last_fillup = serializers.IntegerField(min_value=0, required=False, allow_null=True)
    station_name = serializers.CharField(required=False, allow_blank=True)
    invoice_number = serializers.CharField(required=False, allow_blank=True)
    receipt_file = serializers.FileField(required=False, allow_null=True)
    fueled_at = serializers.DateTimeField()

    def validate(self, attrs):
        has_odometer = attrs.get("odometer_km") is not None
        has_km_driven = attrs.get("km_driven_since_last_fillup") is not None

        if has_odometer == has_km_driven:
            raise serializers.ValidationError(
                "Podaj dokładnie jedno z dwóch: stan licznika (odometer_km) "
                "albo liczbę przejechanych kilometrów od ostatniego tankowania "
                "(km_driven_since_last_fillup)."
            )
        return attrs

from django.core.exceptions import ValidationError as DjangoValidationError
from rest_framework import serializers

from .models import Shift
from .validators import validate_shift


class ShiftSerializer(serializers.ModelSerializer):
    driver_name = serializers.CharField(source="driver.full_name", read_only=True)
    vehicle_plate = serializers.CharField(source="vehicle.plate_number", read_only=True)
    duration_hours = serializers.FloatField(read_only=True)
    km_driven = serializers.IntegerField(read_only=True)

    class Meta:
        model = Shift
        fields = [
            "id", "driver", "driver_name", "vehicle", "vehicle_plate",
            "start_at", "end_at", "status", "odometer_start_km",
            "odometer_end_km", "duration_hours", "km_driven", "notes",
            "created_by", "created_at",
        ]
        read_only_fields = ["created_by", "created_at"]

    def validate(self, attrs):
        """
        Wykorzystujemy istniejacy walidator z apps.scheduling.validators
        (kolizje kierowcy/auta, dostepnosc pojazdu, waznosc dokumentow)
        zamiast pisac te logike od nowa w serializerze. Budujemy
        tymczasowa (niezapisana) instancje Shift, zeby przekazac ja
        do walidatora - dziala identycznie przy tworzeniu i edycji.
        """
        instance = self.instance or Shift()
        for field in ("driver", "vehicle", "start_at", "end_at"):
            if field in attrs:
                setattr(instance, field, attrs[field])
            elif self.instance:
                setattr(instance, field, getattr(self.instance, field))

        try:
            validate_shift(instance)
        except DjangoValidationError as e:
            raise serializers.ValidationError(e.messages)

        return attrs

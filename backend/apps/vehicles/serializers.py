from rest_framework import serializers

from .models import MaintenanceLog, Vehicle, VehicleDocument


class VehicleDocumentSerializer(serializers.ModelSerializer):
    is_expired = serializers.BooleanField(read_only=True)

    class Meta:
        model = VehicleDocument
        fields = [
            "id", "vehicle", "doc_type", "issued_at", "expires_at",
            "file", "alert_threshold_days", "notes", "is_expired",
        ]
        read_only_fields = ["vehicle"]


class MaintenanceLogSerializer(serializers.ModelSerializer):
    class Meta:
        model = MaintenanceLog
        fields = [
            "id", "vehicle", "service_type", "description", "service_date",
            "odometer_km", "cost", "workshop_name", "invoice_file",
            "next_service_due_km", "next_service_due_date", "created_at",
        ]
        read_only_fields = ["vehicle", "created_at"]


class VehicleListSerializer(serializers.ModelSerializer):
    """Lekki serializer do listy pojazdow."""

    class Meta:
        model = Vehicle
        fields = [
            "id", "plate_number", "brand", "model", "year", "fuel_type",
            "ownership_type", "status", "odometer_km",
        ]


class VehicleDetailSerializer(serializers.ModelSerializer):
    """Pelny serializer ze zagniezdzonymi dokumentami i historia serwisowa."""

    documents = VehicleDocumentSerializer(many=True, read_only=True)
    maintenance_logs = MaintenanceLogSerializer(many=True, read_only=True)

    class Meta:
        model = Vehicle
        fields = [
            "id", "plate_number", "brand", "model", "year", "fuel_type",
            "ownership_type", "owner_driver", "status", "odometer_km",
            "manufacturer_avg_consumption", "documents", "maintenance_logs",
            "created_at", "updated_at",
        ]
        read_only_fields = ["odometer_km", "created_at", "updated_at"]

    def validate(self, attrs):
        ownership_type = attrs.get("ownership_type") or getattr(self.instance, "ownership_type", None)
        owner_driver = attrs.get("owner_driver") or getattr(self.instance, "owner_driver", None)

        if ownership_type == Vehicle.OwnershipType.PRIVATE and not owner_driver:
            raise serializers.ValidationError(
                "Dla pojazdu prywatnego nalezy wskazac wlasciciela (owner_driver)."
            )
        if ownership_type == Vehicle.OwnershipType.FLEET and owner_driver:
            raise serializers.ValidationError(
                "Pojazd firmowy nie powinien miec przypisanego wlasciciela."
            )
        return attrs

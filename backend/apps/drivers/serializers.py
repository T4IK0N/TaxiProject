from rest_framework import serializers

from .models import Driver, DriverDocument


class DriverDocumentSerializer(serializers.ModelSerializer):
    is_expired = serializers.BooleanField(read_only=True)
    days_until_expiry = serializers.IntegerField(read_only=True)

    class Meta:
        model = DriverDocument
        fields = [
            "id", "driver", "doc_type", "document_number", "issuing_authority",
            "issued_at", "expires_at", "file", "alert_threshold_days", "notes",
            "is_expired", "days_until_expiry",
        ]
        # driver jest tylko zapisywany przez nas w widoku (z URL-a),
        # nie przez klienta API - zeby nie mozna bylo przypisac
        # dokumentu do cudzego kierowcy przez podstawienie innego ID
        read_only_fields = ["driver"]

    def validate(self, attrs):
        issued_at = attrs.get("issued_at") or getattr(self.instance, "issued_at", None)
        expires_at = attrs.get("expires_at") or getattr(self.instance, "expires_at", None)
        if issued_at and expires_at and expires_at <= issued_at:
            raise serializers.ValidationError(
                "Data wygasniecia musi byc po dacie wydania dokumentu."
            )
        return attrs


class DriverListSerializer(serializers.ModelSerializer):
    """Lekki serializer do listy kierowcow - bez zagniezdzonych dokumentow."""

    full_name = serializers.CharField(read_only=True)
    has_expired_documents = serializers.BooleanField(read_only=True)

    class Meta:
        model = Driver
        fields = [
            "id", "full_name", "first_name", "last_name", "phone_number",
            "status", "hired_at", "has_expired_documents",
        ]


class DriverDetailSerializer(serializers.ModelSerializer):
    """Pelny serializer ze zagniezdzonymi dokumentami - widok szczegolowy."""

    full_name = serializers.CharField(read_only=True)
    documents = DriverDocumentSerializer(many=True, read_only=True)
    has_expired_documents = serializers.BooleanField(read_only=True)

    class Meta:
        model = Driver
        fields = [
            "id", "user", "first_name", "last_name", "full_name", "phone_number",
            "email", "pesel", "hired_at", "status", "documents",
            "has_expired_documents", "created_at", "updated_at",
        ]
        # email jest read-only TUTAJ - zmiana e-mail wymaga weryfikacji
        # kodem (patrz EmailChangeRequestSerializer/ConfirmEmailChangeSerializer
        # nizej i powiazane widoki), nie moze byc nadpisana goim PATCH-em
        # na ten serializer, bo to by omijalo cala weryfikacje.
        read_only_fields = ["created_at", "updated_at", "email"]

    def validate_pesel(self, value):
        if not value.isdigit() or len(value) != 11:
            raise serializers.ValidationError("PESEL musi sklada sie z 11 cyfr.")
        return value


class RequestEmailChangeSerializer(serializers.Serializer):
    """Wejscie dla POST .../request-email-change/ - tylko nowy adres."""

    new_email = serializers.EmailField()


class ConfirmEmailChangeSerializer(serializers.Serializer):
    """Wejscie dla POST .../confirm-email-change/ - tylko kod z e-maila."""

    code = serializers.CharField(max_length=6, min_length=6)

from rest_framework import serializers
from rest_framework_simplejwt.serializers import TokenObtainPairSerializer

from .models import User


class CustomTokenObtainPairSerializer(TokenObtainPairSerializer):
    """
    Standardowy login JWT (username + password -> access + refresh token),
    ale dodajemy do tokenu role i id kierowcy. Frontend dzieki
    temu od razu wie, jaki layout pokazac (panel dyspozytora vs panel
    kierowcy) bez dodatkowego zapytania do API po zalogowaniu.
    """

    @classmethod
    def get_token(cls, user):
        token = super().get_token(user)
        token["role"] = user.role
        token["full_name"] = user.get_full_name() or user.username

        driver_profile = getattr(user, "driver_profile", None)
        token["driver_id"] = str(driver_profile.id) if driver_profile else None

        return token


class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ["id", "username", "email", "first_name", "last_name", "role", "phone_number"]
        read_only_fields = ["id", "role"]

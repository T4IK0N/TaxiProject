from django.core.exceptions import ValidationError as DjangoValidationError
from django.shortcuts import get_object_or_404
from django_filters.rest_framework import DjangoFilterBackend
from rest_framework import generics, status
from rest_framework.exceptions import ValidationError as DRFValidationError
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework.views import APIView

from apps.scheduling.models import Shift
from apps.vehicles.models import Vehicle
from core.permissions import IsOwnerDriverOrStaff

from . import services
from .models import FuelLog
from .serializers import FuelLogCreateSerializer, FuelLogSerializer


class FuelLogListCreateView(generics.ListAPIView):
    """
    GET /api/v1/fuel/logs/?vehicle=...&driver=...

    Lista tankowan - kierowca widzi tylko swoje (potrzebne do
    wymagania "kazdy kierowca moze sprawdzic dane do faktury, ile
    przejechal km, jakie spalanie mial"), staff widzi wszystkie
    (np. ksiegowosc do rozliczen, dyspozytor do kontroli kosztow).

    POST jest osobnym widokiem (FuelLogCreateView) bo logika
    tworzenia jest inna niz standardowy ModelSerializer.create().
    """

    serializer_class = FuelLogSerializer
    permission_classes = [IsAuthenticated]
    filter_backends = [DjangoFilterBackend]
    filterset_fields = {
        "vehicle": ["exact"],
        "driver": ["exact"],
        "fueled_at": ["gte", "lte"],
    }

    def get_queryset(self):
        qs = FuelLog.objects.all().select_related("driver", "vehicle")
        user = self.request.user
        if user.role not in ("dispatcher", "admin", "accountant"):
            driver_profile = getattr(user, "driver_profile", None)
            qs = qs.filter(driver=driver_profile) if driver_profile else qs.none()
        return qs


class FuelLogCreateView(APIView):
    """
    POST /api/v1/fuel/logs/create/

    Kierowca po zatankowaniu wpisuje dane tutaj. driver przypisywany
    jest automatycznie na podstawie zalogowanego uzytkownika (kierowca
    nie moze wpisac tankowania "za kogos innego" przez podstawienie
    cudziego driver_id) - chyba ze loguje to dyspozytor w imieniu
    kierowcy, wtedy driver_id jest wymagany w danych wejsciowych.
    """

    permission_classes = [IsAuthenticated]

    def post(self, request):
        serializer = FuelLogCreateSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        data = serializer.validated_data

        user = request.user
        if user.role in ("dispatcher", "admin"):
            driver_id = request.data.get("driver")
            if not driver_id:
                raise DRFValidationError(
                    {"driver": "To pole jest wymagane, gdy zgłoszenie dodaje dyspozytor/admin."}
                )
            from apps.drivers.models import Driver
            driver = get_object_or_404(Driver, pk=driver_id)
        else:
            driver = getattr(user, "driver_profile", None)
            if driver is None:
                raise DRFValidationError("Ten użytkownik nie ma przypisanego profilu kierowcy.")

        vehicle = get_object_or_404(Vehicle, pk=data["vehicle"])
        shift = None
        if data.get("shift"):
            shift = get_object_or_404(Shift, pk=data["shift"])

        try:
            fuel_log = services.create_fuel_log(
                driver=driver,
                vehicle=vehicle,
                shift=shift,
                fuel_type=data["fuel_type"],
                liters=data["liters"],
                price_per_liter=data["price_per_liter"],
                # Dokladnie jedno z tych dwoch jest ustawione - wymuszone
                # przez FuelLogCreateSerializer.validate(). .get(..., None)
                # bo to teraz pola opcjonalne (nie zawsze obecne w danych
                # wejsciowych) - patrz komentarz w services.create_fuel_log.
                odometer_km=data.get("odometer_km"),
                km_driven_since_last_fillup=data.get("km_driven_since_last_fillup"),
                fueled_at=data["fueled_at"],
                station_name=data.get("station_name", ""),
                invoice_number=data.get("invoice_number", ""),
                receipt_file=data.get("receipt_file"),
            )
        except DjangoValidationError as e:
            raise DRFValidationError(e.messages if hasattr(e, "messages") else str(e))

        return Response(FuelLogSerializer(fuel_log).data, status=status.HTTP_201_CREATED)


class FuelLogDetailView(generics.RetrieveAPIView):
    """GET /api/v1/fuel/logs/<id>/ - szczegoly jednego tankowania (np. do faktury)"""

    serializer_class = FuelLogSerializer
    permission_classes = [IsAuthenticated, IsOwnerDriverOrStaff]
    queryset = FuelLog.objects.all().select_related("driver", "vehicle")


class MyFuelSummaryView(APIView):
    """
    GET /api/v1/fuel/my-summary/?date_from=2026-06-01&date_to=2026-06-30

    Podsumowanie wlasnych tankowan kierowcy w danym okresie - suma
    litrow, suma kosztow, liczba tankowan. To odpowiada bezposrednio
    na wymaganie "kierowca moze sprawdzic dane do faktury" w formie
    zagregowanej (np. do podsumowania miesiecznego).
    """

    permission_classes = [IsAuthenticated]

    def get(self, request):
        driver = getattr(request.user, "driver_profile", None)
        if driver is None:
            raise DRFValidationError("Ten użytkownik nie ma przypisanego profilu kierowcy.")

        date_from = request.query_params.get("date_from")
        date_to = request.query_params.get("date_to")
        if not date_from or not date_to:
            raise DRFValidationError("Parametry date_from i date_to są wymagane.")

        summary = services.get_driver_fuel_summary(driver, date_from, date_to)
        return Response({
            "fillup_count": summary["fillup_count"],
            "total_liters": summary["total_liters"],
            "total_cost": summary["total_cost"],
            "logs": FuelLogSerializer(summary["logs"], many=True).data,
        })

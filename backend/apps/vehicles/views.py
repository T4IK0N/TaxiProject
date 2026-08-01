from django_filters.rest_framework import DjangoFilterBackend
from rest_framework import generics, filters
from rest_framework.permissions import IsAuthenticated

from core.permissions import IsDispatcherOrAdmin

from .models import MaintenanceLog, Vehicle, VehicleDocument
from .serializers import (
    MaintenanceLogSerializer,
    VehicleDetailSerializer,
    VehicleDocumentSerializer,
    VehicleListSerializer,
)


class VehicleListCreateView(generics.ListCreateAPIView):
    """
    GET  /api/v1/vehicles/  -> lista pojazdow (kazdy zalogowany moze
                                przegladac flote, np. kierowca sprawdza
                                jakie auta sa dostepne)
    POST /api/v1/vehicles/  -> nowy pojazd (tylko dyspozytor/admin)
    """

    permission_classes = [IsAuthenticated]
    filter_backends = [DjangoFilterBackend, filters.SearchFilter]
    filterset_fields = ["status", "ownership_type", "fuel_type"]
    search_fields = ["plate_number", "brand", "model"]
    queryset = Vehicle.objects.all().select_related("owner_driver")

    def get_serializer_class(self):
        return VehicleListSerializer if self.request.method == "GET" else VehicleDetailSerializer

    def get_permissions(self):
        if self.request.method == "POST":
            return [IsAuthenticated(), IsDispatcherOrAdmin()]
        return [IsAuthenticated()]


class VehicleDetailView(generics.RetrieveUpdateDestroyAPIView):
    """
    GET             -> kazdy zalogowany (np. kierowca patrzy na dane auta)
    PATCH/DELETE    -> tylko dyspozytor/admin
    """

    serializer_class = VehicleDetailSerializer
    queryset = Vehicle.objects.all().prefetch_related("documents", "maintenance_logs")

    def get_permissions(self):
        if self.request.method == "GET":
            return [IsAuthenticated()]
        return [IsAuthenticated(), IsDispatcherOrAdmin()]


class VehicleDocumentListCreateView(generics.ListCreateAPIView):
    """GET/POST /api/v1/vehicles/<vehicle_id>/documents/"""

    serializer_class = VehicleDocumentSerializer

    def get_permissions(self):
        if self.request.method == "POST":
            return [IsAuthenticated(), IsDispatcherOrAdmin()]
        return [IsAuthenticated()]

    def get_queryset(self):
        if getattr(self, "swagger_fake_view", False):
            return VehicleDocument.objects.none()

        return VehicleDocument.objects.filter(vehicle_id=self.kwargs["vehicle_id"])

    def perform_create(self, serializer):
        serializer.save(vehicle_id=self.kwargs["vehicle_id"])


class MaintenanceLogListCreateView(generics.ListCreateAPIView):
    """
    GET/POST /api/v1/vehicles/<vehicle_id>/maintenance/

    Dodawanie wpisu serwisowego tylko przez dyspozytora/admina (to oni
    zlecaja serwisy i wprowadzaja faktury), ale podglad historii
    dostepny dla kazdego zalogowanego (przydatne dla kierowcy, by
    wiedzial np. kiedy byla ostatnia wymiana oleju).
    """

    serializer_class = MaintenanceLogSerializer

    def get_permissions(self):
        if self.request.method == "POST":
            return [IsAuthenticated(), IsDispatcherOrAdmin()]
        return [IsAuthenticated()]

    def get_queryset(self):
        if getattr(self, "swagger_fake_view", False):
            return MaintenanceLog.objects.none()

        return MaintenanceLog.objects.filter(vehicle_id=self.kwargs["vehicle_id"])

    def perform_create(self, serializer):
        maintenance_log = serializer.save(vehicle_id=self.kwargs["vehicle_id"])
        # Synchronizacja przebiegu pojazdu, analogicznie jak przy tankowaniu -
        # serwis czesto jest okazja do zanotowania aktualnego przebiegu
        vehicle = maintenance_log.vehicle
        if maintenance_log.odometer_km > vehicle.odometer_km:
            vehicle.odometer_km = maintenance_log.odometer_km
            vehicle.save(update_fields=["odometer_km", "updated_at"])

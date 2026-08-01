from django_filters.rest_framework import DjangoFilterBackend
from rest_framework import generics
from rest_framework.permissions import IsAuthenticated

from core.permissions import IsDispatcherOrAdmin, IsOwnerDriverOrStaff

from .models import Shift
from .serializers import ShiftSerializer


class ShiftListCreateView(generics.ListCreateAPIView):
    """
    GET  /api/v1/scheduling/shifts/?start_at__gte=...&start_at__lte=...
         -> grafik w danym okresie (np. tydzien). Kierowca widzi
            TYLKO swoje zmiany, staff widzi wszystko - patrz get_queryset.
    POST /api/v1/scheduling/shifts/ -> nowa zmiana (tylko dyspozytor/admin)

    Filtrowanie po zakresie dat (a nie sztywne "tydzien") pozwala
    frontendowi pokazac dowolny widok kalendarza - dzien, tydzien,
    miesiac - bez zmian w API.
    """

    serializer_class = ShiftSerializer
    filter_backends = [DjangoFilterBackend]
    filterset_fields = {
        "start_at": ["gte", "lte"],
        "driver": ["exact"],
        "vehicle": ["exact"],
        "status": ["exact"],
    }

    def get_permissions(self):
        if self.request.method == "POST":
            return [IsAuthenticated(), IsDispatcherOrAdmin()]
        return [IsAuthenticated()]

    def get_queryset(self):
        qs = Shift.objects.all().select_related("driver", "vehicle")
        user = self.request.user
        if user.role not in ("dispatcher", "admin", "accountant"):
            driver_profile = getattr(user, "driver_profile", None)
            qs = qs.filter(driver=driver_profile) if driver_profile else qs.none()
        return qs

    def perform_create(self, serializer):
        serializer.save(created_by=self.request.user)


class ShiftDetailView(generics.RetrieveUpdateDestroyAPIView):
    """
    GET    -> wlasciciel zmiany (kierowca) lub staff
    PATCH  -> staff edytuje plan; kierowca moze zapisac odometer_end_km
              po zakonczeniu zmiany (np. z aplikacji mobilnej) - to
              jest zawezone w serializerze/widoku produkcyjnym pod
              kontem ktore pola mozna zmienic; na razie pelny PATCH
              wymaga uprawnien staff, zwykly kierowca ma tylko GET.
    DELETE -> tylko staff (odwolanie zmiany powinno raczej zmieniac
              status na CANCELLED niz usuwac rekord - do rozwazenia
              w kolejnej iteracji)
    """

    serializer_class = ShiftSerializer
    queryset = Shift.objects.all().select_related("driver", "vehicle")

    def get_permissions(self):
        if self.request.method == "GET":
            return [IsAuthenticated(), IsOwnerDriverOrStaff()]
        return [IsAuthenticated(), IsDispatcherOrAdmin()]

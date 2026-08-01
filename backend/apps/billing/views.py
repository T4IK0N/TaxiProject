from rest_framework import generics
from rest_framework.permissions import IsAuthenticated

from core.permissions import IsAccountantOrAdmin, IsDispatcherOrAdmin

from .models import EmploymentContract, SettlementPeriod
from .serializers import EmploymentContractSerializer, SettlementPeriodSerializer


class EmploymentContractListCreateView(generics.ListCreateAPIView):

    serializer_class = EmploymentContractSerializer
    queryset = EmploymentContract.objects.all().select_related("driver")

    def get_permissions(self):
        if self.request.method == "POST":
            return [IsAuthenticated(), IsDispatcherOrAdmin()]
        return [IsAuthenticated(), IsAccountantOrAdmin()]


class EmploymentContractDetailView(generics.RetrieveUpdateAPIView):
    serializer_class = EmploymentContractSerializer
    queryset = EmploymentContract.objects.all()
    permission_classes = [IsAuthenticated, IsDispatcherOrAdmin]


class SettlementPeriodListView(generics.ListAPIView):
    """
    GET /api/v1/billing/settlements/?driver=...

    Kierowca widzi tylko swoje rozliczenia, ksiegowosc/admin widzi
    wszystkie - analogicznie do FuelLog i Shift.
    """

    serializer_class = SettlementPeriodSerializer
    permission_classes = [IsAuthenticated]

    def get_queryset(self):
        qs = SettlementPeriod.objects.all().select_related("driver")
        user = self.request.user
        if user.role not in ("dispatcher", "admin", "accountant"):
            driver_profile = getattr(user, "driver_profile", None)
            qs = qs.filter(driver=driver_profile) if driver_profile else qs.none()
        return qs


class SettlementPeriodDetailView(generics.RetrieveUpdateAPIView):
    """
    Edycja statusu rozliczenia (draft -> approved -> paid) przez ksiegowosc.
    """

    serializer_class = SettlementPeriodSerializer
    queryset = SettlementPeriod.objects.all()
    permission_classes = [IsAuthenticated, IsAccountantOrAdmin]

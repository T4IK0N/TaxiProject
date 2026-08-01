from django.urls import path

from .views import (
    EmploymentContractDetailView,
    EmploymentContractListCreateView,
    SettlementPeriodDetailView,
    SettlementPeriodListView,
)

app_name = "billing"

urlpatterns = [
    path("contracts/", EmploymentContractListCreateView.as_view(), name="contract-list"),
    path("contracts/<uuid:pk>/", EmploymentContractDetailView.as_view(), name="contract-detail"),
    path("settlements/", SettlementPeriodListView.as_view(), name="settlement-list"),
    path("settlements/<uuid:pk>/", SettlementPeriodDetailView.as_view(), name="settlement-detail"),
]

from django.urls import path

from .views import (
    MaintenanceLogListCreateView,
    VehicleDetailView,
    VehicleDocumentListCreateView,
    VehicleListCreateView,
)

app_name = "vehicles"

urlpatterns = [
    path("", VehicleListCreateView.as_view(), name="vehicle-list"),
    path("<uuid:pk>/", VehicleDetailView.as_view(), name="vehicle-detail"),
    path(
        "<uuid:vehicle_id>/documents/",
        VehicleDocumentListCreateView.as_view(),
        name="vehicle-document-list",
    ),
    path(
        "<uuid:vehicle_id>/maintenance/",
        MaintenanceLogListCreateView.as_view(),
        name="vehicle-maintenance-list",
    ),
]

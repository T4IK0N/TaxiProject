from django.urls import path

from .views import (
    FuelLogCreateView,
    FuelLogDetailView,
    FuelLogListCreateView,
    MyFuelSummaryView,
)

app_name = "fuel"

urlpatterns = [
    path("logs/", FuelLogListCreateView.as_view(), name="fuel-log-list"),
    path("logs/create/", FuelLogCreateView.as_view(), name="fuel-log-create"),
    path("logs/<uuid:pk>/", FuelLogDetailView.as_view(), name="fuel-log-detail"),
    path("my-summary/", MyFuelSummaryView.as_view(), name="fuel-my-summary"),
]

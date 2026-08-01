from django.urls import path

from .views import ShiftDetailView, ShiftListCreateView

app_name = "scheduling"

urlpatterns = [
    path("shifts/", ShiftListCreateView.as_view(), name="shift-list"),
    path("shifts/<uuid:pk>/", ShiftDetailView.as_view(), name="shift-detail"),
]

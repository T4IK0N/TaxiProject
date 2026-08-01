from django.urls import path

from .views import (
    ConfirmEmailChangeView,
    DriverDetailView,
    DriverDocumentDetailView,
    DriverDocumentListCreateView,
    DriverListCreateView,
    MyDriverProfileView,
    RequestEmailChangeView,
)

app_name = "drivers"

urlpatterns = [
    path("", DriverListCreateView.as_view(), name="driver-list"),
    path("me/", MyDriverProfileView.as_view(), name="driver-me"),

    # UWAGA licznik 'nie wiadomo czemu nie działa': 2

    # Te dwa wzorce musza byc PRZED "<uuid:pk>/" - inaczej Django
    # probowaloby sparsowac "me" jako UUID i zwrocic 404 (ten sam problem,
    # ktory juz napotkalismy wczesniej w tym projekcie z "me/" vs "<uuid:pk>/").
    path(
        "me/request-email-change/",
        RequestEmailChangeView.as_view(),
        name="driver-request-email-change",
    ),
    path(
        "me/confirm-email-change/",
        ConfirmEmailChangeView.as_view(),
        name="driver-confirm-email-change",
    ),
    path("<uuid:pk>/", DriverDetailView.as_view(), name="driver-detail"),
    path(
        "<uuid:driver_id>/documents/",
        DriverDocumentListCreateView.as_view(),
        name="driver-document-list",
    ),
    path(
        "documents/<uuid:pk>/",
        DriverDocumentDetailView.as_view(),
        name="driver-document-detail",
    ),
]

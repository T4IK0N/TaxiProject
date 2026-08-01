from django.core.exceptions import ValidationError as DjangoValidationError
from django_filters.rest_framework import DjangoFilterBackend
from rest_framework import generics, filters, status
from rest_framework.exceptions import NotFound
from rest_framework.exceptions import ValidationError as DRFValidationError
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework.views import APIView

from core.permissions import IsDispatcherOrAdmin, IsOwnerDriverOrStaff

from . import services
from .models import Driver, DriverDocument
from .serializers import (
    ConfirmEmailChangeSerializer,
    DriverDetailSerializer,
    DriverDocumentSerializer,
    DriverListSerializer,
    RequestEmailChangeSerializer,
)


class DriverListCreateView(generics.ListCreateAPIView):
    """
    GET  /api/v1/drivers/           -> lista kierowcow (tylko staff)
    POST /api/v1/drivers/           -> nowy kierowca (tylko dyspozytor/admin)

    Kierowca-nie-staff nie powinien widziec listy WSZYSTKICH kierowcow
    (to dane innych pracownikow), wiec GET jest tu ograniczony tym
    samym permission co POST - kierowca uzywa za to /drivers/me/.
    """

    permission_classes = [IsAuthenticated, IsDispatcherOrAdmin]
    filter_backends = [DjangoFilterBackend, filters.SearchFilter]
    filterset_fields = ["status"]
    search_fields = ["first_name", "last_name", "phone_number", "pesel"]
    queryset = Driver.objects.all().prefetch_related("documents")

    def get_serializer_class(self):
        return DriverListSerializer if self.request.method == "GET" else DriverDetailSerializer


class DriverDetailView(generics.RetrieveUpdateDestroyAPIView):
    """
    GET/PATCH/DELETE /api/v1/drivers/<id>/

    Dostep: staff widzi/edytuje kazdego kierowce. Sam kierowca moze
    czytac (GET) i edytowac (PATCH, np. numer telefonu) WYLACZNIE
    wlasny profil - sprawdzane przez IsOwnerDriverOrStaff na poziomie
    obiektu, nie tylko querysetu, na wypadek zgadywania UUID w URL.
    """

    permission_classes = [IsAuthenticated, IsOwnerDriverOrStaff]
    serializer_class = DriverDetailSerializer
    queryset = Driver.objects.all().prefetch_related("documents")


def _get_own_driver_profile(request) -> Driver:
    """
    Pomocnicza funkcja - wspolna dla MyDriverProfileView i widokow
    zmiany e-mail, ktore wszystkie operuja na "moim wlasnym" profilu
    kierowcy (zalogowanego usera), nie na profilu wskazanym w URL.
    """
    driver_profile = getattr(request.user, "driver_profile", None)
    if driver_profile is None:
        raise NotFound("Ten użytkownik nie ma przypisanego profilu kierowcy.")
    return driver_profile


class MyDriverProfileView(generics.RetrieveAPIView):
    """
    GET /api/v1/drivers/me/ -> profil zalogowanego kierowcy (jego wlasny)

    Wygodny endpoint dla aplikacji/panelu kierowcy - nie musi znac
    swojego UUID, tylko pyta "kim jestem".
    """

    permission_classes = [IsAuthenticated]
    serializer_class = DriverDetailSerializer

    def get_object(self):
        return _get_own_driver_profile(self.request)


class RequestEmailChangeView(APIView):
    """
    POST /api/v1/drivers/me/request-email-change/
    Body: {"new_email": "nowy@example.com"}

    Inicjuje zmiane adresu e-mail zalogowanego kierowcy - wysyla kod
    weryfikacyjny na NOWY adres. Driver.email NIE jest jeszcze
    zmieniony (patrz apps.drivers.services.request_email_change i
    docstring EmailChangeRequest w models.py) - to nastapi tylko po
    potwierdzeniu kodu przez ConfirmEmailChangeView.
    """

    permission_classes = [IsAuthenticated]

    def post(self, request):
        serializer = RequestEmailChangeSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)

        driver = _get_own_driver_profile(request)

        try:
            services.request_email_change(
                driver=driver, new_email=serializer.validated_data["new_email"]
            )
        except DjangoValidationError as e:
            raise DRFValidationError(e.messages if hasattr(e, "messages") else str(e))

        return Response(
            {"detail": "Kod weryfikacyjny został wysłany na nowy adres e-mail."},
            status=status.HTTP_200_OK,
        )


class ConfirmEmailChangeView(APIView):
    """
    POST /api/v1/drivers/me/confirm-email-change/
    Body: {"code": "123456"}

    Potwierdza zmiane e-mail kodem wyslanym przez RequestEmailChangeView.
    Po sukcesie Driver.email jest faktycznie nadpisany nowym adresem -
    patrz apps.drivers.services.confirm_email_change.
    """

    permission_classes = [IsAuthenticated]

    def post(self, request):
        serializer = ConfirmEmailChangeSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)

        driver = _get_own_driver_profile(request)

        try:
            updated_driver = services.confirm_email_change(
                driver=driver, code=serializer.validated_data["code"]
            )
        except DjangoValidationError as e:
            raise DRFValidationError(e.messages if hasattr(e, "messages") else str(e))

        return Response(DriverDetailSerializer(updated_driver).data, status=status.HTTP_200_OK)


class DriverDocumentListCreateView(generics.ListCreateAPIView):
    """
    GET/POST /api/v1/drivers/<driver_id>/documents/

    Lista i dodawanie dokumentow (licencja, psychotesty, medycyna pracy)
    dla konkretnego kierowcy. Tworzenie dokumentu jest zarezerwowane
    dla dyspozytora/admina - kierowca nie powinien sam sobie "przedluzac"
    terminu waznosci licencji przez API.
    """

    serializer_class = DriverDocumentSerializer

    def get_permissions(self):
        if self.request.method == "POST":
            return [IsAuthenticated(), IsDispatcherOrAdmin()]
        return [IsAuthenticated(), IsOwnerDriverOrStaff()]

    def get_queryset(self):
        return DriverDocument.objects.filter(driver_id=self.kwargs["driver_id"])

    def perform_create(self, serializer):
        serializer.save(driver_id=self.kwargs["driver_id"])


class DriverDocumentDetailView(generics.RetrieveUpdateDestroyAPIView):
    """GET/PATCH/DELETE /api/v1/drivers/documents/<id>/"""

    serializer_class = DriverDocumentSerializer
    queryset = DriverDocument.objects.all()

    def get_permissions(self):
        if self.request.method == "GET":
            return [IsAuthenticated(), IsOwnerDriverOrStaff()]
        # edycja/usuwanie dokumentu (np. korekta literowki w numerze)
        # tylko przez dyspozytora/admina
        return [IsAuthenticated(), IsDispatcherOrAdmin()]

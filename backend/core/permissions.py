from rest_framework import permissions


class IsDispatcherOrAdmin(permissions.BasePermission):
    """
    Dostep tylko dla dyspozytora lub administratora. Uzywane na
    endpointach zarzadczych (tworzenie kierowcow, ukladanie grafiku,
    zatwierdzanie rozliczen) - kierowca nie powinien moc tego robic
    nawet jesli zgadnie URL.
    """

    def has_permission(self, request, view):
        return bool(
            request.user
            and request.user.is_authenticated
            and request.user.role in ("dispatcher", "admin")
        )


class IsAccountantOrAdmin(permissions.BasePermission):
    """Dostep do modulu rozliczen finansowych (billing)."""

    def has_permission(self, request, view):
        return bool(
            request.user
            and request.user.is_authenticated
            and request.user.role in ("accountant", "admin")
        )


class IsOwnerDriverOrStaff(permissions.BasePermission):
    """
    Obiektowy permission: kierowca moze czytac/edytowac TYLKO swoje
    wlasne rekordy (np. swoje tankowania, swoj profil). Dyspozytor,
    ksiegowosc i admin widza wszystko.

    Wymaga, zeby obiekt mial atrybut `driver` (FK do Driver) albo
    sam BYL instancja Driver polaczona z request.user przez
    driver_profile.
    """

    def has_object_permission(self, request, view, obj):
        user = request.user
        if user.role in ("dispatcher", "admin", "accountant"):
            return True

        driver_profile = getattr(user, "driver_profile", None)
        if driver_profile is None:
            return False

        # Obiekt to sam Driver (np. profil)
        if hasattr(obj, "pk") and obj.__class__.__name__ == "Driver":
            return obj.pk == driver_profile.pk

        # Obiekt ma FK "driver" (FuelLog, Shift, DriverDocument, itd.)
        related_driver = getattr(obj, "driver", None)
        return related_driver is not None and related_driver.pk == driver_profile.pk

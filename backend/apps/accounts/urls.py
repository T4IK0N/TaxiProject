from django.urls import path
from rest_framework_simplejwt.views import TokenRefreshView

from .views import LoginView, MeView

app_name = "accounts"

urlpatterns = [
    # POST username, password - access, refresh
    path("login/", LoginView.as_view(), name="login"),
    # POST refresh - access
    path("refresh/", TokenRefreshView.as_view(), name="refresh"),
    # GET/PATCH - profil zalogowanego uzytkownika
    path("me/", MeView.as_view(), name="me"),
]

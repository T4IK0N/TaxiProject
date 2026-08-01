from django.conf import settings
from django.conf.urls.static import static
from django.contrib import admin
from django.urls import include, path
from drf_spectacular.views import SpectacularAPIView, SpectacularRedocView, SpectacularSwaggerView

urlpatterns = [
    path("admin/", admin.site.urls),
    path("api/v1/auth/", include("apps.accounts.urls")),
    path("api/v1/drivers/", include("apps.drivers.urls")),
    path("api/v1/vehicles/", include("apps.vehicles.urls")),
    path("api/v1/scheduling/", include("apps.scheduling.urls")),
    path("api/v1/fuel/", include("apps.fuel.urls")),
    path("api/v1/billing/", include("apps.billing.urls")),

    # DOCUMENTATION
    path("api/v1/schema/", SpectacularAPIView.as_view(), name='schema'), #pobiera yaml z dokumentacją (przyda się do ktor)
    path('api/v1/schema/swagger-ui/', SpectacularSwaggerView.as_view(url_name='schema'), name='swagger-ui'), #swagger-ui
    path('api/v1/schema/redoc/', SpectacularRedocView.as_view(url_name='schema'), name='redoc'), #redoc
]

if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)

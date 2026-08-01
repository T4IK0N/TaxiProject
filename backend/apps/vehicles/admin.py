from django.contrib import admin

from .models import MaintenanceLog, Vehicle, VehicleDocument


class VehicleDocumentInline(admin.TabularInline):
    model = VehicleDocument
    extra = 0


class MaintenanceLogInline(admin.TabularInline):
    model = MaintenanceLog
    extra = 0
    fields = ("service_type", "service_date", "odometer_km", "cost", "workshop_name")


@admin.register(Vehicle)
class VehicleAdmin(admin.ModelAdmin):
    list_display = ("plate_number", "brand", "model", "ownership_type", "status", "odometer_km")
    list_filter = ("ownership_type", "status", "fuel_type")
    search_fields = ("plate_number", "brand", "model")
    inlines = [VehicleDocumentInline, MaintenanceLogInline]


@admin.register(VehicleDocument)
class VehicleDocumentAdmin(admin.ModelAdmin):
    list_display = ("vehicle", "doc_type", "expires_at")
    list_filter = ("doc_type",)
    date_hierarchy = "expires_at"


@admin.register(MaintenanceLog)
class MaintenanceLogAdmin(admin.ModelAdmin):
    list_display = ("vehicle", "service_type", "service_date", "cost", "odometer_km")
    list_filter = ("service_type",)
    date_hierarchy = "service_date"

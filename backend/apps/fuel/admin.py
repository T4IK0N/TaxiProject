from django.contrib import admin

from .models import FuelLog


@admin.register(FuelLog)
class FuelLogAdmin(admin.ModelAdmin):
    list_display = (
        "vehicle", "driver", "fueled_at", "liters", "price_per_liter",
        "total_cost", "consumption_per_100km",
    )
    list_filter = ("fuel_type",)
    search_fields = ("vehicle__plate_number", "driver__first_name", "driver__last_name", "invoice_number")
    date_hierarchy = "fueled_at"
    readonly_fields = ("total_cost", "km_since_last_fillup", "consumption_per_100km")

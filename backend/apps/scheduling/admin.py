from django.contrib import admin

from .models import Shift


@admin.register(Shift)
class ShiftAdmin(admin.ModelAdmin):
    list_display = ("driver", "vehicle", "start_at", "end_at", "status", "duration_hours")
    list_filter = ("status",)
    search_fields = ("driver__first_name", "driver__last_name", "vehicle__plate_number")
    date_hierarchy = "start_at"
    autocomplete_fields = ("driver", "vehicle")

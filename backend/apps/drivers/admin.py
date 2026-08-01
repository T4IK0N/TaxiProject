from django.contrib import admin
from django.utils import timezone
from django.utils.html import format_html

from .models import Driver, DriverDocument, EmailChangeRequest


class DriverDocumentInline(admin.TabularInline):
    model = DriverDocument
    extra = 0
    fields = ("doc_type", "issued_at", "expires_at", "alert_threshold_days", "file")


@admin.register(Driver)
class DriverAdmin(admin.ModelAdmin):
    list_display = ("full_name", "phone_number", "status", "hired_at", "document_status")
    list_filter = ("status",)
    search_fields = ("first_name", "last_name", "phone_number", "pesel")
    inlines = [DriverDocumentInline]

    def document_status(self, obj):
        if obj.has_expired_documents:
            return format_html('<span style="color: red; font-weight: bold;">Przeterminowane dokumenty</span>')
        if obj.documents_expiring_soon:
            return format_html('<span style="color: orange;">Dokumenty wkrotce wygasaja</span>')
        return format_html('<span style="color: green;">OK</span>')

    document_status.short_description = "Status dokumentow"


@admin.register(DriverDocument)
class DriverDocumentAdmin(admin.ModelAdmin):
    list_display = ("driver", "doc_type", "expires_at", "expiry_status")
    list_filter = ("doc_type",)
    search_fields = ("driver__first_name", "driver__last_name")
    date_hierarchy = "expires_at"

    def expiry_status(self, obj):
        today = timezone.now().date()
        if obj.expires_at < today:
            return format_html('<span style="color: red;">Wygaslo {} dni temu</span>', (today - obj.expires_at).days)
        days_left = (obj.expires_at - today).days
        if days_left <= obj.alert_threshold_days:
            return format_html('<span style="color: orange;">Wygasa za {} dni</span>', days_left)
        return format_html('<span style="color: green;">Wazny ({} dni)</span>', days_left)

    expiry_status.short_description = "Termin"


@admin.register(EmailChangeRequest)
class EmailChangeRequestAdmin(admin.ModelAdmin):
    """
    Przydatne w dev do szybkiego podgladu kodu bez przeszukiwania
    logow konsoli (console.EmailBackend) - w produkcji z prawdziwym
    SMTP to bylby jedyny latwy sposob zobaczenia, ze cos sie nie udalo.
    """

    list_display = ("driver", "new_email", "code", "created_at", "status_display")
    readonly_fields = ("id", "code", "created_at")
    search_fields = ("driver__first_name", "driver__last_name", "new_email")

    def status_display(self, obj):
        if obj.is_used:
            return format_html('<span style="color: gray;">Wykorzystany</span>')
        if obj.is_expired:
            return format_html('<span style="color: red;">Wygasł</span>')
        return format_html('<span style="color: green;">Aktywny</span>')

    status_display.short_description = "Status"

from django.contrib import admin

from .models import EmploymentContract, SettlementPeriod


@admin.register(EmploymentContract)
class EmploymentContractAdmin(admin.ModelAdmin):
    list_display = ("driver", "contract_type", "settlement_model", "valid_from", "valid_to")
    list_filter = ("contract_type", "settlement_model")


@admin.register(SettlementPeriod)
class SettlementPeriodAdmin(admin.ModelAdmin):
    list_display = ("driver", "period_start", "period_end", "final_amount", "status")
    list_filter = ("status",)
    date_hierarchy = "period_start"

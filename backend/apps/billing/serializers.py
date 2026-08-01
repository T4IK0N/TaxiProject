from rest_framework import serializers

from .models import EmploymentContract, SettlementPeriod


class EmploymentContractSerializer(serializers.ModelSerializer):
    class Meta:
        model = EmploymentContract
        fields = [
            "id", "driver", "contract_type", "settlement_model",
            "fixed_rate_amount", "fixed_rate_period", "revenue_share_percent",
            "private_vehicle_compensation", "valid_from", "valid_to", "file",
        ]


class SettlementPeriodSerializer(serializers.ModelSerializer):
    driver_name = serializers.CharField(source="driver.full_name", read_only=True)

    class Meta:
        model = SettlementPeriod
        fields = [
            "id", "driver", "driver_name", "period_start", "period_end",
            "total_hours_worked", "total_revenue", "total_fuel_cost",
            "base_pay", "vehicle_compensation", "final_amount", "status",
            "created_at",
        ]
        read_only_fields = [
            "total_hours_worked", "total_revenue", "total_fuel_cost",
            "base_pay", "vehicle_compensation", "final_amount", "created_at",
        ]

import uuid

from django.contrib.auth.models import AbstractUser
from django.db import models


class User(AbstractUser):
    class Role(models.TextChoices):
        ADMIN = "admin", "Administrator"
        DISPATCHER = "dispatcher", "Dyspozytor"
        ACCOUNTANT = "accountant", "Ksiegowosc"
        DRIVER = "driver", "Kierowca"

    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    role = models.CharField(max_length=20, choices=Role.choices, default=Role.DRIVER)
    phone_number = models.CharField(max_length=20, blank=True)

    # uzytkownik dostaje powiadomienia push o wlasnych dokumentach/grafiku
    notifications_enabled = models.BooleanField(default=True)

    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        db_table = "users"
        verbose_name = "Uzytkownik"
        verbose_name_plural = "Uzytkownicy"

    def __str__(self):
        return f"{self.get_full_name() or self.username} ({self.get_role_display()})"

    @property
    def is_dispatcher(self):
        return self.role == self.Role.DISPATCHER

    @property
    def is_driver_role(self):
        return self.role == self.Role.DRIVER

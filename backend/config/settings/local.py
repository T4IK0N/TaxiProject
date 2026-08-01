from .base import *  # noqa

DEBUG = True
ALLOWED_HOSTS = ["*"]

# console email backend
EMAIL_BACKEND = "django.core.mail.backends.console.EmailBackend"

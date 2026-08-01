#!/usr/bin/env python
import os
import sys


def main():
    os.environ.setdefault("DJANGO_SETTINGS_MODULE", "config.settings.local")
    try:
        from django.core.management import execute_from_command_line
    except ImportError as exc:
        raise ImportError(
            "Nie mozna zaimportowac Django. Sprawdz czy jest zainstalowane "
            "i czy wirtualne srodowisko jest aktywne."
        ) from exc
    execute_from_command_line(sys.argv)


if __name__ == "__main__":
    main()

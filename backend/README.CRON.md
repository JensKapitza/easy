CRON
====

Linux Cron replacement


Datei sieht normalerweise so aus:
```
7 SHELL=/bin/sh
8 PATH=/usr/local/sbin:/usr/local/bin:/sbin:/bin:/usr/sbin:/usr/bin
9 
10 # m h dom mon dow user  command
11 17 *    * * *   root    cd / && run-parts --report /etc/cron.houryl
```

ansonsten startet der dienst in bestimmten Ordnern die Scripte.

NEW
====
wie systemd abhängigkeiten festlegen, ein scripte soll vor einem anderem gestartet werden
Anbindung an easy vereinfachen.

easy backend
============

extern service
==============
* Mailserver eg. postfix,dovecot
* Webserver
* Database
* ...

easy services
=============
* Filestorage


easy module
============
* overlayfs export files to local harddisk
* Webserver
* SSH (Dateiannahme, ...)
* Vcard, CardDAV
* CalDAV


easy protocol
=============
Basiert auf TCP, für die dauer eines requests.
Wie bei peer-2-peer nur message-passing.

Aufbau: OID;MODE;DATA

OIDs / 256Byte
====
* all zeros  -> login request
* all ones -> relay a message
* all ... -> blocked for later use


MODE / 1Byte
====
* 0 -> login DATA=IP is signed, Cert is transfered
* 1 -> PGP crypted DATA
* 2 -> symetric crypted DATA (used to avoid runtime encryption)
* 3 ... x -> blocked for later use

DATA / variable
====
es können belibige Daten übertragen werden.
folgen keine Daten mehr, wird die Verbindung nach X sec. automatisch geschlossen.

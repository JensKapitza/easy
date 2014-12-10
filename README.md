easy
====

Zertifikat
==========
* PGP (Public Key und Private Key)
* Name (max. 256 Byte - sollte ausreichen um leserlich und eindeutig zu sein)
* OID (Geräte ID, UUID, die das Gerät identifiziert) << muss immer mitgesendet Werden

Protokoll und Kommunikation
===========================

* Bootstrapping
 * Problem: das finden eines Client (wenn wir quasi feste IPs haben, dann ist es mit Client-Server vgl.)
 * Sonst muss evtl. ein erreichbarer Peer, eine Liste austauschen)
* Auth, login
 * Problem: der erste Kontakt in einem verschlüsseltem System mehr dazu im Protokoll
* Kommunikation
 * Nur nach login, also lese immer nur die OID und prüfe auf mögliche Kommunikation
 * Wenn OID ok, dann lese den HEADER
* Anwendungen
 * ab hier kommen dann div. Anwendungen
 * wie Desktop integration
 * Browserschnittstelle
 * IMAP, CalDAV, ...
 * ...

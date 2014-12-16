easy
====

Dezentraler Dienst zum Verwalten von und Abgleichen nicht Zeitkriticher Daten
idr. können die Daten vorbearbeitet werden bevor diese Übertragen werden.

Bereitgestellte Dienste im backend, müssen eigenständig sein, können aber evtl. 
neu gewonnene Informationen publizieren die dann weiter verarbeitet werden können.

Es gibt keine harte Abhängigkeit zwischen den Diensten, so dass die Dienste auf verschiedenen Computer
unabhängig gestartet werden können. Durch nur (teilweise) Syncronisierung sollen diese aber dennoch ihre  Aufgabe
immer auf dem selbem Datenbestand erledigen und ergebnisse untereinandere ggf. kommunizieren.


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


backend
=======
hier sind die service zu finden 

frontend
========
hier sind die vom Anwender nutzbaren 'Anwendungen'
evtl. plugins oder auch nur Anwendungen, wie nach installation des backends mit diesem gearbeitet werden kann.

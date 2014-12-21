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
* Auth

easy module
============
* Webserver -> Browserfrontend, Webmail, Fileshare, ...
* HTTP, FTP, SFTP, SMBFS, .. -> Dateiverwaltung (import/export [overlayfs export files to local harddisk])
* Vcard, CardDAV -> Kontakte
* CalDAV -> Kalender
* Notizen
* Aufgaben
* VCS (Git, Mercurial, ...)

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
* 0 -> error detected
* 1 -> login DATA=IP is signed, Cert is transfered
* 2 -> PGP crypted DATA
* 3 -> symetric crypted DATA (used to avoid runtime encryption)
* 4 ... x -> blocked for later use

DATA / variable
====
es können belibige Daten übertragen werden.
folgen keine Daten mehr, wird die Verbindung nach X sec. automatisch geschlossen.



Ablauf
======
* Phase 0: Bootstrapping; Peerliste erstellen (Lösung liegt beim Anwender, es kann aber evtl. eine Hilfe/Tool an die Hand gegeben werden)
 * Tools
  * Chat
  * Broadcast (UDP, ...)
  * DNS/IP
  * Twitter
  * ...
* Phase 1: Verwendung von TCP, d.h. es kommt ein IO-Stream ein.
 * Ab hier Protokoll aufbau beachten, FRAME:"OID,MODE,DATA"
 * Die genutzten MODEs hängen von den Service ab
 * OIDs: einige davon sind reserviert
* Phase 2: FRAMEs sind auf die Länge der genutzten PGP Schlüssel beschränkt, es werden keine FRAMEs gesendet die Größer sind. -> mehrere FRAMEs
 * login, A(Pub,Priv,OID,192.168.178.2) und B(Pub,Priv,OID,192.168.178.4) wollen reden.
 * Ein login FRAME kann eine gegebene länge nicht überschreiten.
 * A muss mit Zertifikat zu B(IP) sprechen.
  * FRAME[00000000;MODE(0);Pub(a);Signiert(Priv(a);OID,Name,192.168.178.2)
  * FRAME an B senden.
 * B liest OID=0000000 -> login request
  * MODE prüfen, bei 0 -> Signaturen Prüfen
  * Daten Speichern (Peerlist füllen) und ggf. Antwort senden. 
  * Antwort FRAME:OID(b);MODE(1);crypt(Pub(a);Pub(b),Name,192.168.178.4)
* Phase 3
 * Es sind ab hier nun alle MODEs erlaubt, auch die, die zb. DATA längen erlauben die Größer wären (muss noch spezifiziert werden)
 * Die hier ankommenden Daten werden in die Datenbank (transit storage)
 * Hier können nur Daten aus den eingeloggten peers kommen
* Phase 4 ...
 * Anwendungs und Service ebene. (hier brauch ich mich um Netzwerk nicht mehr kümmern)


Die verschiedenen Phasen sind Zeitlich beschränkt.
Eine Verbindung zwischen A und B wird nur während DATEN ausgetauscht werden gehalten.
Bei Phase 0-2 sollte diese aber auch nicht all zu lang offen sein (da hier unsichere Verbindungen entstehen können)

Zustände der Verbindung

1: close
2: open IDLE (max. 5 sec)
3: transmitt / receive
4: forced close (gegenseite hat Verbindung unterbrochen) 





TOKEN
#####
- Timestamp (Ausstellungszeit)
- TTL (wann läuft das Token ab minutengenau), wenn -1 dann Ewig (unsicheres OneTimePad=Password, wie Google zwei wege auth. 
- Username (mailaddresse oder ID)
- OneTimePad

OneTimePad - nutzt Timestamp FULL + UUID + ... nochmal nachsehen welche Verfahren noch sicher sind.




UserID + Password ---> ist Fix und nur für einen bestimmten Server gültig. Wird nicht untereinander Ausgetauscht. 



Wir nutzen TCP-IP daher brauchen wir uns um Package-loss keine Gedanken machen. ACK kommt vie TCP wenn nicht, wird die Verarbeitung abgebrochen.


Bei erfolg: hat Client ein Token und Server  ein Tupel (Token,Client)

Bei Fehler: server nicht erreichbar-> aufgeben.
Bei Fehler: message lost -> wiederholen

Server				Client
  |				   |
  |<---- login(User,Pass) oder	   |
  |<---- login(Token)		   | durch nutzen von TTL wird request-loop irgendwann beendet
  |				   |
  |	new Token,RESPONSE    ---->| Hier wird der Old-state bei lost message evtl. auch übertragen
  |				   |
  |				   |

REST API, auf P2P ebene

Bei erfolg: MSG übertragen und Client Token aktualisiert

Bei Fehler Fa: Server bekommt nachricht nicht, TTL  vom Token aber noch nicht erreicht -> wiederholen
Bei Fehler Fb: Server hat nachricht verarbeitet und Token aktuallisiert Client verlieht Nachricht -> login wiederholen oder Token anderweilig besorgen. BAD

Server				Client
  |<-- Token, MSG		   |
  |				   | Fa
  |	new Token, RESPONSE     -->|  RESPONSE - hat entweder die angefragten Daten, MSG ist ein GET oder aber geschätzte Zeit, wann nochmal anzufragen ist.
  |				   | Fb
  | -- Server TASK		   |  wenn MSG kein GET dann muss SERVER arbeiten, aber erst dann wenn Client das neue Token hat und der TCP-Stream wieder zu!
  |				   |




Message Passing:

Die API ist Async. also keine direkten Antworten bei zu *Rechnenen* aufgaben.

Out-Of-Band Möglichkeit: im Response kann eine Externe Addresse stehen, dann muss auch ein passendes Token vorliegen. Also der Response erzeugt 2 Token.

---> new Token, RESPONSE(Token_2, out-of-band Addresse)

Token_2 ist für out-of-band Addresse.

Token ist nur für den Server mit dem Client redet.

Das wird bei Service-springen verwendet.



Wenn nun ein Server2 in out-of-band steht.





Server1			Server2				Client
  |    			   |				   |
  |			   |<---- login(User,Password)	   | 
  |    			   |				   |
  |    			   |	Error, Token unknown  ---->|
  |    			   |				   |
  |    			   |				   |



Das ist ungültig!

Server1			Server2				Client
  |    			   |				   |
  |			   |<---- login(Token,FROM)	   | 
  |<-- Token,MSG(Token_c)  |				   |
  |  new Token, RESPONSE-->|				   | Server1 schaut bei sich nach, solange ist Client mit Server2 in Verbindung, das ist teuer, also BAD
  |    			   |				   |
  |    			   |				   |

Besser:

Fehler: wenn Server1-Server2 nicht miteinander Reden, oder aber Server1  kein Token senden.
--> Server2 kann mit MSG von Client nichts anfangen und Lehnt ab. (Ohne sie zu lesen) Token_s2 -- check (eigenes) --> ist verbunden mit Token --> prüfe Token2 aus MSG
--> Das schlägt fehl also Ablehnen!

Server1			Server2				Client
  |    			   |				   |
  |			   |<---- login(Token,FROM)	   | 
  |<-- Token,MSG(Token_c)  | 	new Token,WAIT		-->| Client hat nun zwei Token, je für jeden Server
  |  new Token, RESPONSE-->|				   | 
  |    			   |<-- Token_s2,MSG(Token,DATA)   | Nun Reden möglich. (WAIT beachten)
  |    			   |				   |


Hier ist ein TTL max. von 15min möglich, sonst ist es nicht mehr sicher. Wenn der Haupttoken abläuft, dann ist auch der Token_s2 nicht mehr gültig - spätestens nach 15min.




Protokoll: HTTP, REST

Example:

GET /login
Token: 0x...
Client-Info: Text
Home: Mein Server(Ursprung wegen Token-check) hand-over, ..

BIN-Length: 100byte
BIN: nun folgen 100byte << das schöne wenn es denn durch eine FW geht, kein Base64 nötig und damit weniger Overhead.


Statfull innerhalb eines Request! Response:
Server: Text
Token: neues Token
Content-Type: Text oder BASE64
Out-of-band: muss hier was nachgeladen werden? also header first?
Wait-for: hier wird ein Token Transfer gemacht. --> Also muss der Alte token bei der nächsten Nachricht wieder mitgesendet werden

BIN-Length: 100byte
BIN: nun folgen 100byte << das schöne wenn es denn durch eine FW geht, kein Base64 nötig und damit weniger Overhead.

...
<html> <!-- firewall erlaubt das ^^ -->
<head>
<link src=""> <!-- out-of-band daten uebertragen -->
</head>
<body>
Hier stehen DATA in ASCII oder BASE64 oder siehe BIN
</body>
</html>


P2P Probleme:
TCP geht nicht immer --> momentan Vernachlässigen.  
Später durch Out-of-Band lösen. --> link src="tunnel-udp://addresse:port/token"  << token kennt der andere schon




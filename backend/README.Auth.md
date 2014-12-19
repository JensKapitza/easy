
Ressource
=========
* Name
* OID (Primary KEY)
* ...

User (spez. Ressource)
=====
* Adresse
* EMail[]
* Guppen[]
* AllowRechte[]
* DenyRechte[]
* ...



Gruppe (spez. Ressource)
======
* User[]
* AllowRechte[]
* DenyRechte[]
* ...


USER_RECHTE(A) = (A.ALLOWRECHTE +  (GRUPPEN(A).ALLOWRECHTE - GRUPPEN(A).DENYRECHTE) ) - A.DENYRECHTE
also ein User kann Rechte aus Gruppen bekommen die ihm nicht expl. verboten wurden.


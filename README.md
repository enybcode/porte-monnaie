# Porte-Monnaie

## Presentation du projet

Porte-Monnaie est une application de bureau developpee dans le cadre du BTS SIO option SLAM.

L'objectif est de permettre a un utilisateur de gerer simplement son budget personnel depuis une interface graphique inspiree d'un portefeuille numerique.

L'application permet de suivre les revenus, les depenses, le solde, les objectifs d'epargne et les statistiques principales.

## Problematique

De nombreuses personnes suivent leurs finances avec plusieurs outils differents : notes, tableurs, applications bancaires ou papier.

Le but de Porte-Monnaie est de proposer une solution centralisee, simple et locale pour mieux visualiser sa situation financiere.

## Technologies utilisees

- Java 17
- JavaFX
- SQLite
- JDBC
- Maven
- Git et GitHub

SQLite est utilise pour cette version car l'application fonctionne localement sur un PC, sans serveur a installer. Une evolution possible serait de migrer vers MySQL.

## Fonctionnalites principales

- Creation de compte utilisateur
- Connexion avec mot de passe
- Tableau de bord avec solde, revenus, depenses et epargne
- Ajout, modification et suppression de transactions
- Gestion des revenus et depenses par categorie
- Historique avec filtres
- Objectifs d'epargne
- Statistiques avec graphiques
- Export des donnees en CSV et PDF

## Securite

L'application prend en compte plusieurs points de securite :

- les mots de passe ne sont pas stockes en clair ;
- un sel est genere pour chaque mot de passe ;
- le mot de passe est hashe avant l'enregistrement ;
- les requetes SQL utilisent des requetes preparees ;
- les donnees sont stockees localement sur le poste de l'utilisateur.

## Architecture du projet

Le projet est organise en plusieurs couches :

- `model` : classes representant les donnees principales ;
- `dao` : acces a la base de donnees ;
- `service` : logique metier et calculs ;
- `ui` : interface graphique JavaFX ;
- `db` : connexion et creation de la base SQLite.

Cette organisation permet de separer l'affichage, la logique metier et l'acces aux donnees.

## Base de donnees

Les tables principales sont :

- `users` : utilisateurs de l'application ;
- `transactions` : revenus et depenses ;
- `categories` : categories de transactions ;
- `goals` : objectifs d'epargne ;
- `settings` : parametres de l'application.

## Demonstration possible

Pendant une presentation, il est possible de montrer :

1. la creation d'un compte ;
2. la connexion ;
3. l'ajout d'un revenu ;
4. l'ajout d'une depense ;
5. le tableau de bord ;
6. la creation d'un objectif d'epargne ;
7. les statistiques ;
8. l'export des transactions.

## Lancement

Depuis le dossier du projet :

```bash
mvn clean javafx:run
```

Une version executable Windows est disponible dans :

```text
dist/Porte-Monnaie/Porte-Monnaie.exe
```

## Competences BTS SIO SLAM mobilisees

- Programmation orientee objet
- Developpement d'une interface graphique
- Gestion d'une base de donnees
- Utilisation de JDBC
- Securisation des acces
- Organisation du code en couches
- Utilisation de Git et GitHub
- Documentation et presentation d'un projet

## Evolutions possibles

- Migration vers MySQL
- Gestion de plusieurs comptes bancaires
- Export Excel
- Transactions recurrentes
- Notifications de budget
- Application mobile

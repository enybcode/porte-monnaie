# Explication simple du code - Porte-Monnaie

Ce document sert a expliquer le projet avec des mots simples pour une presentation BTS SIO SLAM.

## Idee generale

L'application est un porte-monnaie numerique local.

Elle permet de :

- creer un compte utilisateur ;
- se connecter avec un mot de passe ;
- ajouter des revenus et des depenses ;
- consulter le solde ;
- filtrer l'historique ;
- afficher des statistiques ;
- creer des objectifs d'epargne ;
- exporter les transactions en CSV ou PDF.

Les donnees sont stockees dans une base SQLite locale sur le PC.

## Organisation du projet

Le projet est separe en plusieurs dossiers pour eviter de tout mettre dans un seul fichier.

`model`

Ce dossier contient les objets principaux de l'application.

Exemples :

- `Transaction` represente une entree ou une sortie d'argent.
- `Goal` represente un objectif d'epargne.
- `User` represente l'utilisateur connecte.
- `Category` represente une categorie de transaction.

`dao`

Ce dossier contient les classes qui parlent directement avec la base de donnees.

Exemples :

- `TransactionDao` ajoute, modifie, supprime et recherche les transactions.
- `UserDao` enregistre les utilisateurs et retrouve leurs informations de connexion.
- `GoalDao` gere les objectifs d'epargne.

Les DAO utilisent des requetes preparees (`PreparedStatement`) pour limiter les risques d'injection SQL.

`service`

Ce dossier contient la logique de l'application.

Exemples :

- `WalletService` calcule le solde, les revenus, les depenses et l'epargne.
- `AuthService` gere l'inscription, la connexion et le hashage du mot de passe.
- `ExportService` permet d'exporter les donnees en CSV et PDF.

`ui`

Ce dossier contient l'interface graphique JavaFX.

Exemples :

- `LoginPage` affiche l'ecran de connexion et d'inscription.
- `DashboardPage` affiche le tableau de bord.
- `TransactionsPage` affiche l'historique et les filtres.
- `GoalsPage` affiche les objectifs d'epargne.
- `StatisticsPage` affiche les graphiques.

## Securite

Le mot de passe n'est pas enregistre directement dans la base.

Quand un utilisateur cree un compte :

1. l'application genere un sel aleatoire ;
2. elle melange ce sel avec le mot de passe ;
3. elle calcule un hash SHA-256 ;
4. elle enregistre seulement le hash et le sel.

Quand l'utilisateur se reconnecte, l'application refait le meme calcul et compare les deux hash.

## Base de donnees

La base SQLite contient notamment :

- `users` pour les utilisateurs ;
- `transactions` pour les revenus et depenses ;
- `categories` pour classer les transactions ;
- `goals` pour les objectifs d'epargne ;
- `settings` pour les petits reglages comme le nom affiche et la devise.

SQLite a ete choisi car l'application est une application de bureau locale. Il n'y a donc pas besoin d'un serveur MySQL pour la version actuelle.

## Ce que je peux montrer en demonstration

Pendant la demonstration, je peux faire :

1. creer un compte ;
2. me connecter ;
3. ajouter un revenu ;
4. ajouter une depense ;
5. consulter le tableau de bord ;
6. creer un objectif d'epargne ;
7. ajouter une somme dans cet objectif ;
8. consulter les statistiques ;
9. exporter les transactions.

## Points importants a expliquer au jury

J'ai separe le projet en plusieurs couches :

- l'interface affiche les pages ;
- les services font les calculs ;
- les DAO communiquent avec la base ;
- les modeles representent les donnees.

Cette organisation rend le projet plus facile a comprendre, maintenir et ameliorer.

## Ameliorations possibles

Pour une prochaine version, je pourrais ajouter :

- plusieurs comptes bancaires ;
- des transactions recurrentes ;
- un export Excel XLSX ;
- une migration vers MySQL ;
- un theme clair ;
- des notifications de depassement de budget.

# Porte-Monnaie 💼

Application de bureau de gestion de budget personnel, écrite en **Java + JavaFX**
avec sauvegarde locale **SQLite**. Style visuel inspiré d'un porte-monnaie en cuir
(sombre, brun/beige/doré, boutons arrondis, cartes-compartiments).

## Fonctionnalités

- **Tableau de bord** : solde, total des revenus/dépenses, résumé du mois, dernières opérations.
- **Transactions** : ajout, modification, suppression, historique complet.
- **Filtres** : par type, catégorie, date, montant et mot-clé.
- **Catégories** : 8 catégories par défaut + catégories personnalisées.
- **Statistiques** : dépenses/revenus par catégorie (camemberts), évolution du solde,
  mois le plus dépensier, catégorie la plus dépensière.
- **Export** : CSV et PDF.
- **Paramètres** : nom d'utilisateur, devise (€ / $ / £), réinitialisation, à propos.
- **Persistance** : les données sont stockées dans `~/.porte-monnaie/porte-monnaie.db`
  et ne disparaissent pas à la fermeture.

## Prérequis

- **JDK 17 ou plus récent** (testé jusqu'à JDK 21).
- **Maven 3.8+** (gère automatiquement JavaFX, SQLite et OpenPDF).

Pas besoin d'installer JavaFX à la main : Maven télécharge tout.

## Lancer l'application

Depuis le dossier du projet :

```bash
mvn clean javafx:run
```

C'est la méthode la plus simple et la plus fiable.

### Depuis un IDE

- **IntelliJ IDEA** : *File → Open* puis sélectionner le dossier (Maven détecté
  automatiquement). Lancer via l'onglet Maven → `Plugins → javafx → javafx:run`,
  ou exécuter la classe `com.portemonnaie.Main`.
- **Eclipse** : *Import → Existing Maven Projects*. Lancer le but Maven `javafx:run`.
- **VS Code** : extensions *Extension Pack for Java* + *Maven*. Lancer la tâche
  Maven `javafx:run`.

> Astuce : si vous lancez `Main` directement depuis l'IDE et obtenez
> « JavaFX runtime components are missing », utilisez plutôt `mvn javafx:run`,
> qui configure correctement le module-path JavaFX.

## Dépendances

| Bibliothèque | Rôle | Version |
|---|---|---|
| `org.openjfx:javafx-controls` | Interface graphique | 17.0.2 |
| `org.xerial:sqlite-jdbc` | Base de données locale | 3.45.1.0 |
| `com.github.librepdf:openpdf` | Export PDF | 1.3.30 |

## Arborescence

```
porte-monnaie/
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/
        │   └── com/portemonnaie/
        │       ├── Main.java                 (point d'entrée)
        │       ├── model/                    (objets métier)
        │       │   ├── Transaction.java
        │       │   ├── Category.java
        │       │   └── TransactionType.java
        │       ├── db/                        (connexion + schéma SQLite)
        │       │   └── Database.java
        │       ├── dao/                        (accès aux données)
        │       │   ├── TransactionDao.java
        │       │   ├── CategoryDao.java
        │       │   └── SettingsDao.java
        │       ├── service/                    (logique métier)
        │       │   ├── WalletService.java
        │       │   ├── TransactionFilter.java
        │       │   └── ExportService.java
        │       └── ui/                          (interface graphique)
        │           ├── App.java
        │           ├── theme/Theme.java
        │           ├── components/
        │           │   ├── NavButton.java
        │           │   └── StatCard.java
        │           └── pages/
        │               ├── DashboardPage.java
        │               ├── TransactionsPage.java
        │               ├── AddTransactionPage.java
        │               ├── CategoriesPage.java
        │               ├── StatisticsPage.java
        │               └── SettingsPage.java
        └── resources/
            └── styles.css                      (thème cuir)
```

## Architecture

Séparation claire en trois couches :

- **Interface (`ui`)** : pages JavaFX + thème CSS. Ne touche jamais à la base.
- **Logique (`service`)** : calculs, totaux, statistiques, règles métier.
- **Données (`db` + `dao`)** : connexion SQLite et requêtes.

L'interface ne dialogue qu'avec `WalletService`, qui s'appuie sur les DAO.

## Améliorations possibles (v2)

Voir la conversation : budgets mensuels avec alertes, transactions récurrentes,
multi-comptes, import CSV, sauvegarde/restauration, thème clair/sombre, etc.

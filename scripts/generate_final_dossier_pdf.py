from pathlib import Path
from textwrap import wrap

from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.pdfgen import canvas


ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "output" / "pdf" / "porte-monnaie-dossier-complet.pdf"
OUT.parent.mkdir(parents=True, exist_ok=True)

W, H = A4

BG = colors.HexColor("#150f0a")
PANEL = colors.HexColor("#2c1d13")
PANEL2 = colors.HexColor("#382516")
PANEL3 = colors.HexColor("#21160e")
GOLD = colors.HexColor("#d4af37")
CREAM = colors.HexColor("#f2e8d6")
MUTED = colors.HexColor("#b7a68a")
GREEN = colors.HexColor("#93b97a")
RED = colors.HexColor("#d07a60")
BLUE = colors.HexColor("#78a9c7")
INK = colors.HexColor("#1a1009")
WHITE_CARD = colors.HexColor("#f4ead7")


def register_fonts():
    choices = {
        "Body": r"C:\Windows\Fonts\segoeui.ttf",
        "BodyBold": r"C:\Windows\Fonts\segoeuib.ttf",
        "Title": r"C:\Windows\Fonts\georgiab.ttf",
    }
    for name, path in choices.items():
        try:
            if Path(path).exists():
                pdfmetrics.registerFont(TTFont(name, path))
        except Exception:
            pass


def font(name):
    return name if name in pdfmetrics.getRegisteredFontNames() else "Helvetica"


def bg(c):
    c.setFillColor(BG)
    c.rect(0, 0, W, H, stroke=0, fill=1)
    c.setStrokeColor(colors.HexColor("#5a4326"))
    c.setLineWidth(0.6)
    for x in range(35, int(W), 62):
        c.line(x, 0, x - 160, H)
    c.setStrokeColor(GOLD)
    c.setDash(9, 7)
    c.setLineWidth(1.2)
    c.line(45, H - 38, W - 45, H - 38)
    c.setDash()


def footer(c, page, label="Porte-Monnaie - Dossier BTS SIO SLAM"):
    c.setFont(font("Body"), 8.5)
    c.setFillColor(MUTED)
    c.drawString(45, 26, label)
    c.drawRightString(W - 45, 26, str(page))


def rounded(c, x, y, w, h, fill=PANEL, stroke=None, radius=12, lw=1.0):
    c.setFillColor(fill)
    c.setStrokeColor(stroke or fill)
    c.setLineWidth(lw)
    c.roundRect(x, y, w, h, radius, stroke=1 if stroke else 0, fill=1)


def draw_text(c, txt, x, y, size=11, color=CREAM, bold=False, width=None, leading=None):
    c.setFont(font("BodyBold" if bold else "Body"), size)
    c.setFillColor(color)
    leading = leading or size * 1.35
    if width is None:
        c.drawString(x, y, txt)
        return y - leading
    max_chars = max(12, int(width / (size * 0.48)))
    for line in wrap(txt, max_chars):
        c.drawString(x, y, line)
        y -= leading
    return y


def h1(c, txt, y):
    c.setFont(font("Title"), 28)
    c.setFillColor(GOLD)
    c.drawString(45, y, txt)


def h2(c, txt, x, y):
    c.setFont(font("Title"), 17)
    c.setFillColor(GOLD)
    c.drawString(x, y, txt)


def pill(c, txt, x, y, w, color=GOLD):
    rounded(c, x, y, w, 24, color, None, 8)
    c.setFillColor(INK)
    c.setFont(font("BodyBold"), 9)
    c.drawCentredString(x + w / 2, y + 7.5, txt)


def bullet_list(c, items, x, y, width, size=10.5, gap=8, color=CREAM):
    for item in items:
        c.setFillColor(GOLD)
        c.circle(x + 4, y + 4, 2.2, stroke=0, fill=1)
        y = draw_text(c, item, x + 16, y, size, color, False, width - 16)
        y -= gap
    return y


def wallet_icon(c, x, y, s=1.0):
    rounded(c, x, y, 118 * s, 76 * s, colors.HexColor("#4b301c"), GOLD, 13 * s, 1.5)
    rounded(c, x + 13 * s, y + 13 * s, 92 * s, 50 * s, colors.HexColor("#24170e"), None, 10 * s)
    rounded(c, x + 69 * s, y + 26 * s, 36 * s, 23 * s, colors.HexColor("#6b4423"), GOLD, 6 * s, 1)
    c.setFillColor(GOLD)
    c.circle(x + 82 * s, y + 38 * s, 3.5 * s, stroke=0, fill=1)


def header_page(c, page, title):
    bg(c)
    h1(c, title, H - 82)
    footer(c, page)


def page_cover(c):
    bg(c)
    wallet_icon(c, W - 180, H - 170, 1.05)
    c.setFont(font("Title"), 40)
    c.setFillColor(GOLD)
    c.drawString(45, H - 145, "Porte-Monnaie")
    draw_text(c, "Dossier complet du projet", 48, H - 178, 16, CREAM, True)
    draw_text(c, "Application de bureau JavaFX pour gerer son budget personnel", 48, H - 205, 12.5, MUTED, False, 360)

    rounded(c, 45, 365, W - 90, 245, PANEL, GOLD, 18, 1.6)
    h2(c, "Objectif du projet", 70, 565)
    draw_text(c, "Creer une application simple, moderne et utilisable sur PC pour suivre ses finances personnelles : revenus, depenses, solde, epargne, statistiques et exports.", 70, 532, 12.5, CREAM, False, W - 145)
    y = 430
    for i, label in enumerate(["Java", "JavaFX", "SQLite", "JDBC", "GitHub", "PDF"]):
        pill(c, label, 70 + i * 76, y, 64)

    rounded(c, 45, 170, W - 90, 150, PANEL2, None, 16)
    h2(c, "Livrables", 70, 285)
    bullet_list(c, [
        "Application Windows avec executable.",
        "Code source versionne sur GitHub.",
        "Documentation claire pour comprendre et presenter le projet.",
        "Support visuel type webtoon integre dans ce dossier.",
    ], 70, 252, W - 150, 10.5, 5)
    footer(c, 1, "Porte-Monnaie - Dossier complet")
    c.showPage()


def page_summary(c):
    header_page(c, 2, "Sommaire")
    rounded(c, 45, 100, W - 90, H - 210, PANEL, GOLD, 18, 1.3)
    items = [
        ("01", "Contexte et objectif", "Pourquoi cette application existe."),
        ("02", "Fonctionnalites", "Ce que l'utilisateur peut faire."),
        ("03", "Parcours utilisateur", "De la connexion a l'export."),
        ("04", "Architecture", "Comment le code est organise."),
        ("05", "Base de donnees", "Les tables principales."),
        ("06", "Securite", "Mot de passe et injections SQL."),
        ("07", "Explication du code", "Les classes importantes."),
        ("08", "Tests et executable", "Ce qui a ete verifie."),
        ("09", "Demo BTS", "Scenario pour le jury."),
        ("10", "Recap webtoon", "Version visuelle et rapide."),
    ]
    y = H - 145
    for num, title, desc in items:
        c.setFillColor(GOLD)
        c.setFont(font("Title"), 18)
        c.drawString(75, y, num)
        draw_text(c, title, 125, y + 2, 13, CREAM, True)
        draw_text(c, desc, 125, y - 16, 9.5, MUTED, False, 360)
        y -= 54
    c.showPage()


def page_context(c):
    header_page(c, 3, "1. Contexte et objectif")
    rounded(c, 45, 500, W - 90, 250, PANEL, GOLD, 18, 1.3)
    h2(c, "Probleme de depart", 70, 705)
    bullet_list(c, [
        "Beaucoup de personnes suivent leurs finances avec plusieurs outils differents.",
        "Les revenus, depenses et projets d'epargne ne sont pas toujours centralises.",
        "Il est difficile de savoir rapidement combien il reste et ou part l'argent.",
    ], 70, 670, W - 150, 11.5)

    rounded(c, 45, 255, W - 90, 205, PANEL2, None, 18)
    h2(c, "Solution proposee", 70, 415)
    draw_text(c, "Porte-Monnaie est une application locale qui rassemble les informations financieres importantes dans une interface style portefeuille numerique.", 70, 382, 12, CREAM, False, W - 150)
    draw_text(c, "L'objectif est de proposer un outil simple a utiliser, mais assez complet pour demontrer les competences attendues en BTS SIO SLAM.", 70, 322, 12, CREAM, False, W - 150)

    rounded(c, 45, 105, W - 90, 110, PANEL, None, 18)
    h2(c, "Choix important", 70, 175)
    draw_text(c, "La version actuelle utilise SQLite. C'est coherent pour une application de bureau locale, car aucun serveur n'est necessaire. Une migration MySQL reste possible en evolution.", 70, 145, 11.5, MUTED, False, W - 150)
    c.showPage()


def page_features(c):
    header_page(c, 4, "2. Fonctionnalites")
    cards = [
        ("Compte utilisateur", ["Creation de compte", "Connexion email + mot de passe", "Nom utilisateur affiche"]),
        ("Tableau de bord", ["Solde actuel", "Revenus et depenses", "Epargne suivie", "Dernieres transactions"]),
        ("Transactions", ["Ajout", "Modification", "Suppression", "Filtres par date, categorie, montant"]),
        ("Epargne", ["Objectifs", "Montant cible", "Progression", "Reste a economiser"]),
        ("Statistiques", ["Repartition par categorie", "Evolution du solde", "Mois le plus depensier"]),
        ("Exports", ["CSV", "PDF", "Donnees locales persistantes"]),
    ]
    x_positions = [45, 315]
    y = 570
    for i, (name, items) in enumerate(cards):
        x = x_positions[i % 2]
        if i and i % 2 == 0:
            y -= 185
        rounded(c, x, y, 235, 150, PANEL if i % 2 == 0 else PANEL2, GOLD, 15, 1)
        h2(c, name, x + 18, y + 112)
        bullet_list(c, items, x + 18, y + 82, 195, 9.8, 4)
    rounded(c, 45, 70, W - 90, 95, PANEL3, None, 18)
    draw_text(c, "Ces fonctionnalites permettent une demonstration complete : compte, operations, graphiques, objectif d'epargne et export.", 70, 126, 11.5, CREAM, False, W - 150)
    c.showPage()


def page_journey(c):
    header_page(c, 5, "3. Parcours utilisateur")
    steps = [
        ("1", "Connexion", "L'utilisateur ouvre l'application et se connecte."),
        ("2", "Tableau de bord", "Il visualise son solde, ses revenus, ses depenses et son epargne."),
        ("3", "Ajout", "Il ajoute un revenu ou une depense avec date, categorie et description."),
        ("4", "Analyse", "Il consulte l'historique, les filtres et les statistiques."),
        ("5", "Epargne", "Il cree un objectif et suit sa progression."),
        ("6", "Export", "Il exporte les transactions en CSV ou PDF."),
    ]
    y = 680
    for i, (num, title, desc) in enumerate(steps):
        x = 70 if i % 2 == 0 else 315
        if i and i % 2 == 0:
            y -= 175
        rounded(c, x, y, 210, 125, PANEL2, GOLD, 14, 1)
        c.setFillColor(GOLD)
        c.setFont(font("Title"), 25)
        c.drawString(x + 16, y + 82, num)
        draw_text(c, title, x + 55, y + 92, 12.5, CREAM, True)
        draw_text(c, desc, x + 18, y + 58, 9.8, MUTED, False, 170)
    c.showPage()


def page_architecture(c):
    header_page(c, 6, "4. Architecture")
    draw_text(c, "Le projet est organise en couches. Chaque partie a un role clair, ce qui rend le code plus facile a comprendre et a modifier.", 50, H - 120, 12, CREAM, False, W - 100)
    layers = [
        ("ui", "Pages JavaFX : affichage, boutons, formulaires."),
        ("service", "Logique metier : calculs, authentification, exports."),
        ("dao", "Acces aux donnees : requetes SQL preparees."),
        ("model", "Objets metier : User, Transaction, Goal, Category."),
        ("db", "Connexion SQLite et creation des tables."),
    ]
    y = 615
    for name, desc in layers:
        rounded(c, 80, y, 110, 42, colors.HexColor("#6b4423"), None, 10)
        draw_text(c, name, 105, y + 15, 12, GOLD, True)
        rounded(c, 220, y, 295, 42, PANEL, GOLD, 10, 0.8)
        draw_text(c, desc, 235, y + 15, 10.2, CREAM, False, 255)
        y -= 70
    rounded(c, 45, 115, W - 90, 100, PANEL2, None, 16)
    draw_text(c, "Phrase simple a dire : l'interface ne parle pas directement avec la base. Elle passe par les services, qui utilisent ensuite les DAO.", 70, 175, 12, CREAM, True, W - 150)
    c.showPage()


def page_database(c):
    header_page(c, 7, "5. Base de donnees")
    tables = [
        ("users", "Compte utilisateur : nom, email, hash, sel, date de creation."),
        ("transactions", "Operations financieres : type, montant, categorie, date, description."),
        ("categories", "Categories par defaut et categories personnalisees."),
        ("goals", "Objectifs d'epargne : titre, cible, montant actuel."),
        ("settings", "Petits reglages : nom affiche, devise."),
    ]
    y = 650
    for table, desc in tables:
        rounded(c, 65, y, 135, 44, PANEL3, GOLD, 10, 1)
        draw_text(c, table, 83, y + 17, 11.5, CREAM, True)
        draw_text(c, desc, 230, y + 22, 10.5, MUTED, False, 290)
        y -= 78
    rounded(c, 45, 120, W - 90, 125, PANEL2, None, 16)
    h2(c, "Pourquoi SQLite ?", 70, 202)
    draw_text(c, "SQLite est integre dans l'application et stocke les donnees dans un fichier local. C'est plus simple pour une application PC. MySQL peut etre ajoute plus tard si l'application devient multi-utilisateur ou connectee a un serveur.", 70, 172, 11.5, CREAM, False, W - 150)
    c.showPage()


def page_security(c):
    header_page(c, 8, "6. Securite")
    rounded(c, 45, 500, W - 90, 250, PANEL, GOLD, 18, 1.3)
    h2(c, "Mot de passe", 70, 705)
    bullet_list(c, [
        "Le mot de passe n'est jamais stocke en clair.",
        "Un sel aleatoire est genere pour chaque compte.",
        "Le mot de passe et le sel sont hashes avec SHA-256.",
        "A la connexion, l'application recalcule le hash et compare le resultat.",
    ], 70, 670, W - 150, 11.2)

    rounded(c, 45, 270, W - 90, 180, PANEL2, None, 18)
    h2(c, "Injection SQL", 70, 405)
    draw_text(c, "Les requetes importantes utilisent PreparedStatement. Les valeurs saisies par l'utilisateur ne sont pas collees directement dans le SQL.", 70, 372, 12, CREAM, False, W - 150)
    draw_text(c, "C'est une protection importante contre les injections SQL.", 70, 318, 12, GOLD, True, W - 150)

    rounded(c, 45, 110, W - 90, 110, PANEL3, None, 18)
    draw_text(c, "Limite a expliquer : SHA-256 est acceptable pour montrer le principe dans un projet BTS, mais dans une application professionnelle on prefererait BCrypt, Argon2 ou PBKDF2.", 70, 175, 11.2, MUTED, False, W - 150)
    c.showPage()


def page_code(c):
    header_page(c, 9, "7. Explication du code")
    sections = [
        ("Transaction", "Represente une entree ou une sortie d'argent. Le montant reste positif, le type indique revenu ou depense."),
        ("WalletService", "Calcule les totaux, le solde, les statistiques et appelle les DAO."),
        ("AuthService", "Gere l'inscription, la connexion et le hashage du mot de passe."),
        ("TransactionDao", "Execute les requetes SQL pour ajouter, modifier, supprimer et filtrer les transactions."),
        ("GoalsPage", "Affiche les objectifs d'epargne et permet d'ajouter une somme."),
        ("styles.css", "Contient le theme sombre, cuir et dore de l'application."),
    ]
    y = 680
    for title, desc in sections:
        rounded(c, 55, y, W - 110, 58, PANEL if y % 2 else PANEL2, GOLD, 12, 0.8)
        draw_text(c, title, 75, y + 35, 11.5, GOLD, True)
        draw_text(c, desc, 195, y + 36, 9.8, CREAM, False, 310)
        y -= 78
    c.showPage()


def page_tests(c):
    header_page(c, 10, "8. Tests et executable")
    rounded(c, 45, 475, W - 90, 275, PANEL, GOLD, 18, 1.3)
    h2(c, "Ce qui a ete verifie", 70, 705)
    bullet_list(c, [
        "Compilation Maven du projet.",
        "Creation d'un compte.",
        "Connexion avec un bon mot de passe.",
        "Refus d'un mauvais mot de passe.",
        "Ajout d'un revenu et d'une depense.",
        "Calcul du solde.",
        "Objectifs d'epargne.",
        "Demarrage de l'executable Windows.",
    ], 70, 668, W - 150, 10.5, 5)

    rounded(c, 45, 245, W - 90, 180, PANEL2, None, 18)
    h2(c, "Fichiers utiles", 70, 382)
    bullet_list(c, [
        "Executable : dist/Porte-Monnaie/Porte-Monnaie.exe",
        "Zip complet : Porte-Monnaie-Windows.zip",
        "Explication : EXPLICATION_CODE_BTS.md",
        "PDF final : output/pdf/porte-monnaie-dossier-complet.pdf",
    ], 70, 350, W - 150, 10.5, 6)

    rounded(c, 45, 105, W - 90, 100, PANEL3, None, 18)
    draw_text(c, "Important : pour deplacer l'application, il faut garder le dossier complet, pas seulement le fichier .exe, car le runtime Java est inclus a cote.", 70, 160, 11.5, CREAM, True, W - 150)
    c.showPage()


def page_demo(c):
    header_page(c, 11, "9. Demo BTS")
    steps = [
        "Ouvrir l'application depuis le raccourci bureau.",
        "Creer un compte ou se connecter.",
        "Ajouter un revenu, par exemple un salaire.",
        "Ajouter une depense, par exemple des courses.",
        "Revenir au tableau de bord pour voir le solde.",
        "Creer un objectif d'epargne, par exemple Acheter un PC.",
        "Afficher les statistiques.",
        "Exporter les transactions en CSV ou PDF.",
        "Montrer rapidement la base de donnees et le code.",
    ]
    rounded(c, 45, 165, W - 90, 585, PANEL, GOLD, 18, 1.3)
    y = 695
    for i, step in enumerate(steps, 1):
        c.setFillColor(GOLD)
        c.setFont(font("Title"), 18)
        c.drawString(75, y, f"{i:02d}")
        y = draw_text(c, step, 125, y + 2, 11.5, CREAM, False, 360)
        y -= 18
    rounded(c, 45, 80, W - 90, 55, WHITE_CARD, GOLD, 14, 1)
    draw_text(c, "Phrase cle : ce projet montre du Java, JavaFX, JDBC, une base de donnees, de la securite et GitHub.", 65, 103, 10.5, INK, True, W - 130)
    c.showPage()


def speech(c, txt, x, y, w, h, accent):
    rounded(c, x, y, w, h, WHITE_CARD, accent, 14, 1.3)
    c.setFillColor(accent)
    c.circle(x + 22, y - 7, 7, stroke=0, fill=1)
    draw_text(c, txt, x + 18, y + h - 26, 10.5, INK, True, w - 36)


def page_webtoon(c):
    header_page(c, 12, "10. Recap webtoon")
    rounded(c, 45, 560, W - 90, 185, PANEL, GOLD, 18, 1.4)
    h2(c, "Le probleme", 70, 700)
    speech(c, "Je veux suivre mon argent sans ouvrir trois applications differentes.", 85, 612, 380, 62, RED)

    rounded(c, 45, 330, W - 90, 185, PANEL2, GOLD, 18, 1.4)
    h2(c, "La reponse", 70, 470)
    draw_text(c, "Un portefeuille numerique local : revenus, depenses, epargne, statistiques et exports.", 75, 432, 13, CREAM, True, 310)
    wallet_icon(c, 405, 385, 0.9)

    rounded(c, 45, 105, W - 90, 180, PANEL, GOLD, 18, 1.4)
    h2(c, "La conclusion", 70, 240)
    draw_text(c, "Porte-Monnaie est un projet complet pour un BTS SIO SLAM : application JavaFX, architecture en couches, base SQLite, securite et gestion de projet GitHub.", 75, 205, 12.5, CREAM, False, W - 150)
    c.showPage()


def page_conclusion(c):
    header_page(c, 13, "Conclusion")
    rounded(c, 45, 460, W - 90, 290, PANEL, GOLD, 18, 1.4)
    h2(c, "Ce que le projet demontre", 70, 705)
    bullet_list(c, [
        "Developpement logiciel en Java.",
        "Interface graphique JavaFX.",
        "Architecture MVC simplifiee / couches separees.",
        "JDBC et base de donnees relationnelle.",
        "Securite de base : hashage et requetes preparees.",
        "Versionnement GitHub et documentation.",
    ], 70, 670, W - 150, 11.2)

    rounded(c, 45, 230, W - 90, 180, PANEL2, None, 18)
    h2(c, "Evolutions possibles", 70, 365)
    bullet_list(c, [
        "Migration vers MySQL.",
        "Plusieurs comptes bancaires.",
        "Export Excel XLSX.",
        "Abonnements recurrents.",
        "Notifications de budget.",
        "Application mobile.",
    ], 70, 332, W - 150, 10.5, 4)

    rounded(c, 45, 100, W - 90, 90, WHITE_CARD, GOLD, 16, 1.3)
    draw_text(c, "Phrase finale : l'application est utilisable sur PC et couvre les competences importantes attendues pour l'epreuve professionnelle.", 70, 145, 11.5, INK, True, W - 150)
    c.showPage()


def main():
    register_fonts()
    c = canvas.Canvas(str(OUT), pagesize=A4)
    c.setTitle("Porte-Monnaie - Dossier complet")
    pages = [
        page_cover,
        page_summary,
        page_context,
        page_features,
        page_journey,
        page_architecture,
        page_database,
        page_security,
        page_code,
        page_tests,
        page_demo,
        page_webtoon,
        page_conclusion,
    ]
    for page in pages:
        page(c)
    c.save()
    print(OUT)


if __name__ == "__main__":
    main()

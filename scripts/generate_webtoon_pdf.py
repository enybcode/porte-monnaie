from pathlib import Path
from textwrap import wrap

from reportlab.lib import colors
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.pdfgen import canvas


ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "output" / "pdf" / "porte-monnaie-webtoon.pdf"
OUT.parent.mkdir(parents=True, exist_ok=True)

PAGE_W = 720
PAGE_H = 1280

BG = colors.HexColor("#160f0a")
PANEL = colors.HexColor("#2b1d13")
PANEL_2 = colors.HexColor("#352417")
GOLD = colors.HexColor("#d4af37")
CREAM = colors.HexColor("#f0e6d2")
MUTED = colors.HexColor("#b9a78a")
GREEN = colors.HexColor("#93b97a")
RED = colors.HexColor("#d07a60")
INK = colors.HexColor("#1a1009")


def register_fonts():
    fonts = {
        "Segoe": r"C:\Windows\Fonts\segoeui.ttf",
        "SegoeBold": r"C:\Windows\Fonts\segoeuib.ttf",
        "GeorgiaBold": r"C:\Windows\Fonts\georgiab.ttf",
    }
    for name, path in fonts.items():
        try:
            if Path(path).exists():
                pdfmetrics.registerFont(TTFont(name, path))
        except Exception:
            pass


def font(name):
    return name if name in pdfmetrics.getRegisteredFontNames() else "Helvetica"


def rounded(c, x, y, w, h, fill, stroke=None, radius=20, width=1):
    c.setFillColor(fill)
    c.setStrokeColor(stroke or fill)
    c.setLineWidth(width)
    c.roundRect(x, y, w, h, radius, stroke=1 if stroke else 0, fill=1)


def stitch(c, x, y, w):
    c.setStrokeColor(GOLD)
    c.setLineWidth(2)
    c.setDash(10, 8)
    c.line(x, y, x + w, y)
    c.setDash()


def text(c, value, x, y, size=18, color=CREAM, bold=False, max_width=None, leading=None):
    c.setFillColor(color)
    c.setFont(font("SegoeBold" if bold else "Segoe"), size)
    leading = leading or size * 1.25
    if not max_width:
        c.drawString(x, y, value)
        return y - leading
    approx_chars = max(18, int(max_width / (size * 0.52)))
    for line in wrap(value, approx_chars):
        c.drawString(x, y, line)
        y -= leading
    return y


def title(c, value, x, y, size=42):
    c.setFillColor(GOLD)
    c.setFont(font("GeorgiaBold"), size)
    c.drawString(x, y, value)


def speech(c, value, x, y, w, h, accent=GOLD):
    rounded(c, x, y, w, h, colors.HexColor("#f4ead7"), accent, 18, 2)
    c.setFillColor(accent)
    c.circle(x + 28, y - 8, 9, stroke=0, fill=1)
    text(c, value, x + 22, y + h - 32, 17, INK, True, w - 44)


def wallet_icon(c, x, y, scale=1):
    rounded(c, x, y, 190 * scale, 120 * scale, colors.HexColor("#4a2f1a"), GOLD, 18 * scale, 2)
    rounded(c, x + 18 * scale, y + 18 * scale, 154 * scale, 82 * scale, colors.HexColor("#2a1a10"), None, 12 * scale)
    rounded(c, x + 105 * scale, y + 42 * scale, 62 * scale, 36 * scale, colors.HexColor("#6b4423"), GOLD, 8 * scale, 1)
    c.setFillColor(GOLD)
    c.circle(x + 126 * scale, y + 60 * scale, 5 * scale, stroke=0, fill=1)


def mini_screen(c, x, y, w, h):
    rounded(c, x, y, w, h, colors.HexColor("#20150d"), GOLD, 18, 2)
    rounded(c, x + 24, y + h - 120, w - 48, 78, colors.HexColor("#3b2818"), None, 14)
    text(c, "SOLDE ACTUEL", x + 44, y + h - 74, 15, MUTED, True)
    text(c, "1 124,50 EUR", x + 44, y + h - 105, 28, GREEN, True)
    for i, label in enumerate(["Revenus", "Depenses", "Epargne"]):
        rounded(c, x + 24 + i * ((w - 64) / 3), y + h - 215, (w - 84) / 3, 70, PANEL_2, None, 12)
        text(c, label, x + 38 + i * ((w - 64) / 3), y + h - 174, 13, MUTED, True)


def page_header(c, number, episode):
    c.setFillColor(BG)
    c.rect(0, 0, PAGE_W, PAGE_H, stroke=0, fill=1)
    stitch(c, 50, PAGE_H - 42, PAGE_W - 100)
    c.setFillColor(MUTED)
    c.setFont(font("SegoeBold"), 13)
    c.drawRightString(PAGE_W - 50, PAGE_H - 64, f"Episode {number} - {episode}")


def panel_title(c, value, x, y):
    c.setFillColor(GOLD)
    c.setFont(font("GeorgiaBold"), 26)
    c.drawString(x, y, value)


def draw_page_1(c):
    page_header(c, 1, "Le besoin")
    title(c, "Porte-Monnaie", 55, PAGE_H - 110, 52)
    text(c, "Une application de bureau pour suivre son budget sans tableur complique.", 58, PAGE_H - 148, 19, CREAM, False, 410)
    wallet_icon(c, 455, PAGE_H - 230, 0.9)

    rounded(c, 50, 720, 620, 300, PANEL, GOLD, 24, 2)
    panel_title(c, "Le probleme", 82, 965)
    speech(c, "Je depense partout, mais je ne vois jamais clairement ou part mon argent.", 92, 820, 430, 90, RED)
    text(c, "Papier, notes telephone, appli bancaire, tickets de caisse : les informations sont dispersees.", 90, 785, 18, CREAM, False, 500)

    rounded(c, 50, 350, 620, 300, PANEL_2, GOLD, 24, 2)
    panel_title(c, "La solution", 82, 595)
    mini_screen(c, 360, 390, 250, 220)
    text(c, "Porte-Monnaie centralise les revenus, depenses, objectifs d'epargne et statistiques dans une interface simple.", 90, 535, 19, CREAM, False, 245)

    rounded(c, 50, 90, 620, 190, PANEL, None, 24)
    panel_title(c, "Technos utilisees", 82, 225)
    text(c, "Java 17 - JavaFX - SQLite - JDBC - GitHub - Export CSV/PDF", 92, 184, 20, CREAM, True, 510)
    text(c, "Choix actuel : SQLite, car l'application fonctionne localement sur PC sans serveur.", 92, 146, 16, MUTED, False, 520)
    c.showPage()


def draw_page_2(c):
    page_header(c, 2, "Les fonctions")
    rounded(c, 50, 910, 620, 245, PANEL, GOLD, 24, 2)
    panel_title(c, "Acces securise", 82, 1095)
    text(c, "L'utilisateur cree un compte puis se connecte avec son email et son mot de passe.", 90, 1048, 19, CREAM, False, 500)
    text(c, "Le mot de passe n'est pas stocke en clair : il est sale puis hashe.", 90, 985, 18, GOLD, True, 270)
    speech(c, "Devant le jury : hashage et requetes preparees.", 390, 930, 225, 88, GREEN)

    rounded(c, 50, 560, 620, 285, PANEL_2, GOLD, 24, 2)
    panel_title(c, "Transactions", 82, 788)
    for i, (name, amount, col) in enumerate([
        ("Salaire", "+1200 EUR", GREEN),
        ("Courses", "-75,50 EUR", RED),
        ("Abonnement", "-12,99 EUR", RED),
    ]):
        yy = 720 - i * 58
        rounded(c, 92, yy, 500, 42, colors.HexColor("#21160d"), None, 10)
        text(c, name, 115, yy + 15, 17, CREAM, True)
        c.setFillColor(col)
        c.setFont(font("SegoeBold"), 17)
        c.drawRightString(570, yy + 15, amount)
    text(c, "Filtres : date, categorie, montant et mot-cle.", 92, 585, 16, MUTED, False, 500)

    rounded(c, 50, 250, 620, 245, PANEL, GOLD, 24, 2)
    panel_title(c, "Epargne", 82, 435)
    text(c, "L'utilisateur cree un objectif : PC, voiture, voyage ou fonds d'urgence.", 90, 390, 18, CREAM, False, 500)
    rounded(c, 92, 320, 500, 30, colors.HexColor("#1f160e"), None, 12)
    rounded(c, 92, 320, 285, 30, GOLD, None, 12)
    text(c, "Acheter un PC : 250 / 900 EUR", 105, 330, 15, INK, True)

    rounded(c, 50, 75, 620, 120, PANEL_2, None, 24)
    text(c, "Le tableau de bord affiche directement solde, revenus, depenses et epargne.", 82, 145, 21, CREAM, True, 535)
    c.showPage()


def draw_page_3(c):
    page_header(c, 3, "Architecture")
    rounded(c, 50, 870, 620, 290, PANEL, GOLD, 24, 2)
    panel_title(c, "Organisation du code", 82, 1098)
    layers = [
        ("ui", "les pages JavaFX"),
        ("service", "les calculs et les regles"),
        ("dao", "les requetes SQL"),
        ("model", "les objets metier"),
        ("db", "la base SQLite"),
    ]
    for i, (name, desc) in enumerate(layers):
        yy = 1048 - i * 38
        rounded(c, 92, yy, 160, 30, colors.HexColor("#6b4423"), None, 8)
        text(c, name, 112, yy + 9, 14, GOLD, True)
        text(c, desc, 278, yy + 9, 15, CREAM, False)

    rounded(c, 50, 520, 620, 280, PANEL_2, GOLD, 24, 2)
    panel_title(c, "Base de donnees", 82, 738)
    for i, table in enumerate(["users", "transactions", "categories", "goals", "settings"]):
        x = 92 + (i % 2) * 250
        y = 680 - (i // 2) * 62
        rounded(c, x, y, 210, 38, colors.HexColor("#21160d"), GOLD, 10, 1)
        text(c, table, x + 20, y + 13, 16, CREAM, True)
    text(c, "Acces base : JDBC + PreparedStatement.", 92, 535, 15, MUTED, False, 500)

    rounded(c, 50, 175, 620, 270, PANEL, GOLD, 24, 2)
    panel_title(c, "Demo possible", 82, 385)
    steps = ["1. creer un compte", "2. ajouter un revenu", "3. ajouter une depense", "4. creer un objectif", "5. afficher les statistiques", "6. exporter les donnees"]
    for i, step in enumerate(steps):
        text(c, step, 98 + (i % 2) * 285, 330 - (i // 2) * 48, 18, CREAM, True)
    c.showPage()


def draw_page_4(c):
    page_header(c, 4, "Conclusion BTS")
    rounded(c, 50, 780, 620, 360, PANEL, GOLD, 24, 2)
    title(c, "Pourquoi ce projet est solide ?", 82, 1065, 32)
    bullets = [
        "Programmation orientee objet en Java",
        "Interface graphique JavaFX utilisable sur PC",
        "Architecture separee en couches",
        "Base de donnees relationnelle locale",
        "Securite : hashage + requetes preparees",
        "Gestion de projet avec GitHub",
    ]
    for i, bullet in enumerate(bullets):
        text(c, "- " + bullet, 95, 1005 - i * 42, 19, CREAM, False, 510)

    rounded(c, 50, 430, 620, 280, PANEL_2, GOLD, 24, 2)
    panel_title(c, "Limites assumees", 82, 650)
    text(c, "La version actuelle est une application locale avec SQLite. Pour une V2, je pourrais ajouter MySQL, plusieurs comptes bancaires, les abonnements recurrents et l'export Excel XLSX.", 92, 600, 19, CREAM, False, 505)

    rounded(c, 50, 120, 620, 235, PANEL, GOLD, 24, 2)
    panel_title(c, "Phrase de presentation", 82, 292)
    speech(c, "Porte-Monnaie m'a permis de montrer du Java, une interface JavaFX, une base de donnees, de la securite et une vraie organisation de projet.", 95, 165, 505, 88, GOLD)
    text(c, "Projet BTS SIO SLAM - Enybcode", 82, 92, 17, MUTED, True)
    c.showPage()


def main():
    register_fonts()
    c = canvas.Canvas(str(OUT), pagesize=(PAGE_W, PAGE_H))
    c.setTitle("Porte-Monnaie - Presentation webtoon")
    draw_page_1(c)
    draw_page_2(c)
    draw_page_3(c)
    draw_page_4(c)
    c.save()
    print(OUT)


if __name__ == "__main__":
    main()

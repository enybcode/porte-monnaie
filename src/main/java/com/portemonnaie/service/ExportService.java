package com.portemonnaie.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.portemonnaie.model.Transaction;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Export des transactions vers CSV et PDF.
 *  - CSV  : standard, ouvrable dans Excel / LibreOffice.
 *  - PDF  : tableau présentable grâce à la bibliothèque OpenPDF.
 */
public class ExportService {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ============================ CSV ====================================
    public void exportCsv(List<Transaction> transactions, File file) throws IOException {
        try (Writer w = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            // BOM UTF-8 : aide Excel à reconnaître l'encodage / les accents
            w.write('\uFEFF');
            w.write("Date;Titre;Type;Catégorie;Montant;Description\n");

            for (Transaction t : transactions) {
                w.write(String.join(";",
                        t.getDate().format(DATE_FMT),
                        escape(t.getTitle()),
                        t.getType().getLabel(),
                        escape(t.getCategory()),
                        String.format(Locale.FRANCE, "%.2f", t.getAmount()),
                        escape(t.getDescription())
                ));
                w.write("\n");
            }
        }
    }

    /** Protège les valeurs contenant ; " ou retour à la ligne. */
    private String escape(String value) {
        if (value == null) return "";
        if (value.contains(";") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // ============================ PDF ====================================
    public void exportPdf(List<Transaction> transactions, File file,
                          String userName, String currency) throws IOException {
        Document doc = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(file));
            doc.open();

            // Couleurs cuir
            Color leather = new Color(107, 68, 35);
            Color gold = new Color(212, 175, 55);
            Color cream = new Color(245, 240, 225);

            // Titre
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, leather);
            Paragraph title = new Paragraph("Porte-Monnaie", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);
            Paragraph sub = new Paragraph(
                    "Relevé de " + userName + " — édité le "
                            + LocalDate.now().format(DATE_FMT), subFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(16);
            doc.add(sub);

            // Tableau
            PdfPTable table = new PdfPTable(new float[]{2.2f, 4f, 2.2f, 2.8f, 2.4f});
            table.setWidthPercentage(100);

            Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, cream);
            String[] headers = {"Date", "Titre", "Type", "Catégorie", "Montant"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headFont));
                cell.setBackgroundColor(leather);
                cell.setPadding(6);
                cell.setBorderColor(gold);
                table.addCell(cell);
            }

            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            double total = 0;
            boolean alt = false;
            for (Transaction t : transactions) {
                Color bg = alt ? new Color(248, 244, 236) : Color.WHITE;
                alt = !alt;

                addCell(table, t.getDate().format(DATE_FMT), bodyFont, bg);
                addCell(table, t.getTitle(), bodyFont, bg);
                addCell(table, t.getType().getLabel(), bodyFont, bg);
                addCell(table, t.getCategory(), bodyFont, bg);

                String signed = (t.getSignedAmount() >= 0 ? "+" : "")
                        + String.format(Locale.FRANCE, "%,.2f", t.getSignedAmount())
                        + " " + currency;
                addCell(table, signed, bodyFont, bg);

                total += t.getSignedAmount();
            }
            doc.add(table);

            // Total
            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, leather);
            Paragraph totalP = new Paragraph(
                    "Solde des opérations listées : "
                            + String.format(Locale.FRANCE, "%,.2f", total) + " " + currency,
                    totalFont);
            totalP.setAlignment(Element.ALIGN_RIGHT);
            totalP.setSpacingBefore(16);
            doc.add(totalP);

        } finally {
            doc.close();
        }
    }

    private void addCell(PdfPTable table, String text, Font font, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(5);
        cell.setBorderColor(new Color(220, 210, 195));
        table.addCell(cell);
    }
}

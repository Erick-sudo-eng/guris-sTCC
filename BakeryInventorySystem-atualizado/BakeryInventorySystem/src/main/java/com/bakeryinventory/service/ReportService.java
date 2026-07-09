package com.bakeryinventory.service;

import com.bakeryinventory.dao.SettingsDao;
import com.bakeryinventory.model.Product;
import com.bakeryinventory.model.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ReportService {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final InventoryService inventoryService = new InventoryService();
    private final AuthService authService = new AuthService();
    private final SettingsDao settingsDao = new SettingsDao();

    public String getBakeryName() throws SQLException {
        return settingsDao.get("bakery_name", "Padaria Modelo");
    }

    public void setBakeryName(String name) throws SQLException {
        settingsDao.set("bakery_name", name == null || name.isBlank() ? "Padaria Modelo" : name.trim());
    }

    public List<String> reportTypes() {
        return List.of(
                "Relatorio de estoque",
                "Relatorio de estoque baixo",
                "Relatorio de produtos vencidos",
                "Relatorio de produtos proximos da validade",
                "Relatorio de contas de usuario"
        );
    }

    public ReportData buildReport(String type, User loggedUser) throws SQLException {
        String bakeryName = getBakeryName();
        List<String> headers;
        List<List<String>> rows = new ArrayList<>();

        switch (type) {
            case "Relatorio de estoque" -> {
                headers = productHeaders();
                for (Product product : inventoryService.search("")) {
                    rows.add(productRow(product));
                }
            }
            case "Relatorio de estoque baixo" -> {
                headers = productHeaders();
                for (Product product : inventoryService.search("")) {
                    if (product.isBelowIdealStock()) {
                        rows.add(productRow(product));
                    }
                }
            }
            case "Relatorio de produtos vencidos" -> {
                headers = productHeaders();
                for (Product product : inventoryService.expired()) {
                    rows.add(productRow(product));
                }
            }
            case "Relatorio de produtos proximos da validade" -> {
                headers = productHeaders();
                for (Product product : inventoryService.nearExpiration()) {
                    rows.add(productRow(product));
                }
            }
            case "Relatorio de contas de usuario" -> {
                headers = List.of("ID", "Usuario", "Perfil", "Criado em");
                for (User user : authService.listUsers()) {
                    rows.add(List.of(
                            String.valueOf(user.getId()),
                            user.getUsername(),
                            user.isManager() ? "Gerente" : "Usuario",
                            user.getCreatedAt().format(DATE_TIME)
                    ));
                }
            }
            default -> throw new IllegalArgumentException("Tipo de relatorio invalido.");
        }

        return new ReportData(type, bakeryName, LocalDateTime.now(), loggedUser, headers, rows);
    }

    public void exportPdf(ReportData data, Path file) throws IOException {
        List<String> lines = data.asTextLines();
        List<List<String>> pages = paginate(lines, 42);
        List<String> objects = new ArrayList<>();
        objects.add("<< /Type /Catalog /Pages 2 0 R >>");

        StringBuilder kids = new StringBuilder("[");
        for (int i = 0; i < pages.size(); i++) {
            kids.append(3 + (i * 2)).append(" 0 R ");
        }
        kids.append("]");
        objects.add("<< /Type /Pages /Kids " + kids + " /Count " + pages.size() + " >>");

        for (int i = 0; i < pages.size(); i++) {
            int pageObj = 3 + (i * 2);
            int contentObj = pageObj + 1;
            objects.add("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> >> >> /Contents " + contentObj + " 0 R >>");
            String content = pdfContent(pages.get(i));
            objects.add("<< /Length " + content.getBytes(StandardCharsets.ISO_8859_1).length + " >>\nstream\n" + content + "\nendstream");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write("%PDF-1.4\n".getBytes(StandardCharsets.ISO_8859_1));
        List<Integer> offsets = new ArrayList<>();
        offsets.add(0);
        for (int i = 0; i < objects.size(); i++) {
            offsets.add(out.size());
            out.write(((i + 1) + " 0 obj\n" + objects.get(i) + "\nendobj\n").getBytes(StandardCharsets.ISO_8859_1));
        }
        int xref = out.size();
        out.write(("xref\n0 " + (objects.size() + 1) + "\n0000000000 65535 f \n").getBytes(StandardCharsets.ISO_8859_1));
        for (int i = 1; i < offsets.size(); i++) {
            out.write(String.format("%010d 00000 n \n", offsets.get(i)).getBytes(StandardCharsets.ISO_8859_1));
        }
        out.write(("trailer\n<< /Size " + (objects.size() + 1) + " /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF").getBytes(StandardCharsets.ISO_8859_1));
        Files.write(file, out.toByteArray());
    }

    public void exportDocx(ReportData data, Path file) throws IOException {
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(file))) {
            addZip(zip, "[Content_Types].xml", """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                      <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                      <Default Extension="xml" ContentType="application/xml"/>
                      <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
                    </Types>
                    """);
            addZip(zip, "_rels/.rels", """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                      <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
                    </Relationships>
                    """);
            addZip(zip, "word/document.xml", documentXml(data));
        }
    }

    private List<String> productHeaders() {
        return List.of("ID", "Produto", "Categoria", "Estoque", "Ideal", "Preco", "Validade", "Cadastro", "Situacao");
    }

    private List<String> productRow(Product product) {
        return List.of(
                String.valueOf(product.getId()),
                product.getName(),
                product.getCategory(),
                String.valueOf(product.getQuantity()),
                String.valueOf(product.getIdealQuantity()),
                "R$ " + product.getUnitPrice().setScale(2, RoundingMode.HALF_UP),
                product.getExpirationDate().toString(),
                product.getRegistrationDate().toString(),
                status(product)
        );
    }

    private String status(Product product) {
        if (product.isExpired()) {
            return "Vencido";
        }
        if (product.isNearExpiration(InventoryService.EXPIRATION_WARNING_DAYS)) {
            return "Validade proxima";
        }
        if (product.isBelowIdealStock()) {
            return "Estoque baixo";
        }
        return "OK";
    }

    private List<List<String>> paginate(List<String> lines, int pageSize) {
        List<List<String>> pages = new ArrayList<>();
        for (int i = 0; i < lines.size(); i += pageSize) {
            pages.add(lines.subList(i, Math.min(i + pageSize, lines.size())));
        }
        if (pages.isEmpty()) {
            pages.add(List.of("Nenhum dado encontrado."));
        }
        return pages;
    }

    private String pdfContent(List<String> lines) {
        StringBuilder builder = new StringBuilder("BT\n/F1 10 Tf\n50 800 Td\n14 TL\n");
        for (String line : lines) {
            builder.append("(").append(escapePdf(line)).append(") Tj\nT*\n");
        }
        builder.append("ET");
        return builder.toString();
    }

    private String documentXml(ReportData data) {
        StringBuilder xml = new StringBuilder("""
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:body>
                """);
        for (String line : data.headerLines()) {
            xml.append(paragraph(line));
        }
        xml.append("<w:tbl>");
        xml.append(rowXml(data.headers()));
        for (List<String> row : data.rows()) {
            xml.append(rowXml(row));
        }
        xml.append("</w:tbl>");
        xml.append(paragraph("Total de registros: " + data.rows().size()));
        xml.append("<w:sectPr/></w:body></w:document>");
        return xml.toString();
    }

    private String rowXml(List<String> cells) {
        StringBuilder row = new StringBuilder("<w:tr>");
        for (String cell : cells) {
            row.append("<w:tc><w:p><w:r><w:t>")
                    .append(xml(cell))
                    .append("</w:t></w:r></w:p></w:tc>");
        }
        row.append("</w:tr>");
        return row.toString();
    }

    private String paragraph(String text) {
        return "<w:p><w:r><w:t>" + xml(text) + "</w:t></w:r></w:p>";
    }

    private void addZip(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private String escapePdf(String value) {
        return value.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private String xml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public record ReportData(String title, String bakeryName, LocalDateTime generatedAt, User loggedUser,
                             List<String> headers, List<List<String>> rows) {
        List<String> headerLines() {
            return List.of(
                    bakeryName,
                    title,
                    "Gerado em: " + generatedAt.format(DATE_TIME),
                    "Usuario logado: " + loggedUser.getUsername() + " (" + (loggedUser.isManager() ? "Gerente" : "Usuario") + ")"
            );
        }

        List<String> asTextLines() {
            List<String> lines = new ArrayList<>(headerLines());
            lines.add("Total de registros: " + rows.size());
            lines.add("");
            lines.add(String.join(" | ", headers));
            lines.add("-".repeat(110));
            for (List<String> row : rows) {
                lines.add(String.join(" | ", row));
            }
            return lines;
        }
    }
}

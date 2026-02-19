package org.enginecraft.util;

import org.enginecraft.objects.Difference;
import org.enginecraft.objects.DifferenceOverview;
import org.enginecraft.objects.DifferenceType;
import org.enginecraft.objects.ReportInfo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class HtmlReportUtil {
    public static void generate(Path outputPath, String fileName, DifferenceOverview differenceOverview) throws IOException {
        StringBuilder sb = new StringBuilder();
        initHtml(sb);

        // ================= CONTENT =================
        sb.append("<h1>").append(ReportInfo.TITLE).append("</h1>\n");
        sb.append("<p>").append(ReportInfo.SUBTITLE).append("</p>\n");
        sb.append("<br><p><strong>Library A:</strong> ").append(differenceOverview.libA()).append("<br><strong>Library B:</strong> ").append(differenceOverview.libB()).append("</p>\n");
        sb.append("<br><p>").append(ReportInfo.DESCRIPTION.replace("\n", "<br>")).append("</p>\n");
        sb.append("<h2 style=\"margin-left: 25px;\">").append(ReportInfo.RESULTS).append("</h2>");

        sb.append("<div id='spinner-container'>\n");
        sb.append("  <div class='spinner'></div>\n");
        sb.append("  <div style='margin-top: 10px; color: #999;'>Loading report...</div>\n");
        sb.append("</div>\n");

        // ================= RESULTS =================
        sb.append("<div id='content'>\n");

        Map<String, Map<DifferenceType, Map<Boolean, List<Difference>>>> diffsByRefTypeAndHeader =
                differenceOverview.differences().stream()
                        .collect(Collectors.groupingBy(
                                Difference::ref,
                                TreeMap::new,
                                Collectors.groupingBy(
                                        Difference::type,
                                        LinkedHashMap::new,
                                        Collectors.partitioningBy(
                                                d -> d.rIndex() != null && d.rIndex() == 0
                                        )
                                )
                        ));

        Map<DifferenceType, Map<Boolean, Long>> totalsByTypeAndHeader =
                differenceOverview.differences().stream()
                        .collect(Collectors.groupingBy(
                                Difference::type,
                                Collectors.groupingBy(
                                        d -> d.rIndex() != null && d.rIndex() == 0,
                                        Collectors.counting()
                                )
                        ));

        String[] categories = {
                "Missing Headers",
                "Unknown Headers",
                "Mismatched Headers",
                "Missing Rows",
                "Unknown Rows",
                "Mismatched Rows"
        };

        for (String category : categories) {
            long categoryCount = switch (category) {
                case "Missing Headers" -> totalsByTypeAndHeader.getOrDefault(DifferenceType.MISSING, Map.of()).getOrDefault(true, 0L);
                case "Unknown Headers" -> totalsByTypeAndHeader.getOrDefault(DifferenceType.UNKNOWN, Map.of()).getOrDefault(true, 0L);
                case "Mismatched Headers" -> totalsByTypeAndHeader.getOrDefault(DifferenceType.MISMATCH, Map.of()).getOrDefault(true, 0L);
                case "Missing Rows" -> totalsByTypeAndHeader.getOrDefault(DifferenceType.MISSING, Map.of()).getOrDefault(false, 0L);
                case "Unknown Rows" -> totalsByTypeAndHeader.getOrDefault(DifferenceType.UNKNOWN, Map.of()).getOrDefault(false, 0L);
                case "Mismatched Rows" -> totalsByTypeAndHeader.getOrDefault(DifferenceType.MISMATCH, Map.of()).getOrDefault(false, 0L);
                default -> 0;
            };

            if (categoryCount == 0) continue;

            sb.append("<details>\n");
            sb.append("<summary>").append(category).append(" (").append(categoryCount).append(")</summary>\n");

            for (String ref : diffsByRefTypeAndHeader.keySet()) {
                Map<DifferenceType, Map<Boolean, List<Difference>>> diffsByTypeAndHeader = diffsByRefTypeAndHeader.get(ref);

                List<Difference> missingHeaders = diffsByTypeAndHeader
                        .getOrDefault(DifferenceType.MISSING, Map.of())
                        .getOrDefault(true, List.of());
                List<Difference> missingRows = diffsByTypeAndHeader
                        .getOrDefault(DifferenceType.MISSING, Map.of())
                        .getOrDefault(false, List.of());

                List<Difference> unknownHeaders = diffsByTypeAndHeader
                        .getOrDefault(DifferenceType.UNKNOWN, Map.of())
                        .getOrDefault(true, List.of());
                List<Difference> unknownRows = diffsByTypeAndHeader
                        .getOrDefault(DifferenceType.UNKNOWN, Map.of())
                        .getOrDefault(false, List.of());

                List<Difference> mismatchedHeaders = diffsByTypeAndHeader
                        .getOrDefault(DifferenceType.MISMATCH, Map.of())
                        .getOrDefault(true, List.of());
                List<Difference> mismatchedRows = diffsByTypeAndHeader
                        .getOrDefault(DifferenceType.MISMATCH, Map.of())
                        .getOrDefault(false, List.of());

                switch (category) {
                    case "Missing Headers" -> appendItems(outputPath, fileName, category, ref, sb, missingHeaders);
                    case "Unknown Headers" -> appendItems(outputPath, fileName, category, ref, sb, unknownHeaders);
                    case "Mismatched Headers" -> appendItems(outputPath, fileName, category, ref, sb, mismatchedHeaders);
                    case "Missing Rows" -> appendItems(outputPath, fileName, category, ref, sb, missingRows);
                    case "Unknown Rows" -> appendItems(outputPath, fileName, category, ref, sb, unknownRows);
                    case "Mismatched Rows" -> appendItems(outputPath, fileName, category, ref, sb, mismatchedRows);
                }
            }

            sb.append("</details>\n");
        }

        sb.append("</div>\n");
        sb.append("</body>\n</html>");

        WriteUtil.writeFile(outputPath.resolve(fileName + ".html"), sb.toString());
    }

    private static void initHtml(StringBuilder sb) {
        sb.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n<meta charset=\"UTF-8\">\n");
        sb.append("<title>").append(ReportInfo.TITLE).append("</title>\n");

        // ================= Modern CSS =================
        sb.append("<style>\n");
        sb.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f4f7fa; color: #333; margin: 0; padding: 0; }\n");
        sb.append("h1, h2, p { margin: 0; padding: 0; }\n");
        sb.append("h1 { font-size: 2.5em; color: #2d87f0; text-align: center; padding-top: 30px; }\n");
        sb.append("h2 { font-size: 1.8em; margin: 20px 0 10px; color: #333; }\n");
        sb.append("p { font-size: 1.1em; line-height: 1.6; color: #666; text-align: center; }\n");

        // Details section styling
        sb.append("details { background: #ffffff; border-radius: 8px; margin-bottom: 15px; box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1); padding: 15px; }\n");
        sb.append("summary { font-size: 1.2em; font-weight: bold; color: #2d87f0; cursor: pointer; }\n");
        sb.append("summary:hover { color: #155ba4; }\n");

        // Table styling
        sb.append(".table-container { max-height: 350px; overflow-y: auto; border-radius: 10px; }\n");
        sb.append("table { width: 100%; border-collapse: collapse; background-color: #ffffff; }\n");
        sb.append("th, td { padding: 10px 15px; text-align: left; border-bottom: 1px solid #ddd; }\n");
        sb.append("th { background-color: #3c8dbc; color: white; position: sticky; top: 0; z-index: 10; }\n");
        sb.append("tr:nth-child(even) { background-color: #f9f9f9; }\n");
        sb.append("tr:hover { background-color: #e0f7ff; cursor: pointer; }\n");

        // Spinner
        sb.append("#spinner-container { text-align:center; margin-top:40px; }\n");
        sb.append(".spinner {\n");
        sb.append("  border: 6px solid #333;\n");
        sb.append("  border-top: 6px solid #6cf;\n");
        sb.append("  border-radius: 50%;\n");
        sb.append("  width: 50px;\n");
        sb.append("  height: 50px;\n");
        sb.append("  animation: spin 1s linear infinite;\n");
        sb.append("  margin: auto;\n");
        sb.append("}\n");
        sb.append("@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }\n");

        // Content visibility after load
        sb.append("#content { display: none; padding: 30px; }\n");
        sb.append(".lazy { display: none; }\n");
        sb.append("</style>\n");

        // ================= JS =================
        sb.append("<script>\n");

        sb.append("document.addEventListener('DOMContentLoaded', () => {\n");
        sb.append("  document.querySelectorAll('summary').forEach(s => {\n");
        sb.append("    s.addEventListener('click', () => {\n");
        sb.append("      const div = s.nextElementSibling;\n");
        sb.append("      if(div && div.classList.contains('lazy')) {\n");
        sb.append("         div.style.display = 'block';\n");
        sb.append("         div.classList.remove('lazy');\n");
        sb.append("      }\n");
        sb.append("    });\n");
        sb.append("  });\n");
        sb.append("});\n");

        sb.append("window.onload = function() {\n");
        sb.append("  document.getElementById('spinner-container').style.display = 'none';\n");
        sb.append("  document.getElementById('content').style.display = 'block';\n");
        sb.append("};\n");

        sb.append("</script>\n");

        sb.append("</head>\n<body>\n");
    }

    private static void appendItems(Path outputPath, String fileName, String category, String ref, StringBuilder sb, List<Difference> items) throws IOException {
        if (items.isEmpty() || items.get(0).rowZero() == null) return;

        if (items.size() > 100) {
            String subReportFileName = fileName + "_" + ref.replaceAll("[^a-zA-Z0-9]", "_") + "_" + category.replaceAll("[^a-zA-Z0-9]", "_") + "_report.html";
            generateSubReport(outputPath.resolve(subReportFileName), ref, category, items);

            sb.append("<details>\n");
            sb.append("<summary><a href=\"").append(subReportFileName).append("\">").append(escape(ref)).append(" (").append(items.size()).append(")</a></summary>\n");
            sb.append("</details>\n");
        } else {
            generateItemDetails(category, ref, sb, items, true);
        }
    }

    private static void generateItemDetails(String category, String ref, StringBuilder sb, List<Difference> items, boolean isLazy) {
        if (isLazy) sb.append("<details>\n");
        sb.append("<summary>").append(escape(ref)).append(" (").append(items.size()).append(")</summary>\n");

        switch (category) {
            case "Missing Headers":
            case "Unknown Headers":
                sb.append("<ul>\n");
                for (Difference item : items) {
                    sb.append("<li>").append(escape(item.valueA() == null ? item.valueB() : item.valueA())).append("</li>\n");
                }
                sb.append("</ul>\n");
                break;

            case "Mismatched Headers":
                sb.append("<ul>\n");
                for (Difference item : items) {
                    sb.append("<li>Expected: ").append(escape(item.valueA())).append(" | Found: ").append(escape(item.valueB())).append("</li>\n");
                }
                sb.append("</ul>\n");
                break;

            case "Missing Rows":
            case "Unknown Rows":
                if (isLazy) sb.append("<div class='lazy'>\n");
                sb.append(renderTable(items));
                if (isLazy) sb.append("</div>\n");
                break;

            case "Mismatched Rows":
                if (isLazy) sb.append("<div class='lazy'>\n");
                sb.append(renderMismatchedTable(items));
                if (isLazy) sb.append("</div>\n");
                break;
        }

        if (isLazy) sb.append("</details>\n");
    }

    private static void generateSubReport(Path subReportPath, String ref, String category, List<Difference> items) throws IOException {
        StringBuilder sb = new StringBuilder();
        initHtml(sb);

        // ================= CONTENT =================
        sb.append("<h1>").append(ReportInfo.TITLE).append("</h1>\n");
        sb.append("<h2 style=\"margin-left: 25px;\">").append(ReportInfo.RESULTS).append("</h2>");

        sb.append("<div id='spinner-container'>\n");
        sb.append("  <div class='spinner'></div>\n");
        sb.append("  <div style='margin-top: 10px; color: #999;'>Loading report...</div>\n");
        sb.append("</div>\n");

        // ================= RESULTS =================
        sb.append("<div id='content'>\n");

        generateItemDetails(category, ref, sb, items, false);

        sb.append("</div>\n");
        sb.append("</body>\n</html>");
        WriteUtil.writeFile(subReportPath, sb.toString());
    }

    private static String renderTable(List<Difference> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='table-container'>\n");
        sb.append("<table>");
        sb.append("<thead><tr>");
        for (String h : items.getFirst().rowZero()) sb.append("<th>").append(escape(h)).append("</th>");
        sb.append("</tr></thead><tbody>");

        for (Difference item : items) {
            String[] row = item.rowA() == null ? item.rowB() : item.rowA();
            sb.append("<tr>");
            for (String col : row) {
                sb.append("<td>").append(escape(col)).append("</td>");
            }
            sb.append("</tr>");
        }

        sb.append("</tbody></table>\n</div>\n");
        return sb.toString();
    }

    private static String renderMismatchedTable(List<Difference> items) {

        StringBuilder sb = new StringBuilder();
        sb.append("<div class='table-container'>\n");
        sb.append("<table>");
        sb.append("<thead><tr>");
        for (String h : items.getFirst().rowZero()) sb.append("<th>").append(escape(h)).append("</th>");
        sb.append("</tr></thead><tbody>");

        items.forEach((item) -> {
            String[] aCols = item.rowA();
            String[] bCols = item.rowB();

            // Extracted (green)
            sb.append("<tr style='background-color:#c8e6c9;'>");
            for (int i = 0; i < aCols.length; i++) {
                String val = escape(aCols[i]);
                boolean diff = i < bCols.length &&
                        !aCols[i].equals(bCols[i]);
                if (diff)
                    sb.append("<td style='background-color:#fff176;'>").append(val).append("</td>");
                else
                    sb.append("<td>").append(val).append("</td>");
            }
            sb.append("</tr>");

            // Mod (red)
            sb.append("<tr style='background-color:#ffebee;'>");
            for (int i = 0; i < bCols.length; i++) {
                String val = escape(bCols[i]);
                boolean diff = i < aCols.length &&
                        !bCols[i].equals(aCols[i]);
                if (diff)
                    sb.append("<td style='background-color:#fff176;'>").append(val).append("</td>");
                else
                    sb.append("<td>").append(val).append("</td>");
            }
            sb.append("</tr>");
        });

        sb.append("</tbody></table>\n</div>\n");
        return sb.toString();
    }

    private static String escape(String s) {
        return s == null ? "" :
                s.replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace(">", "&gt;");
    }
}


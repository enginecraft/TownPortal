package org.enginecraft.service;

import lombok.Getter;
import lombok.Setter;
import org.enginecraft.objects.DataDictionary;
import org.enginecraft.objects.Difference;
import org.enginecraft.objects.DifferenceOverview;
import org.enginecraft.objects.DifferenceType;
import org.enginecraft.objects.IndexedRow;
import org.enginecraft.util.SqlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Getter
@Setter
public class LibraryService {
    private static final Logger log = LoggerFactory.getLogger(LibraryService.class);

    public final String name;
    public List<DataDictionary> library;

    public LibraryService(String name) {
        this.name = name
                .replace("\\", "_")
                .replace("/", "_")
                .replaceAll("(?i)\\.txt$", "")
                .replaceAll("[^a-zA-Z0-9_]", "_")
                .toUpperCase();

        library = loadLibraryRows();
    }

    public LibraryService(String name, Path toLoad) throws Exception {
        this.name = name;
        library = loadFiles(toLoad);
    }

    private String tableNormalize(String val) throws Exception {
        if (val == null || val.isEmpty()) return "TABLE";

        String normalized = val
                .replace("\\", "_")
                .replace("/", "_")
                .replaceAll("(?i)\\.txt$", "")
                .replaceAll("[^a-zA-Z0-9_]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_+|_+$", "");

        if (normalized.matches("^[0-9].*")) {
            normalized = "_" + normalized;
        }

        normalized = normalized.toUpperCase();

        if (normalized.isEmpty()) throw new Exception("Invalid value 'val'");
        return normalized;
    }

    private List<DataDictionary> loadLibraryRows() {
        return null;
    }

    private void createLibraryTable(
            Connection conn,
            String ref,
            String[] headers
    ) throws Exception {
        String tableName = tableNormalize(name + "_" + ref);
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS \"")
                .append(tableName)
                .append("\" (ROW_INDEX INTEGER NOT NULL, PATH VARCHAR(255), ");

        for (int i = 0; i < headers.length; i++) {
            sql.append("\"")
                    .append(headers[i].trim())
                    .append("\" TEXT");

            if (i < headers.length - 1) {
                sql.append(", ");
            }
        }

        sql.append(", PRIMARY KEY (ROW_INDEX, PATH));");

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql.toString());
            stmt.execute("CREATE INDEX IF NOT EXISTS IDX_" + tableName + "_PATH ON " + tableName + "(PATH)");
        }
    }

    private List<String[]> insertLibraryRows(
            Connection conn,
            String ref,
            String[] headers,
            BufferedReader reader
    ) throws Exception {
        List<String[]> rows = new ArrayList<>();
        int batchSize = 1000;
        int count = 0;

        StringBuilder columnNames = new StringBuilder("ROW_INDEX, PATH");
        for (String h : headers) {
            columnNames.append(", \"").append(h.trim()).append("\"");
        }

        String placeholders = String.join(", ", Collections.nCopies(headers.length + 2, "?"));

        String tableName = tableNormalize(name + "_" + ref);
        String sql = "MERGE INTO \"" + tableName + "\" (" + columnNames + ") KEY(ROW_INDEX, PATH) VALUES (" + placeholders + ")";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split("\t", -1);
                if (values.length != headers.length) continue;
                ps.setInt(1, count);
                ps.setString(2, ref);
                for (int i = 0; i < headers.length; i++) {
                    ps.setString(i + 3, values[i]);
                }
                ps.addBatch();
                rows.add(values);
                count++;
                if (count % batchSize == 0) {
                    ps.executeBatch();
                }
            }
            ps.executeBatch();
        }

        log.info("Merged {} rows into {}", count, tableName);
        return rows;
    }

     private List<String[]> loadFile(Path toLoad, String ref) throws Exception {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(toLoad)) {
            String headerLine = br.readLine();
            if (headerLine == null) return null;

            String[] headers = headerLine.split("\t");
            rows.add(headers);
            if (headers.length < 2) return rows;

            try (Connection conn = SqlUtil.getConnection()) {
                createLibraryTable(conn, ref, headers);
                rows.addAll(insertLibraryRows(conn, ref, headers, br));
            }
        }

        return rows;
    }

    private List<DataDictionary> loadFiles(Path toLoad) throws IOException {
        try (Stream<Path> stream = Files.walk(toLoad)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".txt"))
                    .map(path -> {
                        String ref = toLoad
                                .relativize(path)
                                .toString()
                                .replace("\\", "/");
                        try {
                            List<String[]> data = loadFile(path, ref);
                            return new DataDictionary(ref, data, null);
                        } catch (Exception e) {
                            log.error("An error occurred loading '{}' at '{}': {}", ref, path, e.getMessage());
                            return new DataDictionary(ref, null, e.getMessage());
                        }
                    })
                    .collect(Collectors.toList());
        }
    }

    public DifferenceOverview compareTo(LibraryService lib) {
        Map<String, DataDictionary> aMap = toMap(this.library);
        Map<String, DataDictionary> bMap = toMap(lib.getLibrary());

        return new DifferenceOverview(
                name,
                lib.name,
                aMap.keySet()
                        .parallelStream()
                        .flatMap(ref -> compareDictionary(ref, aMap.get(ref), bMap.get(ref)).stream())
                        .collect(Collectors.toList())
        );
    }

    private Map<String, DataDictionary> toMap(List<DataDictionary> list) {
        return list.stream().collect(Collectors.toMap(DataDictionary::getRef, d -> d));
    }

    private List<Difference> compareDictionary(
            String ref,
            DataDictionary aDict,
            DataDictionary bDict
    ) {
        List<Difference> diffs = new ArrayList<>();

        if (bDict == null) {
            diffs.add(
                    new Difference(
                            DifferenceType.MISSING,
                            ref,
                            null,
                            null,
                            null,
                            null,
                            null,
                            "File missing in B",
                            null
                    )
            );
            return diffs;
        }

        List<String[]> aRows = aDict.getData();
        List<String[]> bRows = bDict.getData();

        if (!isValid(aRows) || !isValid(bRows)) {
            return diffs;
        }

        String[] aHeader = aRows.getFirst();
        String[] bHeader = bRows.getFirst();

        List<Difference> headerDiffs = compareHeaders(ref, aHeader, bHeader);
        if (!headerDiffs.isEmpty()) {
            return headerDiffs;
        }

        diffs.addAll(compareRowsById(ref, aHeader, aRows, bRows));

        return diffs;
    }

    private List<Difference> compareHeaders(
            String ref,
            String[] aHeader,
            String[] bHeader
    ) {
        List<Difference> diffs = new ArrayList<>();

        Map<String, Integer> aIndex = indexMap(aHeader);
        Map<String, Integer> bIndex = indexMap(bHeader);

        for (int i = 0; i < aHeader.length; i++) {
            if (!bIndex.containsKey(aHeader[i])) {
                diffs.add(
                        new Difference(
                                DifferenceType.MISSING,
                                ref,
                                aHeader,
                                aHeader,
                                null,
                                i,
                                0,
                                aHeader[i],
                                null
                        )
                );
            }
        }

        for (int i = 0; i < bHeader.length; i++) {
            if (!aIndex.containsKey(bHeader[i])) {
                diffs.add(
                        new Difference(
                                DifferenceType.UNKNOWN,
                                ref,
                                aHeader,
                                null,
                                bHeader,
                                i,
                                0,
                                null,
                                bHeader[i]
                        )
                );
            }
        }

        if (!diffs.isEmpty()) return diffs;

        for (int i = 0; i < aHeader.length; i++) {
            if (!Objects.equals(aHeader[i], bHeader[i])) {
                diffs.add(
                        new Difference(
                                DifferenceType.MISMATCH,
                                ref,
                                aHeader,
                                aHeader,
                                bHeader,
                                i,
                                0,
                                aHeader[i],
                                bHeader[i]
                        )
                );
            }
        }

        return diffs;
    }

    private List<Difference> compareRowsById(
            String ref,
            String[] header,
            List<String[]> aRows,
            List<String[]> bRows) {

        List<Difference> diffs = new ArrayList<>();

        Map<String, IndexedRow> aMap = rowMap(aRows);
        Map<String, IndexedRow> bMap = rowMap(bRows);

        for (String id : aMap.keySet()) {
            if (!bMap.containsKey(id)) {
                IndexedRow ir = aMap.get(id);
                diffs.add(
                        new Difference(
                                DifferenceType.MISSING,
                                ref,
                                header,
                                ir.row(),
                                null,
                                null,
                                ir.index(),
                                id,
                                null
                        )
                );
            }
        }

        for (String id : bMap.keySet()) {
            if (!aMap.containsKey(id)) {
                IndexedRow ir = bMap.get(id);
                diffs.add(
                        new Difference(
                                DifferenceType.UNKNOWN,
                                ref,
                                header,
                                null,
                                ir.row(),
                                null,
                                ir.index(),
                                null,
                                id
                        )
                );
            }
        }

        for (String id : aMap.keySet()) {
            if (!bMap.containsKey(id)) continue;
            IndexedRow ir = aMap.get(id);

            String[] aRow = ir.row();
            String[] bRow = bMap.get(id).row();

            for (int col = 1; col < header.length; col++) {
                if (!Objects.equals(aRow[col], bRow[col])) {
                    diffs.add(
                            new Difference(
                                    DifferenceType.MISMATCH,
                                    ref,
                                    header,
                                    aRow,
                                    bRow,
                                    col,
                                    ir.index(),
                                    aRow[col],
                                    bRow[col]
                            )
                    );
                }
            }
        }

        return diffs;
    }

    private boolean isValid(List<String[]> rows) {
        return rows != null && rows.size() > 1;
    }

    private Map<String, Integer> indexMap(String[] header) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            map.put(header[i], i);
        }
        return map;
    }

    private Map<String, IndexedRow> rowMap(List<String[]> rows) {
        return IntStream.range(1, rows.size())
                .boxed()
                .collect(Collectors.toMap(
                        i -> rows.get(i)[0],
                        i -> new IndexedRow(i, rows.get(i)),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

}

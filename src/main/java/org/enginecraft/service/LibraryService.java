package org.enginecraft.service;

import lombok.Getter;
import lombok.Setter;
import org.enginecraft.objects.DataDictionary;
import org.enginecraft.objects.Difference;
import org.enginecraft.objects.DifferenceOverview;
import org.enginecraft.objects.DifferenceType;
import org.enginecraft.objects.IndexedRow;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
    public final String name;
    public List<DataDictionary> dictionaries;

    public LibraryService(String name, Path toLoad) throws IOException {
        this.name = name;
        dictionaries = loadFiles(toLoad);
    }

     private List<String[]> loadFile(Path toLoad) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(toLoad)) {
            String line;
            while ((line = br.readLine()) != null) {
                rows.add(line.split("\t", -1));
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
                            List<String[]> data = loadFile(path);
                            return new DataDictionary(ref, data, null);
                        } catch (IOException e) {
                            return new DataDictionary(ref, null, e.getMessage());
                        }
                    })
                    .collect(Collectors.toList());
        }
    }

    public DifferenceOverview compareTo(LibraryService lib) {
        Map<String, DataDictionary> aMap = toMap(this.dictionaries);
        Map<String, DataDictionary> bMap = toMap(lib.getDictionaries());

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

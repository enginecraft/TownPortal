package org.enginecraft;

import org.enginecraft.service.LibraryService;
import org.enginecraft.util.HtmlReportUtil;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TownPortal {
    public static void main(String[] args) throws IOException {
        LibraryService d2r = new LibraryService("D2R (91636)", Paths.get(".\\extracted\\91636"));
        Map<String, String> modDirs = Map.of(
                "Reimagined", "D:\\Diablo II Resurrected\\mods\\Reimagined\\Reimagined.mpq",
                "Eastern_Sun_Resurrected", "C:\\D2RMM 1.8.0\\mods\\Eastern_Sun_Resurrected"
        );

        final List<CompletableFuture<Void>> futures = new ArrayList<>();
        modDirs.forEach((k, v) ->
                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        LibraryService mod = new LibraryService(k, Paths.get(v));
                        HtmlReportUtil.generate(Paths.get("./docs"), k, d2r.compareTo(mod));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }))
        );
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }
}

package org.enginecraft;

import org.enginecraft.service.LibraryService;
import org.enginecraft.util.HtmlReportUtil;

import java.io.IOException;
import java.nio.file.Paths;

public class TownPortal {
    public static void main(String[] args) throws IOException {
        LibraryService d2r = new LibraryService("D2R", Paths.get(".\\extracted\\91636"));
        LibraryService easternSun = new LibraryService("Eastern Sun", Paths.get("C:\\D2RMM 1.8.0\\mods\\Eastern_Sun_Resurrected"));
        HtmlReportUtil.generate(Paths.get("./docs"), "Eastern_Sun_Resurrected", d2r.compareTo(easternSun));
    }
}

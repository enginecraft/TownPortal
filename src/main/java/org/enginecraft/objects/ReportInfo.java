package org.enginecraft.objects;

public class ReportInfo {
    public static final String TITLE = "Library Comparison Report";
    public static final String SUBTITLE = "This report reveals differences between two D2R data folders. Click categories/files to expand.";
    public static final String DESCRIPTION =
        "* Missing Headers/Rows - Information on if any of the library 'A' columns/rows are missing in library 'B'" +
        "\n* Unknown Headers/Rows - Information on if any of the library 'B' columns/rows are missing in library 'A'" +
        "\n* Mismatched Headers/Rows - Information on if library 'A' and library 'B' have different values on matched rows" +
        "\n* Color Codes - Green row symbolizes library 'A' reference. Red row symbolizes library 'B' reference. Yellow cell symbolizes value with issue." +
        "\n* Large Quantity Dropdowns - These are actually links to sub pages in case the results exceed 100 for a file category, allowing for faster page load";

    public static final String RESULTS = "Results";
}

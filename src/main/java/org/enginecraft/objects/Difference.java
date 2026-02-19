package org.enginecraft.objects;

import java.util.Arrays;

public record Difference(
        DifferenceType type,
        String ref,
        String[] rowZero,
        String[] rowA,
        String[] rowB,
        Integer cIndex,
        Integer rIndex,
        String valueA,
        String valueB
) {
    @Override
    public String toString() {
        return "Difference[" +
                "type=" + type +
                ", ref='" + ref + '\'' +
                ", rowA=" + Arrays.toString(rowA) +
                ", rowB=" + Arrays.toString(rowB) +
                ", cIndex=" + cIndex +
                ", rIndex=" + rIndex +
                ", valueA='" + valueA + '\'' +
                ", valueB='" + valueB + '\'' +
                ']';
    }
}

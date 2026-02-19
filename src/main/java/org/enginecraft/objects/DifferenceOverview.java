package org.enginecraft.objects;

import java.util.List;

public record DifferenceOverview(String libA, String libB, List<Difference> differences) {
}

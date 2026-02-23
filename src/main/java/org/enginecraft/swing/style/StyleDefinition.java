package org.enginecraft.swing.style;

import lombok.Getter;
import lombok.NonNull;

import java.awt.Color;
import java.util.List;

@Getter
public class StyleDefinition {
    private final Color background;
    private final Color foreground;
    private final Color highlightBackground;
    private final Color highlightForeground;
    private final List<StyleComponent> components;

    public StyleDefinition(
            @NonNull Color background,
            @NonNull Color foreground,
            Color highlightBackground,
            Color highlightForeground,
            List<StyleComponent> components
    ) throws IllegalArgumentException {
        this.background = background;
        this.foreground = foreground;
        this.highlightBackground = highlightBackground == null ? background : highlightBackground;
        this.highlightForeground = highlightForeground == null ? foreground : highlightForeground;
        this.components = components;
    }
}

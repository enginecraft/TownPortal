package org.enginecraft.swing.style;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@Getter
@Setter
public class StyleComponent {
    private final JComponent component;
    private Color background, foreground, highlightBackground, highlightForeground;

    public StyleComponent(
            @NonNull JComponent component,
            @NonNull StyleDefinition definition,
            boolean isOpaque
    ) {
        component.setOpaque(isOpaque);
        this.component = component;
        applyStyle(definition);
        setup();
    }

    private void setup() {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                component.setBackground(highlightBackground);
                component.setForeground(highlightForeground);
                component.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                component.setBackground(background);
                component.setForeground(foreground);
                component.repaint();
            }
        });
    }

    public void applyStyle(@NonNull StyleDefinition definition) {
        this.background = definition.getBackground();
        this.foreground = definition.getForeground();
        this.highlightBackground = definition.getHighlightBackground();
        this.highlightForeground = definition.getHighlightForeground();
        component.setBackground(background);
        component.setForeground(foreground);
        component.repaint();
    }
}

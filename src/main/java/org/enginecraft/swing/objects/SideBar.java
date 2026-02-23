package org.enginecraft.swing.objects;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;

@Getter
@Setter
public class SideBar {
    private final JFrame frame;
    private final JPanel panel;

    private Color background;
    private Color foreground;
    private Color highlight;

    public SideBar(
            JFrame frame,
            Color background,
            Color foreground,
            Color highlight
    ) {
        this.frame = frame;
        this.background = background;
        this.foreground = foreground;
        this.highlight = highlight;

        panel = new JPanel();
        setup();
    }

    private void setup() {
        panel.setLayout(null);
        panel.setLocation(2, TopBar.HEIGHT + 2);
        panel.setSize(new Dimension((int) (frame.getWidth() * .3), frame.getHeight() - TopBar.HEIGHT - 4));
        panel.setPreferredSize(new Dimension(panel.getWidth(), panel.getHeight()));
        panel.setBackground(background);

        frame.add(panel);
    }
}

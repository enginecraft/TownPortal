package org.enginecraft.swing.objects;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;

@Getter
@Setter
public class ContentArea {
    private final JFrame frame;
    private final JPanel panel;
    private SideBar sideBar;

    private Color background;
    private Color foreground;
    private Color highlight;

    public ContentArea(
            JFrame frame,
            SideBar sideBar,
            Color background,
            Color foreground,
            Color highlight
    ) {
        this.frame = frame;
        this.sideBar = sideBar;
        this.background = background;
        this.foreground = foreground;
        this.highlight = highlight;

        panel = new JPanel();
        setup();
    }

    private void setup() {
        panel.setLayout(null);
        panel.setLocation(sideBar.getPanel().getWidth() + 4, TopBar.HEIGHT + 2);
        panel.setSize(new Dimension(frame.getWidth() - sideBar.getPanel().getWidth() - 6, frame.getHeight() - TopBar.HEIGHT - 4));
        panel.setPreferredSize(new Dimension(panel.getWidth(), panel.getHeight()));
        panel.setBackground(background);

        frame.add(panel);
    }
}

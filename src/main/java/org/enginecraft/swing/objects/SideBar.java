package org.enginecraft.swing.objects;

import lombok.Getter;
import lombok.Setter;
import org.enginecraft.swing.util.ColorUtil;

import javax.swing.*;
import java.awt.*;

@Getter
@Setter
public class SideBar {
    private final JFrame frame;
    private final JPanel panel;
    private final JPanel buttonBar;

    private Color background;
    private Color foreground;
    private Color highlight;
    private Color buttonBarColor;

    private Navigation navigation;

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
        buttonBarColor = ColorUtil.lighten(background, .1f);

        panel = new JPanel();
        buttonBar = new JPanel();
        setup();
    }

    private void setup() {
        panel.setLayout(null);
        panel.setLocation(2, TopBar.HEIGHT + 2);
        panel.setSize(new Dimension((int) (frame.getWidth() * .3), frame.getHeight() - TopBar.HEIGHT - 4));
        panel.setPreferredSize(new Dimension(panel.getWidth(), panel.getHeight()));
        panel.setBackground(background);

        buttonBar.setLayout(null);
        buttonBar.setLocation(0, 0);
        buttonBar.setSize(panel.getWidth(), (int) (MenuItem.DEFAULT_HEIGHT / 2.0));
        buttonBar.setPreferredSize(new Dimension(panel.getWidth(), (int) (MenuItem.DEFAULT_HEIGHT / 2.0)));
        buttonBar.setBackground(buttonBarColor);

        navigation = new Navigation(
                frame,
                buttonBar,
                null,
                TownPortalNavigation.SIDE_BAR_DEFINITIONS,
                buttonBarColor,
                foreground,
                highlight,
                panel.getX(),
                panel.getY()
        );

        panel.add(buttonBar);
        frame.add(panel);
    }
}

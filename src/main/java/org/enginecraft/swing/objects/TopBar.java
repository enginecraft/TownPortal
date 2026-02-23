package org.enginecraft.swing.objects;

import lombok.Getter;
import lombok.Setter;
import org.enginecraft.swing.TownPortalFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

@Getter
@Setter
public class TopBar {
    public static final int HEIGHT = 40;

    private final JFrame frame;
    private final String appName, iconLoc;
    private final JPanel panel;

    private Color background, foreground, highlight, exitColor;

    private Navigation navigation;
    boolean navShown = false;

    public TopBar(
            JFrame frame,
            String appName,
            String iconLoc,
            Color background,
            Color foreground,
            Color highlight,
            Color exitColor
    ) {
        this.frame = frame;
        this.appName = appName;
        this.iconLoc = iconLoc;
        this.background = background;
        this.foreground = foreground;
        this.highlight = highlight;
        this.exitColor = exitColor;

        panel = new JPanel();
        setup();
    }

    public void setup() {
        panel.setLayout(null);
        panel.setLocation(0, 0);
        panel.setSize(new Dimension(frame.getWidth(), HEIGHT));
        panel.setPreferredSize(new Dimension(frame.getWidth(), HEIGHT));
        panel.setBackground(background);

        URL icon = TownPortalFrame.class.getResource(iconLoc);
        int xBuffer = 5;
        if (icon != null) {
            ImageIcon gifIcon = new ImageIcon(icon);

            JLabel gifLabel = new JLabel(gifIcon);
            gifLabel.setBounds(xBuffer + 5, 5, gifIcon.getIconWidth(), gifIcon.getIconHeight());
            panel.add(gifLabel);

            xBuffer += gifIcon.getIconWidth() + 10;
        }

        JLabel label = new JLabel(appName);
        label.setLocation(xBuffer, 0);
        label.setSize((int) label.getPreferredSize().getWidth(), HEIGHT);
        label.setForeground(foreground);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(label);

        JLabel menu = new JLabel("☰");
        menu.setLocation(xBuffer + label.getWidth() + 15, 0);
        menu.setSize(30, HEIGHT);
        menu.setForeground(foreground);
        menu.setFont(new Font("Segoe UI Symbol", Font.BOLD, 24));
        menu.setHorizontalAlignment(SwingConstants.CENTER);

        navigation = new Navigation(
                frame,
                panel,
                menu,
                TownPortalNavigation.TOP_BAR_DEFINITIONS,
                background,
                foreground,
                highlight,
                menu.getX() + menu.getWidth() + 15,
                0
        );

        JLabel exit = new JLabel("  ☠  ");
        exit.setForeground(foreground);
        exit.setFont(new Font("Segoe UI Symbol", Font.BOLD, 24));
        exit.setHorizontalAlignment(SwingConstants.CENTER);
        int width = (int) exit.getPreferredSize().getWidth();
        exit.setLocation(panel.getWidth() - width, 0);
        exit.setSize(width, HEIGHT);

        exit.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.exit(0);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                exit.setBackground(exitColor);
                exit.setOpaque(true);
                exit.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                exit.setBackground(background);
                exit.setOpaque(false);
                exit.repaint();
            }
        });
        panel.add(exit);
    }
}

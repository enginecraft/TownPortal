package org.enginecraft.swing.objects;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.enginecraft.swing.TownPortalFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;

@Getter
@Setter
public class TopBar {
    public static final int HEIGHT = 40;

    private final JFrame frame;
    private final String appName, iconLoc;
    private final JPanel panel;

    private Color background;
    private Color foreground;
    private Color highlight;

    boolean navShown = false;

    public TopBar(
            JFrame frame,
            String appName,
            String iconLoc,
            Color background,
            Color foreground,
            Color highlight
    ) {
        this.frame = frame;
        this.appName = appName;
        this.iconLoc = iconLoc;
        this.background = background;
        this.foreground = foreground;
        this.highlight = highlight;

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

        java.util.List<NavigationBarItem> navigation = new ArrayList<>();
        for (int i = 0; i < TownPortalNavigation.DEFINITIONS.size(); i++) {
            MenuItemDefinition definition = TownPortalNavigation.DEFINITIONS.get(i);
            int width = menu.getX() + menu.getWidth() + 15 + i * NavigationBarItem.WIDTH;
            NavigationBarItem menuItem = new NavigationBarItem(
                    null,
                    frame,
                    definition,
                    background,
                    foreground,
                    highlight,
                    width,
                    0,
                    width,
                    NavigationBarItem.HEIGHT
            );
            menuItem.getPanel().setVisible(false);
            navigation.add(menuItem);

            final int THROTTLE_MS = 50;
            final MouseEvent[] lastEvent = {null};
            Timer hoverTimer = new Timer(THROTTLE_MS, ae -> {
                MouseEvent e = lastEvent[0];
                if (e == null) return;

                JPanel hoveredPanel = findDeepestPanel(e);
                if (hoveredPanel != null && NavigationBarItem.isSubItem(hoveredPanel, menuItem)) return;
                menuItem.subReset(null);
            });
            hoverTimer.start();

            Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
                if (event instanceof MouseEvent e && e.getID() == MouseEvent.MOUSE_MOVED) {
                    lastEvent[0] = e;
                }
            }, AWTEvent.MOUSE_MOTION_EVENT_MASK);

            JLayeredPane layeredPane = frame.getLayeredPane();
            layeredPane.add(menuItem.getPanel(), JLayeredPane.POPUP_LAYER);
        }

        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (event instanceof MouseEvent e && e.getID() == MouseEvent.MOUSE_CLICKED) {
                Component source = e.getComponent();
                if (!SwingUtilities.isDescendingFrom(source, frame)) return;

                Point framePoint = SwingUtilities.convertPoint(source, e.getPoint(), frame);
                Component deepest = SwingUtilities.getDeepestComponentAt(frame, framePoint.x, framePoint.y);
                if (deepest == menu) return;

                JPanel clickedPanel = null;
                while (deepest != null) {
                    if (deepest instanceof JPanel p) {
                        clickedPanel = p;
                        break;
                    }
                    deepest = deepest.getParent();
                }

                for (NavigationBarItem item : navigation) {
                    if (clickedPanel != null && NavigationBarItem.isSubItem(clickedPanel, item)) return;
                    item.subReset(null);
                }

                for (NavigationBarItem item : navigation) {
                    item.getPanel().setVisible(false);
                }

                navShown = false;
            }
        }, AWTEvent.MOUSE_EVENT_MASK);

        menu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (navShown) {
                    for (NavigationBarItem item : navigation) {
                        item.subReset(null);
                        item.getPanel().setVisible(false);
                    }
                }
                else {
                    for (NavigationBarItem item : navigation) {
                        item.getPanel().setVisible(true);
                    }
                }

                navShown = !navShown;
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                menu.setBackground(highlight);
                menu.setOpaque(true);
                menu.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                menu.setBackground(background);
                menu.setOpaque(false);
                menu.repaint();
            }
        });

        panel.add(menu);
        frame.add(panel);
        frame.repaint();
    }

    public JPanel findDeepestPanel(@NonNull MouseEvent e) {
        Component source = e.getComponent();
        if (!SwingUtilities.isDescendingFrom(source, frame)) return null;

        Point framePoint = SwingUtilities.convertPoint(source, e.getPoint(), frame);
        Component deepest = SwingUtilities.getDeepestComponentAt(frame, framePoint.x, framePoint.y);

        JPanel hoveredPanel = null;
        while (deepest != null) {
            if (deepest instanceof JPanel p) {
                hoveredPanel = p;
                break;
            }
            deepest = deepest.getParent();
        }

        return hoveredPanel;
    }
}

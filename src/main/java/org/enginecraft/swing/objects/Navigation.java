package org.enginecraft.swing.objects;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Navigation {
    private final JFrame frame;
    private final JPanel panel;
    private final JLabel menu;
    private final List<MenuItemDefinition> definitions;
    private final int xOffset, yOffset;

    private Color background, foreground, highlight;

    private List<MenuItem> navigation;
    private boolean navShown = false;

    public Navigation(
            JFrame frame,
            JPanel panel,
            JLabel menu,
            List<MenuItemDefinition> definitions,
            Color background,
            Color foreground,
            Color highlight,
            int xOffset,
            int yOffset
    ) {
        this.frame = frame;
        this.panel = panel;
        this.menu = menu;
        this.definitions = definitions;
        this.background = background;
        this.foreground = foreground;
        this.highlight = highlight;
        this.xOffset = xOffset;
        this.yOffset = yOffset;

        setup();
    }

    public void setup() {
        navigation = new ArrayList<>();
        for (int i = 0; i < definitions.size(); i++) {
            MenuItemDefinition definition = definitions.get(i);
            int height = menu == null ? (int) (MenuItem.DEFAULT_HEIGHT / 2.0) : MenuItem.DEFAULT_HEIGHT;
            int x = xOffset + i * MenuItem.DEFAULT_WIDTH;
            MenuItem menuItem = new MenuItem(
                    null,
                    frame,
                    definition,
                    background,
                    foreground,
                    highlight,
                    x,
                    yOffset,
                    x,
                    yOffset + height,
                    MenuItem.DEFAULT_WIDTH,
                    height
            );
            menuItem.getPanel().setVisible(menu == null);
            navigation.add(menuItem);

            final int THROTTLE_MS = 50;
            final MouseEvent[] lastEvent = {null};
            Timer hoverTimer = new Timer(THROTTLE_MS, ae -> {
                MouseEvent e = lastEvent[0];
                if (e == null) return;

                JPanel hoveredPanel = findDeepestPanel(e);
                if (hoveredPanel != null && MenuItem.isSubItem(hoveredPanel, menuItem)) return;
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
                if (menu != null && deepest == menu) return;

                JPanel clickedPanel = null;
                while (deepest != null) {
                    if (deepest instanceof JPanel p) {
                        clickedPanel = p;
                        break;
                    }
                    deepest = deepest.getParent();
                }

                for (MenuItem item : navigation) {
                    if (clickedPanel != null && MenuItem.isSubItem(clickedPanel, item)) return;
                    item.subReset(null);
                }

                if (menu != null) {
                    for (MenuItem item : navigation) {
                        item.getPanel().setVisible(false);
                    }

                    navShown = false;
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK);

        if (menu != null) {
            menu.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (navShown) {
                        for (MenuItem item : navigation) {
                            item.subReset(null);
                            item.getPanel().setVisible(false);
                        }
                    } else {
                        for (MenuItem item : navigation) {
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
        }

        if (menu != null) panel.add(menu);
        frame.add(panel);
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

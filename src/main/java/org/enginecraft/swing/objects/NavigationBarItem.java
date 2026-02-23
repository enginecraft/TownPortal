package org.enginecraft.swing.objects;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.enginecraft.swing.util.ColorUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class NavigationBarItem {
    public static final int WIDTH = 100;
    public static final int HEIGHT = 40;

    public static boolean isSubItem(JPanel toMatch, NavigationBarItem item) {
        if (toMatch == item.getPanel()) return true;
        List<NavigationBarItem> subItems = item.getSubItems();
        if (subItems != null) {
            for (NavigationBarItem subItem : subItems) {
                if (isSubItem(toMatch, subItem)) return true;
            }
        }

        return false;
    }

    private final MenuItemDefinition definition;
    private Color background, foreground, highlight;
    private int x, y, subX, subY;

    private final NavigationBarItem parent;
    private final JFrame frame;
    private final JPanel panel = new JPanel();
    private final JLabel label;
    private final JLabel arrow = new JLabel("▶");
    private List<NavigationBarItem> subItems = null;
    private JPanel subItemsPanel = null;
    boolean entered = false;

    public NavigationBarItem(
            NavigationBarItem parent,
            @NonNull JFrame frame,
            @NonNull MenuItemDefinition definition,
            @NonNull Color background,
            @NonNull Color foreground,
            @NonNull Color highlight,
            int x,
            int y,
            int subX,
            int subY
    ) {
        this.parent = parent;
        this.frame = frame;
        this.definition = definition;
        this.background = background;
        this.foreground = foreground;
        this.highlight = highlight;
        this.x = x;
        this.y = y;
        this.subX = subX;
        this.subY = subY;

        label = new JLabel(definition.getDisplayText());
        setup();
    }

    private void setup() {
        panel.setSize(WIDTH, HEIGHT);
        panel.setLayout(null);

        label.setLayout(null);
        label.setLocation(5, 0);
        label.setSize(WIDTH - 5, HEIGHT);
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setVerticalAlignment(SwingConstants.CENTER);
        panel.add(label);

        panel.setLocation(x, y);

        List<MenuItemDefinition> subDefinitions = definition.getSubDefinitions();
        if (subDefinitions != null && !subDefinitions.isEmpty()) {
            subItems = new ArrayList<>();
            subItemsPanel = new JPanel();
            subItemsPanel.setLayout(null);
            subItemsPanel.setLocation(subX, subY);
            for (int i = 0; i < subDefinitions.size(); i++) {
                MenuItemDefinition subDefinition = subDefinitions.get(i);
                Color[] colors = ColorUtil.adjustColors(background, foreground, true, 0.05f, 0.25);
                Color[] highlights = ColorUtil.adjustColors(highlight, foreground, true, 0.05f, 0.25);
                NavigationBarItem subItem = new NavigationBarItem(
                        this,
                        frame,
                        subDefinition,
                        colors[0],
                        colors[1],
                        highlights[0],
                        0,
                        HEIGHT * i,
                        subX + WIDTH,
                        subY + HEIGHT * i
                );
                subItems.add(subItem);
                subItemsPanel.add(subItem.panel);
            }

            subItemsPanel.setSize(WIDTH, subDefinitions.size() * HEIGHT);
            subItemsPanel.setVisible(false);
            JLayeredPane layeredPane = frame.getLayeredPane();
            layeredPane.add(subItemsPanel, JLayeredPane.POPUP_LAYER);
        }

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (definition.getActions() == null) return;

                NavigationBarItem menuItem = NavigationBarItem.this;
                while (menuItem.parent != null) {
                    menuItem = menuItem.parent;
                }
                menuItem.subReset(null);

                for (MenuAction menuAction : definition.getActions()) {
                    try {
                        menuAction.run();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Menu Option Failure: " + ex.getMessage());
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                entered = !entered;
                if (entered) {
                    parentReset(NavigationBarItem.this);
                    arrow.setText("▼");
                    onEnter();
                }
                else {
                    subReset(NavigationBarItem.this);
                    arrow.setText("▶");
                }

                if (subItems != null) subItemsPanel.setVisible(entered);
            }
        });
        refresh();

        arrow.setLocation(0, 0);
        arrow.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 6));
        arrow.setSize(WIDTH - 5, HEIGHT);
        arrow.setHorizontalAlignment(SwingConstants.RIGHT);
        arrow.setVerticalAlignment(SwingConstants.CENTER);
        if (subItems != null && !subItems.isEmpty()) panel.add(arrow);
        panel.setVisible(true);
    }

    public void onEnter() {
        panel.setBackground(highlight);
        label.setForeground(foreground);
        arrow.setForeground(foreground);
        panel.repaint();
    }

    public void onExit() {
        if (entered) return;
        panel.setBackground(background);
        label.setForeground(foreground);
        arrow.setForeground(foreground);
        panel.repaint();
    }

    public void refresh() {
        panel.setBackground(background);
        label.setForeground(foreground);
        arrow.setForeground(foreground);
        panel.repaint();
    }

    public void subReset(NavigationBarItem exclude) {
        entered = false;
        arrow.setText("▶");
        if (this != exclude) onExit();

        if (subItems != null) {
            subItemsPanel.setVisible(false);
            for (NavigationBarItem subItem : subItems) {
                subItem.subReset(exclude);
            }
        }
    }

    public void parentReset(NavigationBarItem exclude) {
        if (parent != null) {
            List<NavigationBarItem> subMenu = parent.getSubItems();
            for (NavigationBarItem item : subMenu) {
                if (item == exclude) continue;
                item.subReset(null);
            }
        }
    }
}

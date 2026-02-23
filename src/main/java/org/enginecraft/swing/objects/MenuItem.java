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
public class MenuItem {
    public static final int DEFAULT_WIDTH = 100;
    public static final int DEFAULT_HEIGHT = 40;

    public final int width;
    public final int height;

    public static boolean isSubItem(JPanel toMatch, MenuItem item) {
        if (toMatch == item.getPanel()) return true;
        List<MenuItem> subItems = item.getSubItems();
        if (subItems != null) {
            for (MenuItem subItem : subItems) {
                if (isSubItem(toMatch, subItem)) return true;
            }
        }

        return false;
    }

    private final MenuItemDefinition definition;
    private Color background, foreground, highlight;
    private int x, y, subX, subY;

    private final MenuItem parent;
    private final JFrame frame;
    private final JPanel panel = new JPanel();
    private final JLabel label;
    private final JLabel arrow = new JLabel("▶");
    private List<MenuItem> subItems = null;
    private JPanel subItemsPanel = null;
    boolean entered = false;

    public MenuItem(
            MenuItem parent,
            @NonNull JFrame frame,
            @NonNull MenuItemDefinition definition,
            @NonNull Color background,
            @NonNull Color foreground,
            @NonNull Color highlight,
            int x,
            int y,
            int subX,
            int subY,
            Integer width,
            Integer height
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
        this.width = width == null ? DEFAULT_WIDTH : width;
        this.height = height == null ? DEFAULT_HEIGHT : height;

        label = new JLabel(definition.getDisplayText());
        setup();
    }

    private void setup() {
        panel.setSize(width, height);
        panel.setLayout(null);

        label.setLayout(null);
        label.setLocation(5, 0);
        label.setSize(width - 5, height);
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
                MenuItem subItem = new MenuItem(
                        this,
                        frame,
                        subDefinition,
                        colors[0],
                        colors[1],
                        highlights[0],
                        0,
                        height * i,
                        subX + width,
                        subY + height * i,
                        width,
                        height
                );
                subItems.add(subItem);
                subItemsPanel.add(subItem.panel);
            }

            subItemsPanel.setSize(width, subDefinitions.size() * height);
            subItemsPanel.setVisible(false);
            JLayeredPane layeredPane = frame.getLayeredPane();
            layeredPane.add(subItemsPanel, JLayeredPane.POPUP_LAYER);
        }

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (definition.getActions() == null) return;

                MenuItem menuItem = MenuItem.this;
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
                    parentReset(MenuItem.this);
                    arrow.setText("▼");
                    onEnter();
                }
                else {
                    subReset(MenuItem.this);
                    arrow.setText("▶");
                }

                if (subItems != null) subItemsPanel.setVisible(entered);
            }
        });
        refresh();

        arrow.setLocation(0, 0);
        arrow.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 6));
        arrow.setSize(width - 5, height);
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

    public void subReset(MenuItem exclude) {
        entered = false;
        arrow.setText("▶");
        if (this != exclude) onExit();

        if (subItems != null) {
            subItemsPanel.setVisible(false);
            for (MenuItem subItem : subItems) {
                subItem.subReset(exclude);
            }
        }
    }

    public void parentReset(MenuItem exclude) {
        if (parent != null) {
            List<MenuItem> subMenu = parent.getSubItems();
            for (MenuItem item : subMenu) {
                if (item == exclude) continue;
                item.subReset(null);
            }
        }
    }
}

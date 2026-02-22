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
public class NavigationBarItem {
    public static final int WIDTH = 100;
    public static final int HEIGHT = 30;

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
    private Color backgroundColor, textColor, highlightBackgroundColor, highlightTextColor;
    private int x, y, subX, subY;

    private final NavigationBarItem parent;
    private final JFrame frame;
    private final JPanel panel = new JPanel();
    private final JLabel label;
    private final JLabel arrow = new JLabel("▶");
    private List<NavigationBarItem> subItems = null;
    private JPanel subItemsPanel = null;
    boolean isClicked = false;

    public NavigationBarItem(
            NavigationBarItem parent,
            @NonNull JFrame frame,
            @NonNull MenuItemDefinition definition,
            @NonNull Color backgroundColor,
            @NonNull Color textColor,
            @NonNull Color highlightBackgroundColor,
            @NonNull Color highlightTextColor,
            int x,
            int y,
            int subX,
            int subY
    ) {
        this.parent = parent;
        this.frame = frame;
        this.definition = definition;
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.highlightBackgroundColor = highlightBackgroundColor;
        this.highlightTextColor = highlightTextColor;
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
        label.setSize(WIDTH, HEIGHT);
        panel.add(label);

        panel.setLocation(x, y);

        List<MenuItemDefinition> subDefinitions = definition.getSubDefinitions();
        if (subDefinitions != null && !subDefinitions.isEmpty()) {
            subItems = new ArrayList<>();
            subItemsPanel = new JPanel();
            subItemsPanel.setLayout(null);
            subItemsPanel.setLocation(subX, subY);
            subItemsPanel.setBackground(Color.BLACK);
            for (int i = 0; i < subDefinitions.size(); i++) {
                MenuItemDefinition subDefinition = subDefinitions.get(i);
                NavigationBarItem subItem = new NavigationBarItem(
                        this,
                        frame,
                        subDefinition,
                        backgroundColor,
                        textColor,
                        highlightBackgroundColor,
                        highlightTextColor,
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
                isClicked = !isClicked;
                if (isClicked) {
                    parentReset(NavigationBarItem.this);
                    arrow.setText("▼");
                }
                else {
                    subReset(NavigationBarItem.this);
                    arrow.setText("▶");
                }
                onEnter();

                if (subItems == null) isClicked = false;
                else subItemsPanel.setVisible(isClicked);
                if (definition.getActions() == null) return;
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
                onEnter();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                onExit();
            }
        });
        refresh();

        arrow.setLocation(panel.getWidth() - 20, 0);
        arrow.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 6));
        arrow.setSize(20, 30);
        if (subItems != null && !subItems.isEmpty()) panel.add(arrow);
        panel.setVisible(true);
    }

    public void onEnter() {
        panel.setBackground(highlightBackgroundColor);
        label.setForeground(highlightTextColor);
        panel.repaint();
    }

    public void onExit() {
        if (isClicked) return;
        panel.setBackground(backgroundColor);
        label.setForeground(textColor);
        arrow.setForeground(textColor);
        panel.repaint();
    }

    public void refresh() {
        panel.setBackground(backgroundColor);
        label.setForeground(textColor);
        arrow.setForeground(textColor);
        panel.repaint();
    }

    public void subReset(NavigationBarItem exclude) {
        isClicked = false;
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

package org.enginecraft.swing.menu;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.enginecraft.swing.prompts.MessagePrompt;
import org.enginecraft.swing.style.StyleComponent;
import org.enginecraft.swing.style.StyleDefinition;
import org.enginecraft.swing.util.ColorUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

@Getter
@Setter
public class MenuDefinition {
    private final JFrame frame;
    private final String name;
    private final MenuShowType showType;
    private final List<MenuDefinition> subDefinitions;
    private final MenuAction action;
    private final StyleDefinition definition;
    private final int x, y, subX, subY;
    private final Integer widthOverride, heightOverride, horizontalAlignmentOverride, verticalAlignmentOverride;

    private MenuDefinition parent;
    private boolean visible, enabled, subVisible;

    private JPanel panel;
    private JPanel subPanel;
    private JLabel label;
    private StyleComponent panelStyle;
    private StyleComponent labelStyle;

    public MenuDefinition(
            JFrame frame,
            @NonNull String name,
            MenuShowType showType,
            List<MenuDefinition> subDefinitions,
            MenuAction action,
            @NonNull StyleDefinition definition,
            Integer x,
            Integer y,
            Integer subX,
            Integer subY,
            Integer horizontalAlignmentOverride,
            Integer verticalAlignmentOverride,
            Integer widthOverride,
            Integer heightOverride,
            boolean visible,
            boolean enabled
    ) {
        this.frame = frame;
        this.name = name;
        this.showType = showType == null ? MenuShowType.ON_CLICK : showType;
        this.subDefinitions = subDefinitions;
        this.action = action;
        this.definition = definition;
        this.x = x == null ? 0 : x;
        this.y = y == null ? 0 : y;
        this.subX = subX == null ? this.x : subX;
        this.subY = subY == null ? this.y : subY;
        this.horizontalAlignmentOverride = horizontalAlignmentOverride;
        this.verticalAlignmentOverride = verticalAlignmentOverride;
        this.widthOverride = widthOverride;
        this.heightOverride = heightOverride;
        this.visible = visible;
        this.enabled = enabled;

        panel = new JPanel();
        subPanel = (subDefinitions == null || subDefinitions.isEmpty()) ? null : new JPanel();
        label = new JLabel(name);

        setup();
    }

    public void setup() {
        panel.setLayout(null);
        panel.setLocation(x, y);
        panelStyle = new StyleComponent(panel, definition, true);
        setEnabled(enabled);

        label.setLayout(null);
        label.setLocation(0, 0);
        label.setHorizontalAlignment(horizontalAlignmentOverride == null ? SwingConstants.LEFT : horizontalAlignmentOverride);
        label.setVerticalAlignment(verticalAlignmentOverride == null ? SwingConstants.CENTER : verticalAlignmentOverride);
        label.setSize(
                widthOverride == null ? (int) label.getPreferredSize().getWidth() : widthOverride,
                heightOverride == null ? (int) label.getPreferredSize().getHeight() : heightOverride
        );
        labelStyle = new StyleComponent(label, definition, true);
        panel.add(label);

        if (subPanel != null) {
            subPanel.setLayout(null);

            for (MenuDefinition subDefinition : subDefinitions) {
                subDefinition.setParent(this);
                subPanel.add(subDefinition.getPanel());
            }

            subPanel.setSize(
                    widthOverride == null ? (int) label.getPreferredSize().getWidth() : widthOverride,
                    heightOverride == null ? (int) label.getPreferredSize().getHeight() : heightOverride
            );

            for (MenuDefinition subDefinition : subDefinitions) {
                subDefinition.getPanel().setSize(subPanel.getSize());
            }

            subPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    if (showType == MenuShowType.ON_HOVER) {
                        SwingUtilities.invokeLater(() -> handleHoverExit());
                    }
                }
            });

            setSubVisible(false);
            frame.add(subPanel);
        }

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (showType == MenuShowType.ON_CLICK) {
                    if (enabled && !subVisible) {
                        SwingUtilities.invokeLater(() -> openBranch(MenuDefinition.this));

                        if (action != null) {
                            try {
                                action.run();
                            } catch (Exception ex) {
                                MessagePrompt.send("Menu Action", "Failed to run action for '" + name + "' menu definition!");
                                throw new RuntimeException(ex);
                            }
                        }
                        return;
                    }

                    SwingUtilities.invokeLater(() -> closeBranch(MenuDefinition.this));
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (showType == MenuShowType.ON_HOVER && enabled) {
                    SwingUtilities.invokeLater(() -> openBranch(MenuDefinition.this));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (showType == MenuShowType.ON_HOVER) {
                    SwingUtilities.invokeLater(() -> handleHoverExit());
                }
            }
        });

        panel.setSize(
                widthOverride == null ? (int) label.getPreferredSize().getWidth() : widthOverride,
                heightOverride == null ? (int) label.getPreferredSize().getHeight() : heightOverride
        );
        updateSubPos(this);
        setVisible(visible);
        frame.add(panel);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            panelStyle.applyStyle(definition);
            labelStyle.applyStyle(definition);
        }
        else {
            Color invalidForeground =
                    ColorUtil.isDarker(definition.getBackground(), definition.getForeground()) ?
                            ColorUtil.darken(definition.getForeground(), .2f) :
                            ColorUtil.lighten(definition.getForeground(), .2f);

            panelStyle.applyStyle(
                    new StyleDefinition(
                            definition.getBackground(),
                            definition.getForeground(),
                            definition.getBackground(),
                            definition.getForeground(),
                            definition.getComponents()
                    )
            );

            labelStyle.applyStyle(
                    new StyleDefinition(
                            definition.getBackground(),
                            invalidForeground,
                            definition.getBackground(),
                            invalidForeground,
                            definition.getComponents()
                    )
            );
        }
    }

    public void setVisible(boolean visible) {
        if (this.visible == visible) return;

        this.visible = visible;
        updateSubPos(parent);
        panel.setVisible(visible);
    }

    public void setSubVisible(boolean visible) {
        if (subPanel == null || this.subVisible == visible || !enabled) return;

        subVisible = visible;
        subPanel.setVisible(visible);
    }

    public void updateSubPos(MenuDefinition definition) {
        updateSubPos(0, definition);
    }

    public void updateSubPos(int yOffset, MenuDefinition definition) {
        if (definition == null || definition.getSubPanel() == null) return;

        int yBuffer = 0;
        for (MenuDefinition def : definition.getSubDefinitions()) {
            JPanel panel = def.getPanel();
            panel.setLocation(panel.getX(), yBuffer);
            if (def.isVisible()) yBuffer += panel.getHeight();
        }

        if (definition.getY() == definition.getSubY() && definition.getX() == definition.getSubX()) {
            definition.getPanel().setLocation(definition.getPanel().getX(), yOffset);
            definition.getSubPanel().setLocation(definition.getPanel().getX(), definition.getPanel().getY() + definition.getPanel().getHeight());
            yOffset = definition.getPanel().getY() + definition.getPanel().getHeight() + definition.getSubPanel().getHeight();
            updateSubPos(yOffset, definition.getParent());
        }
        else {
            definition.getSubPanel().setLocation(definition.getSubX(), definition.getSubY());
        }
    }

    private void handleHoverExit() {
        PointerInfo pointerInfo = MouseInfo.getPointerInfo();
        if (pointerInfo == null) return;

        Point mouseLocation = pointerInfo.getLocation();
        SwingUtilities.convertPointFromScreen(mouseLocation, frame);

        if (!isMouseInsideVisibleBranch(this, mouseLocation)) {
            closeBranch(this);
        }
    }

    private void openBranch(MenuDefinition def) {
        MenuDefinition current = def;

        while (current.getParent() != null) {
            MenuDefinition parent = current.getParent();

            for (MenuDefinition sibling : parent.getSubDefinitions()) {
                if (sibling != current) {
                    closeBranch(sibling);
                }
            }

            current = parent;
        }

        def.setSubVisible(true);
    }

    private void closeBranch(MenuDefinition def) {
        if (def == null) return;

        def.setSubVisible(false);

        if (def.getSubDefinitions() != null) {
            for (MenuDefinition child : def.getSubDefinitions()) {
                closeBranch(child);
            }
        }
    }

    private boolean isMouseInsideVisibleBranch(MenuDefinition def, Point mousePoint) {
        if (def == null) return false;

        if (
                (
                        def.getPanel() != null &&
                        def.getPanel().isVisible() &&
                        def.getPanel().getBounds().contains(mousePoint)
                ) ||
                (
                        def.getSubPanel() != null &&
                        def.getSubPanel().isVisible() &&
                        def.getSubPanel().getBounds().contains(mousePoint)
                )
        ) return true;

        if (def.getSubDefinitions() != null) {
            for (MenuDefinition child : def.getSubDefinitions()) {
                if (isMouseInsideVisibleBranch(child, mousePoint)) {
                    return true;
                }
            }
        }

        return false;
    }
}

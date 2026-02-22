package org.enginecraft.swing;

import lombok.Getter;
import lombok.Setter;
import org.enginecraft.swing.objects.MenuItemDefinition;
import org.enginecraft.swing.objects.NavigationBarItem;
import org.enginecraft.swing.objects.TownPortalNavigation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

@Getter
@Setter
public class TownPortalFrame {
    private final String appName = "Town Portal";
    private final String icon = "";

    private final JFrame frame;
    private final JPanel topBar;

    public TownPortalFrame() {
        frame = new JFrame();
        topBar = new JPanel();
        setup();
    }

    public void setup() {
        frame.setLayout(null);
        frame.setUndecorated(true);
        frame.setSize(700, 500);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBackground(Color.WHITE);
        setupTopBar();
    }

    public void setupTopBar() {
        topBar.setLayout(null);
        topBar.setLocation(0, 0);
        topBar.setSize(new Dimension(frame.getWidth(), 30));
        topBar.setPreferredSize(new Dimension(frame.getWidth(), 30));
        topBar.setBackground(Color.BLACK);

        JLabel label = new JLabel(appName);
        label.setLocation(5, 5);
        label.setSize(label.getPreferredSize());
        label.setForeground(Color.WHITE);
        topBar.add(label);

        for (int i = 0; i < TownPortalNavigation.DEFINITIONS.size(); i++) {
            MenuItemDefinition definition = TownPortalNavigation.DEFINITIONS.get(i);
            NavigationBarItem menuItem = new NavigationBarItem(
                    null,
                    frame,
                    definition,
                    Color.BLACK,
                    Color.WHITE,
                    Color.DARK_GRAY,
                    Color.WHITE,
                    i * NavigationBarItem.WIDTH,
                    30,
                    i * NavigationBarItem.WIDTH,
                    60
            );

            Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
                if (event instanceof MouseEvent e && e.getID() == MouseEvent.MOUSE_CLICKED) {
                    Component source = e.getComponent();
                    if (!SwingUtilities.isDescendingFrom(source, frame)) return;

                    Point framePoint = SwingUtilities.convertPoint(source, e.getPoint(), frame);
                    Component deepest = SwingUtilities.getDeepestComponentAt(frame, framePoint.x, framePoint.y);
                    JPanel clickedPanel = null;
                    while (deepest != null) {
                        if (deepest instanceof JPanel panel) {
                            clickedPanel = panel;
                            break;
                        }
                        deepest = deepest.getParent();
                    }

                    if (clickedPanel != null && NavigationBarItem.isSubItem(clickedPanel, menuItem)) return;
                    menuItem.subReset(null);
                }
            }, AWTEvent.MOUSE_EVENT_MASK);

            JLayeredPane layeredPane = frame.getLayeredPane();
            layeredPane.add(menuItem.getPanel(), JLayeredPane.POPUP_LAYER);
        }
        frame.add(topBar);
    }

    public void setupMenuItems() {

    }

    public void toggle() {
        frame.setVisible(!frame.isVisible());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new TownPortalFrame().toggle();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Failed to start: " + e.getMessage());
            }
        });
    }
}

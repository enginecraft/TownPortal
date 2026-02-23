package org.enginecraft.swing;

import lombok.Getter;
import lombok.Setter;
import org.enginecraft.swing.objects.ContentArea;
import org.enginecraft.swing.objects.SideBar;
import org.enginecraft.swing.objects.TopBar;

import javax.swing.*;
import java.awt.*;

@Getter
@Setter
public class TownPortalFrame {
    private final String appName = "Town Portal";
    private final String iconLoc = "/images/Portal.gif";

    private final JFrame frame;
    private TopBar topBar;
    private SideBar sideBar;
    private ContentArea contentArea;
    private boolean navShown = false;

    private Color background = new Color(38, 38, 38);
    private Color foreground = new Color(227, 227, 227);
    private Color highlight = new Color(58, 58, 58);

    public TownPortalFrame() {
        frame = new JFrame();
        setup();
    }

    public void setup() {
        frame.setLayout(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true);
        frame.getContentPane().setBackground(new Color(71, 71, 71));

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gd.getDefaultConfiguration());
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        int x = insets.left;
        int y = insets.top;
        int width = screenSize.width - insets.left - insets.right;
        int height = screenSize.height - insets.top - insets.bottom;

        frame.setBounds(x, y, width, height);
        frame.setBackground(Color.WHITE);

        topBar = new TopBar(frame, "Town Portal", iconLoc, background, foreground, highlight);
        sideBar = new SideBar(frame, background, foreground, highlight);
        contentArea = new ContentArea(frame, sideBar, background, foreground, highlight);
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

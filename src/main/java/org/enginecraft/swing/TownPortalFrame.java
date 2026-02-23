package org.enginecraft.swing;

import lombok.Getter;
import lombok.Setter;
import org.enginecraft.swing.objects.ContentArea;
import org.enginecraft.swing.objects.SideBar;
import org.enginecraft.swing.objects.TopBar;

import javax.swing.*;

@Getter
@Setter
public class TownPortalFrame extends AbstractFrame {
    private TopBar topBar;
    private SideBar sideBar;
    private ContentArea contentArea;

    public TownPortalFrame() {
        super("Town Portal", "/images/Portal.gif");
    }

    @Override
    public void init() {
//        topBar = new TopBar(frame, "Town Portal", iconLoc, background, foreground, highlight, exitColor);
//        sideBar = new SideBar(frame, background, foreground, highlight);
//        contentArea = new ContentArea(frame, sideBar, background, foreground, highlight);
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

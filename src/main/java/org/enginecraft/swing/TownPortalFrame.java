package org.enginecraft.swing;

import lombok.Getter;
import lombok.Setter;
import org.enginecraft.swing.menu.MenuDefinition;
import org.enginecraft.swing.menu.MenuShowType;
import org.enginecraft.swing.objects.ContentArea;
import org.enginecraft.swing.objects.SideBar;
import org.enginecraft.swing.objects.TopBar;

import javax.swing.*;
import java.util.List;

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
        MenuDefinition menu = new MenuDefinition(
                frame,
                "File",
                MenuShowType.ON_HOVER,
                List.of(),
                null,
                baseHighlightDefinition,
                titleLabelOffset + 10, 0, 0, 0,
                null, null, 100, 30,
                true,
                true
        );
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

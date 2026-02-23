package org.enginecraft.swing;

import lombok.Getter;
import lombok.Setter;
import org.enginecraft.swing.style.StyleComponent;
import org.enginecraft.swing.style.StyleDefinition;
import org.enginecraft.swing.style.StyleRegistry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;

@Getter
@Setter
public abstract class AbstractFrame {
    public static final String LIGHT_MODE = "LIGHT_MODE";
    public static final String DARK_MODE = "DARK_MODE";
    protected final JFrame frame;
    private final String appName;
    private final String icon;

    protected List<StyleDefinition> styleDefinitions = new ArrayList<>();
    protected StyleDefinition baseDefinition = new StyleDefinition(
            new Color(38, 38, 38),
            new Color(227, 227, 227),
            null,
            null,
            new ArrayList<>()
    );

    protected StyleDefinition baseHighlightDefinition = new StyleDefinition(
            baseDefinition.getBackground(),
            baseDefinition.getForeground(),
            new Color(58, 58, 58),
            null,
            new ArrayList<>()
    );

    protected StyleDefinition exitDefinition = new StyleDefinition(
            baseDefinition.getBackground(),
            baseDefinition.getForeground(),
            new Color(175, 20, 20),
            null,
            new ArrayList<>()
    );

    protected JPanel titleBar;

    public AbstractFrame(String appName, String icon) {
        frame = new JFrame();
        this.appName = appName;
        this.icon = icon;
        setup();
    }

    public void setup() {
        // Set up frame
        frame.setLayout(null);
        frame.setUndecorated(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gd.getDefaultConfiguration());
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        int x = insets.left;
        int y = insets.top;
        int width = screenSize.width - insets.left - insets.right;
        int height = screenSize.height - insets.top - insets.bottom;

        frame.setBounds(x, y, width, height);

        // Set up title bar
        int titleBarHeight = 30;
        titleBar = new JPanel();
        titleBar.setLayout(null);
        titleBar.setLocation(0, 0);
        titleBar.setSize(new Dimension(frame.getWidth(), titleBarHeight));
        titleBar.setPreferredSize(new Dimension(frame.getWidth(), titleBarHeight));
        baseDefinition.getComponents().add(new StyleComponent(titleBar, baseDefinition, true));

        URL iconLocation = TownPortalFrame.class.getResource(icon);
        int xBuffer = 10;
        if (iconLocation != null) {
            ImageIcon gifIcon = new ImageIcon(iconLocation);
            JLabel gifLabel = new JLabel(gifIcon);
            gifLabel.setBounds(xBuffer, 0, gifIcon.getIconWidth(), gifIcon.getIconHeight());
            titleBar.add(gifLabel);
            xBuffer += gifIcon.getIconWidth() + 5;
        }

        JLabel titleBar_label = new JLabel(appName);
        titleBar_label.setLocation(xBuffer, 0);
        titleBar_label.setSize((int) titleBar_label.getPreferredSize().getWidth(), titleBarHeight);
        titleBar_label.setHorizontalAlignment(SwingConstants.CENTER);
        baseDefinition.getComponents().add(new StyleComponent(titleBar_label, baseDefinition, true));
        titleBar.add(titleBar_label);

        JLabel titleBar_exit = new JLabel("  ☠  ");
        titleBar_exit.setFont(new Font("Segoe UI Symbol", Font.BOLD, 24));
        titleBar_exit.setHorizontalAlignment(SwingConstants.CENTER);
        int titleBar_exit_width = (int) titleBar_exit.getPreferredSize().getWidth();
        titleBar_exit.setLocation(titleBar.getWidth() - titleBar_exit_width, 0);
        titleBar_exit.setSize(titleBar_exit_width, titleBarHeight);
        titleBar_exit.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.exit(0);
            }
        });
        exitDefinition.getComponents().add(new StyleComponent(titleBar_exit, exitDefinition, true));
        titleBar.add(titleBar_exit);

        init();

        styleDefinitions.add(baseDefinition);
        styleDefinitions.add(baseHighlightDefinition);
        styleDefinitions.add(exitDefinition);

        StyleRegistry.register(DARK_MODE, styleDefinitions);

        List<StyleDefinition> lightDefinitions = new ArrayList<>();
        for (StyleDefinition definition : styleDefinitions) {
            if (definition == exitDefinition) {
                lightDefinitions.add(
                        new StyleDefinition(
                                definition.getForeground(),
                                definition.getBackground(),
                                definition.getHighlightBackground(),
                                definition.getBackground(),
                                definition.getComponents()
                        )
                );
            }
            else {
                lightDefinitions.add(
                        new StyleDefinition(
                                definition.getForeground(),
                                definition.getBackground(),
                                definition.getHighlightForeground(),
                                definition.getHighlightBackground(),
                                definition.getComponents()
                        )
                );
            }
        }

        StyleRegistry.register(LIGHT_MODE, lightDefinitions);
        frame.add(titleBar);
    }

    public abstract void init();

    public void toggle() {
        frame.setVisible(!frame.isVisible());
    }
}

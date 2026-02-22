package org.enginecraft.swing.objects;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.awt.*;

@AllArgsConstructor
@RequiredArgsConstructor
public class TopBar {
    private final String appName;
    private Color background = Color.BLACK;
    private Color foreground = Color.WHITE;

}

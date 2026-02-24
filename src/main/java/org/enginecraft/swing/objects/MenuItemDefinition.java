package org.enginecraft.swing.objects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.enginecraft.swing.menu.MenuAction;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class MenuItemDefinition {
    private final String displayText;
    private final List<MenuItemDefinition> subDefinitions;
    private final List<MenuAction> actions;
}

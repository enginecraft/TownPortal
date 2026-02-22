package org.enginecraft.swing.objects;

import java.util.List;

public class TownPortalNavigation {
    public static final List<MenuItemDefinition> DEFINITIONS = List.of(
            new MenuItemDefinition(
                    "File",
                    List.of(
                            new MenuItemDefinition(
                                    "Save",
                                    null,
                                    null
                            ),
                            new MenuItemDefinition(
                                    "Import",
                                    null,
                                    null
                            ),
                            new MenuItemDefinition(
                                    "Exit",
                                    null,
                                    List.of(
                                            () -> {
                                                System.exit(0);
                                            }
                                    )
                            )
                    ),
                    null
            ),
            new MenuItemDefinition(
                    "Window",
                    List.of(
                            new MenuItemDefinition(
                                    "Preferences",
                                    List.of(
                                            new MenuItemDefinition(
                                                    "Change Style",
                                                    List.of(
                                                            new MenuItemDefinition(
                                                                    "Dark Mode",
                                                                    null,
                                                                    null
                                                            ),
                                                            new MenuItemDefinition(
                                                                    "Light Mode",
                                                                    null,
                                                                    null
                                                            )
                                                    ),
                                                    null
                                            ),
                                            new MenuItemDefinition(
                                                    "Import Style",
                                                    null,
                                                    null
                                            )
                                    ),
                                    null
                            )
                    ),
                    null
            )
    );
}

package org.enginecraft.swing.objects;

import java.util.List;

public class TownPortalNavigation {
    public static final List<MenuItemDefinition> TOP_BAR_DEFINITIONS = List.of(
            new MenuItemDefinition(
                    "File",
                    List.of(
                            new MenuItemDefinition(
                                    "Collection",
                                    List.of(
                                            new MenuItemDefinition(
                                                    "New",
                                                    null,
                                                    null
                                            ),
                                            new MenuItemDefinition(
                                                    "Open",
                                                    null,
                                                    null
                                            ),
                                            new MenuItemDefinition(
                                                    "Recent",
                                                    null,
                                                    null
                                            )
                                    ),
                                    null
                            ),
                            new MenuItemDefinition(
                                    "Save",
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
                                                    "Style",
                                                    List.of(
                                                            new MenuItemDefinition(
                                                                    "Custom Mode",
                                                                    null,
                                                                    null
                                                            ),
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
                                            )
                                    ),
                                    null
                            )
                    ),
                    null
            )
    );

    public static final List<MenuItemDefinition> SIDE_BAR_DEFINITIONS = List.of(
            new MenuItemDefinition(
                    "＋",
                    List.of(
                            new MenuItemDefinition(
                                    "New Project",
                                    null,
                                    null
                            ),
                            new MenuItemDefinition(
                                    "Open Project",
                                    null,
                                    null
                            )
                    ),
                    null
            )
    );
}

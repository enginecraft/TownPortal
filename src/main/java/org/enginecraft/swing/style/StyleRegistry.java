package org.enginecraft.swing.style;

import org.enginecraft.swing.prompts.MessagePrompt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StyleRegistry {
    private static volatile String ACTIVE_STYLE = null;
    private static final Map<String, List<StyleDefinition>> STYLES = new HashMap<>();
    public synchronized static void register(String style, List<StyleDefinition> definitions) {
        if (STYLES.containsKey(style)) {
            String message = "Style '" + style + "' already exists!";
            MessagePrompt.send("Style Registry", message);
            throw new RuntimeException(message);
        }

        if (definitions == null || definitions.isEmpty()) {
            String message = "Style definitions given to register '" + style + "' style were empty!";
            MessagePrompt.send("Style Registry", message);
            throw new RuntimeException(message);
        }

        STYLES.put(style, definitions);
    }

    public synchronized static void applyStyle(String style) {
        if (ACTIVE_STYLE != null && ACTIVE_STYLE.equals(style)) return;
        List<StyleDefinition> definitions = STYLES.get(style);
        if (definitions == null) {
            MessagePrompt.send("Style Registry", "'" + style + "' is an unknown style!");
            return;
        }

        for (StyleDefinition definition : definitions) {
            if (definition.getComponents() == null) continue;

            for (StyleComponent component : definition.getComponents()) {
                component.applyStyle(definition);
            }
        }

        ACTIVE_STYLE = style;
    }
}

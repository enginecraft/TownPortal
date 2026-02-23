package org.enginecraft.swing.prompts;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.swing.*;

@Getter
@Setter
public class MessagePrompt {
    private static MessagePrompt messagePrompt = null;

    public static synchronized void send(String title, String message) {
        if (messagePrompt == null) return;
        messagePrompt.setTitle(title);
        messagePrompt.setMessage(message);
        messagePrompt.frame.repaint();
        messagePrompt.frame.setVisible(true);
    }

    public static synchronized void init(@NonNull String appName) {
        if (messagePrompt != null) return;
        messagePrompt = new MessagePrompt(appName);
    }

    private final JFrame frame;
    private final String appName;
    private String title;
    private String message;

    private MessagePrompt(@NonNull String appName) {
        this.frame = new JFrame(appName);
        this.appName = appName;
    }
}

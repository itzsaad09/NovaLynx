package novalynx;

import javax.swing.*;

public class NovaLynx {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Could not set system look and feel.");
            }

            WelcomeScreen welcome = new WelcomeScreen();
            welcome.launch();
        });
    }
}

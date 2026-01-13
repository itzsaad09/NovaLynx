package novalynx;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class NovaTheme {
    public static final Color DEEP_NAVY = new Color(15, 20, 30);
    public static final Color NOVA_PURPLE = new Color(111, 66, 193);
    public static final Color LYNX_BLUE = new Color(0, 150, 255);
    public static final Color STAR_GOLD = new Color(255, 180, 0);
    public static final Color CYBER_GREEN = new Color(0, 255, 150);
    public static final Color DARK_CARD = new Color(25, 30, 45);
    public static final Color ERROR_RED = new Color(255, 80, 80);

    public static final Font HEADER_FONT = new Font("Arial Rounded MT Bold", Font.BOLD, 36);
    public static final Font SUBHEADER_FONT = new Font("Arial Rounded MT Bold", Font.BOLD, 18);
    public static final Font TERMINAL_FONT = new Font("Monospaced", Font.PLAIN, 14);

    public static void applyFrameSettings(JFrame frame) {
        // Set Icon
        try {
            URL iconUrl = NovaTheme.class.getResource("/assets/logo.png");
            if (iconUrl != null) {
                frame.setIconImage(new ImageIcon(iconUrl).getImage());
            } else {
                frame.setIconImage(new ImageIcon("d:\\Programs\\OS\\NovaLynx\\src\\assets\\logo.png").getImage());
            }
        } catch (Exception e) {
            System.err.println("Could not load logo icon.");
        }
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
}

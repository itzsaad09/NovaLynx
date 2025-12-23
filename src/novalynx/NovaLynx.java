/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package novalynx;

/**
 *
 * @author DELL
 */
import javax.swing.SwingUtilities;

public class NovaLynx {
    public static void main(String[] args) {
        // Use invokeLater to ensure the GUI is created on the Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // Set the System Look and Feel for a native window feel
                    javax.swing.UIManager.setLookAndFeel(
                        javax.swing.UIManager.getSystemLookAndFeelClassName()
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Initialize and launch the Welcome Screen
                WelcomeScreen welcome = new WelcomeScreen();
                welcome.launch();
            }
        });
    }
}

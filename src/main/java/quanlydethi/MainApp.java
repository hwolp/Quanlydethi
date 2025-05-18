package quanlydethi;

import quanlydethi.ui.*;
import javax.swing.*;
import java.awt.FlowLayout;
import java.awt.Font;

public class MainApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
             } catch (Exception e) {
                e.printStackTrace();
            }
            HomeIU home = new HomeIU();
            home.setVisible(true);
        });
    }
}
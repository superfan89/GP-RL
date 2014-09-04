package com.hccl.nlip;

import javax.swing.*;

/**
 * Created by Superfan on 2014/9/4.
 */
public class GUIStandalone extends JFrame {
    public GUIStandalone() {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
            System.err.println("Cannot set look and feel.");
        }
        add(new GUIMainPanel());
        setTitle("GP-SARSA DEMO - Simple Maze");
        setSize(850, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                GUIStandalone gui = new GUIStandalone();
                gui.setVisible(true);
            }
        });
    }
}

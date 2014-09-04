package com.hccl.nlip;

import javax.swing.*;
import java.awt.*;


/**
 * Created by Superfan on 2014/9/4.
 */
public class GUIApplet extends JApplet {
    @Override
    public void init() {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
            System.err.println("Cannot set look and feel.");
        }
        Container content = getContentPane();
        content.add(new GUIMainPanel());
    }
}

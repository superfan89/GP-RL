/*
 * Created by JFormDesigner on Tue Sep 02 10:16:01 CST 2014
 */

package com.hccl.nlip;

import java.awt.event.*;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;

/**
 * @author Super Fan
 */
public class GUIMainPanel extends JPanel {
    public GUIMainPanel() {
        initComponents();
    }

    private class AnimationTimer extends Timer implements ActionListener {
        private int delay;
        public AnimationTimer(int delay) {
            super(delay, null);
            this.delay = delay;
            addActionListener(this);
        }

        public void actionPerformed(ActionEvent evt) {
            mazeEnv.doStep(true);
            syncStatusToGUI();
        }
    }

    private void animateBtnActionPerformed(ActionEvent e) {
        animationOn = !animationOn;
        if (animationOn) {
            animateBtn.setText(" Pause ");
            if (animationTimer == null)
                animationTimer = new AnimationTimer(10);
            animationTimer.start();
        }
        else {
            animateBtn.setText("Animate");
            animationTimer.stop();
        }
        setControlButtonsInAnimation();
    }

    private void stepBtnActionPerformed(ActionEvent e) {
        syncParamsToControler();
        mazeEnv.doStep(true);
        syncStatusToGUI();
    }

    private void episodeBtnActionPerformed(ActionEvent e) {
        syncParamsToControler();
        setEnableAllControl(false);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                mazeEnv.doEpisodes(1, true);
                return null;
            }

            @Override
            protected void done() {
                setEnableAllControl(true);
                updateInfo();
            }
        }.execute();
    }

    private void resetBtnActionPerformed(ActionEvent e) {
        mazeEnv.doReset();
        controller = (GPSARSASparseController)mazeEnv.controller();
        syncParamsToControler();
        syncStatusToGUI();
    }

    private void performEpisodesBtnActionPerformed(ActionEvent e) {
        syncParamsToControler();
        if (episodesOn) {
            episodesOn = false;
            performEpisodesBtn.setEnabled(false);
        }
        else {
            try {
                int nEpisodes = Integer.valueOf(numberEpisodesField.getText());
                setControlButtonsInEpisodes(false);
                episodesOn = true;
                if (nEpisodes >= 100) {
                    int batch = nEpisodes / 10;
                    new EpisodesSwingBatchWorker(nEpisodes, batch, true).execute();
                }
                else
                    new EpisodesSwingBatchWorker(nEpisodes, 1, true).execute();
            }
            catch(NumberFormatException except) {
                numberEpisodesField.setText("100");
            }
        }
    }

    private void displayActionsCheckBoxActionPerformed(ActionEvent e) {
        mazeEnv.setDisplayAction(displayActionsCheckBox.isSelected());
    }

    private void obstaclesComboBoxActionPerformed(ActionEvent e) {
        int obstacleInd = obstaclesComboBox.getSelectedIndex();
        mazeEnv.setObstacle(obstacles[obstacleInd]);
    }

    private void goalsComboBoxActionPerformed(ActionEvent e) {
        int goalInd = goalsComboBox.getSelectedIndex();
        mazeEnv.setGoal(goals[goalInd]);
    }

    private void fixedEpsilonCheckBoxActionPerformed(ActionEvent e) {
        controller.setUseFixedEpsilon(fixedEpsilonCheckBox.isSelected());
    }

    private void discountDecActionPerformed(ActionEvent e) {
        try {
            discountField.setText(String.format("%.2f",
                    Double.valueOf(discountField.getText()) - 0.1));
        }
        catch(Exception except) {
            discountField.setText(String.format("%.2f", controller.gamma()));
        }
    }

    private void discountIncActionPerformed(ActionEvent e) {
        try {
            discountField.setText(String.format("%.2f",
                    Double.valueOf(discountField.getText()) + 0.1));
        }
        catch(Exception except) {
            discountField.setText(String.format("%.2f", controller.gamma()));
        }
    }

    private void rewardCollisionDecActionPerformed(ActionEvent e) {
        try {
            rewardCollisionField.setText(String.format("%.2f",
                    Double.valueOf(rewardCollisionField.getText()) - 1.0));
        }
        catch(Exception except) {
            rewardCollisionField.setText(String.format("%.2f", MazeEnvironment.rewardOfCollision()));
        }
    }

    private void rewardCollisionIncActionPerformed(ActionEvent e) {
        try {
            rewardCollisionField.setText(String.format("%.2f",
                    Double.valueOf(rewardCollisionField.getText()) + 1.0));
        }
        catch(Exception except) {
            rewardCollisionField.setText(String.format("%.2f", MazeEnvironment.rewardOfCollision()));
        }
    }

    private void rewardGoalDecActionPerformed(ActionEvent e) {
        try {
            rewardGoalField.setText(String.format("%.2f",
                    Double.valueOf(rewardGoalField.getText()) - 1.0));
        }
        catch(Exception except) {
            rewardGoalField.setText(String.format("%.2f", MazeEnvironment.rewardOfReachingGoal()));
        }
    }

    private void rewardGoalIncActionPerformed(ActionEvent e) {
        try {
            rewardGoalField.setText(String.format("%.2f",
                    Double.valueOf(rewardGoalField.getText()) + 1.0));
        }
        catch(Exception except) {
            rewardGoalField.setText(String.format("%.2f", MazeEnvironment.rewardOfReachingGoal()));
        }
    }

    private void rewardOneStepDecActionPerformed(ActionEvent e) {
        try {
            rewardOneStepField.setText(String.format("%.2f",
                    Double.valueOf(rewardOneStepField.getText()) - 1.0));
        }
        catch(Exception except) {
            rewardOneStepField.setText(String.format("%.2f",
                    MazeEnvironment.rewardOfStepOutsideGoalRegion()));
        }
    }

    private void rewardOneStepIncActionPerformed(ActionEvent e) {
        try {
            rewardOneStepField.setText(String.format("%.2f",
                    Double.valueOf(rewardOneStepField.getText()) + 1.0));
        }
        catch(Exception except) {
            rewardOneStepField.setText(String.format("%.2f",
                    MazeEnvironment.rewardOfStepOutsideGoalRegion()));
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - Super Fan
        bestActionChartPanel = new JPanel();
        consolePanel = new JPanel();
        RLCaptionPanel = new JPanel();
        RLCaption = new JLabel();
        discountLabel = new JLabel();
        discountPanel = new JPanel();
        discountDec = new JButton();
        discountField = new JTextField();
        discountInc = new JButton();
        rewardCollisionLabel = new JLabel();
        rewardCollisionPanel = new JPanel();
        rewardCollisionDec = new JButton();
        rewardCollisionField = new JTextField();
        rewardCollisionInc = new JButton();
        rewadGoalLabel = new JLabel();
        rewardGoalPanel = new JPanel();
        rewardGoalDec = new JButton();
        rewardGoalField = new JTextField();
        rewardGoalInc = new JButton();
        rewardOneStepLabel = new JLabel();
        rewardOneStepPanel = new JPanel();
        rewardOneStepDec = new JButton();
        rewardOneStepField = new JTextField();
        rewardOneStepInc = new JButton();
        fixedEpsilonCheckBox = new JCheckBox();
        fixedEpsilonPanel = new JPanel();
        fixedEpsilonField = new JTextField();
        sepPanel1 = new JPanel();
        separator1 = new JSeparator();
        envCaptionPanel = new JPanel();
        envLabel = new JLabel();
        obstaclesLabel = new JLabel();
        obstaclesPanel = new JPanel();
        obstaclesComboBox = new JComboBox();
        goalsLabel = new JLabel();
        goalsPanel = new JPanel();
        goalsComboBox = new JComboBox();
        sepPanel2 = new JPanel();
        separator2 = new JSeparator();
        controlCaptionPanel = new JPanel();
        controlLabel = new JLabel();
        displayActionsPanel = new JPanel();
        displayActionsCheckBox = new JCheckBox();
        controlActionsPanel = new JPanel();
        stepBtn = new JButton();
        episodeBtn = new JButton();
        animateBtn = new JButton();
        resetBtn = new JButton();
        controlEpisodesPanel = new JPanel();
        performEpisodesBtn = new JButton();
        numberEpisodesField = new JTextField();
        numberEpisodesLabel = new JLabel();
        episodesProgressBar = new JProgressBar();
        sepPanel3 = new JPanel();
        separator3 = new JSeparator();
        totalStepsLabel = new JLabel();
        totalStepsField = new JLabel();
        totalEpisodesLabel = new JLabel();
        totalEpisodesFields = new JLabel();
        totalRewardLabel = new JLabel();
        totalRewardField = new JLabel();
        dicSizeField = new JLabel();
        dicSizeLabel = new JLabel();

        //======== this ========
        setPreferredSize(new Dimension(700, 500));
        setMinimumSize(new Dimension(780, 580));

        setLayout(new GridBagLayout());
        ((GridBagLayout)getLayout()).columnWidths = new int[] {396, 239, 0};
        ((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0};
        ((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 0.0, 1.0E-4};
        ((GridBagLayout)getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

        //======== bestActionChartPanel ========
        {
            bestActionChartPanel.setPreferredSize(new Dimension(250, 250));
            bestActionChartPanel.setLayout(new BorderLayout());
        }
        add(bestActionChartPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 5), 0, 0));

        //======== consolePanel ========
        {
            consolePanel.setLayout(new GridBagLayout());
            ((GridBagLayout)consolePanel.getLayout()).columnWidths = new int[] {108, 129, 0};
            ((GridBagLayout)consolePanel.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            ((GridBagLayout)consolePanel.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
            ((GridBagLayout)consolePanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

            //======== RLCaptionPanel ========
            {
                RLCaptionPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 1, 1));

                //---- RLCaption ----
                RLCaption.setText("RL Parameters");
                RLCaption.setFont(new Font("\u5b8b\u4f53", Font.PLAIN, 16));
                RLCaptionPanel.add(RLCaption);
            }
            consolePanel.add(RLCaptionPanel, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //---- discountLabel ----
            discountLabel.setText("Discount:");
            consolePanel.add(discountLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 5, 5), 0, 0));

            //======== discountPanel ========
            {
                discountPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));

                //---- discountDec ----
                discountDec.setText("-");
                discountDec.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        discountDecActionPerformed(e);
                    }
                });
                discountPanel.add(discountDec);

                //---- discountField ----
                discountField.setText("000.00");
                discountField.setColumns(6);
                discountPanel.add(discountField);

                //---- discountInc ----
                discountInc.setText("+");
                discountInc.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        discountIncActionPerformed(e);
                    }
                });
                discountPanel.add(discountInc);
            }
            consolePanel.add(discountPanel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //---- rewardCollisionLabel ----
            rewardCollisionLabel.setText("Reward collision:");
            consolePanel.add(rewardCollisionLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 5, 5), 0, 0));

            //======== rewardCollisionPanel ========
            {
                rewardCollisionPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));

                //---- rewardCollisionDec ----
                rewardCollisionDec.setText("-");
                rewardCollisionDec.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        rewardCollisionDecActionPerformed(e);
                    }
                });
                rewardCollisionPanel.add(rewardCollisionDec);

                //---- rewardCollisionField ----
                rewardCollisionField.setText("000.00");
                rewardCollisionField.setColumns(6);
                rewardCollisionPanel.add(rewardCollisionField);

                //---- rewardCollisionInc ----
                rewardCollisionInc.setText("+");
                rewardCollisionInc.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        rewardCollisionIncActionPerformed(e);
                    }
                });
                rewardCollisionPanel.add(rewardCollisionInc);
            }
            consolePanel.add(rewardCollisionPanel, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //---- rewadGoalLabel ----
            rewadGoalLabel.setText("Reward goal:");
            consolePanel.add(rewadGoalLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 5, 5), 0, 0));

            //======== rewardGoalPanel ========
            {
                rewardGoalPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));

                //---- rewardGoalDec ----
                rewardGoalDec.setText("-");
                rewardGoalDec.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        rewardGoalDecActionPerformed(e);
                    }
                });
                rewardGoalPanel.add(rewardGoalDec);

                //---- rewardGoalField ----
                rewardGoalField.setText("000.00");
                rewardGoalField.setColumns(6);
                rewardGoalPanel.add(rewardGoalField);

                //---- rewardGoalInc ----
                rewardGoalInc.setText("+");
                rewardGoalInc.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        rewardGoalIncActionPerformed(e);
                    }
                });
                rewardGoalPanel.add(rewardGoalInc);
            }
            consolePanel.add(rewardGoalPanel, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //---- rewardOneStepLabel ----
            rewardOneStepLabel.setText("Reward one step:");
            consolePanel.add(rewardOneStepLabel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 5, 5), 0, 0));

            //======== rewardOneStepPanel ========
            {
                rewardOneStepPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));

                //---- rewardOneStepDec ----
                rewardOneStepDec.setText("-");
                rewardOneStepDec.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        rewardOneStepDecActionPerformed(e);
                    }
                });
                rewardOneStepPanel.add(rewardOneStepDec);

                //---- rewardOneStepField ----
                rewardOneStepField.setText("000.00");
                rewardOneStepField.setColumns(6);
                rewardOneStepPanel.add(rewardOneStepField);

                //---- rewardOneStepInc ----
                rewardOneStepInc.setText("+");
                rewardOneStepInc.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        rewardOneStepIncActionPerformed(e);
                    }
                });
                rewardOneStepPanel.add(rewardOneStepInc);
            }
            consolePanel.add(rewardOneStepPanel, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //---- fixedEpsilonCheckBox ----
            fixedEpsilonCheckBox.setText("Fixed Epsilon:");
            fixedEpsilonCheckBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fixedEpsilonCheckBoxActionPerformed(e);
                }
            });
            consolePanel.add(fixedEpsilonCheckBox, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 5, 5), 0, 0));

            //======== fixedEpsilonPanel ========
            {
                fixedEpsilonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));

                //---- fixedEpsilonField ----
                fixedEpsilonField.setText("00.10");
                fixedEpsilonField.setColumns(6);
                fixedEpsilonPanel.add(fixedEpsilonField);
            }
            consolePanel.add(fixedEpsilonPanel, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 5, 0), 0, 0));

            //======== sepPanel1 ========
            {
                sepPanel1.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

                //---- separator1 ----
                separator1.setPreferredSize(new Dimension(210, 2));
                sepPanel1.add(separator1);
            }
            consolePanel.add(sepPanel1, new GridBagConstraints(0, 6, 2, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //======== envCaptionPanel ========
            {
                envCaptionPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 1, 1));

                //---- envLabel ----
                envLabel.setText("Environment");
                envLabel.setFont(new Font("\u5b8b\u4f53", Font.PLAIN, 16));
                envCaptionPanel.add(envLabel);
            }
            consolePanel.add(envCaptionPanel, new GridBagConstraints(0, 7, 2, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //---- obstaclesLabel ----
            obstaclesLabel.setText("Obstacles:");
            consolePanel.add(obstaclesLabel, new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 5, 5), 0, 0));

            //======== obstaclesPanel ========
            {
                obstaclesPanel.setLayout(new FlowLayout());

                //---- obstaclesComboBox ----
                obstaclesComboBox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        obstaclesComboBoxActionPerformed(e);
                    }
                });
                obstaclesPanel.add(obstaclesComboBox);
            }
            consolePanel.add(obstaclesPanel, new GridBagConstraints(1, 8, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 5, 0), 0, 0));

            //---- goalsLabel ----
            goalsLabel.setText("Goals:");
            consolePanel.add(goalsLabel, new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 5, 5), 0, 0));

            //======== goalsPanel ========
            {
                goalsPanel.setLayout(new FlowLayout());

                //---- goalsComboBox ----
                goalsComboBox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        goalsComboBoxActionPerformed(e);
                    }
                });
                goalsPanel.add(goalsComboBox);
            }
            consolePanel.add(goalsPanel, new GridBagConstraints(1, 9, 1, 1, 0.0, 0.0,
                GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 5, 0), 0, 0));

            //======== sepPanel2 ========
            {
                sepPanel2.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

                //---- separator2 ----
                separator2.setPreferredSize(new Dimension(210, 2));
                sepPanel2.add(separator2);
            }
            consolePanel.add(sepPanel2, new GridBagConstraints(0, 10, 2, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //======== controlCaptionPanel ========
            {
                controlCaptionPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 1, 1));

                //---- controlLabel ----
                controlLabel.setText("Control");
                controlLabel.setFont(new Font("\u5b8b\u4f53", Font.PLAIN, 16));
                controlCaptionPanel.add(controlLabel);
            }
            consolePanel.add(controlCaptionPanel, new GridBagConstraints(0, 11, 2, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //======== displayActionsPanel ========
            {
                displayActionsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

                //---- displayActionsCheckBox ----
                displayActionsCheckBox.setText("Display action vectors");
                displayActionsCheckBox.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        displayActionsCheckBoxActionPerformed(e);
                    }
                });
                displayActionsPanel.add(displayActionsCheckBox);
            }
            consolePanel.add(displayActionsPanel, new GridBagConstraints(0, 12, 2, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //======== controlActionsPanel ========
            {
                controlActionsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 0));

                //---- stepBtn ----
                stepBtn.setText("1 Step");
                stepBtn.setMargin(new Insets(2, 2, 2, 2));
                stepBtn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        stepBtnActionPerformed(e);
                    }
                });
                controlActionsPanel.add(stepBtn);

                //---- episodeBtn ----
                episodeBtn.setText("1 Episode");
                episodeBtn.setMargin(new Insets(2, 4, 2, 4));
                episodeBtn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        episodeBtnActionPerformed(e);
                    }
                });
                controlActionsPanel.add(episodeBtn);

                //---- animateBtn ----
                animateBtn.setText("Animate");
                animateBtn.setMargin(new Insets(2, 2, 2, 2));
                animateBtn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        animateBtnActionPerformed(e);
                    }
                });
                controlActionsPanel.add(animateBtn);

                //---- resetBtn ----
                resetBtn.setText("Reset");
                resetBtn.setMargin(new Insets(2, 2, 2, 2));
                resetBtn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        resetBtnActionPerformed(e);
                    }
                });
                controlActionsPanel.add(resetBtn);
            }
            consolePanel.add(controlActionsPanel, new GridBagConstraints(0, 13, 2, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //======== controlEpisodesPanel ========
            {
                controlEpisodesPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 0));

                //---- performEpisodesBtn ----
                performEpisodesBtn.setText("Perform");
                performEpisodesBtn.setMargin(new Insets(2, 2, 2, 2));
                performEpisodesBtn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        performEpisodesBtnActionPerformed(e);
                    }
                });
                controlEpisodesPanel.add(performEpisodesBtn);

                //---- numberEpisodesField ----
                numberEpisodesField.setText("100");
                numberEpisodesField.setColumns(5);
                controlEpisodesPanel.add(numberEpisodesField);

                //---- numberEpisodesLabel ----
                numberEpisodesLabel.setText("episodes");
                controlEpisodesPanel.add(numberEpisodesLabel);

                //---- episodesProgressBar ----
                episodesProgressBar.setMinimumSize(new Dimension(5, 10));
                episodesProgressBar.setPreferredSize(new Dimension(80, 20));
                controlEpisodesPanel.add(episodesProgressBar);
            }
            consolePanel.add(controlEpisodesPanel, new GridBagConstraints(0, 14, 2, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //======== sepPanel3 ========
            {
                sepPanel3.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

                //---- separator3 ----
                separator3.setPreferredSize(new Dimension(210, 2));
                sepPanel3.add(separator3);
            }
            consolePanel.add(sepPanel3, new GridBagConstraints(0, 15, 2, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //---- totalStepsLabel ----
            totalStepsLabel.setText("Total steps:");
            consolePanel.add(totalStepsLabel, new GridBagConstraints(0, 16, 1, 1, 0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 5, 5), 0, 0));

            //---- totalStepsField ----
            totalStepsField.setText("0000000");
            consolePanel.add(totalStepsField, new GridBagConstraints(1, 16, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //---- totalEpisodesLabel ----
            totalEpisodesLabel.setText("Total episodes:");
            consolePanel.add(totalEpisodesLabel, new GridBagConstraints(0, 17, 1, 1, 0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 5, 5), 0, 0));

            //---- totalEpisodesFields ----
            totalEpisodesFields.setText("0000000");
            consolePanel.add(totalEpisodesFields, new GridBagConstraints(1, 17, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //---- totalRewardLabel ----
            totalRewardLabel.setText("Total reward:");
            consolePanel.add(totalRewardLabel, new GridBagConstraints(0, 18, 1, 1, 0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 5, 5), 0, 0));

            //---- totalRewardField ----
            totalRewardField.setText("00000000.00000000");
            consolePanel.add(totalRewardField, new GridBagConstraints(1, 18, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //---- dicSizeField ----
            dicSizeField.setText("00000000.00000000");
            consolePanel.add(dicSizeField, new GridBagConstraints(1, 19, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //---- dicSizeLabel ----
            dicSizeLabel.setText("Dictionary size:");
            consolePanel.add(dicSizeLabel, new GridBagConstraints(0, 19, 1, 1, 0.0, 0.0,
                GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
                new Insets(0, 0, 5, 5), 0, 0));
        }
        add(consolePanel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
            new Insets(0, 0, 0, 0), 0, 0));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
        mazeEnv = new MazeEnvironment();
        controller = (GPSARSASparseController)mazeEnv.controller();
        JFreeChart jFreeChart = mazeEnv.genGraph();
        //Put the jFreeChart in a chartPanel
        ChartPanel chartPanel = new ChartPanel(jFreeChart);
        chartPanel.setPreferredSize(new Dimension(250, 250));
        syncParamsToGUI();
        syncStatusToGUI();
        bestActionChartPanel.add(chartPanel, BorderLayout.CENTER);
    }

    private void syncParamsToControler() {
        try {
            controller.setGamma(Double.valueOf(discountField.getText()));
        }
        catch(Exception e) {
            discountField.setText(String.format("%.2f", controller.gamma()));
        }
        try {
            mazeEnv.setRewardCollision(Double.valueOf(rewardCollisionField.getText()));
        }
        catch(Exception e) {
            rewardCollisionField.setText(String.format("%.2f", MazeEnvironment.rewardOfCollision()));
        }
        try {
            mazeEnv.setRewardGoal(Double.valueOf(rewardGoalField.getText()));
        }
        catch(Exception e) {
            rewardGoalField.setText(String.format("%.2f", MazeEnvironment.rewardOfReachingGoal()));
        }
        try {
            mazeEnv.setRewardStep(Double.valueOf(rewardOneStepField.getText()));
        }
        catch(Exception e) {
            rewardOneStepField.setText(String.format("%.2f", MazeEnvironment.rewardOfStepOutsideGoalRegion()));
        }
        try {
            controller.setFixedEpsilon(Double.valueOf(fixedEpsilonField.getText()));
        }
        catch(Exception e) {
            fixedEpsilonField.setText(String.format("%.2f", controller.fixedEpsilon()));
        }
    }

    private void syncParamsToGUI() {
        // RL Parameters
        discountField.setText(String.format("%.2f", controller.gamma()));
        rewardCollisionField.setText(String.format("%.2f", MazeEnvironment.rewardOfCollision()));
        rewardGoalField.setText(String.format("%.2f", MazeEnvironment.rewardOfReachingGoal()));
        rewardOneStepField.setText(String.format("%.2f", MazeEnvironment.rewardOfStepOutsideGoalRegion()));
        fixedEpsilonField.setText(String.format("%.2f", controller.fixedEpsilon()));
        fixedEpsilonCheckBox.setSelected(controller.useFixedEpsilon());
        // Environment
        obstacles = GeoSettings.getObstaclesArray();
        goals = GeoSettings.getGoalsArray();
        for (int i = 0; i < obstacles.length; i++) {
            Shapes obstacle = obstacles[i];
            if (obstacle.name()!=null)
                obstaclesComboBox.addItem(obstacle.name());
            else
                obstaclesComboBox.addItem(String.format("Obstacle %d", i));
        }
        for (int i = 0; i < goals.length; i++) {
            Shapes goal = goals[i];
            if (goal.name()!=null)
                goalsComboBox.addItem(goal.name());
            else
                goalsComboBox.addItem(String.format("Goal %d", i));
        }
        // Control
        displayActionsCheckBox.setSelected(mazeEnv.displayAction());
        // Info
        updateInfo();
    }

    private void syncStatusToGUI() {
        totalStepsField.setText(String.format("%d", mazeEnv.totalSteps()));
        totalEpisodesFields.setText(String.format("%d", mazeEnv.successfulEpisodes()));
        totalRewardField.setText(String.format("%.2f", mazeEnv.totalReward()));
        dicSizeField.setText(String.format("%d", controller.getDictSize()));
    }

    private void updateInfo() {
        totalEpisodesFields.setText(String.valueOf(mazeEnv.successfulEpisodes()));
        totalRewardField.setText(String.format("%.2f", mazeEnv.totalReward()));
        totalStepsField.setText(String.valueOf(mazeEnv.totalSteps()));
    }

    private void setControlButtonsInAnimation() {
        fixedEpsilonCheckBox.setEnabled(!animationOn);
        stepBtn.setEnabled(!animationOn);
        episodeBtn.setEnabled(!animationOn);
        performEpisodesBtn.setEnabled(!animationOn);
        resetBtn.setEnabled(!animationOn);
        goalsComboBox.setEnabled(!animationOn);
        obstaclesComboBox.setEnabled(!animationOn);
    }

    private void setControlButtonsInEpisodes(Boolean enable) {
        fixedEpsilonCheckBox.setEnabled(enable);
        displayActionsCheckBox.setEnabled(enable);
        stepBtn.setEnabled(enable);
        episodeBtn.setEnabled(enable);
        resetBtn.setEnabled(enable);
        goalsComboBox.setEnabled(enable);
        obstaclesComboBox.setEnabled(enable);
        animateBtn.setEnabled(enable);
        if (!enable) performEpisodesBtn.setText("  Stop ");
        else performEpisodesBtn.setText("Perform");
    }

    private void setEnableAllControl(Boolean enable) {
        fixedEpsilonCheckBox.setEnabled(enable);
        displayActionsCheckBox.setEnabled(enable);
        stepBtn.setEnabled(enable);
        episodeBtn.setEnabled(enable);
        performEpisodesBtn.setEnabled(enable);
        resetBtn.setEnabled(enable);
        goalsComboBox.setEnabled(enable);
        obstaclesComboBox.setEnabled(enable);
        animateBtn.setEnabled(enable);
    }

    private class EpisodesSwingBatchWorker extends SwingWorker<Void, Void> {
        private int totalEpisodes;
        private int remainingEpisodes;
        private int batch;
        private int intervalUpdateProgress;
        private int processedEpisodes;
        private Boolean doEnable;
        public EpisodesSwingBatchWorker(int totalEpisodes,
                                        int batch,
                                        Boolean doEnable) {
            this(totalEpisodes, totalEpisodes, batch, doEnable);
        }

        public EpisodesSwingBatchWorker(final int totalEpisodes,
                                        final int remainingEpisodes,
                                        final int batch,
                                        final Boolean doEnable) {
            this.remainingEpisodes = remainingEpisodes;
            this.processedEpisodes = totalEpisodes - remainingEpisodes;
            this.batch = batch;
            this.doEnable = doEnable;
            if (!episodesOn) {
                clean();
                return;
            }
            if (totalEpisodes < 100)
                intervalUpdateProgress = 1;
            else
                intervalUpdateProgress = totalEpisodes / 100;
            if (remainingEpisodes > batch)
                addPropertyChangeListener(new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent pcEvt) {
                        if (pcEvt.getNewValue() == SwingWorker.StateValue.DONE) {
                            mazeEnv.updatePlot();
                            syncStatusToGUI();
                            new EpisodesSwingBatchWorker(totalEpisodes,
                                    remainingEpisodes - batch,
                                    batch, doEnable).execute();
                        }
                    }
                });
            else
            // Last batch
                addPropertyChangeListener(new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent pcEvt) {
                    if (pcEvt.getNewValue() == SwingWorker.StateValue.DONE)
                        clean();
                    }
                });
            addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("progress".equals(evt.getPropertyName())) {
                        episodesProgressBar.setValue((Integer)evt.getNewValue());
                    }
                }
            });
        }
        private void clean() {
            mazeEnv.updatePlot();
            syncStatusToGUI();
            if (doEnable) {
                setControlButtonsInEpisodes(true);
            }
            performEpisodesBtn.setEnabled(true);
            episodesProgressBar.setValue(0);
            episodesOn = false;
        }
        @Override
        public Void doInBackground() throws Exception {
            int episodes;
            if (remainingEpisodes > batch)
                episodes = batch;
            else
                episodes = remainingEpisodes;
            for (int i=0; i < episodes; i++) {
                if (!episodesOn) {
                    clean();
                    return null;
                }
                mazeEnv.doEpisodes(1, false);
                if ((processedEpisodes + i + 1) % intervalUpdateProgress == 0 ||
                        intervalUpdateProgress == 1) {
                    setProgress((processedEpisodes + i + 1) / intervalUpdateProgress);
                }

            }
            return null;
        }
    }

    private MazeEnvironment mazeEnv;
    private GPSARSASparseController controller;
    private Boolean animationOn = false;
    private Boolean episodesOn = false;
    private AnimationTimer animationTimer = null;
    private Shapes[] obstacles;
    private Shapes[] goals;
    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Super Fan
    private JPanel bestActionChartPanel;
    private JPanel consolePanel;
    private JPanel RLCaptionPanel;
    private JLabel RLCaption;
    private JLabel discountLabel;
    private JPanel discountPanel;
    private JButton discountDec;
    private JTextField discountField;
    private JButton discountInc;
    private JLabel rewardCollisionLabel;
    private JPanel rewardCollisionPanel;
    private JButton rewardCollisionDec;
    private JTextField rewardCollisionField;
    private JButton rewardCollisionInc;
    private JLabel rewadGoalLabel;
    private JPanel rewardGoalPanel;
    private JButton rewardGoalDec;
    private JTextField rewardGoalField;
    private JButton rewardGoalInc;
    private JLabel rewardOneStepLabel;
    private JPanel rewardOneStepPanel;
    private JButton rewardOneStepDec;
    private JTextField rewardOneStepField;
    private JButton rewardOneStepInc;
    private JCheckBox fixedEpsilonCheckBox;
    private JPanel fixedEpsilonPanel;
    private JTextField fixedEpsilonField;
    private JPanel sepPanel1;
    private JSeparator separator1;
    private JPanel envCaptionPanel;
    private JLabel envLabel;
    private JLabel obstaclesLabel;
    private JPanel obstaclesPanel;
    private JComboBox obstaclesComboBox;
    private JLabel goalsLabel;
    private JPanel goalsPanel;
    private JComboBox goalsComboBox;
    private JPanel sepPanel2;
    private JSeparator separator2;
    private JPanel controlCaptionPanel;
    private JLabel controlLabel;
    private JPanel displayActionsPanel;
    private JCheckBox displayActionsCheckBox;
    private JPanel controlActionsPanel;
    private JButton stepBtn;
    private JButton episodeBtn;
    private JButton animateBtn;
    private JButton resetBtn;
    private JPanel controlEpisodesPanel;
    private JButton performEpisodesBtn;
    private JTextField numberEpisodesField;
    private JLabel numberEpisodesLabel;
    private JProgressBar episodesProgressBar;
    private JPanel sepPanel3;
    private JSeparator separator3;
    private JLabel totalStepsLabel;
    private JLabel totalStepsField;
    private JLabel totalEpisodesLabel;
    private JLabel totalEpisodesFields;
    private JLabel totalRewardLabel;
    private JLabel totalRewardField;
    private JLabel dicSizeField;
    private JLabel dicSizeLabel;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}

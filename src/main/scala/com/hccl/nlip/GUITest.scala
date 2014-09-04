package com.hccl.nlip

import java.awt.{Container, Dimension}
import javax.swing.{UIManager, JApplet}
import org.jfree.chart.{ChartPanel, JFreeChart}

/**
 * Created by Superfan on 2014/8/27.
 */
class GUITest extends JApplet {
    override def init(): Unit = {
//        val env = new MazeEnvironment
//        val jFreeChart = env.genGraph
//        //Put the jFreeChart in a chartPanel
//        val chartPanel = new ChartPanel(jFreeChart)
//        //        chartPanel.setPreferredSize(new Dimension(900, 600))
//        chartPanel.setPopupMenu(null)
//        //add the chartPanel to the container (getContentPane is inherited from JApplet which AppletGraph extends).
        UIManager.setLookAndFeel(
            UIManager.getSystemLookAndFeelClassName)
        val content: Container = getContentPane
        content.add(new GUIMainPanel)
    }
}

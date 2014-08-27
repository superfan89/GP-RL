package com.hccl.nlip

/**
 * Created by Superfan on 2014/8/26.
 */

import Math._
import java.awt.Color
import java.awt.event.{ActionEvent, ActionListener}
import java.awt.geom.Ellipse2D
import javax.swing.Timer
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.plot.{XYPlot, PlotOrientation}
import org.jfree.chart.renderer.xy.{XYDotRenderer, XYBubbleRenderer}
import org.jfree.chart.{ChartFactory, JFreeChart}
import org.jfree.data.xy.{DefaultXYDataset, XYDataset}
import org.jfree.util.ShapeUtilities

case class MazeState(location: Point2D)

case class MazeAction(radiansToMove: Double)

trait MazePolicy {
    def getAction(s: MazeState): MazeAction
}

object RandomMazePolicy extends MazePolicy {
    def getAction(s: MazeState) = MazeAction(PI * 2 * math.random)
}

class MazeEnvironment {
    import GeoSettings._

    var location: Point2D = null
    var totalSteps: Int = 0
    var totalReward: Double = 0.0
    var policy: MazePolicy = RandomMazePolicy
    val mazeBound: Shapes = defaultBound
//    var obstacles: Shapes = defaultObstacle
    var obstacles: Shapes = obstaclesCollection.o1
    var goalRegions: Shapes = defaultGoal
    var stepSize = 0.02
    val rewardOfCollision = 0.0
    val rewardOfStepOutsideGoalRegion = -1.0
    val rewardOfReachingGoal = 10

    def getValidRandomInitialLocation = {
        var initialLoc = Point2D(math.random, math.random)
        while (!mazeBound.contains(initialLoc) ||
         obstacles.contains(initialLoc) ||
         goalRegions.contains(initialLoc)) {
            initialLoc = Point2D(math.random, math.random)
        }
//        print(initialLoc)
        initialLoc
    }

    doReset()

    def doReset(): Unit = {
        location = getValidRandomInitialLocation
        totalSteps = 0
    }

    def doStep(): Double = {
        val action = policy.getAction(getCurrentState)
        val p2 = location.getPointFromThis(action.radiansToMove, stepSize)
        val l = LineSegment(location, p2)
        var reward = 0.0
        //Collision
        if (mazeBound.intersectsWithLineSeg(l) || obstacles.intersectsWithLineSeg(l)) {
            reward += rewardOfCollision + rewardOfStepOutsideGoalRegion
        }
        //A step outside the goal regions
        else if (goalRegions.contains(p2)) {
            reward += rewardOfReachingGoal
            location = getValidRandomInitialLocation
        }
        //Reach the goal get the reward and 
        //got flung elsewhere starting to explore again
        else {
            reward += rewardOfStepOutsideGoalRegion
            location = p2
        }
        totalReward += reward
        totalSteps += 1
        reward
    }

    def setPolicy(p: MazePolicy): Unit = {
        policy = p
    }

    def getTotalSteps: Int = totalSteps

    def getTotalReward: Double = totalReward

    def getCurrentState = MazeState(location)

    var chart: JFreeChart = null

    var plot: XYPlot = null

    def genDataset: XYDataset = {
        val dataset = new DefaultXYDataset()
        dataset.addSeries("Agent", Array(Array(location.X), Array(location.Y)))
        dataset
    }

    def genGraph: JFreeChart = {
        chart = ChartFactory.createScatterPlot(
            "GP-RL DEMO", // chart title
            "X", // x axis label
            "Y", // y axis label
            genDataset, // data
            PlotOrientation.VERTICAL,
            false, // include legend
            false, // tooltips
            false // urls
        )
        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white)
        // get a reference to the plot for further customisation...
        plot = chart.getXYPlot



        plot.setBackgroundPaint(Color.white)
        plot.getDomainAxis.setRange(0.0, 1.0)
        plot.getRangeAxis.setRange(0.0, 1.0)
//        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0))
        plot.setDomainGridlinePaint(Color.lightGray)
        plot.setRangeGridlinePaint(Color.lightGray)
        val renderer = plot.getRenderer
        renderer.setSeriesShape(0, new Ellipse2D.Double(-6, -6, 12, 12))
        renderer.setSeriesPaint(0, Color.red)
        // change the auto tick unit selection to integer units only...
        val rangeAxis = plot.getRangeAxis
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits())
        // OPTIONAL CUSTOMISATION COMPLETED.
        goalRegions.registerXYPlot(plot)
        obstacles.registerXYPlot(plot)
        mazeBound.registerXYPlot(plot)
        //Start the animation
        new DataGenerator(1).start()

        chart
    }

    class DataGenerator(delay: Int) extends Timer(delay, null) with ActionListener {
        addActionListener(this)

        def actionPerformed(event: ActionEvent): Unit = {
            doStep()
            plot.setDataset(genDataset)
        }
    }
}

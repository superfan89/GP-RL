package com.hccl.nlip

/**
 * Created by Superfan on 2014/8/26.
 */

import Math._
import java.awt.Color
import java.awt.event.{ActionEvent, ActionListener}
import java.awt.geom.Ellipse2D
import javax.swing.Timer
import com.typesafe.scalalogging.LazyLogging
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.plot.{XYPlot, PlotOrientation}
import org.jfree.chart.renderer.xy.{VectorRenderer, XYDotRenderer, XYBubbleRenderer}
import org.jfree.chart.{ChartFactory, JFreeChart}
import org.jfree.data.xy.{DefaultXYDataset, XYDataset}
import org.jfree.util.ShapeUtilities

case class MazeState(location: Point2D)

object MazeAction {
    def randomAction = MazeAction(random * 2 * PI)
}

case class MazeAction(radiansToMove: Double)

trait MazePolicy {
    def getAction(s: MazeState): MazeAction
}

object RandomMazePolicy extends MazePolicy {
    def getAction(s: MazeState) = MazeAction(PI * 2 * math.random)
}

object MazeEnvironment {
    val rewardOfCollision = 0.0
    val rewardOfStepOutsideGoalRegion = -1.0
    val rewardOfReachingGoal = 10.0
    var stepSize = 0.1
    val xLower = 0.0
    val xUpper = 1.0
    val yLower = 0.0
    val yUpper = 1.0
}

class MazeEnvironment extends LazyLogging {
    import GeoSettings._
    import MazeEnvironment._

    var location: Point2D = null
    var totalSteps: Int = 0
    var totalReward: Double = 0.0
    var policy: MazePolicy = null
    val mazeBound: Shapes = defaultBound
//    var obstacles: Shapes = defaultObstacle
    var obstacles: Shapes = obstaclesCollection.o6
    var goalRegions: Shapes = goalCollection.g1
    var controller: Controller = null
    var actionToPerform = MazeAction(random * PI * 2)
    var successfulEpisodes = 0

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
        totalReward = 0.0
        controller = new GPSARSASparseController(MazeState(location), actionToPerform)
//        controller = new GPSARSAUnoptimizedSparseControler(MazeState(location), actionToPerform)
        policy = controller.getPolicy
//        policy = RandomMazePolicy
    }

    def doStep(): Double = {
        val oldState = getCurrentState
        val oldAction = actionToPerform
        // Add random perturbation to the true action
        val trueAction = MazeAction(actionToPerform.radiansToMove + PI / 6 * (random - 0.5))
        val p2 = location.getPointFromThis(trueAction.radiansToMove, stepSize)
        val l = LineSegment(location, p2)
        var reward = 0.0
//        logger.debug(s"Candidate action: ${actionToPerform.radiansToMove} point: $p2")
        //Collision
        if (mazeBound.intersectsWithLineSeg(l) || obstacles.intersectsWithLineSeg(l)) {
            reward += rewardOfCollision + rewardOfStepOutsideGoalRegion
//            logger.debug("Collision")
        }
        //Reach the goal get the reward and
        //got flung elsewhere starting to explore again
        else if (goalRegions.contains(p2)) {
            reward += rewardOfReachingGoal
            location = getValidRandomInitialLocation
//            logger.debug("Reach goal")
            successfulEpisodes += 1
            logger.debug(s"Successful episodes $successfulEpisodes, " +
             s"total reward $totalReward")
        }
        //A step outside the goal regions
        else {
            reward += rewardOfStepOutsideGoalRegion
            location = p2
//            logger.debug("Haven't reached goal region")
        }
        totalReward += reward
        totalSteps += 1
        actionToPerform = policy.getAction(getCurrentState)
        val newState = getCurrentState
        controller.observeStep(oldState, oldAction,
            reward, newState, actionToPerform)
//        logger.debug(s"Current reward $reward, " +
//         s"total reward $totalReward, " +
//         s"location ${location}")
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
        plot.getDomainAxis.setRange(xLower, xUpper)
        plot.getRangeAxis.setRange(yLower, yUpper)
//        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0))
        plot.setDomainGridlinePaint(Color.lightGray)
        plot.setRangeGridlinePaint(Color.lightGray)
        val renderer = plot.getRenderer
        renderer.setSeriesShape(0, new Ellipse2D.Double(-6, -6, 12, 12))
        renderer.setSeriesPaint(0, Color.red)
        val localVectorRenderer = new VectorRenderer()
        localVectorRenderer.setSeriesPaint(0, Color.blue)
//        localVectorRenderer.setBaseToolTipGenerator(new VectorToolTipGenerator())
        plot.setRenderer(1, localVectorRenderer)
        // change the auto tick unit selection to integer units only...
        val rangeAxis = plot.getRangeAxis
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits())
        // OPTIONAL CUSTOMISATION COMPLETED.
        goalRegions.registerXYPlot(plot)
        obstacles.registerXYPlot(plot)
        mazeBound.registerXYPlot(plot)
//        for (i <- 1 to 200)
//            doStep()
        while(successfulEpisodes < 400)
            doStep()
        //Start the animation
        new DataGenerator(1).start()
        chart
    }

    class DataGenerator(delay: Int) extends Timer(delay, null) with ActionListener {
        addActionListener(this)

        def actionPerformed(event: ActionEvent): Unit = {
            doStep()
            plot.setDataset(0, genDataset)
            plot.setDataset(1, controller.getActionVectorSeries(0.02))
        }
    }
}

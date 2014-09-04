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
import org.jfree.chart.axis.{NumberTickUnit, NumberAxis}
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
    var rewardOfCollision = 0.0
    var rewardOfStepOutsideGoalRegion = -1.0
    var rewardOfReachingGoal = 10.0
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
    var actionVectorStep = 0.02
    var policy: MazePolicy = null
    val mazeBound: Shapes = defaultBound
//    var obstacles: Shapes = defaultObstacle
    var obstacles: Shapes = obstaclesCollection.o1
    var goalRegions: Shapes = goalCollection.g1
    var controller: Controller = null
    var actionToPerform = MazeAction(random * PI * 2)
    var successfulEpisodes = 0
    var displayAction = true
    var dataGenerator: DataGenerator = null

    def setDisplayAction(displayAction: Boolean): Unit = {
        this.displayAction = displayAction
        if (plot != null)
            plot.setDataset(1, controller.getActionVectorSeries(actionVectorStep, !displayAction))
    }

    def setRewardCollision(rewardOfCollision: Double): Unit = {
        MazeEnvironment.rewardOfCollision = rewardOfCollision
    }

    def setRewardGoal(rewardGoal: Double): Unit = {
        MazeEnvironment.rewardOfReachingGoal = rewardGoal
    }

    def setRewardStep(rewardStep: Double): Unit = {
        MazeEnvironment.rewardOfStepOutsideGoalRegion = rewardStep
    }

    def setObstacle(obstacle: Shapes): Unit = {
        if (obstacles != null)
            obstacles.unregisterXYPlot
        obstacles = obstacle
        obstacles.registerXYPlot(plot)
        if (!isLocationValid(location)) {
            location = getValidRandomInitialLocation
            updatePlot
        }
            
    }

    def setGoal(goal: Shapes): Unit = {
        if (goalRegions != null)
            goalRegions.unregisterXYPlot
        goalRegions = goal
        goalRegions.registerXYPlot(plot)
        if (!isLocationValid(location)) {
            location = getValidRandomInitialLocation
            updatePlot
        }
    }

    def isLocationValid(location: Point2D) =
        mazeBound.contains(location) &&
         !obstacles.contains(location) &&
         !goalRegions.contains(location)

    def getValidRandomInitialLocation = {
        var initialLoc = Point2D(math.random, math.random)
        while (!isLocationValid(initialLoc)) {
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
        successfulEpisodes = 0
        controller = new GPSARSASparseController(MazeState(location), actionToPerform)
//        controller = new GPSARSAUnoptimizedSparseControler(MazeState(location), actionToPerform)
        policy = controller.getPolicy
//        policy = RandomMazePolicy
        updatePlot
    }

    def doStep(doUpdatePlot: Boolean=false): (Double, Boolean) = {
        val oldState = getCurrentState
        val oldAction = actionToPerform
        // Add random perturbation to the true action
        val trueAction = MazeAction(actionToPerform.radiansToMove + PI / 6 * (random - 0.5))
        val p2 = location.getPointFromThis(trueAction.radiansToMove, stepSize)
        val l = LineSegment(location, p2)
        var reward = 0.0
        var episodeEnd = false
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
            episodeEnd = true
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
        if (doUpdatePlot) updatePlot
        (reward, episodeEnd)
    }

    def doEpisodes(n: Int=1,
                   doUpdatePlot: Boolean=false) {
//        val updateInterval = n / 10
        for (i <- 0 until n) {
            var episodeEnd = false
            while(!episodeEnd)
                episodeEnd = doStep(false)._2
//            if (updateInterval > 0 && i % updateInterval==0)
//                updatePlot
        }
        if (doUpdatePlot) updatePlot
    }

    def setPolicy(p: MazePolicy): Unit = {
        policy = p
    }

    def getTotalSteps: Int = totalSteps

    def getTotalReward: Double = totalReward

    def getCurrentState = MazeState(location)

    var chart: JFreeChart = null

    var plot: XYPlot = null

    def genAgentDataset: XYDataset = {
        val dataset = new DefaultXYDataset()
        dataset.addSeries("Agent", Array(Array(location.X), Array(location.Y)))
        dataset
    }

    def genGraph: JFreeChart = {
        chart = ChartFactory.createScatterPlot(
            "GP-SARSA DEMO", // chart title
            "X", // x axis label
            "Y", // y axis label
            genAgentDataset, // data
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
        plot.getDomainAxis.asInstanceOf[(NumberAxis)].
         setTickUnit(new NumberTickUnit(0.1))
        plot.getRangeAxis.setRange(yLower, yUpper)
        plot.getRangeAxis.asInstanceOf[(NumberAxis)].
         setTickUnit(new NumberTickUnit(0.1))
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
        plot.setDataset(1, controller.getActionVectorSeries(actionVectorStep, false))
        // change the auto tick unit selection to integer units only...
        val rangeAxis = plot.getRangeAxis
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits())
        // OPTIONAL CUSTOMISATION COMPLETED.
        goalRegions.registerXYPlot(plot)
        obstacles.registerXYPlot(plot)
        mazeBound.registerXYPlot(plot)
//        for (i <- 1 to 200)
//            doStep()
//        while(successfulEpisodes < 400)
//            doStep()
        //Start the animation
//        new DataGenerator(1).start()
        chart
    }

    def startAnimation {
        if (dataGenerator!=null)
            dataGenerator.restart()
        else {
            dataGenerator = new DataGenerator(1)
            dataGenerator.start()
        }
    }

    def pauseAnimation {
        if (dataGenerator!=null)
            dataGenerator.stop()
    }

    def updatePlot {
        if (plot!=null) {
            plot.setDataset(0, genAgentDataset)
            if (displayAction)
                plot.setDataset(1, controller.getActionVectorSeries(actionVectorStep, false))
        }
    }

    class DataGenerator(delay: Int) extends Timer(delay, null) with ActionListener {
        addActionListener(this)

        def actionPerformed(event: ActionEvent): Unit = {
            doStep()
            updatePlot
        }
    }
}

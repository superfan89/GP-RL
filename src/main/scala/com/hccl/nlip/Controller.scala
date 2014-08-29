package com.hccl.nlip

import org.jfree.data.xy.{VectorXYDataset, VectorSeriesCollection, VectorSeries}

/**
 * Created by Superfan on 2014/8/28.
 */
trait Controller {
    def getPolicy: MazePolicy
    def observeStep(oldState: MazeState,
                    action: MazeAction,
                    reward: Double,
                    newState: MazeState,
                    newAction: MazeAction): Unit
    def getActionVectorSeries(step: Double): VectorXYDataset
}

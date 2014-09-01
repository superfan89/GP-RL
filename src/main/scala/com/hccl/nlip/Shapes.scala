package com.hccl.nlip

/**
 * Created by Superfan on 2014/8/26.
 */

import java.awt.{BasicStroke, Color, Paint, Shape}
import java.awt.geom.Rectangle2D
import org.jfree.chart.annotations.{XYPolygonAnnotation, XYShapeAnnotation}
import org.jfree.chart.plot.XYPlot
import math._

trait Drawable {
    var plot: XYPlot = null
    def registerXYPlot(p: XYPlot): Unit = {
        plot = p
        updatePlot
    }
    def updatePlot: Unit = {}
}

trait Shapes extends Drawable {
    def contains(p: Point2D): Boolean
    def intersectsWithLineSeg(ls: LineSegment): Boolean
    def +(that: Shapes) = {
        val outer = this
        new Shapes {
            override def contains(p: Point2D) =
                outer.contains(p) || that.contains(p)

            override def intersectsWithLineSeg(ls: LineSegment) =
                outer.intersectsWithLineSeg(ls) || that.intersectsWithLineSeg(ls)

            override def registerXYPlot(p: XYPlot): Unit = {
                outer.plot = p
                that.plot = p
                updatePlot
            }

            override def updatePlot = {
                outer.updatePlot
                that.updatePlot
            }
        }
    }
}

object Point2D {
    def apply(X: Double, Y: Double) = new Point2D(X, Y)
}

class Point2D(val X: Double, val Y: Double) {
    def getPointFromThis(radiusToMove: Double, stepSize: Double) =
        Point2D(X + stepSize * cos(radiusToMove), Y + stepSize * sin(radiusToMove))

    def unary_- = Point2D(-X, -Y)

    def crossProdWith(that: Point2D) = X * that.Y - Y * that.X

    def dotProdWith(that: Point2D) = X * that.X + Y * that.Y

    def +(that: Point2D) = Point2D(X + that.X, Y + that.Y)

    def -(that: Point2D) = Point2D(X - that.X, Y - that.Y)

    override def toString = s"($X, $Y)"
}

object LineSegment {
    def apply(p1: Point2D, p2: Point2D) = new LineSegment(p1, p2)
}

class LineSegment(val p1: Point2D, val p2: Point2D) {
    //Ref: http://stackoverflow.com/questions/563198/how-do-you-detect-where-two-line-segments-intersect
    def touchLineSeg(ls: LineSegment) = {
        val p = p1
        val r = p2 - p1
        val q = ls.p1
        val s = ls.p2 - ls.p1
        if (r.crossProdWith(s)!=0) {
            val t = (q - p).crossProdWith(s) / r.crossProdWith(s)
            val u = (p - q).crossProdWith(r) / s.crossProdWith(r)
            if (t>=0 && t<=1 && u>=0 && u<=1)
                true
            else
                false
        }
        else {
            if (r.crossProdWith(s) == 0 && (q - p).crossProdWith(r) == 0) {
                val x = (q - p).dotProdWith(r)
                val y = (p - q).dotProdWith(s)
                if (x>=0 && x<=r.dotProdWith(r) || y>=0 && y<=s.dotProdWith(s))
                    true
                else
                    false
            }
            else
                false
        }
    }

    def containsPoint(p: Point2D) =
        abs((p2 - p1).crossProdWith(p - p1)) < 1e-6 && (p - p1).dotProdWith(p2 - p) >=0

    override def toString = s"$p1-$p2"
}

object Polygon {
    def apply(vertices: Seq[Point2D],
              outlinePaint: Paint = Color.black,
              fillPaint: Paint = null) = new Polygon(vertices, outlinePaint, fillPaint)
}

class Polygon(val vertices: Seq[Point2D],
              outlinePaint: Paint,
              fillPaint: Paint) extends Shapes {
    val nSides = vertices.length
    val verticesFlatArray = vertices.flatMap(p => Seq(p.X, p.Y)).toArray
    var anno: XYPolygonAnnotation = null

    def getAllSides: Seq[LineSegment] = {
        for (i <- Range(0, nSides))
            yield LineSegment(vertices((i + nSides - 1) % nSides), vertices(i))
    }

    def numberOfIntersectionWith(ls: LineSegment) =
        getAllSides.count(ls touchLineSeg)

    def intersectsWithLineSeg(ls: LineSegment) =
        numberOfIntersectionWith(ls) > 0

    def boundaryContains(p: Point2D) = getAllSides.exists(_ containsPoint p)

    def contains(p: Point2D): Boolean = {
        if (boundaryContains(p))
            true
        else {
            val virtualRay = LineSegment(Point2D(-100.101, -100.011), p)
            numberOfIntersectionWith(virtualRay) % 2 == 1
        }
    }

    override def updatePlot = {
        anno = new XYPolygonAnnotation(verticesFlatArray,
            new BasicStroke(1.0f), outlinePaint, fillPaint)
        plot.addAnnotation(anno)
    }

    override def toString = "Poly: " + vertices.mkString(" - ")
}

object Rectangular {
    def apply(pa: Point2D,
              pb: Point2D,
              outlinePaint: Paint = Color.black,
              fillPaint: Paint = null): Rectangular = {
        val (p1, p2) =
            if (pa.X < pb.X)
                (pa, pb)
            else
                (pb, pa)
        if (p1.Y < p2.Y)
            new Rectangular(Point2D(p1.X, p2.Y), p2, Point2D(p2.X, p1.Y), p1,
                outlinePaint, fillPaint)
        else
            new Rectangular(p1, Point2D(p2.X, p1.Y), p2, Point2D(p1.X, p2.Y),
                outlinePaint, fillPaint)
    }
}

class Rectangular(val p1: Point2D,
                  val p2: Point2D,
                  val p3: Point2D,
                  val p4: Point2D,
                  outlinePaint: Paint,
                  fillPaint: Paint) extends Polygon(Seq(p1, p2, p3, p4), outlinePaint, fillPaint) {
    val height = p1.Y - p3.Y
    val width = p3.X - p1.X

//    override def drawOnXYPlot(p: XYPlot): Unit = {
//        val rect = new Rectangle2D.Double(p4.X, p4.Y, width, height)
//        val anno = new XYShapeAnnotation(rect)
//        p.addAnnotation(anno)
//    }

    override def toString = s"Rect: $p1 - $p3"
}

//class Agent(var X: Double, var Y: Double) extends Drawable {
//    var anno: XYPolygonAnnotation = null
//
//    def updatePos(p: Point2D): Unit = {
//        X = p.X
//        Y = p.Y
//    }
//
//    override def updatePlot = {
//        anno = new XYPolygonAnnotation(verticesFlatArray)
//        plot.addAnnotation(anno)
    //    }
    //}

    object GeoSettings {
        val defaultBound = Rectangular(Point2D(0.0, 0.0), Point2D(1.0, 1.0))
        val defaultGoal = Rectangular(Point2D(0.0, 0.0), Point2D(1.0, 0.2),
            outlinePaint = Color.blue, fillPaint = Color.green)
    val defaultObstacle = Rectangular(Point2D(0.2, 0.4), Point2D(0.8, 0.6),
        fillPaint = Color.black)

    object goalCollection {
        val g1 = Rectangular(Point2D(0.0, 0.0), Point2D(0.1, 0.1), outlinePaint = Color.blue, fillPaint = Color.green)
    }

    object obstaclesCollection {
        val o1 = Rectangular(Point2D(0.0, 0.6), Point2D(0.4, 0.4), fillPaint = Color.black) +
         Rectangular(Point2D(0.6, 0.6), Point2D(1.0, 0.4), fillPaint = Color.black)
        val o2 = Rectangular(Point2D(0.0, 1.0), Point2D(0.4, 0.3), fillPaint = Color.black) +
         Rectangular(Point2D(0.6, 1.0), Point2D(1.0, 0.3), fillPaint = Color.black)
        val o3 = Rectangular(Point2D(0.0, 0.6), Point2D(0.1, 0.4), fillPaint = Color.black)
        val o4 = Rectangular(Point2D(0.0, 0.0), Point2D(-0.1, 0.4), fillPaint = Color.black)
        val o5 = Rectangular(Point2D(0.1, 0.4), Point2D(0.5, 0.5), fillPaint = Color.black) +
         Rectangular(Point2D(0.4, 0.0), Point2D(0.5, 0.5), fillPaint = Color.black)
    }
}
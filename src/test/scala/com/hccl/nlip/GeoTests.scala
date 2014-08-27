package com.hccl.nlip

/**
 * Created by Superfan on 2014/8/26.
 */
import org.scalatest.FlatSpec

class LineSpec extends FlatSpec {
    val l1 = LineSegment(Point2D(0.0, 0.0), Point2D(1.0, 1.0))
    val l2 = LineSegment(Point2D(1.0, 0.0), Point2D(0.0, 1.0))
    val l2_1 = LineSegment(Point2D(0.0, 1.0), Point2D(1.0, 0.0))
    val l3 = LineSegment(Point2D(1.0, 0.0), Point2D(0.5, 0.5))
    val l4 = LineSegment(Point2D(1.0, 0.0), Point2D(0.5, 0.5))
    val l5 = LineSegment(Point2D(0.0, 1.0), Point2D(-1.0, 0.0))

    val l6 = LineSegment(Point2D(-1.0, 0.0), Point2D(0.0, 1.0))
    val l7 = LineSegment(Point2D(1.1, 1.1), Point2D(1.3, 1.3))

    "A line segment" should "intersect with another" in {
        assert(l1.touchLineSeg(l2))
        assert(l1.touchLineSeg(l2_1))
        assert(l1.touchLineSeg(l3))
    }

    it should "touch itself" in {
        assert(l1.touchLineSeg(l1))
        assert(l2.touchLineSeg(l2))
        assert(l2.touchLineSeg(l2_1))
    }

    it should "not touch another parallel/disjoint line segment" in {
        assert(!l1.touchLineSeg(l6))
        assert(!l1.touchLineSeg(l7))
    }
}

class BoxAndPointSpec extends FlatSpec {
    import GeoSettings._

    val p1 = Point2D(0.5, 0.5)
    val p2 = Point2D(-1, 0.5)
    val p3 = Point2D(1.0, 1.0)
    val p4 = Point2D(0.0, 0.0)
    val p5 = Point2D(1.1, 1.1)

    "A box" should "contain inner points" in {
        assert(defaultBound.contains(p1))
    }

    it should "contain points on boundary" in {
        assert(defaultBound.contains(p3))
        assert(defaultBound.contains(p4))
    }

    it should "not contain outer points" in {
        assert(!defaultBound.contains(p2))
        assert(!defaultBound.contains(p5))
    }
}

class BoxAndLineSpec extends FlatSpec {
    import GeoSettings._

    val l1 = LineSegment(Point2D(0.1, 0.5), Point2D(0.9, 0.5))
    val l2 = LineSegment(Point2D(0.5, 0.1), Point2D(0.5, 0.9))
    val l3 = LineSegment(Point2D(0.1, 0.1), Point2D(0.9, 0.9))

    val l4 = LineSegment(Point2D(0.5, 0.5), Point2D(1.1, 0.5))
    val l5 = LineSegment(Point2D(0.5, 0.5), Point2D(0.5, 1.1))
    val l6 = LineSegment(Point2D(0.5, 0.5), Point2D(-0.1, 0.5))
    val l7 = LineSegment(Point2D(0.5, 0.5), Point2D(0.5, -0.1))

    val l8 = LineSegment(Point2D(1.1, 0.5), Point2D(1.1, 1))
    val l9 = LineSegment(Point2D(-3, 0), Point2D(-0.1, 0))

    "A box" should "not intersect with containing line segments" in {
        assert(!defaultBound.intersectsWithLineSeg(l1))
        assert(!defaultBound.intersectsWithLineSeg(l2))
        assert(!defaultBound.intersectsWithLineSeg(l3))
    }

    it should "intersect with intersecting line segments" in {
        assert(defaultBound.intersectsWithLineSeg(l4))
        assert(defaultBound.intersectsWithLineSeg(l5))
        assert(defaultBound.intersectsWithLineSeg(l6))
        assert(defaultBound.intersectsWithLineSeg(l7))
    }

    it should "intersect with its own sides" in {
        for (side <- defaultBound.getAllSides) {
            assert(defaultBound.intersectsWithLineSeg(side))
        }
    }

    it should "not intersect with disjoint line segments" in {
        assert(!defaultBound.intersectsWithLineSeg(l8))
        assert(!defaultBound.intersectsWithLineSeg(l9))
    }
}

class PolygonSpec extends FlatSpec {
    val poly1 = Polygon(List(Point2D(0, 0), Point2D(0, 4), Point2D(1, 1),
        Point2D(2, 4), Point2D(3, 1), Point2D(4, 4), Point2D(4, 0)))
    val p1 = Point2D(2, 1)
    val p2 = Point2D(1, 2)
    val p3 = Point2D(2, 2)
    val p4 = Point2D(3, 2)
    val p5 = Point2D(3.5, 1)
    val p6 = Point2D(-1, 1)

    val l1 = LineSegment(p6, p2)
    val l2 = LineSegment(p2, p3)
    val l3 = LineSegment(p3, p4)
    val l4 = LineSegment(p1, p5)
    val l5 = LineSegment(Point2D(1, 1), Point2D(3, 1))

    val l6 = LineSegment(p1, p3)
    val l7 = LineSegment(Point2D(0.5, 0.5), Point2D(0.5, 1.5))
    val l8 = LineSegment(Point2D(0.5, 0.5), Point2D(3.5, 0.5))

    "A polygon" should "contain inner points" in {
        assert(poly1.contains(p1))
        assert(poly1.contains(p3))
        assert(poly1.contains(p5))
    }

    it should "not contain outer points" in {
        assert(!poly1.contains(p2))
        assert(!poly1.contains(p4))
        assert(!poly1.contains(p6))
    }

    it should "contain its own vertices" in {
        for (p <- poly1.vertices) {
            assert(poly1.contains(p))
        }
    }

    it should "intersect with intersecting line segments" in {
        assert(poly1.intersectsWithLineSeg(l1))
        assert(poly1.intersectsWithLineSeg(l2))
        assert(poly1.intersectsWithLineSeg(l3))
        assert(poly1.intersectsWithLineSeg(l4))
        assert(poly1.intersectsWithLineSeg(l5))
    }

    it should "not intersect with inner line segments" in {
        assert(!poly1.intersectsWithLineSeg(l6))
        assert(!poly1.intersectsWithLineSeg(l7))
        assert(!poly1.intersectsWithLineSeg(l8))
    }

    it should "intersect with its own sides" in {
        for (side <- poly1.getAllSides) {
            assert(poly1.intersectsWithLineSeg(side))
        }
    }
}

class CompositeShapeSpec extends FlatSpec {
    val rect1 = Rectangular(Point2D(0.0, 0.0), Point2D(0.4, 0.4))
    val rect2 = Rectangular(Point2D(0.6, 0.0), Point2D(1.0, 0.4))
    val rect3 = Rectangular(Point2D(0.6, 0.6), Point2D(1.0, 1.0))
    val rect4 = Rectangular(Point2D(0.0, 0.6), Point2D(0.4, 1.0))

    val c1 = rect1 + rect2
    val c2 = rect3 + rect4
    val c3 = rect1 + rect3 + rect4
    val c4 = rect1 + rect2 + rect3 + rect4

    val l1 = LineSegment(Point2D(-1, 0.2), Point2D(0.2, 0.2))
    val l2 = LineSegment(Point2D(0.2, -1), Point2D(0.2, 0.2))
    val l3 = LineSegment(Point2D(0.8, 0.8), Point2D(0.8, 1.1))
    val l4 = LineSegment(Point2D(0.8, 0.8), Point2D(1.1, 0.8))

    val l5 = LineSegment(Point2D(0.5, 0.5), Point2D(0.5, 0.9))
    val l6 = LineSegment(Point2D(0.5, 0.5), Point2D(0.5, 0.1))
    val l7 = LineSegment(Point2D(0.5, 0.5), Point2D(0.1, 0.5))
    val l8 = LineSegment(Point2D(0.5, 0.5), Point2D(0.9, 0.5))

    "A composite shape" should "intersect with intersecting line segments" in {
        assert(rect1.intersectsWithLineSeg(l1))
        assert(rect1.intersectsWithLineSeg(l2))
        assert(rect3.intersectsWithLineSeg(l3))
        assert(rect3.intersectsWithLineSeg(l4))
        assert(rect1.intersectsWithLineSeg(l1))
        assert(c1.intersectsWithLineSeg(l1))
        assert(c1.intersectsWithLineSeg(l2))
        assert(c2.intersectsWithLineSeg(l3))
        assert(c2.intersectsWithLineSeg(l4))
        assert(c3.intersectsWithLineSeg(l3))
        assert(c3.intersectsWithLineSeg(l4))
        assert(c4.intersectsWithLineSeg(l1))
        assert(c4.intersectsWithLineSeg(l2))
        assert(c4.intersectsWithLineSeg(l3))
        assert(c4.intersectsWithLineSeg(l4))
    }

    it should "not intersect with disjoint line segments" in {
        assert(!c4.intersectsWithLineSeg(l5))
        assert(!c4.intersectsWithLineSeg(l6))
        assert(!c4.intersectsWithLineSeg(l7))
        assert(!c4.intersectsWithLineSeg(l8))
    }

    it should "contain points properly" in {
        for (i <- Range(0, 10)) {
            for (j <- Range(0, 10)) {
                val p = Point2D(i * 0.1, j * 0.1)
                if (c1.contains(p) || c2.contains(p) || c3.contains(p) || c4.contains(p))
                    assert(c4.contains(p))
                else
                    assert(!c4.contains(p))
            }
        }
    }
}
package org.proteovir.roimanager.utils;

import java.awt.BasicStroke;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for dilating (expanding) or eroding (contracting) a polygon
 * represented as an array of Point2D.Double without external dependencies.
 */
public class PolygonUtils {
    /**
     * Builds a closed Path2D from the given points.
     */
    private static Path2D.Double buildPath(Point2D.Double[] pts) {
        if (pts == null || pts.length < 3) {
            throw new IllegalArgumentException("A polygon must have at least 3 points");
        }
        Path2D.Double path = new Path2D.Double();
        path.moveTo(pts[0].x, pts[0].y);
        for (int i = 1; i < pts.length; i++) {
            path.lineTo(pts[i].x, pts[i].y);
        }
        path.closePath();
        return path;
    }

    /**
     * Extracts the outline points from a shape by flattening curves to line segments.
     */
    private static Polygon extractPoints(Area area, double flatness) {
        // 1) Decompose into individual rings
        List<Polygon> rings = new ArrayList<>();
        PathIterator pit = new FlatteningPathIterator(area.getPathIterator(null), flatness);
        double[] coords = new double[6];
        Polygon current = null;
        boolean inRing = false;

        while (!pit.isDone()) {
            int type = pit.currentSegment(coords);
            switch (type) {
              case PathIterator.SEG_MOVETO:
                // start a brand new ring
                current = new Polygon();
                rings.add(current);
                current.addPoint(
                  (int)Math.round(coords[0]),
                  (int)Math.round(coords[1])
                );
                inRing = true;
                break;

              case PathIterator.SEG_LINETO:
                if (inRing) {
                    current.addPoint(
                      (int)Math.round(coords[0]),
                      (int)Math.round(coords[1])
                    );
                }
                break;

              case PathIterator.SEG_CLOSE:
                if (inRing) {
                    // close the ring by repeating first point
                    current.addPoint(
                      current.xpoints[0],
                      current.ypoints[0]
                    );
                    inRing = false;
                }
                break;
            }
            pit.next();
        }

        // 2) Pick the ring with the largest absolute area
        Polygon outer = null;
        double maxArea = -1;
        for (Polygon ring : rings) {
            double a = Math.abs(signedArea(ring));
            if (a > maxArea) {
                maxArea = a;
                outer = ring;
            }
        }

        return outer;
    }

    /** Shoelace formula: positive or negative depending on winding */
    private static double signedArea(Polygon p) {
        double sum = 0;
        int n = p.npoints;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            sum += (p.xpoints[j] * p.ypoints[i] - p.xpoints[i] * p.ypoints[j]);
        }
        return sum / 2.0;
    }


    /**
     * Returns a new polygon offset outward by the given distance (dilation).
     * @param polygon Original polygon vertices (must form a closed shape)
     * @param distance Offset distance (>0 for dilation)
     * @return New polygon vertices after dilation
     */
    public static Polygon dilate(Polygon polygon, double distance) {
    	Point2D.Double[] pts = new Point2D.Double[polygon.npoints];
    	for (int i = 0; i < polygon.npoints; i ++) {
    		pts[i] = new Point2D.Double(polygon.xpoints[i], polygon.ypoints[i]);
    	}
        Path2D.Double path = buildPath(pts);
        // Stroke width = 2 * distance (centered stroke)
        BasicStroke stroke = new BasicStroke(
            (float)(2 * distance),
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND
        );
        Shape outline = stroke.createStrokedShape(path);
        Area area = new Area(path);
        area.add(new Area(outline));

        double flatness = Math.max(0.1, Math.abs(distance) / 4);
        return extractPoints(area, flatness);
    }

    /**
     * Returns a new polygon offset inward by the given distance (erosion).
     * @param polygon Original polygon vertices (must form a closed shape)
     * @param distance Offset distance (>0 for erosion amount)
     * @return New polygon vertices after erosion
     */
    public static Polygon erode(Polygon polygon, double distance) {
    	Point2D.Double[] pts = new Point2D.Double[polygon.npoints];
    	for (int i = 0; i < polygon.npoints; i ++) {
    		pts[i] = new Point2D.Double(polygon.xpoints[i], polygon.ypoints[i]);
    	}
        Path2D.Double path = buildPath(pts);
        BasicStroke stroke = new BasicStroke(
            (float)(2 * distance),
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND
        );
        Shape outline = stroke.createStrokedShape(path);
        Area area = new Area(path);
        area.subtract(new Area(outline));

        double flatness = Math.max(0.1, Math.abs(distance) / 4);
        return extractPoints(area, flatness);
    }

    /**
     * Checks whether two polygons overlap (i.e., have any intersecting area).
     * @param pol1 Vertices of the first polygon
     * @param pol2 Vertices of the second polygon
     * @return true if the intersection area is non-empty
     */
    public static boolean overlaps(Polygon pol1, Polygon pol2) {
    	Point2D.Double[] pts1 = new Point2D.Double[pol1.npoints];
    	for (int i = 0; i < pol1.npoints; i ++) {
    		pts1[i] = new Point2D.Double(pol1.xpoints[i], pol1.ypoints[i]);
    	}
    	Point2D.Double[] pts2 = new Point2D.Double[pol2.npoints];
    	for (int i = 0; i < pol2.npoints; i ++) {
    		pts2[i] = new Point2D.Double(pol2.xpoints[i], pol2.ypoints[i]);
    	}
        // Build Path2D for each polygon
        Path2D.Double path1 = buildPath(pts1);
        Path2D.Double path2 = buildPath(pts2);

        // Create Area objects and intersect them
        Area area1 = new Area(path1);
        Area area2 = new Area(path2);
        area1.intersect(area2);

        // If the resulting area is not empty, they overlap
        return !area1.isEmpty();
    }

    /**
     * Merges two overlapping polygons into a single combined region.
     * @param pol1 Vertices of the first polygon
     * @param pol2 Vertices of the second polygon
     * @return Vertices of the merged polygon
     */
    public static Polygon merge(Polygon pol1, Polygon pol2) {
    	Point2D.Double[] pts1 = new Point2D.Double[pol1.npoints];
    	for (int i = 0; i < pol1.npoints; i ++) {
    		pts1[i] = new Point2D.Double(pol1.xpoints[i], pol1.ypoints[i]);
    	}
    	Point2D.Double[] pts2 = new Point2D.Double[pol2.npoints];
    	for (int i = 0; i < pol2.npoints; i ++) {
    		pts2[i] = new Point2D.Double(pol2.xpoints[i], pol2.ypoints[i]);
    	}
        // Build areas for both polygons
        Area area1 = new Area(buildPath(pts1));
        Area area2 = new Area(buildPath(pts2));

        // Union the shapes
        area1.add(area2);

        // Flatten and extract outline points
        double flatness = 0.1;
        return extractPoints(area1, flatness);
    }
}

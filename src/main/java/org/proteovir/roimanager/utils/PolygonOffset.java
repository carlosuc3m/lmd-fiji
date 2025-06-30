package org.proteovir.roimanager.utils;
import java.awt.BasicStroke;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for dilating (expanding) or eroding (contracting) a polygon
 * represented as an array of Point2D.Double without external dependencies.
 */
public class PolygonOffset {
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
    private static List<Point2D.Double> extractPoints(Shape shape, double flatness) {
        List<Point2D.Double> pts = new ArrayList<>();
        PathIterator it = shape.getPathIterator(null, flatness);
        double[] coords = new double[6];
        while (!it.isDone()) {
            int type = it.currentSegment(coords);
            if (type == PathIterator.SEG_MOVETO || type == PathIterator.SEG_LINETO) {
                pts.add(new Point2D.Double(coords[0], coords[1]));
            }
            // SEG_CLOSE can be ignored because the first point == last point
            it.next();
        }
        return pts;
    }

    /**
     * Returns a new polygon offset outward by the given distance (dilation).
     * @param polygon Original polygon vertices (must form a closed shape)
     * @param distance Offset distance (>0 for dilation)
     * @return New polygon vertices after dilation
     */
    public static Point2D.Double[] dilate(Polygon polygon, double distance) {
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
        List<Point2D.Double> result = extractPoints(area, flatness);
        return result.toArray(new Point2D.Double[0]);
    }

    /**
     * Returns a new polygon offset inward by the given distance (erosion).
     * @param polygon Original polygon vertices (must form a closed shape)
     * @param distance Offset distance (>0 for erosion amount)
     * @return New polygon vertices after erosion
     */
    public static Point2D.Double[] erode(Polygon polygon, double distance) {
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
        List<Point2D.Double> result = extractPoints(area, flatness);
        return result.toArray(new Point2D.Double[0]);
    }
}

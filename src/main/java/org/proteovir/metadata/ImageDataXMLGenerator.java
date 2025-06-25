package org.proteovir.metadata;

import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;

public class ImageDataXMLGenerator {

    // Header fields
    private int globalCoordinates;
    private int calibrationX1;
    private int calibrationY1;
    private int calibrationX2;
    private int calibrationY2;
    private int calibrationX3;
    private int calibrationY3;
    
    // List of shapes
    private List<Polygon> shapes = new ArrayList<>();

    /**
     * Constructor to initialize the header values.
     *
     * @param globalCoordinates The GlobalCoordinates value.
     * @param calibrationX1     X coordinate of CalibrationPoint 1.
     * @param calibrationY1     Y coordinate of CalibrationPoint 1.
     * @param calibrationX2     X coordinate of CalibrationPoint 2.
     * @param calibrationY2     Y coordinate of CalibrationPoint 2.
     * @param calibrationX3     X coordinate of CalibrationPoint 3.
     * @param calibrationY3     Y coordinate of CalibrationPoint 3.
     */
    public ImageDataXMLGenerator() {
        this.globalCoordinates = 1;
    }
    
    public void setCalibrationPoints(List<Point> points) {
        this.calibrationX1 = points.get(0).x;
        this.calibrationY1 = points.get(0).y;
        this.calibrationX2 = points.get(1).x;
        this.calibrationY2 = points.get(1).y;
        this.calibrationX3 = points.get(2).x;
        this.calibrationY3 = points.get(2).y;
    }
    
    /**
     * Adds a shape to the list of shapes.
     *
     * @param shape The Shape to add.
     */
    public void addRoi(Polygon shape) {
        shapes.add(shape);
    }
    
    /**
     * Generates an XML string representation of the image data.
     *
     * @return A string containing the complete XML.
     */
    public String generateXML() {
        StringBuilder sb = new StringBuilder();
        
        // XML header
        sb.append("<?xml version='1.0' encoding='UTF-8'?>\n");
        sb.append("<ImageData>\n");

        // Header elements
        sb.append("  <GlobalCoordinates>").append(globalCoordinates).append("</GlobalCoordinates>\n");
        sb.append("  <X_CalibrationPoint_1>").append(calibrationX1).append("</X_CalibrationPoint_1>\n");
        sb.append("  <Y_CalibrationPoint_1>").append(calibrationY1).append("</Y_CalibrationPoint_1>\n");
        sb.append("  <X_CalibrationPoint_2>").append(calibrationX2).append("</X_CalibrationPoint_2>\n");
        sb.append("  <Y_CalibrationPoint_2>").append(calibrationY2).append("</Y_CalibrationPoint_2>\n");
        sb.append("  <X_CalibrationPoint_3>").append(calibrationX3).append("</X_CalibrationPoint_3>\n");
        sb.append("  <Y_CalibrationPoint_3>").append(calibrationY3).append("</Y_CalibrationPoint_3>\n");
        
        // Total number of shapes
        sb.append("  <ShapeCount>").append(shapes.size()).append("</ShapeCount>\n");

        // Loop through each shape
        for (int i = 0; i < shapes.size(); i++) {
            StringBuilder subSb = new StringBuilder();
        	Polygon shape = shapes.get(i);
            int shapeIndex = i + 1;
            subSb.append("  <Shape_").append(shapeIndex).append(">\n");
            
            // Loop through each point in the shape
            int[] xs = shape.xpoints;
            int[] ys = shape.ypoints;
            subSb.append("    <PointCount>").append(xs.length).append("</PointCount>\n");
            for (int ii = 0; ii < xs.length; ii ++) {
            	subSb.append("    <X_").append(ii).append(">").append(xs[ii])
                .append("</X_").append(ii).append(">\n");
            	subSb.append("    <Y_").append(ii).append(">").append(ys[ii])
                .append("</Y_").append(ii).append(">\n");
            }
            
            subSb.append("  </Shape_").append(shapeIndex).append(">\n");
            sb.append(String.format(subSb.toString(), xs.length));
        }

        sb.append("</ImageData>\n");
        return sb.toString();
    }

    /**
     * Main method to demonstrate usage.
     */
    public static void main(String[] args) {
        // Initialize the generator with header values
        ImageDataXMLGenerator generator = new ImageDataXMLGenerator();
        
        // Generate the XML and print it
        String xmlOutput = generator.generateXML();
        System.out.println(xmlOutput);
    }
}

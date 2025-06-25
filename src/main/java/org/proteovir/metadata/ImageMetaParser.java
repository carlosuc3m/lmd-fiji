package org.proteovir.metadata;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ImageMetaParser {

    // Parsed metadata fields
    private double tilePosX;
    private double tilePosY;
    private double tileDimX;
    private double tileDimY;
    private double pixelSizeX;
    private double pixelSizeY;
    private int nbPixelsX;
    private int nbPixelsY;
    private String filename;

    /**
     * Constructor: parses the given image file's metadata.
     * @param imageFile path to the TIFF image
     * @param unit desired unit ("µm", "um", "mm", or "m")
     * @throws Exception on file errors or parsing failures
     */
    public ImageMetaParser(String imageFile, String unit) throws Exception {
        File metaFile = new File(imageFile);
        if (!metaFile.exists()) {
            throw new IllegalArgumentException("File does not exist: " + imageFile);
        }

        String name = metaFile.getName();
        int dotIndex = name.lastIndexOf(".xml");
        if (dotIndex < 0) {
            throw new IllegalArgumentException("File is not an .xml");
        }
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(metaFile);

        // 2. Prepare XPath
        XPath xpath = XPathFactory.newInstance().newXPath();

        // 3. XPath expressions for X and Y lengths
        String exprX = "/Data/Image/ImageDescription/Dimensions/DimensionDescription[@DimID='X']/@%s";
        String exprY = "/Data/Image/ImageDescription/Dimensions/DimensionDescription[@DimID='Y']/@%s";

        // 4. Evaluate
        String lenXStr = (String) xpath.evaluate(String.format(exprX, "Length"), doc, XPathConstants.STRING);
        String lenYStr = (String) xpath.evaluate(String.format(exprY, "Length"), doc, XPathConstants.STRING);
        String voxelXStr = (String) xpath.evaluate(String.format(exprX, "Voxel"), doc, XPathConstants.STRING);
        String voxelYStr = (String) xpath.evaluate(String.format(exprY, "Voxel"), doc, XPathConstants.STRING);
        String numXStr = (String) xpath.evaluate(String.format(exprX, "NumberOfElements"), doc, XPathConstants.STRING);
        String numYStr = (String) xpath.evaluate(String.format(exprY, "NumberOfElements"), doc, XPathConstants.STRING);
        
        String posX = "";//tile.getAttribute("PosX");
        String posY = "";//tile.getAttribute("PosY");
        

        // Unit conversion factors
        double posFac, dimFac;
        switch (unit) {
            case "µm":
            case "um":
                posFac = 1e6; dimFac = 1.0; break;
            case "mm":
                posFac = 1e3; dimFac = 1e-3; break;
            case "m":
                posFac = 1.0; dimFac = 1e-6; break;
            default:
                throw new IllegalArgumentException("Unsupported unit: " + unit);
        }

        // Populate fields
        this.tilePosX = parseCsn(posX) * posFac;
        this.tilePosY = parseCsn(posY) * posFac;
        this.tileDimX = parseCsn(lenXStr) * dimFac;
        this.tileDimY = parseCsn(lenYStr) * dimFac;
        this.pixelSizeX = parseCsn(voxelXStr) * dimFac;
        this.pixelSizeY = parseCsn(voxelYStr) * dimFac;
        this.nbPixelsX = (int) parseCsn(numXStr);
        this.nbPixelsY = (int) parseCsn(numYStr);

        System.out.println(this.filename + " " + this);
    }

    // Comma-separated number parser
    private static double parseCsn(String number) {
        return Double.parseDouble(number.replace(",", ""));
    }

    // Helper to find an Element by attribute in a NodeList
    private static Element getElementByAttribute(NodeList nodes, String attrName, String attrValue) throws Exception {
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) node;
                if (attrValue.equals(elem.getAttribute(attrName))) {
                    return elem;
                }
            }
        }
        throw new Exception("Element with " + attrName + "=\"" + attrValue + "\" not found");
    }

    // Getters for all metadata fields
    public double getTilePosX() { return tilePosX; }
    public double getTilePosY() { return tilePosY; }
    public double getTileDimX() { return tileDimX; }
    public double getTileDimY() { return tileDimY; }
    public double getPixelSizeX() { return pixelSizeX; }
    public double getPixelSizeY() { return pixelSizeY; }
    public int getNbPixelsX() { return nbPixelsX; }
    public int getNbPixelsY() { return nbPixelsY; }
    public String getFilename() { return filename; }

    @Override
    public String toString() {
        return String.format("{tilePosX=%.3f, tilePosY=%.3f, tileDimX=%.3f, tileDimY=%.3f, pixelSizeX=%.3f, pixelSizeY=%.3f, nbPixelsX=%d, nbPixelsY=%d}",
                tilePosX, tilePosY, tileDimX, tileDimY, pixelSizeX, pixelSizeY, nbPixelsX, nbPixelsY);
    }

    // CLI entry
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java ImageMetaParser --imageFile <path> [--unit <unit>]");
            return;
        }
        String imageFile = null;
        String unit = "µm";
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--imageFile":
                    imageFile = args[++i]; break;
                case "--unit":
                    unit = args[++i]; break;
                default:
                    System.err.println("Unknown argument: " + args[i]); return;
            }
        }
        if (imageFile == null) {
            System.err.println("Error: --imageFile is required"); return;
        }
        try {
            new ImageMetaParser(imageFile, unit);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

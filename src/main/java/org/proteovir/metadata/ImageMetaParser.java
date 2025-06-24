package org.proteovir.metadata;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
        File file = new File(imageFile);
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + imageFile);
        }

        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex < 0) {
            throw new IllegalArgumentException("File has no extension");
        }
        String ext = name.substring(dotIndex);
        this.filename = name.substring(0, dotIndex);

        if (!ext.matches("(?i)\\\\.tiff?")) {
            throw new IllegalArgumentException("File not a TIFF: " + ext);
        }

        File metaFile = new File(file.getParentFile(), "MetaData" + File.separator + this.filename + "_Properties.xml");
        if (!metaFile.exists()) {
            throw new IllegalArgumentException("No metadata file found at " + metaFile.getPath());
        }

        // Parse XML
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(metaFile);
        doc.getDocumentElement().normalize();

        // Extract TileScanInfo
        NodeList attachments = doc.getElementsByTagName("Attachment");
        Element tileScanInfo = getElementByAttribute(attachments, "Name", "TileScanInfo");
        Element tile = (Element) tileScanInfo.getElementsByTagName("Tile").item(0);
        String posXStr = tile.getAttribute("PosX");
        String posYStr = tile.getAttribute("PosY");

        // Extract Dimensions
        NodeList dimsList = doc.getElementsByTagName("Dimensions");
        Element dimsElem = (Element) dimsList.item(0);
        NodeList dims = dimsElem.getElementsByTagName("Dimension");
        Element dimX = getElementByAttribute(dims, "DimID", "X");
        Element dimY = getElementByAttribute(dims, "DimID", "Y");
        String lenXStr = dimX.getAttribute("Length");
        String voxelXStr = dimX.getAttribute("Voxel");
        String numXStr = dimX.getAttribute("NumberOfElements");
        String lenYStr = dimY.getAttribute("Length");
        String voxelYStr = dimY.getAttribute("Voxel");
        String numYStr = dimY.getAttribute("NumberOfElements");

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
        this.tilePosX = parseCsn(posXStr) * posFac;
        this.tilePosY = parseCsn(posYStr) * posFac;
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

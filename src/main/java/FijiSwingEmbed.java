import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;

import javax.swing.*;
import java.awt.*;

public class FijiSwingEmbed {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 1) Load an image (from args[0] or a default URL)
            String path = args.length > 0
                ? args[0]
                : "/home/carlos/Desktop/Fiji.app/models/StarDist Fluorescence Nuclei Segmentation_10042025_141036/sample_output_0.tif";
            ImagePlus imp = IJ.openImage(path);
            if (imp == null) {
                IJ.error("Could not open image at " + path);
                return;
            }

            // 2) Create an ImageCanvas for that ImagePlus
            //    (there is no ImagePlus.setCanvas(...) API; you just wrap it here)
            ImageCanvas canvas = new ImageCanvas(imp);  // :contentReference[oaicite:0]{index=0}

            // 3) Put the canvas into a scroll pane so large images can scroll
            JScrollPane imagePane = new JScrollPane(canvas);

            // 4) Create the OK button
            JButton okButton = new JButton("OK");
            okButton.addActionListener(e -> {
                System.out.println("OK pressed");
                System.exit(0);
            });

            // 5) Build a right‐hand panel for the button
            JPanel rightPanel = new JPanel();
            rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
            rightPanel.add(okButton);
            rightPanel.add(Box.createVerticalGlue());

            // 6) Lay out everything in your main frame
            JFrame frame = new JFrame("Fiji Embedded ImagePlus");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());
            frame.add(imagePane, BorderLayout.CENTER);
            frame.add(rightPanel, BorderLayout.EAST);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // 7) Trigger the initial draw
            imp.updateAndDraw();
        });
    }
}

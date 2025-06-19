package org.proteovir.gui;

import java.io.File;
import java.util.function.Consumer;

import org.proteovir.roimanager.ConsumerInterface;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.ImageLayout;
import ij.gui.PlotCanvas;
import ij.gui.PlotWindow;

/**
 * A simple Swing application demonstrating a 4:3 window with:
 * - A square canvas on the left that fills the full height
 * - A vertical column of buttons on the right
 */
public class MainPanel extends MainPanelGUI {

    private static final long serialVersionUID = 8550100052731297639L;
    
    private ImagePlus imp;
    private ImageCanvas currentCanvas;

	public MainPanel(ConsumerInterface consumer) {
		super(consumer);
		Consumer<File> openIm = (file) -> {
            // load the ImagePlus and its canvas
            imp = IJ.openImage(file.getAbsolutePath());
            imp.show();
            ImageCanvas ic = imp.getCanvas();
            if (ic == null) ic = new ImageCanvas(imp);

            // remove the old one
            canvasPanel.removeAll();

            // keep a reference so we can resize on-the-fly
            currentCanvas = ic;

            // add it and size it to fill the panel
            canvasPanel.add(ic);
            ic.setBounds(0, 0, canvasPanel.getWidth(), canvasPanel.getHeight());

            canvasPanel.revalidate();
            canvasPanel.repaint();
		};
		
		this.sidePanel.setOpenImageConsumer(openIm);
    }

	public void close() {
		// TODO Auto-generated method stub
		
	}

	public void setCancelCallback(Runnable runnable) {
		// TODO Auto-generated method stub
		
	}
}

package org.proteovir.gui;

import java.io.File;
import java.util.function.Function;

import org.proteovir.roimanager.ConsumerInterface;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;

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
		Function<File, Boolean> openIm = (file) -> {
		    try {
		        ImagePlus imp = IJ.openImage(file.getAbsolutePath());
		        if (imp == null) {
		            return false;
		        }
		        imp.show();
		        return true;
		    } catch (Exception ex) {
		        // you can log ex here if you like
		        return false;
		    }
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

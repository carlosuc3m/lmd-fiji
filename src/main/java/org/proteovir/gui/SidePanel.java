package org.proteovir.gui;

import java.io.File;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.proteovir.roimanager.ConsumerInterface;
import org.proteovir.roimanager.RoiManager;

import ij.IJ;
import ij.ImagePlus;

public class SidePanel extends SidePanelGUI {

    private static final long serialVersionUID = -8405747451234902128L;
    
    private ImagePlus imp;

	public SidePanel(ConsumerInterface consumer) {
		super(consumer);
		Consumer<File> openIm = (file) -> {
            // load the ImagePlus and its canvas
            imp = IJ.openImage(file.getAbsolutePath());
            imp.show();
		};
		imageGUI.setOpenImageCallback(openIm);
    }

	public void setOpenImageConsumer(Consumer<File> openIm) {
		this.imageGUI.setOpenImageCallback(openIm);
	}

	public void close() {
		// TODO Auto-generated method stub
		
	}

	public void setCancelCallback(Runnable runnable) {
		// TODO Auto-generated method stub
		
	}
}

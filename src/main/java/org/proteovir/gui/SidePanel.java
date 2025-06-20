package org.proteovir.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.io.IOException;
import java.util.function.Function;

import org.proteovir.roimanager.ConsumerInterface;

import ai.nets.samj.communication.model.SAM2Tiny;
import ai.nets.samj.ui.SAMJLogger;
import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.util.Cast;

public class SidePanel extends SidePanelGUI implements ActionListener, ImageListener {

    private static final long serialVersionUID = -8405747451234902128L;
    
    private ImagePlus imp;
    
    SAM2Tiny samj;
    
    private static final String LOST_FOCUS = ""
    		+ "<html>"
    		+ "<span style=\"color: orange;\">Warning: Select again target image</span>"
    		+ "</html>";
    
    private static final SAMJLogger LOGGER = new SAMJLogger() {
		@Override
		public void info(String text) {System.out.println(text);}
		@Override
		public void warn(String text) {System.out.println(text);}
		@Override
		public void error(String text) {System.out.println(text);}
	};
	
	private Function<File, Boolean> openIm = (file) -> {
	    try {
	        ImagePlus imp = IJ.openImage(file.getAbsolutePath());
	        if (imp == null) {
	            return false;
	        }
	        imp.show();
	        imp.getWindow().addWindowFocusListener(new WindowFocusListener() {
	            @Override
	            public void windowGainedFocus(WindowEvent e) {
	                changeOnFocusGained(imp);
	            }
	            @Override
	            public void windowLostFocus(WindowEvent e) {
	                activationBtn.setSelected(false);
	                activationBtn.setEnabled(false);
	                activationLabel.setText(LOST_FOCUS);
	            }
	        });
	        return true;
	    } catch (Exception ex) {
	        // you can log ex here if you like
	        return false;
	    }
	};

	public SidePanel(ConsumerInterface consumer) {
		super(consumer);
		imageGUI.setOpenImageCallback(openIm);
		samjBtn.addActionListener(this);
		
		samjBtn.setEnabled(false);
		activationBtn.setEnabled(false);
    }

	public void setOpenImageConsumer(Function<File, Boolean> openIm) {
		this.imageGUI.setOpenImageCallback(openIm);
	}
	
	private void changeOnFocusGained(ImagePlus imp) {
		
	}

	public void close() {
		// TODO Auto-generated method stub
		
	}

	public void setCancelCallback(Runnable runnable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (samj == null)
			samj = new SAM2Tiny();
		new Thread(() -> {
			try {
				samj.setImage(Cast.unchecked(ImageJFunctions.wrap(imp)), LOGGER);
			} catch (IOException | InterruptedException | RuntimeException e1) {
				e1.printStackTrace();
			}
			
		}).start();
	}

	@Override
	public void imageOpened(ImagePlus imp) {
	}

	@Override
	public void imageClosed(ImagePlus imp) {
		if (!imp.equals(this.imp))
			return;
		this.imageGUI.setTargetSet(false);
		this.samj.closeProcess();
		this.activationBtn.setEnabled(false);
		this.samjBtn.setEnabled(false);
		this.imageGUI.imagePath.setText("");
	}

	@Override
	public void imageUpdated(ImagePlus imp) {
		if (!imp.equals(this.imp))
			return;
	}
}

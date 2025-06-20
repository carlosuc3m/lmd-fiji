package org.proteovir.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.io.IOException;
import java.util.function.Function;

import javax.swing.SwingUtilities;

import org.proteovir.roimanager.ConsumerInterface;

import ai.nets.samj.communication.model.SAM2Tiny;
import ai.nets.samj.ui.SAMJLogger;
import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.gui.Toolbar;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.util.Cast;

public class SidePanel extends SidePanelGUI implements ActionListener, ImageListener {

    private static final long serialVersionUID = -8405747451234902128L;
    
    private ImagePlus imp;
    
    private boolean alreadyFocused = false;
    
    SAM2Tiny samj;
    
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
	        imp = IJ.openImage(file.getAbsolutePath());
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
	            }
	        });
	        imp.getWindow().requestFocus();
	        return true;
	    } catch (Exception ex) {
	        // you can log ex here if you like
	        return false;
	    }
	};

	public SidePanel(ConsumerInterface consumer) {
		super(consumer);
		samjBtn.setEnabled(false);
		activationBtn.setEnabled(false);
		
		imageGUI.setOpenImageCallback(openIm);
		
		samjBtn.addActionListener(this);
		ImagePlus.addImageListener(this);
    }

	public void setOpenImageConsumer(Function<File, Boolean> openIm) {
		this.imageGUI.setOpenImageCallback(openIm);
	}
	
	private boolean isValidPromptSelected() {
		return Toolbar.getToolName().equals("rectangle")
				 || Toolbar.getToolName().equals("point")
				 || Toolbar.getToolName().equals("multipoint");
	}
	
	private void changeOnFocusGained(ImagePlus imp) {
		if (alreadyFocused)
			return;
		this.activationLabel.setText("");
		this.samjBtn.setEnabled(true);
		boolean samjLoaded = samj != null && samj.isLoaded();
		this.samjBtn.setSelected(samjLoaded);
		this.activationBtn.setSelected(false);
		if (isValidPromptSelected() && samjLoaded) {
			this.activationBtn.setEnabled(true);
			this.activationLabel.setText(ACTIVATE_TO_SEGMENT);
		} else if(samjLoaded) {
			this.activationBtn.setEnabled(false);
			this.activationLabel.setText(ONLY_PROMPTS);
		} else {
			this.activationBtn.setEnabled(false);
			this.activationLabel.setText(SAMJ);
		}
		alreadyFocused = true;
		
	}

	public void close() {
		// TODO Auto-generated method stub
		
	}

	public void setCancelCallback(Runnable runnable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		new Thread(() -> {
			SwingUtilities.invokeLater(() -> blockToEncode(true));
			if (samj == null || !samj.isLoaded())
				samj = new SAM2Tiny();
			try {
				samj.setImage(Cast.unchecked(ImageJFunctions.wrap(imp)), LOGGER);
			} catch (IOException | InterruptedException | RuntimeException e1) {
				e1.printStackTrace();
			}
			SwingUtilities.invokeLater(() -> blockToEncode(false));
			
		}).start();
	}

	@Override
	public void imageOpened(ImagePlus imp) {
		if (this.imp != null && imp.equals(this.imp))
			return;
		imp.getWindow().addWindowFocusListener(new WindowFocusListener() {

			@Override
			public void windowGainedFocus(WindowEvent e) {
            	alreadyFocused = false;
                activationBtn.setSelected(false);
                activationBtn.setEnabled(false);
                if (SidePanel.this.imp != null)
                	activationLabel.setText(LOST_FOCUS);
                else
                	activationLabel.setText(OPEN_TARGET);
			}

			@Override
			public void windowLostFocus(WindowEvent e) {
			}
			
		});
	}

	@Override
	public void imageClosed(ImagePlus imp) {
		if (imp == null || !imp.equals(this.imp))
			return;
		if (samj != null)
			this.samj.closeProcess();
		this.imageGUI.setTargetSet(false);
		this.activationBtn.setSelected(false);
		this.activationBtn.setEnabled(false);
		this.samjBtn.setEnabled(false);
		this.samjBtn.setSelected(false);
		this.imageGUI.imagePath.setText("");
		activationLabel.setText(OPEN_TARGET);
		this.imp = null;
		alreadyFocused = false;
	}

	@Override
	public void imageUpdated(ImagePlus imp) {
	}
}

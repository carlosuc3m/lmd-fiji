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
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.util.Cast;

public class SidePanel extends SidePanelGUI implements ActionListener, ImageListener {

    private static final long serialVersionUID = -8405747451234902128L;
    
    private ImagePlus imp;
    
    private boolean alreadyFocused = false;
    
    private SAM2Tiny samj;
    
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
		activationBtn.addActionListener(this);
		ImagePlus.addImageListener(this);
    }

	public void setOpenImageConsumer(Function<File, Boolean> openIm) {
		this.imageGUI.setOpenImageCallback(openIm);
	}
	
	private void changeOnFocusGained(ImagePlus imp) {
		if (alreadyFocused)
			return;
		alreadyFocused = true;
		if (samj != null && samj.isLoaded() && isValidPromptSelected() && wasActive) {
			activationBtn.setSelected(true);
			activationBtn.setEnabled(true);
			activationLabel.setText(READY);
			return;
		} else if (samj != null && samj.isLoaded() && isValidPromptSelected()) {
			activationBtn.setSelected(false);
			activationBtn.setEnabled(true);
			activationLabel.setText(ACTIVATE_TO_SEGMENT);
			return;
		} else if (samj != null && samj.isLoaded()) {
			activationBtn.setSelected(false);
			activationBtn.setEnabled(false);
			activationLabel.setText(ONLY_PROMPTS);
			return;
		} else {
			samjBtn.setEnabled(true);
			samjBtn.setSelected(false);
			activationBtn.setSelected(false);
			activationBtn.setEnabled(false);
			activationLabel.setText(SAMJ);
			return;
		}
		
	}

	public void close() {
		if (samj != null && samj.isLoaded())
			samj.closeProcess();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == samjBtn) {
			new Thread(() -> {
				SwingUtilities.invokeLater(() -> blockToEncode(true));
				SwingUtilities.invokeLater(() -> activationLabel.setText(ENCODING));
				try {
					if (samj == null || !samj.isLoaded())
						samj = new SAM2Tiny();
					samj.setImage(Cast.unchecked(ImageJFunctions.wrap(imp)), LOGGER);
					guiAfterEnconding(true);
				} catch (IOException | InterruptedException | RuntimeException e1) {
					e1.printStackTrace();
					guiAfterEnconding(false);
				}
				
			}).start();
		} else if (e.getSource() == activationBtn && activationBtn.isSelected()) {
			wasActive = false;
			activationLabel.setText(ACTIVATE_TO_SEGMENT);
		} else if (e.getSource() == activationBtn) {
			wasActive = true;
			activationLabel.setText(READY);
		}
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
		roiManager.block(true);
		this.imp = null;
		alreadyFocused = false;
	}

	@Override
	public void imageUpdated(ImagePlus imp) {
	}
}

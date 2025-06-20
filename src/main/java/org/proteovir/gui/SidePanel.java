package org.proteovir.gui;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;

import javax.swing.SwingUtilities;

import org.proteovir.roimanager.RoiManagerConsumer;

import ai.nets.samj.annotation.Mask;
import ai.nets.samj.communication.model.SAM2Tiny;
import ai.nets.samj.models.AbstractSamJ;
import ai.nets.samj.ui.SAMJLogger;
import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.Toolbar;
import io.bioimage.modelrunner.system.PlatformDetection;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.util.Cast;

public class SidePanel extends SidePanelGUI implements ActionListener, ImageListener, MouseListener {

    private static final long serialVersionUID = -8405747451234902128L;
    
    private ImagePlus imp;
    
    private boolean alreadyFocused = false;
    
    private SAM2Tiny samj;
    
    /**
	 * Counter of the ROIs created
	 */
	private int promptsCreatedCnt = 0;
	/**
	 * For the point prompts, whether if hte user is collecting several prompts (pressing the ctrl key)
	 * or just one
	 */
	private boolean isCollectingPoints = false;
	/**
	 * All the points being collected that reference the instance of interest
	 */
	private List<Localizable> collectedPoints = new ArrayList<Localizable>();
	/**
	 * All the points being collected that reference the background (ctrl + alt)
	 */
	private List<Localizable> collecteNegPoints = new ArrayList<Localizable>();
	/**
	 * Save lists of rois that have been added at the same time to delete them if necessary
	 */
   private Stack<List<Mask>> undoStack = new Stack<>();
   /**
    * Save lists of polygons deleted at the same time to undo their deleting
    */
   private Stack<List<Mask>> redoStack = new Stack<>();
   /**
    * List of the annotated masks on an image
    */
   private Stack<List<Mask>> annotatedMask = new Stack<List<Mask>>();
   /**
    * List that keeps track of the annotated masks
    */
   private Stack<List<Mask>> redoAnnotatedMask = new Stack<List<Mask>>();
   /**
    * Tracks if Ctrl+Z has already been handled
    */
   private boolean undoPressed = false;
   /**
    * Tracks if Ctrl+Y has already been handled
    */
   private boolean redoPressed = false;
    
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
	        imp.getCanvas().addMouseListener(SidePanel.this);
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

	public SidePanel() {
		this(null);
	}

	public SidePanel(RoiManagerConsumer consumer) {
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
					samj.setReturnOnlyBiggest(true);
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
	public void mouseReleased(MouseEvent e) {
		if (!wasActive || !alreadyFocused || imp.getRoi() == null)
			return;
		if (Toolbar.getToolName().equals("rectangle")) {
			annotateRect();
		} else if (Toolbar.getToolName().equals("point") || Toolbar.getToolName().equals("multipoint")) {
			annotatePoints(e);
		} else {
			return;
		}
		if (!isCollectingPoints) imp.deleteRoi();
	}
	
	private void annotateRect() {
		final Roi roi = imp.getRoi();
		final Rectangle rectBounds = roi.getBounds();
		final Interval rectInterval = new FinalInterval(
				new long[] { rectBounds.x, rectBounds.y },
				new long[] { rectBounds.x+rectBounds.width-1, rectBounds.y+rectBounds.height-1 } );
		submitRectPrompt(rectInterval);
	}
	
	private void submitRectPrompt(Interval rectInterval) {
		try {
			addToRoiManager(this.samj.fetch2dSegmentation(rectInterval), "rect");
		} catch (Exception ex) {
			ex.printStackTrace();;
		}
	}
	
	private void annotatePoints(MouseEvent e) {
		final Roi roi = imp.getRoi();
		if ((e.isControlDown() && !PlatformDetection.isMacOS()) || (e.isMetaDown() && PlatformDetection.isMacOS())) {
			//add point to the list only
			isCollectingPoints = true;
			Iterator<java.awt.Point> iterator = roi.iterator();
			java.awt.Point p = iterator.next();
			while (iterator.hasNext()) p = iterator.next();
			collectedPoints.add( new Point(p.x,p.y) );
		} else {
			isCollectingPoints = false;
			//collect this last one
			Iterator<java.awt.Point> iterator = roi.iterator();
			java.awt.Point p = iterator.next();
			while (iterator.hasNext()) p = iterator.next();
			collectedPoints.add( new Point(p.x,p.y) );
			submitAndClearPoints();
		}
	}

	/**
	 * Send the point prompts to SAM and clear the lists collecting them
	 */
	private void submitAndClearPoints() {
		if (this.samj == null) return;
		if (collectedPoints.size() == 0) return;

		//TODO log.info("Image window: Processing now points, this count: "+collectedPoints.size());
		isCollectingPoints = false;
		imp.deleteRoi();
		Rectangle zoomedRectangle = this.imp.getCanvas().getSrcRect();
		try {
			if (imp.getWidth() * imp.getHeight() > Math.pow(AbstractSamJ.MAX_ENCODED_AREA_RS, 2)
					|| imp.getWidth() > AbstractSamJ.MAX_ENCODED_SIDE || imp.getHeight() > AbstractSamJ.MAX_ENCODED_SIDE)
				addToRoiManager(samj.fetch2dSegmentation(collectedPoints, collecteNegPoints, zoomedRectangle),
						(collectedPoints.size() > 1 ? "points" : "point"));
			else
				addToRoiManager(samj.fetch2dSegmentation(collectedPoints, collecteNegPoints),
						(collectedPoints.size() > 1 ? "points" : "point"));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		collectedPoints = new ArrayList<Localizable>();
		collecteNegPoints = new ArrayList<Localizable>();
	}
	
	void addToRoiManager(final List<Mask> polys, final String promptShape) {
		if (this.roiManager.getROIsNumber() == 0 && undoStack.size() != 0)
			annotatedMask.clear();
			
		this.redoStack.clear();
		this.redoAnnotatedMask.clear();
		promptsCreatedCnt++;
		int resNo = 1;
		List<Mask> undoRois = new ArrayList<Mask>();
		for (Mask m : polys) {
			m.setName(promptsCreatedCnt + "." + (resNo ++) + "_"+promptShape + "_" + this.samj.getName());
			this.roiManager.addRoi(m);
			undoRois.add(m);
		}
		this.undoStack.push(undoRois);
		this.annotatedMask.push(polys);
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
	@Override
	public void mouseClicked(MouseEvent e) {		
	}
	@Override
	public void mousePressed(MouseEvent e) {		
	}
	@Override
	public void mouseEntered(MouseEvent e) {		
	}
	@Override
	public void mouseExited(MouseEvent e) {		
	}
}

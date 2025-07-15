package org.proteovir.gui;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.proteovir.cellpose.LMDCellpose;
import org.proteovir.metadata.ImageDataXMLGenerator;
import org.proteovir.roimanager.RoiManagerConsumer;
import org.proteovir.roimanager.commands.AddRoiCommand;
import org.proteovir.roimanager.commands.Command;
import org.proteovir.roimanager.commands.DeleteRoiCommand;

import ai.nets.samj.annotation.Mask;
import ai.nets.samj.communication.model.SAM2Tiny;
import ai.nets.samj.models.AbstractSamJ;
import ai.nets.samj.ui.SAMJLogger;
import ij.IJ;
import ij.IJEventListener;
import ij.ImageListener;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.plugin.CompositeConverter;
import io.bioimage.modelrunner.exceptions.LoadModelException;
import io.bioimage.modelrunner.exceptions.RunModelException;
import io.bioimage.modelrunner.system.PlatformDetection;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.util.Cast;

public class SidePanel extends SidePanelGUI implements ActionListener, ImageListener, MouseListener, KeyListener, IJEventListener {

    private static final long serialVersionUID = -8405747451234902128L;
    
    private ImagePlus imp;
    
    private boolean alreadyFocused = false;
    
    private SAM2Tiny samj;
    
    private LMDCellpose cellpose;
    
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
    * List of the annotated masks on an image
    */
   private Stack<Command> annotatedMask = new Stack<Command>();
   /**
    * List that keeps track of the annotated masks
    */
   private Stack<Command> redoAnnotatedMask = new Stack<Command>();
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
	        imp.getCanvas().addKeyListener(SidePanel.this);
			imp.getCanvas().removeKeyListener(IJ.getInstance());
	        imp.getWindow().addWindowFocusListener(new WindowFocusListener() {
	            @Override
	            public void windowGainedFocus(WindowEvent e) {
	                changeOnFocusGained(imp);
	            }
	            @Override
	            public void windowLostFocus(WindowEvent e) {
	            }
	        });
	        roiManager.deleteAll();
	        imp.getWindow().requestFocus();
	        return true;
	    } catch (Exception ex) {
	        // you can log ex here if you like
	        return false;
	    }
	};
	
	private static final String LMD_SUFFIX = "_LMD_ROIS";
	
	private Runnable closeSAMJCallback = () -> {
		if (samj != null && samj.isLoaded())
			samj.closeProcess();
		wasActive = false;
		activationBtn.setSelected(false);
		activationBtn.setEnabled(false);
		samjBtn.setEnabled(false);
		samjBtn.setSelected(false);
		activationLabel.setText(OPEN_TARGET);
		roiManager.block(true);
		this.imp = null;
		alreadyFocused = false;
	};
	
	private Consumer<Boolean> activationCallback = (bool) -> {
		wasActive = bool;
	};

	public SidePanel() {
		this(null);
	}

	public SidePanel(RoiManagerConsumer consumer) {
		super(consumer);
		cellposeBtn.setEnabled(false);
		samjBtn.setEnabled(false);
		activationBtn.setEnabled(false);
		
		activationBtn.setSelectionCallback(activationCallback);
		imageGUI.setOpenImageCallback(openIm);
		imageGUI.setChangeImageCallback(closeSAMJCallback);

		roiManager.getList().addMouseListener(this);
		roiManager.setExportLMDcallback((masks) -> exportLMDFormat(masks));
		roiManager.addCommandCallback((cmd) -> {
			redoAnnotatedMask.clear();
			annotatedMask.add(cmd);
		});
		cellposeBtn.addActionListener(this);
		samjBtn.addActionListener(this);
		activationBtn.addActionListener(this);
		ImagePlus.addImageListener(this);
    	IJ.addEventListener(this);
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
			cellposeBtn.setEnabled(true);
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
					boolean isColorRGB = imp.getType() == ImagePlus.COLOR_RGB;
					Img<?> image = ImageJFunctions.wrap(isColorRGB ? CompositeConverter.makeComposite(imp) : imp);
					samj.setImage(Cast.unchecked(image), LOGGER);
					samj.setReturnOnlyBiggest(true);
					roiManager.setImage(imp);
					guiAfterEnconding(true);
				} catch (IOException | InterruptedException | RuntimeException e1) {
					e1.printStackTrace();
					guiAfterEnconding(false);
				}
				
			}).start();
		} else if (e.getSource() == cellposeBtn) {
			new Thread(() -> {
				SwingUtilities.invokeLater(() -> blockToCellpose(true));
				SwingUtilities.invokeLater(() -> activationLabel.setText(RUNNING_CELLPOSE));
				try {
					if (cellpose == null || !cellpose.isLoaded())
						cellpose = LMDCellpose.create();
					boolean isColorRGB = imp.getType() == ImagePlus.COLOR_RGB;
					Img<?> image = ImageJFunctions.wrap(isColorRGB ? CompositeConverter.makeComposite(imp) : imp);
					if (!cellpose.isLoaded())
						cellpose.loadModel();
					List<Mask> masks = cellpose.inferenceContours(Cast.unchecked(Collections.singletonList(image)));
					roiManager.setImage(imp);
					guiAfterCellpose(true, samj);
					masks.stream().forEach(mm -> roiManager.addRoi(mm));
				} catch (IOException | InterruptedException | RuntimeException | LoadModelException | RunModelException e1) {
					e1.printStackTrace();
					guiAfterCellpose(false, samj);
				}
				
			}).start();
		} else if (e.getSource() == activationBtn && activationBtn.isSelected()) {
			activationLabel.setText(ACTIVATE_TO_SEGMENT);
		} else if (e.getSource() == activationBtn) {
			activationLabel.setText(READY);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getSource() == this.roiManager.getList())
			return;
		if (!wasActive || !alreadyFocused || imp.getRoi() == null) {
			return;
		}
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
		if (this.roiManager.getROIsNumber() == 0 && annotatedMask.size() != 0 
				&& !(annotatedMask.peek() instanceof DeleteRoiCommand)) {
			annotatedMask.clear();
			roiManager.deleteAll();
		}
			
		this.redoAnnotatedMask.clear();
		promptsCreatedCnt++;
		int resNo = 1;
		List<Mask> masks = new ArrayList<Mask>();
		for (Mask m : polys) {
			m.setName(promptsCreatedCnt + "." + (resNo ++) + "_"+promptShape + "_" + this.samj.getName());
			masks.add(m);
		}
		Command command = new AddRoiCommand(roiManager, masks);
		command.execute();
		this.annotatedMask.push(command);
	}

	private Object exportLMDFormat(List<Mask> masks) {
		ImageDataXMLGenerator generator = new ImageDataXMLGenerator();
		List<java.awt.Point> calPoints = new ArrayList<java.awt.Point>();
		calPoints.add(firstCalibration.getAbsCalPoint());
		calPoints.add(secondCalibration.getAbsCalPoint());
		calPoints.add(thirdCalibration.getAbsCalPoint());
		generator.setCalibrationPoints(calPoints);
		for (Mask mm : masks) {
			Polygon pol = imageGUI.toAbsCoord(mm.getContour());
			generator.addRoi(pol);
		}
		String xmltext = generator.generateXML();
        // Write the XML string into the file
		File fileToSave = new File(generatedCalibrationName());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
            writer.write(xmltext);
            IJ.showMessage("LMD Roi file successfully saved at:" + System.lineSeparator() + fileToSave.getAbsolutePath());;
        } catch (IOException e) {
            // Display an error dialog and print the stack trace for debugging
            JOptionPane.showMessageDialog(null, "Error saving file: " + e.getMessage(),
                                          "Save Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return null;
	}
	
	private String generatedCalibrationName() {
		String str = imageGUI.imagePath.getText().trim();
		String strNoSuffix = str;
		if (str.lastIndexOf(".") != -1)
			strNoSuffix = str.substring(0, str.lastIndexOf("."));
		String fname = strNoSuffix + LMD_SUFFIX;
		int i = 1;
		while (new File(fname + ".xml").exists()) {
			fname += "-" + i;
			i ++;
		}
		return fname + ".xml";
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
		this.cellposeBtn.setEnabled(false);
		this.samjBtn.setEnabled(false);
		this.samjBtn.setSelected(false);
		this.imageGUI.imagePath.setText("");
		activationLabel.setText(OPEN_TARGET);
		roiManager.block(true);
		roiManager.readyToExport(false);
		this.imp = null;
		alreadyFocused = false;
		imageGUI.setInfoState(false);
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == this.roiManager.getList() && activationBtn.isSelected()) {
			this.activationBtn.setSelected(false);
			this.activationLabel.setText(ACTIVATE_TO_SEGMENT);
			wasActive = false;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z && this.annotatedMask.size() != 0 && !redoPressed) {
        	redoPressed = true;
        	Command undo = annotatedMask.pop();
        	undo.undo();
        	redoAnnotatedMask.push(undo);
        } else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Y && this.redoAnnotatedMask.size() != 0 && !undoPressed) {
        	undoPressed = true;
        	Command redo = redoAnnotatedMask.pop();
        	redo.execute();
        	annotatedMask.push(redo);
        }
        e.consume();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if ((e.getKeyCode() == KeyEvent.VK_CONTROL && !PlatformDetection.isMacOS()) 
				|| (e.getKeyCode() == KeyEvent.VK_META && PlatformDetection.isMacOS())) {
			submitAndClearPoints();
		}
	    if (e.getKeyCode() == KeyEvent.VK_Z) {
	        redoPressed = false;
	    }
	    if (e.getKeyCode() == KeyEvent.VK_Y) {
	        undoPressed = false;
	    }
	}
	
	@Override
	public void eventOccurred(int eventID) {
		if (eventID != IJEventListener.TOOL_CHANGED)
			return;
		boolean isvalid = IJ.getToolName().equals("rectangle") 
				|| IJ.getToolName().equals("point") 
				|| IJ.getToolName().equals("multipoint");
		if (!isvalid && activationBtn.isEnabled()) {
			this.activationBtn.setSelected(isvalid);
			this.activationBtn.setEnabled(isvalid);
			this.activationLabel.setText(ONLY_PROMPTS);
		} else if (isvalid && samjBtn.isSelected() && !activationBtn.isEnabled() && WindowManager.getCurrentImage().equals(imp)) {
			activationBtn.setEnabled(isvalid);
			activationLabel.setText(ACTIVATE_TO_SEGMENT);
		}
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
	}
	@Override
	public void imageUpdated(ImagePlus imp) {
	}
	@Override
	public void mouseEntered(MouseEvent e) {		
	}
	@Override
	public void mouseExited(MouseEvent e) {		
	}
	@Override
	public void keyTyped(KeyEvent e) {		
	}
}

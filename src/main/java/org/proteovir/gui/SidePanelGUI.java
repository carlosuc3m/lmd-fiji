package org.proteovir.gui;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.proteovir.gui.components.ColoredButton;
import org.proteovir.gui.components.IntegerTextField;
import org.proteovir.roimanager.RoiManagerConsumer;
import org.proteovir.roimanager.RoiManagerIJ;

import ai.nets.samj.communication.model.SAMModel;

import org.proteovir.roimanager.RoiManager;

import ij.gui.Toolbar;

public class SidePanelGUI extends JPanel {

    private static final long serialVersionUID = -8405747451234902128L;
    
    protected boolean wasActive = false;
    
    protected JLabel statusLabel;
    
    protected CalibrationPointsGUI firstCalibration;
    
    protected CalibrationPointsGUI secondCalibration;
    
    protected CalibrationPointsGUI thirdCalibration;
    
    protected ImageGUI imageGUI;
    
    protected ColoredButton cellposeBtn;
    
    protected JLabel diameterLabel;
    
    protected IntegerTextField diameterVal;
    
    protected ColoredButton samjBtn;
    
    protected ColoredButton activationBtn;
    
    protected JLabel activationLabel;
    
    protected RoiManager roiManager;
    
    protected static final String NOT_CALIBRATED = ""
    		+ "<html>"
    		+ "<span style=\"color: red;\">&#9888; Calibration points not set</span>"
    		+ "</html>";
    
    protected static final String CALIBRATED = ""
    		+ "<html>"
    		+ "<span style=\"color: green;\">&#x2714; Calibration points ready</span>"
    		+ "</html>";
    
    protected static final String LOST_FOCUS = ""
    		+ "<html>"
    		+ "<span style=\"color: orange;\">&#9888; Focus again on target image</span>"
    		+ "</html>";
    
    protected static final String ONLY_PROMPTS = ""
    		+ "<html>"
    		+ "<font color='orange'>&#9888; Only rect and points!</font>"
    		+ "</html>";
    
    protected static final String OPEN_TARGET = ""
    		+ "<html>"
    		+ "<font color='orange'>&#9888; Open image to annotate</font>"
    		+ "</html>";
    
    protected static final String ACTIVATE_TO_SEGMENT = ""
    		+ "<html>"
    		+ "<span style=\"color: blue;\">Activate to start annotating</span>"
    		+ "</html>";
    
    protected static final String SAMJ = ""
    		+ "<html>"
    		+ "<span style=\"color: blue;\">Click on CELLPOSE or SAMJ to start</span>"
    		+ "</html>";
    
    protected static final String READY = ""
    		+ "<html>"
    		+ "<span style=\"color: green;\">Ready to annotate</span>"
    		+ "</html>";
    
    protected static final String ENCODING = ""
    		+ "<html>"
    		+ "<span style=\"color: gray;\">Processing...</span>"
    		+ "</html>";
    
    protected static final String ERROR_ENCODING = ""
    		+ "<html>"
    		+ "<span style=\"color: red;\">&#9888; Error running SAMJ</span>"
    		+ "</html>";
    
    protected static final String RUNNING_CELLPOSE = ""
    		+ "<html>"
    		+ "<span style=\"color: gray;\">Running CellPose...</span>"
    		+ "</html>";
    
    protected static final String ERROR_CELLPOSE = ""
    		+ "<html>"
    		+ "<span style=\"color: red;\">&#9888; Error running CellPose</span>"
    		+ "</html>";
    
    
    private static final double ROI_MANAGER_H_RATIO = 0.542d;
    
    private static final double CAL_PANEL_H_RATIO = 0.067d;
    
    private static final double STATUS_PANEL_H_RATIO = 0.03d;
    
    private static final double IMAGE_PANEL_H_RATIO = 0.067d;
    
    private static final double SAMJ_BUTON_H_RATIO = 0.05d;
    
    private static final double CELLPOSE_BUTON_H_RATIO = 0.05d;
    
    private static final double CELLPOSE_BUTON_W_RATIO = 0.5d;
    
    private static final double ACTIVATION_BUTON_H_RATIO = 0.035d;
    
    private static final double ACTIVATION_LABEL_H_RATIO = 0.025d;

	public SidePanelGUI() {
		this(new RoiManagerIJ());
	}

	public SidePanelGUI(RoiManagerConsumer consumer) {
		if (consumer == null)
			consumer = new RoiManagerIJ();
		setFocusable(true);
		
		statusLabel = new JLabel(NOT_CALIBRATED);
		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		statusLabel.setVerticalAlignment(SwingConstants.CENTER);
		Runnable callback = () -> {
			boolean allGood = firstCalibration.isCalibrated() 
					&& secondCalibration.isCalibrated() 
					&& thirdCalibration.isCalibrated();
			setCalibrated(allGood);
			imageGUI.setCalibrated(allGood);
			
		};
		firstCalibration = new CalibrationPointsGUI(1);
		firstCalibration.setCallback(callback);
		secondCalibration = new CalibrationPointsGUI(2);
		secondCalibration.setCallback(callback);
		thirdCalibration = new CalibrationPointsGUI(3);
		thirdCalibration.setCallback(callback);
		
		imageGUI = new ImageGUI();
		imageGUI.notifyRoiManager(() -> {
			boolean allGood = firstCalibration.isCalibrated() 
					&& secondCalibration.isCalibrated() 
					&& thirdCalibration.isCalibrated();
			roiManager.readyToExport(allGood && imageGUI.isDefined());
		});

		cellposeBtn = new ColoredButton("CELLPOSE", new Color(150, 255, 150), Color.LIGHT_GRAY,
				new Color(190, 255, 190), new Color(220, 220, 220));
		diameterLabel = new JLabel("Cell diameter");
		diameterLabel.setHorizontalAlignment(SwingConstants.CENTER);
		diameterLabel.setVerticalAlignment(SwingConstants.CENTER);
		diameterVal = new IntegerTextField("Optional (only integers)");
		samjBtn = new ColoredButton("SAMJ", new Color(150, 255, 150), Color.LIGHT_GRAY,
				new Color(190, 255, 190), new Color(220, 220, 220));
		activationBtn =  new ColoredButton("Activate", new Color(150, 255, 150), Color.LIGHT_GRAY,
				new Color(190, 255, 190), new Color(220, 220, 220));
		activationLabel = new JLabel(OPEN_TARGET);
		activationLabel.setHorizontalAlignment(SwingConstants.CENTER);
		activationLabel.setVerticalAlignment(SwingConstants.CENTER);
		
		roiManager = new RoiManager(consumer);
		roiManager.block(true);
        
		add(statusLabel);
		add(firstCalibration);
		add(secondCalibration);
		add(thirdCalibration);
		add(imageGUI);
		add(cellposeBtn);
		add(diameterLabel);
		add(diameterVal);
		add(samjBtn);
		add(activationBtn);
		add(activationLabel);
		add(roiManager);
    }
	
	@Override
	public void doLayout() {
        int rawW = getWidth();
        int rawH = getHeight();
        int inset = 2;
        
        int statusH = Math.max(inset, (int) (rawH * STATUS_PANEL_H_RATIO));
        int calH = Math.max(inset, (int) (rawH * CAL_PANEL_H_RATIO));
        int imH = Math.max(inset, (int) (rawH * IMAGE_PANEL_H_RATIO));
        int cellposeH = Math.max(inset, (int) (rawH * CELLPOSE_BUTON_H_RATIO));
        int cellposeW = Math.max(inset, (int) (rawW * CELLPOSE_BUTON_W_RATIO));
        int samjH = Math.max(inset, (int) (rawH * SAMJ_BUTON_H_RATIO));
        int actH = Math.max(inset, (int) (rawH * ACTIVATION_BUTON_H_RATIO));
        int actLabelH = Math.max(inset, (int) (rawH * ACTIVATION_LABEL_H_RATIO));
        int roiH = Math.max(inset, (int) (rawH * ROI_MANAGER_H_RATIO));
                        
        int w = Math.max(inset, rawW - 2 * inset);
        int y = inset;
        statusLabel.setBounds(inset, y, w, statusH - inset);
        y = statusH;
        firstCalibration.setBounds(inset, y, w, calH - inset);
        y = statusH + calH;
        secondCalibration.setBounds(inset, y, w, calH - inset);
        y = statusH + calH * 2;
        thirdCalibration.setBounds(inset, y, w, calH - inset);
        y = statusH + calH * 3;
        imageGUI.setBounds(inset, y, w, imH - inset);
        y = statusH + calH * 3 + imH;
        int diameterW = Math.max(inset, (w - cellposeW) / 2 - inset * 2);
        cellposeBtn.setBounds(diameterW + inset, y, cellposeW, cellposeH - inset);
        diameterLabel.setBounds(diameterW + cellposeW + 2 * inset * 2, y, diameterW, (cellposeH - inset) / 2);
        diameterVal.setBounds(diameterW + cellposeW + 2 * inset * 2, y + (cellposeH - inset) / 2, diameterW, (cellposeH - inset) / 2);
        y = statusH + calH * 3 + imH + cellposeH;
        samjBtn.setBounds(inset, y, w, samjH - inset);
        y = statusH + calH * 3 + imH + cellposeH + samjH;
        activationBtn.setBounds(inset, y, w, actH - inset);
        y = statusH + calH * 3 + imH + cellposeH + samjH + actH;
        activationLabel.setBounds(inset, y, w, actLabelH - inset);
        y = statusH + calH * 3 + imH + cellposeH + samjH + actH + actLabelH;

        roiManager.setBounds(inset, y, w, roiH - inset);
	}
	
	protected void setCalibrated(boolean isCalibrated) {
		if (isCalibrated)
			statusLabel.setText(CALIBRATED);
		else
			statusLabel.setText(NOT_CALIBRATED);
		roiManager.readyToExport(isCalibrated && imageGUI.isDefined());
	}
	
	protected void blockToEncode(boolean block) {
		this.samjBtn.setLoading(block);

		this.samjBtn.setEnabled(!block);
		this.cellposeBtn.setEnabled(!block);
		this.diameterLabel.setEnabled(!block);
		this.diameterVal.setEnabled(!block);
		this.activationBtn.setEnabled(!block);
		
		this.firstCalibration.block(block);
		this.secondCalibration.block(block);
		this.thirdCalibration.block(block);
		
		this.imageGUI.block(block);
		this.roiManager.block(block);
	}
	
	protected void blockToCellpose(boolean block) {
		this.cellposeBtn.setLoading(block);

		this.samjBtn.setEnabled(!block);
		this.cellposeBtn.setEnabled(!block);
		this.diameterLabel.setEnabled(!block);
		this.diameterVal.setEnabled(!block);
		this.activationBtn.setEnabled(!block);
		
		this.firstCalibration.block(block);
		this.secondCalibration.block(block);
		this.thirdCalibration.block(block);
		
		this.imageGUI.block(block);
		this.roiManager.block(block);
	}
	
	protected boolean isValidPromptSelected() {
		return Toolbar.getToolName().equals("rectangle")
				 || Toolbar.getToolName().equals("point")
				 || Toolbar.getToolName().equals("multipoint");
	}
	
	protected void guiAfterEnconding(boolean success) {
		SwingUtilities.invokeLater(() -> {
			blockToEncode(false);
			roiManager.block(!success);
			samjBtn.setEnabled(!success);
			samjBtn.setSelected(success);
			activationBtn.setEnabled(success);
			if (!success) {
				activationBtn.setSelected(false);
				activationLabel.setText(ERROR_ENCODING);
			} else if (activationLabel.getText().equals(LOST_FOCUS)) {
				activationBtn.setSelected(false);
				activationBtn.setEnabled(false);
				activationLabel.setText(LOST_FOCUS);
				wasActive = false;
			} else if (isValidPromptSelected()) {
				activationBtn.setSelected(true);
				activationLabel.setText(READY);
				wasActive = true;
			} else if (!isValidPromptSelected()){
				activationBtn.setSelected(false);
				activationBtn.setEnabled(false);
				activationLabel.setText(ONLY_PROMPTS);
				wasActive = false;
			} else {
				activationBtn.setSelected(false);
				activationBtn.setEnabled(false);
				activationLabel.setText(LOST_FOCUS);
				wasActive = false;
			}
		});
	}
	
	protected void guiAfterCellpose(boolean success, SAMModel samj) {
		SwingUtilities.invokeLater(() -> {
			blockToCellpose(false);
			roiManager.block(!success && (samj == null || !samj.isLoaded()));
			cellposeBtn.setEnabled(success);
			cellposeBtn.setSelected(false);
			diameterLabel.setEnabled(success);
			diameterVal.setEnabled(success);
			wasActive = false;
			activationBtn.setSelected(false);
			if (!success) {
				activationLabel.setText(ERROR_CELLPOSE);
			} else if (samj == null || !samj.isLoaded()) {
				activationLabel.setText(SAMJ);
			} else if (activationLabel.getText().equals(LOST_FOCUS)) {
				activationBtn.setEnabled(false);
				activationLabel.setText(LOST_FOCUS);
			} else if (isValidPromptSelected()) {
				activationLabel.setText(ACTIVATE_TO_SEGMENT);
			} else if (!isValidPromptSelected()){
				activationBtn.setEnabled(false);
				activationLabel.setText(ONLY_PROMPTS);
			} else {
				activationBtn.setEnabled(false);
				activationLabel.setText(LOST_FOCUS);
			}
		});
	}
}

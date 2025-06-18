package org.proteovir.gui;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class SidePanelGUI extends JPanel {

    private static final long serialVersionUID = -8405747451234902128L;
    
    private JLabel statusLabel;
    
    private CalibrationPointsGUI firstCalibration;
    
    private CalibrationPointsGUI secondCalibration;
    
    private CalibrationPointsGUI thirdCalibration;
    
    private ImageGUI imageGUI;
    
    private JButton samjBtn;
    
    private JButton activationBtn;
    
    private JLabel activationLabel;
    
    private RoiManagerGUI roiManagerGUI;
    
    
    private static final double ROI_MANAGER_H_RATIO = 0.4d;
    
    private static final double CAL_PANEL_H_RATIO = 0.12d;
    
    private static final double STATUS_PANEL_H_RATIO = 0.03d;
    
    private static final double IMAGE_PANEL_H_RATIO = 0.10d;
    
    private static final double SAMJ_BUTON_H_RATIO = 0.05d;
    
    private static final double ACTIVATION_BUTON_H_RATIO = 0.035d;
    
    private static final double ACTIVATION_LABEL_H_RATIO = 0.025d;

	public SidePanelGUI() {
		
		statusLabel = new JLabel("Calibration points not set");
		firstCalibration = new CalibrationPointsGUI(1);
		secondCalibration = new CalibrationPointsGUI(2);
		thirdCalibration = new CalibrationPointsGUI(3);
		
		imageGUI = new ImageGUI();
		
		samjBtn = new JButton("SAMJ");
		activationBtn = new JButton("Activate");
		activationLabel = new JLabel("");
		
		roiManagerGUI = new RoiManagerGUI();
        
		add(statusLabel);
		add(firstCalibration);
		add(secondCalibration);
		add(thirdCalibration);
		add(imageGUI);
		add(samjBtn);
		add(activationBtn);
		add(activationLabel);
		add(roiManagerGUI);
    }
	
	@Override
	public void doLayout() {
        int rawW = getWidth();
        int rawH = getHeight();
        int inset = 2;
        
        int statusH = Math.max(inset, (int) (rawH * STATUS_PANEL_H_RATIO));
        int calH = Math.max(inset, (int) (rawH * CAL_PANEL_H_RATIO));
        int imH = Math.max(inset, (int) (rawH * IMAGE_PANEL_H_RATIO));
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
        samjBtn.setBounds(inset, y, w, samjH - inset);
        y = statusH + calH * 3 + imH + samjH;
        activationBtn.setBounds(inset, y, w, actH - inset);
        y = statusH + calH * 3 + imH + samjH + actH;
        activationLabel.setBounds(inset, y, w, actLabelH - inset);
        y = statusH + calH * 3 + imH + samjH + actH + actLabelH;

        roiManagerGUI.setBounds(inset, y, w, roiH - inset);
	}
}

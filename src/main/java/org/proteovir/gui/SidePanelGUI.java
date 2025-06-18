package org.proteovir.gui;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class SidePanelGUI extends JPanel {

    private static final long serialVersionUID = -8405747451234902128L;
    
    private JLabel statusLabel;
    
    private CalibrationPointsGUI firstCalibration;
    
    private CalibrationPointsGUI secondCalibration;
    
    private CalibrationPointsGUI thirdCalibration;
    
    private ImageGUI imageGUI;
    
    private RoiManagerGUI roiManagerGUI;
    
    
    private static final double ROI_MANAGER_H_RATIO = 0.4d;

	public SidePanelGUI() {
		
		statusLabel = new JLabel();
		firstCalibration = new CalibrationPointsGUI();
		secondCalibration = new CalibrationPointsGUI();
		thirdCalibration = new CalibrationPointsGUI();
		
		imageGUI = new ImageGUI();
		
		roiManagerGUI = new RoiManagerGUI();
        
		add(statusLabel);
		add(firstCalibration);
		add(secondCalibration);
		add(thirdCalibration);
		add(imageGUI);
		add(roiManagerGUI);
		
        organiseComponents();
    }
    
    private void organiseComponents() {
    	addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int rawW = getWidth();
                int rawH = getHeight();
                
                int roiH = (int) (rawH * ROI_MANAGER_H_RATIO);
                roiH = Math.max(1, roiH);
                
                int restH = Math.max(0, rawH - roiH);

                roiManagerGUI.setBounds(0, restH, rawW, roiH);
            }
        });
    }

	public void close() {
		// TODO Auto-generated method stub
		
	}

	public void setCancelCallback(Runnable runnable) {
		// TODO Auto-generated method stub
		
	}
}

package org.proteovir.gui;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class CalibrationPointsGUI extends JPanel {

    private static final long serialVersionUID = -8405747451234902128L;
    
    private final int n;
    
    JLabel title;
    PlaceholderTextField imagePath;
    JButton imageBtn;
    PlaceholderTextField metadataPath;
    JButton metaBtn;
    // TODO remove JLabel status;

	public CalibrationPointsGUI(int n) {
		this.n = n;
		title = new JLabel(String.format("Calibration image %s not set", "" + n));
		imagePath = new PlaceholderTextField(String.format("Choose caibration image %s", "" + n));
		metadataPath = new PlaceholderTextField(String.format("Choose metadata for calibration image %s", "" + n));

		imageBtn = new JButton("Add image");
		metaBtn = new JButton("Add image metadata");
		
		add(title);
		add(imagePath);
		add(imageBtn);
		add(metadataPath);
		add(metaBtn);
        
        organiseComponents();
    }
    
    private void organiseComponents() {
    	addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
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

package org.proteovir.gui;

import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
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
		
		setBorder(BorderFactory.createLineBorder(Color.black, 1));
		
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
                int rawW = getWidth();
                int rawH = getHeight();
                int inset = 2;
                
                int labelH = Math.max(inset, (int) (rawH / 3));
                int imH = Math.max(inset, (int) (rawH / 3));
                int metaH = Math.max(inset, (int) (rawH / 3));
                
                int y = inset;
                title.setBounds(inset, y, rawW - 2 * inset, labelH - inset);
                y = labelH;
                int pathW = (int) ((rawW - inset * 3) * 0.7);
                int btnW = (int) ((rawW - inset * 3) * 0.3);
                imagePath.setBounds(inset, y, pathW, imH - inset);
                imageBtn.setBounds(inset + pathW + inset, y, btnW, imH - inset);

                metadataPath.setBounds(inset, y, pathW, imH - inset);
                metaBtn.setBounds(inset + pathW + inset, y, btnW, imH - inset);
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

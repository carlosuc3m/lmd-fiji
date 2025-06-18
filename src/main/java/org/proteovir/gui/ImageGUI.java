package org.proteovir.gui;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ImageGUI extends JPanel {
    
    JLabel title;
    PlaceholderTextField imagePath;
    JButton imageBtn;
    PlaceholderTextField metadataPath;
    JButton metaBtn;

	private static final long serialVersionUID = 1679779479777549841L;

	public ImageGUI() {
        setLayout(null);
		title = new JLabel("Target image not set");
		imagePath = new PlaceholderTextField("Choose target image");
		metadataPath = new PlaceholderTextField("Choose metadata for target image");

		imageBtn = new JButton("Add image");
		metaBtn = new JButton("Add metadata");
		
		add(title);
		add(imagePath);
		add(imageBtn);
		add(metadataPath);
		add(metaBtn);
    }
	
	@Override
	public void doLayout() {
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
        y = labelH + imH;
        metadataPath.setBounds(inset, y, pathW, metaH - inset);
        metaBtn.setBounds(inset + pathW + inset, y, btnW, metaH - inset);
	}
}

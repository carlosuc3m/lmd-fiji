package org.proteovir.gui;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.TransferHandler;

public class CalibrationPointsGUI extends JPanel {

    private static final long serialVersionUID = -8405747451234902128L;
    
    private final int n;
    
    JLabel title;
    PlaceholderTextField imagePath;
    JButton imageBtn;
    PlaceholderTextField metadataPath;
    JButton metaBtn;
    


    private static final String SET_TEXT = "<html><span style=\\\"color: green;\\\">Calibration image %s set</span></html>";
    private static final String NOT_SET_TEXT = ""
    		+ "<html>"
            + "Calibration image %s "
            + "<span style=\"color: red;\">not set</span>"
            + "</html>";

	public CalibrationPointsGUI(int n) {
        setLayout(null);
		this.n = n;
		title = new JLabel(String.format(NOT_SET_TEXT, "" + n));
		imagePath = new PlaceholderTextField(String.format("Choose calibration image %s", "" + n));
		metadataPath = new PlaceholderTextField(String.format("Choose metadata for calibration image %s", "" + n));

		imageBtn = new JButton("Add image");
		metaBtn = new JButton("Add metadata");
		
		setBorder(BorderFactory.createLineBorder(Color.black, 1));
		
		add(title);
		add(imagePath);
		add(imageBtn);
		add(metadataPath);
		add(metaBtn);
		
		imageBtn.addActionListener(e -> {
		    JFileChooser chooser = new JFileChooser();
		    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		    int result = chooser.showOpenDialog(imageBtn.getParent());
		    if (result == JFileChooser.APPROVE_OPTION) {
		        File selected = chooser.getSelectedFile();
		        imagePath.setText(selected.getAbsolutePath());
		    }
		});
		
		metaBtn.addActionListener(e -> {
		    JFileChooser chooser = new JFileChooser();
		    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		    int result = chooser.showOpenDialog(metaBtn.getParent());
		    if (result == JFileChooser.APPROVE_OPTION) {
		        File selected = chooser.getSelectedFile();
		        metadataPath.setText(selected.getAbsolutePath());
		    }
		});
		
		imagePath.setTransferHandler(new TransferHandler() {
			private static final long serialVersionUID = -7894592726795662573L;

			@Override
		    public boolean canImport(TransferSupport support) {
		        return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
		    }

		    @Override
		    public boolean importData(TransferSupport support) {
		        if (!canImport(support)) return false;
		        try {
		            @SuppressWarnings("unchecked")
		            List<File> files = (List<File>) 
		                support.getTransferable()
		                       .getTransferData(DataFlavor.javaFileListFlavor);

		            // Reject if not exactly one
		            if (files.size() != 1) {
		                Toolkit.getDefaultToolkit().beep();
		                return false;
		            }

		            File f = files.get(0);
		            imagePath.setText(f.getAbsolutePath());
		            return true;
		        } catch (Exception ex) {
		            ex.printStackTrace();
		            return false;
		        }
		    }
		});
		
		metadataPath.setTransferHandler(new TransferHandler() {
		    private static final long serialVersionUID = -7894592726795662573L;

			@Override
		    public boolean canImport(TransferSupport support) {
		        return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
		    }

		    @Override
		    public boolean importData(TransferSupport support) {
		        if (!canImport(support)) return false;
		        try {
		            @SuppressWarnings("unchecked")
		            List<File> files = (List<File>) 
		                support.getTransferable()
		                       .getTransferData(DataFlavor.javaFileListFlavor);

		            // Reject if not exactly one
		            if (files.size() != 1) {
		                Toolkit.getDefaultToolkit().beep();
		                return false;
		            }

		            File f = files.get(0);
		            metadataPath.setText(f.getAbsolutePath());
		            return true;
		        } catch (Exception ex) {
		            ex.printStackTrace();
		            return false;
		        }
		    }
		});
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

	public void setTargetSet(boolean b) {
		if (b)
			title.setText(String.format(SET_TEXT, n));
		else
			title.setText(String.format(NOT_SET_TEXT, n));
		
	}
}

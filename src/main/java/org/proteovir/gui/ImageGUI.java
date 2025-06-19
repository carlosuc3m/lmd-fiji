package org.proteovir.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.TransferHandler;

public class ImageGUI extends JPanel {
	
	Consumer<File> openImageCallback;
    
    JLabel title;
    PlaceholderTextField imagePath;
    JButton imageBtn;
    PlaceholderTextField metadataPath;
    JButton metaBtn;

    private static final String SET_TEXT = "<html><span style=\\\"color: green;\\\">Target image set</span></html>";
    private static final String NOT_SET_TEXT = ""
    		+ "<html>"
            + "Target image "
            + "<span style=\"color: red;\">not set</span>"
            + "</html>";

	private static final long serialVersionUID = 1679779479777549841L;

	public ImageGUI() {
        setLayout(null);
		title = new JLabel(NOT_SET_TEXT);
		imagePath = new PlaceholderTextField("Choose target image");
		metadataPath = new PlaceholderTextField("Choose metadata for target image");

		imageBtn = new JButton("Add image");
		metaBtn = new JButton("Add metadata");
		
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
	            if (openImageCallback != null)
	            	openImageCallback.accept(selected);
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
			private static final long serialVersionUID = 8107226480642783470L;

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
		            if (openImageCallback != null)
		            	openImageCallback.accept(f);
		            return true;
		        } catch (Exception ex) {
		            ex.printStackTrace();
		            return false;
		        }
		    }
		});
		
		metadataPath.setTransferHandler(new TransferHandler() {
			private static final long serialVersionUID = -4114518312821048355L;

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

	public void setOpenImageCallback(Consumer<File> openIm) {
		openImageCallback = openIm;
	}

	public void setTargetSet(boolean b) {
		if (b)
			title.setText(SET_TEXT);
		else
			title.setText(NOT_SET_TEXT);
		
	}
}

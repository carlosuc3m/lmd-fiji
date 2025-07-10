package org.proteovir.gui;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.proteovir.gui.components.PlaceholderTextField;
import org.proteovir.metadata.ImageMetaParser;

import ij.IJ;

public class ImageGUI extends JPanel implements DocumentListener {
	
	private boolean good = false;
	private boolean isCalibrated = false;
	
	private boolean imSet = false;
	private ImageMetaParser meta;
    private String calImage;

    private Runnable changeImageCallback;
	private Function<File, Boolean> openImageCallback;
	private Runnable roiManagerCallback;
    
    JLabel title;
    PlaceholderTextField imagePath;
    JButton imageBtn;
    
    private static final String META_SUFFIX = "_Properties.xml";
    
    private static final String META_FOLDER = "MetaData";

    private static final String ADD_IM = "Add image";
    private static final String CHANGE_IM = "Change image";
    
    private static final String ALL_SET = ""
    		+ "<html>"
    		+ "<span style=\"color: green;\">Ready to annotate  and export</span>"
    		+ "</html>";

    protected static final String CALIBRATION_NOT_SET = ""
    		+ "<html>"
    		+ "<span style=\"color: #D8BFD8;\">Calibration missing</span>"
    		+ "</html>";
    private static final String NOT_SET_TEXT = ""
    		+ "<html>"
            + "<span style=\"color: white;\"> Target image </span>"
            + "<span style=\"color: red;\">not set</span>"
            + "</html>";
    private static final String META_NOT_SET_TEXT = ""
    		+ "<html>"
            + "<span style=\"color: white;\"> Target image </span>"
            + "<span style=\"color: red;\"> medatada not set</span>"
            + "</html>";

	private static final long serialVersionUID = 1679779479777549841L;

	public ImageGUI() {
        setLayout(null);
		setOpaque(true);
		setBackground(Color.black);
		title = new JLabel(NOT_SET_TEXT);
		title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setVerticalAlignment(SwingConstants.CENTER);
		imagePath = new PlaceholderTextField("Choose target image");
		imagePath.getDocument().addDocumentListener(this);
		
		imageBtn = new JButton("Add image");
		
		add(title);
		add(imagePath);
		add(imageBtn);
		
		imageBtn.addActionListener(e -> {
			if (new File(imagePath.getText()).isFile() && !imSet) {
				openImage(imagePath.getText()); 
		        return;
			}
		    JFileChooser chooser;
		    if (new File(imagePath.getText()).isDirectory())
		    	chooser = new JFileChooser(imagePath.getText());
	    	else
		    	chooser = new JFileChooser();
		    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		    int result = chooser.showOpenDialog(imageBtn.getParent());
		    if (result == JFileChooser.APPROVE_OPTION) {
		        File selected = chooser.getSelectedFile();
		        imagePath.setText(selected.getAbsolutePath());
		        openImage(selected.getAbsolutePath());
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
					openImage(f.getAbsolutePath());
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
        
        int labelH = Math.max(inset, (int) (rawH / 2));
        int imH = Math.max(inset, (int) (rawH / 2));
        
        int y = inset;
        title.setBounds(inset, y, rawW - 2 * inset, labelH - inset);
        y = labelH;
        int pathW = (int) ((rawW - inset * 3) * 0.7);
        int btnW = (int) ((rawW - inset * 3) * 0.3);
        imagePath.setBounds(inset, y, pathW, imH - inset);
        imageBtn.setBounds(inset + pathW + inset, y, btnW, imH - inset);
        y = labelH + imH;
	}

	public Polygon toAbsCoord(Polygon pol) {
		if (!imSet || meta == null)
			return new Polygon(new int[0], new int[0], 0);
		int offsetX = (int) Math.round(meta.getTilePosX() / meta.getPixelSizeX());
		int offsetY = (int) Math.round(meta.getTilePosY() / meta.getPixelSizeY());
		int[] x = pol.xpoints;
		int[] y = pol.ypoints;
		for (int i = 0; i < x.length; i ++) {
			x[i] = x[i] + offsetX;
			y[i] = y[i] + offsetY;
		}
		return new Polygon(x, y, x.length);
	}
	
	public boolean isDefined() {
		return good;
	}
	
	public void setChangeImageCallback(Runnable changeImageCallback) {
		this.changeImageCallback = changeImageCallback;
	}

	public void setOpenImageCallback(Function<File, Boolean> openIm) {
		openImageCallback = openIm;
	}

	public void setTargetSet(boolean b) {
		imSet = b;
	}
	
	public void setCalibrated(boolean isCalibrated) {
		this.isCalibrated = isCalibrated;
		setInfoState(false);
	}
	
	public void block(boolean block) {
		this.imageBtn.setEnabled(!block);
		this.imagePath.setEditable(!block);
		this.imagePath.setEnabled(!block);
	}
	
	private void setDefault(boolean modifyTextField) {
		title.setText(NOT_SET_TEXT);
		if (modifyTextField) imagePath.setText("");

		imageBtn.setText(ADD_IM);
	}
	
	private void setInfoState() {
		setInfoState(true);
	}
	
	protected void setInfoState(boolean modifyTextField) {
		good = false;
		if (imSet && meta == null) {
			title.setText(META_NOT_SET_TEXT);
			imageBtn.setText(CHANGE_IM);
		} else if (!imSet && meta != null) {
			title.setText(NOT_SET_TEXT);
			imageBtn.setText(ADD_IM);
			if (modifyTextField) imagePath.setText("");
		} else if (imSet && meta != null && !isCalibrated) {
			title.setText(CALIBRATION_NOT_SET);
			imageBtn.setText(CHANGE_IM);
			good = true;
		} else if (imSet && meta != null) {
			title.setText(ALL_SET);
			imageBtn.setText(CHANGE_IM);
			good = true;
		} else {
			setDefault(modifyTextField);
		}
		this.roiManagerCallback.run();
	}
	
	private void searchMeta(String imgFile) {
		meta = null;
		File metaFolder = new File(new File(imgFile).getParent() + File.separator + META_FOLDER);
		if (!metaFolder.isDirectory()
				|| metaFolder.listFiles() == null
				|| metaFolder.listFiles().length == 0) {
        	IJ.error("Unable to find corresponding metadata");
			return;
		}
		File metaFile = Arrays.stream(metaFolder.listFiles())
				.filter(ff -> ff.getName().endsWith(META_SUFFIX))
				.findFirst().orElse(null);
		if (metaFile == null) {
        	IJ.error("Unable to find corresponding metadata");
			return;
		}
		openMeta(metaFile.getAbsolutePath());
	}
	
	private synchronized void openMeta(String strFile) {
		meta = null;
		File file = new File(strFile);
		if (!file.isFile() || !file.getAbsolutePath().endsWith(".xml")) {
        	IJ.error("File did not correspond to a valid image.");
            return;
		}
		try {
			meta = new ImageMetaParser(file.getAbsolutePath(), "µm");
		} catch (Exception e) {
			e.printStackTrace();
			IJ.error("Unable to read metadata of selected image.");
			
		}
	}
	
	private synchronized void openImage(String strFile) {
		try {
			imSet = false;
			calImage = null;
            if (openImageCallback == null) {
        		setInfoState();
        		return;
            }
            imSet = openImageCallback.apply(new File(strFile));
            if (imSet)
            	calImage = strFile;
            searchMeta(strFile);
    		setInfoState();
    		if (!imSet)
            	imagePath.setTempPlaceholder("Choose a valid image");
	    } catch (Exception ex) {
	    	ex.printStackTrace();
        	IJ.error("An error occurred");
    		setInfoState();
        	imagePath.setTempPlaceholder("Choose a valid image");
	    }
	}
    
    private synchronized void onChange(DocumentEvent e) {
        SwingUtilities.invokeLater(() -> {
        	try {
				String str = e.getDocument().getText(0, e.getDocument().getLength());
	        	if (e.getDocument().equals(imagePath.getDocument()) && imSet
	        			&& (calImage == null || !calImage.equals(str))) {
	        		imSet = false;
	        		changeImageCallback.run();
	        		setInfoState(false);
	        	}
			} catch (BadLocationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        });
    }

	@Override
	public void insertUpdate(DocumentEvent e) {
		onChange(e);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		onChange(e);
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		onChange(e);
	}

	public void notifyRoiManager(Runnable roiManagerCallback) {
		this.roiManagerCallback = roiManagerCallback;
	}
}

package org.proteovir.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.List;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.proteovir.gui.components.PlaceholderTextField;
import org.proteovir.metadata.ImageMetaParser;

import ij.IJ;
import ij.gui.Toolbar;
import io.bioimage.modelrunner.gui.YesNoDialog;

public class ImageGUI extends JPanel implements DocumentListener {
	
	private boolean good = false;
	
	private boolean imSet = false;
	private ImageMetaParser meta;
    private String calImage;
    private String calMeta;

    private Runnable infoCallback;
	Function<File, Boolean> openImageCallback;
    
    JLabel title;
    PlaceholderTextField imagePath;
    JButton imageBtn;
    PlaceholderTextField metadataPath;
    JButton metaBtn;

    private static final String ADD_IM = "Add image";
    private static final String ADD_META = "Add metadata";
    private static final String CHANGE_IM = "Change image";
    private static final String CHANGE_META = "Change metadata";
    
    private static final String ALL_SET = ""
    		+ "<html>"
    		+ "<span style=\\\"color: green;\\\">Ready to segment and export</span>"
    		+ "</html>";

    private static final String CALIBRATION_NOT_SET = ""
    		+ "<html>"
    		+ "<span style=\\\"color: orange;\\\">Calibration missing</span>"
    		+ "</html>";
    private static final String NOT_SET_TEXT = ""
    		+ "<html>"
            + "Target image "
            + "<span style=\"color: red;\">not set</span>"
            + "</html>";
    private static final String META_NOT_SET_TEXT = ""
    		+ "<html>"
            + "Target image "
            + "<span style=\"color: red;\"> medatada not set</span>"
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
			if (new File(imagePath.getText()).isFile() && !imSet) {
	            if (openImageCallback != null)
	            	openImageCallback.apply(new File(imagePath.getText()));
	            if (imSet) calImage = imagePath.getText();
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
	            if (openImageCallback != null)
	            	openImageCallback.apply(selected);
		    }
		});
		
		metaBtn.addActionListener(e -> {
			if (new File(metadataPath.getText()).isFile() && meta == null) {
				openMeta(metadataPath.getText());
		        return;
			}
		    JFileChooser chooser;
		    if (new File(metadataPath.getText()).isDirectory())
		    	chooser = new JFileChooser(metadataPath.getText());
	    	else
		    	chooser = new JFileChooser();
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
		            if (openImageCallback == null)
		            	throw new RuntimeException("There is no way to open the selected image");
		            boolean isImage = openImageCallback.apply(f);
		            if (isImage)
		            	imagePath.setText(f.getAbsolutePath());
		            else 
		            	imagePath.setTempPlaceholder("Choose a valid image");
		            return isImage;
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
			        openMeta(f.getAbsolutePath());
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
	
	public boolean isDefined() {
		return good;
	}

	public void setOpenImageCallback(Function<File, Boolean> openIm) {
		openImageCallback = openIm;
	}
	
	public void setInfoCallback(Runnable infoCallback) {
		this.infoCallback = infoCallback;
	}

	public void setTargetSet(boolean b) {
		if (b) {
			title.setText(ALL_SET);
			imSet = true;
		} else {
			title.setText(NOT_SET_TEXT);
			imSet = false;
		}
	}
	
	public void block(boolean block) {
		this.imageBtn.setEnabled(!block);
		this.metaBtn.setEnabled(!block);
		this.imagePath.setEditable(!block);
		this.imagePath.setEnabled(!block);
		this.metadataPath.setEditable(!block);
		this.metadataPath.setEnabled(!block);
	}
	
	private void setDefault(boolean modifyTextField) {
		title.setText(NOT_SET_TEXT);
		if (modifyTextField) imagePath.setText("");
		if (modifyTextField) metadataPath.setText("");

		imageBtn.setText(ADD_IM);
		metaBtn.setText(ADD_META);
	}
	
	private void setInfoState() {
		setInfoState(true);
	}
	
	private void setInfoState(boolean modifyTextField) {
		good = false;
		if (imSet && meta == null) {
			title.setText(META_NOT_SET_TEXT);
			imageBtn.setText(CHANGE_IM);
			metaBtn.setText(ADD_META);
			if (modifyTextField) metadataPath.setText("");
		} else if (!imSet && meta != null) {
			title.setText(NOT_SET_TEXT);
			imageBtn.setText(ADD_IM);
			metaBtn.setText(CHANGE_META);
			if (modifyTextField) imagePath.setText("");
		} else if (imSet && meta != null) {
			title.setText(ALL_SET);
			imageBtn.setText(CHANGE_IM);
			metaBtn.setText(CHANGE_META);
			good = true;
		} else {
			setDefault(modifyTextField);
		}
		this.infoCallback.run();
	}
	
	private synchronized void openMeta(String strFile) {
		meta = null;
		calMeta = null;
		File file = new File(strFile);
		if (!file.isFile() || !file.getAbsolutePath().endsWith(".xml")) {
        	IJ.error("File did not correspond to a valid image.");
        	setInfoState();
        	metadataPath.setTempPlaceholder("Select a valid .xml file");
            return;
		}
		try {
			meta = new ImageMetaParser(file.getAbsolutePath(), "µm");
			calMeta = strFile;
        	setInfoState();
		} catch (Exception e) {
			e.printStackTrace();
			IJ.error("Please select a valid properties.xml file.");
        	setInfoState();
        	metadataPath.setTempPlaceholder("Select a valid .xml file");
			
		}
	}
	
	private synchronized void openImage(String strFile) {
		try {
			imSet = null;
			calImage = null;
			File file = new File(strFile);
	        imp = IJ.openImage(file.getAbsolutePath());
	        if (imp == null) {
	        	IJ.error("File did not correspond to a valid image.");
	        	setInfoState();
	        	imagePath.setTempPlaceholder("Select a valid image");
	            return;
	        }
	        imp.show();
	        boolean agreed = YesNoDialog.askQuestion("Select calibration point", 
	        		String.format("Click on the place of calibration point %s", n));
	        if (!agreed) {
	        	imp.getWindow().dispose();
	        	imp.close();
	        } else {
	        	calImage = strFile;
		        blockOthers(imp.getWindow());
		        IJ.setTool(Toolbar.POINT);
		        imp.getCanvas().addMouseListener(this);
	        }
	    } catch (Exception ex) {
	    	ex.printStackTrace();
        	IJ.error("An error occurred");;
    		setInfoState();
	    }
	}
    
    private synchronized void onChange(DocumentEvent e) {
        SwingUtilities.invokeLater(() -> {
        	try {
				String str = e.getDocument().getText(0, e.getDocument().getLength());
	        	if (e.getDocument().equals(metadataPath.getDocument()) && meta != null
	        			&& (calMeta == null || !calMeta.equals(str))) {
	        		meta = null;
	        		setInfoState(false);
	        	} else if (e.getDocument().equals(imagePath.getDocument()) && imSet
	        			&& (calImage == null || !calImage.equals(str))) {
	        		imSet = false;
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
}

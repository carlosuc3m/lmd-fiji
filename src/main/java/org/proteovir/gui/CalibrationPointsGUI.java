package org.proteovir.gui;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
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
import ij.ImageListener;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.Toolbar;
import io.bioimage.modelrunner.gui.YesNoDialog;

public class CalibrationPointsGUI extends JPanel implements MouseListener, DocumentListener {

    private static final long serialVersionUID = -8405747451234902128L;
    
    private final int n;

    private ImagePlus imp;
    private ImageMetaParser meta;
    private int[] calibrationPoint;
    private String calImage;
    
    private boolean good = false;
    
    private Runnable callback;
    
    
    JLabel title;
    PlaceholderTextField imagePath;
    JButton imageBtn;
    
    
    private static final String META_SUFFIX = "_Properties.xml";
    
    private static final String META_FOLDER = "MetaData";

    private static final String NOT_SET_TEXT = ""
    		+ "<html>"
            + "Calibration image and metadata %s "
            + "<span style=\"color: red;\">not set</span>"
            + "</html>";

	private static final String IM_SET = ""
    		+ "<html>"
            + "Image %s metadata "
            + "<span style=\"color: orange;\">not set</span>"
            + "</html>";

	private static final String META_SET = ""
    		+ "<html>"
            + "Calibration image %s "
            + "<span style=\"color: orange;\">not set</span>"
            + "</html>";

	private static final String ALL_SET = ""
    		+ "<html>"
            + "<span style=\"color: green;\">Everything set</span>"
            + "</html>";
    
    private static final String NOT_SET_IM = "Choose calibration image %s";
    private static final String ADD_IM = "Add image";
    private static final String CHANGE_IM = "Change image";

	public CalibrationPointsGUI(int n) {
        setLayout(null);
		this.n = n;
		title = new JLabel(String.format(NOT_SET_TEXT, "" + n));
		imagePath = new PlaceholderTextField(String.format(NOT_SET_IM, "" + n));
		imagePath.getDocument().addDocumentListener(this);

		imageBtn = new JButton(ADD_IM);
		
		setBorder(BorderFactory.createLineBorder(Color.black, 1));
		
		add(title);
		add(imagePath);
		add(imageBtn);
		
		ImagePlus.addImageListener(new ImageListener() {

			@Override
			public void imageClosed(ImagePlus imp) {
				if (CalibrationPointsGUI.this.imp != null && imp.equals(CalibrationPointsGUI.this.imp)) {
					unblockAll();
					CalibrationPointsGUI.this.imp = null;
					setInfoState();
				}
			}

			@Override
			public void imageUpdated(ImagePlus imp) {}
			@Override
			public void imageOpened(ImagePlus imp) {}
        	
        });
		
		imageBtn.addActionListener(e -> {
			if (new File(imagePath.getText()).isFile() && calibrationPoint == null) {
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

	public Point getAbsCalPoint() {
		if (calibrationPoint == null || meta == null)
			return new Point(0, 0);
		int x = (int) (Math.round(meta.getTilePosX() / meta.getPixelSizeX()) + calibrationPoint[0]);
		int y = (int) (Math.round(meta.getTilePosY() / meta.getPixelSizeY()) + calibrationPoint[1]);
		return new Point(x, y);
	}
	
	public boolean isCalibrated() {
		return good;
	}
	
	public void setCallback(Runnable callback) {
		this.callback = callback;
	}
	
	public void block(boolean block) {
		this.imageBtn.setEnabled(!block);
		this.imagePath.setEditable(!block);
		this.imagePath.setEnabled(!block);
	}
	
	private void setDefault(boolean modifyTextField) {
		title.setText(String.format(NOT_SET_TEXT, "" + n));
		if (modifyTextField) imagePath.setText("");

		imageBtn.setText(ADD_IM);
	}
	
	private void setInfoState() {
		setInfoState(true);
	}
	
	private void setInfoState(boolean modifyTextField) {
		good = false;
		if (calibrationPoint != null && meta == null) {
			title.setText(String.format(IM_SET, "" + n));
			imageBtn.setText(CHANGE_IM);
		} else if (calibrationPoint == null && meta != null) {
			title.setText(String.format(META_SET, "" + n));
			imageBtn.setText(ADD_IM);
			if (modifyTextField) imagePath.setText("");
		} else if (calibrationPoint != null && meta != null) {
			title.setText(String.format(ALL_SET, "" + n));
			imageBtn.setText(CHANGE_IM);
			good = true;
		} else {
			setDefault(modifyTextField);
		}
		callback.run();
	}
	
	private void searchMeta(String imgFile) {
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
			calibrationPoint = null;
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
	
	public static void blockOthers(Frame blocker) {
        for (Window w : Window.getWindows()) {
            if (w instanceof Frame && w != blocker) {
                w.setEnabled(false);
            }
        }
    }
    public static void unblockAll() {
        for (Window w : Window.getWindows()) {
            w.setEnabled(true);
        }
    }
    
    private synchronized void onChange(DocumentEvent e) {
        SwingUtilities.invokeLater(() -> {
        	try {
				String str = e.getDocument().getText(0, e.getDocument().getLength());
	        	if (e.getDocument().equals(imagePath.getDocument()) && calibrationPoint != null
	        			&& (calImage == null || !calImage.equals(str))) {
	        		calibrationPoint = null;
	        		setInfoState(false);
	        	}
			} catch (BadLocationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        });
    }

	@Override
	public void mouseClicked(MouseEvent e) {
		Roi roi = imp.getRoi();
    	if (roi != null) {
	    	Iterator<java.awt.Point> iterator = roi.iterator();
			java.awt.Point p = iterator.next();
			calibrationPoint = new int[] {(int) p.getX(), (int) p.getY()};
	        searchMeta(imagePath.getText());
    	}
    	imp.getWindow().dispose();
    	imp.close();
		setInfoState();
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

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}
}

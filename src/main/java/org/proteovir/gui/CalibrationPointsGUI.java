package org.proteovir.gui;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.TransferHandler;

import org.proteovir.gui.components.PlaceholderTextField;
import org.proteovir.metadata.ImageMetaParser;

import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.Toolbar;
import io.bioimage.modelrunner.gui.YesNoDialog;

public class CalibrationPointsGUI extends JPanel implements MouseListener{

    private static final long serialVersionUID = -8405747451234902128L;
    
    private final int n;
    
    private int[] calibrationPoint;
    
    private ImagePlus imp;
    private ImageMetaParser meta;
    
    JLabel title;
    PlaceholderTextField imagePath;
    JButton imageBtn;
    PlaceholderTextField metadataPath;
    JButton metaBtn;
    


    private static final String SET_TEXT = "<html><span style=\\\"color: green;\\\">Calibration image %s set</span></html>";
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
    private static final String NOT_SET_META = "Choose metadata for calibration image %s";
    private static final String ADD_IM = "Add image";
    private static final String ADD_META = "Add metadata";
    private static final String CHANGE_IM = "Change image";
    private static final String CHANGE_META = "Change metadata";

	public CalibrationPointsGUI(int n) {
        setLayout(null);
		this.n = n;
		title = new JLabel(String.format(NOT_SET_TEXT, "" + n));
		imagePath = new PlaceholderTextField(String.format(NOT_SET_IM, "" + n));
		metadataPath = new PlaceholderTextField(String.format(NOT_SET_META, "" + n));

		imageBtn = new JButton(ADD_IM);
		metaBtn = new JButton(ADD_META);
		
		setBorder(BorderFactory.createLineBorder(Color.black, 1));
		
		add(title);
		add(imagePath);
		add(imageBtn);
		add(metadataPath);
		add(metaBtn);
		
		ImagePlus.addImageListener(new ImageListener() {

			@Override
			public void imageClosed(ImagePlus imp) {
				if (CalibrationPointsGUI.this.imp != null && imp.equals(CalibrationPointsGUI.this.imp)) {
					unblockAll();
					CalibrationPointsGUI.this.imp = null;
					if (calibrationPoint == null)
						setDefault();
				}
			}

			@Override
			public void imageUpdated(ImagePlus imp) {}
			@Override
			public void imageOpened(ImagePlus imp) {}
        	
        });
		
		imageBtn.addActionListener(e -> {
			if (new File(imagePath.getText()).isFile()) {
		        openImage(new File(imagePath.getText()));
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
		        openImage(selected);
		    }
		});
		
		metaBtn.addActionListener(e -> {
			if (new File(metadataPath.getText()).isFile()) {
				openMeta(new File(metadataPath.getText()));
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
		        openMeta(selected);
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
			        openImage(f);
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
			        openMeta(f);
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
	
	public void block(boolean block) {
		this.imageBtn.setEnabled(!block);
		this.metaBtn.setEnabled(!block);
		this.imagePath.setEditable(!block);
		this.imagePath.setEnabled(!block);
		this.metadataPath.setEditable(!block);
		this.metadataPath.setEnabled(!block);
	}
	
	private void setDefault() {
		title.setText(String.format(NOT_SET_TEXT, "" + n));
		imagePath.setText("");
		metadataPath.setText("");

		imageBtn = new JButton(ADD_IM);
		metaBtn = new JButton(ADD_META);
	}
	
	private void setInfoState() {
		if (calibrationPoint != null && true) {
			title.setText(String.format(IM_SET, "" + n));
			imageBtn.setText(CHANGE_IM);
			metaBtn.setText(ADD_META);
		} else if (calibrationPoint == null && meta != null) {
			title.setText(String.format(META_SET, "" + n));
			imageBtn.setText(ADD_IM);
			metaBtn.setText(CHANGE_META);
		} else if (calibrationPoint != null && meta != null) {
			title.setText(String.format(ALL_SET, "" + n));
			imageBtn.setText(CHANGE_IM);
			metaBtn.setText(CHANGE_META);
		} else {
			setDefault();
		}
	}
	
	private void openMeta(File file) {
		meta = null;
		if (!file.isFile() || !file.getAbsolutePath().endsWith(".xml")) {
        	IJ.error("File did not correspond to a valid image.");
        	metadataPath.setTempPlaceholder("Select a valid .xml file");
        	setInfoState();
            return;
		}
		try {
			meta = new ImageMetaParser(file.getAbsolutePath(), "µm");
		} catch (Exception e) {
			e.printStackTrace();
			IJ.error("Please select a valid properties.xml file.");
			
		}
	}
	
	private void openImage(File file) {
		try {
			calibrationPoint = null;
	        imp = IJ.openImage(file.getAbsolutePath());
	        if (imp == null) {
	        	IJ.error("File did not correspond to a valid image.");
	        	imagePath.setTempPlaceholder("Select a valid image");
	        	setInfoState();
	            return;
	        }
	        imp.show();
	        boolean agreed = YesNoDialog.askQuestion("Select calibration point", 
	        		String.format("Click on the place of calibration point %s", n));
	        if (!agreed) {
	        	imp.getWindow().dispose();
	        	imp.close();
	        } else {
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

	@Override
	public void mouseClicked(MouseEvent e) {
		Roi roi = imp.getRoi();
    	if (roi != null) {
	    	Iterator<java.awt.Point> iterator = roi.iterator();
			java.awt.Point p = iterator.next();
			calibrationPoint = new int[] {(int) p.getX(), (int) p.getY()};
    	}
    	imp.getWindow().dispose();
    	imp.close();
		setInfoState();
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}

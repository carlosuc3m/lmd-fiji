package org.proteovir.gui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.macro.Interpreter;
import ij.macro.MacroRunner;
import ij.plugin.frame.Recorder;


public class RoiManagerGUI extends JPanel implements MouseWheelListener, ListSelectionListener, MouseListener, ActionListener, ItemListener {

    private static final long serialVersionUID = -8405747451234902128L;
    
    private DefaultListModel<String> listModel;
	private JList<String> list;
	private JPanel panel;
	private JCheckBox showAllCheckbox = new JCheckBox("Show All", false);
	private JCheckBox labelsCheckbox = new JCheckBox("Labels", false);

	private static final int BUTTONS = 11;
	
	
	public RoiManagerGUI() {
		list = new JList<String>();
		listModel = new DefaultListModel<String>();
		list.setModel(listModel);
		setLayout(new BorderLayout());
		listModel = new DefaultListModel<String>();
		list.setModel(listModel);
		list.setPrototypeCellValue("0000-0000-0000 ");
		list.addListSelectionListener(this);
		list.addMouseListener(this);
		list.addMouseWheelListener(this);
		list.setBackground(Color.white);
		list.setBackground(Color.white);
		JScrollPane scrollPane = new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add("Center", scrollPane);
		panel = new JPanel();
		int nButtons = BUTTONS;
		panel.setLayout(new GridLayout(nButtons, 1, 5, 0));
		addButton("Add");
		addButton("Delete");
		addButton("Simplify");
		addButton("Complicate");
		addButton("Dilate");
		addButton("Erode");
		addButton("Merge");
		addButton("Export mask");
		addButton("Export LMD");
		showAllCheckbox.addItemListener(this);
		panel.add(showAllCheckbox);
		labelsCheckbox.addItemListener(this);
		panel.add(labelsCheckbox);
		add("East", panel);
		list.remove(0);
    }

	void addButton(String label) {
		JButton b = new JButton(label);
		b.addActionListener(this);
		// TODO remove b.addKeyListener(IJ.getInstance());
		b.addMouseListener(this);
		panel.add(b);
	}

	boolean addRoi(Roi roi, boolean promptForName, Color color, int lineWidth) {
		if (listModel==null)
			IJ.log("<<Error: Uninitialized RoiManager>>");
		ImagePlus imp = roi==null?getImage():WindowManager.getCurrentImage();
		if (roi==null) {
			if (imp==null)
				return false;
			roi = imp.getRoi();
			if (roi==null) {
				error("The active image does not have a selection.");
				return false;
			}
		}
		if ((roi instanceof PolygonRoi) && ((PolygonRoi)roi).getNCoordinates()==0)
			return false;
		if (color==null && roi.getStrokeColor()!=null)
			color = roi.getStrokeColor();
		else if (color==null && defaultColor!=null)
			color = defaultColor;
		boolean ignorePosition = false;
		if (lineWidth==IGNORE_POSITION) {
			ignorePosition = true;
			lineWidth = -1;
		}
		if (lineWidth<0) {
			int sw = (int)roi.getStrokeWidth();
			lineWidth = sw>1?sw:defaultLineWidth;
		}
		if (lineWidth>100) lineWidth = 1;
		int n = getCount();
		int position = imp!=null&&!ignorePosition?roi.getPosition():0;
		int saveCurrentSlice = imp!=null?imp.getCurrentSlice():0;
		if (position>0 && position!=saveCurrentSlice) {
			if (imp.lock())
				imp.setSliceWithoutUpdate(position);
			else
				return false;	//can't lock image, must not change the stack slice
		} else
			position = 0;		//we need to revert to the original stack slice and unlock if position>0
		if (n>0 && !IJ.isMacro() && imp!=null && !allowDuplicates) {
			// check for duplicate
			Roi roi2 = (Roi)rois.get(n-1);
			if (roi2!=null) {
				String label = (String)listModel.getElementAt(n-1);
				int slice2 = getSliceNumber(roi2, label);
				if (roi.equals(roi2) && (slice2==-1||slice2==imp.getCurrentSlice()) && imp.getID()==prevID && !Interpreter.isBatchMode()) {
					if (position>0) {
						imp.setSliceWithoutUpdate(saveCurrentSlice);
						imp.unlock();
					}
					return false;
				}
			}
		}
		allowDuplicates = false;
		prevID = imp!=null?imp.getID():0;
		String name = roi.getName();
		if (isStandardName(name))
			name = null;
		String label = name!=null?name:getLabel(imp, roi, -1);
		if (promptForName)
			label = promptForName(label);
		if (label==null) {
			if (position>0) {
				imp.setSliceWithoutUpdate(saveCurrentSlice);
				imp.unlock();
			}
			return false;
		}
		listModel.addElement(label);
		roi.setName(label);
		Roi roiCopy = (Roi)roi.clone();
		if (ignorePosition && imp!=null && imp.getStackSize()>1 && imp.getWindow()!=null && isVisible()) {
 			// set ROI position to current stack position if image and RoiManager are visible
			roiCopy.setPosition(imp);
		}
		if (lineWidth>1)
			roiCopy.setStrokeWidth(lineWidth);
		if (color!=null)
			roiCopy.setStrokeColor(color);
		rois.add(roiCopy);
		updateShowAll();
		if (record())
			recordAdd(defaultColor, defaultLineWidth);
		if (position>0) {
			imp.setSliceWithoutUpdate(saveCurrentSlice);
			imp.unlock();
		}
		return true;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String label = e.getActionCommand();
		if (label==null)
			return;
		String command = label;
		if (command.equals("Add"))
			runCommand("add");
		else if (command.equals("Simplify"))
			update(true);
		else if (command.equals("Delete"))
			delete(false);
		else if (command.equals("Complicate"))
			rename(null);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getSource();
		boolean showAllMode = showAllCheckbox.getState();
		if (source==showAllCheckbox) {
			if (firstTime && okToSet())
				labelsCheckbox.setState(true);
			showAll(showAllCheckbox.getState()?SHOW_ALL:SHOW_NONE);
			recordShowAll = true;
			firstTime = false;
			return;
		}
		if (source==labelsCheckbox) {
			if (firstTime && okToSet())
				showAllCheckbox.setState(true);
			boolean editState = labelsCheckbox.getState();
			boolean showAllState = showAllCheckbox.getState();
			if (!showAllState && !editState)
				showAll(SHOW_NONE);
			else {
				showAll(editState?LABELS:NO_LABELS);
				if (editState && !showAllState && okToSet()) {
					showAllCheckbox.setState(true);
					recordShowAll = false;
				}
			}
			firstTime = false;
			return;
		}
	}
}

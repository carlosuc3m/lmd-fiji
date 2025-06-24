package org.proteovir.roimanager;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ItemListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionListener;


public abstract class RoiManagerGUI extends JPanel implements ListSelectionListener, MouseListener, MouseWheelListener, ItemListener {

    private static final long serialVersionUID = -8405747451234902128L;
    
    protected DefaultListModel<String> listModel;
    protected JList<String> list;
    protected JPanel panel;
    protected JCheckBox showAllCheckbox = new JCheckBox("Show All", true);
    protected JCheckBox labelsCheckbox = new JCheckBox("Labels", true);
    
    protected List<JButton> btns = new ArrayList<JButton>();

	private static final int BUTTONS = 11;
	
	
	public RoiManagerGUI() {
		list = new JList<String>();
		listModel = new DefaultListModel<String>();
		list.setModel(listModel);
		setLayout(new BorderLayout());
		listModel = new DefaultListModel<String>();
		list.setModel(listModel);
		list.setPrototypeCellValue("0000-0000-0000 ");
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
		panel.add(showAllCheckbox);
		panel.add(labelsCheckbox);
		add("East", panel);
		list.remove(0);
		

		list.addListSelectionListener(this);
		list.addMouseListener(this);
		list.addMouseWheelListener(this);
		labelsCheckbox.addItemListener(this);
		showAllCheckbox.addItemListener(this);
    }
	
	public void block(boolean block) {
		for (JButton b : btns)
			b.setEnabled(!block);
		list.setEnabled(!block);
		showAllCheckbox.setEnabled(!block);
		labelsCheckbox.setEnabled(!block);
		
	}
	
	public JList<String> getList() {
		return this.list;
	}

	protected abstract void addButton(String label);
}

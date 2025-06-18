package org.proteovir.gui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		
	}
}

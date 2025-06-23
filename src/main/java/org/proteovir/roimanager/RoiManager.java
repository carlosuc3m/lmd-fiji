package org.proteovir.roimanager;


import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.proteovir.roimanager.utils.DouglasPeucker;

import ai.nets.samj.annotation.Mask;


public class RoiManager extends RoiManagerGUI implements MouseWheelListener, ListSelectionListener, MouseListener, ActionListener, ItemListener {

    private static final long serialVersionUID = -8405747451234902128L;
    
    private RoiManagerConsumer consumer;
    
    private List<Mask> rois = new ArrayList<Mask>();
    
	
	public RoiManager(RoiManagerConsumer consumer) {
		this.consumer = consumer;
    }

	protected void addButton(String label) {
		JButton b = new JButton(label);
		btns.add(b);
		b.addActionListener(this);
		// TODO remove b.addKeyListener(IJ.getInstance());
		b.addMouseListener(this);
		panel.add(b);
	}
	
	/**
	 * Returns the ROIs as an array.
	 * 
	 * @return the ROIs as an array
	 */
	public synchronized Mask[] getRoisAsArray() {
		Mask[] array = new Mask[rois.size()];
		return (Mask[])rois.toArray(array);
	}

	/** Returns the ROI count. */
	public int getROIsNumber() {
		return listModel!=null?listModel.getSize():0;
	}

	public void addRoi(Mask roi) {
		listModel.addElement(roi.getName());
		rois.add(roi);
		updateShowAll();
	}

	public void deleteAll() {
		int n = getROIsNumber();
		if (n == 0)
			return;
		int index[] = list.getSelectedIndices();
		if (index.length==0 || n == index.length) {
			rois.clear();
			listModel.removeAllElements();
		} else {
			for (int i = index.length - 1; i >= 0; i ++) {
				rois.remove(i);
				listModel.remove(i);
			}
		}
		updateShowAll();
	}

	/** Deletes the ROI at 'index' and updates the display. */
	public void delete(int index) {
		int count = this.getROIsNumber();
		if (count==0 || index>=count)
			return;
		rois.remove(index);
		listModel.remove(index);
		updateShowAll();
		repaint();
	}

	private void updateShowAll() {
		if (showAllCheckbox.isSelected() && getROIsNumber()>0)
			consumer.setRois(rois);
		else
			consumer.deleteAllRois();
	}
	
	private void addRoiFromGUI() {
		
	}
	
	private void simplify() {
		Mask mask = rois.get(list.getSelectedIndex());
		mask.simplify();
		consumer.setSelected(mask);
	}
	
	private void complicate() {
		Mask mask = rois.get(list.getSelectedIndex());
		mask.complicate();
		consumer.setSelected(mask);
	}
	
	private void merge() {
		
	}
	
	private void dilate() {
		
	}
	
	private void erode() {
		
	}
	
	private void deleteSelected() {
		
	}
	
	private void exportMask() {
		
	}

	private void exportLMD() {
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String label = e.getActionCommand();
		if (label==null)
			return;
		String command = label;
		if (command.equals("Add"))
			addRoiFromGUI();
		else if (command.equals("Simplify"))
			simplify();
		else if (command.equals("Delete"))
			deleteSelected();
		else if (command.equals("Complicate"))
			complicate();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getComponent().equals(this.list)) {
			int ind = this.list.getSelectedIndex();
			consumer.setSelected(this.rois.get(ind));
		}
		
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
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getSource();
		if (source == showAllCheckbox && showAllCheckbox.isSelected()) {
			consumer.setRois(rois);
		} else if (source == labelsCheckbox) {
			consumer.deleteAllRois();
		} else if (source == labelsCheckbox && this.labelsCheckbox.isSelected()) {
		} else if (source == labelsCheckbox) {
		}
	}
}

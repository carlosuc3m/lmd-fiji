package org.proteovir.roimanager;


import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.proteovir.roimanager.commands.Command;
import org.proteovir.roimanager.commands.ModifyRoiCommand;
import org.proteovir.roimanager.utils.PolygonUtils;

import ai.nets.samj.annotation.Mask;


public class RoiManager extends RoiManagerGUI implements MouseWheelListener, ListSelectionListener, MouseListener, ActionListener, ItemListener {

    private static final long serialVersionUID = -8405747451234902128L;
    
    private Object image;
    
    private RoiManagerConsumer consumer;

	private Consumer<List<Mask>> exportLMDCallback;
	
	private Consumer<Command> commandCallback;
    
    private List<Mask> rois = new ArrayList<Mask>();
    
    private boolean justClickedDelete = false;
    
	
	public RoiManager(RoiManagerConsumer consumer) {
		this.consumer = consumer;
		BiConsumer<Integer,Polygon> mod = (ii, pol) -> {
		    int n = rois.size();
		    if (ii < 0 || ii >= n)
		        return;
		    rois.get(ii).clear();
		    rois.get(ii).setContour(pol);
		    consumer.setRois(rois, ii);
		};
		consumer.setModifyRoiCallback(mod);
    }

	protected void addButton(String label) {
		JButton b = new JButton(label);
		btns.add(b);
		b.addActionListener(this);
		// TODO remove b.addKeyListener(IJ.getInstance());
		b.addMouseListener(this);
		panel.add(b);
	}
	
	public void setImage(Object image) {
		consumer.setImage(image);
		this.image = image;
	}
	
	public void addCommandCallback(Consumer<Command> commandCallback) {
		this.commandCallback = commandCallback;
	}
	
	public void setExportLMDcallback(Consumer<List<Mask>> exportLMDCallback) {
		this.exportLMDCallback = exportLMDCallback;
	}
	
	public Object getCurrentImage() {
		return this.image;
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
		rois.clear();
		listModel.removeAllElements();
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
	}

	/** Deletes the ROI at 'index' and updates the display. */
	public void delete(int[] indeces) {
		int count = this.getROIsNumber();
		for (int i = indeces.length - 1; i >= 0; i --) {
			int index = indeces[i];
			if (count == 0 || index >= count)
				continue;
			rois.remove(index);
			listModel.remove(index);
		}
		updateShowAll();
	}

	public void updateShowAll() {
		if (showAllCheckbox.isSelected())
			consumer.setRois(rois);
		else
			consumer.deleteAllRois();
	}
	
	private void addRoiFromGUI() {
		
	}
	
	private void simplify() {
		if (list.getSelectedIndex() == -1)
			return;
		int[] indices = list.getSelectedIndices();
		ModifyRoiCommand command = new ModifyRoiCommand(this, rois);
		for (int ind : indices) {
			Mask mask = rois.get(ind);
			command.setOldContour(mask.getUUID(), mask.getContour());
			mask.simplify();
			command.setNewContour(mask.getUUID(), mask.getContour());
			consumer.setSelected(mask);
			consumer.setRois(rois, ind);
		}
		this.commandCallback.accept(command);
	}
	
	private void complicate() {
		if (list.getSelectedIndex() == -1)
			return;
		int[] indices = list.getSelectedIndices();
		ModifyRoiCommand command = new ModifyRoiCommand(this, rois);
		for (int ind : indices) {
			Mask mask = rois.get(ind);
			command.setOldContour(mask.getUUID(), mask.getContour());
			mask.complicate();
			command.setNewContour(mask.getUUID(), mask.getContour());
			consumer.setRois(rois, ind);
		}
		this.commandCallback.accept(command);
	}
	
	private void merge() {
		if (list.getSelectedIndex() == -1 || list.getSelectedIndices().length <= 1)
			return;
		for (int i = list.getSelectedIndices().length - 1; i >= 0; i --) {
			int ii = list.getSelectedIndices()[i];
			for (int j = i - 1; j >= 0; j --) {
				int jj = list.getSelectedIndices()[j];
				if (!PolygonUtils.overlaps(rois.get(ii).getContour(), rois.get(jj).getContour()))
					continue;
				Mask roi = rois.get(jj);
				roi.setContour(PolygonUtils.merge(roi.getContour(), rois.get(ii).getContour()));
				rois.remove(rois.get(ii));
				listModel.remove(ii);
				break;
			}
		}
		this.consumer.setRois(rois);
	}
	
	private void dilate() {
		if (list.getSelectedIndex() == -1)
			return;
		ModifyRoiCommand command = new ModifyRoiCommand(this, rois);
		int[] indices = list.getSelectedIndices();
		for (int ind : indices) {
			Mask mask = rois.get(ind);
			command.setOldContour(mask.getUUID(), mask.getContour());
			Polygon newPol = PolygonUtils.dilate(mask.getContour(), 1);
			command.setNewContour(mask.getUUID(), newPol);
			mask.setContour(newPol);
			consumer.setRois(rois, ind);
		}
		this.commandCallback.accept(command);
	}
	
	private void erode() {
		if (list.getSelectedIndex() == -1)
			return;
		int[] indices = list.getSelectedIndices();
		ModifyRoiCommand command = new ModifyRoiCommand(this, rois);
		for (int ind : indices) {
			Mask mask = rois.get(ind);
			command.setOldContour(mask.getUUID(), mask.getContour());
			Polygon newPol = PolygonUtils.dilate(mask.getContour(), 1);
			command.setNewContour(mask.getUUID(), newPol);
			mask.setContour(newPol);
			consumer.setRois(rois, ind);
		}
		this.commandCallback.accept(command);
	}
	
	private void exportMask() {
		
	}

	private void exportLMD() {
		exportLMDCallback.accept(rois);
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
		else if (command.equals("Delete") && list.getSelectedIndex() != -1 && list.getSelectedIndices().length == 1)
			delete(list.getSelectedIndex());
		else if (command.equals("Delete") && list.getSelectedIndex() != -1 && list.getSelectedIndices().length != 1)
			delete(list.getSelectedIndices());
		else if (command.equals("Delete") && list.getSelectedIndex() == -1 && justClickedDelete)
			deleteAll();
		else if (command.equals("Delete") && list.getSelectedIndex() == -1) {
			justClickedDelete = true;
			return;
		} else if (command.equals("Complicate"))
			complicate();
		else if (command.equals("Export LMD"))
			exportLMD();
		else if (command.equals("Dilate"))
			dilate();
		else if (command.equals("Erode"))
			erode();
		else if (command.equals("Merge"))
			merge();
		
		justClickedDelete = false;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getComponent().equals(this.list)) {
			int ind = this.list.getSelectedIndex();
			if (ind < 0)
				return;
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
		for (JButton btn : this.btns) {
			if (btn.getText().equals("Dilate") || btn.getText().equals("Erode") || btn.getText().equals("Simplify")
					|| btn.getText().equals("Complicate"))
				btn.setEnabled(list.getSelectedIndex()  != -1 && rois.size() > 0);
			else if (btn.getText().equals("Merge"))
				btn.setEnabled(list.getSelectedIndex()  != -1 && list.getSelectedIndices().length > 1);
			
		}
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

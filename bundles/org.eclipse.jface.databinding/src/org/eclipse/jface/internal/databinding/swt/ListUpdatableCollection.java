/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.SelectionAwareUpdatableCollection;
import org.eclipse.jface.databinding.swt.SWTProperties;
import org.eclipse.jface.databinding.viewers.ViewersProperties;
import org.eclipse.swt.widgets.List;

/**
 * @since 3.2
 *
 */
public class ListUpdatableCollection extends SelectionAwareUpdatableCollection {
	
	private final List list;

	private final String attribute;

	private boolean updating = false;

	/**
	 * @param list
	 * @param attribute
	 */
	public ListUpdatableCollection(List list, String attribute) {
		this.list = list;
		
		
		if (attribute.equals(ViewersProperties.CONTENT))
			this.attribute = SWTProperties.ITEMS;
		else
			this.attribute = attribute;
		
		if (this.attribute.equals(SWTProperties.ITEMS)) {
			//TODO List does not fire any event when items are changed.
//			list.addModifyListener(new ModifyListener() {
//				public void modifyText(ModifyEvent e) {
//					if (!updating) {
//						fireChangeEvent(ChangeEvent.CHANGE, null, null);
//					}
//				}
//			});
		}
		else
			throw new IllegalArgumentException();
	}

	public int computeSize() {
		return list.getItemCount();
	}

	public int addElement(Object value, int index) {
		updating=true;		
		try {
			if (index<0 || index>computeSize())
				index=computeSize();
			String[] newItems = new String[computeSize()+1];			
			System.arraycopy(list.getItems(), 0, newItems,0, index);
			newItems[index]=(String)value;
			System.arraycopy(list.getItems(), index, newItems,index+1, computeSize()-index);
			list.setItems(newItems);
			fireChangeEvent(ChangeEvent.ADD, null, value, index);
		}
		finally{
			updating=false;
		}
		return index;
	}

	public void removeElement(int index) {
		updating=true;		
		try {
			if (index<0 || index>computeSize())
				index=computeSize();
			String[] newItems = new String[computeSize()-1];
			String old = list.getItem(index);
			System.arraycopy(list.getItems(), 0, newItems,0, index);			
			System.arraycopy(list.getItems(), index, newItems,index-1, computeSize()-index);			
			list.setItems(newItems);
			fireChangeEvent(ChangeEvent.REMOVE, old, null, index);
		}
		finally{
			updating=false;
		}		
	}

	public void setElement(int index, Object value) {
		String old = list.getItem(index);
		list.setItem(index, (String)value);
		fireChangeEvent(ChangeEvent.CHANGE, old, value, index);
	}

	public Object computeElement(int index) {
		return list.getItem(index);
	}

	public Class getElementType() {
		return String.class;
	}

	public Object getSelectedObject() {
		if (list.getSelectionCount() > 0) {
			return list.getSelection()[0];
		} 
		return null;
	}

	public void setSelectedObject(Object object) {
		if (object == null) {
			list.setSelection(-1);
		} else {
			int index = list.indexOf((String) object);
			if (index > -1) {
				list.setSelection(index);
			}
		}
	}

	
}

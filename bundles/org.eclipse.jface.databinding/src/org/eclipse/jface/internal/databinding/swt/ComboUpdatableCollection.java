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

import org.eclipse.jface.databinding.BindingException;
import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.SelectionAwareUpdatableCollection;
import org.eclipse.jface.databinding.swt.SWTProperties;
import org.eclipse.jface.databinding.viewers.ViewersProperties;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Combo;

/**
 * @since 3.2
 *
 */
public class ComboUpdatableCollection extends SelectionAwareUpdatableCollection {
	
	private final Combo combo;

	private final String attribute;

	private boolean updating = false;

	/**
	 * @param combo
	 * @param attribute
	 */
	public ComboUpdatableCollection(Combo combo, String attribute) {
		this.combo = combo;
		
		
		if (attribute.equals(ViewersProperties.CONTENT))
			this.attribute = SWTProperties.ITEMS;
		else
			this.attribute = attribute;
		
		if (this.attribute.equals(SWTProperties.ITEMS)) {
			combo.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if (!updating) {
						fireChangeEvent(ChangeEvent.CHANGE, null, null);
					}
				}
			});
		}
		else
			throw new IllegalArgumentException();
	}

	public int computeSize() {
		return combo.getItemCount();
	}

	public int addElement(Object value, int index) {
		updating=true;		
		try {
			if (index<0 || index>computeSize())
				index=computeSize();
			String[] newItems = new String[computeSize()+1];			
			System.arraycopy(combo.getItems(), 0, newItems,0, index);
			newItems[index]=(String)value;
			System.arraycopy(combo.getItems(), index, newItems,index+1, computeSize()-index);
			combo.setItems(newItems);
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
			if (index<0 || index > computeSize() - 1)
				throw new BindingException("Request to remove an element out of the collection bounds"); //$NON-NLS-1$

			String[] newItems = new String[computeSize()-1];
			String old = combo.getItem(index);
			System.arraycopy(combo.getItems(), 0, newItems,0, index);
			if (newItems.length > 0) {
				System.arraycopy(combo.getItems(), 0, newItems,0, index);
				if (computeSize() - 1 > index) {
					System.arraycopy(combo.getItems(), index + 1, newItems, index, computeSize() - index - 1);
				}
			}
			combo.setItems(newItems);
			fireChangeEvent(ChangeEvent.REMOVE, old, null, index);
		}
		finally{
			updating=false;
		}		
	}

	public void setElement(int index, Object value) {
		String old = combo.getItem(index);
		combo.setItem(index, (String)value);
		fireChangeEvent(ChangeEvent.CHANGE, old, value, index);
	}

	public Object computeElement(int index) {
		return combo.getItem(index);
	}

	public Class getElementType() {
		return String.class;
	}

	public Object getSelectedObject() {
		SyncRunnable runnable = new SyncRunnable() {
			public Object run() {
				int index = combo.getSelectionIndex();
				if (index > -1) {
					return combo.getItem(index);
				}
				return null;
			}
		};
		return runnable.runOn(combo.getDisplay());
	}

	public void setSelectedObject(final Object object) {
		SyncRunnable runnable = new SyncRunnable() {
			public Object run() {
				if (object == null) {
					combo.deselectAll();
				} else {
					int index = combo.indexOf((String) object);
					if (index != -1) {
						combo.select(index);
					}
				}
				return null;
			}
		};
		runnable.runOn(combo.getDisplay());
	}
}

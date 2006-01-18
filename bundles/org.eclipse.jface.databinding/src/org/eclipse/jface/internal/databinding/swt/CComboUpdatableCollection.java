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
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;

/**
 * @since 3.2
 *
 */
public class CComboUpdatableCollection extends SelectionAwareUpdatableCollection {
	
	private final CCombo ccombo;

	private final String attribute;

	private boolean updating = false;

	/**
	 * @param ccombo
	 * @param attribute
	 */
	public CComboUpdatableCollection(CCombo ccombo, String attribute) {
		this.ccombo = ccombo;
		
		
		if (attribute.equals(ViewersProperties.CONTENT))
			this.attribute = SWTProperties.ITEMS;
		else
			this.attribute = attribute;
		
		if (this.attribute.equals(SWTProperties.ITEMS)) {
			ccombo.addModifyListener(new ModifyListener() {
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

	public int getSize() {
		return ccombo.getItemCount();
	}

	public int addElement(Object value, int index) {
		updating=true;		
		try {
			if (index<0 || index>getSize())
				index=getSize();
			String[] newItems = new String[getSize()+1];			
			System.arraycopy(ccombo.getItems(), 0, newItems,0, index);
			newItems[index]=(String)value;
			System.arraycopy(ccombo.getItems(), index, newItems,index+1, getSize()-index);
			ccombo.setItems(newItems);
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
			if (index<0 || index > getSize() - 1)
				throw new BindingException("Request to remove an element out of the collection bounds"); //$NON-NLS-1$
			
			String[] newItems = new String[getSize()-1];
			String old = ccombo.getItem(index);
			if (newItems.length > 0) {
				System.arraycopy(ccombo.getItems(), 0, newItems,0, index);
				if (getSize() - 1 > index) {
					System.arraycopy(ccombo.getItems(), index + 1, newItems, index, getSize() - index - 1);
				}
			}
			ccombo.setItems(newItems);
			fireChangeEvent(ChangeEvent.REMOVE, old, null, index);
		}
		finally{
			updating=false;
		}		
	}

	public void setElement(int index, Object value) {
		String old = ccombo.getItem(index);
		ccombo.setItem(index, (String)value);
		fireChangeEvent(ChangeEvent.CHANGE, old, value, index);
	}

	public Object getElement(int index) {
		return ccombo.getItem(index);
	}

	public Class getElementType() {
		return String.class;
	}

	public Object getSelectedObject() {
		SyncRunnable runnable = new SyncRunnable() {
			public Object run() {
				int index = ccombo.getSelectionIndex();
				if (index > -1) {
					return ccombo.getItem(index);
				}
				return null;
			}
		};
		return runnable.runOn(ccombo.getDisplay());
	}

	public void setSelectedObject(final Object object) {
		SyncRunnable runnable = new SyncRunnable() {
			public Object run() {
				if (object == null) {
					ccombo.clearSelection();
				} else {
					int index = ccombo.indexOf((String) object);
					if (index != -1) {
						ccombo.select(index);
					}
				}
				return null;
			}
		};
		runnable.runOn(ccombo.getDisplay());
	}
}

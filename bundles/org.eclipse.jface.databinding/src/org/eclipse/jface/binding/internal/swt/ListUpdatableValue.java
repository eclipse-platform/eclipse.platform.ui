/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.binding.internal.swt;

import org.eclipse.jface.binding.IChangeEvent;
import org.eclipse.jface.binding.UpdatableValue;
import org.eclipse.jface.binding.swt.SWTBindingConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.List;

/**
 * @since 3.2
 * 
 */
public class ListUpdatableValue extends UpdatableValue {

	/**
	 * 
	 */

	private final List list;

	private final String attribute;

	private boolean updating = false;

	/**
	 * @param list
	 * @param attribute
	 */
	public ListUpdatableValue(List list, String attribute) {
		this.list = list;
		this.attribute = attribute;
		
		if ((list.getStyle()&SWT.MULTI)>0)
			throw new IllegalArgumentException("SWT.SINGLE support only for a List selection"); //$NON-NLS-1$
		
		if (attribute.equals(SWTBindingConstants.SELECTION)) {
			list.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					if (!updating) {
						fireChangeEvent(IChangeEvent.CHANGE, null, null);
					}					
				}
				public void widgetDefaultSelected(SelectionEvent e) {
					if (!updating) {
						fireChangeEvent(IChangeEvent.CHANGE, null, null);
					}				
				}
			});
		} else
			throw new IllegalArgumentException();
	}

	public void setValue(Object value) {
		String oldValue = null;
		if (list.getSelection()!=null && list.getSelection().length>0)
			oldValue = list.getSelection()[0];
		try {
			updating = true;
			if (attribute.equals(SWTBindingConstants.SELECTION)) {
				String items[] = list.getItems();
				int index = -1;
				if (items != null && value != null) {
					for (int i = 0; i < items.length; i++) {
						if (value.equals(items[i])) {
							index = i;
							break;
						}
					}
					list.select(index); // -1 will not "unselect"
				}
			}
		} finally {
			updating = false;
		}
		fireChangeEvent(IChangeEvent.CHANGE, oldValue, value);
	}

	public Object getValue() {
		if (attribute.equals(SWTBindingConstants.SELECTION)) {
			int index = list.getSelectionIndex();
			if (index >= 0)
				return list.getItem(index);
			return null;
		}
		throw new AssertionError("unexpected attribute"); //$NON-NLS-1$

	}

	public Class getValueType() {
		if (attribute.equals(SWTBindingConstants.SELECTION)) {
			return String.class;
		}
		throw new AssertionError("unexpected attribute"); //$NON-NLS-1$
	}

}

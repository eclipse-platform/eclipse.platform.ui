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
package org.eclipse.jface.databinding.internal.swt;

import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.SWTProperties;
import org.eclipse.jface.databinding.UpdatableValue;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;

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
		
		if (attribute.equals(SWTProperties.SELECTION)) {
			list.addListener(SWT.Selection, new Listener(){			
				public void handleEvent(Event event) {
					if (!updating) {
						fireChangeEvent(ChangeEvent.CHANGE, null, null);
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
			if (attribute.equals(SWTProperties.SELECTION)) {
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
		fireChangeEvent(ChangeEvent.CHANGE, oldValue, value);
	}

	public Object getValue() {
		Assert.isTrue(attribute.equals(SWTProperties.SELECTION),
				"unexpected attribute" + attribute); //$NON-NLS-1$
		int index = list.getSelectionIndex();
		if (index >= 0)
			return list.getItem(index);
		return null;
	}

	public Class getValueType() {
		Assert.isTrue(attribute.equals(SWTProperties.SELECTION),
				"unexpected attribute" + attribute); //$NON-NLS-1$
		return String.class;
	}

}

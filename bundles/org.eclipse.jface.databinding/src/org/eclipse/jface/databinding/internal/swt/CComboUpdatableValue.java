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

import org.eclipse.jface.databinding.DataBinding;
import org.eclipse.jface.databinding.IChangeEvent;
import org.eclipse.jface.databinding.UpdatableValue;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;

/**
 * @since 3.2
 * 
 */
public class CComboUpdatableValue extends UpdatableValue {

	/**
	 * 
	 */

	private final CCombo ccombo;

	private final String attribute;

	private boolean updating = false;

	/**
	 * @param ccombo
	 * @param attribute
	 */
	public CComboUpdatableValue(CCombo ccombo, String attribute) {
		this.ccombo = ccombo;
		this.attribute = attribute;
		if (attribute.equals(DataBinding.CONTENT))
			attribute = DataBinding.TEXT;

		if (attribute.equals(DataBinding.SELECTION) || 
			attribute.equals(DataBinding.TEXT)) {
			ccombo.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if (!updating) {
						fireChangeEvent(IChangeEvent.CHANGE, null, null);
					}
				}
			});
		}
		else
			throw new IllegalArgumentException();
	}

	public void setValue(Object value) {
		String oldValue = ccombo.getText();
		try {
			updating = true;
			if (attribute.equals(DataBinding.TEXT)) {
				String stringValue = (String) value;
				ccombo.setText(stringValue);
			} else if (attribute.equals(DataBinding.SELECTION)) {
				String items[] = ccombo.getItems();
				int index = -1;
				if (items != null && value != null) {
					for (int i = 0; i < items.length; i++) {
						if (value.equals(items[i])) {
							index = i;
							break;
						}
					}
					ccombo.select(index); // -1 will not "unselect"
				}
			}
		} finally {
			updating = false;
		}
		fireChangeEvent(IChangeEvent.CHANGE, oldValue, ccombo
				.getText());
	}

	public Object getValue() {
		if (attribute.equals(DataBinding.TEXT)) {
			return ccombo.getText();
		} else if (attribute.equals(DataBinding.SELECTION)) {
			// The problem with a ccombo, is that it changes the text an fires before 
			// it update its selection index
			return ccombo.getText();
//			int index = ccombo.getSelectionIndex();
//			if (index >= 0)
//				return ccombo.getItem(index);
//			return null;
		} else
			throw new AssertionError("unexpected attribute"); //$NON-NLS-1$

	}

	public Class getValueType() {
		if (attribute.equals(DataBinding.TEXT)
				|| attribute.equals(DataBinding.SELECTION)) {
			return String.class;
		}
		throw new AssertionError("unexpected attribute"); //$NON-NLS-1$
	}

}

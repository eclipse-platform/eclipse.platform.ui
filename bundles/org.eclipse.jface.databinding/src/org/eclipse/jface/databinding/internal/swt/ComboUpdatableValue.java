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

import org.eclipse.jface.databinding.IChangeEvent;
import org.eclipse.jface.databinding.UpdatableValue;
import org.eclipse.jface.databinding.swt.SWTBindingConstants;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Combo;

/**
 * @since 3.2
 * 
 */
public class ComboUpdatableValue extends UpdatableValue {

	/**
	 * 
	 */

	private final Combo combo;

	private final String attribute;

	private boolean updating = false;

	/**
	 * @param combo
	 * @param attribute
	 */
	public ComboUpdatableValue(Combo combo, String attribute) {
		this.combo = combo;
		this.attribute = attribute;
		if (attribute.equals(SWTBindingConstants.CONTENT))
			attribute = SWTBindingConstants.TEXT;

		if (attribute.equals(SWTBindingConstants.TEXT)
				|| attribute.equals(SWTBindingConstants.SELECTION)) {
			combo.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if (!updating) {
						fireChangeEvent(IChangeEvent.CHANGE, null, null);
					}
				}
			});
		} else
			throw new IllegalArgumentException();
	}

	public void setValue(Object value) {
		String oldValue = combo.getText();
		try {
			updating = true;
			if (attribute.equals(SWTBindingConstants.TEXT)) {
				String stringValue = (String) value;
				combo.setText(stringValue);
			} else if (attribute.equals(SWTBindingConstants.SELECTION)) {
				String items[] = combo.getItems();
				int index = -1;
				if (items != null && value != null) {
					for (int i = 0; i < items.length; i++) {
						if (value.equals(items[i])) {
							index = i;
							break;
						}
					}
					combo.select(index); // -1 will not "unselect"
				}
			}
		} finally {
			updating = false;
		}
		fireChangeEvent(IChangeEvent.CHANGE, oldValue, combo
				.getText());
	}

	public Object getValue() {
		if (attribute.equals(SWTBindingConstants.TEXT)) {
			return combo.getText();
		} else if (attribute.equals(SWTBindingConstants.SELECTION)) {
			int index = combo.getSelectionIndex();
			if (index >= 0)
				return combo.getItem(index);
			return null;
		} else
			throw new AssertionError("unexpected attribute"); //$NON-NLS-1$

	}

	public Class getValueType() {
		if (attribute.equals(SWTBindingConstants.TEXT)
				|| attribute.equals(SWTBindingConstants.SELECTION)) {
			return String.class;
		}
		throw new AssertionError("unexpected attribute"); //$NON-NLS-1$
	}

}

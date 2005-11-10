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
import org.eclipse.jface.util.Assert;
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
		if (attribute.equals(DataBinding.CONTENT))
			attribute = DataBinding.TEXT;

		if (attribute.equals(DataBinding.TEXT)
				|| attribute.equals(DataBinding.SELECTION)) {
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
			if (attribute.equals(DataBinding.TEXT)) {
				String stringValue = value!=null?value.toString() : ""; //$NON-NLS-1$
				combo.setText(stringValue);
			} else if (attribute.equals(DataBinding.SELECTION)) {
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
		fireChangeEvent(IChangeEvent.CHANGE, oldValue, combo.getText());
	}

	public Object getValue() {
		if (attribute.equals(DataBinding.TEXT)) {
			return combo.getText();
		}
		Assert.isTrue(attribute.equals(DataBinding.SELECTION),
				"unexpected attribute" + attribute); //$NON-NLS-1$
		int index = combo.getSelectionIndex();
		if (index >= 0)
			return combo.getItem(index);
		return null;
	}

	public Class getValueType() {
		Assert.isTrue(attribute.equals(DataBinding.TEXT)
				|| attribute.equals(DataBinding.SELECTION),
				"unexpected attribute" + attribute); //$NON-NLS-1$
		return String.class;
	}

}

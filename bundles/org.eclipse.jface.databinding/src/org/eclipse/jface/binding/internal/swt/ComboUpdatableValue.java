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

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.binding.IChangeEvent;
import org.eclipse.jface.binding.UpdatableValue;
import org.eclipse.jface.binding.swt.SWTBindingConstants;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Combo;

/**
 * @since 3.2
 *
 */
public class ComboUpdatableValue extends UpdatableValue {

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
		if (attribute.equals(SWTBindingConstants.TEXT)) {
			combo.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if (!updating) {
						fireChangeEvent(IChangeEvent.CHANGE, null, null);
					}
				}
			});
		} else if (!attribute.equals(SWTBindingConstants.ITEMS)) {
			throw new IllegalArgumentException();
		}
	}

	public void setValue(Object value) {
		try {
			updating = true;
			if (attribute.equals(SWTBindingConstants.TEXT)) {
				String stringValue = (String) value;
				combo.setText(stringValue);
			} else if (attribute.equals(SWTBindingConstants.ITEMS)) {
				List listValue = (List) value;
				combo.setItems((String[]) listValue
						.toArray(new String[listValue.size()]));
			}
		} finally {
			updating = false;
		}
	}

	public Object getValue() {
		if (attribute.equals(SWTBindingConstants.TEXT)) {
			return combo.getText();
		} else if (attribute.equals(SWTBindingConstants.ITEMS)) {
			return Arrays.asList(combo.getItems());
		} else {
			throw new AssertionError("unexpected attribute");
		}
	}

	public Class getValueType() {
		if (attribute.equals(SWTBindingConstants.TEXT)) {
			return String.class;
		} else if (attribute.equals(SWTBindingConstants.ITEMS)) {
			return List.class;
		} else {
			throw new AssertionError("unexpected attribute");
		}
	}

}

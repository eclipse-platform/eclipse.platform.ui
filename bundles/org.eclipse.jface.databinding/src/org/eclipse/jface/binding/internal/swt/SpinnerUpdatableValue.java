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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Spinner;

public class SpinnerUpdatableValue extends UpdatableValue {

	private final Spinner spinner;

	private final String attribute;

	private boolean updating = false;

	/**
	 * @param spinner
	 * @param attribute
	 */
	public SpinnerUpdatableValue(Spinner spinner, String attribute) {
		this.spinner = spinner;
		this.attribute = attribute;
		if (attribute.equals(SWTBindingConstants.SELECTION)) {
			spinner.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if (!updating) {
						fireChangeEvent(IChangeEvent.CHANGE, null, null);
					}
				}
			});
		} else if (!attribute.equals(SWTBindingConstants.MIN)
				&& !attribute.equals(SWTBindingConstants.MAX)) {
			throw new IllegalArgumentException();
		}
	}

	public void setValue(Object value) {
		try {
			updating = true;
			int intValue = ((Integer) value).intValue();
			if (attribute.equals(SWTBindingConstants.SELECTION)) {
				spinner.setSelection(intValue);
			} else if (attribute.equals(SWTBindingConstants.MIN)) {
				spinner.setMinimum(intValue);
			} else if (attribute.equals(SWTBindingConstants.MAX)) {
				spinner.setMaximum(intValue);
			}
		} finally {
			updating = false;
		}
	}

	public Object getValue() {
		int value;
		if (attribute.equals(SWTBindingConstants.SELECTION)) {
			value = spinner.getSelection();
		} else if (attribute.equals(SWTBindingConstants.MIN)) {
			value = spinner.getMinimum();
		} else if (attribute.equals(SWTBindingConstants.MAX)) {
			value = spinner.getMaximum();
		} else {
			throw new AssertionError("unexpected attribute");
		}
		return new Integer(value);
	}

	public Class getValueType() {
		return Integer.class;
	}

}

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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Spinner;

/**
 * @since 3.2
 * 
 */
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
		if (attribute.equals(DataBinding.SELECTION)) {
			spinner.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if (!updating) {
						fireChangeEvent(IChangeEvent.CHANGE, null, null);
					}
				}
			});
		} else if (!attribute.equals(DataBinding.MIN)
				&& !attribute.equals(DataBinding.MAX)) {
			throw new IllegalArgumentException(
					"Attribute name not valid: " + attribute); //$NON-NLS-1$
		}
	}

	public void setValue(Object value) {
		int oldValue;
		int newValue;
		try {
			updating = true;
			newValue = ((Integer) value).intValue();
			if (attribute.equals(DataBinding.SELECTION)) {
				oldValue = spinner.getSelection();
				spinner.setSelection(newValue);
			} else if (attribute.equals(DataBinding.MIN)) {
				oldValue = spinner.getMinimum();
				spinner.setMinimum(newValue);
			} else if (attribute.equals(DataBinding.MAX)) {
				oldValue = spinner.getMaximum();
				spinner.setMaximum(newValue);
			} else {
				throw new AssertionError("invalid attribute name"); //$NON-NLS-1$
			}
			fireChangeEvent(IChangeEvent.CHANGE, new Integer(
					oldValue), new Integer(newValue));
		} finally {
			updating = false;
		}
	}

	public Object getValue() {
		int value = 0;
		if (attribute.equals(DataBinding.SELECTION)) {
			value = spinner.getSelection();
		} else if (attribute.equals(DataBinding.MIN)) {
			value = spinner.getMinimum();
		} else if (attribute.equals(DataBinding.MAX)) {
			value = spinner.getMaximum();
		}
		return new Integer(value);
	}

	public Class getValueType() {
		return Integer.class;
	}

}

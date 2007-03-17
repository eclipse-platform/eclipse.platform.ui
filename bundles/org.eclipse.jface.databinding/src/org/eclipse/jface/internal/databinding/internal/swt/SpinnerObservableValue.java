/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 164653
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.internal.swt;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.jface.internal.databinding.provisional.swt.AbstractSWTObservableValue;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Spinner;

/**
 * @since 1.0
 * 
 */
public class SpinnerObservableValue extends AbstractSWTObservableValue {

	private final Spinner spinner;

	private final String attribute;

	private boolean updating = false;

	private int currentSelection;

	/**
	 * @param spinner
	 * @param attribute
	 */
	public SpinnerObservableValue(Spinner spinner, String attribute) {
		super(spinner);
		this.spinner = spinner;
		this.attribute = attribute;
		if (attribute.equals(SWTProperties.SELECTION)) {
			currentSelection = spinner.getSelection();
			spinner.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if (!updating) {
						int newSelection = SpinnerObservableValue.this.spinner
								.getSelection();
						fireValueChange(Diffs.createValueDiff(new Integer(
								currentSelection), new Integer(newSelection)));
						currentSelection = newSelection;
					}
				}
			});
		} else if (!attribute.equals(SWTProperties.MIN)
				&& !attribute.equals(SWTProperties.MAX)) {
			throw new IllegalArgumentException(
					"Attribute name not valid: " + attribute); //$NON-NLS-1$
		}
	}

	public void doSetValue(final Object value) {
		int oldValue;
		int newValue;
		try {
			updating = true;
			newValue = ((Integer) value).intValue();
			if (attribute.equals(SWTProperties.SELECTION)) {
				oldValue = spinner.getSelection();
				spinner.setSelection(newValue);
				currentSelection = newValue;
			} else if (attribute.equals(SWTProperties.MIN)) {
				oldValue = spinner.getMinimum();
				spinner.setMinimum(newValue);
			} else if (attribute.equals(SWTProperties.MAX)) {
				oldValue = spinner.getMaximum();
				spinner.setMaximum(newValue);
			} else {
				Assert.isTrue(false, "invalid attribute name:" + attribute); //$NON-NLS-1$
				return;
			}
			fireValueChange(Diffs.createValueDiff(new Integer(oldValue),
					new Integer(newValue)));
		} finally {
			updating = false;
		}
	}

	public Object doGetValue() {
		int value = 0;
		if (attribute.equals(SWTProperties.SELECTION)) {
			value = spinner.getSelection();
		} else if (attribute.equals(SWTProperties.MIN)) {
			value = spinner.getMinimum();
		} else if (attribute.equals(SWTProperties.MAX)) {
			value = spinner.getMaximum();
		}
		return new Integer(value);
	}

	public Object getValueType() {
		return Integer.TYPE;
	}

	/**
	 * @return attribute being observed
	 */
	public String getAttribute() {
		return attribute;
	}
}

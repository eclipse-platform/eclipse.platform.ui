/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 164653
 *     Ashley Cambrell - bug 198904
 *     Matthew Hall - bug 118516
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.internal.databinding.provisional.swt.AbstractSWTObservableValue;
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

	private ModifyListener modifyListener;

	/**
	 * @param spinner
	 * @param attribute
	 */
	public SpinnerObservableValue(Spinner spinner, String attribute) {
		super(spinner);
		this.spinner = spinner;
		this.attribute = attribute;
		init();
	}
	
	/**
	 * @param realm
	 * @param spinner
	 * @param attribute
	 */
	public SpinnerObservableValue(Realm realm, Spinner spinner, String attribute) {
		super(realm, spinner);
		this.spinner = spinner;
		this.attribute = attribute;
		init();
	}
	
	private void init() {		
		if (attribute.equals(SWTProperties.SELECTION)) {
			currentSelection = spinner.getSelection();
			modifyListener = new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if (!updating) {
						int newSelection = SpinnerObservableValue.this.spinner
						.getSelection();
						notifyIfChanged(currentSelection, newSelection);
						currentSelection = newSelection;
					}
				}
			};
			spinner.addModifyListener(modifyListener);
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
			notifyIfChanged(oldValue, newValue);
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

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.databinding.observable.value.AbstractObservableValue#dispose()
	 */
	public synchronized void dispose() {
		super.dispose();
		if (modifyListener != null && !spinner.isDisposed()) {
			spinner.removeModifyListener(modifyListener);
		}
	}
	
	private void notifyIfChanged(int oldValue, int newValue) {
		if (oldValue != newValue) {
			fireValueChange(Diffs.createValueDiff(new Integer(oldValue),
					new Integer(newValue)));
		}
	}
}

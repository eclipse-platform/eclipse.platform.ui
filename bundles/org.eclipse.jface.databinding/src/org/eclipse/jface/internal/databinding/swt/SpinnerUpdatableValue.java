/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.UpdatableValue;
import org.eclipse.jface.databinding.swt.SWTProperties;
import org.eclipse.jface.util.Assert;
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
		if (attribute.equals(SWTProperties.SELECTION)) {
			spinner.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if (!updating) {
						fireChangeEvent(ChangeEvent.CHANGE, null, null);
					}
				}
			});
		} else if (!attribute.equals(SWTProperties.MIN)
				&& !attribute.equals(SWTProperties.MAX)) {
			throw new IllegalArgumentException(
					"Attribute name not valid: " + attribute); //$NON-NLS-1$
		}
	}

	public void setValue(final Object value) {
		AsyncRunnable runnable = new AsyncRunnable(){
			public void run(){
				int oldValue;
				int newValue;
				try {
					updating = true;
					newValue = ((Integer) value).intValue();
					if (attribute.equals(SWTProperties.SELECTION)) {
						oldValue = spinner.getSelection();
						spinner.setSelection(newValue);
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
					fireChangeEvent(ChangeEvent.CHANGE, new Integer(
							oldValue), new Integer(newValue));
				} finally {
					updating = false;
				}				
			}
		};
		runnable.runOn(spinner.getDisplay());
	}

	public Object getValue() {
		SyncRunnable runnable = new SyncRunnable(){
			public Object run(){
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
		};
		return runnable.runOn(spinner.getDisplay());
	}

	public Class getValueType() {
		return Integer.class;
	}

}

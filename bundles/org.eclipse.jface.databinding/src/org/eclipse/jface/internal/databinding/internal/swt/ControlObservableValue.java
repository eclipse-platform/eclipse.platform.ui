/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.internal.swt;

import org.eclipse.jface.databinding.observable.Diffs;
import org.eclipse.jface.internal.databinding.provisional.swt.AbstractSWTObservableValue;
import org.eclipse.jface.internal.databinding.provisional.swt.SWTProperties;
import org.eclipse.swt.widgets.Control;

/**
 * @since 1.0
 * 
 */
public class ControlObservableValue extends AbstractSWTObservableValue {

	private final Control control;

	private final String attribute;

	/**
	 * @param control
	 * @param attribute
	 */
	public ControlObservableValue(Control control, String attribute) {
		super(control);
		this.control = control;
		this.attribute = attribute;
		if (!attribute.equals(SWTProperties.ENABLED)
				&& !attribute.equals(SWTProperties.VISIBLE)) {
			throw new IllegalArgumentException();
		}
	}

	public void setValue(Object value) {
		Object oldValue = doGetValue();
		if (attribute.equals(SWTProperties.ENABLED)) {
			control.setEnabled(((Boolean) value).booleanValue());
		} else if (attribute.equals(SWTProperties.VISIBLE)) {
			control.setVisible(((Boolean) value).booleanValue());
		}
		fireValueChange(Diffs.createValueDiff(oldValue, value));
	}

	public Object doGetValue() {
		boolean value = attribute.equals(SWTProperties.ENABLED) ? control
				.getEnabled() : control.getVisible();
		return value ? Boolean.TRUE : Boolean.FALSE;
	}

	public Object getValueType() {
		return Boolean.TYPE;
	}

}

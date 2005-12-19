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
package org.eclipse.jface.databinding.internal.swt;

import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.UpdatableValue;
import org.eclipse.jface.databinding.swt.SWTProperties;
import org.eclipse.swt.widgets.Control;

/**
 * @since 3.2
 *
 */
public class ControlUpdatableValue extends UpdatableValue {

	private final Control control;
	private final String attribute;

	/**
	 * @param control
	 * @param attribute
	 */
	public ControlUpdatableValue(Control control, String attribute) {
		this.control = control;
		this.attribute = attribute;
		if (!attribute.equals(SWTProperties.ENABLED) && !attribute.equals(SWTProperties.VISIBLE)) {
			throw new IllegalArgumentException();
		}
	}

	public void setValue(Object value) {
		Object oldValue = getValue();
		if (attribute.equals(SWTProperties.ENABLED)) {
			control.setEnabled(((Boolean) value).booleanValue());
		} else if (attribute.equals(SWTProperties.VISIBLE)) {
			control.setVisible(((Boolean) value).booleanValue());
		}
		fireChangeEvent(ChangeEvent.CHANGE, oldValue, value);
	}

	public Object getValue() {
		return new Boolean(attribute.equals(SWTProperties.ENABLED) ? control.getEnabled() : control.getVisible());
	}

	public Class getValueType() {
		return Boolean.TYPE;
	}

}

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

	/**
	 * @param control
	 * @param attribute
	 */
	public ControlUpdatableValue(Control control, String attribute) {
		this.control = control;
		if (!attribute.equals(SWTProperties.ENABLED)) {
			throw new IllegalArgumentException();
		}
	}

	public void setValue(Object value) {
		boolean oldValue = control.getEnabled();
		control.setEnabled(((Boolean) value).booleanValue());
		fireChangeEvent(ChangeEvent.CHANGE, new Boolean(oldValue), value);
	}

	public Object getValue() {
		return new Boolean(control.getEnabled());
	}

	public Class getValueType() {
		return Boolean.class;
	}

}

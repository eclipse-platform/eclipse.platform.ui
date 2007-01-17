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
 *     Matt Carter - bug 170668
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.internal.swt;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.jface.internal.databinding.provisional.swt.AbstractSWTObservableValue;
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
				&& !attribute.equals(SWTProperties.VISIBLE) && !attribute.equals(SWTProperties.TOOLTIP_TEXT)) {
			throw new IllegalArgumentException();
		}
	}

	public void doSetValue(Object value) {
		Object oldValue = doGetValue();
		if (attribute.equals(SWTProperties.ENABLED)) {
			control.setEnabled(((Boolean) value).booleanValue());
		} else if (attribute.equals(SWTProperties.VISIBLE)) {
			control.setVisible(((Boolean) value).booleanValue());
		} else if (attribute.equals(SWTProperties.TOOLTIP_TEXT)) {
			control.setToolTipText((String) value);
		}
		fireValueChange(Diffs.createValueDiff(oldValue, value));
	}

	public Object doGetValue() {
		if (attribute.equals(SWTProperties.ENABLED)) {
			return control.getEnabled() ? Boolean.TRUE : Boolean.FALSE;
		}
		if (attribute.equals(SWTProperties.VISIBLE)) {
			return control.getVisible() ? Boolean.TRUE : Boolean.FALSE;
		}
		return control.getToolTipText();
	}

	public Object getValueType() {
		return attribute.equals(SWTProperties.TOOLTIP_TEXT) ? String.class : Boolean.TYPE;
	}

}

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
 *     Matt Carter - bug 170668
 *     Brad Reynolds - bug 170848
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.swt;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.jface.internal.databinding.provisional.swt.AbstractSWTObservableValue;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Control;

/**
 * @since 1.0
 * 
 */
public class ControlObservableValue extends AbstractSWTObservableValue {

	private final Control control;

	private final String attribute;

	private Object valueType;
	
	private static final Map SUPPORTED_ATTRIBUTES = new HashMap();
	static {
		SUPPORTED_ATTRIBUTES.put(SWTProperties.ENABLED, Boolean.TYPE);
		SUPPORTED_ATTRIBUTES.put(SWTProperties.VISIBLE, Boolean.TYPE);
		SUPPORTED_ATTRIBUTES.put(SWTProperties.TOOLTIP_TEXT, String.class);
		SUPPORTED_ATTRIBUTES.put(SWTProperties.FOREGROUND, Color.class);
		SUPPORTED_ATTRIBUTES.put(SWTProperties.BACKGROUND, Color.class);
		SUPPORTED_ATTRIBUTES.put(SWTProperties.FONT, Font.class);
	}
	
	/**
	 * @param control
	 * @param attribute
	 */
	public ControlObservableValue(Control control, String attribute) {
		super(control);
		this.control = control;
		this.attribute = attribute;
		if (SUPPORTED_ATTRIBUTES.keySet().contains(attribute)) {
			this.valueType = SUPPORTED_ATTRIBUTES.get(attribute); 
		} else {
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
		} else if (attribute.equals(SWTProperties.FOREGROUND)) {
			control.setForeground((Color) value);
		} else if (attribute.equals(SWTProperties.BACKGROUND)) {
			control.setBackground((Color) value);
		} else if (attribute.equals(SWTProperties.FONT)) {
			control.setFont((Font) value);
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
		if (attribute.equals(SWTProperties.TOOLTIP_TEXT)) {
			return control.getToolTipText();			
		}
		if (attribute.equals(SWTProperties.FOREGROUND))	 {
			return control.getForeground();
		}
		if (attribute.equals(SWTProperties.BACKGROUND)) {
			return control.getBackground();
		}
		if (attribute.equals(SWTProperties.FONT)) {
			return control.getFont();
		}
		
		return null;
	}

	public Object getValueType() {
		return valueType;
	}
}

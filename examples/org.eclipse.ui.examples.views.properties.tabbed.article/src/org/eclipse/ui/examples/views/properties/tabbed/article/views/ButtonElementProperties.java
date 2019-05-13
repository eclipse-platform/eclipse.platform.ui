/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.views.properties.tabbed.article.views;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * The property source provider for a button element.
 * 
 * @author Anthony Hunter
 */
public class ButtonElementProperties
	implements IPropertySource {

	final protected ButtonElement element;

	protected static final String PROPERTY_FONT = "font"; //$NON-NLS-1$

	protected static final String PROPERTY_SIZE = "size"; //$NON-NLS-1$

	protected static final String PROPERTY_TEXT = "text"; //$NON-NLS-1$

	private final Object PropertiesTable[][] = {
		{PROPERTY_FONT, new FontPropertyDescriptor(PROPERTY_FONT, "Font")},//$NON-NLS-1$
		{PROPERTY_SIZE, new PropertyDescriptor(PROPERTY_SIZE, "Size")},//$NON-NLS-1$
		{PROPERTY_TEXT, new TextPropertyDescriptor(PROPERTY_TEXT, "Text")}, //$NON-NLS-1$
	};

	String strFont = "";//$NON-NLS-1$

	Point ptSize = null;

	String strText = "";//$NON-NLS-1$

	protected void firePropertyChanged(String propName, Object value) {
		Button ctl = element.getControl();

		if (ctl == null) {
			// the GUIView is probably hidden in this case
			return;
		}

		if (propName.equals(PROPERTY_FONT)) {
			/**
			 * Font oldfont = ctl.getFont(); if (oldfont != null) {
			 * oldfont.dispose(); }
			 */
			ctl
				.setFont(new Font(ctl.getDisplay(),
					new FontData((String) value)));
			return;
		}
		if (propName.equals(PROPERTY_TEXT)) {
			ctl.setText((String) value);
			return;
		}

	}

	protected void initProperties() {
		Button ctl = element.getControl();

		if (ctl == null) {
			// the GUIView is probably hidden in this case
			return;
		}

		strText = ctl.getText();
		/**
		 * Font font = ctl.getFont(); if (font != null) { strFont =
		 * font.getFontData().toString(); }
		 */
		ptSize = ctl.getSize();
	}

	/**
	 * Creates a new ButtonElementProperties.
	 * 
	 * @param element
	 *            the element whose properties this instance represents
	 */
	public ButtonElementProperties(ButtonElement element) {
		super();
		this.element = element;
		initProperties();
	}

	/**
	 * @see org.eclipse.ui.views.properties.IPropertySource#getEditableValue()
	 */
	public Object getEditableValue() {
		return this;
	}

	/**
	 * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyDescriptors()
	 */
	public IPropertyDescriptor[] getPropertyDescriptors() {
		// Create the property vector.
		IPropertyDescriptor[] propertyDescriptors = new IPropertyDescriptor[PropertiesTable.length];

		for (int i = 0; i < PropertiesTable.length; i++) {
			// Add each property supported.

			PropertyDescriptor descriptor;

			descriptor = (PropertyDescriptor) PropertiesTable[i][1];
			propertyDescriptors[i] = descriptor;
			descriptor.setCategory("Basic");//$NON-NLS-1$
		}

		// Return it.
		return propertyDescriptors;

	}

	/**
	 * @see org.eclipse.ui.views.properties.IPropertySource#getPropertyValue(Object)
	 */
	public Object getPropertyValue(Object name) {
		if (name.equals(PROPERTY_FONT))
			return strFont;
		if (name.equals(PROPERTY_SIZE))
			return new SizePropertySource(element, ptSize);
		if (name.equals(PROPERTY_TEXT))
			return strText;

		return null;
	}

	/**
	 * @see org.eclipse.ui.views.properties.IPropertySource#isPropertySet(Object)
	 */
	public boolean isPropertySet(Object id) {
		return false;
	}

	/**
	 * @see org.eclipse.ui.views.properties.IPropertySource#resetPropertyValue(Object)
	 */
	public void resetPropertyValue(Object id) {
		//
	}

	/**
	 * @see org.eclipse.ui.views.properties.IPropertySource#setPropertyValue(Object,
	 *      Object)
	 */
	public void setPropertyValue(Object name, Object value) {
		firePropertyChanged((String) name, value);

		if (name.equals(PROPERTY_FONT)) {
			strFont = (String) value;
			return;
		}
		if (name.equals(PROPERTY_TEXT)) {
			strText = (String) value;
			return;
		}
		if (name.equals(PROPERTY_SIZE)) {
			SizePropertySource sizeProp = (SizePropertySource) value;
			ptSize = sizeProp.getValue();
		}

	}

	/**
	 * Returns the mocha element.
	 * 
	 * @return MochaElement
	 */
	public ButtonElement getButtonElement() {
		return element;
	}

}

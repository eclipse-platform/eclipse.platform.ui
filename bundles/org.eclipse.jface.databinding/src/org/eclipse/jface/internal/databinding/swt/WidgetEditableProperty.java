/*******************************************************************************
 * Copyright (c) 2009, 2011 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 264286)
 *     Matthew Hall - bug 306203
 *******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Text;

/**
 * @since 3.3
 * 
 */
public class WidgetEditableProperty extends WidgetDelegatingValueProperty {
	IValueProperty text;
	IValueProperty ccombo;
	IValueProperty styledText;

	/**
	 * 
	 */
	public WidgetEditableProperty() {
		super(Boolean.TYPE);
	}

	protected IValueProperty doGetDelegate(Object source) {
		if (source instanceof Text) {
			if (text == null)
				text = new TextEditableProperty();
			return text;
		}
		if (source instanceof CCombo) {
			if (ccombo == null) {
				ccombo = new CComboEditableProperty();
			}
			return ccombo;
		}
		if (source instanceof StyledText) {
			if (styledText == null) {
				styledText = new StyledTextEditableProperty();
			}
			return styledText;
		}
		throw notSupported(source);
	}
}
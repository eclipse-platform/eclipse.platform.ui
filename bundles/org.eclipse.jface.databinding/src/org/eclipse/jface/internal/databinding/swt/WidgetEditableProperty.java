/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 264286)
 *******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.swt.widgets.Text;

/**
 * @since 3.3
 * 
 */
public class WidgetEditableProperty extends WidgetDelegatingValueProperty {
	IValueProperty text;

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
		throw notSupported(source);
	}
}
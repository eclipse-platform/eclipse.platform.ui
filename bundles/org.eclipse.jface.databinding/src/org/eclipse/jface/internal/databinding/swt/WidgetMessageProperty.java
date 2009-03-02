/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 266563)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolTip;

/**
 * @since 3.3
 * 
 */
public class WidgetMessageProperty extends WidgetDelegatingValueProperty {
	private IValueProperty text = new TextMessageProperty();
	private IValueProperty toolTip = new ToolTipMessageProperty();

	/**
	 * 
	 */
	public WidgetMessageProperty() {
		super(String.class);
	}

	protected IValueProperty doGetDelegate(Object source) {
		if (source instanceof Text)
			return text;
		if (source instanceof ToolTip)
			return toolTip;
		throw notSupported(source);
	}
}

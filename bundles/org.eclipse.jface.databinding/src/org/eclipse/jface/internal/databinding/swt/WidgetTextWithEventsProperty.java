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
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Text;

/**
 * @since 3.3
 * 
 */
public class WidgetTextWithEventsProperty extends WidgetDelegatingValueProperty {
	private IValueProperty styledText;
	private IValueProperty text;

	/**
	 * @param events
	 */
	public WidgetTextWithEventsProperty(int[] events) {
		super(String.class);
		styledText = new StyledTextTextProperty(events);
		text = new TextTextProperty(events);
	}

	protected IValueProperty doGetDelegate(Object source) {
		if (source instanceof StyledText)
			return styledText;
		if (source instanceof Text)
			return text;
		throw notSupported(source);
	}
}
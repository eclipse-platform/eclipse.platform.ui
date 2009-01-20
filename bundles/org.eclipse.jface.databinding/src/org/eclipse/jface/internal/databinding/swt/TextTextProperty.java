/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

/**
 * @since 3.3
 * 
 */
public class TextTextProperty extends WidgetStringValueProperty {
	/**
	 * @param event
	 */
	public TextTextProperty(int event) {
		super(checkEvent(event));
	}

	private static int checkEvent(int event) {
		switch (event) {
		case SWT.None:
		case SWT.Modify:
		case SWT.FocusOut:
			return event;
		default:
			throw new IllegalArgumentException("UpdateEventType [" //$NON-NLS-1$
					+ event + "] is not supported."); //$NON-NLS-1$
		}
	}

	String doGetStringValue(Object source) {
		return ((Text) source).getText();
	}

	void doSetStringValue(Object source, String value) {
		((Text) source).setText(value == null ? "" : value); //$NON-NLS-1$
	}

	public String toString() {
		return "Text.text <String>"; //$NON-NLS-1$
	}

	protected ISWTObservableValue wrapObservable(IObservableValue observable,
			Widget widget) {
		return new SWTVetoableValueDecorator(observable, widget);
	}
}

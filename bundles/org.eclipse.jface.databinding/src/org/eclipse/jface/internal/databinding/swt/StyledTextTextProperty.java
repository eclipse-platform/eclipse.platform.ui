/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bug 256543
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Widget;

/**
 * @since 3.3
 * 
 */
public class StyledTextTextProperty extends WidgetStringValueProperty {
	/**
	 * 
	 */
	public StyledTextTextProperty() {
	}

	/**
	 * @param events
	 */
	public StyledTextTextProperty(int[] events) {
		super(checkEvents(events));
	}

	private static int[] checkEvents(int[] events) {
		for (int i = 0; i < events.length; i++)
			checkEvent(events[i]);
		return events;
	}

	private static void checkEvent(int event) {
		if (event != SWT.None && event != SWT.Modify && event != SWT.FocusOut
				&& event != SWT.DefaultSelection)
			throw new IllegalArgumentException("UpdateEventType [" //$NON-NLS-1$
					+ event + "] is not supported."); //$NON-NLS-1$
	}

	String doGetStringValue(Object source) {
		return ((StyledText) source).getText();
	}

	void doSetStringValue(Object source, String value) {
		((StyledText) source).setText(value == null ? "" : value); //$NON-NLS-1$
	}

	public String toString() {
		return "StyledText.text <String>"; //$NON-NLS-1$
	}

	protected ISWTObservableValue wrapObservable(IObservableValue observable,
			Widget widget) {
		return new SWTVetoableValueDecorator(widget, this, observable);
	}
}

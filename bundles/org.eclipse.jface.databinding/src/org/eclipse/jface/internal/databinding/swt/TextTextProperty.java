/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bugs 256543, 262287
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
	 * 
	 */
	public TextTextProperty() {
		this(null);
	}

	/**
	 * @param events
	 */
	public TextTextProperty(int[] events) {
		super(checkEvents(events), staleEvents(events));
	}

	private static int[] checkEvents(int[] events) {
		if (events != null)
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

	private static int[] staleEvents(int[] changeEvents) {
		if (changeEvents != null)
			for (int i = 0; i < changeEvents.length; i++)
				if (changeEvents[i] == SWT.Modify)
					return null;
		return new int[] { SWT.Modify };
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
		return new SWTVetoableValueDecorator(widget, this, observable);
	}
}

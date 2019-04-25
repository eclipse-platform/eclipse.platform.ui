/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
public class TextTextProperty extends WidgetStringValueProperty<Text> {
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
			for (int event : events)
				checkEvent(event);
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
			for (int changeEvent : changeEvents)
				if (changeEvent == SWT.Modify)
					return null;
		return new int[] { SWT.Modify };
	}

	@Override
	String doGetStringValue(Text source) {
		return source.getText();
	}

	@Override
	void doSetStringValue(Text source, String value) {
		source.setText(value == null ? "" : value); //$NON-NLS-1$
	}

	@Override
	public String toString() {
		return "Text.text <String>"; //$NON-NLS-1$
	}

	@Override
	protected ISWTObservableValue<String> wrapObservable(IObservableValue<String> observable, Widget widget) {
		return new SWTVetoableValueDecorator(widget, this, observable);
	}
}

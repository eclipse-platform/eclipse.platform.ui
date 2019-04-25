/*******************************************************************************
 * Copyright (c) 2009, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 264286)
 *******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

/**
 * @param <S> type of the source object
 *
 * @since 3.3
 *
 */
public class WidgetTextWithEventsProperty<S extends Widget> extends WidgetDelegatingValueProperty<S, String> {
	private final int[] events;

	private IValueProperty<S, String> styledText;
	private IValueProperty<S, String> text;

	/**
	 * @param events
	 */
	public WidgetTextWithEventsProperty(int[] events) {
		super(String.class);
		this.events = checkEvents(events);
	}

	private static int[] checkEvents(int[] events) {
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

	@SuppressWarnings("unchecked")
	@Override
	protected IValueProperty<S, String> doGetDelegate(S source) {
		if (source instanceof StyledText) {
			if (styledText == null)
				styledText = (IValueProperty<S, String>) new StyledTextTextProperty(events);
			return styledText;
		}
		if (source instanceof Text) {
			if (text == null)
				text = (IValueProperty<S, String>) new TextTextProperty(events);
			return text;
		}
		throw notSupported(source);
	}
}
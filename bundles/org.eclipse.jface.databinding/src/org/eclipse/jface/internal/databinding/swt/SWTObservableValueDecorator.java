/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bug 281723
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.core.databinding.observable.value.DecoratingObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

/**
 * @since 3.3
 * 
 */
public class SWTObservableValueDecorator extends DecoratingObservableValue
		implements ISWTObservableValue, Listener {
	private Widget widget;

	/**
	 * @param decorated
	 * @param widget
	 */
	public SWTObservableValueDecorator(IObservableValue decorated, Widget widget) {
		super(decorated, true);
		this.widget = widget;
		WidgetListenerUtil.asyncAddListener(widget, SWT.Dispose, this);
	}

	public void handleEvent(Event event) {
		if (event.type == SWT.Dispose)
			dispose();
	}

	public Widget getWidget() {
		return widget;
	}

	public synchronized void dispose() {
		if (widget != null) {
			WidgetListenerUtil.asyncRemoveListener(widget, SWT.Dispose, this);
			widget = null;
		}
		super.dispose();
	}
}

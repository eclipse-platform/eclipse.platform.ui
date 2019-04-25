/*******************************************************************************
 * Copyright (c) 2007, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 		Matthew Hall - initial API and implementation (bug 180746)
 * 		Boris Bokowski, IBM - initial API and implementation
 * 		Matthew Hall - bugs 212223, 208332, 245647, 281723
 *  	Will Horn - bug 215297
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IVetoableValue;
import org.eclipse.core.databinding.observable.value.ValueChangingEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;

/**
 * {@link IObservableValue} implementation that wraps an
 * {@link IObservableValue} and delays notification of value change events from
 * the wrapped observable value until a certain time has passed since the last
 * change event, or until a FocusOut event is received from the underlying
 * widget, if any (whichever happens earlier). This class helps to delay
 * validation until the user stops typing. To notify about pending changes, a
 * delayed observable value will fire a stale event when the wrapped observable
 * value fires a change event, but this change is being delayed.
 *
 * Note that this class will not forward {@link ValueChangingEvent} events from
 * a wrapped {@link IVetoableValue}.
 *
 * @param <T>
 *            the type of value being observed
 *
 * @since 1.2
 */
public class SWTDelayedObservableValueDecorator<T> extends SWTObservableValueDecorator<T> {
	private Control control;

	/**
	 * Constructs a new instance bound to the given
	 * <code>ISWTObservableValue</code> and configured to fire change events
	 * once there have been no value changes in the observable for
	 * <code>delay</code> milliseconds.
	 *
	 * @param decorated
	 * @param widget
	 * @throws IllegalArgumentException
	 *             if <code>updateEventType</code> is an incorrect type.
	 */
	public SWTDelayedObservableValueDecorator(IObservableValue<T> decorated, Widget widget) {
		super(decorated, widget);

		if (widget instanceof Control) {
			control = (Control) widget;
			WidgetListenerUtil.asyncAddListener(control, SWT.FocusOut, this);
		}
	}

	@Override
	public void handleEvent(Event event) {
		// When the control loses focus..
		if (event.type == SWT.FocusOut && isStale())
			getValue(); // short-circuit the delay

		super.handleEvent(event);
	}

	@Override
	public synchronized void dispose() {
		if (control != null) {
			WidgetListenerUtil.asyncRemoveListener(control, SWT.FocusOut, this);
			control = null;
		}
		super.dispose();
	}
}

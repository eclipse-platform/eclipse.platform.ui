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

package org.eclipse.jface.databinding.swt;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IVetoableValue;
import org.eclipse.core.databinding.observable.value.ValueChangingEvent;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.swt.widgets.Widget;

/**
 * {@link IValueProperty} for observing an SWT Widget
 *
 * @param <S> type of the source widget
 * @param <T> type of the value of the property
 *
 * @since 1.3
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IWidgetValueProperty<S extends Widget, T> extends IValueProperty<S, T> {
	/**
	 * Returns an {@link ISWTObservableValue} observing this value property on
	 * the given widget
	 *
	 * @param widget
	 *            the source widget
	 * @return an observable value observing this value property on the given
	 *         widget
	 */
	@Override
	public ISWTObservableValue<T> observe(S widget);

	/**
	 * Returns an {@link ISWTObservableValue} observing this value property on the
	 * given widget, which delays notification of value changes until at least
	 * <code>delay</code> milliseconds have elapsed since that last change event, or
	 * until a FocusOut event is received from the widget (whichever happens first).
	 * <p>
	 * This observable helps to boost performance in situations where an observable
	 * has computationally expensive listeners (e.g. changing filters in a viewer)
	 * or many dependencies (master fields with multiple detail fields). A common
	 * use of this observable is to delay validation of user input until the user
	 * stops typing in a UI field.
	 * <p>
	 * To notify about pending changes, the returned observable fires a stale event
	 * when the wrapped observable value fires a change event, and remains stale
	 * until the delay has elapsed and the value change is fired. A call to
	 * {@link IObservableValue#getValue} while a value change is pending will fire
	 * the value change immediately, short-circuiting the delay.
	 * <p>
	 * Only updates resulting from the observed widget are delayed. Calls directly
	 * to {@link IObservableValue#setValue} are not, and they cancel pending delayed
	 * values.
	 * <p>
	 * Note that this observable will not forward {@link ValueChangingEvent} events
	 * from a wrapped {@link IVetoableValue}.
	 * <p>
	 * This method is equivalent to
	 * <code>SWTObservables.observeDelayedValue(delay, observe(widget))</code>.
	 *
	 * @param delay  the delay in milliseconds.
	 * @param widget the source widget
	 * @return an observable value observing this value property on the given
	 *         widget, and which delays change notifications for <code>delay</code>
	 *         milliseconds.
	 */
	public ISWTObservableValue<T> observeDelayed(int delay, S widget);
}

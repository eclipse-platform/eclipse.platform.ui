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

package org.eclipse.jface.databinding.swt;

import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.swt.widgets.Widget;

/**
 * {@link IValueProperty} for observing an SWT Widget
 * 
 * @since 1.3
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IWidgetValueProperty extends IValueProperty {
	/**
	 * Returns an {@link ISWTObservableValue} observing this value property on
	 * the given widget
	 * 
	 * @param widget
	 *            the source widget
	 * @return an observable value observing this value property on the given
	 *         widget
	 */
	public ISWTObservableValue observe(Widget widget);

	/**
	 * Returns an {@link ISWTObservableValue} observing this value property on
	 * the given widget, which delays notification of value changes until at
	 * least <code>delay</code> milliseconds have elapsed since that last change
	 * event, or until a FocusOut event is received from the widget (whichever
	 * happens first).
	 * <p>
	 * This method is equivalent to
	 * <code>SWTObservables.observeDelayedValue(delay, observe(widget))</code>.
	 * 
	 * @param delay
	 *            the delay in milliseconds.
	 * @param widget
	 *            the source widget
	 * @return an observable value observing this value property on the given
	 *         widget, and which delays change notifications for
	 *         <code>delay</code> milliseconds.
	 */
	public ISWTObservableValue observeDelayed(int delay, Widget widget);
}

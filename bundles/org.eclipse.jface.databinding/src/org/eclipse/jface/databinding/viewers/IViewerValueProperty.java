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

package org.eclipse.jface.databinding.viewers;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.jface.viewers.Viewer;

/**
 * {@link IValueProperty} for observing a JFace viewer
 *
 * @param <S>
 *            type of the source object
 * @param <T>
 *            type of the value of the property
 *
 * @since 1.3
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IViewerValueProperty<S, T> extends IValueProperty<S, T> {
	/**
	 * Returns an {@link IViewerObservableValue} observing this value property
	 * on the given viewer
	 *
	 * @param viewer
	 *            the source viewer
	 * @return an observable value observing this value property on the given
	 *         viewer
	 */
	public IViewerObservableValue<T> observe(Viewer viewer);

	/**
	 * This method is redeclared to trigger ambiguous method errors that are hidden
	 * by a suspected Eclipse compiler bug 536911. By triggering the bug in this way
	 * clients avoid a change of behavior when the bug is fixed. When the bug is
	 * fixed this redeclaration should be removed.
	 */
	@Override
	public IObservableValue<T> observe(S viewer);

	/**
	 * Returns an {@link IViewerObservableValue} observing this value property
	 * on the given viewer, which delays notification of value changes until at
	 * least <code>delay</code> milliseconds have elapsed since that last change
	 * event, or until a FocusOut event is received from the viewer's control
	 * (whichever happens first).
	 * <p>
	 * This method is equivalent to
	 * <code>ViewersObservables.observeDelayedValue(delay, observe(viewer))</code>.
	 *
	 * @param delay
	 *            the delay in milliseconds.
	 * @param viewer
	 *            the source viewer
	 * @return an observable value observing this value property on the given
	 *         viewer, and which delays change notifications for
	 *         <code>delay</code> milliseconds.
	 */
	public IViewerObservableValue<T> observeDelayed(int delay, Viewer viewer);
}

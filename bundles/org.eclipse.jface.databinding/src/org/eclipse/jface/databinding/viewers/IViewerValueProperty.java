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

package org.eclipse.jface.databinding.viewers;

import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.jface.viewers.Viewer;

/**
 * {@link IValueProperty} for observing a JFace viewer
 * 
 * @since 1.3
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IViewerValueProperty extends IValueProperty {
	/**
	 * Returns an {@link IViewerObservableValue} observing this value property
	 * on the given viewer
	 * 
	 * @param viewer
	 *            the source viewer
	 * @return an observable value observing this value property on the given
	 *         viewer
	 */
	public IViewerObservableValue observe(Viewer viewer);

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
	public IViewerObservableValue observeDelayed(int delay, Viewer viewer);
}

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
 *     Matthew Hall - initial API and implementation (bug 245647)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import org.eclipse.core.databinding.observable.value.DecoratingObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * @param <T>
 *            the type of value being observed
 *
 * @since 3.3
 */
public class ViewerObservableValueDecorator<T> extends DecoratingObservableValue<T>
		implements IViewerObservableValue<T>, Listener {
	private Viewer viewer;

	/**
	 * @param decorated
	 * @param viewer
	 */
	public ViewerObservableValueDecorator(IObservableValue<T> decorated, Viewer viewer) {
		super(decorated, true);
		this.viewer = viewer;
		viewer.getControl().addListener(SWT.Dispose, this);
	}

	@Override
	public void handleEvent(Event event) {
		if (event.type == SWT.Dispose)
			dispose();
	}

	@Override
	public Viewer getViewer() {
		return viewer;
	}

	@Override
	public synchronized void dispose() {
		if (viewer != null) {
			Control control = viewer.getControl();
			if (control != null && !control.isDisposed()) {
				control.removeListener(SWT.Dispose, this);
			}
			viewer = null;
		}
		super.dispose();
	}
}

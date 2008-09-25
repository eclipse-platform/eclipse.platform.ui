/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * @since 3.3
 * 
 */
public class ViewerObservableValueDecorator extends DecoratingObservableValue
		implements IViewerObservableValue, Listener {
	private Viewer viewer;

	/**
	 * @param decorated
	 * @param viewer
	 */
	public ViewerObservableValueDecorator(IObservableValue decorated,
			Viewer viewer) {
		super(decorated, true);
		this.viewer = viewer;
		viewer.getControl().addListener(SWT.Dispose, this);
	}

	public void handleEvent(Event event) {
		if (event.type == SWT.Dispose)
			dispose();
	}

	public Viewer getViewer() {
		return viewer;
	}

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

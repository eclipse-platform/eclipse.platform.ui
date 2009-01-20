/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import org.eclipse.core.databinding.observable.set.DecoratingObservableSet;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.jface.databinding.viewers.IViewerObservableSet;
import org.eclipse.jface.viewers.Viewer;

/**
 * @since 3.3
 * 
 */
public class ViewerObservableSetDecorator extends DecoratingObservableSet
		implements IViewerObservableSet {
	private final Viewer viewer;

	/**
	 * @param decorated
	 * @param viewer
	 */
	public ViewerObservableSetDecorator(IObservableSet decorated, Viewer viewer) {
		super(decorated, true);
		this.viewer = viewer;
	}

	public Viewer getViewer() {
		return viewer;
	}

}

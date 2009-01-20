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

import org.eclipse.core.databinding.observable.list.DecoratingObservableList;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.jface.databinding.viewers.IViewerObservableList;
import org.eclipse.jface.viewers.Viewer;

/**
 * @since 3.3
 * 
 */
public class ViewerObservableListDecorator extends DecoratingObservableList
		implements IViewerObservableList {
	private final Viewer viewer;

	/**
	 * @param decorated
	 * @param viewer
	 */
	public ViewerObservableListDecorator(IObservableList decorated,
			Viewer viewer) {
		super(decorated, true);
		this.viewer = viewer;
	}

	public Viewer getViewer() {
		return viewer;
	}

}

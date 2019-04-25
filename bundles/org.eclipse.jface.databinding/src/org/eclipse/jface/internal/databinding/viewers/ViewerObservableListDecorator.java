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
 *     Matthew Hall - initial API and implementation (bug 194734)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import org.eclipse.core.databinding.observable.list.DecoratingObservableList;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.jface.databinding.viewers.IViewerObservableList;
import org.eclipse.jface.viewers.Viewer;

/**
 * @param <E>
 *            the list element type
 *
 * @since 3.3
 */
public class ViewerObservableListDecorator<E> extends DecoratingObservableList<E> implements IViewerObservableList<E> {
	private final Viewer viewer;

	/**
	 * @param decorated
	 * @param viewer
	 */
	public ViewerObservableListDecorator(IObservableList<E> decorated, Viewer viewer) {
		super(decorated, true);
		this.viewer = viewer;
	}

	@Override
	public Viewer getViewer() {
		return viewer;
	}

}

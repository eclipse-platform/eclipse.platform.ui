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

import org.eclipse.core.databinding.observable.set.DecoratingObservableSet;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.jface.databinding.viewers.IViewerObservableSet;
import org.eclipse.jface.viewers.Viewer;

/**
 * @param <E>
 *            the type of the elements in this set
 *
 * @since 3.3
 */
public class ViewerObservableSetDecorator<E> extends DecoratingObservableSet<E> implements IViewerObservableSet<E> {
	private final Viewer viewer;

	/**
	 * @param decorated
	 * @param viewer
	 */
	public ViewerObservableSetDecorator(IObservableSet<E> decorated, Viewer viewer) {
		super(decorated, true);
		this.viewer = viewer;
	}

	@Override
	public Viewer getViewer() {
		return viewer;
	}

}

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

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.jface.viewers.Viewer;

/**
 * {@link IListProperty} for observing a JFace viewer
 *
 * @param <S>
 *            type of the source object
 * @param <E>
 *            type of the elements in the list
 *
 * @since 1.3
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IViewerListProperty<S, E> extends IListProperty<S, E> {
	/**
	 * Returns an {@link IViewerObservableList} observing this list property on
	 * the given viewer
	 *
	 * @param viewer
	 *            the source viewer
	 * @return an observable list observing this list property on the given
	 *         viewer
	 */
	public IViewerObservableList<E> observe(Viewer viewer);

	/**
	 * This method is redeclared to trigger ambiguous method errors that are hidden
	 * by a suspected Eclipse compiler bug 536911. By triggering the bug in this way
	 * clients avoid a change of behavior when the bug is fixed. When the bug is
	 * fixed this redeclaration should be removed.
	 */
	@Override
	public IObservableList<E> observe(S widget);
}

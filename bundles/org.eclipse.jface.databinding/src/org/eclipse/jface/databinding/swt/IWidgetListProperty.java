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

package org.eclipse.jface.databinding.swt;

import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.swt.widgets.Widget;

/**
 * {@link IListProperty} for observing an SWT Widget
 *
 * @param <S>
 *            type of the source object
 * @param <E>
 *            type of the elements in the list
 *
 * @since 1.3
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IWidgetListProperty<S extends Widget, E> extends IListProperty<S, E> {
	/**
	 * Returns an {@link ISWTObservableList} observing this list property on the
	 * given widget
	 *
	 * @param widget
	 *            the source widget
	 * @return an observable list observing this list property on the given
	 *         widget
	 */
	@Override
	public ISWTObservableList<E> observe(S widget);
}

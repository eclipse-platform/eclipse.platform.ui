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
 *     Matthew Hall - bugs 263413, 264286
 ******************************************************************************/

package org.eclipse.jface.databinding.viewers;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.property.set.SimpleSetProperty;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.internal.databinding.viewers.ViewerObservableSetDecorator;
import org.eclipse.jface.viewers.Viewer;

/**
 * Abstract set property implementation for {@link Viewer} properties. This
 * class implements some basic behavior that viewer properties are generally
 * expected to have, namely:
 * <ul>
 * <li>Calling {@link #observe} should create the observable on the display
 * realm of the viewer's control, rather than the current default realm
 * <li>All <code>observe()</code> methods should return an
 * {@link IViewerObservableSet}
 * </ul>
 *
 * @param <S>
 *            type of the source object
 * @param <E>
 *            type of the elements in the set
 *
 * @since 1.3
 */
public abstract class ViewerSetProperty<S, E> extends SimpleSetProperty<S, E> implements IViewerSetProperty<S, E> {

	@Override
	public IObservableSet<E> observe(S source) {
		if (source instanceof Viewer) {
			return observe((Viewer) source);
		}
		return super.observe(source);
	}

	@Override
	public IObservableSet<E> observe(Realm realm, S source) {
		IObservableSet<E> observable = super.observe(realm, source);
		if (source instanceof Viewer) {
			return new ViewerObservableSetDecorator<>(observable, (Viewer) source);
		}
		return observable;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IViewerObservableSet<E> observe(Viewer viewer) {
		return (IViewerObservableSet<E>) observe(DisplayRealm.getRealm(viewer.getControl().getDisplay()), (S) viewer);
	}

}

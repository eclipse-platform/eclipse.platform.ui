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
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.value.SimpleValueProperty;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.internal.databinding.viewers.ViewerObservableValueDecorator;
import org.eclipse.jface.viewers.Viewer;

/**
 * Abstract value property implementation for {@link Viewer} properties. This
 * class implements some basic behavior that viewer properties are generally
 * expected to have, namely:
 * <ul>
 * <li>Calling {@link #observe(Object)} should create the observable on the
 * display realm of the viewer's control, rather than the current default realm
 * <li>All <code>observe()</code> methods should return an
 * {@link IViewerObservableValue}
 * </ul>
 *
 * @param <S> type of the source object
 * @param <T> type of the value of the property
 *
 * @since 1.3
 */
public abstract class ViewerValueProperty<S, T> extends SimpleValueProperty<S, T>
		implements IViewerValueProperty<S, T> {
	@Override
	public IObservableValue<T> observe(S source) {
		if (source instanceof Viewer) {
			return observe((Viewer) source);
		}
		return super.observe(source);
	}

	@Override
	public IObservableValue<T> observe(Realm realm, S source) {
		IObservableValue<T> observable = super.observe(realm, source);
		if (source instanceof Viewer)
			observable = new ViewerObservableValueDecorator<>(observable, (Viewer) source);
		return observable;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IViewerObservableValue<T> observe(Viewer viewer) {
		return (IViewerObservableValue<T>) observe(DisplayRealm.getRealm(viewer.getControl().getDisplay()), (S) viewer);
	}

	@Override
	public IViewerObservableValue<T> observeDelayed(int delay, Viewer viewer) {
		return ViewersObservables.observeDelayedValue(delay, observe(viewer));
	}
}

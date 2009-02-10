/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bugs 263413, 264286
 ******************************************************************************/

package org.eclipse.jface.databinding.viewers;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.value.SimpleValueProperty;
import org.eclipse.jface.databinding.swt.SWTObservables;
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
 * @since 1.3
 */
public abstract class ViewerValueProperty extends SimpleValueProperty implements
		IViewerValueProperty {
	public IObservableValue observe(Object source) {
		if (source instanceof Viewer) {
			return observe((Viewer) source);
		}
		return super.observe(source);
	}

	public IObservableValue observe(Realm realm, Object source) {
		IObservableValue observable = super.observe(realm, source);
		if (source instanceof Viewer)
			observable = new ViewerObservableValueDecorator(observable,
					(Viewer) source);
		return observable;
	}

	public IViewerObservableValue observe(Viewer viewer) {
		return (IViewerObservableValue) observe(SWTObservables.getRealm(viewer
				.getControl().getDisplay()), viewer);
	}

	public IViewerObservableValue observeDelayed(int delay, Viewer viewer) {
		return ViewersObservables.observeDelayedValue(delay, observe(viewer));
	}
}

/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation
 *     Matthew Hall - bugs 263413, 264286
 ******************************************************************************/

package org.eclipse.jface.databinding.viewers;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.property.list.SimpleListProperty;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.internal.databinding.viewers.ViewerObservableListDecorator;
import org.eclipse.jface.viewers.Viewer;

/**
 * Abstract list property implementation for {@link Viewer} properties. This
 * class implements some basic behavior that viewer properties are generally
 * expected to have, namely:
 * <ul>
 * <li>Calling {@link #observe(Object)} should create the observable on the
 * display realm of the viewer's control, rather than the current default realm
 * <li>All <code>observe()</code> methods should return an
 * {@link IViewerObservableList}
 * </ul>
 * 
 * @since 1.3
 */
public abstract class ViewerListProperty extends SimpleListProperty implements
		IViewerListProperty {
	public IObservableList observe(Object source) {
		if (source instanceof Viewer) {
			return observe((Viewer) source);
		}
		return super.observe(source);
	}

	public IObservableList observe(Realm realm, Object source) {
		IObservableList observable = super.observe(realm, source);
		if (source instanceof Viewer)
			observable = new ViewerObservableListDecorator(observable,
					(Viewer) source);
		return observable;
	}

	public IViewerObservableList observe(Viewer viewer) {
		return (IViewerObservableList) observe(SWTObservables.getRealm(viewer
				.getControl().getDisplay()), viewer);
	}
}

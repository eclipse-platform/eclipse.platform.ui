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

package org.eclipse.jface.databinding.swt;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.property.list.SimpleListProperty;
import org.eclipse.jface.internal.databinding.swt.SWTObservableListDecorator;
import org.eclipse.swt.widgets.Widget;

/**
 * Abstract list property implementation for {@link Widget} properties. This
 * class implements some basic behavior that widget properties are generally
 * expected to have, namely:
 * <ul>
 * <li>Calling {@link #observe(Object)} should create the observable on the
 * display realm of the widget, rather than the current default realm
 * <li>All <code>observe()</code> methods should return an
 * {@link ISWTObservable}
 * </ul>
 * 
 * @since 1.3
 */
public abstract class WidgetListProperty extends SimpleListProperty implements
		IWidgetListProperty {
	public IObservableList observe(Object source) {
		if (source instanceof Widget) {
			return observe((Widget) source);
		}
		return super.observe(source);
	}

	public IObservableList observe(Realm realm, Object source) {
		return new SWTObservableListDecorator(super.observe(realm, source),
				(Widget) source);
	}

	public ISWTObservableList observe(Widget widget) {
		return (ISWTObservableList) observe(SWTObservables.getRealm(widget
				.getDisplay()), widget);
	}
}
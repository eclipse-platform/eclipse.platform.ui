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

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.property.list.SimpleListProperty;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.widgets.Widget;

/**
 * @since 3.3
 * 
 */
public abstract class WidgetListProperty extends SimpleListProperty {
	public IObservableList observe(Object source) {
		if (source instanceof Widget) {
			return observe(SWTObservables.getRealm(((Widget) source)
					.getDisplay()), source);
		}
		return super.observe(source);
	}

	public IObservableList observe(Realm realm, Object source) {
		return new SWTObservableListDecorator(super.observe(realm, source), (Widget) source);
	}
}

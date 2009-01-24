/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 259380)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.property.set.SetProperty;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * @since 3.3
 * 
 */
public class CheckableCheckedElementsProperty extends SetProperty {
	private final Object elementType;

	/**
	 * @param elementType
	 */
	public CheckableCheckedElementsProperty(Object elementType) {
		this.elementType = elementType;
	}

	public Object getElementType() {
		return elementType;
	}

	public IObservableSet observe(Object source) {
		if (source instanceof Viewer) {
			return observe(SWTObservables.getRealm(((Viewer) source)
					.getControl().getDisplay()), source);
		}
		return super.observe(source);
	}

	public IObservableSet observe(Realm realm, Object source) {
		Set wrappedSet = new HashSet();
		if (source instanceof StructuredViewer)
			wrappedSet = ViewerElementSet
					.withComparer(((StructuredViewer) source).getComparer());
		IObservableSet observable = new CheckableCheckedElementsObservableSet(
				realm, wrappedSet, elementType, (ICheckable) source);
		if (source instanceof Viewer)
			observable = new ViewerObservableSetDecorator(observable,
					(Viewer) source);
		return observable;
	}
}

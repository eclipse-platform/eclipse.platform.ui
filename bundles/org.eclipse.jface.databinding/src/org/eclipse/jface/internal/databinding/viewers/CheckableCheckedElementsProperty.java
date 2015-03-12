/*******************************************************************************
 * Copyright (c) 2009, 2010 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 259380)
 *     Matthew Hall - bug 283204
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.databinding.property.set.SetProperty;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.jface.viewers.IElementComparer;
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

	@Override
	public Object getElementType() {
		return elementType;
	}

	@Override
	protected Set doGetSet(Object source) {
		throw new UnsupportedOperationException(
				"Cannot query the checked elements on an ICheckable"); //$NON-NLS-1$
	}

	@Override
	protected void doSetSet(Object source, Set set) {
		throw new UnsupportedOperationException(
				"Cannot batch replace the checked elements on an ICheckable.  " + //$NON-NLS-1$
						"Use updateSet(SetDiff) instead"); //$NON-NLS-1$
	}

	@Override
	protected void doUpdateSet(Object source, SetDiff diff) {
		ICheckable checkable = (ICheckable) source;
		for (Iterator it = diff.getAdditions().iterator(); it.hasNext();)
			checkable.setChecked(it.next(), true);
		for (Iterator it = diff.getRemovals().iterator(); it.hasNext();)
			checkable.setChecked(it.next(), false);
	}

	@Override
	public IObservableSet observe(Object source) {
		if (source instanceof Viewer) {
			return observe(DisplayRealm.getRealm(((Viewer) source)
					.getControl().getDisplay()), source);
		}
		return super.observe(source);
	}

	@Override
	public IObservableSet observe(Realm realm, Object source) {
		IElementComparer comparer = null;
		if (source instanceof StructuredViewer)
			comparer = ((StructuredViewer) source).getComparer();
		Set wrappedSet = ViewerElementSet.withComparer(comparer);
		IObservableSet observable = new CheckableCheckedElementsObservableSet(
				realm, wrappedSet, elementType, comparer, (ICheckable) source);
		if (source instanceof Viewer)
			observable = new ViewerObservableSetDecorator(observable,
					(Viewer) source);
		return observable;
	}
}

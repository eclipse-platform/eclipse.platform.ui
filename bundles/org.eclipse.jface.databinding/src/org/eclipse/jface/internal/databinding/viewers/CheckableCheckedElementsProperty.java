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
 *     Matthew Hall - initial API and implementation (bug 259380)
 *     Matthew Hall - bug 283204
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

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
 * @param <S> type of the source object
 * @param <E> type of the elements in the set
 *
 * @since 3.3
 */
public class CheckableCheckedElementsProperty<S extends ICheckable, E> extends SetProperty<S, E> {
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
	protected Set<E> doGetSet(S source) {
		throw new UnsupportedOperationException(
				"Cannot query the checked elements on an ICheckable"); //$NON-NLS-1$
	}

	@Override
	protected void doSetSet(S source, Set<E> set) {
		throw new UnsupportedOperationException(
				"Cannot batch replace the checked elements on an ICheckable.  " + //$NON-NLS-1$
						"Use updateSet(SetDiff) instead"); //$NON-NLS-1$
	}

	@Override
	protected void doUpdateSet(S source, SetDiff<E> diff) {
		for (Object e : diff.getAdditions())
			source.setChecked(e, true);
		for (Object e : diff.getRemovals())
			source.setChecked(e, false);
	}

	@Override
	public IObservableSet<E> observe(S source) {
		if (source instanceof Viewer) {
			return observe(DisplayRealm.getRealm(((Viewer) source).getControl().getDisplay()), source);
		}
		return super.observe(source);
	}

	@Override
	public IObservableSet<E> observe(Realm realm, S source) {
		IElementComparer comparer = null;
		if (source instanceof StructuredViewer)
			comparer = ((StructuredViewer) source).getComparer();
		Set<E> wrappedSet = ViewerElementSet.withComparer(comparer);
		IObservableSet<E> observable = new CheckableCheckedElementsObservableSet<>(realm, wrappedSet, elementType,
				comparer, source);
		if (source instanceof Viewer)
			observable = new ViewerObservableSetDecorator<>(observable, (Viewer) source);
		return observable;
	}
}

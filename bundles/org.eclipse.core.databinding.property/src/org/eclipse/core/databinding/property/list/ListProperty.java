/*******************************************************************************
 * Copyright (c) 2008, 2010 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bug 195222
 *     Ovidio Mallo - bug 331348
 ******************************************************************************/

package org.eclipse.core.databinding.property.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.masterdetail.MasterDetailObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.internal.databinding.property.ListPropertyDetailValuesList;

/**
 * Abstract implementation of IListProperty.
 * 
 * @since 1.2
 */
public abstract class ListProperty implements IListProperty {

	/**
	 * By default, this method returns <code>Collections.EMPTY_LIST</code> in
	 * case the source object is <code>null</code>. Otherwise, this method
	 * delegates to {@link #doGetList(Object)}.
	 * 
	 * <p>
	 * Clients may override this method if they e.g. want to return a specific
	 * default list in case the source object is <code>null</code>.
	 * </p>
	 * 
	 * @see #doGetList(Object)
	 * 
	 * @since 1.3
	 */
	public List getList(Object source) {
		if (source == null) {
			return Collections.EMPTY_LIST;
		}
		return Collections.unmodifiableList(doGetList(source));
	}

	/**
	 * Returns a List with the current contents of the source's list property
	 * 
	 * @param source
	 *            the property source
	 * @return a List with the current contents of the source's list property
	 * @noreference This method is not intended to be referenced by clients.
	 * @since 1.3
	 */
	protected List doGetList(Object source) {
		IObservableList observable = observe(source);
		try {
			return new ArrayList(observable);
		} finally {
			observable.dispose();
		}
	}

	/**
	 * @since 1.3
	 */
	public final void setList(Object source, List list) {
		if (source != null) {
			doSetList(source, list);
		}
	}

	/**
	 * Updates the property on the source with the specified change.
	 * 
	 * @param source
	 *            the property source
	 * @param list
	 *            the new list
	 * @since 1.3
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected void doSetList(Object source, List list) {
		doUpdateList(source, Diffs.computeListDiff(doGetList(source), list));
	}

	/**
	 * @since 1.3
	 */
	public final void updateList(Object source, ListDiff diff) {
		if (source != null) {
			doUpdateList(source, diff);
		}
	}

	/**
	 * Updates the property on the source with the specified change
	 * 
	 * @param source
	 *            the property source
	 * @param diff
	 *            a diff describing the change
	 * @since 1.3
	 */
	protected void doUpdateList(Object source, ListDiff diff) {
		IObservableList observable = observe(source);
		try {
			diff.applyTo(observable);
		} finally {
			observable.dispose();
		}
	}

	public IObservableList observe(Object source) {
		return observe(Realm.getDefault(), source);
	}

	public IObservableFactory listFactory() {
		return new IObservableFactory() {
			public IObservable createObservable(Object target) {
				return observe(target);
			}
		};
	}

	public IObservableFactory listFactory(final Realm realm) {
		return new IObservableFactory() {
			public IObservable createObservable(Object target) {
				return observe(realm, target);
			}
		};
	}

	public IObservableList observeDetail(IObservableValue master) {
		return MasterDetailObservables.detailList(master,
				listFactory(master.getRealm()), getElementType());
	}

	public final IListProperty values(IValueProperty detailValue) {
		return new ListPropertyDetailValuesList(this, detailValue);
	}
}

/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bug 195222
 *     Ovidio Mallo - bug 331348
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 ******************************************************************************/

package org.eclipse.core.databinding.property.set;

import java.util.Collections;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.masterdetail.MasterDetailObservables;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.map.IMapProperty;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.internal.databinding.identity.IdentitySet;
import org.eclipse.core.internal.databinding.property.SetPropertyDetailValuesMap;

/**
 * Abstract implementation of ISetProperty
 *
 * @param <S>
 *            type of the source object
 * @param <E>
 *            type of the elements in the set
 * @since 1.2
 */
public abstract class SetProperty<S, E> implements ISetProperty<S, E> {

	/**
	 * By default, this method returns <code>Collections.EMPTY_SET</code> in
	 * case the source object is <code>null</code>. Otherwise, this method
	 * delegates to {@link #doGetSet(Object)}.
	 *
	 * <p>
	 * Clients may override this method if they e.g. want to return a specific
	 * default set in case the source object is <code>null</code>.
	 * </p>
	 *
	 * @see #doGetSet(Object)
	 *
	 * @since 1.3
	 */
	@Override
	public Set<E> getSet(S source) {
		if (source == null) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(doGetSet(source));
	}

	/**
	 * Returns a Set with the current contents of the source's set property
	 *
	 * @param source
	 *            the property source
	 * @return a Set with the current contents of the source's set property
	 * @since 1.6
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected Set<E> doGetSet(S source) {
		IObservableSet<E> observable = observe(source);
		try {
			return new IdentitySet<>(observable);
		} finally {
			observable.dispose();
		}
	}

	/**
	 * @since 1.3
	 */
	@Override
	public final void setSet(S source, Set<E> set) {
		if (source != null) {
			doSetSet(source, set);
		}
	}

	/**
	 * Updates the property on the source with the specified change.
	 *
	 * @param source
	 *            the property source
	 * @param set
	 *            the new set
	 * @since 1.6
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected void doSetSet(S source, Set<E> set) {
		doUpdateSet(source, Diffs.computeSetDiff(doGetSet(source), set));
	}

	/**
	 * @since 1.3
	 */
	@Override
	public final void updateSet(S source, SetDiff<E> diff) {
		if (source != null && !diff.isEmpty()) {
			doUpdateSet(source, diff);
		}
	}

	/**
	 * Updates the property on the source with the specified change.
	 *
	 * @param source
	 *            the property source
	 * @param diff
	 *            a diff describing the change
	 * @since 1.6
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected void doUpdateSet(S source, SetDiff<E> diff) {
		IObservableSet<E> observable = observe(source);
		try {
			diff.applyTo(observable);
		} finally {
			observable.dispose();
		}
	}

	@Override
	public IObservableSet<E> observe(S source) {
		return observe(Realm.getDefault(), source);
	}

	@Override
	public IObservableFactory<S, IObservableSet<E>> setFactory() {
		return new IObservableFactory<S, IObservableSet<E>>() {
			@Override
			public IObservableSet<E> createObservable(S target) {
				return observe(target);
			}
		};
	}

	@Override
	public IObservableFactory<S, IObservableSet<E>> setFactory(final Realm realm) {
		return new IObservableFactory<S, IObservableSet<E>>() {
			@Override
			public IObservableSet<E> createObservable(S target) {
				return observe(realm, target);
			}
		};
	}

	@Override
	public <U extends S> IObservableSet<E> observeDetail(IObservableValue<U> master) {
		return MasterDetailObservables.detailSet(master, setFactory(master.getRealm()), getElementType());
	}

	@Override
	public final <T> IMapProperty<S, E, T> values(IValueProperty<? super E, T> detailValues) {
		return new SetPropertyDetailValuesMap<S, E, T>(this, detailValues);
	}
}

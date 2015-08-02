/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 247997)
 *     Matthew Hall - bug 264306
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 ******************************************************************************/

package org.eclipse.core.databinding.property.set;

import java.util.Collections;
import java.util.Set;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;

/**
 * @param <S>
 *            type of the source object
 * @param <E>
 *            type of the elements in the set
 * @since 1.2
 *
 */
public abstract class DelegatingSetProperty<S, E> extends SetProperty<S, E> {
	private final Object elementType;
	private final ISetProperty<S, E> nullProperty = new NullSetProperty();

	protected DelegatingSetProperty() {
		this(null);
	}

	protected DelegatingSetProperty(Object elementType) {
		this.elementType = elementType;
	}

	/**
	 * Returns the property to delegate to for the specified source object.
	 * Repeated calls to this method with the same source object returns the
	 * same delegate instance.
	 *
	 * @param source
	 *            the property source (may be null)
	 * @return the property to delegate to for the specified source object.
	 */
	protected final ISetProperty<S, E> getDelegate(S source) {
		if (source == null)
			return nullProperty;
		ISetProperty<S, E> delegate = doGetDelegate(source);
		if (delegate == null)
			delegate = nullProperty;
		return delegate;
	}

	/**
	 * Returns the property to delegate to for the specified source object.
	 * Implementers must ensure that repeated calls to this method with the same
	 * source object returns the same delegate instance.
	 *
	 * @param source
	 *            the property source
	 * @return the property to delegate to for the specified source object.
	 */
	protected abstract ISetProperty<S, E> doGetDelegate(S source);

	@Override
	public Object getElementType() {
		return elementType;
	}

	@Override
	protected Set<E> doGetSet(S source) {
		return getDelegate(source).getSet(source);
	}

	@Override
	protected void doSetSet(S source, Set<E> set) {
		getDelegate(source).setSet(source, set);
	}

	@Override
	protected void doUpdateSet(S source, SetDiff<E> diff) {
		getDelegate(source).updateSet(source, diff);
	}

	@Override
	public IObservableSet<E> observe(S source) {
		return getDelegate(source).observe(source);
	}

	@Override
	public IObservableSet<E> observe(Realm realm, S source) {
		return getDelegate(source).observe(realm, source);
	}

	private class NullSetProperty extends SimpleSetProperty<S, E> {
		@Override
		public Object getElementType() {
			return elementType;
		}

		@Override
		protected Set<E> doGetSet(S source) {
			return Collections.emptySet();
		}

		@Override
		protected void doSetSet(S source, Set<E> set, SetDiff<E> diff) {
		}

		@Override
		protected void doSetSet(S source, Set<E> set) {
		}

		@Override
		protected void doUpdateSet(S source, SetDiff<E> diff) {
		}

		@Override
		public INativePropertyListener<S> adaptListener(ISimplePropertyListener<S, SetDiff<E>> listener) {
			return null;
		}
	}
}

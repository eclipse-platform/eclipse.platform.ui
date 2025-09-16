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
 *     Matthew Hall - initial API and implementation (bug 265727)
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 ******************************************************************************/

package org.eclipse.core.databinding.property.set;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.databinding.observable.set.UnionSet;
import org.eclipse.core.internal.databinding.property.PropertyObservableUtil;

/**
 * A set property for observing the union of multiple set properties a combined
 * set.
 *
 * @param <S>
 *            type of the source object
 * @param <E>
 *            type of the elements in the set
 * @since 1.2
 */
public class UnionSetProperty<S, E> extends SetProperty<S, E> {
	private final ISetProperty<S, E>[] properties;
	private final Object elementType;

	/**
	 * @param properties the property sets to unify
	 */
	public UnionSetProperty(ISetProperty<S, E>[] properties) {
		this(properties, null);
	}

	/**
	 * @param properties  the property sets to unify
	 * @param elementType the element type or <code>null</code>
	 */
	public UnionSetProperty(ISetProperty<S, E>[] properties, Object elementType) {
		this.properties = properties;
		this.elementType = elementType;
	}

	@Override
	public Object getElementType() {
		return elementType;
	}

	@Override
	protected Set<E> doGetSet(S source) {
		Set<E> set = new HashSet<>();
		for (ISetProperty<S, E> property : properties) {
			set.addAll(property.getSet(source));
		}
		return set;
	}

	@Override
	protected void doSetSet(S source, Set<E> set) {
		throw new UnsupportedOperationException("UnionSetProperty is unmodifiable"); //$NON-NLS-1$
	}

	@Override
	protected void doUpdateSet(S source, SetDiff<E> diff) {
		throw new UnsupportedOperationException("UnionSetProperty is unmodifiable"); //$NON-NLS-1$
	}

	@Override
	public IObservableSet<E> observe(Realm realm, S source) {
		Set<IObservableSet<? extends E>> sets = new HashSet<>(properties.length);
		for (ISetProperty<S, E> property : properties) {
			sets.add(property.observe(realm, source));
		}
		IObservableSet<E> unionSet = new UnionSet<>(sets, elementType);

		for (IObservableSet<? extends E> set : sets) {
			PropertyObservableUtil.cascadeDispose(unionSet, set);
		}

		return unionSet;
	}
}

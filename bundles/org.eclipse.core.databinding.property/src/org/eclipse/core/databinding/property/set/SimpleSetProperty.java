/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation
 *     Matthew Hall - bugs 195222, 247997, 265561
 *     Ovidio Mallo - bug 301774
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 ******************************************************************************/

package org.eclipse.core.databinding.property.set;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.internal.databinding.property.set.SimplePropertyObservableSet;

/**
 * Simplified abstract implementation of ISetProperty. This class takes care of
 * most of the functional requirements for an ISetProperty implementation,
 * leaving only the property-specific details to subclasses.
 * <p>
 * Subclasses must implement these methods:
 * <ul>
 * <li> {@link #getElementType()}
 * <li> {@link #doGetSet(Object)}
 * <li> {@link #doSetSet(Object, Set, SetDiff)}
 * <li> {@link #adaptListener(ISimplePropertyListener)}
 * </ul>
 * <p>
 * In addition, we recommended overriding {@link #toString()} to return a
 * description suitable for debugging purposes.
 *
 * @param <S>
 *            type of the source object
 * @param <E>
 *            type of the elements in the set
 * @since 1.2
 */
public abstract class SimpleSetProperty<S, E> extends SetProperty<S, E> {
	@Override
	public IObservableSet<E> observe(Realm realm, S source) {
		return new SimplePropertyObservableSet<>(realm, source, this);
	}

	// Accessors

	@Override
	protected abstract Set<E> doGetSet(S source);

	// Mutators

	/**
	 * Updates the property on the source with the specified change.
	 *
	 * @param source
	 *            the property source
	 * @param set
	 *            the new set
	 * @param diff
	 *            a diff describing the change
	 * @noreference This method is not intended to be referenced by clients.
	 * @since 1.6
	 */
	public final void setSet(S source, Set<E> set, SetDiff<E> diff) {
		if (source != null && !diff.isEmpty()) {
			doSetSet(source, set, diff);
		}
	}

	/**
	 * Updates the property on the source with the specified change.
	 *
	 * @param source
	 *            the property source
	 * @param set
	 *            the new set
	 * @param diff
	 *            a diff describing the change
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected abstract void doSetSet(S source, Set<E> set, SetDiff<E> diff);

	@Override
	protected void doSetSet(S source, Set<E> set) {
		SetDiff<E> diff = Diffs.computeLazySetDiff(doGetSet(source), set);
		doSetSet(source, set, diff);
	}

	@Override
	protected void doUpdateSet(S source, SetDiff<E> diff) {
		Set<E> set = new HashSet<>(doGetSet(source));
		diff.applyTo(set);
		doSetSet(source, set, diff);
	}

	// Listeners

	/**
	 * Returns a listener capable of adding or removing itself as a listener on
	 * a source object using the the source's "native" listener API. Events
	 * received from the source objects are parlayed to the specified listener
	 * argument.
	 * <p>
	 * This method returns null if the source object has no listener APIs for
	 * this property.
	 *
	 * @param listener
	 *            the property listener to receive events
	 * @return a native listener which parlays property change events to the
	 *         specified listener, or null if the source object has no listener
	 *         APIs for this property.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public abstract INativePropertyListener<S> adaptListener(ISimplePropertyListener<S, SetDiff<E>> listener);
}

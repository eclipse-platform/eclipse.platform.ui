/*******************************************************************************
 * Copyright (c) 2008, 2010 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bugs 195222, 247997, 265561
 *     Ovidio Mallo - bug 301774
 ******************************************************************************/

package org.eclipse.core.databinding.property.list;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.internal.databinding.property.list.SimplePropertyObservableList;

/**
 * Simplified abstract implementation of IListProperty. This class takes care of
 * most of the functional requirements for an IListProperty implementation,
 * leaving only the property-specific details to subclasses.
 * <p>
 * Subclasses must implement these methods:
 * <ul>
 * <li> {@link #getElementType()}
 * <li> {@link #doGetList(Object)}
 * <li> {@link #doSetList(Object, List, ListDiff)}
 * <li> {@link #adaptListener(ISimplePropertyListener)}
 * </ul>
 * <p>
 * In addition, we recommended overriding {@link #toString()} to return a
 * description suitable for debugging purposes.
 * 
 * @since 1.2
 */
public abstract class SimpleListProperty extends ListProperty {
	public IObservableList observe(Realm realm, Object source) {
		return new SimplePropertyObservableList(realm, source, this);
	}

	// Accessors

	protected abstract List doGetList(Object source);

	// Mutators

	/**
	 * Updates the property on the source with the specified change.
	 * 
	 * @param source
	 *            the property source
	 * @param list
	 *            the new list
	 * @param diff
	 *            a diff describing the change
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public final void setList(Object source, List list, ListDiff diff) {
		if (source != null && !diff.isEmpty()) {
			doSetList(source, list, diff);
		}
	}

	/**
	 * Updates the property on the source with the specified change.
	 * 
	 * @param source
	 *            the property source
	 * @param list
	 *            the new list
	 * @param diff
	 *            a diff describing the change
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected abstract void doSetList(Object source, List list, ListDiff diff);

	protected void doSetList(Object source, List list) {
		ListDiff diff = Diffs.computeLazyListDiff(doGetList(source), list);
		doSetList(source, list, diff);
	}

	protected void doUpdateList(Object source, ListDiff diff) {
		List list = new ArrayList(doGetList(source));
		diff.applyTo(list);
		doSetList(source, list, diff);
	}

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
	public abstract INativePropertyListener adaptListener(
			ISimplePropertyListener listener);
}

/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.databinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.internal.databinding.BindingMessages;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

/**
 * This class can be used to aggregate status values from a data binding context
 * into a single status value. Instances of this class can be used as an
 * observable value with a value type of {@link IStatus}, or the static methods
 * can be called directly if an aggregated status result is only needed once.
 * 
 * @since 1.0
 * 
 */
public final class AggregateValidationStatus implements IObservableValue {

	private IObservableValue implementation;

	/**
	 * Constant denoting an aggregation strategy that merges multiple non-OK
	 * status objects in a {@link MultiStatus}. Returns an OK status result if
	 * all statuses from the given bindings are the an OK status. Returns a
	 * single status if there is only one non-OK status.
	 * 
	 * @see #getStatusMerged(Collection)
	 */
	public static final int MERGED = 1;

	/**
	 * Constant denoting an aggregation strategy that always returns the most
	 * severe status from the given bindings. If there is more than one status
	 * at the same severity level, it picks the first one it encounters.
	 * 
	 * @see #getStatusMaxSeverity(Collection)
	 */
	public static final int MAX_SEVERITY = 2;

	/**
	 * @param bindings
	 *            an observable collection containing elements of type IStatus
	 * @param strategy
	 *            a strategy constant, one of {@link #MERGED} or
	 *            {@link #MAX_SEVERITY}.
	 */
	public AggregateValidationStatus(final IObservableCollection bindings,
			int strategy) {
		this(Realm.getDefault(), bindings, strategy);
	}
	
	/**
	 * @param realm
	 * 			  Realm
	 * @param bindings
	 *            an observable collection containing elements of type IStatus
	 * @param strategy
	 *            a strategy constant, one of {@link #MERGED} or
	 *            {@link #MAX_SEVERITY}.
	 */
	public AggregateValidationStatus(final Realm realm, final IObservableCollection bindings,
			int strategy) {
		if (strategy == MERGED) {
			implementation = new ComputedValue(realm, IStatus.class) {
				protected Object calculate() {
					return getStatusMerged(bindings);
				}
			};
		} else {
			implementation = new ComputedValue(realm, IStatus.class) {
				protected Object calculate() {
					return getStatusMaxSeverity(bindings);
				}
			};
		}
	}

	/**
	 * @param listener
	 * @see org.eclipse.core.databinding.observable.IObservable#addChangeListener(org.eclipse.core.databinding.observable.IChangeListener)
	 */
	public void addChangeListener(IChangeListener listener) {
		implementation.addChangeListener(listener);
	}

	/**
	 * @param listener
	 * @see org.eclipse.core.databinding.observable.IObservable#addStaleListener(org.eclipse.core.databinding.observable.IStaleListener)
	 */
	public void addStaleListener(IStaleListener listener) {
		implementation.addStaleListener(listener);
	}

	/**
	 * @param listener
	 * @see org.eclipse.core.databinding.observable.value.IObservableValue#addValueChangeListener(org.eclipse.core.databinding.observable.value.IValueChangeListener)
	 */
	public void addValueChangeListener(IValueChangeListener listener) {
		implementation.addValueChangeListener(listener);
	}

	public void dispose() {
		implementation.dispose();
	}

	public Realm getRealm() {
		return implementation.getRealm();
	}

	public Object getValue() {
		return implementation.getValue();
	}

	public Object getValueType() {
		return implementation.getValueType();
	}

	public boolean isStale() {
		return implementation.isStale();
	}

	public void removeChangeListener(IChangeListener listener) {
		implementation.removeChangeListener(listener);
	}

	public void removeStaleListener(IStaleListener listener) {
		implementation.removeStaleListener(listener);
	}

	public void removeValueChangeListener(IValueChangeListener listener) {
		implementation.removeValueChangeListener(listener);
	}

	public void setValue(Object value) {
		implementation.setValue(value);
	}

	/**
	 * Returns a status object that merges multiple non-OK status objects in a
	 * {@link MultiStatus}. Returns an OK status result if all statuses from
	 * the given bindings are the an OK status. Returns a single status if there
	 * is only one non-OK status.
	 * 
	 * @param bindings
	 *            a collection of bindings
	 * @return a merged status
	 */
	public static IStatus getStatusMerged(Collection bindings) {
		List statuses = new ArrayList();
		for (Iterator it = bindings.iterator(); it.hasNext();) {
			Binding binding = (Binding) it.next();
			IStatus status = (IStatus) binding
					.getValidationStatus().getValue();
			if (!status.isOK()) {
				statuses.add(status);
			}
		}
		if (statuses.size() == 1) {
			return (IStatus) statuses.get(0);
		}
		if (!statuses.isEmpty()) {
			MultiStatus result = new MultiStatus(
					Policy.JFACE_DATABINDING,
					0,
					BindingMessages
							.getString(BindingMessages.MULTIPLE_PROBLEMS),
					null);
			for (Iterator it = statuses.iterator(); it.hasNext();) {
				IStatus status = (IStatus) it.next();
				result.merge(status);
			}
			return result;
		}
		return Status.OK_STATUS;
	}

	/**
	 * Returns a status that always returns the most severe status from the
	 * given bindings. If there is more than one status at the same severity
	 * level, it picks the first one it encounters.
	 * 
	 * @param bindings
	 *            a collection of bindings
	 * @return a single status reflecting the most severe status from the given
	 *         bindings
	 */
	public static IStatus getStatusMaxSeverity(Collection bindings) {
		int maxSeverity = IStatus.OK;
		IStatus maxStatus = Status.OK_STATUS;
		for (Iterator it = bindings.iterator(); it.hasNext();) {
			Binding binding = (Binding) it.next();
			IStatus status = (IStatus) binding
					.getValidationStatus().getValue();
			if (status.getSeverity() > maxSeverity) {
				maxSeverity = status.getSeverity();
				maxStatus = status;
			}
		}
		return maxStatus;
	}

}
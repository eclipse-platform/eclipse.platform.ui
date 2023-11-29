/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Carter - bug 182822
 *     Boris Bokowski - bug 218269
 *     Matthew Hall - bugs 218269, 146397, 249526, 267451
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 489098
 *******************************************************************************/
package org.eclipse.core.databinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.ComputedValue;
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
 */
public final class AggregateValidationStatus extends ComputedValue<IStatus> {
	/**
	 * Constant denoting an aggregation strategy that merges multiple non-OK
	 * status objects in a {@link MultiStatus}. Returns an OK status result if
	 * all statuses from the given validation status providers are the an OK
	 * status. Returns a single status if there is only one non-OK status.
	 *
	 * @see #getStatusMerged(Collection)
	 */
	public static final int MERGED = 1;

	/**
	 * Constant denoting an aggregation strategy that always returns the most
	 * severe status from the given validation status providers. If there is
	 * more than one status at the same severity level, it picks the first one
	 * it encounters.
	 *
	 * @see #getStatusMaxSeverity(Collection)
	 */
	public static final int MAX_SEVERITY = 2;

	private int strategy;
	private IObservableCollection<? extends ValidationStatusProvider> validationStatusProviders;

	/**
	 * Creates a new aggregate validation status observable for the given data
	 * binding context.
	 *
	 * @param dbc
	 *            a data binding context
	 * @param strategy
	 *            a strategy constant, one of {@link #MERGED} or
	 *            {@link #MAX_SEVERITY}.
	 * @since 1.1
	 */
	public AggregateValidationStatus(DataBindingContext dbc, int strategy) {
		this(dbc.getValidationRealm(), dbc.getValidationStatusProviders(),
				strategy);
	}

	/**
	 * @param validationStatusProviders
	 *            an observable collection containing elements of type
	 *            {@link ValidationStatusProvider}
	 * @param strategy
	 *            a strategy constant, one of {@link #MERGED} or
	 *            {@link #MAX_SEVERITY}.
	 * @see DataBindingContext#getValidationStatusProviders()
	 */
	public AggregateValidationStatus(
			final IObservableCollection<? extends ValidationStatusProvider> validationStatusProviders,
			int strategy) {
		this(Realm.getDefault(), validationStatusProviders, strategy);
	}

	/**
	 * @param realm
	 *            Realm
	 * @param validationStatusProviders
	 *            an observable collection containing elements of type
	 *            {@link ValidationStatusProvider}
	 * @param strategy
	 *            a strategy constant, one of {@link #MERGED} or
	 *            {@link #MAX_SEVERITY}.
	 * @see DataBindingContext#getValidationStatusProviders()
	 * @since 1.1
	 */
	public AggregateValidationStatus(
			final Realm realm,
			final IObservableCollection<? extends ValidationStatusProvider> validationStatusProviders,
			int strategy) {
		super(realm, IStatus.class);
		this.validationStatusProviders = validationStatusProviders;
		this.strategy = strategy;
	}

	@Override
	protected IStatus calculate() {
		IStatus result;
		if (strategy == MERGED) {
			result = getStatusMerged(validationStatusProviders);
		} else {
			result = getStatusMaxSeverity(validationStatusProviders);
		}
		return result;
	}

	/**
	 * Returns a status object that merges multiple non-OK status objects in a
	 * {@link MultiStatus}. Returns an OK status result if all statuses from the
	 * given validation status providers are the an OK status. Returns a single
	 * status if there is only one non-OK status.
	 *
	 * @param validationStatusProviders
	 *            a collection of validation status providers
	 * @return a merged status
	 */
	public static IStatus getStatusMerged(Collection<? extends ValidationStatusProvider> validationStatusProviders) {
		List<IStatus> statuses = new ArrayList<>();
		for (ValidationStatusProvider provider : validationStatusProviders) {
			IStatus status = provider.getValidationStatus().getValue();
			if (!status.isOK()) {
				statuses.add(status);
			}
		}
		if (statuses.size() == 1) {
			return statuses.get(0);
		}
		if (!statuses.isEmpty()) {
			MultiStatus result = new MultiStatus(Policy.JFACE_DATABINDING, 0,
					BindingMessages.getString(BindingMessages.MULTIPLE_PROBLEMS));
			for (IStatus status : statuses) {
				result.merge(status);
			}
			return result;
		}
		return Status.OK_STATUS;
	}

	/**
	 * Returns a status that always returns the most severe status from the
	 * given validation status providers. If there is more than one status at
	 * the same severity level, it picks the first one it encounters.
	 *
	 * @param validationStatusProviders
	 *            a collection of validation status providers
	 * @return a single status reflecting the most severe status from the given
	 *         validation status providers
	 */
	public static IStatus getStatusMaxSeverity(
			Collection<? extends ValidationStatusProvider> validationStatusProviders) {
		int maxSeverity = IStatus.OK;
		IStatus maxStatus = Status.OK_STATUS;
		for (ValidationStatusProvider provider : validationStatusProviders) {
			IStatus status = provider.getValidationStatus().getValue();
			if (status.getSeverity() > maxSeverity) {
				maxSeverity = status.getSeverity();
				maxStatus = status;
			}
		}
		return maxStatus;
	}
}
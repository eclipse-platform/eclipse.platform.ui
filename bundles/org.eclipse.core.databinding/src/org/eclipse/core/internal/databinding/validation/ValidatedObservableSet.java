/*******************************************************************************
 * Copyright (c) 2008, 2017 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 218269)
 ******************************************************************************/

package org.eclipse.core.internal.databinding.validation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.ObservableSet;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;

/**
 * @param <E>
 *            The element type.
 * @since 3.3
 *
 */
public class ValidatedObservableSet<E> extends ObservableSet<E> {
	private IObservableSet<E> target;
	private IObservableValue<IStatus> validationStatus;

	// Only true when out of sync with target due to validation status
	private boolean stale;

	// True when validation status changes from invalid to valid.
	private boolean computeNextDiff = false;

	private boolean updatingTarget = false;

	private ISetChangeListener<E> targetChangeListener = event -> {
		if (updatingTarget)
			return;
		IStatus status = validationStatus.getValue();
		if (isValid(status)) {
			if (stale) {
				// this.stale means we are out of sync with target,
				// so reset wrapped list to exactly mirror target
				stale = false;
				updateWrappedSet(new HashSet<>(target));
			} else {
				SetDiff<? extends E> diff = event.diff;
				if (computeNextDiff) {
					diff = Diffs.computeSetDiff(wrappedSet, target);
					computeNextDiff = false;
				}
				applyDiff(diff, wrappedSet);
				fireSetChange(Diffs.unmodifiableDiff(diff));
			}
		} else {
			makeStale();
		}
	};

	private IStaleListener targetStaleListener = staleEvent -> fireStale();

	private IValueChangeListener<IStatus> validationStatusChangeListener = event -> {
		IStatus oldStatus = event.diff.getOldValue();
		IStatus newStatus = event.diff.getNewValue();
		if (stale && !isValid(oldStatus) && isValid(newStatus)) {
			// this.stale means we are out of sync with target,
			// reset wrapped set to exactly mirror target
			stale = false;
			updateWrappedSet(new HashSet<>(target));
			// If the validation status becomes valid because of a change in
			// target observable
			computeNextDiff = true;
		}
	};

	/**
	 * @param target
	 * @param validationStatus
	 */
	public ValidatedObservableSet(final IObservableSet<E> target,
			final IObservableValue<IStatus> validationStatus) {
		super(target.getRealm(), new HashSet<>(target), target
				.getElementType());
		Assert.isNotNull(validationStatus,
				"Validation status observable cannot be null"); //$NON-NLS-1$
		Assert.isTrue(target.getRealm().equals(validationStatus.getRealm()),
				"Target and validation status observables must be on the same realm"); //$NON-NLS-1$
		this.target = target;
		this.validationStatus = validationStatus;
		target.addSetChangeListener(targetChangeListener);
		target.addStaleListener(targetStaleListener);
		validationStatus.addValueChangeListener(validationStatusChangeListener);
	}

	private void updateWrappedSet(Set<E> newSet) {
		Set<E> oldSet = wrappedSet;
		SetDiff<E> diff = Diffs.computeSetDiff(oldSet, newSet);
		wrappedSet = newSet;
		fireSetChange(diff);
	}

	private static boolean isValid(IStatus status) {
		return status.isOK() || status.matches(IStatus.INFO | IStatus.WARNING);
	}

	private void applyDiff(SetDiff<? extends E> diff, Set<E> set) {
		for (Iterator<? extends E> iterator = diff.getRemovals().iterator(); iterator
				.hasNext();) {
			set.remove(iterator.next());
		}
		for (Iterator<? extends E> iterator = diff.getAdditions().iterator(); iterator
				.hasNext();) {
			set.add(iterator.next());
		}
	}

	private void makeStale() {
		if (!stale) {
			stale = true;
			fireStale();
		}
	}

	private void updateTargetSet(SetDiff<E> diff) {
		updatingTarget = true;
		try {
			if (stale) {
				stale = false;
				applyDiff(Diffs.computeSetDiff(target, wrappedSet), target);
			} else {
				applyDiff(diff, target);
			}
		} finally {
			updatingTarget = false;
		}
	}

	@Override
	public boolean isStale() {
		getterCalled();
		return stale || target.isStale();
	}

	@Override
	public boolean add(E o) {
		getterCalled();
		boolean changed = wrappedSet.add(o);
		if (changed) {
			SetDiff<E> diff = Diffs.createSetDiff(Collections.singleton(o),
					Collections.<E> emptySet());
			updateTargetSet(diff);
			fireSetChange(diff);
		}
		return changed;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		getterCalled();
		HashSet<E> set = new HashSet<E>(wrappedSet);
		boolean changed = set.addAll(c);
		if (changed) {
			SetDiff<E> diff = Diffs.computeSetDiff(wrappedSet, set);
			wrappedSet = set;
			updateTargetSet(diff);
			fireSetChange(diff);
		}
		return changed;
	}

	@Override
	public void clear() {
		getterCalled();
		if (isEmpty())
			return;
		SetDiff<E> diff = Diffs.createSetDiff(Collections.<E> emptySet(),
				wrappedSet);
		wrappedSet = new HashSet<E>();
		updateTargetSet(diff);
		fireSetChange(diff);
	}

	@Override
	public Iterator<E> iterator() {
		getterCalled();
		final Iterator<E> wrappedIterator = wrappedSet.iterator();
		return new Iterator<E>() {
			E last = null;

			@Override
			public boolean hasNext() {
				return wrappedIterator.hasNext();
			}

			@Override
			public E next() {
				return last = wrappedIterator.next();
			}

			@Override
			public void remove() {
				wrappedIterator.remove();
				SetDiff<E> diff = Diffs
						.createSetDiff(Collections.<E> emptySet(),
								Collections.singleton(last));
				updateTargetSet(diff);
				fireSetChange(diff);
			}
		};
	}

	@Override
	public boolean remove(Object o) {
		getterCalled();
		boolean changed = wrappedSet.remove(o);
		if (changed) {
			@SuppressWarnings("unchecked")
			SetDiff<E> diff = Diffs.createSetDiff(Collections.emptySet(),
					Collections.singleton((E) o));
			updateTargetSet(diff);
			fireSetChange(diff);
		}
		return changed;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		getterCalled();
		Set<E> set = new HashSet<E>(wrappedSet);
		boolean changed = set.removeAll(c);
		if (changed) {
			SetDiff<E> diff = Diffs.computeSetDiff(wrappedSet, set);
			wrappedSet = set;
			updateTargetSet(diff);
			fireSetChange(diff);
		}
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		getterCalled();
		Set<E> set = new HashSet<E>(wrappedSet);
		boolean changed = set.retainAll(c);
		if (changed) {
			SetDiff<E> diff = Diffs.computeSetDiff(wrappedSet, set);
			wrappedSet = set;
			updateTargetSet(diff);
			fireSetChange(diff);
		}
		return changed;
	}

	@Override
	public synchronized void dispose() {
		target.removeSetChangeListener(targetChangeListener);
		target.removeStaleListener(targetStaleListener);
		validationStatus
				.removeValueChangeListener(validationStatusChangeListener);
		super.dispose();
	}
}

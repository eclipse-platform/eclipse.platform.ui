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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.core.databinding.observable.list.ObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;

/**
 * @param <E>
 *            The element type of the list.
 * @since 3.3
 *
 */
public class ValidatedObservableList<E> extends ObservableList<E> {
	private IObservableList<E> target;
	private IObservableValue<IStatus> validationStatus;

	// Only true when out of sync with target due to validation status
	private boolean stale;

	// True when validation status changes from invalid to valid.
	private boolean computeNextDiff = false;

	private boolean updatingTarget = false;

	private IListChangeListener<E> targetChangeListener = event -> {
		if (updatingTarget)
			return;
		IStatus status = validationStatus.getValue();
		if (isValid(status)) {
			if (stale) {
				// this.stale means we are out of sync with target,
				// so reset wrapped list to exactly mirror target
				stale = false;
				updateWrappedList(new ArrayList<>(target));
			} else {
				ListDiff<? extends E> diff = event.diff;
				if (computeNextDiff) {
					diff = Diffs.computeListDiff(wrappedList, target);
					computeNextDiff = false;
				}
				applyDiff(diff, wrappedList);
				fireListChange(Diffs.unmodifiableDiff(diff));
			}
		} else {
			makeStale();
		}
	};

	private static boolean isValid(IStatus status) {
		return status.isOK() || status.matches(IStatus.INFO | IStatus.WARNING);
	}

	private IStaleListener targetStaleListener = staleEvent -> fireStale();

	private IValueChangeListener<IStatus> validationStatusChangeListener = event -> {
		IStatus oldStatus = event.diff.getOldValue();
		IStatus newStatus = event.diff.getNewValue();
		if (stale && !isValid(oldStatus) && isValid(newStatus)) {
			// this.stale means we are out of sync with target,
			// reset wrapped list to exactly mirror target
			stale = false;
			updateWrappedList(new ArrayList<>(target));

			// If the validation status becomes valid because of a change in
			// target observable
			computeNextDiff = true;
		}
	};

	/**
	 * @param target
	 * @param validationStatus
	 */
	public ValidatedObservableList(final IObservableList<E> target, final IObservableValue<IStatus> validationStatus) {
		super(target.getRealm(), new ArrayList<>(target), target.getElementType());
		Assert.isNotNull(validationStatus,
				"Validation status observable cannot be null"); //$NON-NLS-1$
		Assert.isTrue(target.getRealm().equals(validationStatus.getRealm()),
				"Target and validation status observables must be on the same realm"); //$NON-NLS-1$
		this.target = target;
		this.validationStatus = validationStatus;
		target.addListChangeListener(targetChangeListener);
		target.addStaleListener(targetStaleListener);
		validationStatus.addValueChangeListener(validationStatusChangeListener);
	}

	private void makeStale() {
		if (!stale) {
			stale = true;
			fireStale();
		}
	}

	private void updateTargetList(ListDiff<E> diff) {
		updatingTarget = true;
		try {
			if (stale) {
				stale = false;
				applyDiff(Diffs.computeListDiff(target, wrappedList), target);
			} else {
				applyDiff(diff, target);
			}
		} finally {
			updatingTarget = false;
		}
	}

	private void applyDiff(ListDiff<? extends E> diff, final List<E> list) {
		diff.accept(new ListDiffVisitor<E>() {
			@Override
			public void handleAdd(int index, E element) {
				list.add(index, element);
			}

			@Override
			public void handleRemove(int index, E element) {
				list.remove(index);
			}

			@Override
			public void handleReplace(int index, E oldElement, E newElement) {
				list.set(index, newElement);
			}
		});
	}

	@Override
	public boolean isStale() {
		ObservableTracker.getterCalled(this);
		return stale || target.isStale();
	}

	@Override
	public void add(int index, E element) {
		checkRealm();
		wrappedList.add(index, element);
		ListDiff<E> diff = Diffs.createListDiff(Diffs.createListDiffEntry(index, true, element));
		updateTargetList(diff);
		fireListChange(diff);
	}

	@Override
	public boolean add(E o) {
		checkRealm();
		add(wrappedList.size(), o);
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		checkRealm();
		return addAll(wrappedList.size(), c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		checkRealm();
		List<ListDiffEntry<E>> entries = new ArrayList<>(c.size());
		int i = index;
		for (E element : c) {
			wrappedList.add(i, element);
			entries.add(Diffs.createListDiffEntry(i, true, element));
			i++;
		}
		ListDiff<E> diff = Diffs.createListDiff(entries);
		updateTargetList(diff);
		fireListChange(diff);
		return true;
	}

	@Override
	public void clear() {
		checkRealm();
		if (isEmpty())
			return;
		ListDiff<E> diff = Diffs.computeListDiff(wrappedList, Collections.emptyList());
		wrappedList.clear();
		updateTargetList(diff);
		fireListChange(diff);
	}

	@Override
	public Iterator<E> iterator() {
		getterCalled();
		final ListIterator<E> wrappedIterator = wrappedList.listIterator();
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
				int index = wrappedIterator.previousIndex();
				wrappedIterator.remove();
				ListDiff<E> diff = Diffs.createListDiff(Diffs.createListDiffEntry(index, false, last));
				updateTargetList(diff);
				fireListChange(diff);
			}
		};
	}

	@Override
	public ListIterator<E> listIterator() {
		return listIterator(0);
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		getterCalled();
		final ListIterator<E> wrappedIterator = wrappedList.listIterator(index);
		return new ListIterator<E>() {
			int lastIndex = -1;
			E last = null;

			@Override
			public void add(E o) {
				wrappedIterator.add(o);
				lastIndex = previousIndex();
				ListDiff<E> diff = Diffs.createListDiff(Diffs.createListDiffEntry(lastIndex, true, o));
				updateTargetList(diff);
				fireListChange(diff);
			}

			@Override
			public boolean hasNext() {
				return wrappedIterator.hasNext();
			}

			@Override
			public boolean hasPrevious() {
				return wrappedIterator.hasPrevious();
			}

			@Override
			public E next() {
				last = wrappedIterator.next();
				lastIndex = previousIndex();
				return last;
			}

			@Override
			public int nextIndex() {
				return wrappedIterator.nextIndex();
			}

			@Override
			public E previous() {
				last = wrappedIterator.previous();
				lastIndex = nextIndex();
				return last;
			}

			@Override
			public int previousIndex() {
				return wrappedIterator.previousIndex();
			}

			@Override
			public void remove() {
				wrappedIterator.remove();
				ListDiff<E> diff = Diffs.createListDiff(Diffs.createListDiffEntry(lastIndex, false, last));
				lastIndex = -1;
				updateTargetList(diff);
				fireListChange(diff);
			}

			@Override
			public void set(E o) {
				wrappedIterator.set(o);
				ListDiff<E> diff = Diffs.createListDiff(
						Diffs.createListDiffEntry(lastIndex, false, last),
						Diffs.createListDiffEntry(lastIndex, true, o));
				last = o;
				updateTargetList(diff);
				fireListChange(diff);
			}
		};
	}

	@Override
	public E move(int oldIndex, int newIndex) {
		checkRealm();
		int size = wrappedList.size();
		if (oldIndex >= size)
			throw new IndexOutOfBoundsException(
					"oldIndex: " + oldIndex + ", size:" + size); //$NON-NLS-1$ //$NON-NLS-2$
		if (newIndex >= size)
			throw new IndexOutOfBoundsException(
					"newIndex: " + newIndex + ", size:" + size); //$NON-NLS-1$ //$NON-NLS-2$
		if (oldIndex == newIndex)
			return wrappedList.get(oldIndex);
		E element = wrappedList.remove(oldIndex);
		wrappedList.add(newIndex, element);
		ListDiff<E> diff = Diffs.createListDiff(
				Diffs.createListDiffEntry(oldIndex, false, element),
				Diffs.createListDiffEntry(newIndex, true, element));
		updateTargetList(diff);
		fireListChange(diff);
		return element;
	}

	@Override
	public E remove(int index) {
		checkRealm();
		E element = wrappedList.remove(index);
		ListDiff<E> diff = Diffs.createListDiff(Diffs.createListDiffEntry(
				index, false, element));
		updateTargetList(diff);
		fireListChange(diff);
		return element;
	}

	@Override
	public boolean remove(Object o) {
		checkRealm();
		int index = wrappedList.indexOf(o);
		if (index == -1)
			return false;
		remove(index);
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		checkRealm();
		List<E> list = new ArrayList<>(wrappedList);
		boolean changed = list.removeAll(c);
		if (changed) {
			ListDiff<E> diff = Diffs.computeListDiff(wrappedList, list);
			wrappedList = list;
			updateTargetList(diff);
			fireListChange(diff);
		}
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		checkRealm();
		List<E> list = new ArrayList<>(wrappedList);
		boolean changed = list.retainAll(c);
		if (changed) {
			ListDiff<E> diff = Diffs.computeListDiff(wrappedList, list);
			wrappedList = list;
			updateTargetList(diff);
			fireListChange(diff);
		}
		return changed;
	}

	@Override
	public E set(int index, E element) {
		checkRealm();
		E oldElement = wrappedList.set(index, element);
		ListDiff<E> diff = Diffs.createListDiff(
				Diffs.createListDiffEntry(index, false, oldElement),
				Diffs.createListDiffEntry(index, true, element));
		updateTargetList(diff);
		fireListChange(diff);
		return oldElement;
	}

	@Override
	public synchronized void dispose() {
		target.removeListChangeListener(targetChangeListener);
		target.removeStaleListener(targetStaleListener);
		validationStatus
				.removeValueChangeListener(validationStatusChangeListener);
		super.dispose();
	}
}

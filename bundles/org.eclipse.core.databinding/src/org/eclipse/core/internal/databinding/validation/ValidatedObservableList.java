/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.core.databinding.observable.list.ObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;

/**
 * @since 3.3
 * 
 */
public class ValidatedObservableList extends ObservableList {
	private IObservableList target;
	private IObservableValue validationStatus;

	// Only true when out of sync with target due to validation status
	private boolean stale;

	// True when validaton status changes from invalid to valid.
	private boolean computeNextDiff = false;

	private boolean updatingTarget = false;

	private IListChangeListener targetChangeListener = new IListChangeListener() {
		public void handleListChange(ListChangeEvent event) {
			if (updatingTarget)
				return;
			IStatus status = (IStatus) validationStatus.getValue();
			if (isValid(status)) {
				if (stale) {
					// this.stale means we are out of sync with target,
					// so reset wrapped list to exactly mirror target
					stale = false;
					updateWrappedList(new ArrayList(target));
				} else {
					ListDiff diff = event.diff;
					if (computeNextDiff) {
						diff = Diffs.computeListDiff(wrappedList, target);
						computeNextDiff = false;
					}
					applyDiff(diff, wrappedList);
					fireListChange(diff);
				}
			} else {
				makeStale();
			}
		}
	};

	private static boolean isValid(IStatus status) {
		return status.isOK() || status.matches(IStatus.INFO | IStatus.WARNING);
	}

	private IStaleListener targetStaleListener = new IStaleListener() {
		public void handleStale(StaleEvent staleEvent) {
			fireStale();
		}
	};

	private IValueChangeListener validationStatusChangeListener = new IValueChangeListener() {
		public void handleValueChange(ValueChangeEvent event) {
			IStatus oldStatus = (IStatus) event.diff.getOldValue();
			IStatus newStatus = (IStatus) event.diff.getNewValue();
			if (stale && !isValid(oldStatus) && isValid(newStatus)) {
				// this.stale means we are out of sync with target,
				// reset wrapped list to exactly mirror target
				stale = false;
				updateWrappedList(new ArrayList(target));

				// If the validation status becomes valid because of a change in
				// target observable
				computeNextDiff = true;
			}
		}
	};

	/**
	 * @param target
	 * @param validationStatus
	 */
	public ValidatedObservableList(final IObservableList target,
			final IObservableValue validationStatus) {
		super(target.getRealm(), new ArrayList(target), target.getElementType());
		Assert.isNotNull(validationStatus,
				"Validation status observable cannot be null"); //$NON-NLS-1$
		Assert
				.isTrue(target.getRealm().equals(validationStatus.getRealm()),
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

	private void updateTargetList(ListDiff diff) {
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

	private void applyDiff(ListDiff diff, final List list) {
		diff.accept(new ListDiffVisitor() {
			public void handleAdd(int index, Object element) {
				list.add(index, element);
			}

			public void handleRemove(int index, Object element) {
				list.remove(index);
			}

			public void handleReplace(int index, Object oldElement,
					Object newElement) {
				list.set(index, newElement);
			}
		});
	}

	public boolean isStale() {
		ObservableTracker.getterCalled(this);
		return stale || target.isStale();
	}

	public void add(int index, Object element) {
		checkRealm();
		wrappedList.add(index, element);
		ListDiff diff = Diffs.createListDiff(Diffs.createListDiffEntry(index,
				true, element));
		updateTargetList(diff);
		fireListChange(diff);
	}

	public boolean add(Object o) {
		checkRealm();
		add(wrappedList.size(), o);
		return true;
	}

	public boolean addAll(Collection c) {
		checkRealm();
		return addAll(wrappedList.size(), c);
	}

	public boolean addAll(int index, Collection c) {
		checkRealm();
		Object[] elements = c.toArray();
		ListDiffEntry[] entries = new ListDiffEntry[elements.length];
		for (int i = 0; i < elements.length; i++) {
			wrappedList.add(index + i, elements[i]);
			entries[i] = Diffs
					.createListDiffEntry(index + i, true, elements[i]);
		}
		ListDiff diff = Diffs.createListDiff(entries);
		updateTargetList(diff);
		fireListChange(diff);
		return true;
	}

	public void clear() {
		checkRealm();
		if (isEmpty())
			return;
		ListDiff diff = Diffs.computeListDiff(wrappedList,
				Collections.EMPTY_LIST);
		wrappedList.clear();
		updateTargetList(diff);
		fireListChange(diff);
	}

	public Iterator iterator() {
		getterCalled();
		final ListIterator wrappedIterator = wrappedList.listIterator();
		return new Iterator() {
			Object last = null;

			public boolean hasNext() {
				return wrappedIterator.hasNext();
			}

			public Object next() {
				return last = wrappedIterator.next();
			}

			public void remove() {
				int index = wrappedIterator.previousIndex();
				wrappedIterator.remove();
				ListDiff diff = Diffs.createListDiff(Diffs.createListDiffEntry(
						index, false, last));
				updateTargetList(diff);
				fireListChange(diff);
			}
		};
	}

	public ListIterator listIterator() {
		return listIterator(0);
	}

	public ListIterator listIterator(int index) {
		getterCalled();
		final ListIterator wrappedIterator = wrappedList.listIterator(index);
		return new ListIterator() {
			int lastIndex = -1;
			Object last = null;

			public void add(Object o) {
				wrappedIterator.add(o);
				lastIndex = previousIndex();
				ListDiff diff = Diffs.createListDiff(Diffs.createListDiffEntry(
						lastIndex, true, o));
				updateTargetList(diff);
				fireListChange(diff);
			}

			public boolean hasNext() {
				return wrappedIterator.hasNext();
			}

			public boolean hasPrevious() {
				return wrappedIterator.hasPrevious();
			}

			public Object next() {
				last = wrappedIterator.next();
				lastIndex = previousIndex();
				return last;
			}

			public int nextIndex() {
				return wrappedIterator.nextIndex();
			}

			public Object previous() {
				last = wrappedIterator.previous();
				lastIndex = nextIndex();
				return last;
			}

			public int previousIndex() {
				return wrappedIterator.previousIndex();
			}

			public void remove() {
				wrappedIterator.remove();
				ListDiff diff = Diffs.createListDiff(Diffs.createListDiffEntry(
						lastIndex, false, last));
				lastIndex = -1;
				updateTargetList(diff);
				fireListChange(diff);
			}

			public void set(Object o) {
				wrappedIterator.set(o);
				ListDiff diff = Diffs.createListDiff(Diffs.createListDiffEntry(
						lastIndex, false, last), Diffs.createListDiffEntry(
						lastIndex, true, o));
				last = o;
				updateTargetList(diff);
				fireListChange(diff);
			}
		};
	}

	public Object move(int oldIndex, int newIndex) {
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
		Object element = wrappedList.remove(oldIndex);
		wrappedList.add(newIndex, element);
		ListDiff diff = Diffs.createListDiff(Diffs.createListDiffEntry(
				oldIndex, false, element), Diffs.createListDiffEntry(newIndex,
				true, element));
		updateTargetList(diff);
		fireListChange(diff);
		return element;
	}

	public Object remove(int index) {
		checkRealm();
		Object element = wrappedList.remove(index);
		ListDiff diff = Diffs.createListDiff(Diffs.createListDiffEntry(index,
				false, element));
		updateTargetList(diff);
		fireListChange(diff);
		return element;
	}

	public boolean remove(Object o) {
		checkRealm();
		int index = wrappedList.indexOf(o);
		if (index == -1)
			return false;
		remove(index);
		return true;
	}

	public boolean removeAll(Collection c) {
		checkRealm();
		List list = new ArrayList(wrappedList);
		boolean changed = list.removeAll(c);
		if (changed) {
			ListDiff diff = Diffs.computeListDiff(wrappedList, list);
			wrappedList = list;
			updateTargetList(diff);
			fireListChange(diff);
		}
		return changed;
	}

	public boolean retainAll(Collection c) {
		checkRealm();
		List list = new ArrayList(wrappedList);
		boolean changed = list.retainAll(c);
		if (changed) {
			ListDiff diff = Diffs.computeListDiff(wrappedList, list);
			wrappedList = list;
			updateTargetList(diff);
			fireListChange(diff);
		}
		return changed;
	}

	public Object set(int index, Object element) {
		checkRealm();
		Object oldElement = wrappedList.set(index, element);
		ListDiff diff = Diffs.createListDiff(Diffs.createListDiffEntry(index,
				false, oldElement), Diffs.createListDiffEntry(index, true,
				element));
		updateTargetList(diff);
		fireListChange(diff);
		return oldElement;
	}

	public synchronized void dispose() {
		target.removeListChangeListener(targetChangeListener);
		target.removeStaleListener(targetStaleListener);
		validationStatus
				.removeValueChangeListener(validationStatusChangeListener);
		super.dispose();
	}
}

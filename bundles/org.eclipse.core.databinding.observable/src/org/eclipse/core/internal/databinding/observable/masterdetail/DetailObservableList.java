/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 147515
 *     Matthew Hall - bug 221351, 247875, 246782, 249526, 268022, 251424
 *     Ovidio Mallo - bug 241318
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 *     Stefan Xenos <sxenos@gmail.com> - Bug 474065
 *******************************************************************************/
package org.eclipse.core.internal.databinding.observable.masterdetail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.DisposeEvent;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.IObserving;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ObservableList;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.runtime.Assert;

/**
 * @param <M>
 *            type of the master observable
 * @param <E>
 *            type of the elements in the inner observable list
 * @since 3.2
 *
 */
public class DetailObservableList<M, E> extends ObservableList<E>implements IObserving {

	private boolean updating = false;

	private IListChangeListener<E> innerChangeListener = new IListChangeListener<E>() {
		@Override
		public void handleListChange(ListChangeEvent<? extends E> event) {
			if (!updating) {
				fireListChange(Diffs.unmodifiableDiff(event.diff));
			}
		}
	};

	private M currentOuterValue;

	private IObservableList<E> innerObservableList;

	private IObservableFactory<? super M, IObservableList<E>> factory;

	private IObservableValue<M> outerObservableValue;

	private Object detailType;

	/**
	 * @param factory
	 * @param outerObservableValue
	 * @param detailType
	 */
	public DetailObservableList(
			IObservableFactory<? super M, IObservableList<E>> factory,
			IObservableValue<M> outerObservableValue, Object detailType) {
		super(outerObservableValue.getRealm(), Collections.<E> emptyList(), detailType);
		Assert.isTrue(!outerObservableValue.isDisposed(),
				"Master observable is disposed"); //$NON-NLS-1$

		this.factory = factory;
		this.outerObservableValue = outerObservableValue;
		this.detailType = detailType;

		outerObservableValue.addDisposeListener(new IDisposeListener() {
			@Override
			public void handleDispose(DisposeEvent staleEvent) {
				dispose();
			}
		});

		ObservableTracker.setIgnore(true);
		try {
			updateInnerObservableList();
		} finally {
			ObservableTracker.setIgnore(false);
		}
		outerObservableValue.addValueChangeListener(outerChangeListener);
	}

	IValueChangeListener<M> outerChangeListener = new IValueChangeListener<M>() {
		@Override
		public void handleValueChange(ValueChangeEvent<? extends M> event) {
			if (isDisposed())
				return;
			ObservableTracker.setIgnore(true);
			try {
				List<E> oldList = new ArrayList<E>(wrappedList);
				updateInnerObservableList();
				fireListChange(Diffs.computeListDiff(oldList, wrappedList));
			} finally {
				ObservableTracker.setIgnore(false);
			}
		}
	};

	private void updateInnerObservableList() {
		if (innerObservableList != null) {
			innerObservableList.removeListChangeListener(innerChangeListener);
			innerObservableList.dispose();
		}
		currentOuterValue = outerObservableValue.getValue();
		if (currentOuterValue == null) {
			innerObservableList = null;
			wrappedList = Collections.emptyList();
		} else {
			ObservableTracker.setIgnore(true);
			try {
				innerObservableList = factory
						.createObservable(currentOuterValue);
			} finally {
				ObservableTracker.setIgnore(false);
			}
			DetailObservableHelper.warnIfDifferentRealms(getRealm(),
					innerObservableList.getRealm());
			wrappedList = innerObservableList;

			if (detailType != null) {
				Object innerValueType = innerObservableList.getElementType();
				Assert.isTrue(getElementType().equals(innerValueType),
						"Cannot change value type in a nested observable list"); //$NON-NLS-1$
			}
			innerObservableList.addListChangeListener(innerChangeListener);
		}
	}

	@Override
	public boolean add(final E o) {
		ObservableTracker.setIgnore(true);
		try {
			return wrappedList.add(o);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	@Override
	public void add(final int index, final E element) {
		ObservableTracker.setIgnore(true);
		try {
			wrappedList.add(index, element);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	@Override
	public boolean remove(final Object o) {
		ObservableTracker.setIgnore(true);
		try {
			return wrappedList.remove(o);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	@Override
	public E set(final int index, final E element) {
		ObservableTracker.setIgnore(true);
		try {
			return wrappedList.set(index, element);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	@Override
	public E move(final int oldIndex, final int newIndex) {
		if (innerObservableList != null) {
			ObservableTracker.setIgnore(true);
			try {
				return innerObservableList.move(oldIndex, newIndex);
			} finally {
				ObservableTracker.setIgnore(false);
			}
		}
		return super.move(oldIndex, newIndex);
	}

	@Override
	public E remove(final int index) {
		ObservableTracker.setIgnore(true);
		try {
			return wrappedList.remove(index);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	@Override
	public boolean addAll(final Collection<? extends E> c) {
		ObservableTracker.setIgnore(true);
		try {
			return wrappedList.addAll(c);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends E> c) {
		ObservableTracker.setIgnore(true);
		try {
			return wrappedList.addAll(index, c);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		ObservableTracker.setIgnore(true);
		try {
			return wrappedList.removeAll(c);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		ObservableTracker.setIgnore(true);
		try {
			return wrappedList.retainAll(c);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	@Override
	public void clear() {
		ObservableTracker.setIgnore(true);
		try {
			wrappedList.clear();
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	@Override
	public synchronized void dispose() {
		super.dispose();

		if (outerObservableValue != null) {
			outerObservableValue.removeValueChangeListener(outerChangeListener);
		}
		if (innerObservableList != null) {
			innerObservableList.removeListChangeListener(innerChangeListener);
			innerObservableList.dispose();
		}
		outerObservableValue = null;
		outerChangeListener = null;
		currentOuterValue = null;
		factory = null;
		innerObservableList = null;
		innerChangeListener = null;
	}

	@Override
	public Object getObserved() {
		if (innerObservableList instanceof IObserving) {
			return ((IObserving) innerObservableList).getObserved();
		}
		return null;
	}
}

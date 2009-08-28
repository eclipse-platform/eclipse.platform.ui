/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
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
 * @since 3.2
 * 
 */

public class DetailObservableList extends ObservableList implements IObserving {

	private boolean updating = false;

	private IListChangeListener innerChangeListener = new IListChangeListener() {
		public void handleListChange(ListChangeEvent event) {
			if (!updating) {
				fireListChange(event.diff);
			}
		}
	};

	private Object currentOuterValue;

	private IObservableList innerObservableList;

	private IObservableFactory factory;

	private IObservableValue outerObservableValue;

	private Object detailType;

	/**
	 * @param factory
	 * @param outerObservableValue
	 * @param detailType
	 */
	public DetailObservableList(IObservableFactory factory,
			IObservableValue outerObservableValue, Object detailType) {
		super(outerObservableValue.getRealm(), Collections.EMPTY_LIST,
				detailType);
		Assert.isTrue(!outerObservableValue.isDisposed(),
				"Master observable is disposed"); //$NON-NLS-1$

		this.factory = factory;
		this.outerObservableValue = outerObservableValue;
		this.detailType = detailType;

		outerObservableValue.addDisposeListener(new IDisposeListener() {
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

	IValueChangeListener outerChangeListener = new IValueChangeListener() {
		public void handleValueChange(ValueChangeEvent event) {
			if (isDisposed())
				return;
			ObservableTracker.setIgnore(true);
			try {
				List oldList = new ArrayList(wrappedList);
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
			wrappedList = Collections.EMPTY_LIST;
		} else {
			ObservableTracker.setIgnore(true);
			try {
				innerObservableList = (IObservableList) factory
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

	public boolean add(final Object o) {
		ObservableTracker.setIgnore(true);
		try {
			return wrappedList.add(o);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	public void add(final int index, final Object element) {
		ObservableTracker.setIgnore(true);
		try {
			wrappedList.add(index, element);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	public boolean remove(final Object o) {
		ObservableTracker.setIgnore(true);
		try {
			return wrappedList.remove(o);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	public Object set(final int index, final Object element) {
		ObservableTracker.setIgnore(true);
		try {
			return wrappedList.set(index, element);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	public Object move(final int oldIndex, final int newIndex) {
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

	public Object remove(final int index) {
		ObservableTracker.setIgnore(true);
		try {
			return wrappedList.remove(index);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	public boolean addAll(final Collection c) {
		ObservableTracker.setIgnore(true);
		try {
			return wrappedList.addAll(c);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	public boolean addAll(final int index, final Collection c) {
		ObservableTracker.setIgnore(true);
		try {
			return wrappedList.addAll(index, c);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	public boolean removeAll(final Collection c) {
		ObservableTracker.setIgnore(true);
		try {
			return wrappedList.removeAll(c);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	public boolean retainAll(final Collection c) {
		ObservableTracker.setIgnore(true);
		try {
			return wrappedList.retainAll(c);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	public void clear() {
		ObservableTracker.setIgnore(true);
		try {
			wrappedList.clear();
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

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

	public Object getObserved() {
		if (innerObservableList instanceof IObserving) {
			return ((IObserving) innerObservableList).getObserved();
		}
		return null;
	}
}

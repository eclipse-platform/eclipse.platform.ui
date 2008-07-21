/*******************************************************************************
 * Copyright (c) 2005-2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 147515
 *     Matthew Hall - bug 221351
 *     Ovidio Mallo - bug 241318
 *******************************************************************************/
package org.eclipse.core.internal.databinding.observable.masterdetail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IObserving;
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
		super(outerObservableValue.getRealm(), Collections.EMPTY_LIST, detailType);
		this.factory = factory;
		this.outerObservableValue = outerObservableValue;
		this.detailType = detailType;
		updateInnerObservableList(outerObservableValue);

		outerObservableValue.addValueChangeListener(outerChangeListener);
	}

	IValueChangeListener outerChangeListener = new IValueChangeListener() {
		public void handleValueChange(ValueChangeEvent event) {
			List oldList = new ArrayList(wrappedList);
			updateInnerObservableList(outerObservableValue);
			fireListChange(Diffs.computeListDiff(oldList, wrappedList));
		}
	};

	private void updateInnerObservableList(IObservableValue outerObservableValue) {
		if (innerObservableList != null) {
			innerObservableList.removeListChangeListener(innerChangeListener);
			innerObservableList.dispose();
		}
		currentOuterValue = outerObservableValue.getValue();
		if (currentOuterValue == null) {
			innerObservableList = null;
			wrappedList = Collections.EMPTY_LIST;
		} else {
			this.innerObservableList = (IObservableList) factory
					.createObservable(currentOuterValue);
			wrappedList = innerObservableList;

			if (detailType != null) {
				Object innerValueType = innerObservableList.getElementType();
				Assert.isTrue(getElementType().equals(innerValueType),
						"Cannot change value type in a nested observable list"); //$NON-NLS-1$
			}
			innerObservableList.addListChangeListener(innerChangeListener);
		}
	}

	public boolean add(Object o) {
		return wrappedList.add(o);
	}

	public void add(int index, Object element) {
		wrappedList.add(index, element);
	}

	public boolean remove(Object o) {
		return wrappedList.remove(o);
	}

	public Object set(int index, Object element) {
		return wrappedList.set(index, element);
	}

	public Object move(int oldIndex, int newIndex) {
		if (innerObservableList != null)
			return innerObservableList.move(oldIndex, newIndex);
		return super.move(oldIndex, newIndex);
	}

	public Object remove(int index) {
		return wrappedList.remove(index);
	}

	public boolean addAll(Collection c) {
		return wrappedList.addAll(c);
	}

	public boolean addAll(int index, Collection c) {
		return wrappedList.addAll(index, c);
	}

	public boolean removeAll(Collection c) {
		return wrappedList.removeAll(c);
	}

	public boolean retainAll(Collection c) {
		return wrappedList.retainAll(c);
	}

	public void clear() {
		wrappedList.clear();
	}
	
	public void dispose() {
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

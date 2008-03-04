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
package org.eclipse.core.internal.databinding.observable.masterdetail;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IObserving;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.ObservableSet;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.runtime.Assert;

/**
 * @since 3.2
 * 
 */
public class DetailObservableSet extends ObservableSet implements IObserving {

	private boolean updating = false;

	private ISetChangeListener innerChangeListener = new ISetChangeListener() {
		public void handleSetChange(SetChangeEvent event) {
			if (!updating) {
				fireSetChange(event.diff);
			}
		}
	};

	private Object currentOuterValue;

	private IObservableSet innerObservableSet;

	private IObservableValue outerObservableValue;

	private IObservableFactory factory;

	/**
	 * @param factory
	 * @param outerObservableValue
	 * @param detailType
	 */
	public DetailObservableSet(IObservableFactory factory,
			IObservableValue outerObservableValue, Object detailType) {
		super(outerObservableValue.getRealm(), Collections.EMPTY_SET,
				detailType);
		this.factory = factory;
		this.outerObservableValue = outerObservableValue;
		updateInnerObservableSet(outerObservableValue);

		outerObservableValue.addValueChangeListener(outerChangeListener);
	}

	IValueChangeListener outerChangeListener = new IValueChangeListener() {
		public void handleValueChange(ValueChangeEvent event) {
			Set oldSet = new HashSet(wrappedSet);
			updateInnerObservableSet(outerObservableValue);
			fireSetChange(Diffs.computeSetDiff(oldSet, wrappedSet));
		}
	};

	private void updateInnerObservableSet(IObservableValue outerObservableValue) {
		currentOuterValue = outerObservableValue.getValue();
		if (innerObservableSet != null) {
			innerObservableSet.removeSetChangeListener(innerChangeListener);
			innerObservableSet.dispose();
		}
		if (currentOuterValue == null) {
			innerObservableSet = null;
			wrappedSet = Collections.EMPTY_SET;
		} else {
			this.innerObservableSet = (IObservableSet) factory
					.createObservable(currentOuterValue);
			wrappedSet = innerObservableSet;

			if (elementType != null) {
				Object innerValueType = innerObservableSet.getElementType();

				Assert.isTrue(elementType.equals(innerValueType),
						"Cannot change value type in a nested observable set"); //$NON-NLS-1$
			}

			innerObservableSet.addSetChangeListener(innerChangeListener);
		}
	}

	public boolean add(Object o) {
		getterCalled();
		return wrappedSet.add(o);
	}

	public boolean remove(Object o) {
		getterCalled();
		return wrappedSet.remove(o);
	}

	public boolean addAll(Collection c) {
		getterCalled();
		return wrappedSet.addAll(c);
	}

	public boolean removeAll(Collection c) {
		getterCalled();
		return wrappedSet.removeAll(c);
	}

	public boolean retainAll(Collection c) {
		getterCalled();
		return wrappedSet.retainAll(c);
	}

	public void clear() {
		getterCalled();
		wrappedSet.clear();
	}

	public void dispose() {
		super.dispose();

		if (outerObservableValue != null) {
			outerObservableValue.removeValueChangeListener(outerChangeListener);
			outerObservableValue.dispose();
		}
		if (innerObservableSet != null) {
			innerObservableSet.removeSetChangeListener(innerChangeListener);
			innerObservableSet.dispose();
		}
		currentOuterValue = null;
		factory = null;
		innerObservableSet = null;
		innerChangeListener = null;
	}

	public Object getObserved() {
		if (innerObservableSet instanceof IObserving) {
			return ((IObserving) innerObservableSet).getObserved();
		}
		return null;
	}

}

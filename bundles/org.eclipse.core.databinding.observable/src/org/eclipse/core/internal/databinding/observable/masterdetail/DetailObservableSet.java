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
		super(outerObservableValue.getRealm(), new HashSet(), detailType);
		this.factory = factory;
		this.outerObservableValue = outerObservableValue;
		updateInnerObservableValue(outerObservableValue);

		outerObservableValue.addValueChangeListener(outerChangeListener);
	}

	IValueChangeListener outerChangeListener = new IValueChangeListener() {
		public void handleValueChange(ValueChangeEvent event) {
			Set oldSet = new HashSet(wrappedSet);
			updateInnerObservableValue(outerObservableValue);
			fireSetChange(Diffs.computeSetDiff(oldSet, wrappedSet));
		}
	};

	private void updateInnerObservableValue(
			IObservableValue outerObservableValue) {
		currentOuterValue = outerObservableValue.getValue();
		if (innerObservableSet != null) {
			innerObservableSet.removeSetChangeListener(innerChangeListener);
			innerObservableSet.dispose();
		}
		if (currentOuterValue == null) {
			innerObservableSet = null;
			wrappedSet = new HashSet();
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
			return ((IObserving)innerObservableSet).getObserved();
		}
		return null;
	}

}

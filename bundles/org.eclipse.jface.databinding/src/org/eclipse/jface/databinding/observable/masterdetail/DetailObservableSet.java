/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.databinding.observable.masterdetail;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.databinding.observable.Diffs;
import org.eclipse.jface.databinding.observable.set.IObservableSet;
import org.eclipse.jface.databinding.observable.set.ISetChangeListener;
import org.eclipse.jface.databinding.observable.set.ObservableSet;
import org.eclipse.jface.databinding.observable.set.SetDiff;
import org.eclipse.jface.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.observable.value.IValueChangeListener;
import org.eclipse.jface.databinding.observable.value.ValueDiff;

/**
 * @since 3.2
 * 
 */
/* package */class DetailObservableSet extends ObservableSet {

	private boolean updating = false;

	private ISetChangeListener innerChangeListener = new ISetChangeListener() {
		public void handleSetChange(IObservableSet source, SetDiff diff) {
			if (!updating) {
				fireSetChange(diff);
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
		public void handleValueChange(IObservableValue source, ValueDiff diff) {
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
			Object innerValueType = innerObservableSet.getElementType();
			if (elementType == null) {
				elementType = innerValueType;
			} else {
				Assert.isTrue(elementType.equals(innerValueType),
						"Cannot change value type in a nested updatable value"); //$NON-NLS-1$
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
}

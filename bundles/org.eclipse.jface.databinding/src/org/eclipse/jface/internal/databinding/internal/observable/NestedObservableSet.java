/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.internal.observable;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.internal.databinding.provisional.DataBindingContext;
import org.eclipse.jface.internal.databinding.provisional.description.Property;
import org.eclipse.jface.internal.databinding.provisional.observable.Diffs;
import org.eclipse.jface.internal.databinding.provisional.observable.set.IObservableSet;
import org.eclipse.jface.internal.databinding.provisional.observable.set.ISetChangeListener;
import org.eclipse.jface.internal.databinding.provisional.observable.set.ObservableSet;
import org.eclipse.jface.internal.databinding.provisional.observable.set.SetDiff;
import org.eclipse.jface.internal.databinding.provisional.observable.value.IObservableValue;
import org.eclipse.jface.internal.databinding.provisional.observable.value.IValueChangeListener;
import org.eclipse.jface.internal.databinding.provisional.observable.value.ValueDiff;
import org.eclipse.jface.util.Assert;

/**
 * @since 3.2
 * 
 */
public class NestedObservableSet extends ObservableSet {

	private boolean updating = false;

	private ISetChangeListener innerChangeListener = new ISetChangeListener() {
		public void handleSetChange(IObservableSet source, SetDiff diff) {
			if (!updating) {
				fireSetChange(diff);
			}
		}
	};

	private Object currentOuterValue;

	private Object feature;

	private IObservableSet innerObservableSet;

	private DataBindingContext databindingContext;

	private IObservableValue outerObservableValue;

	/**
	 * @param databindingContext
	 * @param outerObservableValue
	 * @param feature
	 * @param featureType
	 */
	public NestedObservableSet(DataBindingContext databindingContext,
			IObservableValue outerObservableValue, Object feature,
			Object featureType) {
		super(new HashSet(), featureType);
		this.databindingContext = databindingContext;
		this.feature = feature;
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
			this.innerObservableSet = (IObservableSet) databindingContext
					.createObservable(new Property(currentOuterValue, feature));
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
		databindingContext = null;
		feature = null;
		innerObservableSet = null;
		innerChangeListener = null;
	}
}

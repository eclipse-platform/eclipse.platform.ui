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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.internal.databinding.provisional.DataBindingContext;
import org.eclipse.jface.internal.databinding.provisional.description.Property;
import org.eclipse.jface.internal.databinding.provisional.observable.Diffs;
import org.eclipse.jface.internal.databinding.provisional.observable.list.IListChangeListener;
import org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList;
import org.eclipse.jface.internal.databinding.provisional.observable.list.ListDiff;
import org.eclipse.jface.internal.databinding.provisional.observable.list.ObservableList;
import org.eclipse.jface.internal.databinding.provisional.observable.value.IObservableValue;
import org.eclipse.jface.internal.databinding.provisional.observable.value.IValueChangeListener;
import org.eclipse.jface.internal.databinding.provisional.observable.value.ValueDiff;
import org.eclipse.jface.util.Assert;

/**
 * @since 3.2
 * 
 */
public class NestedObservableList extends ObservableList {

	private boolean updating = false;

	private IListChangeListener innerChangeListener = new IListChangeListener() {
		public void handleListChange(IObservableList source, ListDiff diff) {
			if (!updating) {
				fireListChange(diff);
			}
		}
	};

	private Object currentOuterValue;

	private Object feature;

	private IObservableList innerObservableList;

	private DataBindingContext databindingContext;

	private IObservableValue outerObservableValue;

	/**
	 * @param databindingContext
	 * @param outerObservableValue
	 * @param feature
	 * @param featureType
	 */
	public NestedObservableList(DataBindingContext databindingContext,
			IObservableValue outerObservableValue, Object feature,
			Class featureType) {
		super(new ArrayList(), featureType);
		this.databindingContext = databindingContext;
		this.feature = feature;
		this.outerObservableValue = outerObservableValue;
		updateInnerObservableValue(outerObservableValue);

		outerObservableValue.addValueChangeListener(outerChangeListener);
	}

	IValueChangeListener outerChangeListener = new IValueChangeListener() {
		public void handleValueChange(IObservableValue source, ValueDiff diff) {
			List oldList = new ArrayList(wrappedList);
			updateInnerObservableValue(outerObservableValue);
			fireListChange(Diffs.computeListDiff(oldList, wrappedList));
		}
	};

	private void updateInnerObservableValue(
			IObservableValue outerObservableValue) {
		currentOuterValue = outerObservableValue.getValue();
		if (innerObservableList != null) {
			innerObservableList.removeListChangeListener(innerChangeListener);
			innerObservableList.dispose();
		}
		if (currentOuterValue == null) {
			innerObservableList = null;
			wrappedList = new ArrayList();
		} else {
			this.innerObservableList = (IObservableList) databindingContext
					.createObservable(new Property(currentOuterValue, feature,
							(Class) getElementType(), Boolean.TRUE));
			wrappedList = innerObservableList;
			Object innerValueType = innerObservableList.getElementType();
			Assert.isTrue(getElementType().equals(innerValueType),
					"Cannot change value type in a nested updatable value"); //$NON-NLS-1$
			innerObservableList.addListChangeListener(innerChangeListener);
		}
	}

	public void dispose() {
		super.dispose();

		if (outerObservableValue != null) {
			outerObservableValue.removeValueChangeListener(outerChangeListener);
			outerObservableValue.dispose();
		}
		if (innerObservableList != null) {
			innerObservableList.removeListChangeListener(innerChangeListener);
			innerObservableList.dispose();
		}
		currentOuterValue = null;
		databindingContext = null;
		feature = null;
		innerObservableList = null;
		innerChangeListener = null;
	}

}

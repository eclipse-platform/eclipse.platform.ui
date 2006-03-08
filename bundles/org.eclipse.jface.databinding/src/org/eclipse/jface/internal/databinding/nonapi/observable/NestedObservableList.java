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
package org.eclipse.jface.internal.databinding.nonapi.observable;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.internal.databinding.api.IDataBindingContext;
import org.eclipse.jface.internal.databinding.api.description.Property;
import org.eclipse.jface.internal.databinding.api.observable.Diffs;
import org.eclipse.jface.internal.databinding.api.observable.list.IListChangeListener;
import org.eclipse.jface.internal.databinding.api.observable.list.IListDiff;
import org.eclipse.jface.internal.databinding.api.observable.list.IObservableList;
import org.eclipse.jface.internal.databinding.api.observable.list.ObservableList;
import org.eclipse.jface.internal.databinding.api.observable.value.IObservableValue;
import org.eclipse.jface.internal.databinding.api.observable.value.IValueChangeListener;
import org.eclipse.jface.internal.databinding.api.observable.value.IValueDiff;
import org.eclipse.jface.util.Assert;

/**
 * @since 3.2
 * 
 */
public class NestedObservableList extends ObservableList {

	private boolean updating = false;

	private IListChangeListener innerChangeListener = new IListChangeListener() {
		public void handleListChange(IObservableList source, IListDiff diff) {
			if (!updating) {
				fireListChange(diff);
			}
		}
	};

	private Object currentOuterValue;

	private Object feature;

	private IObservableList innerObservableList;

	private IDataBindingContext databindingContext;

	private IObservableValue outerObservableValue;

	/**
	 * @param databindingContext
	 * @param outerObservableValue
	 * @param feature
	 * @param featureType
	 */
	public NestedObservableList(IDataBindingContext databindingContext,
			IObservableValue outerObservableValue, Object feature,
			Object featureType) {
		super(new ArrayList());
		this.databindingContext = databindingContext;
		this.feature = feature;
		this.outerObservableValue = outerObservableValue;
		elementType = featureType;
		updateInnerObservableValue(outerObservableValue);

		outerObservableValue.addValueChangeListener(outerChangeListener);
	}

	IValueChangeListener outerChangeListener = new IValueChangeListener() {
		public void handleValueChange(IObservableValue source, IValueDiff diff) {
			List oldList = new ArrayList(wrappedList);
			updateInnerObservableValue(outerObservableValue);
			fireListChange(Diffs.computeDiff(oldList, wrappedList));
		}
	};

	private Object elementType;

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
					.createObservable(new Property(currentOuterValue, feature));
			wrappedList = innerObservableList;
			Object innerValueType = innerObservableList.getElementType();
			if (elementType == null) {
				elementType = innerValueType;
			} else {
				Assert.isTrue(elementType.equals(innerValueType),
						"Cannot change value type in a nested updatable value"); //$NON-NLS-1$
			}
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

	public Object getElementType() {
		return elementType;
	}
}

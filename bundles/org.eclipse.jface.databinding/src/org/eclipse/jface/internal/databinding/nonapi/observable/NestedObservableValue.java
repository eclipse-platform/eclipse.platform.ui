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
package org.eclipse.jface.internal.databinding.nonapi.observable;

import org.eclipse.jface.internal.databinding.api.IDataBindingContext;
import org.eclipse.jface.internal.databinding.api.Property;
import org.eclipse.jface.internal.databinding.api.observable.value.AbstractObservableValue;
import org.eclipse.jface.internal.databinding.api.observable.value.IObservableValue;
import org.eclipse.jface.internal.databinding.api.observable.value.IValueChangeListener;
import org.eclipse.jface.internal.databinding.api.observable.value.IValueDiff;
import org.eclipse.jface.internal.databinding.api.observable.value.ValueDiff;
import org.eclipse.jface.util.Assert;

/**
 * @since 3.2
 * 
 */
public class NestedObservableValue extends AbstractObservableValue {

	private boolean updating = false;

	private IValueChangeListener innerChangeListener = new IValueChangeListener() {
		public void handleValueChange(IObservableValue source, IValueDiff diff) {
			if (!updating) {
				fireValueChange(diff);
			}
		}
	};

	private Object currentOuterValue;

	private Object feature;

	private IObservableValue innerObservableValue;

	private IDataBindingContext databindingContext;

	private Object featureType;

	private IObservableValue outerObservableValue;

	/**
	 * @param databindingContext
	 * @param outerObservableValue
	 * @param feature
	 * @param featureType
	 */
	public NestedObservableValue(IDataBindingContext databindingContext,
			IObservableValue outerObservableValue, Object feature,
			Class featureType) {
		this.databindingContext = databindingContext;
		this.feature = feature;
		this.featureType = featureType;
		this.outerObservableValue = outerObservableValue;
		updateInnerObservableValue(outerObservableValue);

		outerObservableValue.addValueChangeListener(outerChangeListener);
	}

	IValueChangeListener outerChangeListener = new IValueChangeListener() {
		public void handleValueChange(IObservableValue source, IValueDiff diff) {
			Object oldValue = doGetValue();
			updateInnerObservableValue(outerObservableValue);
			fireValueChange(new ValueDiff(oldValue, doGetValue()));
		}
	};

	private void updateInnerObservableValue(
			IObservableValue outerObservableValue) {
		currentOuterValue = outerObservableValue.getValue();
		if (innerObservableValue != null) {
			innerObservableValue.removeValueChangeListener(innerChangeListener);
			innerObservableValue.dispose();
		}
		if (currentOuterValue == null) {
			innerObservableValue = null;
		} else {
			this.innerObservableValue = (IObservableValue) databindingContext
					.createObservable(new Property(currentOuterValue, feature));
			Object innerValueType = innerObservableValue.getValueType();
			if (featureType == null) {
				featureType = innerValueType;
			} else {
				Assert
						.isTrue(featureType.equals(innerValueType),
								"Cannot change value type in a nested observable value"); //$NON-NLS-1$
			}
			innerObservableValue.addValueChangeListener(innerChangeListener);
		}
	}

	public void setValue(Object value) {
		if (innerObservableValue != null)
			innerObservableValue.setValue(value);
	}

	public Object doGetValue() {
		return innerObservableValue == null ? null : innerObservableValue
				.getValue();
	}

	public Object getValueType() {
		return featureType;
	}

	public void dispose() {
		super.dispose();

		if (outerObservableValue != null) {
			outerObservableValue.removeValueChangeListener(outerChangeListener);
			outerObservableValue.dispose();
		}
		if (innerObservableValue != null) {
			innerObservableValue.removeValueChangeListener(innerChangeListener);
			innerObservableValue.dispose();
		}
		currentOuterValue = null;
		databindingContext = null;
		feature = null;
		innerObservableValue = null;
		innerChangeListener = null;
	}

	public IObservableValue getInnerObservableValue() {
		return innerObservableValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.internal.databinding.INestedObservableValue#getOuterObservableValue()
	 */
	public IObservableValue getOuterObservableValue() {
		return outerObservableValue;
	}
}

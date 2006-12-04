/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 164653
 *******************************************************************************/
package org.eclipse.core.databinding.observable.masterdetail;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.core.runtime.Assert;

/**
 * @since 1.0
 * 
 */
/* package */class DetailObservableValue extends AbstractObservableValue {

	private boolean updating = false;

	private IValueChangeListener innerChangeListener = new IValueChangeListener() {
		public void handleValueChange(IObservableValue source, ValueDiff diff) {
			if (!updating) {
				fireValueChange(diff);
			}
		}
	};

	private Object currentOuterValue;

	private IObservableValue innerObservableValue;

	private Object detailType;

	private IObservableValue outerObservableValue;

	private IObservableFactory factory;

	/**
	 * @param realm 
	 * @param factory
	 * @param outerObservableValue
	 * @param detailType
	 */
	public DetailObservableValue(IObservableFactory factory,
			IObservableValue outerObservableValue, Object detailType) {
		super(outerObservableValue.getRealm());
		this.factory = factory;
		this.detailType = detailType;
		this.outerObservableValue = outerObservableValue;
		updateInnerObservableValue(outerObservableValue);

		outerObservableValue.addValueChangeListener(outerChangeListener);
	}

	IValueChangeListener outerChangeListener = new IValueChangeListener() {
		public void handleValueChange(IObservableValue source, ValueDiff diff) {
			Object oldValue = doGetValue();
			updateInnerObservableValue(outerObservableValue);
			fireValueChange(Diffs.createValueDiff(oldValue, doGetValue()));
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
			this.innerObservableValue = (IObservableValue) factory
					.createObservable(currentOuterValue);
			Object innerValueType = innerObservableValue.getValueType();
			if (detailType == null) {
				detailType = innerValueType;
			} else {
				Assert
						.isTrue(detailType.equals(innerValueType),
								"Cannot change value type in a nested observable value"); //$NON-NLS-1$
			}
			innerObservableValue.addValueChangeListener(innerChangeListener);
		}
	}

	public void doSetValue(Object value) {
		if (innerObservableValue != null)
			innerObservableValue.setValue(value);
	}

	public Object doGetValue() {
		return innerObservableValue == null ? null : innerObservableValue
				.getValue();
	}

	public Object getValueType() {
		return detailType;
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
		factory = null;
		innerObservableValue = null;
		innerChangeListener = null;
	}

}

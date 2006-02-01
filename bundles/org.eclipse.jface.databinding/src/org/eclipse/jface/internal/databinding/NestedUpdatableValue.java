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
package org.eclipse.jface.internal.databinding;

import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.IChangeListener;
import org.eclipse.jface.databinding.IDataBindingContext;
import org.eclipse.jface.databinding.INestedUpdatableValue;
import org.eclipse.jface.databinding.IUpdatableValue;
import org.eclipse.jface.databinding.Property;
import org.eclipse.jface.databinding.UpdatableValue;
import org.eclipse.jface.util.Assert;

/**
 * @since 3.2
 * 
 */
public class NestedUpdatableValue extends UpdatableValue implements INestedUpdatableValue {
	
	private boolean updating=false;

	private IChangeListener innerChangeListener = new IChangeListener() {
		public void handleChange(ChangeEvent changeEvent) {
			if (!updating) {
				fireChangeEvent(changeEvent.getChangeType(), changeEvent
						.getOldValue(), changeEvent.getNewValue());
			}
		}
	};

	private Object currentOuterValue;

	private Object feature;

	private IUpdatableValue innerUpdatableValue;

	private IDataBindingContext databindingContext;

	private Class featureType;

	private IUpdatableValue outerUpdatableValue;

	/**
	 * @param databindingContext
	 * @param outerUpdatableValue
	 * @param feature
	 * @param featureType 
	 */
	public NestedUpdatableValue(IDataBindingContext databindingContext,
			IUpdatableValue outerUpdatableValue, Object feature, Class featureType) {
		this.databindingContext = databindingContext;
		this.feature = feature;
		this.featureType = featureType;
		this.outerUpdatableValue = outerUpdatableValue;
		updateInnerUpdatableValue(outerUpdatableValue);
		
		outerUpdatableValue.addChangeListener(outerChangeListener);
	}

	IChangeListener outerChangeListener = new IChangeListener() {
		public void handleChange(ChangeEvent changeEvent) {
			if (changeEvent.getChangeType() == ChangeEvent.CHANGE) {
				Object oldValue = computeValue();
				updateInnerUpdatableValue(outerUpdatableValue);
				fireChangeEvent(ChangeEvent.CHANGE, oldValue, computeValue());
			}
		}
	};
	
	private void updateInnerUpdatableValue(IUpdatableValue outerUpdatableValue) {
		currentOuterValue = outerUpdatableValue.getValue();
		if (innerUpdatableValue != null) {
			innerUpdatableValue.removeChangeListener(innerChangeListener);
			innerUpdatableValue.dispose();
		}
		if (currentOuterValue == null) {
			innerUpdatableValue = null;
		} else {
			this.innerUpdatableValue = (IUpdatableValue) databindingContext
					.createUpdatable(new Property(currentOuterValue, feature));
			Class innerValueType = innerUpdatableValue.getValueType();
			if(featureType==null) {
				featureType = innerValueType;
			} else {
				Assert.isTrue(featureType.equals(innerValueType), "Cannot change value type in a nested updatable value"); //$NON-NLS-1$
			}
			innerUpdatableValue.addChangeListener(innerChangeListener);
		}
	}

	public void setValue(Object value) {
		if (innerUpdatableValue!=null)
		   innerUpdatableValue.setValue(value);
	}

	public Object computeValue() {
		return innerUpdatableValue == null ? null : innerUpdatableValue
				.getValue();
	}

	public Class getValueType() {
		return featureType;
	}

	public void dispose() {
		super.dispose();

		if (outerUpdatableValue != null) {
			outerUpdatableValue.removeChangeListener(outerChangeListener);
			outerUpdatableValue.dispose();
		}
		if (innerUpdatableValue != null) {
			innerUpdatableValue.removeChangeListener(innerChangeListener);
			innerUpdatableValue.dispose();
		}
		currentOuterValue = null;
		databindingContext = null;
		feature = null;
		innerUpdatableValue = null;
		innerChangeListener = null;
	}

	public IUpdatableValue getInnerUpdatableValue() {
		return innerUpdatableValue;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.INestedUpdatableValue#getOuterUpdatableValue()
	 */
	public IUpdatableValue getOuterUpdatableValue() {
		return outerUpdatableValue;
	}
}

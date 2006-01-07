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
package org.eclipse.jface.databinding.internal;

import org.eclipse.jface.databinding.BindingException;
import org.eclipse.jface.databinding.IChangeEvent;
import org.eclipse.jface.databinding.IChangeListener;
import org.eclipse.jface.databinding.IDataBindingContext;
import org.eclipse.jface.databinding.IUpdatableValue;
import org.eclipse.jface.databinding.PropertyDescription;
import org.eclipse.jface.databinding.UpdatableValue;

/**
 * @since 3.2
 * 
 */
public class NestedUpdatableValue extends UpdatableValue {
	
	private boolean updating=false;

	private IChangeListener innerChangeListener = new IChangeListener() {
		public void handleChange(IChangeEvent changeEvent) {
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

	/**
	 * @param databindingContext
	 * @param outerUpdatableValue
	 * @param feature
	 * @param featureType 
	 */
	public NestedUpdatableValue(IDataBindingContext databindingContext,
			final IUpdatableValue outerUpdatableValue, Object feature, Class featureType) {
		this.databindingContext = databindingContext;
		this.feature = feature;
		this.featureType = featureType;
		updateInnerUpdatableValue(outerUpdatableValue);
		IChangeListener outerChangeListener = new IChangeListener() {
			public void handleChange(IChangeEvent changeEvent) {
				Object oldValue = getValue();
				updateInnerUpdatableValue(outerUpdatableValue);
				fireChangeEvent(IChangeEvent.CHANGE, oldValue, getValue());
			}
		};
		outerUpdatableValue.addChangeListener(outerChangeListener);
	}

	private void updateInnerUpdatableValue(IUpdatableValue outerUpdatableValue) {
		currentOuterValue = outerUpdatableValue.getValue();
		if (innerUpdatableValue != null) {
			innerUpdatableValue.removeChangeListener(innerChangeListener);
			innerUpdatableValue.dispose();
		}
		if (currentOuterValue == null) {
			innerUpdatableValue = null;
		} else {
			try {
				this.innerUpdatableValue = (IUpdatableValue) databindingContext
						.createUpdatable(new PropertyDescription(currentOuterValue, feature));
				Class innerValueType = innerUpdatableValue.getValueType();
				if(featureType==null) {
					featureType = innerValueType;
				} else {
					if(!featureType.equals(innerValueType)) {
						throw new AssertionError("Cannot change value type in a nested updatable value"); //$NON-NLS-1$
					}
				}
			} catch (BindingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			innerUpdatableValue.addChangeListener(innerChangeListener);
		}
	}

	public void setValue(Object value) {
		innerUpdatableValue.setValue(value);
	}

	public Object getValue() {
		return innerUpdatableValue == null ? null : innerUpdatableValue
				.getValue();
	}

	public Class getValueType() {
		return featureType;
	}

	public void dispose() {
		super.dispose();
		if (innerUpdatableValue != null) {
			innerUpdatableValue.dispose();
		}
		currentOuterValue = null;
		databindingContext = null;
		feature = null;
		innerUpdatableValue = null;
		innerChangeListener = null;
	}

}

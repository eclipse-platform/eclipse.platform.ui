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
package org.eclipse.jface.binding.internal;

import org.eclipse.jface.binding.BindingException;
import org.eclipse.jface.binding.DatabindingService;
import org.eclipse.jface.binding.IChangeEvent;
import org.eclipse.jface.binding.IChangeListener;
import org.eclipse.jface.binding.IUpdatableValue;
import org.eclipse.jface.binding.UpdatableValue;

/**
 * @since 3.2
 *
 */
public class DerivedUpdatableValue extends UpdatableValue {

	private IChangeListener innerChangeListener = new IChangeListener() {
		public void handleChange(IChangeEvent changeEvent) {
			fireChangeEvent(changeEvent.getChangeType(), changeEvent
					.getOldValue(), changeEvent.getNewValue());
		}
	};

	private Object currentOuterValue;

	private Object feature;

	private IUpdatableValue innerUpdatableValue;

	private DatabindingService databindingService;

	/**
	 * @param databindingService
	 * @param outerUpdatableValue
	 * @param feature
	 */
	public DerivedUpdatableValue(DatabindingService databindingService,
			final IUpdatableValue outerUpdatableValue, Object feature) {
		this.databindingService = databindingService;
		this.feature = feature;
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
				this.innerUpdatableValue = (IUpdatableValue) databindingService
						.createUpdatable(currentOuterValue, feature);
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
		return innerUpdatableValue.getValueType();
	}

	public void dispose() {
		super.dispose();
		if (innerUpdatableValue != null) {
			innerUpdatableValue.dispose();
		}
		currentOuterValue = null;
		databindingService = null;
		feature = null;
		innerUpdatableValue = null;
		innerChangeListener = null;
	}

}

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
import org.eclipse.jface.databinding.IUpdatableCollection;
import org.eclipse.jface.databinding.IUpdatableValue;
import org.eclipse.jface.databinding.PropertyDescription;
import org.eclipse.jface.databinding.Updatable;
import org.eclipse.jface.util.Assert;

/**
 * @since 3.2
 * 
 */
public class NestedUpdatableCollection extends Updatable implements
		IUpdatableCollection {

	private boolean updating = false;

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

	private IUpdatableCollection innerUpdatableCollection;

	private IDataBindingContext databindingContext;

	private Class elementType;

	/**
	 * @param databindingContext
	 * @param outerUpdatableValue
	 * @param feature
	 * @param elementType 
	 */
	public NestedUpdatableCollection(IDataBindingContext databindingContext,
			final IUpdatableValue outerUpdatableValue, Object feature,
			Class elementType) {
		this.databindingContext = databindingContext;
		this.feature = feature;
		this.elementType = elementType;
		updateInnerUpdatableValue(outerUpdatableValue);
		IChangeListener outerChangeListener = new IChangeListener() {
			public void handleChange(IChangeEvent changeEvent) {
				updateInnerUpdatableValue(outerUpdatableValue);
				fireChangeEvent(IChangeEvent.CHANGE, null, null, -1);
			}
		};
		outerUpdatableValue.addChangeListener(outerChangeListener);
	}

	private void updateInnerUpdatableValue(IUpdatableValue outerUpdatableValue) {
		currentOuterValue = outerUpdatableValue.getValue();
		if (innerUpdatableCollection != null) {
			innerUpdatableCollection.removeChangeListener(innerChangeListener);
			innerUpdatableCollection.dispose();
		}
		if (currentOuterValue == null) {
			innerUpdatableCollection = null;
		} else {
			try {
				this.innerUpdatableCollection = (IUpdatableCollection) databindingContext
						.createUpdatable(new PropertyDescription(
								currentOuterValue, feature, elementType, Boolean.TRUE));
				Class innerElementType = innerUpdatableCollection
						.getElementType();
				if (elementType == null) {
					elementType = innerElementType;
				} else {
					Assert.isTrue(elementType.equals(innerElementType), "Cannot change element type in a nested updatable collection"); //$NON-NLS-1$
				}
			} catch (BindingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			innerUpdatableCollection.addChangeListener(innerChangeListener);
		}
	}

	// public void setValue(Object value) {
	// innerUpdatableValue.setValue(value);
	// }
	//
	// public Object getValue() {
	// return innerUpdatableValue == null ? null : innerUpdatableValue
	// .getValue();
	// }
	//
	// public Class getValueType() {
	// return featureType;
	// }

	public void dispose() {
		super.dispose();
		if (innerUpdatableCollection != null) {
			innerUpdatableCollection.dispose();
		}
		currentOuterValue = null;
		databindingContext = null;
		feature = null;
		innerUpdatableCollection = null;
		innerChangeListener = null;
	}

	public int getSize() {
		return innerUpdatableCollection == null ? 0 : innerUpdatableCollection
				.getSize();
	}

	public int addElement(Object value, int index) {
		return innerUpdatableCollection.addElement(value, index);
	}

	public void removeElement(int index) {
		innerUpdatableCollection.removeElement(index);
	}

	public void setElement(int index, Object value) {
		innerUpdatableCollection.setElement(index, value);
	}

	public Object getElement(int index) {
		return innerUpdatableCollection.getElement(index);
	}

	public Class getElementType() {
		return elementType;
	}

}

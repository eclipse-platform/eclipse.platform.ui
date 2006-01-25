/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.databinding.updatables;

import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;

import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.IChangeListener;
import org.eclipse.jface.databinding.IUpdatableValue;
import org.eclipse.jface.databinding.UpdatableValue;

/**
 * An IUpdatableValue implementation that represents a calculated value
 * that depends on the values of other IUpdatables or Java Beans.
 * <p>
 * Exposes an IChangeListener and a PropertyChangeListener that may
 * be added to objects on which this calculation depends.
 * 
 * @since 3.2
 */
public abstract class CalculatedValue extends UpdatableValue {
	
	private Class valueType;

	/**
	 * Construct a CalculatedUpdatableValue for the specified valueType
	 * @param valueType
	 */
	public CalculatedValue(Class valueType) {
		this.valueType = valueType;
		handleChange();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.IUpdatableValue#setValue(java.lang.Object)
	 */
	public void setValue(Object value) {
		throw new UnsupportedOperationException("CalculatedUpdatableValue objects are read-only"); //$NON-NLS-1$
	}
	
	private Object calculatedResult = null;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.IUpdatableValue#getValueType()
	 */
	public Class getValueType() {
		return valueType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.IUpdatableValue#getValue()
	 */
	public Object getValue() {
		return calculatedResult;
	}
	
	/**
	 * Perform the calculation represented by this CalculatedValue.
	 * 
	 * @return the result of the calculation.
	 */
	protected abstract Object calculate();
	
	private boolean modifying = false;
	
	protected void handleChange() {
		if (!modifying) {
			modifying = true;
			try {
				Object oldValue = calculatedResult;
				calculatedResult = calculate();
				fireChangeEvent(ChangeEvent.CHANGE, oldValue, calculatedResult);
			} finally {
				modifying = false;
			}
		}
	}

	private final IChangeListener updatableChangeListener = new IChangeListener() {
		/* (non-Javadoc)
		 * @see org.eclipse.jface.databinding.IChangeListener#handleChange(org.eclipse.jface.databinding.ChangeEvent)
		 */
		public void handleChange(ChangeEvent changeEvent) {
			CalculatedValue.this.handleChange();
		}
	};
	
	private final PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
		/* (non-Javadoc)
		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
		 */
		public void propertyChange(java.beans.PropertyChangeEvent arg0) {
			CalculatedValue.this.handleChange();
		}
	};

	/**
	 * @return Returns the propertyChangeListener.
	 */
	public PropertyChangeListener getPropertyChangeListener() {
		return propertyChangeListener;
	}

	/**
	 * @return Returns the updatableChangeListener.
	 */
	public IChangeListener getUpdatableChangeListener() {
		return updatableChangeListener;
	}
	
	private IUpdatableValue[] dependantUpdatableValues;

	/**
	 * Adds an array of IUpdatableValue objects as objects on which this
	 * calculation depends.  Whenever any of these objects changes, this
	 * object will recalculate.
	 * 
	 * @param values An array of IUpdatableValue objects to set.
	 */
	public void setDependencies(IUpdatableValue[] values) {
		unhookDependantUpdatableValues();
		this.dependantUpdatableValues = values;
		hookDependantUpdatableValues();
	}

	/**
	 * @return Returns the dependantUpdatableValues.
	 */
	protected IUpdatableValue[] getDependantUpdatableValues() {
		return dependantUpdatableValues;
	}

	private void unhookDependantUpdatableValues() {
		if (dependantUpdatableValues == null)
			return;
		
		for (int i = 0; i < dependantUpdatableValues.length; i++) {
			dependantUpdatableValues[i].removeChangeListener(getUpdatableChangeListener());
		}
	}

	private void hookDependantUpdatableValues() {
		for (int i = 0; i < dependantUpdatableValues.length; i++) {
			dependantUpdatableValues[i].addChangeListener(getUpdatableChangeListener());
		}
	}
	
	private Object[] dependantJavaBeans;

	/**
	 * Adds an array of IUpdatableValue objects as objects on which this
	 * calculation depends.  Whenever any of these objects changes, this
	 * object will recalculate.
	 * 
	 * @param objects An array of Java bean objects to set.
	 */
	public void setDependencies(Object[] objects) {
		unhookDependantJavaBeans();
		this.dependantJavaBeans = objects;
		hookDependantJavaBeans();
	}
	
	private void addPropertyChangeListener(Object bean) {
		try {
			Method addPCL = bean.getClass().getMethod("addPropertyChangeListener", new Class[] {PropertyChangeListener.class}); //$NON-NLS-1$
			addPCL.invoke(bean, new Object[] {propertyChangeListener});
		} catch (Exception e) {
			throw new IllegalArgumentException(bean.getClass().getName() + " does not define addPropertyChangeListener" + e); //$NON-NLS-1$
		}
	}
	
	private void removePropertyChangeListener(Object bean) {
		try {
			Method addPCL = bean.getClass().getMethod("removePropertyChangeListener", new Class[] {PropertyChangeListener.class}); //$NON-NLS-1$
			addPCL.invoke(bean, new Object[] {propertyChangeListener});
		} catch (Exception e) {
			throw new IllegalArgumentException(bean.getClass().getName() + " does not define removePropertyChangeListener" + e); //$NON-NLS-1$
		}
	}

	private void unhookDependantJavaBeans() {
		if (dependantJavaBeans == null)
			return;
		
		for (int i = 0; i < dependantJavaBeans.length; i++) {
			removePropertyChangeListener(dependantJavaBeans[i]);
		}
	}

	private void hookDependantJavaBeans() {
		for (int i = 0; i < dependantJavaBeans.length; i++) {
			addPropertyChangeListener(dependantJavaBeans[i]);
		}
	}

}

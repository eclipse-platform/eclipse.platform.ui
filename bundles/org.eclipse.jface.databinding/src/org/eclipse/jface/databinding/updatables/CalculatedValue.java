/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     The Pampered Chef - Initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.databinding.updatables;

import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.IChangeListener;
import org.eclipse.jface.databinding.IDataBindingContext;
import org.eclipse.jface.databinding.IUpdatable;
import org.eclipse.jface.databinding.UpdatableValue;

/**
 * An IUpdatableValue implementation that represents a calculated value
 * that depends on the values of other IUpdatables.
 * <p>
 * Exposes an IChangeListener that may be added to objects on which this 
 * calculation depends.
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
	public Object computeValue() {
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
	
	/**
	 * @return Returns the updatableChangeListener.
	 */
	public IChangeListener getUpdatableChangeListener() {
		return updatableChangeListener;
	}
	
	private IUpdatable[] dependantUpdatableValues;
	private boolean selfMadeUpdatables;

	/**
	 * Adds an array of IUpdatable objects as objects on which this
	 * calculation depends.  Whenever any of these objects changes, this
	 * object will recalculate.
	 * 
	 * @param values An array of IUpdatable objects to set.
	 */
	public void setDependencies(IUpdatable[] values) {
		unhookDependantUpdatableValues();
		this.dependantUpdatableValues = values;
		hookDependantUpdatableValues();
		selfMadeUpdatables=false;
	}

	/**
	 * @return Returns the dependantUpdatableValues.
	 */
	protected IUpdatable[] getDependantUpdatableValues() {
		return dependantUpdatableValues;
	}

	private void unhookDependantUpdatableValues() {
		if (dependantUpdatableValues == null)
			return;
		
		for (int i = 0; i < dependantUpdatableValues.length; i++) {
			dependantUpdatableValues[i].removeChangeListener(getUpdatableChangeListener());
			if (selfMadeUpdatables) {
				dependantUpdatableValues[i].dispose();
			}
		}
		dependantUpdatableValues = null;
	}

	private void hookDependantUpdatableValues() {
		for (int i = 0; i < dependantUpdatableValues.length; i++) {
			dependantUpdatableValues[i].addChangeListener(getUpdatableChangeListener());
		}
	}
	
	/**
	 * Adds an array of dependencies.  These objects must be all convertable
	 * to instances of IUpdatable using IDataBindingContext#createUpdatable.
	 * <p>
	 * Often this is used to make this CalculatedValue depend on one or more
	 * JavaBean property change events, but anything that can be converted to
	 * an IUpdatable will work.
	 *  
	 * @param dbc The data binding context to use for converting objects into IUpdatableValues
	 * @param descriptionObjects An array of description objects that are convertable to IUpdatableValues.
	 */
	public void setDependencies(IDataBindingContext dbc, Object[] descriptionObjects) {
		IUpdatable[] values = convertToUpdatables(dbc, descriptionObjects);
		setDependencies(values);
		selfMadeUpdatables = true;
	}
	
	private IUpdatable[] convertToUpdatables(IDataBindingContext dbc, Object[] objects) {
		IUpdatable[] result = new IUpdatable[objects.length];
		for (int i = 0; i < objects.length; i++) {
			result[i] = dbc.createUpdatable(objects[i]);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.Updatable#dispose()
	 */
	public void dispose() {
		unhookDependantUpdatableValues();
		super.dispose();
	}
}

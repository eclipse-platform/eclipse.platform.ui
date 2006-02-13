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

package org.eclipse.jface.internal.databinding.api.observable.value;

/**
 * @since 3.2
 * 
 */
public class WritableValue extends AbstractObservableValue {

	private final Object valueType;

	public WritableValue(Object initialValue) {
		this(null, initialValue);
	}

	public WritableValue(Class type) {
		this(type, null);
	}
	
	public WritableValue(Object valueType, Object initialValue) {
		this.valueType = valueType;
		this.value = initialValue;
	}
	
	private Object value = null;

	public Object doGetValue() {
		return value;
	}

	/**
	 * @param value
	 *            The value to set.
	 */
	public void setValue(Object value) {
		fireValueChange(new ValueDiff(this.value, this.value = value));
	}

	public Object getValueType() {
		return valueType;
	}

}

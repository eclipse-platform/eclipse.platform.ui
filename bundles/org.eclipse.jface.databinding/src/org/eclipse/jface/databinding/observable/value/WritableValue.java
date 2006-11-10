/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 158687
 ******************************************************************************/

package org.eclipse.jface.databinding.observable.value;

import org.eclipse.jface.databinding.observable.Diffs;
import org.eclipse.jface.databinding.observable.Realm;

/**
 * @since 1.0
 * 
 */
public class WritableValue extends AbstractObservableValue {

	private final Object valueType;

	/**
	 * @param realm 
	 * @param initialValue
	 */
	public WritableValue(Object initialValue) {
		this(Realm.getDefault(), null, initialValue);
	}

	/**
	 * @param realm 
	 * @param type
	 */
	public WritableValue(Class type) {
		this(Realm.getDefault(), type, null);
	}

	/**
	 * @param realm 
	 * @param valueType
	 * @param initialValue
	 */
	public WritableValue(Object valueType, Object initialValue) {
		this(Realm.getDefault(), valueType, initialValue);
	}

	/**
	 * @param realm 
	 * @param initialValue
	 */
	public WritableValue(Realm realm, Object initialValue) {
		this(realm, null, initialValue);
	}
	
	/**
	 * @param realm 
	 * @param type
	 */
	public WritableValue(Realm realm, Class type) {
		this(realm, type, null);
	}
	
	/**
	 * @param realm 
	 * @param valueType
	 * @param initialValue
	 */
	public WritableValue(Realm realm, Object valueType, Object initialValue) {
		super(realm);
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
        boolean changed = false;

        if (this.value == null && value != null) {
            changed = true;
        } else if (this.value != null && !this.value.equals(value)) {
            changed = true;
        }

        if (changed) {
            fireValueChange(Diffs.createValueDiff(this.value, this.value = value));
        }
	}

	public Object getValueType() {
		return valueType;
	}

}

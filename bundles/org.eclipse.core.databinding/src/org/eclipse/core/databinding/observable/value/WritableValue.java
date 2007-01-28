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
 *     Brad Reynolds - bug 164653, 147515
 ******************************************************************************/

package org.eclipse.core.databinding.observable.value;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;

/**
 * @since 1.0
 * 
 */
public class WritableValue extends AbstractObservableValue {

	private final Object valueType;

	/**
	 * Constructs a new instance with the default realm, a <code>null</code>
	 * value type, and a <code>null</code> value.
	 */
	public WritableValue() {
		this(null, null);
	}

	/**
	 * Constructs a new instance with the default realm.
	 * 
	 * @param valueType
	 *            can be <code>null</code>
	 * @param initialValue
	 *            can be <code>null</code>
	 */
	public WritableValue(Object valueType, Object initialValue) {
		this(Realm.getDefault(), valueType, initialValue);
	}

	/**
	 * Constructs a new instance with the provided <code>realm</code>, a
	 * <code>null</code> value type, and a <code>null</code> initial value.
	 * 
	 * @param realm
	 */
	public WritableValue(Realm realm) {
		this(realm, null, null);
	}

	/**
	 * Constructs a new instance.
	 * 
	 * @param realm
	 * @param valueType
	 *            can be <code>null</code>
	 * @param initialValue
	 *            can be <code>null</code>
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
	public void doSetValue(Object value) {
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

	/**
	 * @param elementType can be <code>null</code>
	 * @return new instance with the default realm and a value of <code>null</code>
	 */
	public static WritableValue withValueType(Object elementType) {
		return new WritableValue(Realm.getDefault(), elementType, null );
	}
}

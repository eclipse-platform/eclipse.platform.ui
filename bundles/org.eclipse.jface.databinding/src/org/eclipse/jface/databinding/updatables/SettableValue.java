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
package org.eclipse.jface.databinding.updatables;

import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.UpdatableValue;

/**
 * An updatable value that can be instantiated directly. 
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will remain
 * unchanged during the 3.2 release cycle. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 *
 */
public class SettableValue extends UpdatableValue {

	private final Class type;

	private Object value;

	/**
	 * Creates a new settable value of the given value type, and with the given
	 * initial value.
	 * 
	 * @param type
	 * @param initialValue
	 */
	public SettableValue(Class type, Object initialValue) {
		this.type = type;
		value = initialValue;
	}

	/**
	 * Creates a new settable value of the given value type, and with an
	 * initial value of <code>null</code>.
	 * 
	 * @param type
	 */
	public SettableValue(Class type) {
		this(type, null);
	}
	
	public void setValue(Object value) {
		Object oldValue = this.value;
		this.value = value;
		fireChangeEvent(ChangeEvent.CHANGE, oldValue, value);
	}

	public Object computeValue() {
		return value;
	}

	public Class getValueType() {
		return type;
	}

}

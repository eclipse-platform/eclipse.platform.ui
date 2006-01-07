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
package org.eclipse.jface.databinding;

/**
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
	 * @param type
	 * @param initialValue
	 */
	public SettableValue(Class type, String initialValue) {
		this.type = type;
		value = initialValue;
	}

	/**
	 * @param type
	 */
	public SettableValue(Class type) {
		this(type, null);
	}
	
	public void setValue(Object value) {
		Object oldValue = this.value;
		this.value = value;
		fireChangeEvent(IChangeEvent.CHANGE, oldValue, value);
	}

	public Object getValue() {
		return value;
	}

	public Class getValueType() {
		return type;
	}

}

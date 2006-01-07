/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.databinding;

/**
 * An updatable value wraps a single value that can be updated programmatically,
 * and whose changes can be tracked by registering a change listener.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will remain
 * unchanged during the 3.2 release cycle. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public interface IUpdatableValue extends IUpdatable {

	/**
	 * Sets the value of this updatable value to the given object, which must be
	 * an instance of the value type returned by getValueType(). If the value
	 * type is an object type, then the given value may be </code>null</code>.
	 * Change listeners will be notified of this change with the exception of
	 * the given listener. If the given listener is null, all listeners will be
	 * notified.
	 * 
	 * TODO we probably need to be able to veto a change. Maybe returning a
	 * status?
	 * 
	 * @param value
	 */
	public void setValue(Object value);

	/**
	 * Returns the current value, which must be an instance of the value type
	 * returned by getValueType(). If the value type is an object type, then the
	 * returned value may be </code>null</code>.
	 * 
	 * @return the current value
	 */
	public Object getValue();

	/**
	 * Returns the type of the values that this updatable value can hold. This
	 * method's return value does not change over time.
	 * 
	 * @return the value type
	 */
	public Class getValueType();
}
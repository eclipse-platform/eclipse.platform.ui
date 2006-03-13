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

package org.eclipse.jface.internal.databinding.provisional.observable.value;

import org.eclipse.jface.internal.databinding.provisional.observable.IObservable;

/**
 * @since 1.0
 * 
 */
public interface IObservableValue extends IObservable {

	/**
	 * The value type of this observable value, or <code>null</code> if this
	 * observable value is untyped.
	 * 
	 * @return the value type, or <code>null</null>
	 */
	public Object getValueType();

	/**
	 * @return the current value
	 * @TrackedGetter
	 */
	public Object getValue();

	/**
	 * @param value
	 *            the value to set
	 * @throws UnsupportedOperationException
	 *             if this observable value cannot be set.
	 */
	public void setValue(Object value);

	/**
	 * 
	 * @param listener
	 */
	public void addValueChangeListener(IValueChangeListener listener);

	/**
	 * @param listener
	 */
	public void removeValueChangeListener(IValueChangeListener listener);
}

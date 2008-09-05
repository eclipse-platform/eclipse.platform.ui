/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 237718
 *******************************************************************************/

package org.eclipse.core.databinding.observable.value;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;

/**
 * A value whose changes can be tracked by value change listeners.
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 *              Clients should instead subclass one of the classes that
 *              implement this interface. Note that direct implementers of this
 *              interface outside of the framework will be broken in future
 *              releases when methods are added to this interface.
 * 
 * @see AbstractObservableValue
 * 
 * @since 1.0
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
	 * Returns the value.  Must be invoked in the {@link Realm} of the observable.
	 * 
	 * @return the current value
	 * @TrackedGetter
	 */
	public Object getValue();

	/**
	 * Sets the value.  Must be invoked in the {@link Realm} of the observable.
	 * 
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

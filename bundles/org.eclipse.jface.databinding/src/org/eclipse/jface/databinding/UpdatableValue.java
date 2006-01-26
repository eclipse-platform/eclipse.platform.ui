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
 * Abstract base class for updatable values.
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
public abstract class UpdatableValue extends WritableUpdatable implements IUpdatableValue {

	/**
	 * Computes the current value. Subclasses should overload this to provide the current
	 * value.
	 * 
	 * @return the current value
	 */
	protected abstract Object computeValue();

	/**
	 * Returns the current value, which must be an instance of the value type
	 * returned by getValueType(). If the value type is an object type, then the
	 * returned value may be </code>null</code>. Fires a ChangeEvent.CHANGE event
	 * whenever the result of this method changes. 
	 * 
	 * @TrackedGetter This method will notify UpdateTracker that the reciever has been read from
	 * 
	 * @return the current value
	 */
	public final Object getValue() {
		UpdatableTracker.getterCalled(this);
		return computeValue();
	}
}

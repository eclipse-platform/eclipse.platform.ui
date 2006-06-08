/*******************************************************************************
 * Copyright (c) 2006 The Pampered Chef and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     The Pampered Chef - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.provisional.observable;

/**
 * Represents the data required to insert a new object into a lazy list.
 * 
 * @since 3.3
 */
public class LazyInsertEvent {
	/**
	 * The position where the target list is requesting that the model insert
	 * the new object in its list.  This is a *hint*.  The model is free to
	 * insert the event wherever it wishes according to its internal semantics.
	 * (It must just return the actual place where the insert occurred back to
	 * the target object.)
	 */
	public final int positionHint;
	
	/**
	 * A field that implementations may use to pass application-specific data
	 * from the target list to the insert event handlers.  This field may be null.
	 */
	public final Object initializationData;

	/**
	 * Construct a LazyInsertEvent.
	 * 
	 * @param positionHint The position where the target is requesting the insert to occur.
	 * @param initializationData Application-specific initialization data.  This may be null.
	 */
	public LazyInsertEvent(final int positionHint, final Object initializationData) {
		this.positionHint = positionHint;
		this.initializationData = initializationData;
	}
}

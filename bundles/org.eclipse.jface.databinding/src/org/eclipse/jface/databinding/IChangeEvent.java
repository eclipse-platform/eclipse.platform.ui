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
 * @since 3.2
 *
 */
public interface IChangeEvent {
	
	/**
	 * Position constant denoting a change affecting more than one element, or a
	 * change with an unknown position.
	 */
	public static final int POSITION_UNKNOWN = -1;

	/**
	 * Change type constant denoting a general change. If the source is a
	 * list, getPosition() returns the index of the changed element, or
	 * <code>POSITION_UNKNOWN</code> if more than one element was changed,
	 * added or removed, or if the position of the changed, added or removed
	 * element is not known.
	 */
	public static final int CHANGE = 1;

	/**
	 * Change type constant denoting an addition of an element to a list. The
	 * added element will be returned by getNewValue(), and the position of the
	 * added element will be returned by getPosition().
	 */
	public static final int ADD = 2;

	/**
	 * Change type constant denoting a removal of an element from a list. The
	 * removed element will be returned by getOldValue(), and the position of
	 * the removed element before the removal will be returned by getPosition().
	 */
	public static final int REMOVE = 3;

	/**
	 * Change type constant used to inform listeners about a pending change that
	 * can still be vetoed. The updatable's value has not been changed yet,
	 * <code>getNewValue()</code> returns the new value that will be the
	 * updatable's new value if the change is not vetoed by calling
	 * <code>setVeto(true)</code>.
	 * TODO turn this into flags, so that we can
	 * express pre-add etc.
	 */
	public static final int VERIFY = 4;
	
	/**
	 * Change type constant denoting a complete replacement of a list. The
	 * added list will be returned by getNewValue(), and the position of the
	 * added element will be returned by getPosition().
	 */	
	public static final int REPLACE = 5;
		
	/**
	 * Change type constant that is used to inform a listener that a virtual data is being
	 * requested.
	 */
	public static final int VIRTUAL = 6;
	
	/**
	 * Returns the change type (CHANGE, ADD, or REMOVE).
	 * 
	 * @return the change type
	 */
	public int getChangeType() ;

	/**
	 * Returns the value after the change, if available, or <code>null</code>
	 * if the change is a removal from a list.
	 * 
	 * @return the new value, or null
	 */
	public Object getNewValue();

	/**
	 * Returns the value before the change, if available, or <code>null</code>
	 * if the change is an addition to a list.
	 * 
	 * @return the old value, or null
	 */
	public Object getOldValue();

	/**
	 * If the updatable is a list, return the position of the changed element,
	 * or POSITION_ALL if more than one element was changed, added or removed.
	 * The return value is unspecified if the updatable is not a list.
	 * 
	 * @return the position of the changed element
	 */
	public int getPosition();

}
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
package org.eclipse.jface.binding;

/**
 * An event describing a change to an updatable object.
 * <p>
 * <code>IChangeEvent</code> objects are delivered to
 * <code>IChangeEventListener</code> objects registered with an
 * <code>IUpdatable</code> object when a change occurs in the updatable.
 * </p>
 * <p>
 * This interface is not intended to be implemented or extended by clients. To
 * create IChangeEvent objects, use a constructor of class
 * <code>ChangeEvent</code>.
 * </p>
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
public interface IChangeEvent {

	/**
	 * Change type constant denoting a general change. If the updatable is a
	 * list, getPosition() returns the index of the changed element, or -1 if
	 * more than one element was changed or position of the changed element is
	 * not known.
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
	 */
	public static final int VERIFY = 4;

	/**
	 * Returns the updatable that was changed.
	 * 
	 * @return the updatable
	 */
	public IUpdatable getUpdatable();

	/**
	 * Returns the change type (CHANGE, ADD, or REMOVE).
	 * 
	 * @return the change type
	 */
	public int getChangeType();

	/**
	 * Returns the value before the change, if available, or <code>null</code>
	 * if the change is an addition to a list.
	 * 
	 * @return the old value, or null
	 */
	public Object getOldValue();

	/**
	 * Returns the value after the change, if available, or <code>null</code>
	 * if the change is a removal from a list.
	 * 
	 * @return the new value, or null
	 */
	public Object getNewValue();

	/**
	 * If the updatable is a list, return the position of the changed element.
	 * The return value is unspecified if the updatable is not a list.
	 * 
	 * @return the position of the changed element
	 */
	public int getPosition();

	/**
	 * If the change event is of type VERIFY, listeners can call this method to
	 * veto the change.
	 * 
	 * @param veto
	 */
	public void setVeto(boolean veto);

	/**
	 * If the change event is of type VERIFY, this method returns true if this
	 * event has been vetoed by a listener.
	 * 
	 * @return <code>true</code> if the change has been vetoed.
	 */
	public boolean getVeto();
}

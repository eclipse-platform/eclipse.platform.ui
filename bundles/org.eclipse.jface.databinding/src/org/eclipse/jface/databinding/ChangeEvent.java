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
 * An event describing a change to an updatable object.
 * <p>
 * <code>ChangeEvent</code> objects are delivered to
 * <code>IChangeEventListener</code> objects registered with an
 * <code>IUpdatable</code> object when a change occurs in the updatable.
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
public class ChangeEvent {

	/**
	 * Position constant denoting a change affecting more than one element, or a
	 * change with an unknown position.
	 */
	public static final int POSITION_UNKNOWN = -1;

	/**
	 * Change type constant denoting a general change. If the updatable is a
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
	 * Change type constant that is used to inform a listener that a virtual data is being
	 * reauested.
	 */
	public static final int VIRTUAL = 5;

	private final IUpdatable updatable;

	private final int changeType;

	private final Object oldValue;

	private final Object newValue;
	
	private final Object parent;

	private final int position;

	private boolean vetoed = false;

	/**
	 * @param updatable
	 * @param changeType
	 * @param oldValue
	 * @param newValue
	 */
	public ChangeEvent(IUpdatable updatable, int changeType, Object oldValue,
			Object newValue) {
		this(updatable, changeType, oldValue, newValue, 0);
	}

	/**
	 * @param updatable
	 * @param changeType
	 * @param oldValue
	 * @param newValue
	 * @param position
	 */
	public ChangeEvent(IUpdatable updatable, int changeType, Object oldValue,
			Object newValue, int position) {
		this(updatable, changeType, oldValue, newValue, null, position);
	}
	
	/**
	 * @param updatable
	 * @param changeType
	 * @param oldValue
	 * @param newValue
	 * @param parent 
	 * @param position
	 */
	public ChangeEvent(IUpdatable updatable, int changeType, Object oldValue,
			Object newValue, Object parent, int position) {
		this.updatable = updatable;
		this.oldValue = oldValue;
		this.changeType = changeType;
		this.newValue = newValue;
		this.parent = parent;
		this.position = position;		
	}

	/**
	 * Returns the change type (CHANGE, ADD, or REMOVE).
	 * 
	 * @return the change type
	 */
	public int getChangeType() {
		return changeType;
	}

	/**
	 * Returns the value after the change, if available, or <code>null</code>
	 * if the change is a removal from a list.
	 * 
	 * @return the new value, or null
	 */
	public Object getNewValue() {
		return newValue;
	}

	/**
	 * Returns the value before the change, if available, or <code>null</code>
	 * if the change is an addition to a list.
	 * 
	 * @return the old value, or null
	 */
	public Object getOldValue() {
		return oldValue;
	}

	/**
	 * If the updatable is a list, return the position of the changed element,
	 * or POSITION_ALL if more than one element was changed, added or removed.
	 * The return value is unspecified if the updatable is not a list.
	 * 
	 * @return the position of the changed element
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * Returns the updatable that was changed.
	 * 
	 * @return the updatable
	 */
	public IUpdatable getUpdatable() {
		return updatable;
	}

	/**
	 * If the change event is of type VERIFY, this method returns true if this
	 * event has been vetoed by a listener.
	 * 
	 * @return <code>true</code> if the change has been vetoed.
	 */
	public boolean getVeto() {
		return vetoed;
	}

	/**
	 * If the change event is of type VERIFY, listeners can call this method to
	 * veto the change.
	 * 
	 * @param veto
	 */
	public void setVeto(boolean veto) {
		vetoed = veto;
	}

	public Object getParent() {
		return parent;
	}

}

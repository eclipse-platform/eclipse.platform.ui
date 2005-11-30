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

import java.util.EventObject;

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
public class ChangeEvent extends EventObject implements IChangeEvent {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
		this(updatable, changeType, oldValue, newValue, POSITION_UNKNOWN);
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
		super(updatable);
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
		return (IUpdatable)getSource();
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

	/**
	 * parent is typically used with tree structures.
	 * @return parent
	 */
	public Object getParent() {
		return parent;
	}

}

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
 * @since 3.2
 *
 */
public class ChangeEvent implements IChangeEvent {

	private final IUpdatable updatable;

	private final int changeType;

	private final Object oldValue;

	private final Object newValue;

	private int position;

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
		this.updatable = updatable;
		this.oldValue = oldValue;
		this.changeType = changeType;
		this.newValue = newValue;
		this.position = position;
	}

	public IUpdatable getUpdatable() {
		return updatable;
	}

	public int getChangeType() {
		return changeType;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public Object getNewValue() {
		return newValue;
	}

	public int getPosition() {
		return position;
	}

	public void setVeto(boolean veto) {
		vetoed = veto;
	}

	public boolean getVeto() {
		return vetoed;
	}

}

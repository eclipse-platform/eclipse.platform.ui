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

package org.eclipse.ui;

import java.util.EventObject;


/**
 * Event object describing a change to a set of Saveable objects.
 * 
 * @since 3.2
 */
public class SaveablesLifecycleEvent extends EventObject {

	/**
	 * Serial version UID for this class.
	 * <p>
	 * Note: This class is not intended to be serialized.
	 * </p>
	 */
	private static final long serialVersionUID = -3530773637989046452L;

	/**
	 * Event type constant specifying that the given saveables have been opened.
	 */
	public static final int POST_OPEN = 1;

	/**
	 * Event type constant specifying that the given saveables are about to be
	 * closed. Listeners may veto the closing if isForce() is false.
	 */
	public static final int PRE_CLOSE = 2;

	/**
	 * Event type constant specifying that the given saveables have been closed.
	 */
	public static final int POST_CLOSE = 3;

	/**
	 * Event type constant specifying that the dirty state of the given saveables
	 * has changed.
	 */
	public static final int DIRTY_CHANGED = 4;

	private int eventType;

	private Saveable[] saveables;

	private boolean force;

	private boolean veto = false;

	/**
	 * Creates a new SaveablesLifecycleEvent.
	 * 
	 * @param source
	 *            The source of the event. If an ISaveablesSource notifies
	 *            about changes to the saveables returned by
	 *            {@link ISaveablesSource#getSaveables()}, the source must be
	 *            the ISaveablesSource object.
	 * @param eventType
	 *            the event type, currently one of POST_OPEN, PRE_CLOSE,
	 *            POST_CLOSE, DIRTY_CHANGED
	 * @param saveables
	 *            The affected saveables
	 * @param force
	 *            true if the event type is PRE_CLOSE and this is a closed force
	 *            that cannot be canceled.
	 */
	public SaveablesLifecycleEvent(Object source, int eventType,
			Saveable[] saveables, boolean force) {
		super(source);
		this.eventType = eventType;
		this.saveables = saveables;
		this.force = force;
	}

	/**
	 * Returns the eventType, currently one of POST_OPEN, PRE_CLOSE, POST_CLOSE,
	 * DIRTY_CHANGED. Listeners should silently ignore unknown event types since
	 * new event types might be added in the future.
	 * 
	 * @return the eventType
	 */
	public int getEventType() {
		return eventType;
	}

	/**
	 * Returns the affected saveables.
	 * 
	 * @return the saveables
	 */
	public Saveable[] getSaveables() {
		return saveables;
	}

	/**
	 * Returns the veto. This value is ignored for POST_OPEN,POST_CLOSE, and
	 * DIRTY_CHANGED.
	 * 
	 * @return Returns the veto.
	 */
	public boolean isVeto() {
		return veto;
	}

	/**
	 * @param veto
	 *            The veto to set.
	 */
	public void setVeto(boolean veto) {
		this.veto = veto;
	}

	/**
	 * Sets the force flag. This value is ignored for POST_OPEN, POST_CLOSE, and
	 * DIRTY_CHANGED.
	 * 
	 * @return Returns the force.
	 */
	public boolean isForce() {
		return force;
	}

}

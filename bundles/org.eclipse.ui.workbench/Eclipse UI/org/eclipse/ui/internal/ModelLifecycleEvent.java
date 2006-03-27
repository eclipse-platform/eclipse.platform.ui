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

package org.eclipse.ui.internal;

import java.util.EventObject;

import org.eclipse.ui.ISaveableModel;

/**
 * @since 3.2
 * 
 */
public class ModelLifecycleEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3530773637989046452L;

	/**
	 * 
	 */
	public static final int POST_OPEN = 1;

	/**
	 * 
	 */
	public static final int PRE_CLOSE = 2;

	/**
	 * 
	 */
	public static final int POST_CLOSE = 3;
	
	/**
	 * 
	 */
	public static final int DIRTY_CHANGED = 4;

	private int eventType;

	private ISaveableModel[] models;

	private boolean force;

	private boolean veto = false;

	/**
	 * @param source
	 * @param eventType
	 * @param models
	 * @param force
	 */
	public ModelLifecycleEvent(Object source, int eventType,
			ISaveableModel[] models, boolean force) {
		super(source);
		this.eventType = eventType;
		this.models = models;
		this.force = force;
	}

	/**
	 * @return Returns the eventType.
	 */
	public int getEventType() {
		return eventType;
	}

	/**
	 * @return Returns the models.
	 */
	public ISaveableModel[] getModels() {
		return models;
	}

	/**
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
	 * @return Returns the force.
	 */
	public boolean isForce() {
		return force;
	}

}

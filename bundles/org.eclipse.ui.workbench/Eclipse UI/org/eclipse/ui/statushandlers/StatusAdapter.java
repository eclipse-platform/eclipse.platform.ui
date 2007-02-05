/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.statushandlers;

import java.util.HashMap;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;

/**
 * <p>
 * Contains an instance of IStatus subclass and its handling hint. Used during
 * status handling process. During the process both status and hint can be
 * changed, so a subsequent handler can receive different status or handling
 * hint.
 * </p>
 * 
 * <p>
 * Hint values are defined in {@link StatusManager}.
 * </p>
 * 
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UI team.
 * 
 * @since 3.3
 */
public class StatusAdapter implements IAdaptable {

	private IStatus status;

	private HashMap adapters;

	private int handlingHint;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param status
	 *            the status set in the adapter
	 */
	public StatusAdapter(IStatus status) {
		this.status = status;
		adapters = new HashMap();
	}

	/**
	 * Adds new adapter.
	 * 
	 * @param adapter
	 *            the adapter class
	 * @param object
	 *            the adapter instance
	 */
	public void addAdapter(Class adapter, Object object) {
		adapters.put(adapter, object);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		return adapters.get(adapter);
	}

	/**
	 * @return Returns the status.
	 */
	public IStatus getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            The status to set.
	 */
	public void setStatus(IStatus status) {
		this.status = status;
	}

	/**
	 * @return Returns one of the values defined in {@link StatusManager}, for
	 *         instance StatusManager.SHOW
	 */
	public int getHandlingHint() {
		return handlingHint;
	}

	/**
	 * @param handlingHint
	 *            The handlingHint to set.
	 */
	public void setHandlingHint(int handlingHint) {
		this.handlingHint = handlingHint;
	}
}

/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoRequest;

/**
 * @since 3.3
 */
interface IMementoManager {

	/**
	 * Adds the request to this manager.
	 * 
	 * @param memento request
	 */
	public void addRequest(IElementMementoRequest request);
	
	/**
	 * Notification the request is complete.
	 * 
	 * @param request
	 */
	public void requestComplete(IElementMementoRequest request);
	
	/**
	 * Process the queued requests. Accepts no more new requests.
	 */
	public void processReqeusts();

    /**
     * Cancels the requests in progress.
     */
    public void cancel();
}

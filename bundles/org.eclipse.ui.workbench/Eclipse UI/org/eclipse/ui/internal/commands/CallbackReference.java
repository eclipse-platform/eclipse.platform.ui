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

package org.eclipse.ui.internal.commands;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.commands.ICallbackReference;

/**
 * Our callback reference that is used during callback
 * registration/unregistration.
 * 
 * @since 3.3
 */
public class CallbackReference implements ICallbackReference {

	private String commandId;
	private IAdaptable callback;
	private HashMap parameters;

	/**
	 * Construct the reference.
	 * 
	 * @param id
	 *            command id. Must not be <code>null</code>.
	 * @param adapt
	 *            the callback. Must not be <code>null</code>.
	 * @param parms.
	 *            parameters used for filtering. Must not be <code>null</code>.
	 */
	public CallbackReference(String id, IAdaptable adapt, Map parms) {
		commandId = id;
		callback = adapt;
		if (parms == null) {
			parameters = new HashMap();
		} else {
			parameters = new HashMap(parms);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.ICallbackReference#getCallback()
	 */
	public IAdaptable getCallback() {
		return callback;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.ICallbackReference#getCommandId()
	 */
	public String getCommandId() {
		return commandId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.commands.ICallbackReference#getParameters()
	 */
	public Map getParameters() {
		return parameters;
	}
}

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

package org.eclipse.ui.statushandling;

import java.util.Map;

import org.eclipse.core.runtime.IStatus;

/**
 * <p>
 * All status handlers are <code>AbstractStatusHandler</code> subclasses. Each
 * handler has to have <code>handle(StatusHandlingState status)</code>
 * implemented. This method handles the status due to handling hint. The hint
 * indicates how status handler should handle a status but this is only a
 * suggestion.
 * </p>
 * 
 * <p>
 * Hint values are defined in {@link StatusManager}.
 * </p>
 * 
 * <p>
 * Handlers shoudn't be used directly but through the <code>StatusManager</code>
 * singleton which keeps the status handling policy and chooses handlers due to
 * it.
 * </p>
 * 
 * <p>
 * A status handler has a set of parameters. The handler can use these
 * parameters during handling. These parameters are used by status manager too.
 * In default policy the status manager checks "prefix" parameter. See
 * {@link StatusManager}.
 * </p>
 * 
 * <p>
 * Each status handler can be set and configured using
 * <code>org.eclipse.ui.statusHandlers</code> extension-point.
 * </p>
 * 
 * <p>
 * Handler parameters can be defined in the extension. The id parameter is set
 * using id attribute of statusHandler element from statusHandlers extension.
 * All handlers are instantiated during the status handling facility
 * initialization. Handlers shouldn't be instantiated in different way,
 * </p>
 * 
 * 
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UI team.
 * 
 * @since 3.3
 */
public abstract class AbstractStatusHandler {

	private Map params;

	private String id;

	/**
	 * Handles {@link StatusHandlingState} objects. This method can modify
	 * status and hint.
	 * 
	 * @param handlingState
	 *            the handling state
	 */
	abstract public void handle(StatusHandlingState handlingState);

	/**
	 * @return Returns the params.
	 */
	public Map getParams() {
		return params;
	}

	/**
	 * @param key
	 * @return Returns a value of the param.
	 */
	public Object getParam(Object key) {
		if (params != null) {
			return params.get(key);
		}

		return null;
	}

	/**
	 * @param params
	 *            The params to set.
	 */
	public void setParams(Map params) {
		this.params = params;
	}

	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            The id to set.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * This method is used for collecting statuses which were already handled
	 * and logged by the handler. Because the status handling facility handles
	 * statuses from plug-in's log, it prevents handling logged statuses again.
	 * 
	 * This method should be called before each status logging in handlers.
	 * 
	 * @param status
	 *            already handled and logged status
	 */
	protected void addLoggedStatus(IStatus status) {
		StatusManager.getManager().addLoggedStatus(status);
	}
}

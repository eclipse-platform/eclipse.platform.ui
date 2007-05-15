/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.statushandlers;

import java.util.Map;

import org.eclipse.ui.application.WorkbenchAdvisor;

/**
 * <p>
 * Status handlers are part of the status handling facility. The facility is
 * responsible for handling errors and other important issues in Eclipse based
 * applications. The handlers are responsible for presenting this errors by
 * logging or showing error dialogs.
 * </p>
 * 
 * <p>
 * All status handlers extends
 * <code>org.eclipse.ui.statushandlers.AbstractStatusHandler</code>. Each
 * handler implements <code>handle(StatusAdapter status, int style)</code>.
 * This method handles statuses due to handling style. The style indicates how
 * status handler should handle a status.
 * </p>
 * 
 * <p>
 * For acceptable styles check {@link StatusManager}.
 * </p>
 * 
 * <p>
 * Handlers shoudn't be used directly but through the {@link StatusManager}. It
 * chooses which handler should be used for handling. There are two ways for
 * adding handlers to the handling flow. First using extension point
 * <code>org.eclipse.ui.statusHandlers</code>, second by the workbench
 * advisor and its method {@link WorkbenchAdvisor#getWorkbenchErrorHandler()}.
 * If a handler is associated with a product, it is used instead of this defined
 * in advisor.
 * </p>
 * 
 * <p>
 * A status handler has the id and a set of parameters. The handler can use them
 * during handling. If the handler is added as an extension, both are set during
 * initialization of the handler using elements and attributes of
 * <code>statusHandler</code> element.
 * </p>
 * 
 * @since 3.3
 */
public abstract class AbstractStatusHandler {

	private Map params;

	private String id;

	/**
	 * Handles {@link StatusAdapter} objects based on the set style. 
	 * 
	 * @param statusAdapter
	 *            the status adapter. May not be <code>null</code>.
	 * @param style
	 *            style constant. Acceptable values are defined in
	 *            {@link StatusManager} and can be combined with logical OR.
	 * 
	 * @see StatusManager#BLOCK
	 * @see StatusManager#NONE
	 * @see StatusManager#SHOW
	 * @see StatusManager#LOG
	 */
	public abstract void handle(StatusAdapter statusAdapter, int style);

	/**
	 * Returns all parameters of the handler.
	 * 
	 * @return the parameters
	 */
	public Map getParams() {
		return params;
	}

	/**
	 * Returns the value of the handler's parameter identified by the given key,
	 * or <code>null</code> if this handler has no such parameter.
	 * 
	 * @param key
	 *            the name of the property
	 * @return the value of the parameter, or <code>null</code> if this
	 *         handler has no such parameter
	 */
	public Object getParam(Object key) {
		if (params != null) {
			return params.get(key);
		}

		return null;
	}

	/**
	 * Sets the parameters for the handler. If the handler is added via the
	 * <code> org.eclipse.ui.statushandlers extension</code>, the parameters are set 
	 * during initialization of the handler using <code>parameter</code> 
	 * elements from <code>statusHandler</code>
	 * element.
	 * 
	 * @param params
	 *            the parameters to set
	 */
	public void setParams(Map params) {
		this.params = params;
	}

	/**
	 * Returns the id of the handler.
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the id for the handler. If the handler is added as an extension, the
	 * id is set during initialization of the handler using <code>id</code>
	 * attribute of <code>statusHandler</code> element.
	 * 
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
}

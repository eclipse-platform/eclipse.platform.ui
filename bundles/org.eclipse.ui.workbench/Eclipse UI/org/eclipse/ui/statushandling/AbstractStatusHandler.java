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

/**
 * <p>
 * A base class for status handlers used in status handling facility. Each
 * handler has to have <code>handle(StatusHandlingState)</code> implemented.
 * {@link StatusHandlingState} objects contains status and mode which is a hint
 * for handler how to handle the problem.
 * 
 * <p>
 * Each status handler can be set and configured in
 * <code>org.eclipse.ui.statusHandler</code> extension-point.
 * </p>
 * 
 * @since 3.3
 */
public abstract class AbstractStatusHandler {

	private Map params;

	private String contributorName;

	private String id;

	/**
	 * Handles {@link StatusHandlingState} objects. It can change status and
	 * mode of them.
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
	 * @return Returns the contributorName.
	 */
	public String getContributorName() {
		return contributorName;
	}

	/**
	 * @param contributorName
	 *            The contributorName to set.
	 */
	public void setContributorName(String contributorName) {
		this.contributorName = contributorName;
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
}

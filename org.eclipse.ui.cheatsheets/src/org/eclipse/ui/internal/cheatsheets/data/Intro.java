/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets.data;

public class Intro {
	private String contextId;
	private String href;
	private String description;

	/**
	 * Constructor for Intro.
	 */
	public Intro() {
		super();
	}
	
	public Intro(String description, String href, String contextId) {
		super();
		this.description = description;
		this.href = href;
		this.contextId = contextId;
	}
	
	/**
	 * Returns the contenxtId.
	 * @return String
	 */
	public String getContextId() {
		return contextId;
	}

	/**
	 * Returns the helpLink.
	 * @return String
	 */
	public String getHref() {
		return href;
	}

	/**
	 * Returns the description.
	 * @return String
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the contextId.
	 * @param contextId The contextId to set
	 */
	public void setContextId(String contextId) {
		this.contextId = contextId;
	}

	/**
	 * Sets the helpLink.
	 * @param helpLink The helpLink to set
	 */
	public void setHref(String helpLink) {
		this.href = helpLink;
	}

	/**
	 * Sets the description.
	 * @param description The description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
}

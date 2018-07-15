/*******************************************************************************
 * Copyright (c) 2018 SAP SE and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP SE - initial version
 *******************************************************************************/
package org.eclipse.urischeme.internal.registration;

import org.eclipse.urischeme.ISchemeInformation;

/**
 * The pojo for holding information of a scheme w.r.t. the handling eclipse.
 *
 */
public class SchemeInformation implements ISchemeInformation {

	private String scheme;
	private String schemeDescription;
	private boolean handled;
	private String handlerInstanceLocation;

	@SuppressWarnings("javadoc")
	public SchemeInformation(String schemeName, String schemeDescription, String handlerLocation) {
		super();
		this.scheme = schemeName;
		this.schemeDescription = schemeDescription;
		setHandlerLocation(handlerLocation);
	}

	@Override
	public String getScheme() {
		return scheme;
	}

	@Override
	public boolean isHandled() {
		return handled;
	}

	@Override
	public String getHandlerInstanceLocation() {
		return handlerInstanceLocation;
	}

	@SuppressWarnings("javadoc")
	public void setHandled(boolean handled) {
		this.handled = handled;
	}

	@Override
	public void setHandlerLocation(String handlerInstanceLocation) {
		this.handlerInstanceLocation = handlerInstanceLocation;
	}

	@Override
	public String getDescription() {
		return schemeDescription;
	}

}
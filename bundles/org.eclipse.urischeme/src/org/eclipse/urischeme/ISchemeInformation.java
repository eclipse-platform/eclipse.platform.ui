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
package org.eclipse.urischeme;

import org.eclipse.urischeme.internal.registration.SchemeInformation;

/**
 * The basic information of an URI scheme with regards to the handler.
 *
 */
public interface ISchemeInformation {

	/**
	 * @return the name of the scheme
	 */
	String getScheme();

	/**
	 * @return the description of the scheme
	 */
	String getDescription();

	/**
	 * @return true if the scheme is handled by the running Eclipse installation;
	 *         false otherwise
	 */
	boolean isHandled();

	/**
	 * Sets the handled value to true if scheme is handled by current Eclipse
	 * installation and false otherwise
	 * @param value
	 */
	void setHandled(boolean value);

	/**
	 * @return the path of the application
	 */
	String getHandlerInstanceLocation();

	/**
	 * @param location
	 */
	void setHandlerLocation(String location);

	/**
	 * Returns the instance of ISchemeInformation interface.
	 *
	 * @param schemeName
	 * @param schemeDescription
	 * @param handlerLocation
	 * @return instance of ISchemeInformation
	 */
	static ISchemeInformation getInstance(String schemeName, String schemeDescription, String handlerLocation) {
		return new SchemeInformation(schemeName, schemeDescription, handlerLocation);
	}
}
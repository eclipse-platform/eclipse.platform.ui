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

/**
 * The basic information of an URI scheme with regards to the handler.
 *
 */
public interface ISchemeInformation extends IScheme {

	/**
	 * @return true if the scheme is handled by the running Eclipse installation;
	 *         false otherwise
	 */
	boolean isHandled();

	/**
	 * @return the path of the application
	 */
	String getHandlerInstanceLocation();

	/**
	 * @return true if the scheme is handled by another application; false
	 *         otherwise.
	 */
	default boolean schemeIsHandledByOther() {
		boolean schemeIsNotHandled = !isHandled();
		String handlerInstanceLocation = getHandlerInstanceLocation();
		boolean handlerLocationIsSet = handlerInstanceLocation != null && !handlerInstanceLocation.isEmpty();
		return schemeIsNotHandled && handlerLocationIsSet;
	}
}
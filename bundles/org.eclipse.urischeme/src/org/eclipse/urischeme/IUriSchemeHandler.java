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
 * This interface belongs to extension point
 * org.eclipse.core.runtime.uriSchemeHandlers.
 *
 */
public interface IUriSchemeHandler {

	/**
	 * Sent by the platform when an URL needs to be handled.
	 *
	 * @param uri The URI to be handled
	 */
	void handle(String uri);
}
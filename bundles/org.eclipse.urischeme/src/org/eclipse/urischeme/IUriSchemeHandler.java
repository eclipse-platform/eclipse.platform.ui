/*******************************************************************************
 * Copyright (c) 2018 SAP SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP SE - initial version
 *******************************************************************************/
package org.eclipse.urischeme;

/**
 * This interface belongs to extension point
 * org.eclipse.core.runtime.uriSchemeHandlers.
 */
public interface IUriSchemeHandler {

	/**
	 * Sent by the platform when an URL needs to be handled.
	 *
	 * @param uri The URI to be handled
	 */
	void handle(String uri);
}
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
 * The basic information of an URI scheme like name and description.
 *
 */
public interface IScheme {

	/**
	 * @return the name of the scheme
	 */
	String getName();

	/**
	 * @return the description of the scheme
	 */
	String getDescription();
}

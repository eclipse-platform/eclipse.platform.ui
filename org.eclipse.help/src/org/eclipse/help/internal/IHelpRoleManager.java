/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.internal;

/**
 * @since 3.0
 */
public interface IHelpRoleManager {

	/**
	 * Checks if href is valid in the actives roles
	 * 
	 * @param href
	 * @return
	 */
	public boolean isEnabled(String href);

	/**
	 * Enables all the roles whose activity binding pattersn match the href
	 * 
	 * @param href
	 */
	public void enabledActivities(String href);
}
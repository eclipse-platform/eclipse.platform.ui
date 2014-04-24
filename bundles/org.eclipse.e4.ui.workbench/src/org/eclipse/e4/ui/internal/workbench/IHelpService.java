/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

/**
 * Provides services related to the help system.
 */
public interface IHelpService {

	/**
	 * Calls the help support system to display the given help context ID.
	 *
	 * @param contextId
	 *            the ID of the context to display
	 */
	public void displayHelp(String contextId);
}

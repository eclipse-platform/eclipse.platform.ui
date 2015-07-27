/*******************************************************************************
 * Copyright (c) 2014, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 445723, 445600
 ******************************************************************************/

package org.eclipse.e4.ui.services.help;

/**
 * Provides services related to the help system.
 */
public interface EHelpService {

	/**
	 * Calls the help support system to display the given help context ID.
	 *
	 * @param contextId
	 *            the ID of the context to display
	 */
	void displayHelp(String contextId);

	/**
	 * Sets the given id for the help system on the given object.
	 *
	 * @param element
	 *            the element on which to register the help id
	 * @param helpContextId
	 *            the id to use when help system is invoked
	 */
	void setHelp(Object element, String helpContextId);

}

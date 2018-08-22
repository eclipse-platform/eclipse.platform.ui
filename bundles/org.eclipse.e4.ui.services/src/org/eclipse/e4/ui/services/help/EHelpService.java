/*******************************************************************************
 * Copyright (c) 2014, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 445723, 445600, 505896
 ******************************************************************************/

package org.eclipse.e4.ui.services.help;

/**
 * The help service provides clients with the functionalities of dealing with
 * the help system.
 * <p>
 * You can use it to define the help context id for an object and for displaying
 * the help based on this help context id.
 * </p>
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 1.3
 */
public interface EHelpService {
	public static final String HELP_CONTEXT_ID = "HelpContextId"; //$NON-NLS-1$
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

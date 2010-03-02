/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.IWorkbenchHelpSystem;

/**
 * Displays the help resource specified in the <code>href</code> command
 * parameter or simply displays the help bookshelf if no parameter was passed.
 * 
 * @since 3.2
 */
public final class DisplayHelpHandler extends AbstractHandler {

	/**
	 * The identifier of the command parameter for the URI to oepn.
	 */
	private static final String PARAM_ID_HREF = "href"; //$NON-NLS-1$

	public final Object execute(final ExecutionEvent event) {
		final IWorkbenchHelpSystem helpSystem = PlatformUI.getWorkbench()
				.getHelpSystem();
		final String href = event.getParameter(PARAM_ID_HREF);

		if (href == null) {
			helpSystem.displayHelp();
		} else {
			helpSystem.displayHelpResource(href);
		}

		return null;
	}
}

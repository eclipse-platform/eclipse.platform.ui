/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.externaltools.internal.menu;

import org.eclipse.debug.ui.actions.AbstractLaunchToolbarAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;

/**
 * This action delegate is responsible for producing the
 * Run > External Tools sub menu contents, which includes
 * an items to run last tool, favorite tools, and show the
 * external tools launch configuration dialog.
 */
public class ExternalToolMenuDelegate extends AbstractLaunchToolbarAction {
	
	/**
	 * Creates the action delegate
	 */
	public ExternalToolMenuDelegate() {
		super(IExternalToolConstants.ID_EXTERNAL_TOOLS_LAUNCH_GROUP);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.AbstractLaunchToolbarAction#getOpenDialogAction()
	 */
	protected IAction getOpenDialogAction() {
		IAction action= new OpenExternalToolsConfigurations();
		action.setActionDefinitionId("org.eclipse.ui.externalTools.commands.OpenExternalToolsConfigurations"); //$NON-NLS-1$
		return action;
	}
}

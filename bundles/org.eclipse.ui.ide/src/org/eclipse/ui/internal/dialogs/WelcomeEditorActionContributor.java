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

package org.eclipse.ui.internal.dialogs;

import org.eclipse.ui.*;
import org.eclipse.ui.part.EditorActionBarContributor;


/**
 * Manages the installation and deinstallation of global actions for 
 * the welcome editor.
 */
public class WelcomeEditorActionContributor extends EditorActionBarContributor {
	/**
	 * The <code>WelcomeEditorActionContributor</code> implementation of this 
	 * <code>IEditorActionBarContributor</code> method installs the global 
	 * action handler for the given editor.
	 */
	public void setActiveEditor(IEditorPart part) {	
		IActionBars actionBars= getActionBars();
		if (actionBars != null) {
			actionBars.setGlobalActionHandler(IWorkbenchActionConstants.COPY, ((WelcomeEditor)part).getCopyAction());
		}
	}
}

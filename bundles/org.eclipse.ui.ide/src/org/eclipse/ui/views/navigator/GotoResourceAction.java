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
package org.eclipse.ui.views.navigator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;

/**
 * Implements the go to resource action. Opens a dialog and set
 * the navigator selection with the resource selected by
 * the user.
 * @deprecated as of 3.5, use the Common Navigator Framework classes instead
 */
public class GotoResourceAction extends ResourceNavigatorAction {
    /**
     * Creates a new instance of the class.
     * 
     * @param navigator the navigator
     * @param label the label
     * @since 2.0
     */
    public GotoResourceAction(IResourceNavigator navigator, String label) {
        super(navigator, label);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
                INavigatorHelpContextIds.GOTO_RESOURCE_ACTION);
    }

    /**
     * Collect all resources in the workbench open a dialog asking
     * the user to select a resource and change the selection in
     * the navigator.
     */
    public void run() {
        IContainer container = (IContainer) getViewer().getInput();
        GotoResourceDialog dialog = new GotoResourceDialog(getShell(),
                container, IResource.FILE | IResource.FOLDER
                        | IResource.PROJECT);
        dialog.open();
        Object[] result = dialog.getResult();
        if (result == null || result.length == 0
                || result[0] instanceof IResource == false) {
			return;
		}

        IResource selection = (IResource) result[0];
        getViewer().setSelection(new StructuredSelection(selection), true);
    }
}

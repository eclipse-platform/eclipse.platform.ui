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
package org.eclipse.ui.views.navigator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ResourceListSelectionDialog;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Shows a list of resources to the user with a text entry field
 * for a string pattern used to filter the list of resources.
 *
 */
/*package*/class GotoResourceDialog extends ResourceListSelectionDialog {

    /**
     * Creates a new instance of the class.
     */
    protected GotoResourceDialog(Shell parentShell, IContainer container,
            int typesMask) {
        super(parentShell, container, typesMask);
        setTitle(ResourceNavigatorMessages.getString("Goto.title")); //$NON-NLS-1$
        WorkbenchHelp.setHelp(parentShell,
                INavigatorHelpContextIds.GOTO_RESOURCE_DIALOG);
    }
}
/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.dialogs;

import org.eclipse.core.resources.IContainer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ResourceListSelectionDialog;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

/**
 * Shows a list of resources to the user with a text entry field
 * for a string pattern used to filter the list of resources.
 *
 * @since 2.1
 */
public class OpenResourceDialog extends ResourceListSelectionDialog {

    /**
     * Creates a new instance of the class.
     * 
     * @param parentShell the parent shell
     * @param container the container
     * @param typesMask the types mask
     */
    public OpenResourceDialog(Shell parentShell, IContainer container,
            int typesMask) {
        super(parentShell, container, typesMask);
        setTitle(IDEWorkbenchMessages.OpenResourceDialog_title);
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(parentShell, IIDEHelpContextIds.OPEN_RESOURCE_DIALOG);
        setAllowUserToToggleDerived(true);
        setShowDerived(IDEWorkbenchPlugin.getDefault().getDialogSettings().getBoolean("ResourceListSelectionDialog.showDerived")); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.dialogs.ResourceListSelectionDialog#okPressed()
     */
    protected void okPressed() {
        IDEWorkbenchPlugin.getDefault().getDialogSettings().put("ResourceListSelectionDialog.showDerived", getShowDerived()); //$NON-NLS-1$
        super.okPressed();
    }
}

/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.about;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dialogs.AboutDialog;

/**
 * Creates an About dialog and opens it.
 */
public class AboutAction extends Action implements
        ActionFactory.IWorkbenchAction {

    /**
     * The workbench window; or <code>null</code> if this action has been
     * <code>dispose</code>d.
     */
    private IWorkbenchWindow workbenchWindow;

    /**
     * Creates a new <code>AboutAction</code> with the given label
     */
    public AboutAction(IWorkbenchWindow window) {
        if (window == null)
            throw new IllegalArgumentException();

        this.workbenchWindow = window;

        // use message with no fill-in
        IProduct product = Platform.getProduct();
        String productName = null;
        if (product != null)
            productName = product.getName();
        if (productName == null)
            productName = ""; //$NON-NLS-1$
        setText(WorkbenchMessages.format(
                "AboutAction.text", new Object[] { productName })); //$NON-NLS-1$
        setToolTipText(WorkbenchMessages.format(
                "AboutAction.toolTip", new Object[] { productName })); //$NON-NLS-1$
        setId("about"); //$NON-NLS-1$
        setActionDefinitionId("org.eclipse.ui.help.aboutAction"); //$NON-NLS-1$
        WorkbenchHelp.setHelp(this, IWorkbenchHelpContextIds.ABOUT_ACTION);
    }

    /*
     * (non-Javadoc) Method declared on IAction.
     */
    public void run() {
        // make sure action is not disposed
        if (workbenchWindow != null)
            new AboutDialog(workbenchWindow.getShell()).open();
    }

    /*
     * (non-Javadoc) Method declared on ActionFactory.IWorkbenchAction.
     */
    public void dispose() {
        workbenchWindow = null;
    }
}
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
package org.eclipse.ui.examples.readmetool;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * This class is used to demonstrate view action extensions.
 * An extension should be defined in the readme plugin.xml.
 */
public class ViewActionDelegate implements IViewActionDelegate {
    public IViewPart view;

    /**
     * Creates a new ViewActionDelegate.
     */
    public ViewActionDelegate() {
        super();
    }

    /* (non-Javadoc)
     * Method declared on IViewActionDelegate
     */
    public void init(IViewPart view) {
        this.view = view;
    }

    /* (non-Javadoc)
     * Method declared on IActionDelegate
     */
    public void run(org.eclipse.jface.action.IAction action) {
        MessageDialog.openInformation(view.getSite().getShell(), MessageUtil
                .getString("Readme_Editor"), //$NON-NLS-1$
                MessageUtil.getString("View_Action_executed")); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * Method declared on IActionDelegate
     */
    public void selectionChanged(org.eclipse.jface.action.IAction action,
            org.eclipse.jface.viewers.ISelection selection) {
        // do nothing
    }
}

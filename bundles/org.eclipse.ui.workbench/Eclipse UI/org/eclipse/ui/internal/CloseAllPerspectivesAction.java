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
package org.eclipse.ui.internal;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * The <code>CloseAllPerspectivesAction</code> is used to close all of 
 * the opened perspectives in the workbench window's active page.
 */
public class CloseAllPerspectivesAction extends PerspectiveAction {

    /**
     * Create a new instance of <code>CloseAllPerspectivesAction</code>
     * 
     * @param window the workbench window this action applies to
     */
    public CloseAllPerspectivesAction(IWorkbenchWindow window) {
        super(window);
        setText(WorkbenchMessages.CloseAllPerspectivesAction_text);
        setActionDefinitionId("org.eclipse.ui.window.closeAllPerspectives"); //$NON-NLS-1$
        // @issue missing action id
        setToolTipText(WorkbenchMessages.CloseAllPerspectivesAction_toolTip); 
        window.getWorkbench().getHelpSystem().setHelp(this,
				IWorkbenchHelpContextIds.CLOSE_ALL_PAGES_ACTION);
    }

    /* (non-Javadoc)
     * Method declared on PerspectiveAction.
     */
    protected void run(IWorkbenchPage page, IPerspectiveDescriptor persp) {
        page.closeAllPerspectives(true, true);
    }

}

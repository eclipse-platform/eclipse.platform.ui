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
 * The <code>ClosePerspectiveAction</code> is used to close the
 * active perspective in the workbench window's active page.
 */
public class ClosePerspectiveAction extends PerspectiveAction {

    /**
     * Create a new instance of <code>ClosePerspectiveAction</code>
     * 
     * @param window the workbench window this action applies to
     */
    public ClosePerspectiveAction(IWorkbenchWindow window) {
        super(window);
        setText(WorkbenchMessages.ClosePerspectiveAction_text);
        setActionDefinitionId("org.eclipse.ui.window.closePerspective"); //$NON-NLS-1$
        // @issue missing action id
        setToolTipText(WorkbenchMessages.ClosePerspectiveAction_toolTip);
        window.getWorkbench().getHelpSystem().setHelp(this,
				IWorkbenchHelpContextIds.CLOSE_PAGE_ACTION);
    }

    /* (non-Javadoc)
     * Method declared on PerspectiveAction.
     */
    protected void run(IWorkbenchPage page, IPerspectiveDescriptor perspDesc) {
        Perspective persp = ((WorkbenchPage) page).getActivePerspective();
        if (persp != null) {
            closePerspective((WorkbenchPage) page, persp);
        }
    }

    /**
     * Close the argument perspective in the argument page.  Do nothing if the page or
     * perspective are null.
     * 
     * @param page the page
     * @param persp the perspective
	 * @since 3.1
     */
    public static void closePerspective(WorkbenchPage page, Perspective persp) {
        if (page != null && persp != null) {
			page.closePerspective(persp, true, true);
		}
    }
}

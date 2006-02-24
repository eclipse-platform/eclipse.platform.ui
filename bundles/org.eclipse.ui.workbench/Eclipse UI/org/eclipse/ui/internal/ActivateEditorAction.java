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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Activates the most recently used editor in the current window.
 */
public class ActivateEditorAction extends PageEventAction {

    private int accelerator;

    /**
     * Creates an ActivateEditorAction.
     * 
     * @param window the window
     */
    public ActivateEditorAction(IWorkbenchWindow window) {
        super(WorkbenchMessages.ActivateEditorAction_text, window);
        setToolTipText(WorkbenchMessages.ActivateEditorAction_toolTip); 
        // @issue missing action id
        updateState();
        window.getWorkbench().getHelpSystem().setHelp(this,
				IWorkbenchHelpContextIds.ACTIVATE_EDITOR_ACTION);
        setActionDefinitionId("org.eclipse.ui.window.activateEditor"); //$NON-NLS-1$
    }

    public void pageActivated(IWorkbenchPage page) {
        super.pageActivated(page);
        updateState();
    }

    public void pageClosed(IWorkbenchPage page) {
        super.pageClosed(page);
        updateState();
    }

    /* (non-Javadoc)
     * Method declared on Action.
     */
    public void runWithEvent(Event e) {
        if (getWorkbenchWindow() == null) {
            // action has been disposed
            return;
        }
        accelerator = e.detail;
        IWorkbenchPage page = getActivePage();
        if (page != null) {
            IEditorPart part = page.getActiveEditor(); // may not actually be active
            if (part != null) {
                page.activate(part);
                part.setFocus();
            } else {
                IWorkbenchPartReference ref = page.getActivePartReference();
                if (ref instanceof IViewReference) {
                    if (((WorkbenchPage) page).isFastView((IViewReference) ref)) {
						((WorkbenchPage) page)
                                .toggleFastView((IViewReference) ref);
					}
                }
            }
        }
    }

    /**
     * Updates the enabled state.
     */
    public void updateState() {
        IWorkbenchPage page = getActivePage();
        setEnabled(page != null);
    }

    public int getAccelerator() {
        int accelerator = this.accelerator;
        accelerator = accelerator & ~SWT.CTRL;
        accelerator = accelerator & ~SWT.SHIFT;
        accelerator = accelerator & ~SWT.ALT;
        return accelerator;
    }
}


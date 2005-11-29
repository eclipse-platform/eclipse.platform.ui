/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dialogs.DialogUtil;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;

/**
 * This convenience class provides a "New Editor" system menu item that 
 * opens another editor of the same type as the current editor, on the same input.
 * Presentations can use this to add a "New Editor" item to their system menu.
 *  
 * @since 3.1
 */
public final class SystemMenuNewEditor extends Action implements ISelfUpdatingAction {

    private IStackPresentationSite site;
    private IPresentablePart part;

	/**
	 * Creates a new instance of the action
	 * 
	 * @param site the presentation site
	 */
    public SystemMenuNewEditor(IStackPresentationSite site) {
        this.site = site;
        setText(WorkbenchMessages.PartPane_newEditor);
    }

	/**
	 * Disposes the action.
	 */
    public void dispose() {
        site = null;
    }

    public void run() {
		if (part != null) {
			// the site doesn't give access to the page or the active editor,
			// so we need to reach
			IWorkbenchWindow window = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();
			if (window != null) {
				IWorkbenchPage page = window.getActivePage();
				if (page != null) {
					IEditorPart editor = page == null ? null : page
							.getActiveEditor();
					if (editor != null) {
						String editorId = editor.getSite().getId();
						if (editorId != null) {
							try {
								page.openEditor(editor
										.getEditorInput(), editorId, true,
										IWorkbenchPage.MATCH_NONE);
							} catch (PartInitException e) {
								DialogUtil.openError(page.getWorkbenchWindow()
										.getShell(), WorkbenchMessages.Error, e
										.getMessage(), e);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Sets the target of this action to the given part.
	 * 
	 * @param presentablePart
	 *            the target part for this action, or <code>null</code> if
	 *            there is no appopriate target part
	 */
    public void setTarget(IPresentablePart presentablePart) {
        this.part = presentablePart;
        setEnabled(presentablePart != null);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.ISelfUpdatingAction#update()
     */
    public void update() {
        setTarget(site.getSelectedPart());
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.ISelfUpdatingAction#shouldBeVisible()
     */
    public boolean shouldBeVisible() {
        return true;
    }
}

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

package org.eclipse.ui.presentations;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.presentations.EditorPresentation;
import org.eclipse.ui.internal.presentations.PartTabFolderPresentation;

/**
 * The default presentation factory for the Workbench.
 * 
 * @since 3.0
 */
public class WorkbenchPresentationFactory extends AbstractPresentationFactory {

    public StackPresentation createPartPresentation(Composite parent,
            IStackPresentationSite site, int role, int flags,
            String perspectiveId, String folderId) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.presentations.AbstractPresentationFactory
     */
    public StackPresentation createPresentation(Composite parent,
            IStackPresentationSite site, int role, int flags,
            String perspectiveId, String folderId) {
        switch (role) {
        case ROLE_EDITOR_WORKBOOK:
            return new EditorPresentation(parent, site, flags);
        case ROLE_DOCKED_VIEW:
            return new PartTabFolderPresentation(parent, site, flags);
        //case ROLE_FAST_VIEW:
            // TODO 
         //   return new PartTabFolderPresentation(parent, site, flags);
        //case ROLE_DETACHED_VIEW:
            // TODO
            //return new PartTabFolderPresentation(parent, site, flags);
        default:
            throw new Error("not implemented"); //$NON-NLS-1$
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.presentations.AbstractPresentationFactory
     */
    public Control createStatusLineControl(IStatusLineManager statusLine,
            Composite parent) {
        return ((StatusLineManager) statusLine).createControl(parent, SWT.NONE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.presentations.AbstractPresentationFactory
     */
    public IStatusLineManager createStatusLineManager() {
        return new StatusLineManager();
    }
}
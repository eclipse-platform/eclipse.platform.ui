/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dnd;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * @since 3.0
 */
public class DragDropPerspectiveFactory implements IPerspectiveFactory {
    /* (non-Javadoc)
     * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
     */
    public void createInitialLayout(IPageLayout layout) {
        String folderId = "org.eclipse.ui.test.dnd.mystack";

        IFolderLayout folder = layout.createFolder(folderId,
                IPageLayout.BOTTOM, 0.5f, IPageLayout.ID_EDITOR_AREA);
        folder.addView(IPageLayout.ID_OUTLINE);
        folder.addView(IPageLayout.ID_PROBLEM_VIEW);
        folder.addView(IPageLayout.ID_PROP_SHEET);

        layout.addView(IPageLayout.ID_RES_NAV, IPageLayout.LEFT, 0.5f,
                IPageLayout.ID_EDITOR_AREA);
    }
}
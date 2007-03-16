/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.zoom;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * @since 3.0
 */
public class ZoomPerspectiveFactory implements IPerspectiveFactory {
    public static final String PERSP_ID = "org.eclipse.ui.tests.zoom.ZoomPerspectiveFactory";

    public static final String STACK1_VIEW1 = IPageLayout.ID_OUTLINE;
    public static final String STACK1_VIEW2 = IPageLayout.ID_PROBLEM_VIEW;
    public static final String STACK1_PLACEHOLDER1 = IPageLayout.ID_PROP_SHEET;
    public static final String STACK1_VIEW3 = IPageLayout.ID_TASK_LIST;
    public static final String UNSTACKED_VIEW1 = IPageLayout.ID_RES_NAV;
    public static final String FASTVIEW1 = IPageLayout.ID_BOOKMARKS;
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
     */
    public void createInitialLayout(IPageLayout layout) {
        String folderId = "org.eclipse.ui.test.zoom.mystack";

        IFolderLayout folder = layout.createFolder(folderId,
                IPageLayout.LEFT, 0.5f, IPageLayout.ID_EDITOR_AREA);
        folder.addView(STACK1_VIEW1);
        folder.addView(STACK1_VIEW2);
        folder.addPlaceholder(STACK1_PLACEHOLDER1);
        folder.addView(STACK1_VIEW3);
        
        layout.addView(UNSTACKED_VIEW1,  IPageLayout.TOP, 0.5f, IPageLayout.ID_EDITOR_AREA);
        
        layout.addFastView(FASTVIEW1);
    }
}

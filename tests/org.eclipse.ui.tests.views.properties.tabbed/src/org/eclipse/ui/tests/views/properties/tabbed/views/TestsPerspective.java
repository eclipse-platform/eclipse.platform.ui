/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.views.properties.tabbed.views;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * Perspective used by the tabbed properties view test JUnit tests.
 * 
 * @since 3.3
 */
public class TestsPerspective
    implements IPerspectiveFactory {

    public static final String TESTS_PERSPECTIVE_ID = "org.eclipse.ui.tests.views.properties.tabbed.views.TestsPerspective"; //$NON-NLS-1$

    public void createInitialLayout(IPageLayout layout) {
        String editorArea = layout.getEditorArea();
        IFolderLayout left = layout.createFolder(
            "left", IPageLayout.LEFT, (float) 0.25, editorArea);//$NON-NLS-1$
        left.addView(TestsView.TESTS_VIEW_ID);
        IFolderLayout bottom = layout.createFolder(
            "bottom", IPageLayout.BOTTOM, (float) 0.75,//$NON-NLS-1$
            editorArea);
        bottom.addView(IPageLayout.ID_PROP_SHEET);
    }

}

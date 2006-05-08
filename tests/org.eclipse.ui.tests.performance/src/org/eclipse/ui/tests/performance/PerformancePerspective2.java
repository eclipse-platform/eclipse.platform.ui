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

package org.eclipse.ui.tests.performance;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * @since 3.1
 */
public class PerformancePerspective2 implements IPerspectiveFactory {

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
     */
    public void createInitialLayout(IPageLayout layout) {
    	layout.setEditorAreaVisible(false);
        layout.addView(IPageLayout.ID_TASK_LIST, IPageLayout.RIGHT, .75f, IPageLayout.ID_EDITOR_AREA);
        layout.addView(IPageLayout.ID_OUTLINE, IPageLayout.BOTTOM, .75f, IPageLayout.ID_EDITOR_AREA);
    }
}

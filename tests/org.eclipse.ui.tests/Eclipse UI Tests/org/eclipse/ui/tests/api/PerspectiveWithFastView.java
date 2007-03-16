/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * This perspective is used for testing api. It defines an initial layout with a
 * fast view.
 */
public class PerspectiveWithFastView implements IPerspectiveFactory {

    public static String PERSP_ID = "org.eclipse.ui.tests.fastview_perspective"; //$NON-NLS-1$

    /**
     * Constructs a new Default layout engine.
     */
    public PerspectiveWithFastView() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
     */
    public void createInitialLayout(IPageLayout layout) {
        defineLayout(layout);
    }

    /**
     * Define the initial layout by adding a fast view.
     * 
     * @param layout
     *            The page layout.
     */
    public void defineLayout(IPageLayout layout) {
        layout.addFastView("org.eclipse.ui.views.ResourceNavigator", .8f); //$NON-NLS-1$
    }
}

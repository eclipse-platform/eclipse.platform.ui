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
package org.eclipse.ui.tests.rcp.util;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * This perspective is used for testing api.  It defines an initial
 * layout with no parts, just an editor area.
 */
public class EmptyPerspective implements IPerspectiveFactory {

    /**
     * The perspective id for the empty perspective.
     */
    public static final String PERSP_ID = "org.eclipse.ui.tests.rcp.util.EmptyPerspective"; //$NON-NLS-1$

    /**
     * Constructs a new Default layout engine.
     */
    public EmptyPerspective() {
        super();
    }

    /**
     * Defines the initial layout for a perspective.
     *
     * Implementors of this method may add additional views to a
     * perspective.  The perspective already contains an editor folder
     * with <code>ID = ILayoutFactory.ID_EDITORS</code>.  Add additional views
     * to the perspective in reference to the editor folder.
     *
     * This method is only called when a new perspective is created.  If
     * an old perspective is restored from a persistence file then
     * this method is not called.
     *
     * @param factory the factory used to add views to the perspective
     */
    @Override
	public void createInitialLayout(IPageLayout layout) {
        // do nothing, this is the empty perspective
    }
}

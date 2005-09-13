/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dnd;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * 
 * @since 3.1.1
 */
public class StandaloneViewPerspective implements IPerspectiveFactory {

	public static final String OUTLINE_ID = IPageLayout.ID_OUTLINE;

	public static final String RESOURCE_ID = IPageLayout.ID_RES_NAV;

	public static final String TASK_ID = IPageLayout.ID_TASK_LIST;

	public static final String PERSP_ID = "org.eclipse.ui.tests.dnd.StandaloneViewPerspective";

	public StandaloneViewPerspective() {
		// do nothing
	}

	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(true);

		layout.addStandaloneView(RESOURCE_ID, true, IPageLayout.LEFT, 0.25f,
				IPageLayout.ID_EDITOR_AREA);
		layout.addStandaloneView(OUTLINE_ID, true, IPageLayout.RIGHT, 0.25f,
				IPageLayout.ID_EDITOR_AREA);
		layout.getViewLayout(OUTLINE_ID).setCloseable(false);
		layout.addStandaloneView(TASK_ID, true, IPageLayout.BOTTOM, 0.25f,
				IPageLayout.ID_EDITOR_AREA);
	}
}

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
package org.eclipse.team.internal.ui.sync.views;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class TeamSynchronizingPerspective implements IPerspectiveFactory {

	public final static String ID = "org.eclipse.team.internal.ui.sync.views.TeamSynchronizingPerspective"; //$NON-NLS-1$

	/* (Non-javadoc)
	 * Method declared on IPerpsectiveFactory
	 */
	public void createInitialLayout(IPageLayout layout) {
		defineActions(layout);
		defineLayout(layout);
	}

	/**
	 * Defines the initial actions for a page.  
	 */
	public void defineActions(IPageLayout layout) {

		// Add "new wizards".
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.project"); //$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder"); //$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file"); //$NON-NLS-1$

		// Add "show views".
		layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		layout.addShowViewShortcut(SynchronizeView.VIEW_ID);
		
		layout.addActionSet("org.eclipse.team.ui.actionSet"); //$NON-NLS-1$
	}

	/**
	 * Defines the initial layout for a page.  
	 */
	public void defineLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		IFolderLayout top = layout.createFolder("top", IPageLayout.LEFT, 0.25f, editorArea);	//$NON-NLS-1$
		top.addView(SynchronizeView.VIEW_ID);
		top.addView(IPageLayout.ID_RES_NAV);
		top.addView(IPageLayout.ID_OUTLINE);
		layout.setEditorAreaVisible(true);
	}
}

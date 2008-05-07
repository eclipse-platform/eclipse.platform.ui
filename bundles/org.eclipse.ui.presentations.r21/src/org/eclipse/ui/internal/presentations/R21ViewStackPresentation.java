/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.presentations.r21.R21PresentationMessages;
import org.eclipse.ui.internal.presentations.r21.widgets.CTabFolderEvent;
import org.eclipse.ui.internal.presentations.r21.widgets.R21PaneFolder;
import org.eclipse.ui.internal.presentations.r21.widgets.R21PaneFolderButtonListener;
import org.eclipse.ui.presentations.IStackPresentationSite;

/**
 * Controls the appearance of views stacked into the workbench.
 * 
 * @since 3.0
 */
public class R21ViewStackPresentation extends R21BasicStackPresentation {

   	// don't reset this dynamically, so just keep the information static.
	// see bug:
	//   75422 [Presentations] Switching presentation to R21 switches immediately, but only partially
    private static int tabPos = PlatformUI.getPreferenceStore().getInt(IWorkbenchPreferenceConstants.VIEW_TAB_POSITION);
  
    private R21PaneFolderButtonListener showListListener = new R21PaneFolderButtonListener() {

        public void showList(CTabFolderEvent event) {
            event.doit = false;
            showListDefaultLocation();
        }
    };

    /**
	 * Create a new view stack presentation.
	 *
     * @param parent
     * @param newSite
     */
    public R21ViewStackPresentation(Composite parent,
            IStackPresentationSite newSite) {

        super(new R21PaneFolder(parent, SWT.BORDER), newSite);
        R21PaneFolder tabFolder = getPaneFolder();

        tabFolder.addButtonListener(showListListener);

        tabFolder.setTabPosition(tabPos);
        updateGradient();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.skins.Presentation#setActive(boolean)
     */
    public void setActive(boolean isActive) {
        super.setActive(isActive);

        updateGradient();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.DefaultPartPresentation#getPartMenu()
     */
    protected String getPaneName() {
        return R21PresentationMessages.getString("ViewPane.moveView"); //$NON-NLS-1$ 
    }
}

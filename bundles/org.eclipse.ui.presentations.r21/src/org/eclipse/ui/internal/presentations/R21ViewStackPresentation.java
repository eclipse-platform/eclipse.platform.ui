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
package org.eclipse.ui.internal.presentations;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
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

    private IPreferenceStore preferenceStore = WorkbenchPlugin.getDefault()
            .getPreferenceStore();

    private final IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            if (IPreferenceConstants.VIEW_TAB_POSITION
                    .equals(propertyChangeEvent.getProperty())
                    && !isDisposed()) {
                int tabLocation = preferenceStore
                        .getInt(IPreferenceConstants.VIEW_TAB_POSITION);
                getPaneFolder().setTabPosition(tabLocation);
            }
            //			else if (IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS.equals(propertyChangeEvent.getProperty()) && !isDisposed()) {
            //				boolean traditionalTab = preferenceStore.getBoolean(IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS); 
            //				setTabStyle(traditionalTab);
            //			}		
        }
    };

    private R21PaneFolderButtonListener showListListener = new R21PaneFolderButtonListener() {

        public void showList(CTabFolderEvent event) {
            event.doit = false;
            showListDefaultLocation();
        }
    };

    public R21ViewStackPresentation(Composite parent,
            IStackPresentationSite newSite) {

        super(new R21PaneFolder(parent, SWT.BORDER), newSite);
        R21PaneFolder tabFolder = getPaneFolder();

        tabFolder.addButtonListener(showListListener);

        preferenceStore.addPropertyChangeListener(propertyChangeListener);
        int tabLocation = preferenceStore
                .getInt(IPreferenceConstants.VIEW_TAB_POSITION);

        tabFolder.setTabPosition(tabLocation);
        //		setTabStyle(preferenceStore.getBoolean(IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS));

        //		// do not support close box on unselected tabs.
        //		tabFolder.setUnselectedCloseVisible(false);
        //		
        //		// do not support icons in unselected tabs.
        //		tabFolder.setUnselectedImageVisible(false);
        //		
        // set basic colors
        //ColorSchemeService.setTabAttributes(this, tabFolder);

        updateGradient();
    }

    //	/**
    //     * Set the tab folder tab style to a tradional style tab
    //	 * @param traditionalTab <code>true</code> if traditional style tabs should be used
    //     * <code>false</code> otherwise.
    //	 */
    //	protected void setTabStyle(boolean traditionalTab) {
    //		// set the tab style to non-simple
    //		getTabFolder().setSimpleTab(traditionalTab);
    //	}

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.skins.Presentation#setActive(boolean)
     */
    public void setActive(boolean isActive) {
        super.setActive(isActive);

        updateGradient();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.StackPresentation#dispose()
     */
    public void dispose() {
        preferenceStore.removePropertyChangeListener(propertyChangeListener);
        super.dispose();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.DefaultPartPresentation#getPartMenu()
     */
    protected String getPaneName() {
        return R21PresentationMessages.getString("ViewPane.moveView"); //$NON-NLS-1$ 
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.r21presentation.presentations.R21BasicStackPresentation#drawGradient(org.eclipse.swt.graphics.Color, org.eclipse.swt.graphics.Color[], int[], boolean)
     */

}
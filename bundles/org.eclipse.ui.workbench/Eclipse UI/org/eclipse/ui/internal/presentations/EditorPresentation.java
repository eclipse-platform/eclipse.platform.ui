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
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.Gradient;
import org.eclipse.jface.resource.GradientRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ColorSchemeService;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.IWorkbenchPresentationConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.themes.ITheme;

/**
 * Controls the appearance of views stacked into the workbench.
 * 
 * @since 3.0
 */
public class EditorPresentation extends BasicStackPresentation {

    private IPreferenceStore preferenceStore = WorkbenchPlugin.getDefault()
            .getPreferenceStore();

    private final IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            if (IPreferenceConstants.VIEW_TAB_POSITION
                    .equals(propertyChangeEvent.getProperty())
                    && !isDisposed()) {
                int tabLocation = preferenceStore
                        .getInt(IPreferenceConstants.VIEW_TAB_POSITION);
                setTabPosition(tabLocation);
            } else if (IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS
                    .equals(propertyChangeEvent.getProperty())
                    && !isDisposed()) {
                boolean traditionalTab = preferenceStore
                        .getBoolean(IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS);
                setTabStyle(traditionalTab);
            }
            
			boolean multiChanged = IPreferenceConstants.SHOW_MULTIPLE_EDITOR_TABS.equals(propertyChangeEvent.getProperty());
			boolean styleChanged = IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS.equals(propertyChangeEvent.getProperty());
	        CTabFolder tabFolder = getTabFolder();
	        
			if ((multiChanged || styleChanged) && tabFolder != null) {
				if (multiChanged) {
					boolean multi = preferenceStore.getBoolean(IPreferenceConstants.SHOW_MULTIPLE_EDITOR_TABS);					
					tabFolder.setSingleTab(!multi);
				} else {
					boolean simple = preferenceStore.getBoolean(IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS);
					tabFolder.setSimpleTab(simple);
				}

				CTabItem[] tabItems = tabFolder.getItems();
				
				for (int i = 0; i < tabItems.length; i++) {
				    CTabItem tabItem = tabItems[i];
				    initTab(tabItem, getPartForTab(tabItem));
				}
			}         
        }
    };

    public EditorPresentation(Composite parent, IStackPresentationSite newSite,
            int flags) {
        super(new CTabFolder(parent, SWT.BORDER), newSite);
        CTabFolder tabFolder = getTabFolder();
        preferenceStore.addPropertyChangeListener(propertyChangeListener);
        int tabLocation = preferenceStore
                .getInt(IPreferenceConstants.VIEW_TAB_POSITION);
        setTabPosition(tabLocation);
		tabFolder.setSingleTab(!preferenceStore.getBoolean(IPreferenceConstants.SHOW_MULTIPLE_EDITOR_TABS));
        setTabStyle(preferenceStore
                .getBoolean(IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS));
        // do not support close box on unselected tabs.
        tabFolder.setUnselectedCloseVisible(true);
        // do not support icons in unselected tabs.
        tabFolder.setUnselectedImageVisible(true);
        //tabFolder.setBorderVisible(true);
        // set basic colors
        ColorSchemeService.setTabAttributes(tabFolder);
        updateGradient();
        tabFolder.setMinimizeVisible((flags & SWT.MIN) != 0);
        tabFolder.setMaximizeVisible((flags & SWT.MAX) != 0);
    }

    protected void initTab(CTabItem tabItem, IPresentablePart part) {
		String title = part.getTitle().trim();
		String text = title;
		
		if (part.isDirty())
			text = "*" + text; //$NON-NLS-1$
		
		if ((getTabFolder().getStyle() & SWT.MULTI) == 0) {		
			String titleTooltip = part.getTitleToolTip().trim();
	
			if (titleTooltip.endsWith(title))
				titleTooltip =
					titleTooltip
						.substring(0, titleTooltip.lastIndexOf(title))
						.trim();

			if (titleTooltip.endsWith("\\"))
				titleTooltip =
					titleTooltip
						.substring(0, titleTooltip.lastIndexOf("\\"))
						.trim();    
			
			if (titleTooltip.endsWith("/"))
				titleTooltip =
					titleTooltip
						.substring(0, titleTooltip.lastIndexOf("/"))
						.trim();    	
			
			if (titleTooltip.length() >= 1)
				text += " - " + titleTooltip; //$NON-NLS-1$
		}
        
        tabItem.setText(text);
        tabItem.setImage(part.getTitleImage());
        tabItem.setToolTipText(part.getTitleToolTip());        
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.skins.Presentation#setActive(boolean)
     */
    public void setActive(boolean isActive) {
        super.setActive(isActive);

        updateGradient();
    }

    /**
     * Set the tab folder tab style to a tradional style tab
     * 
     * @param traditionalTab
     *            <code>true</code> if traditional style tabs should be used
     *            <code>false</code> otherwise.
     */
    protected void setTabStyle(boolean traditionalTab) {
        // set the tab style to non-simple
        getTabFolder().setSimpleTab(traditionalTab);
    }

    /**
     * Update the tab folder's colours to match the current theme settings and
     * active state
     */
    private void updateGradient() {
        Color fgColor;
        ITheme currentTheme = PlatformUI.getWorkbench().getThemeManager()
                .getCurrentTheme();
        FontRegistry fontRegistry = currentTheme.getFontRegistry();
        ColorRegistry colorRegistry = currentTheme.getColorRegistry();
        GradientRegistry gradientRegistry = currentTheme.getGradientRegistry();
        Gradient gradient = null;

        if (isActive()) {
            fgColor = colorRegistry
                    .get(IWorkbenchPresentationConstants.ACTIVE_TAB_TEXT_COLOR);
            gradient = gradientRegistry
                    .get(IWorkbenchPresentationConstants.ACTIVE_TAB_BG_GRADIENT);
            getTabFolder()
                    .setFont(
                            fontRegistry
                                    .get(IWorkbenchPresentationConstants.ACTIVE_TAB_TEXT_FONT)); //$NON-NLS-1$
        } else {
            fgColor = colorRegistry
                    .get(IWorkbenchPresentationConstants.INACTIVE_TAB_TEXT_COLOR);
            gradient = gradientRegistry
                    .get(IWorkbenchPresentationConstants.INACTIVE_TAB_BG_GRADIENT);
            getTabFolder()
                    .setFont(
                            fontRegistry
                                    .get(IWorkbenchPresentationConstants.INACTIVE_TAB_TEXT_FONT)); //$NON-NLS-1$
        }
        
        drawGradient(fgColor, gradient);
    }
}
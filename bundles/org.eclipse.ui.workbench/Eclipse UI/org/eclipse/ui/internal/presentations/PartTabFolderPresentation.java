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
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ColorSchemeService;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.IWorkbenchThemeConstants;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.themes.ITheme;

/**
 * Controls the appearance of views stacked into the workbench.
 * 
 * @since 3.0
 */
public class PartTabFolderPresentation extends BasicStackPresentation {
	
	private IPreferenceStore preferenceStore = WorkbenchPlugin.getDefault().getPreferenceStore();
		
	private final IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
			if (IPreferenceConstants.VIEW_TAB_POSITION.equals(propertyChangeEvent.getProperty()) && !isDisposed()) {
				int tabLocation = preferenceStore.getInt(IPreferenceConstants.VIEW_TAB_POSITION); 
				setTabPosition(tabLocation);
			} else if (IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS.equals(propertyChangeEvent.getProperty()) && !isDisposed()) {
				boolean traditionalTab = preferenceStore.getBoolean(IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS); 
				setTabStyle(traditionalTab);
			}		
		}
	};
	
	public PartTabFolderPresentation(Composite parent, IStackPresentationSite newSite) {
		
		super(new CTabFolder(parent, SWT.BORDER), newSite);
		CTabFolder tabFolder = getTabFolder();
		
		preferenceStore.addPropertyChangeListener(propertyChangeListener);
		int tabLocation = preferenceStore.getInt(IPreferenceConstants.VIEW_TAB_POSITION); 
		
		setTabPosition(tabLocation);
		setTabStyle(preferenceStore.getBoolean(IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS));
		
		// do not support close box on unselected tabs.
		tabFolder.setUnselectedCloseVisible(false);
		
		// do not support icons in unselected tabs.
		tabFolder.setUnselectedImageVisible(false);
		
		// set basic colors
		ColorSchemeService.setTabAttributes(this, tabFolder);

		updateGradient();
	}
	
	/**
     * Set the tab folder tab style to a tradional style tab
	 * @param traditionalTab <code>true</code> if traditional style tabs should be used
     * <code>false</code> otherwise.
	 */
	protected void setTabStyle(boolean traditionalTab) {
		// set the tab style to non-simple
		getTabFolder().setSimpleTab(traditionalTab);
	}

	/**
	 * Update the tab folder's colours to match the current theme settings
	 * and active state
	 */
	private void updateGradient() {
		Color fgColor;
		ITheme currentTheme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
        FontRegistry fontRegistry = currentTheme.getFontRegistry();	    
		ColorRegistry colorRegistry = currentTheme.getColorRegistry();
		Color [] bgColors = new Color[2];
		int [] percent = new int[1];
		boolean vertical;
		
        if (isActive()){
        	
        	CTabItem item = getTabFolder().getSelection();
            if(item != null && !getPartForTab(item).isBusy()){
            	Font tabFont = fontRegistry.get(IWorkbenchThemeConstants.TAB_TEXT_FONT);
            	item.setFont(tabFont);
            }
            
	        fgColor = colorRegistry.get(IWorkbenchThemeConstants.ACTIVE_TAB_TEXT_COLOR);
            bgColors[0] = colorRegistry.get(IWorkbenchThemeConstants.ACTIVE_TAB_BG_START);
            bgColors[1] = colorRegistry.get(IWorkbenchThemeConstants.ACTIVE_TAB_BG_END);
            percent[0] = currentTheme.getInt(IWorkbenchThemeConstants.ACTIVE_TAB_PERCENT);
            vertical = currentTheme.getBoolean(IWorkbenchThemeConstants.ACTIVE_TAB_VERTICAL);
		} else {
	        fgColor = colorRegistry.get(IWorkbenchThemeConstants.INACTIVE_TAB_TEXT_COLOR);
            bgColors[0] = colorRegistry.get(IWorkbenchThemeConstants.INACTIVE_TAB_BG_START);
            bgColors[1] = colorRegistry.get(IWorkbenchThemeConstants.INACTIVE_TAB_BG_END);
            percent[0] = currentTheme.getInt(IWorkbenchThemeConstants.INACTIVE_TAB_PERCENT);
            vertical = currentTheme.getBoolean(IWorkbenchThemeConstants.INACTIVE_TAB_VERTICAL);
		}	
		drawGradient(fgColor, bgColors, percent, vertical);	
	}
	
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
	 * @see org.eclipse.ui.internal.presentations.BasicStackPresentation#getPartMenu()
	 */
	protected String getPaneName() {
		return WorkbenchMessages.getString("ViewPane.moveView"); //$NON-NLS-1$ 
	}
}

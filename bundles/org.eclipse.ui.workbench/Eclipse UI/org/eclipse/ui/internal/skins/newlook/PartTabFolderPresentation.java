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
package org.eclipse.ui.internal.skins.newlook;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.Gradient;
import org.eclipse.jface.resource.GradientRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.ColorSchemeService;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.skins.IStackPresentationSite;
import org.eclipse.ui.internal.themes.ITabThemeDescriptor;
import org.eclipse.ui.internal.themes.IThemeDescriptor;
import org.eclipse.ui.themes.ITheme;

/**
 * Controls the appearance of views stacked into the workbench.
 * 
 * @since 3.0
 */
public class PartTabFolderPresentation extends BasicStackPresentation {
	
	private IPreferenceStore preferenceStore = WorkbenchPlugin.getDefault().getPreferenceStore();
	private ITheme theme;
		
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
	
	public PartTabFolderPresentation(Composite parent, IStackPresentationSite newSite, 
			int flags, ITheme theme) {
		
		super(new CTabFolder(parent, SWT.BORDER), newSite);
		this.theme = theme;
		CTabFolder tabFolder = getTabFolder();
		
		preferenceStore.addPropertyChangeListener(propertyChangeListener);
		int tabLocation = preferenceStore.getInt(IPreferenceConstants.VIEW_TAB_POSITION); 
		
		setTabPosition(tabLocation);
		setTabStyle(preferenceStore.getBoolean(IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS));
		
		// do not support close box on unselected tabs.
		tabFolder.setUnselectedCloseVisible(false);
		
		// do not support icons in unselected tabs.
		tabFolder.setUnselectedImageVisible(false);
		
		//tabFolder.setBorderVisible(true);
		// set basic colors
		ColorSchemeService.setTabColors(tabFolder);

		applyTheme(theme);
		
		tabFolder.setMinimizeVisible((flags & SWT.MIN) != 0);
		tabFolder.setMaximizeVisible((flags & SWT.MAX) != 0);
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

	private void applyTheme(ITheme theTheme) {
		this.theme = theTheme;
		
		CTabFolder tabFolder = getTabFolder();
		
		FontRegistry fontRegistry = theTheme.getFontRegistry();
	    tabFolder.setFont(fontRegistry.get("org.eclipse.workbench.tabFont")); //$NON-NLS-1$
	    
	    updateGradient();
	    
	    ITabThemeDescriptor tabThemeDescriptor = theTheme.getTabTheme();

	    // TODO: This is bad... the theme shouldn't be returning null for this
	    // kind of thing. If there are no custom values, it should still return
	    // a theme descriptor that describes the defaults. For now, just bail
	    // to prevent a NPE.
	    if (tabThemeDescriptor == null) {
	    	return;
	    }
	    
		//	if (tabThemeDescriptor.getSelectedImageDesc() != null)
		//		tabFolder.setSelectedTabImage(tabThemeDescriptor.getSelectedImageDesc().createImage());
		//	if (tabThemeDescriptor.getUnselectedImageDesc() != null)
		//		tabFolder.setUnselectedTabImage(tabThemeDescriptor.getUnselectedImageDesc().createImage());

		if (tabThemeDescriptor.getTabMarginSize(SWT.DEFAULT) != -1) {
			//		tabFolder.setUseSameMarginAllSides(true);		
			//		tabFolder.setMarginHeight(tabThemeDescriptor.getTabMarginSize(SWT.DEFAULT));
			//		tabFolder.setBorderMarginHeightColor(tabThemeDescriptor.getTabMarginColor(SWT.DEFAULT));
		} else if (tabThemeDescriptor.getTabMarginSize(getTabPosition()) != -1) {
			//		tabFolder.setMarginHeight(tabThemeDescriptor.getTabMarginSize(tabPosition));
			//		tabFolder.setBorderMarginHeightColor(tabThemeDescriptor.getTabMarginColor(tabPosition));
		}

		if (tabThemeDescriptor.getTabFixedHeight() > 0) {
			tabFolder.setTabHeight(tabThemeDescriptor.getTabFixedHeight());
		}
		if (tabThemeDescriptor.getTabFixedWidth() > 0) {
			//		tabFolder.setTabWidth(tabThemeDescriptor.getTabFixedWidth());
		}
		if (tabThemeDescriptor.getBorderStyle() == SWT.NONE) {
			tabFolder.setBorderVisible(false);
		}

		//	setTabDragInFolder(tabThemeDescriptor.isDragInFolder());
	}
	
	private ITheme getTheme() {
		return theme;
	}
	
	/**
	 * Update the tab folder's colours to match the current theme settings
	 * and active state
	 */
	private void updateGradient() {
		Color fgColor;
		Color[] bgColors;
		int[] bgPercents;

		ColorRegistry colorRegistry = getTheme().getColorRegistry();
		GradientRegistry gradientRegistry = getTheme().getGradientRegistry();
		
		Gradient gradient = null;
        if (isActive()){
	        fgColor = colorRegistry.get(IThemeDescriptor.VIEW_TITLE_TEXT_COLOR_ACTIVE);
	        gradient = gradientRegistry.get(IThemeDescriptor.VIEW_TITLE_GRADIENT_COLOR_ACTIVE);
//	        fgColor = WorkbenchColors.getActiveViewForeground();
//			bgColors = WorkbenchColors.getActiveViewGradient();
//			bgPercents = WorkbenchColors.getActiveViewGradientPercents();
		} else {
	        fgColor = colorRegistry.get(IThemeDescriptor.VIEW_TITLE_TEXT_COLOR_DEACTIVATED);
	        gradient = gradientRegistry.get(IThemeDescriptor.VIEW_TITLE_GRADIENT_COLOR_DEACTIVATED);
//			fgColor = WorkbenchColors.getActiveViewForeground();
//			bgColors = WorkbenchColors.getActiveNoFocusViewGradient();
//			bgPercents = WorkbenchColors.getActiveNoFocusViewGradientPercents();
		}		
		drawGradient(fgColor, gradient);
		//drawGradient(fgColor, bgColors, bgPercents, activeState);	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.Presentation#setActive(boolean)
	 */
	public void setActive(boolean isActive) {
		super.setActive(isActive);
		
		updateGradient();
	}
}

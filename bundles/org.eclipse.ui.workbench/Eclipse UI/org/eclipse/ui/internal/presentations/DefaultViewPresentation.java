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
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.IWorkbenchThemeConstants;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.themes.ITheme;

/**
 * Controls the appearance of views stacked into the workbench.
 * 
 * @since 3.0
 */
public class DefaultViewPresentation extends DefaultPartPresentation {
	
	private IPreferenceStore preferenceStore = WorkbenchPlugin.getDefault().getPreferenceStore();
	private IPreferenceStore apiPreferenceStore = PrefUtil.getAPIPreferenceStore();
		
	private final IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
			if (IPreferenceConstants.VIEW_TAB_POSITION.equals(propertyChangeEvent.getProperty()) && !isDisposed()) {
				int tabLocation = preferenceStore.getInt(IPreferenceConstants.VIEW_TAB_POSITION); 
				getTabFolder().setTabPosition(tabLocation);
				layout(false);
			} else if (IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS.equals(propertyChangeEvent.getProperty()) && !isDisposed()) {
				boolean traditionalTab = apiPreferenceStore.getBoolean(IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS); 
				setTabStyle(traditionalTab);
			}		
		}
	};
	
	public DefaultViewPresentation(Composite parent, IStackPresentationSite newSite) {
		
		super(new PaneFolder(parent, SWT.BORDER), newSite);
		PaneFolder tabFolder = getTabFolder();
		
		preferenceStore.addPropertyChangeListener(propertyChangeListener);
		apiPreferenceStore.addPropertyChangeListener(propertyChangeListener);
		int tabLocation = preferenceStore.getInt(IPreferenceConstants.VIEW_TAB_POSITION); 
		
		tabFolder.setTabPosition(tabLocation);
		setTabStyle(apiPreferenceStore.getBoolean(IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS));
		
		// do not support close box on unselected tabs.
		tabFolder.setUnselectedCloseVisible(false);
		
		// do not support icons in unselected tabs.
		tabFolder.setUnselectedImageVisible(false);
		
		init();
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


	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.presentations.DefaultPartPresentation#updateGradient()
	 */
	protected void updateGradient() {
        if (isDisposed())
            return;

	    ITheme theme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();	    
	    	    
	    if (isActive()) {
	        setActiveTabColors();
	    }
	    else {
	        setInactiveTabColors();
	    }
	    boolean resizeNeeded = false;
	    
    	CTabItem item = getTabFolder().getSelection();
    	Font tabFont = theme.getFontRegistry().get(IWorkbenchThemeConstants.TAB_TEXT_FONT);
        if(item != null && !getPartForTab(item).isBusy()){
        	item.setFont(null);
        }            
	    
	    Font oldTabFont = getTabFolder().getControl().getFont();
	    if (!oldTabFont.equals(tabFont)) {	    	    
	        getTabFolder().getControl().setFont(tabFont);
	        resizeNeeded = true;
	    }
	    
	    //call super to ensure that the toolbar is updated properly.	    
	    super.updateGradient();	 	   	   
	    
	    if (resizeNeeded) {	        
			getTabFolder().setTabHeight(computeTabHeight());

			//ensure proper control sizes for new fonts
		    setControlSize();
	    }
  	}

    /* (non-Javadoc)
	 * @see org.eclipse.ui.internal.skins.Presentation#setActive(int)
	 */
	public void setActive(int newState) {
		super.setActive(newState);
		
		updateGradient();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.presentations.DefaultPartPresentation#widgetDisposed()
	 */
	protected void widgetDisposed() {
		preferenceStore.removePropertyChangeListener(propertyChangeListener);
		apiPreferenceStore.removePropertyChangeListener(propertyChangeListener);
		super.widgetDisposed();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.presentations.DefaultPartPresentation#getPartMenu()
	 */
	protected String getPaneName() {
		return WorkbenchMessages.getString("ViewPane.moveView"); //$NON-NLS-1$ 
	}
}

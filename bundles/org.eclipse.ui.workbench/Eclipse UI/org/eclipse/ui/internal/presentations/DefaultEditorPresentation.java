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

import java.text.MessageFormat;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.IWorkbenchThemeConstants;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.StackPresentation;
import org.eclipse.ui.themes.ITheme;

/**
 * Controls the appearance of views stacked into the workbench.
 * 
 * @since 3.0
 */
public class DefaultEditorPresentation extends DefaultPartPresentation {

    private IPreferenceStore preferenceStore = WorkbenchPlugin.getDefault()
            .getPreferenceStore();
    private IPreferenceStore apiPreferenceStore = PrefUtil.getAPIPreferenceStore();

    public static String DIRTY_PREFIX = "*";
    
    private final IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            if (IPreferenceConstants.EDITOR_TAB_POSITION
                    .equals(propertyChangeEvent.getProperty())
                    && !isDisposed()) {
                int tabLocation = preferenceStore
                        .getInt(IPreferenceConstants.EDITOR_TAB_POSITION);
                getTabFolder().setTabPosition(tabLocation);
                layout(false);
            } else if (IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS
                    .equals(propertyChangeEvent.getProperty())
                    && !isDisposed()) {
                boolean traditionalTab = apiPreferenceStore
                        .getBoolean(IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS);
                setTabStyle(traditionalTab);
            }

            boolean multiChanged = IPreferenceConstants.SHOW_MULTIPLE_EDITOR_TABS
                    .equals(propertyChangeEvent.getProperty());
            boolean styleChanged = IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS
                    .equals(propertyChangeEvent.getProperty());
            PaneFolder tabFolder = getTabFolder();

            if ((multiChanged || styleChanged) && tabFolder != null) {
                if (multiChanged) {
                    boolean multi = preferenceStore
                            .getBoolean(IPreferenceConstants.SHOW_MULTIPLE_EDITOR_TABS);
                    tabFolder.setSingleTab(!multi);
                } else {
                    boolean simple = apiPreferenceStore
                            .getBoolean(IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS);
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

    public DefaultEditorPresentation(Composite parent, IStackPresentationSite newSite) {
        super(new PaneFolder(parent, SWT.BORDER), newSite);
        final PaneFolder tabFolder = getTabFolder();

        preferenceStore.addPropertyChangeListener(propertyChangeListener);
        apiPreferenceStore.addPropertyChangeListener(propertyChangeListener);
        int tabLocation = preferenceStore
                .getInt(IPreferenceConstants.EDITOR_TAB_POSITION);
        tabFolder.setTabPosition(tabLocation);
        tabFolder.setSingleTab(!preferenceStore
                .getBoolean(IPreferenceConstants.SHOW_MULTIPLE_EDITOR_TABS));
        setTabStyle(apiPreferenceStore
                .getBoolean(IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS));
        // do not support close box on unselected tabs.
        tabFolder.setUnselectedCloseVisible(true);
        // do not support icons in unselected tabs.
        tabFolder.setUnselectedImageVisible(true);

        getSystemMenuManager().add(new UpdatingActionContributionItem(new SystemMenuCloseOthers(this)));
        getSystemMenuManager().add(new UpdatingActionContributionItem(new SystemMenuCloseAll(this)));
        
        init();
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.presentations.DefaultPartPresentation#widgetDisposed()
	 */
	protected void widgetDisposed() {
        preferenceStore.removePropertyChangeListener(propertyChangeListener);
        apiPreferenceStore.removePropertyChangeListener(propertyChangeListener);
        super.widgetDisposed();
	}
	
    protected void initTab(CTabItem tabItem, IPresentablePart part) {
        tabItem.setText(getLabelText(part, (getTabFolder().getControl().getStyle() & SWT.MULTI) == 0));
        tabItem.setImage(getLabelImage(part));
        String toolTipText = part.getTitleToolTip();
        if (!toolTipText.equals(Util.ZERO_LENGTH_STRING)) {
        	tabItem.setToolTipText(toolTipText);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.StackPresentation#setActive(int)
     */
    public void setActive(int newState) {
        super.setActive(newState);
       
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
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.presentations.DefaultPartPresentation#getCurrentTitle()
     */
    protected String getCurrentTitle() {
        return ""; //$NON-NLS-1$
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.DefaultPartPresentation#updateGradient()
     */
    protected void updateGradient() {
        if (isDisposed())
            return;

	    ITheme theme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();	    
	    ColorRegistry colorRegistry = theme.getColorRegistry();
	    
	    
	    if (getActive() == StackPresentation.AS_ACTIVE_FOCUS) {	        
	        setActiveTabColors();
	    }
	    else if (getActive() == StackPresentation.AS_ACTIVE_NOFOCUS) {
	        drawGradient(
	                colorRegistry.get(IWorkbenchThemeConstants.INACTIVE_TAB_TEXT_COLOR), 
	                new Color [] {
	                        colorRegistry.get(IWorkbenchThemeConstants.INACTIVE_TAB_BG_START) 
	                }, 
	                new int [0],
	                true);	        
	    }
	    else {
	        setInactiveTabColors();
	    }
	    
	    boolean resizeNeeded = false;
	    Font tabFont = theme.getFontRegistry().get(IWorkbenchThemeConstants.TAB_TEXT_FONT);
	    Font oldTabFont = getTabFolder().getControl().getFont();
	    if (!oldTabFont.equals(tabFont)) {	    	    
	        getTabFolder().getControl().setFont(tabFont);

	        //only layout on font changes.
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
	 * @see org.eclipse.ui.internal.presentations.DefaultPartPresentation#getPaneName()
	 */
	protected String getPaneName() {		
		return WorkbenchMessages.getString("EditorPane.moveEditor"); //$NON-NLS-1$ 
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.presentations.DefaultPartPresentation#getLabelText(org.eclipse.ui.presentations.IPresentablePart, boolean)
	 */
	String getLabelText(IPresentablePart presentablePart, boolean includePath) {
	    String title = super.getLabelText(presentablePart, includePath); 
		String text = title;

        if (includePath) {
        	String contentDescription = presentablePart.getTitleStatus();
        	
        	if (contentDescription.equals("")) { //$NON-NLS-1$
	        	
	            String titleTooltip = presentablePart.getTitleToolTip().trim();
	
	            if (titleTooltip.endsWith(title))
	                    titleTooltip = titleTooltip.substring(0,
	                            titleTooltip.lastIndexOf(title)).trim();
	
	            if (titleTooltip.endsWith("\\")) //$NON-NLS-1$
	                    titleTooltip = titleTooltip.substring(0,
	                            titleTooltip.lastIndexOf("\\")).trim(); //$NON-NLS-1$
	
	            if (titleTooltip.endsWith("/")) //$NON-NLS-1$
	                    titleTooltip = titleTooltip.substring(0,
	                            titleTooltip.lastIndexOf("/")).trim(); //$NON-NLS-1$
	            
	            contentDescription = titleTooltip;	            
        	}
        	
        	if (!contentDescription.equals("")) {  //$NON-NLS-1$
        		text = MessageFormat.format(WorkbenchMessages.getString("EditorPart.AutoTitleFormat"), new String[] {text, contentDescription}); //$NON-NLS-1$
        	}
        }

        if (presentablePart.isDirty()) {
                text = DIRTY_PREFIX + text; //$NON-NLS-1$
        }

        return text;
	}
}
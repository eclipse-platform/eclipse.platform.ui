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

import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.AbstractHandler;
import org.eclipse.ui.commands.ExecutionException;
import org.eclipse.ui.commands.HandlerSubmission;
import org.eclipse.ui.commands.IHandler;
import org.eclipse.ui.commands.Priority;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.IWorkbenchThemeConstants;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.Util;
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

    private HandlerSubmission openEditorDropDownHandlerSubmission;

    private PaneFolderButtonListener showListListener = new PaneFolderButtonListener () {

        public void showList(CTabFolderEvent event) {
            event.doit = false;
            showListDefaultLocation();
        }
    };

    private final IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            if (IPreferenceConstants.EDITOR_TAB_POSITION
                    .equals(propertyChangeEvent.getProperty())
                    && !isDisposed()) {
                int tabLocation = preferenceStore
                        .getInt(IPreferenceConstants.EDITOR_TAB_POSITION);
                getTabFolder().setTabPosition(tabLocation);
            } else if (IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS
                    .equals(propertyChangeEvent.getProperty())
                    && !isDisposed()) {
                boolean traditionalTab = preferenceStore
                        .getBoolean(IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS);
                setTabStyle(traditionalTab);
            }

            boolean multiChanged = IPreferenceConstants.SHOW_MULTIPLE_EDITOR_TABS
                    .equals(propertyChangeEvent.getProperty());
            boolean styleChanged = IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS
                    .equals(propertyChangeEvent.getProperty());
            PaneFolder tabFolder = getTabFolder();

            if ((multiChanged || styleChanged) && tabFolder != null) {
                if (multiChanged) {
                    boolean multi = preferenceStore
                            .getBoolean(IPreferenceConstants.SHOW_MULTIPLE_EDITOR_TABS);
                    tabFolder.setSingleTab(!multi);
                } else {
                    boolean simple = preferenceStore
                            .getBoolean(IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS);
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

    public EditorPresentation(Composite parent, IStackPresentationSite newSite) {
        super(new PaneFolder(parent, SWT.BORDER), newSite);
        final PaneFolder tabFolder = getTabFolder();
        tabFolder.addButtonListener(showListListener);
        preferenceStore.addPropertyChangeListener(propertyChangeListener);
        int tabLocation = preferenceStore
                .getInt(IPreferenceConstants.EDITOR_TAB_POSITION);
        tabFolder.setTabPosition(tabLocation);
        tabFolder.setSingleTab(!preferenceStore
                .getBoolean(IPreferenceConstants.SHOW_MULTIPLE_EDITOR_TABS));
        setTabStyle(preferenceStore
                .getBoolean(IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS));
        // do not support close box on unselected tabs.
        tabFolder.setUnselectedCloseVisible(true);
        // do not support icons in unselected tabs.
        tabFolder.setUnselectedImageVisible(true);
        //tabFolder.setBorderVisible(true);
        // set basic colors       
        updateGradient();
        final Shell shell = tabFolder.getControl().getShell();
        IHandler openEditorDropDownHandler = new AbstractHandler() {

            public Object execute(Map parameterValuesByName) throws ExecutionException {
            	showListDefaultLocation();
                return null;
            }
        };
        openEditorDropDownHandlerSubmission = new HandlerSubmission(null,
                shell, null, "org.eclipse.ui.window.openEditorDropDown", //$NON-NLS-1$
                openEditorDropDownHandler, Priority.MEDIUM);
    }

    public void dispose() {
        PlatformUI.getWorkbench().getCommandSupport()
                .removeHandlerSubmission(
                        openEditorDropDownHandlerSubmission);

        preferenceStore.removePropertyChangeListener(propertyChangeListener);
        getTabFolder().removeButtonListener(showListListener);
        super.dispose();
    }

    protected void initTab(CTabItem tabItem, IPresentablePart part) {
        tabItem.setText(getLabelText(part, (getTabFolder().getControl().getStyle() & SWT.MULTI) == 0));
        tabItem.setImage(getLabelImage(part));
        String toolTipText = part.getTitleToolTip();
        if (!toolTipText.equals(Util.ZERO_LENGTH_STRING)) {
        	tabItem.setToolTipText(toolTipText);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.skins.Presentation#setActive(boolean)
     */
    public void setActive(boolean isActive) {
        super.setActive(isActive);

        updateGradient();
        /*
         * this following is to fix bug 57715
         * when activating the EditorPresentation, add support for drop down list
         * when disactivating the EditorPresentation, remove support for drop down list
         */
        if (openEditorDropDownHandlerSubmission != null)
	        if (isActive)
	        	PlatformUI.getWorkbench().getCommandSupport().addHandlerSubmission(
	                openEditorDropDownHandlerSubmission);
	        else
	        	PlatformUI.getWorkbench().getCommandSupport().removeHandlerSubmission(
	                    openEditorDropDownHandlerSubmission);
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
     * @see org.eclipse.ui.internal.presentations.BasicStackPresentation#getCurrentTitle()
     */
    protected String getCurrentTitle() {
        return ""; //$NON-NLS-1$
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.BasicStackPresentation#updateGradient()
     */
    protected void updateGradient() {
        if (isDisposed())
            return;

	    ITheme theme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();	    
	    ColorRegistry colorRegistry = theme.getColorRegistry();
	    
	    if (isActive()) {
	        drawGradient(
	                colorRegistry.get(IWorkbenchThemeConstants.ACTIVE_TAB_TEXT_COLOR), 
	                new Color [] {
	                        colorRegistry.get(IWorkbenchThemeConstants.ACTIVE_TAB_BG_START), 
	                        colorRegistry.get(IWorkbenchThemeConstants.ACTIVE_TAB_BG_END)
	                }, 
	                new int [] {theme.getInt(IWorkbenchThemeConstants.ACTIVE_TAB_PERCENT)},
	                theme.getBoolean(IWorkbenchThemeConstants.ACTIVE_TAB_VERTICAL));
	    }
	    else {
	        drawGradient(
	                colorRegistry.get(IWorkbenchThemeConstants.INACTIVE_TAB_TEXT_COLOR), 
	                new Color [] {
	                        colorRegistry.get(IWorkbenchThemeConstants.INACTIVE_TAB_BG_START), 
	                        colorRegistry.get(IWorkbenchThemeConstants.INACTIVE_TAB_BG_END)
	                }, 
	                new int [] {theme.getInt(IWorkbenchThemeConstants.INACTIVE_TAB_PERCENT)},
	                theme.getBoolean(IWorkbenchThemeConstants.INACTIVE_TAB_VERTICAL));	        
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
	 * @see org.eclipse.ui.internal.presentations.BasicStackPresentation#getPaneName()
	 */
	protected String getPaneName() {		
		return WorkbenchMessages.getString("EditorPane.moveEditor"); //$NON-NLS-1$ 
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.presentations.BasicStackPresentation#getLabelText(org.eclipse.ui.presentations.IPresentablePart, boolean)
	 */
	String getLabelText(IPresentablePart presentablePart, boolean includePath) {
	    String title = super.getLabelText(presentablePart, includePath); 
		String text = title;

        if (includePath) {
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

            if (titleTooltip.length() >= 1) text += " - " + titleTooltip; //$NON-NLS-1$
        }

        if (presentablePart.isDirty()) {
                text = "* " + text; //$NON-NLS-1$
        }

        return text;
	}
}
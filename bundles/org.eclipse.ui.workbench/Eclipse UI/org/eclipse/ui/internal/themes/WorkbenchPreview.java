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
package org.eclipse.ui.internal.themes;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IThemePreview;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.IWorkbenchThemeConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.themes.ITheme;


/**
 * @since 3.0
 */
public class WorkbenchPreview implements IThemePreview {

    private IPreferenceStore store;
    private boolean disposed = false;
    private CTabFolder folder;
    private ITheme theme;
    private IPropertyChangeListener fontAndColorListener = new IPropertyChangeListener(){        
        public void propertyChange(PropertyChangeEvent event) {  
            if (!disposed)
                setColorsAndFonts();              
        }};
        
    private IPropertyChangeListener preferenceListener = new IPropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent event) {
			if (IPreferenceConstants.VIEW_TAB_POSITION.equals(event.getProperty()) && !disposed) {				 
				setTabPosition();
			} else if (IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS.equals(event.getProperty()) && !disposed) {				
				setTabStyle();
			}		
        }};


    /* (non-Javadoc)
     * @see org.eclipse.ui.IPresentationPreview#createControl(org.eclipse.swt.widgets.Composite, org.eclipse.ui.themes.ITheme)
     */
    public void createControl(Composite parent, ITheme currentTheme) {        
        this.theme = currentTheme;
        store = WorkbenchPlugin.getDefault().getPreferenceStore();
        folder = new CTabFolder(parent, SWT.BORDER);
        folder.setUnselectedCloseVisible(false);
        folder.setEnabled(false);
        folder.setMaximizeVisible(true);
        folder.setMinimizeVisible(true);
        CTabItem item = new CTabItem(folder, SWT.CLOSE);        
        item.setText("Lorem"); //$NON-NLS-1$
        Composite c = new Composite(folder, SWT.READ_ONLY);
        c.setLayout(new FillLayout());
        c.setBackground(folder.getDisplay().getSystemColor(SWT.COLOR_WHITE));        
        Text text = new Text(c, SWT.NONE);
        text.setText("Lorem ipsum dolor sit amet\n"); //$NON-NLS-1$                
        item = new CTabItem(folder, SWT.CLOSE);
        item.setText("Ipsum"); //$NON-NLS-1$
        item.setControl(c);        
            
        folder.setSelection(item);
        
        item = new CTabItem(folder, SWT.CLOSE);
        item.setText("Dolor"); //$NON-NLS-1$
        item = new CTabItem(folder, SWT.CLOSE);
        item.setText("Sit"); //$NON-NLS-1$
        
        currentTheme.addPropertyChangeListener(fontAndColorListener);
        store.addPropertyChangeListener(preferenceListener);
        setColorsAndFonts();
        setTabPosition();
        setTabStyle();
    }

    /**
     * Set the tab style from preferences.
     */
    protected void setTabStyle() {
        boolean traditionalTab = store.getBoolean(IPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS);
        folder.setSimpleTab(traditionalTab);
    }

    /**
     * Set the tab location from preferences.
     */
    protected void setTabPosition() {
        int tabLocation = store.getInt(IPreferenceConstants.VIEW_TAB_POSITION);
        folder.setTabPosition(tabLocation);        
    }

    /**
     * Set the folder colors and fonts
     */
    private void setColorsAndFonts() {
        folder.setSelectionForeground(theme.getColorRegistry().get(IWorkbenchThemeConstants.ACTIVE_TAB_TEXT_COLOR));               
        folder.setForeground(theme.getColorRegistry().get(IWorkbenchThemeConstants.INACTIVE_TAB_TEXT_COLOR));
        
        Color [] colors = new Color[2];
        colors[0] = theme.getColorRegistry().get(IWorkbenchThemeConstants.INACTIVE_TAB_BG_START);
        colors[1] = theme.getColorRegistry().get(IWorkbenchThemeConstants.INACTIVE_TAB_BG_END);
        folder.setBackground(colors, new int [] {theme.getInt(IWorkbenchThemeConstants.INACTIVE_TAB_PERCENT)}, theme.getBoolean(IWorkbenchThemeConstants.INACTIVE_TAB_VERTICAL));
        
        colors[0] = theme.getColorRegistry().get(IWorkbenchThemeConstants.ACTIVE_TAB_BG_START);
        colors[1] = theme.getColorRegistry().get(IWorkbenchThemeConstants.ACTIVE_TAB_BG_END);
        folder.setSelectionBackground(colors, new int [] {theme.getInt(IWorkbenchThemeConstants.ACTIVE_TAB_PERCENT)}, theme.getBoolean(IWorkbenchThemeConstants.ACTIVE_TAB_VERTICAL));
        
        folder.setFont(theme.getFontRegistry().get(IWorkbenchThemeConstants.TAB_TEXT_FONT));
    }


    /* (non-Javadoc)
     * @see org.eclipse.ui.IPresentationPreview#dispose()
     */
    public void dispose() {
        disposed = true;
        theme.removePropertyChangeListener(fontAndColorListener);
        store.removePropertyChangeListener(preferenceListener);
    }
}

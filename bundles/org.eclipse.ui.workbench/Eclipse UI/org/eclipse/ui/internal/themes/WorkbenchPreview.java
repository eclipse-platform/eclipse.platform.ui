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

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IPresentationPreview;
import org.eclipse.ui.internal.IWorkbenchThemeConstants;
import org.eclipse.ui.themes.ITheme;


/**
 * @since 3.0
 */
public class WorkbenchPreview implements IPresentationPreview {

    private CTabFolder folder;
    private ITheme theme;
    private IPropertyChangeListener listener;

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPresentationPreview#createControl(org.eclipse.swt.widgets.Composite, org.eclipse.ui.themes.ITheme)
     */
    public void createControl(Composite parent, ITheme currentTheme) {        
        this.theme = currentTheme;
        folder = new CTabFolder(parent, SWT.BORDER);
        folder.setSimpleTab(false);
        folder.setUnselectedCloseVisible(false);
        folder.setEnabled(false);
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
        
        listener = new IPropertyChangeListener(){        
            public void propertyChange(PropertyChangeEvent event) {
                setAll();              
            }};
            
        folder.setSelection(item);
        currentTheme.addPropertyChangeListener(listener);
        setAll();
    }

    /**
     * 
     */
    private void setAll() {
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
        theme.removePropertyChangeListener(listener);
    }
}

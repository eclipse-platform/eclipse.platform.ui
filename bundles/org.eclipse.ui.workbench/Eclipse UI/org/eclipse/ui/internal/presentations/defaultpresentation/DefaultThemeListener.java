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
package org.eclipse.ui.internal.presentations.defaultpresentation;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.internal.IWorkbenchThemeConstants;
import org.eclipse.ui.internal.preferences.AbstractPropertyListener;
import org.eclipse.ui.internal.preferences.IPropertyMap;
import org.eclipse.ui.presentations.StackPresentation;

/**
 * @since 3.1
 */
public class DefaultThemeListener extends AbstractPropertyListener {

    private DefaultTabFolder folder;
    private IPropertyMap theme;
    
    public DefaultThemeListener(DefaultTabFolder folder, IPropertyMap theme) {
        this.folder = folder;
        this.theme = theme;
    }
    
    private Color getColor(String id, Color defaultValue) {
        Color value = (Color)theme.getValue(id, Color.class);
        if (value == null) {
            value = defaultValue;
        }
        
        return value;
    }
    
    private int getInt(String id, int defaultValue) {
        Integer result = ((Integer)theme.getValue(id, Integer.class));
        
        if (result == null) {
            return defaultValue;
        }
        
        return result.intValue();
    }
    
    private boolean getBoolean(String id, boolean defaultValue) {
        Boolean result = ((Boolean)theme.getValue(id, Boolean.class));
        
        if (result == null) {
            return defaultValue;
        }
        
        return result.booleanValue();
    }
    
    public void update() {
            
        folder.setColors(new DefaultTabFolderColors(
                getColor(IWorkbenchThemeConstants.ACTIVE_TAB_TEXT_COLOR, null),
                new Color[] {
                        getColor(IWorkbenchThemeConstants.ACTIVE_TAB_BG_START, null),
                        getColor(IWorkbenchThemeConstants.ACTIVE_TAB_BG_END, null) 
                        },
                new int[] {
                        getInt(IWorkbenchThemeConstants.ACTIVE_TAB_PERCENT, 0) },
                        getBoolean(IWorkbenchThemeConstants.ACTIVE_TAB_VERTICAL, true)),
                        StackPresentation.AS_ACTIVE_FOCUS, true);
        
        folder.setColors(new DefaultTabFolderColors(
                getColor(IWorkbenchThemeConstants.ACTIVE_NOFOCUS_TAB_TEXT_COLOR, null),
                new Color[] {
                        getColor(IWorkbenchThemeConstants.ACTIVE_NOFOCUS_TAB_BG_START, null),
                        getColor(IWorkbenchThemeConstants.ACTIVE_NOFOCUS_TAB_BG_END, null) 
                        },
                new int[] {
                        getInt(IWorkbenchThemeConstants.ACTIVE_NOFOCUS_TAB_PERCENT, 0) },
                        getBoolean(IWorkbenchThemeConstants.ACTIVE_NOFOCUS_TAB_VERTICAL, true)),
                        StackPresentation.AS_ACTIVE_FOCUS, false);
        
        folder.setColors(new DefaultTabFolderColors(
                getColor(IWorkbenchThemeConstants.INACTIVE_TAB_TEXT_COLOR, null),
                new Color[] {
                        getColor(IWorkbenchThemeConstants.INACTIVE_TAB_BG_START, null),
                        getColor(IWorkbenchThemeConstants.INACTIVE_TAB_BG_END, null) },
                new int[] { 
                		getInt(IWorkbenchThemeConstants.INACTIVE_TAB_PERCENT, 0) },
                		getBoolean(IWorkbenchThemeConstants.INACTIVE_TAB_VERTICAL, true)),
                        StackPresentation.AS_INACTIVE);
        
        folder.setColors(new DefaultTabFolderColors(
                getColor(IWorkbenchThemeConstants.INACTIVE_TAB_TEXT_COLOR, null),
                new Color[] { 
                        getColor(IWorkbenchThemeConstants.INACTIVE_TAB_BG_START, null) },
                new int[0], 
                true), StackPresentation.AS_ACTIVE_NOFOCUS);
        
        folder.setFont((Font)theme.getValue(IWorkbenchThemeConstants.TAB_TEXT_FONT, Font.class));
    }

}

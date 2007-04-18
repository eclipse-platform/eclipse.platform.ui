/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations.defaultpresentation;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.internal.IWorkbenchThemeConstants;
import org.eclipse.ui.internal.preferences.AbstractPropertyListener;
import org.eclipse.ui.internal.preferences.IPropertyMap;
import org.eclipse.ui.internal.preferences.PropertyUtil;
import org.eclipse.ui.internal.themes.LightColorFactory;
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

    /*
     * Update the ACTIVE_TAB_HIGHLIGHT_START color in the color registry.
     * Return true if we're using highlights on tabs, false otherwise.
     * The highlight color is computed based on the ACTIVE_TAB_BG_START.
     * We need to do this here, in the ThemeListener, so that we can catch
     * the change to the ACTIVE_TAB_BG_START begin color and update the
     * highlight color appropriately.
     * @return boolean use highlight color
     */  
    private boolean updateHighlightColor() {
    	if(! useHighlight())
    		return false;
    	//get newTabBegin from theme, not from ColorRegistry, which may not have been updated yet
		RGB newTabBegin = getColor(IWorkbenchThemeConstants.ACTIVE_TAB_BG_START, null).getRGB();
		RGB newHighlight = LightColorFactory.createHighlightStartColor(newTabBegin);
		//Registry handles lifecycle of colors so no leakage and if RGB s.equals then no change
		JFaceResources.getColorRegistry().put(IWorkbenchThemeConstants.ACTIVE_TAB_HIGHLIGHT_START, newHighlight);
		return true;
    }
    
    private boolean useHighlight() {
     	return PropertyUtil.get(
     			this.theme,
     			IWorkbenchThemeConstants.ACTIVE_TAB_HIGHLIGHT,
     			false);
    }
    
    public void update() {
    	Color[] activeFocusBackgroundColors = updateHighlightColor()
    		? new Color[] {
	            getColor(IWorkbenchThemeConstants.ACTIVE_TAB_BG_START, null),
	            getColor(IWorkbenchThemeConstants.ACTIVE_TAB_BG_END, null),
	            JFaceResources.getColorRegistry().get(IWorkbenchThemeConstants.ACTIVE_TAB_HIGHLIGHT_START)
    			}
            : new Color[] {
	            getColor(IWorkbenchThemeConstants.ACTIVE_TAB_BG_START, null),
	            getColor(IWorkbenchThemeConstants.ACTIVE_TAB_BG_END, null)
	            };
    	
        folder.setColors(new DefaultTabFolderColors(
                getColor(IWorkbenchThemeConstants.ACTIVE_TAB_TEXT_COLOR, null),
                activeFocusBackgroundColors,
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

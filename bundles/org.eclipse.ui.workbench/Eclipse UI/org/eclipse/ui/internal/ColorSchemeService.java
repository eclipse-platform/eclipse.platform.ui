/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

/**
 * ColorSchemeService is the service that sets the colors on widgets as
 * appropriate.
 * 
 * TODO: do we need this?  can this be expanded or removed entirely?
 */
public class ColorSchemeService {

    private static final String LISTENER_KEY = "theme.listener"; //$NON-NLS-1$
    
    public static void setViewColors(final Control control) {
	    ITheme theme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
	    if (control.getData(LISTENER_KEY) == null) {
	        final IPropertyChangeListener listener = new IPropertyChangeListener() {

                /* (non-Javadoc)
                 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
                 */
                public void propertyChange(PropertyChangeEvent event) {
                    
                    String property = event.getProperty();
                    if (property.equals(IThemeManager.CHANGE_CURRENT_THEME) 
                            || property.equals(IWorkbenchThemeConstants.INACTIVE_TAB_BG_END)
                            || property.equals(IWorkbenchThemeConstants.VIEW_MESSAGE_TEXT_FONT)) {
                        setViewColors(control);                        
                    }
                }	            
	        };
	        control.setData(LISTENER_KEY, listener);
	        control.addDisposeListener(new DisposeListener() {

                /* (non-Javadoc)
                 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
                 */
                public void widgetDisposed(DisposeEvent e) {
                    PlatformUI
                    .getWorkbench()
                    .getThemeManager()
                    .removePropertyChangeListener(listener);
                }});
	        
	        PlatformUI
	        .getWorkbench()
	        .getThemeManager()
	        .addPropertyChangeListener(listener);	
	        
	        
	    }
	    control.setBackground(theme.getColorRegistry().get(IWorkbenchThemeConstants.INACTIVE_TAB_BG_END));
	    control.setFont(theme.getFontRegistry().get(IWorkbenchThemeConstants.VIEW_MESSAGE_TEXT_FONT));
    }
    
	public static void setTabAttributes(final CTabFolder control) {
	    ITheme theme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
	    if (control.getData(LISTENER_KEY) == null) {
	        final IPropertyChangeListener listener = new IPropertyChangeListener() {

                /* (non-Javadoc)
                 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
                 */
                public void propertyChange(PropertyChangeEvent event) {
                    
                    String property = event.getProperty();
                    if (property.equals(IThemeManager.CHANGE_CURRENT_THEME) 
                            || property.equals(IWorkbenchThemeConstants.INACTIVE_TAB_BG_START)
                            || property.equals(IWorkbenchThemeConstants.INACTIVE_TAB_BG_END)
                            || property.equals(IWorkbenchThemeConstants.INACTIVE_TAB_TEXT_COLOR)
                            || property.equals(IWorkbenchThemeConstants.ACTIVE_TAB_TEXT_COLOR)
							|| property.equals(IWorkbenchThemeConstants.ACTIVE_TAB_BG_START)
							|| property.equals(IWorkbenchThemeConstants.ACTIVE_TAB_BG_END)
                            || property.equals(IWorkbenchThemeConstants.TAB_TEXT_FONT)) {
                        setTabAttributes(control);                        
                    }
                }	            
	        };
	        control.setData(LISTENER_KEY, listener);
	        control.addDisposeListener(new DisposeListener() {

                /* (non-Javadoc)
                 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
                 */
                public void widgetDisposed(DisposeEvent e) {
                    PlatformUI
                    .getWorkbench()
                    .getThemeManager()
                    .removePropertyChangeListener(listener);
                }});
	        
	        PlatformUI
	        .getWorkbench()
	        .getThemeManager()
	        .addPropertyChangeListener(listener);	        
	    }
	    
	    int [] percent = new int[1];
	    boolean vertical;
	    ColorRegistry colorRegistry = theme.getColorRegistry();
        control.setForeground(colorRegistry.get(IWorkbenchThemeConstants.INACTIVE_TAB_TEXT_COLOR));

        Color [] c = new Color[2];
        c[0] = colorRegistry.get(IWorkbenchThemeConstants.INACTIVE_TAB_BG_START);
        c[1] = colorRegistry.get(IWorkbenchThemeConstants.INACTIVE_TAB_BG_END);

        percent[0] = theme.getInt(IWorkbenchThemeConstants.ACTIVE_TAB_PERCENT);
        vertical = theme.getBoolean(IWorkbenchThemeConstants.ACTIVE_TAB_VERTICAL);
        
        control.setBackground(c, percent, vertical);
			
		control.setSelectionForeground(colorRegistry.get(IWorkbenchThemeConstants.ACTIVE_TAB_TEXT_COLOR));
        
		c[0] = colorRegistry.get(IWorkbenchThemeConstants.ACTIVE_TAB_BG_START);
        c[1] = colorRegistry.get(IWorkbenchThemeConstants.ACTIVE_TAB_BG_END);

        percent[0] = theme.getInt(IWorkbenchThemeConstants.ACTIVE_TAB_PERCENT);
        vertical = theme.getBoolean(IWorkbenchThemeConstants.ACTIVE_TAB_VERTICAL);
        
        control.setSelectionBackground(c, percent, vertical);
        
        control.setFont(theme.getFontRegistry().get(IWorkbenchThemeConstants.TAB_TEXT_FONT));
	}	
}

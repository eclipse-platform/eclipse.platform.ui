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

import java.lang.ref.WeakReference;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.presentations.BasicStackPresentation;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

/**
 * ColorSchemeService is the service that sets the colors on widgets as
 * appropriate.
 */
public class ColorSchemeService {

    private static final String LISTENER_KEY = "org.eclipse.ui.internal.ColorSchemeService"; //$NON-NLS-1$
    
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
                    control.setData(LISTENER_KEY, null);
                }});
	        
	        PlatformUI
	        .getWorkbench()
	        .getThemeManager()
	        .addPropertyChangeListener(listener);	
	    }
	    control.setBackground(theme.getColorRegistry().get(IWorkbenchThemeConstants.INACTIVE_TAB_BG_END));
	    control.setFont(theme.getFontRegistry().get(IWorkbenchThemeConstants.VIEW_MESSAGE_TEXT_FONT));
    }
    
	public static void setTabAttributes(BasicStackPresentation presentation, final CTabFolder control) {
	    if (presentation == null)  // the reference to the presentation was lost by the listener
	    	return;	    

	    ITheme theme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
	    if (control.getData(LISTENER_KEY) == null) {
	    	final WeakReference ref = new WeakReference(presentation);
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
                        setTabAttributes((BasicStackPresentation) ref.get(), control);                        
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
                    control.setData(LISTENER_KEY, null);                    
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

        percent[0] = theme.getInt(IWorkbenchThemeConstants.INACTIVE_TAB_PERCENT);
        vertical = theme.getBoolean(IWorkbenchThemeConstants.INACTIVE_TAB_VERTICAL);
        
        control.setBackground(c, percent, vertical);		

        if (presentation.isActive()) {                
			control.setSelectionForeground(colorRegistry.get(IWorkbenchThemeConstants.ACTIVE_TAB_TEXT_COLOR));
			c[0] = colorRegistry.get(IWorkbenchThemeConstants.ACTIVE_TAB_BG_START);
	        c[1] = colorRegistry.get(IWorkbenchThemeConstants.ACTIVE_TAB_BG_END);
	
	        percent[0] = theme.getInt(IWorkbenchThemeConstants.ACTIVE_TAB_PERCENT);
	        vertical = theme.getBoolean(IWorkbenchThemeConstants.ACTIVE_TAB_VERTICAL);
		}
        control.setSelectionBackground(c, percent, vertical);
        CTabItem [] items = control.getItems();
        Font tabFont = theme.getFontRegistry().get(IWorkbenchThemeConstants.TAB_TEXT_FONT);
        for (int i = 0; i < items.length; i++) {
			items[i].setFont(tabFont);
		}
	}	
}

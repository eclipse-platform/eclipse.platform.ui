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
package org.eclipse.ui.internal;


/**
 * A source of color/font/gradient constants used throughout the workbench.
 * 
 * @since 3.0
 */
public interface IWorkbenchPresentationConstants {
    
    // used in FastViewPane.  This seems excessive for a 3 pixel border.  
    // I suggest defining one and then lighten it accordingly or use a gradient 
    // instead.
    public static final String FAST_VIEW_BORDER_1 = "FAST_VIEW_BORDER_1"; //$NON-NLS-1$
    public static final String FAST_VIEW_BORDER_2 = "FAST_VIEW_BORDER_2"; //$NON-NLS-1$
    public static final String FAST_VIEW_BORDER_3 = "FAST_VIEW_BORDER_3"; //$NON-NLS-1$

    public static final String ACTIVE_TAB_TEXT_FONT = "org.eclipse.ui.workbench.ACTIVE_TAB_TEXT_FONT"; //$NON-NLS-1$
    public static final String INACTIVE_TAB_TEXT_FONT = "org.eclipse.ui.workbench.INACTIVE_TAB_TEXT_FONT"; //$NON-NLS-1$
    
    public static final String ACTIVE_TAB_TEXT_COLOR = "org.eclipse.ui.workbench.ACTIVE_TAB_TEXT_COLOR"; //$NON-NLS-1$
    public static final String INACTIVE_TAB_TEXT_COLOR = "org.eclipse.ui.workbench.INACTIVE_TAB_TEXT_COLOR"; //$NON-NLS-1$
    
    public static final String TAB_BG_GRADIENT = "org.eclipse.ui.workbench.TAB_BG_GRADIENT"; //$NON-NLS-1$
    public static final String ACTIVE_TAB_BG_GRADIENT = "org.eclipse.ui.workbench.ACTIVE_TAB_BG_GRADIENT"; //$NON-NLS-1$
    public static final String INACTIVE_TAB_BG_GRADIENT = "org.eclipse.ui.workbench.INACTIVE_TAB_BG_GRADIENT"; //$NON-NLS-1$
    
    public static final String BACKGROUND = "org.eclipse.ui.workbench.BACKGROUND"; //$NON-NLS-1$
    public static final String FOREGROUND = "org.eclipse.ui.workbench.FOREGROUND"; //$NON-NLS-1$
    
}

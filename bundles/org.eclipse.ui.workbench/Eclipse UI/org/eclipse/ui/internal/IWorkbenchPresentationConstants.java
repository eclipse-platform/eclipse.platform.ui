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

    // used in EditorWorkbook
    public static final String EDITOR_TITLE_TEXT_COLOR_ACTIVE_FOCUS = "EDITOR_TITLE_TEXT_COLOR_ACTIVE_FOCUS"; //$NON-NLS-1$
    public static final String EDITOR_TITLE_TEXT_COLOR_ACTIVE_NOFOCUS = "EDITOR_TITLE_TEXT_COLOR_ACTIVE_NOFOCUS"; //$NON-NLS-1$
    public static final String EDITOR_TITLE_TEXT_COLOR_INACTIVE = "EDITOR_TITLE_TEXT_COLOR_INACTIVE"; //$NON-NLS-1$
    
    public static final String EDITOR_TITLE_GRADIENT_INACTIVE = "EDITOR_TITLE_GRADIENT_INACTIVE"; //$NON-NLS-1$
    public static final String EDITOR_TITLE_GRADIENT_ACTIVE_FOCUS = "EDITOR_TITLE_GRADIENT_ACTIVE_FOCUS"; //$NON-NLS-1$
    public static final String EDITOR_TITLE_GRADIENT_ACTIVE_NOFOCUS = "EDITOR_TITLE_GRADIENT_ACTIVE_NOFOCUS"; //$NON-NLS-1$
    
}

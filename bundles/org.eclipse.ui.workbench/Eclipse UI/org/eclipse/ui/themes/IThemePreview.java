/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.themes;

import org.eclipse.swt.widgets.Composite;

/**
 * Interface used by theme element developers to preview the usage of their 
 * elements within the colors and fonts preference page.
 * 
 * @since 3.0
 */
public interface IThemePreview {

    /**
     * Create the preview control.
     *  
     * @param parent the Composite in which to create the example
     * @param currentTheme the theme to preview
     */
    void createControl(Composite parent, ITheme currentTheme);

    /**
     * Dispose of resources used by this previewer.   This method is called by 
     * the workbench when appropriate and should never be called by a user.
     */
    void dispose();
}

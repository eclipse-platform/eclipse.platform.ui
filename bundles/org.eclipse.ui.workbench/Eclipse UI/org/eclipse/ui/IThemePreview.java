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
package org.eclipse.ui;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.themes.ITheme;


/**
 * <em>EXPERIMENTAL</em>
 * @since 3.0
 */
public interface IThemePreview {
    
    /** 
     * @param parent the Composite in which to create the example
     * @param theme the theme to preview
     */
    void createControl(Composite parent, ITheme currentTheme);
    
    /**
     * Dispose of resources used by this previewer.
     */
    void dispose();
}

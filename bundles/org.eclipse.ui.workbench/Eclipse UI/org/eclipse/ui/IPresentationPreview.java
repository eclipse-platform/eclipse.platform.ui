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

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.widgets.Composite;


/**
 * <em>EXPERIMENTAL</em>
 * @since 3.0
 */
public interface IPresentationPreview {
    
    /** 
     * @param parent the Composite in which to create the example
     * @param colorRregistry the registry from which colors should be used
     * @param fontRregistry the registry from which font should be used
     */
    void createControl(Composite parent, ColorRegistry colorRegistry, FontRegistry fontRegistry);
    
    /**
     * Dispose of resources used by this previewer.
     */
    void dispose();
}

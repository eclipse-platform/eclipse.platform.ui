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

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.colors.ColorDefinition;
import org.eclipse.ui.internal.presentation.DisposableColorRegistry;
import org.eclipse.ui.internal.presentation.PresentationRegistryPopulator;


/**
 * @since 3.0
 */
public class Theme implements ITheme {

    private DisposableColorRegistry themeColorRegistry;
    private IThemeDescriptor descriptor;
    
    /**
     * @param descriptor
     */
    public Theme(IThemeDescriptor descriptor) {
        this.descriptor = descriptor;
        if (descriptor != null) {
	        ColorDefinition [] definitions = this.descriptor.getColorOverrides();
	        if (definitions.length > 0) {
	            themeColorRegistry = new DisposableColorRegistry(JFaceResources.getColorRegistry(), Display.getCurrent());
	            PresentationRegistryPopulator.populateRegistry(themeColorRegistry, definitions, null);
	        }
        }
    }
    
    public ColorRegistry getColorRegistry() {
        if (themeColorRegistry != null) 
            return themeColorRegistry;
        else 
            return JFaceResources.getColorRegistry();
    }
    
    public FontRegistry getFontRegistry() {
        return JFaceResources.getFontRegistry();
    }
    
    public void dispose() {
        if (themeColorRegistry != null) {
            themeColorRegistry.dispose();
        }
    }
    
    public ITabThemeDescriptor getTabTheme() {
        return descriptor == null ? null : descriptor.getTabThemeDescriptor();
    }
}

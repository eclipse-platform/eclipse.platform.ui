/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.part.services;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.part.components.services.IPluginResources;
import org.osgi.framework.Bundle;

/**
 * @since 3.1
 */
public class PluginResources implements IPluginResources {

    private Bundle bundle;
    
    public PluginResources(Bundle pluginBundle) {
        bundle = pluginBundle;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.workbench.services.IPluginImages#getImage(java.lang.String)
     */
    public ImageDescriptor getImage(String path) {
        return ImageDescriptor.createFromURL(getPluginURL(path));
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.components.services.IPluginResources#getPluginFile(java.lang.String)
     */
    public URL getPluginURL(String path) {
        return Platform.find(bundle, new Path(path));
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.components.services.IPluginResources#getStateFile(java.lang.String)
     */
    public File getStateFile(String path) {
        return Platform.getStateLocation(bundle).append(path).toFile();
    }

}

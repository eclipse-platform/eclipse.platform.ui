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
package org.eclipse.ui.internal.part.components.services;

import java.io.File;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Provides access to plugin-specific resources.
 * 
 * Not intended to be implemented by clients.
 * 
 * @since 3.1
 */
public interface IPluginResources {
    
    /**
     * Returns an image descriptor for an image in the plugin's directory.
     *
     * @param path plugin-relative path to the image
     * @return an image descriptor for the given image
     */
    public ImageDescriptor getImage(String path);
    
    /**
     * Returns a file in the plugin's state location. This is
     * an area reserved for the plugin's use.
     *
     * @param path local path within the plugin's state location
     * @return a URL pointing to a file in the plugin state location
     */
    public File getStateFile(String path);

    /**
     * Returns a file within the plugin's install directory. 
     *
     * @param path a plugin-relative path. For most plugins, this is relative
     * to the directory that contains the plugin.xml file.
     * @return a URL pointing to a file in the plugin
     */
    public URL getPluginURL(String path);
}

/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Joe Bowbeer (jozart@blarg.net) - removed dependency on runtime compatibility layer (bug 74526)
 *******************************************************************************/
package org.eclipse.ui.examples.readmetool;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Convenience class for storing references to image descriptors
 * used by the readme tool.
 */
public class ReadmeImages {
    static final URL BASE_URL = ReadmePlugin.getDefault().getBundle().getEntry("/"); //$NON-NLS-1$

    static final ImageDescriptor EDITOR_ACTION1_IMAGE;

    static final ImageDescriptor EDITOR_ACTION2_IMAGE;

    static final ImageDescriptor EDITOR_ACTION3_IMAGE;

    static final ImageDescriptor EDITOR_ACTION1_IMAGE_DISABLE;

    static final ImageDescriptor EDITOR_ACTION2_IMAGE_DISABLE;

    static final ImageDescriptor EDITOR_ACTION3_IMAGE_DISABLE;

    static final ImageDescriptor EDITOR_ACTION1_IMAGE_ENABLE;

    static final ImageDescriptor EDITOR_ACTION2_IMAGE_ENABLE;

    static final ImageDescriptor EDITOR_ACTION3_IMAGE_ENABLE;

    static final ImageDescriptor README_WIZARD_BANNER;

    static {
        String iconPath = "icons/"; //$NON-NLS-1$

        String prefix = iconPath + "ctool16/"; //$NON-NLS-1$
        EDITOR_ACTION1_IMAGE = createImageDescriptor(prefix + "action1.gif"); //$NON-NLS-1$
        EDITOR_ACTION2_IMAGE = createImageDescriptor(prefix + "action2.gif"); //$NON-NLS-1$
        EDITOR_ACTION3_IMAGE = createImageDescriptor(prefix + "action3.gif"); //$NON-NLS-1$

        prefix = iconPath + "dtool16/"; //$NON-NLS-1$
        EDITOR_ACTION1_IMAGE_DISABLE = createImageDescriptor(prefix
                + "action1.gif"); //$NON-NLS-1$
        EDITOR_ACTION2_IMAGE_DISABLE = createImageDescriptor(prefix
                + "action2.gif"); //$NON-NLS-1$
        EDITOR_ACTION3_IMAGE_DISABLE = createImageDescriptor(prefix
                + "action3.gif"); //$NON-NLS-1$

        prefix = iconPath + "etool16/"; //$NON-NLS-1$
        EDITOR_ACTION1_IMAGE_ENABLE = createImageDescriptor(prefix
                + "action1.gif"); //$NON-NLS-1$
        EDITOR_ACTION2_IMAGE_ENABLE = createImageDescriptor(prefix
                + "action2.gif"); //$NON-NLS-1$
        EDITOR_ACTION3_IMAGE_ENABLE = createImageDescriptor(prefix
                + "action3.gif"); //$NON-NLS-1$

        prefix = iconPath + "wizban/"; //$NON-NLS-1$
        README_WIZARD_BANNER = createImageDescriptor(prefix
                + "newreadme_wiz.gif"); //$NON-NLS-1$
    }

    /**
     * Utility method to create an <code>ImageDescriptor</code>
     * from a path to a file.
     */
    private static ImageDescriptor createImageDescriptor(String path) {
        try {
            URL url = new URL(BASE_URL, path);
            return ImageDescriptor.createFromURL(url);
        } catch (MalformedURLException e) {
            // do nothing
        }
        return ImageDescriptor.getMissingImageDescriptor();
    }
}

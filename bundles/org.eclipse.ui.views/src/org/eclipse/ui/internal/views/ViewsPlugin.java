/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 460405
 *******************************************************************************/
package org.eclipse.ui.internal.views;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The plug-in runtime class for the views UI plug-in (id <code>"org.eclipse.ui.views"</code>).
 * <p>
 * This class provides static methods and fields only; it is not intended to be
 * instantiated or subclassed by clients.
 * </p>
 *
 * @since 2.1
 */
public final class ViewsPlugin extends AbstractUIPlugin {
    /**
     * Views UI plug-in id (value <code>"org.eclipse.ui.views"</code>).
     */
    public static final String PLUGIN_ID = "org.eclipse.ui.views"; //$NON-NLS-1$

	private final static String ICONS_PATH = "$nl$/icons/full/";//$NON-NLS-1$

    private static ViewsPlugin instance;

    /**
     * Returns the singleton instance.
     *
     * @return the singleton instance.
     */
    public static ViewsPlugin getDefault() {
        return instance;
    }

    /**
     * Creates a new instance of the receiver.
     *
     * @see org.eclipse.core.runtime.Plugin#Plugin()
     */
    public ViewsPlugin() {
        super();
        instance = this;
    }

	/**
	 * Get the workbench image with the given path relative to
	 * ICON_PATH.
	 * @param relativePath
	 * @return ImageDescriptor
	 */
	public static ImageDescriptor getViewImageDescriptor(String relativePath){
		return imageDescriptorFromPlugin(PLUGIN_ID, ICONS_PATH + relativePath);
	}
}

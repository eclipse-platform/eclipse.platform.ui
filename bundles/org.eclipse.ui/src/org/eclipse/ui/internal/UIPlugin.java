/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The plug-in class for the org.eclipse.ui plug-in.
 * This class is internal to the workbench and should not be
 * referenced by clients.
 */
public final class UIPlugin extends AbstractUIPlugin {

    private static UIPlugin inst;

    /**
     * Creates an instance of the UIPlugin.
     *
     * @since 3.0
     */
    public UIPlugin() {
        super();
        inst = this;
    }

    /**
	 * Returns the image registry for this plugin.
	 *
	 * Where are the images? The images (typically png) are found in the same
	 * plugins directory.
	 *
	 * Note: The workbench uses the standard JFace ImageRegistry to track its
	 * images. In addition the class WorkbenchGraphicResources provides
	 * convenience access to the graphics resources and fast field access for
	 * some of the commonly used graphical images.
	 *
	 * @see ImageRegistry
	 */
    @Override
	protected ImageRegistry createImageRegistry() {
        /* Just to be sure that we don't access this
         * plug-ins image registry.
         */
        Assert.isLegal(false);
        return null;
    }

    @Override
	public ImageRegistry getImageRegistry() {
        /* Just to be sure that we don't access this
         * plug-ins image registry.
         */
        Assert.isLegal(false);
        return null;
    }

    /**
     * Returns the default instance of the receiver. This represents the runtime plugin.
     *
     * @return UIPlugin the singleton instance of the receiver.
     * @see AbstractUIPlugin for the typical implementation pattern for plugin classes.
     */
    public static UIPlugin getDefault() {
        return inst;
    }


    @Override
	public void start(BundleContext context) throws Exception {
        super.start(context);

        // set a callback allowing the workbench plugin to obtain
        // and save the UI plugin's preference store
        PrefUtil.setUICallback(new PrefUtil.ICallback() {
            @Override
			public IPreferenceStore getPreferenceStore() {
                return UIPlugin.this.getPreferenceStore();
            }

            @Override
			public void savePreferences() {
                UIPlugin.this.savePluginPreferences();
            }
        });
    }
}

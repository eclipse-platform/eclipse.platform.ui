/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.util.Assert;
import org.eclipse.ui.plugin.AbstractUIPlugin;

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
	 * Create an instance of the UIPlugin.
	 */
	public UIPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		inst = this;
	}
	/**
	 * Returns the image registry for this plugin.
	 *
	 * Where are the images?  The images (typically gifs) are found in the 
	 * same plugins directory.
	 *
	 * @see JFace's ImageRegistry
	 *
	 * Note: The workbench uses the standard JFace ImageRegistry to track its images. In addition 
	 * the class WorkbenchGraphicResources provides convenience access to the graphics resources 
	 * and fast field access for some of the commonly used graphical images.
	 */
	protected ImageRegistry createImageRegistry() {
		/* Just to be sure that we don't access this
		 * plug-ins image registry.
		 */
		Assert.isLegal(false);
		return null;
	}
	
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
	 * @see AbstractPlugin for the typical implementation pattern for plugin classes.
	 */
	public static UIPlugin getDefault() {
		return inst;
	}

	/** 
	 * Set default preference values.
	 * This method must be called whenever the preference store is initially loaded
	 * because the default values are not stored in the preference store.
	 */
	protected void initializeDefaultPreferences(IPreferenceStore store) {
	    // This should not be called.
	    // Prefs are initialized in UIPreferenceInitializer.
	    throw new UnsupportedOperationException();
	}

    /**
     * Initialize the prefs.  Called from UIPreferenceCustomization.
     * 
     * @since 3.0
     */
    void internalInitPrefs() {
        initializeDefaultPreferences(getPreferenceStore());
    }
}

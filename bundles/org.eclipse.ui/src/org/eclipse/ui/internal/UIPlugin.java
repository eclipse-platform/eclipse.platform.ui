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
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * This class represents the TOP of the workbench UI world
 * A plugin class is effectively an application wrapper
 * for a plugin & its classes. This class should be thought
 * of as the workbench UI's application class.
 *
 * This class is responsible for tracking various registries
 * font, preference, graphics, dialog store.
 *
 * This class is explicitly referenced by the 
 * workbench plugin's  "plugin.xml" and places it
 * into the UI start extension point of the main
 * overall application harness
 *
 * When is this class started?
 *      When the Application
 *      calls createExecutableExtension to create an executable
 *      instance of our workbench class.
 */
public class UIPlugin extends AbstractUIPlugin {
	
	private static UIPlugin inst;
	
	/**
	 * Create an instance of the WorkbenchPlugin.
	 * The workbench plugin is effectively the "application" for the workbench UI.
	 * The entire UI operates as a good plugin citizen.
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
		return WorkbenchImages.getImageRegistry();
	}
	
	public ImageRegistry getImageRegistry() {
		/* Just to be sure that we don't access this
		 * plug-ins image registry.
		 */
		Assert.isLegal(false);
		return null;
	}

	/* Return the default instance of the receiver. This represents the runtime plugin.
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
		
		store.setDefault(IWorkbenchPreferenceConstants.DEFAULT_PERSPECTIVE_ID, IWorkbenchConstants.DEFAULT_LAYOUT_ID); 
		store.setDefault(IWorkbenchPreferenceConstants.OPEN_NEW_PERSPECTIVE, IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE);
		store.setDefault(IWorkbenchPreferenceConstants.PROJECT_OPEN_NEW_PERSPECTIVE, IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE);

		//Deprecated but kept for backwards compatibility
		store.setDefault(IWorkbenchPreferenceConstants.SHIFT_OPEN_NEW_PERSPECTIVE, IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE);
		store.setDefault(IWorkbenchPreferenceConstants.ALTERNATE_OPEN_NEW_PERSPECTIVE, IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE);
		
		// Although there is no longer any item on the preference pages 
		// for setting the linking preference, since it is now a per-part setting, 
		// it remains as a preference to allow product overrides of the 
		// initial state of linking in the Navigator.
		// By default, linking is off.
		store.setDefault(IWorkbenchPreferenceConstants.LINK_NAVIGATOR_TO_EDITOR, false);
		
		store.addPropertyChangeListener(new PlatformUIPreferenceListener());
	}

	}

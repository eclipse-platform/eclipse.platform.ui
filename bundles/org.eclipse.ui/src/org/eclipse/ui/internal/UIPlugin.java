package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IPluginDescriptor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.OpenStrategy;

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
		
		JFacePreferences.setPreferenceStore(store);
		store.setDefault(IPreferenceConstants.AUTO_BUILD, true);
		store.setDefault(IPreferenceConstants.SAVE_ALL_BEFORE_BUILD, false);
		store.setDefault(IPreferenceConstants.SAVE_INTERVAL, 5); //5 minutes
		store.setDefault(IPreferenceConstants.WELCOME_DIALOG, true);
		store.setDefault(IPreferenceConstants.REFRESH_WORKSPACE_ON_STARTUP, false);
		store.setDefault(IPreferenceConstants.REUSE_EDITORS_BOOLEAN, false);
		store.setDefault(IPreferenceConstants.REUSE_EDITORS, 8);
		store.setDefault(IPreferenceConstants.OPEN_ON_SINGLE_CLICK, false);
		store.setDefault(IPreferenceConstants.SELECT_ON_HOVER, false);
		store.setDefault(IPreferenceConstants.OPEN_AFTER_DELAY, false);
		store.setDefault(IPreferenceConstants.RECENT_FILES, 4);
		store.setDefault(IPreferenceConstants.VIEW_TAB_POSITION, SWT.BOTTOM);
		store.setDefault(IPreferenceConstants.EDITOR_TAB_POSITION, SWT.TOP);
		store.setDefault(IPreferenceConstants.OPEN_VIEW_MODE, IPreferenceConstants.OVM_EMBED);
		store.setDefault(IPreferenceConstants.OPEN_PERSP_MODE, IPreferenceConstants.OPM_ACTIVE_PAGE);
		store.setDefault(IPreferenceConstants.ENABLED_DECORATORS, ""); //$NON-NLS-1$
		store.setDefault(IWorkbenchPreferenceConstants.DEFAULT_PERSPECTIVE_ID, IWorkbenchConstants.DEFAULT_LAYOUT_ID); 
		
		//Set the default error colour to red
		PreferenceConverter.setDefault(store,JFacePreferences.ERROR_COLOR, new RGB(255, 0, 0));
		//Set the default hyperlink line colour to dark blue
		PreferenceConverter.setDefault(store,JFacePreferences.HYPERLINK_COLOR, new RGB(0, 0, 153));
		//Set the default active hyperlink line colour to blue
		PreferenceConverter.setDefault(store,JFacePreferences.ACTIVE_HYPERLINK_COLOR, new RGB(0, 0, 255));
		
		
		// Temporary option to enable wizard for project capability
		store.setDefault("ENABLE_CONFIGURABLE_PROJECT_WIZARD", false); //$NON-NLS-1$
		// Temporary option to enable single click
		store.setDefault("SINGLE_CLICK_METHOD", OpenStrategy.DOUBLE_CLICK); //$NON-NLS-1$
		// Temporary option to enable cool bars
		store.setDefault("ENABLE_COOL_BARS", true); //$NON-NLS-1$
		// Temporary option to enable new menu organization
		store.setDefault("ENABLE_NEW_MENUS", true); //$NON-NLS-1$	
			

		FontRegistry registry = JFaceResources.getFontRegistry();
		initializeFont(JFaceResources.DIALOG_FONT, registry, store);
		initializeFont(JFaceResources.BANNER_FONT, registry, store);
		initializeFont(JFaceResources.HEADER_FONT, registry, store);
		initializeFont(JFaceResources.TEXT_FONT, registry, store);
		
		// deprecated constants - keep to be backward compatible
		store.setDefault(IWorkbenchPreferenceConstants.OPEN_NEW_PERSPECTIVE, IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE);
		store.setDefault(IWorkbenchPreferenceConstants.SHIFT_OPEN_NEW_PERSPECTIVE, IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE);
		store.setDefault(IWorkbenchPreferenceConstants.ALTERNATE_OPEN_NEW_PERSPECTIVE, IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE);
		store.setDefault(IWorkbenchPreferenceConstants.PROJECT_OPEN_NEW_PERSPECTIVE, IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE);
		store.setDefault(IWorkbenchConstants.ACCELERATOR_CONFIGURATION_ID, IWorkbenchConstants.DEFAULT_ACCELERATOR_CONFIGURATION_ID);
		store.setDefault(IWorkbenchPreferenceConstants.LINK_NAVIGATOR_TO_EDITOR, true);
		
		store.addPropertyChangeListener(new PlatformUIPreferenceListener());
	}

	private void initializeFont(String fontKey, FontRegistry registry, IPreferenceStore store) {
	
		FontData[] fontData = registry.getFontData(fontKey);
		PreferenceConverter.setDefault(store, fontKey, fontData);
	}

	}
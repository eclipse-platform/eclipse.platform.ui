/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.window.WindowManager;
import org.eclipse.ui.AboutInfo;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchPreferences;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;

/**
 * Internal class providing special access for configuring the workbench.
 * <p>
 * Note that these objects are only available to the main application
 * (the plug-in that creates and owns the workbench).
 * </p>
 * <p>
 * This class is not intended to be instantiated or subclassed by clients.
 * </p>
 * 
 * @since 3.0
 */
public final class WorkbenchConfigurer implements IWorkbenchConfigurer {
	
	/**
	 * Table to hold arbitrary key-data settings (key type: <code>String</code>,
	 * value type: <code>Object</code>).
	 * @see #setData
	 */
	private Map extraData = new HashMap();
	
	/**
	 * Indicates whether workbench state should be saved on close and 
	 * restored on subsequence open.
	 */
	private boolean saveAndRestore = false;
	
	/**
	 * The configuration information read from the <code>about.ini</code>
	 * file of the primary feature.
	 */
	private AboutInfo aboutInfo = null;
	
	/**
	 * Creates a new workbench configurer.
	 * <p>
	 * This method is declared package-private. Clients are passed an instance
	 * only via {@link WorkbenchAdviser#initialize WorkbenchAdviser.initialize}
	 * </p>
	 */
	/* package */ WorkbenchConfigurer() {
		super();
	}

	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchConfigurer#getWorkbench
	 */
	public IWorkbench getWorkbench() {
		return PlatformUI.getWorkbench();
	}

	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchConfigurer#getPrimaryFeatureAboutInfo
	 */
	public AboutInfo getPrimaryFeatureAboutInfo() throws WorkbenchException {
		if (aboutInfo == null) {
			readPrimaryFeatureAboutInfo();
		}
		return aboutInfo;
	}
	
	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchConfigurer#getWorkbenchWindowManager
	 */
	public WindowManager getWorkbenchWindowManager() {
		// return the global workbench window manager
		return ((Workbench)getWorkbench()).getWindowManager();
	}	
	
	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchConfigurer#getWorkbenchImageRegistry
	 */
	public ImageRegistry getWorkbenchImageRegistry() {
		// return the global workbench image registry
		return WorkbenchPlugin.getDefault().getImageRegistry();
	}	

	/* (non-javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchConfigurer#getWindowConfigurer
	 */
	public IWorkbenchWindowConfigurer getWindowConfigurer(IWorkbenchWindow window) {
		if (window == null) {
			throw new IllegalArgumentException();
		}
		return ((WorkbenchWindow) window).getWindowConfigurer();
	}


	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchConfigurer#getSaveAndRestore()
	 */
	public boolean getSaveAndRestore() {
		return saveAndRestore;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchConfigurer#setSaveAndRestore(boolean)
	 */
	public void setSaveAndRestore(boolean enabled) {
		saveAndRestore = enabled;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchConfigurer#getData
	 */
	public Object getData(String key) {
		if (key == null) {
			throw new IllegalArgumentException();
		}
		return extraData.get(key);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchConfigurer#setData(String, Object)
	 */
	public void setData(String key, Object data) {
		if (key == null) {
			throw new IllegalArgumentException();
		}
		if (data != null) {
			extraData.put(key, data);
		} else {
			extraData.remove(key);
		}
	}
	
	/**
	 * Allows the configurer to initialize its state that
	 * depends on a Display existing.
	 */
	/* package */ void init() {
		saveAndRestore = WorkbenchPlugin.getDefault().getPreferenceStore().getBoolean(IWorkbenchPreferences.SHOULD_SAVE_WORKBENCH_STATE);
	}
	
	/*
	 * Reads the about.ini file for the primary feature.
	 */
	private void readPrimaryFeatureAboutInfo() throws WorkbenchException {
		if (aboutInfo != null)
			return;
			
		// determine the identifier of the primary feature (application)
		IPlatformConfiguration conf = BootLoader.getCurrentPlatformConfiguration();
		String versionedFeatureId = conf.getPrimaryFeatureIdentifier();
		IPlatformConfiguration.IFeatureEntry primaryFeature = conf.findConfiguredFeatureEntry(versionedFeatureId);

		if (primaryFeature == null) {
			aboutInfo = AboutInfo.create(null, null);
		} else {
			String versionedFeaturePluginIdentifier = primaryFeature.getFeaturePluginIdentifier();
			String versionedFeaturePluginVersion = primaryFeature.getFeaturePluginVersion();

			if (versionedFeaturePluginIdentifier == null) {
				aboutInfo = AboutInfo.create(null, null);
			} else {
				if (versionedFeaturePluginVersion == null) {
					aboutInfo = AboutInfo.create(versionedFeaturePluginIdentifier, null);
				} else {
					PluginVersionIdentifier mainPluginVersion = null;
					try {
						mainPluginVersion = new PluginVersionIdentifier(versionedFeaturePluginVersion);
					} catch (Exception e) {
						IStatus iniStatus = new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, "Unknown plugin version: " + versionedFeatureId, e); //$NON-NLS-1$
						WorkbenchPlugin.log("Problem obtaining primary feature configuration info", iniStatus); //$NON-NLS-1$
					}
					aboutInfo = AboutInfo.create(versionedFeaturePluginIdentifier, mainPluginVersion);
				}
			}
		}
	}
}

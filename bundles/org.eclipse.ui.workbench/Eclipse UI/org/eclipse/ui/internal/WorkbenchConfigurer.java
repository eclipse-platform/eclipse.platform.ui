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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
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
	 * The dialog setting key to access the known installed features
	 * since the last time the workbench was run.
	 */
	private static final String INSTALLED_FEATURES = "installedFeatures"; //$NON-NLS-1$
	
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
	 * The configuration information read from the <code>about.ini</code>
	 * file of all installed features.
	 */
	private AboutInfo[] allFeaturesAboutInfo = null;
	
	/**
	 * The configuration information read from the <code>about.ini</code>
	 * file of newly installed features (since last time workbench was run).
	 */
	private AboutInfo[] newFeaturesAboutInfo = null;
	
	/**
	 * Indicates whether the workbench is being force to close. During
	 * an emergency close, no interaction with the user should be done.
	 */
	private boolean isEmergencyClosing = false;
	
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

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchConfigurer#getAllFeaturesAboutInfo()
	 */
	public AboutInfo[] getAllFeaturesAboutInfo() throws WorkbenchException {
		if (allFeaturesAboutInfo == null) {
			readFeaturesAboutInfo();
		}
		return allFeaturesAboutInfo;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchConfigurer#getNewFeaturesAboutInfo()
	 */
	public AboutInfo[] getNewFeaturesAboutInfo() throws WorkbenchException {
		if (newFeaturesAboutInfo == null) {
			readFeaturesAboutInfo();
		}
		return newFeaturesAboutInfo;
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

	/*
	 * Reads the about.ini file for all installed features. Keeps track
	 * of new features installed since the last time the workbench was run.
	 */
	private void readFeaturesAboutInfo() throws WorkbenchException {
		if (allFeaturesAboutInfo != null || newFeaturesAboutInfo != null) {
			return;
		}
		
		// get the list of known installed feature the last time the workbench was run	
		IDialogSettings settings = WorkbenchPlugin.getDefault().getDialogSettings();
		String[] oldFeaturesArray = settings.getArray(INSTALLED_FEATURES);
		List oldFeatures = null;
		if (oldFeaturesArray != null) {
			oldFeatures = Arrays.asList(oldFeaturesArray);
		}

		// get the list of currently installed features
		IPlatformConfiguration platformConfiguration = BootLoader.getCurrentPlatformConfiguration();
		IPlatformConfiguration.IFeatureEntry[] features = platformConfiguration.getConfiguredFeatureEntries();

		String[] idArray = new String[features.length];
		ArrayList allAboutInfos = new ArrayList(features.length);
		ArrayList newAboutInfos = new ArrayList(features.length);

		// get the about.ini info for each feature
		for (int i = 0; i < features.length; i++) {
			String id = features[i].getFeatureIdentifier();
			String version = features[i].getFeatureVersion();
			PluginVersionIdentifier vid = null;
			if (version != null) {
				vid = new PluginVersionIdentifier(version);
			}			

			String versionedId = id + ":" + vid; //$NON-NLS-1$
			idArray[i] = versionedId;

			AboutInfo info = AboutInfo.create(id, vid);
			allAboutInfos.add(info);
			if (oldFeatures != null && !oldFeatures.contains(versionedId)) {
				newAboutInfos.add(info);
			}
		}
		
		// store the list of installed features for next time the workbench is run
		settings.put(INSTALLED_FEATURES, idArray);

		// ensure a consistent ordering
		Collections.sort(allAboutInfos, new Comparator() {
			Collator coll = Collator.getInstance();
			public int compare(Object a, Object b) {
				AboutInfo infoA = (AboutInfo) a;
				AboutInfo infoB = (AboutInfo) b;
				int c = coll.compare(infoA.getFeatureId(), infoB.getFeatureId());
				if (c == 0) {
					c = infoA.getVersionId().isGreaterThan(infoB.getVersionId()) ? 1 : -1;
				}
				return c;
			}
		});

		// exclude features for which there is no corresponding plug-in
		allFeaturesAboutInfo = new AboutInfo[allAboutInfos.size()];
		allAboutInfos.toArray(allFeaturesAboutInfo);

		for (int i = 0; i < allFeaturesAboutInfo.length; i++) {
			if (allFeaturesAboutInfo[i].getPluginDescriptor() == null) {
				allAboutInfos.remove(allFeaturesAboutInfo[i]);
				newAboutInfos.remove(allFeaturesAboutInfo[i]);
			}
		}

		// hold onto the results 
		newFeaturesAboutInfo = new AboutInfo[newAboutInfos.size()];
		newAboutInfos.toArray(newFeaturesAboutInfo);
		if (allAboutInfos.size() < allFeaturesAboutInfo.length) {
			allFeaturesAboutInfo = new AboutInfo[allAboutInfos.size()];
			allAboutInfos.toArray(allFeaturesAboutInfo);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchConfigurer#emergencyClose()
	 */
	public void emergencyClose() {
		if (!isEmergencyClosing) {
			isEmergencyClosing = true;
			if (Workbench.getInstance() != null && !Workbench.getInstance().isClosing()) {
				Workbench.getInstance().close(PlatformUI.RETURN_EMERGENCY_CLOSE, true);
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.IWorkbenchConfigurer#emergencyClosing()
	 */
	public boolean emergencyClosing() {
		return isEmergencyClosing;
	}
}

/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM - Initial implementation
************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.dialogs.IDialogSettings;
import java.util.List;
import java.util.Collections;
import java.lang.String;
import org.eclipse.ui.PlatformUI;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.internal.dialogs.WelcomeEditorInput;
import org.eclipse.core.resources.IContainer;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.core.runtime.IPluginDescriptor;
import java.util.ArrayList;
import org.eclipse.ui.WorkbenchException;
import java.lang.Exception;
import java.text.Collator;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IEditorInput;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.ui.PartInitException;
import java.util.Comparator;
import java.lang.RuntimeException;
import java.util.Arrays;
import org.eclipse.ui.IEditorPart;
import org.eclipse.core.boot.BootLoader;
import java.net.URL;
import org.eclipse.core.boot.IPlatformConfiguration;
import java.lang.Object;

public class WorkbenchConfigurationInfo {
	private AboutInfo aboutInfo;
	private AboutInfo[] featuresInfo;
	private AboutInfo[] newFeaturesInfo;
	
	private static final String INSTALLED_FEATURES = "installedFeatures";


/**
 * Returns the about info.
 *
 * @return the about info
 */
public AboutInfo getAboutInfo() {
	return aboutInfo;
}

/**
 * Returns the about info for all configured features with a corresponding plugin.
 *
 * @return the about info
 */
public AboutInfo[] getFeaturesInfo() {
	if (featuresInfo == null)
		readFeaturesInfo();
	return featuresInfo;
}

/**
 * Returns the about info for all new (since the last time the workbench was run)
 * configured features with a corresponding plugin.
 *
 * @return the about info
 */
private AboutInfo[] getNewFeaturesInfo() {
	if (newFeaturesInfo == null)
		readFeaturesInfo();
	return newFeaturesInfo;
}

private Workbench getWorkbench() {
	return (Workbench)PlatformUI.getWorkbench();
}
/**
 * Open the Welcome editor for the primary feature or for a new feature
 */
protected void openWelcomeEditors() {
	AboutInfo info = getAboutInfo();
	AboutInfo[] newFeatures = getNewFeaturesInfo();

	if (WorkbenchPlugin.getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.WELCOME_DIALOG)) {
		// Show the quick start wizard the first time the workbench opens.

		// See if a welcome page is specified
		URL url = info.getWelcomePageURL();
		if (url == null)
			return;

		// Don't show it again
		WorkbenchPlugin.getDefault().getPreferenceStore().setValue(IPreferenceConstants.WELCOME_DIALOG, false);

		openEditor(new WelcomeEditorInput(info), Workbench.WELCOME_EDITOR_ID, null);
	} else {
		// Show the welcome page for any newly installed features

		// Get the infos with welcome pages
		ArrayList welcomeFeatures = new ArrayList();
		for (int i = 0; i < newFeatures.length; i++) {
			if (newFeatures[i].getWelcomePageURL() != null) {
				if (newFeatures[i].getFeatureId() != null && newFeatures[i].getWelcomePerspective() != null) {
					IPluginDescriptor desc = newFeatures[i].getDescriptor();
					//activates the feature plugin so it can run some install code.
					try {
						if(desc != null)
							desc.getPlugin();
					} catch (CoreException e) {
					}
				}
				welcomeFeatures.add(newFeatures[i]);
			}
		}

		int wCount = getWorkbench().getWorkbenchWindowCount();
		for (int i = 0; i < welcomeFeatures.size(); i++) {
			AboutInfo newInfo = (AboutInfo) welcomeFeatures.get(i);
			String id = newInfo.getWelcomePerspective();
			if (id == null || i >= wCount) //Other editors were already opened in restoreState(..)
				openEditor(new WelcomeEditorInput(newInfo), Workbench.WELCOME_EDITOR_ID, id);
		}
	}
}
	
protected boolean readInfo() {
	// determine the identifier of the "dominant" application
	IPlatformConfiguration conf = BootLoader.getCurrentPlatformConfiguration();
	String versionedFeatureId = conf.getPrimaryFeatureIdentifier();
	IPlatformConfiguration.IFeatureEntry primaryFeature = conf.findConfiguredFeatureEntry(versionedFeatureId);

	if (primaryFeature == null) {
		aboutInfo = new AboutInfo(null, null); // Ok to pass null
	} else {
		String versionedFeaturePluginIdentifier = primaryFeature.getFeaturePluginIdentifier();
		String versionedFeaturePluginVersion = primaryFeature.getFeaturePluginVersion();

		if (versionedFeaturePluginIdentifier == null) {
			aboutInfo = new AboutInfo(null, null); // Ok to pass null
		} else {
			if (versionedFeaturePluginVersion == null) {
				aboutInfo = new AboutInfo(versionedFeaturePluginIdentifier, null);
			} else {
				PluginVersionIdentifier mainPluginVersion = null;
				try {
					mainPluginVersion = new PluginVersionIdentifier(versionedFeaturePluginVersion);
				} catch (Exception e) {
					IStatus iniStatus = new Status(IStatus.ERROR, WorkbenchPlugin.getDefault().getDescriptor().getUniqueIdentifier(), 0, "Unknown plugin version " + versionedFeatureId, e); //$NON-NLS-1$
					WorkbenchPlugin.log("Problem obtaining configuration info ", iniStatus); //$NON-NLS-1$
				}
				aboutInfo = new AboutInfo(versionedFeaturePluginIdentifier, mainPluginVersion);
			}
		}
	}

	boolean success = true;

	try {
		aboutInfo.readINIFile();
	} catch (CoreException e) {
		WorkbenchPlugin.log("Error reading about info file", e.getStatus()); //$NON-NLS-1$
		success = false;
	}

	return success;
}

/**
 * Reads the about info for all the configured features.
 */
private void readFeaturesInfo() {

	IDialogSettings settings = WorkbenchPlugin.getDefault().getDialogSettings();

	String[] oldFeaturesArray = settings.getArray(INSTALLED_FEATURES);
	List oldFeatures = null;
	if (oldFeaturesArray != null)
		oldFeatures = Arrays.asList(oldFeaturesArray);

	ArrayList aboutInfos = new ArrayList();
	ArrayList newAboutInfos = new ArrayList();


	IPlatformConfiguration platformConfiguration = BootLoader.getCurrentPlatformConfiguration();
	IPlatformConfiguration.IFeatureEntry[] features = platformConfiguration.getConfiguredFeatureEntries();

	String[] idArray = new String[features.length];
	for (int i = 0; i < features.length; i++) {
		String id = features[i].getFeatureIdentifier();
		String version = features[i].getFeatureVersion();
		PluginVersionIdentifier vid = null;
		if (version != null)
			vid = new PluginVersionIdentifier(version);			
		
		String versionedId = id + ":" + vid;
		idArray[i] = versionedId;

		try {
			AboutInfo info = new AboutInfo(id, vid);
			aboutInfos.add(info);
			if (oldFeatures != null && !oldFeatures.contains(versionedId))
				// only report a feature as new if we have a previous record of old features
				newAboutInfos.add(info);
		} catch (RuntimeException e) {
			if (WorkbenchPlugin.DEBUG) // only report ini problems if the -debug command line argument is used
				WorkbenchPlugin.log("Error parsing version \"" + vid + "\" for plugin: " + id + " in Workbench.readFeaturesInfo()");
			// continue
		}
	}
	settings.put(INSTALLED_FEATURES, idArray);

	// ensure a consistent ordering
	Collections.sort(aboutInfos, new Comparator() {
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

	featuresInfo = new AboutInfo[aboutInfos.size()];
	aboutInfos.toArray(featuresInfo);

	for (int i = 0; i < featuresInfo.length; i++) {
		try {
			featuresInfo[i].readINIFile();
			// Exclude any feature for which there is no corresponding plug-in
			if (featuresInfo[i].getDescriptor() == null) {
				aboutInfos.remove(featuresInfo[i]);
				newAboutInfos.remove(featuresInfo[i]);
			}
		} catch (CoreException e) {
			if (WorkbenchPlugin.DEBUG) // only report ini problems if the -debug command line argument is used
				WorkbenchPlugin.log("Error reading about info file for feature: " + featuresInfo[i].getFeatureId(), e.getStatus()); //$NON-NLS-1$
		}
	}

	newFeaturesInfo = new AboutInfo[newAboutInfos.size()];
	newAboutInfos.toArray(newFeaturesInfo);
	if (aboutInfos.size() < featuresInfo.length) {
		featuresInfo = new AboutInfo[aboutInfos.size()];
		aboutInfos.toArray(featuresInfo);
	}
}
/**
 * Return an array with all new welcome perspectives declared in the
 * new installed features.
 */
protected AboutInfo[] collectNewFeaturesWithPerspectives() {
	ArrayList result = new ArrayList();
	AboutInfo newFeatures[] = getNewFeaturesInfo();
	for (int i = 0; i < newFeatures.length; i++) {
		AboutInfo info = newFeatures[i];
		if (info.getWelcomePerspective() != null && info.getWelcomePageURL() != null)
			result.add(info);
	}
	return (AboutInfo[]) result.toArray(new AboutInfo[result.size()]);
}
/**
 * Open an editor for the given input
 */
private void openEditor(IEditorInput input, String editorId, String perspectiveId) {
	if (getWorkbench().getWorkbenchWindowCount() == 0) {
		// Something is wrong, there should be at least
		// one workbench window open by now.
		return;
	}

	IWorkbenchWindow win = null;
	if (perspectiveId == null) {
		win = getWorkbench().getActiveWorkbenchWindow();
	} else {
		IContainer root = WorkbenchPlugin.getPluginWorkspace().getRoot();
		try {
			win = getWorkbench().openWorkbenchWindow(perspectiveId, root);
		} catch (WorkbenchException e) {
			if (WorkbenchPlugin.DEBUG) // only report ini problems if the -debug command line argument is used
				WorkbenchPlugin.log("Error opening window in Workbench.openEditor(..)");
			return;
		}
	}

	if (win == null)
		win = getWorkbench().getWorkbenchWindows()[0];

	WorkbenchPage page = (WorkbenchPage) win.getActivePage();
	String id = perspectiveId;
	if (id == null)
		id = WorkbenchPlugin.getDefault().getPerspectiveRegistry().getDefaultPerspective();

	if (page == null) {
		// Create the page.
		try {
			IContainer root = WorkbenchPlugin.getPluginWorkspace().getRoot();
			page = (WorkbenchPage) getWorkbench().getActiveWorkbenchWindow().openPage(id, root);
		} catch (WorkbenchException e) {
			ErrorDialog.openError(
				win.getShell(), 
				WorkbenchMessages.getString("Problems_Opening_Page"), //$NON-NLS-1$
				e.getMessage(),
				e.getStatus());
		}
	}

	if (page == null)
		return;

	if (page.getActivePerspective() == null) {
		try {
			page = (WorkbenchPage)getWorkbench().showPerspective(id, win);
		} catch (WorkbenchException e) {
			IStatus status = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, 1, WorkbenchMessages.getString("QuickStartAction.openEditorException"), e); //$NON-NLS-1$
			ErrorDialog.openError(
				win.getShell(),
				WorkbenchMessages.getString("Workbench.openEditorErrorDialogTitle"),  //$NON-NLS-1$
			WorkbenchMessages.getString("Workbench.openEditorErrorDialogMessage"), //$NON-NLS-1$
			status);
			return;
		}
	}

	page.setEditorAreaVisible(true);

	// see if we already have an editor
	IEditorPart editor = page.findEditor(input);
	if (editor != null) {
		page.activate(editor);
		return;
	}

	try {
		page.openEditor(input, editorId);
	} catch (PartInitException e) {
		IStatus status = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, 1, WorkbenchMessages.getString("QuickStartAction.openEditorException"), e); //$NON-NLS-1$
		ErrorDialog.openError(
			win.getShell(),
			WorkbenchMessages.getString("Workbench.openEditorErrorDialogTitle"),  //$NON-NLS-1$
		WorkbenchMessages.getString("Workbench.openEditorErrorDialogMessage"), //$NON-NLS-1$
		status);
	}
	return;
}
/**
 * Open the system summary editor
 */
public void openSystemSummaryEditor() {
	openEditor(new SystemSummaryEditorInput(), PlatformUI.PLUGIN_ID + ".SystemSummaryEditor", null); //$NON-NLS-1$
}					
}
package org.eclipse.ui.plugin;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;

import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Abstract base class for plug-ins that integrate with the Eclipse platform UI.
 * <p>
 * Subclasses obtain the following capabilities:
 * </p>
 * <p>
 * Preferences
 * <ul>
 * <li> Preferences are read the first time <code>getPreferenceStore</code> is
 *      called. </li>
 * <li> Preferences are found in the file whose name is given by the constant
 *      <code>FN_PREF_STORE</code>. A preference file is looked for in the plug-in's 
 *		read/write state area.</li>
 * <li> Subclasses should reimplement <code>initializeDefaultPreferences</code>
 *      to set up any default values for preferences. These are the values 
 *      typically used if the user presses the Default button in a preference
 *      dialog. </li>
 * <li>	The plug-in's install directory is checked for a file whose name is given by 
 *		<code>FN_DEFAULT_PREFERENCES</code>.
 *      This allows a plug-in to ship with a read-only copy of a preference file 
 *      containing default values for certain settings different from the 
 *      hard-wired default ones (perhaps as a result of localizing, or for a
 *      common configuration).</li>
 * <li> Plug-in code can call <code>savePreferenceStore</code> to cause 
 *      non-default settings to be saved back to the file in the plug-in's
 *      read/write state area. </li>
 * <li> Preferences are also saved automatically on plug-in shutdown.</li>
 * </ul>
 * Dialogs
 * <ul>
 * <li> Dialog store are read the first time <code>getDialogSettings</code> is 
 *      called.</li>
 * <li> The dialog store allows the plug-in to "record" important choices made
 *      by the user in a wizard or dialog, so that the next time the
 *      wizard/dialog is used the widgets can be defaulted to better values. A
 *      wizard could also use it to record the last 5 values a user entered into
 *      an editable combo - to show "recent values". </li>
 * <li> The dialog store is found in the file whose name is given by the
 *      constant <code>FN_DIALOG_STORE</code>. A dialog store file is first
 *      looked for in the plug-in's read/write state area; if not found there,
 *      the plug-in's install directory is checked.
 *      This allows a plug-in to ship with a read-only copy of a dialog store
 *      file containing initial values for certain settings.</li>
 * <li> Plug-in code can call <code>saveDialogSettings</code> to cause settings to
 *      be saved in the plug-in's read/write state area. A plug-in may opt to do
 *      this each time a wizard or dialog is closed to ensure the latest 
 *      information is always safe on disk. </li>
 * <li> Dialog settings are also saved automatically on plug-in shutdown.</li>
 * </ul>
 * Images
 * <ul>
 * <li> A typical UI plug-in will have some images that are used very frequently
 *      and so need to be cached and shared.  The plug-in's image registry 
 *      provides a central place for a plug-in to store its common images. 
 *      Images managed by the registry are created lazily as needed, and will be
 *      automatically disposed of when the plug-in shuts down. Note that the
 *      number of registry images should be kept to a minimum since many OSs
 *      have severe limits on the number of images that can be in memory at once.
 * </ul>
 * <p>
 * For easy access to your plug-in object, use the singleton pattern. Declare a
 * static variable in your plug-in class for the singleton. Store the first
 * (and only) instance of the plug-in class in the singleton when it is created.
 * Then access the singleton when needed through a static <code>getDefault</code>
 * method.
 * </p>
 */
public abstract class AbstractUIPlugin extends Plugin
{
	/**
	 * The name of the preference storage file (value
	 * <code>"pref_store.ini"</code>).
	 */
	private static final String FN_PREF_STORE= "pref_store.ini";//$NON-NLS-1$
	/**
	 * The name of the default preference settings file (value
	 * <code>"preferences.ini"</code>).
	 */
	private static final String FN_DEFAULT_PREFERENCES= "preferences.ini";//$NON-NLS-1$

	/**
	 * The name of the dialog settings file (value 
	 * <code>"dialog_settings.xml"</code>).
	 */
	private static final String FN_DIALOG_SETTINGS = "dialog_settings.xml";//$NON-NLS-1$

	
	/**
	 * Storage for dialog and wizard data; <code>null</code> if not yet
	 * initialized.
	 */
	private DialogSettings dialogSettings = null;

	/**
	 * Storage for preferences; <code>null</code> if not yet initialized.
	 */
	private PreferenceStore preferenceStore = null;

	/**
	 * The registry for all graphic images; <code>null</code> if not yet
	 * initialized.
	 */
	private ImageRegistry imageRegistry = null;
/**
 * Creates an abstract UI plug-in runtime object for the given plug-in descriptor.
 * <p>
 * Note that instances of plug-in runtime classes are automatically created 
 * by the platform in the course of plug-in activation.
 * </p>
 *
 * @param descriptor the plug-in descriptor
 */
public AbstractUIPlugin(IPluginDescriptor descriptor) {
	super(descriptor);
}
/** 
 * Returns a new image registry for this plugin-in.  The registry will be
 * used to manage images which are frequently used by the plugin-in.
 * <p>
 * The default implementation of this method creates an empty registry.
 * Subclasses may override this method if needed.
 * </p>
 *
 * @return ImageRegistry the resulting registry.
 * @see #getImageRegistry
 */
protected ImageRegistry createImageRegistry() {
	return new ImageRegistry();
}
/**
 * Returns the dialog settings for this UI plug-in.
 * The dialog settings is used to hold persistent state data for the various
 * wizards and dialogs of this plug-in in the context of a workbench. 
 * <p>
 * If an error occurs reading the dialog store, an empty one is quietly created
 * and returned.
 * </p>
 * <p>
 * Subclasses may override this method but are not expected to.
 * </p>
 *
 * @return the dialog settings
 */
public IDialogSettings getDialogSettings() {
	if (dialogSettings == null)
		loadDialogSettings();
	return dialogSettings;
}
/**
 * Returns the image registry for this UI plug-in. 
 * <p>
 * The image registry contains the images used by this plug-in that are very 
 * frequently used and so need to be globally shared within the plug-in. Since 
 * many OSs have a severe limit on the number of images that can be in memory at 
 * any given time, a plug-in should only keep a small number of images in their 
 * registry.
 * <p>
 * Subclasses should reimplement <code>initializeImageRegistry</code> if they have
 * custom graphic images to load.
 * </p>
 * <p>
 * Subclasses may override this method but are not expected to.
 * </p>
 *
 * @return the image registry
 */
public ImageRegistry getImageRegistry() {
	if (imageRegistry == null) {
		imageRegistry = createImageRegistry();
		initializeImageRegistry(imageRegistry);
	}
	return imageRegistry;
}
/**
 * Returns the preference store for this UI plug-in.
 * This preference store is used to hold persistent settings for this plug-in in
 * the context of a workbench. Some of these settings will be user controlled, 
 * whereas others may be internal setting that are never exposed to the user.
 * <p>
 * If an error occurs reading the preference store, an empty preference store is
 * quietly created, initialized with defaults, and returned.
 * </p>
 * <p>
 * Subclasses should reimplement <code>initializeDefaultPreferences</code> if
 * they have custom graphic images to load.
 * </p>
 *
 * @return the preference store 
 */
public IPreferenceStore getPreferenceStore() {
	if (preferenceStore == null) {
		loadPreferenceStore();
		initializeDefaultPreferences(preferenceStore);
		initializePluginPreferences(preferenceStore);
		initializeConfigurationPreferences(preferenceStore);
	}
	return preferenceStore;
}
/**
 * Returns the Platform UI workbench.  
 * <p> 
 * This method exists as a convenience for plugin implementors.  The
 * workbench can also be accessed by invoking <code>PlatformUI.getWorkbench()</code>.
 * </p>
 */
public IWorkbench getWorkbench() {
	return PlatformUI.getWorkbench();
}
/**
 * Sets default preferences based on the current configuration
 */
private void initializeConfigurationPreferences(IPreferenceStore store) {
	Hashtable table= ((Workbench)getWorkbench()).getProductInfo().getConfigurationPreferences();
	if (table == null) return;
	Object preferences= table.get(getDescriptor().getUniqueIdentifier());
	if (preferences == null) return;
	String[] preferenceArray= (String[]) preferences;
	for (int i= 0; i < preferenceArray.length; i+=2) {
		String name= preferenceArray[i];
		String value= preferenceArray[i+1];
		store.setDefault(name, value);
	}

}
/** 
 * Initializes a preference store with default preference values 
 * for this plug-in.
 * <p>
 * This method is called after the preference store is initially loaded
 * (default values are never stored in preference stores).
 * <p><p>
 * The default implementation of this method does nothing.
 * Subclasses should reimplement this method if the plug-in has any preferences.
 * </p>
 *
 * @param store the preference store to fill
 */
protected void initializeDefaultPreferences(IPreferenceStore store) {
}
/** 
 * Initializes an image registry with images which are frequently used by the 
 * plugin-in.
 * <p>
 * The image registry contains the images used by this plug-in that are very
 * frequently used and so need to be globally shared within the plug-in. Since
 * many OSs have a severe limit on the number of images that can be in memory
 * at any given time, each plug-in should only keep a small number of images in 
 * its registry.
 * </p><p>
 * Implementors should create a JFace image descriptor for each frequently used
 * image.  The descriptors describe how to create/find the image should it be needed. 
 * The image described by the descriptor is not actually allocated until someone 
 * retrieves it.
 * </p><p>
 * Subclasses may override this method to fill the image registry.
 * </p>
 *
 * @return ImageRegistry the resulting registry.
 * @see #getImageRegistry
 */
protected void initializeImageRegistry(ImageRegistry reg) {
}
/**
 * Sets default preferences defined in the plugin directory.
 * If there are no default preferences defined, or some other
 * problem occurs, we fail silently.
 */
private void initializePluginPreferences(IPreferenceStore store) {
	URL baseURL = getDescriptor().getInstallURL();

	URL iniURL= null;
	try {
		iniURL = new URL(baseURL, FN_DEFAULT_PREFERENCES);
	} catch (MalformedURLException e) {
		return;
	}

	Properties ini = new Properties();
	InputStream is = null;
	try {
		is = iniURL.openStream();
		ini.load(is);
	}
	catch (IOException e) {
		// Cannot read ini file;
		return;
	}
	finally {
		try { 
			if (is != null)
				is.close(); 
		} catch (IOException e) {}
	}

	Enumeration enum = ini.propertyNames();
	while (enum.hasMoreElements()) {
		String key = (String)enum.nextElement();
		store.setDefault(key, ini.getProperty(key));
	}
}
/**
 * Loads the dialog settings for this plug-in.
 * The default implementation first looks for a standard named file in the 
 * plug-in's read/write state area; if no such file exists, the plug-in's
 * install directory is checked to see if one was installed with some default
 * settings; if no file is found in either place, a new empty dialog settings
 * is created. If a problem occurs, an empty settings is silently used.
 * <p>
 * This framework method may be overridden, although this is typically
 * unnecessary.
 * </p>
 */
protected void loadDialogSettings() {
	dialogSettings = new DialogSettings("Workbench"); //$NON-NLS-1$

	// try r/w state area in the local file system
	String readWritePath =
		getStateLocation().append(FN_DIALOG_SETTINGS).toOSString();
	File settingsFile = new File(readWritePath);
	if (settingsFile.exists()) {
		try {
			dialogSettings.load(readWritePath);
		} catch (IOException e) {
			// load failed so ensure we have an empty settings
			dialogSettings = new DialogSettings("Workbench"); //$NON-NLS-1$
		}
	} else {
		// not found - use installed  defaults if available
		URL baseURL = getDescriptor().getInstallURL();

		URL dsURL = null;
		try {
			dsURL = new URL(baseURL, FN_DIALOG_SETTINGS);
		} catch (MalformedURLException e) {
			return;
		}
		InputStream is = null;
		try {
			is = dsURL.openStream();
			InputStreamReader reader = new InputStreamReader(is, "utf-8");
			dialogSettings.load(reader);
		} catch (IOException e) {
			// load failed so ensure we have an empty settings
			dialogSettings = new DialogSettings("Workbench");  //$NON-NLS-1$
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
			}
		}
	}
}
/**
 * Loads the preference store for this plug-in.
 * The default implementation looks for a standard named file in the 
 * plug-in's read/write state area. If no file is found or a problem
 * occurs, a new empty preference store is silently created. 
 * <p>
 * This framework method may be overridden, although this is typically 
 * unnecessary.
 * </p>
 */
protected void loadPreferenceStore() {
	String readWritePath = getStateLocation().append(FN_PREF_STORE).toOSString();
	preferenceStore = new PreferenceStore(readWritePath);
	try {
		preferenceStore.load();
	}
	catch (IOException e) {
		// Load failed, perhaps because the file does not yet exist.
		// At any rate we just return and leave the store empty.
	}
	return;
}
/**
 * Refreshes the actions for the plugin.
 * This method is called from <code>startup</code>.
 * <p>
 * This framework method may be overridden, although this is typically 
 * unnecessary.
 * </p>
 */
protected void refreshPluginActions() {
	final Workbench wb = (Workbench)PlatformUI.getWorkbench();
	if (wb != null) {
		// startup() is not guaranteed to be called in the UI thread,
		// but refreshPluginActions must run in the UI thread, 
		// so use asyncExec.  See bug 6623 for more details.
		Display.getDefault().asyncExec(
			new Runnable() {
				public void run() {
					wb.refreshPluginActions(getDescriptor().getUniqueIdentifier());
				}
			}
		);
	}
}	
/**
 * Saves this plug-in's dialog settings.
 * Any problems which arise are silently ignored.
 */
protected void saveDialogSettings() {
	if (dialogSettings == null) {
		return;
	}
	
	try {
		String readWritePath = getStateLocation().append(FN_DIALOG_SETTINGS).toOSString();
		dialogSettings.save(readWritePath);
	}
	catch (IOException e) {
	}
}
/**
 * Saves this plug-in's preference store.
 * Any problems which arise are silently ignored.
 */
protected void savePreferenceStore() {
	if (preferenceStore == null) {
		return;
	}
	try {
		if (preferenceStore.needsSaving())
			preferenceStore.save(); // the store knows its filename - no need to pass it
	}
	catch (IOException e) {
	}
}

/**
 * The <code>AbstractUIPlugin</code> implementation of this <code>Plugin</code>
 * method refreshes the plug-in actions.  Subclasses may extend this method,
 * but must send super first.
 * <p>
 * WARNING: Plug-ins may not be started in the UI thread.
 * The <code>startup()</code> method should not assume that its code runs in
 * the UI thread, otherwise SWT thread exceptions may occur on startup.
 */
public void startup() throws CoreException {
	refreshPluginActions();
}
/**
 * The <code>AbstractUIPlugin</code> implementation of this <code>Plugin</code>
 * method saves this plug-in's preference and dialog stores and shuts down 
 * its image registry (if they are in use). Subclasses may extend this method,
 * but must send super first.
 */
public void shutdown() throws CoreException {
	super.shutdown();
	saveDialogSettings();
	savePreferenceStore();
	preferenceStore = null;
	imageRegistry = null;
}
}

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
package org.eclipse.core.runtime;

import java.io.*;

import org.eclipse.core.internal.runtime.*;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public abstract class BundleHelper {
	private BundleContext context;
	private Preferences preferences;
	/* package */
	static final String PREFERENCES_FILE_NAME = "pref_store.ini"; //$NON-NLS-1$
	private boolean debug;

	public BundleHelper(BundleContext context) {
		this.context = context;
		// sets the debug flag
		String key = context.getBundle().getGlobalName() + "/debug"; //$NON-NLS-1$
		String value = InternalPlatform.getDefault().getOption(key);
		this.debug = value == null ? false : value.equalsIgnoreCase("true"); //$NON-NLS-1$		
		
	}
	public BundleContext getBundleContext() {
		return context;
	}
	public ILog getLog() {
		return InternalPlatform.getDefault().getLog(context.getBundle());
	}

	/**
	* Returns the preference store for this plug - in.* < p > * Note that if an error occurs reading the preference store from disk,
		an empty * preference store is quietly created,
		initialized with defaults,
		and returned.*
			< / p
			> *
			< p
			> * Calling this method may cause the preference store to be created and * initialized.Subclasses which reimplement the *
			< code
			> initializeDefaultPluginPreferences
			< / code
			> method have this opportunity * to initialize preference default values,
		just prior to processing override * default values imposed externally to this plug - in(specified for the product, * or at platform start up).*
			< / p
			> *
			< p
			> * After settings in the preference store are changed(for example, with * < code > Preferences.setValue < / code > or < code > setToDefault < / code >),
		*
			< code
			> savePluginPreferences
			< / code
			> should be called to store the changed * values back to disk.Otherwise the changes will be lost on plug - in * shutdown.*
			< / p
			> * 
			* @ return the preference store
			* @ see # savePluginPreferences
			* @ see Preferences # setValue
			* @ see Preferences # setToDefault
			* @ since 2.0
			* 
			/ 
			 * 
			 * @return
			 */
	public final Preferences getPluginPreferences() {
		if (preferences != null) {
			//			if (InternalPlatform.DEBUG_PREFERENCES) {
			//				System.out.println("Plugin preferences already loaded for " + getDescriptor().getUniqueIdentifier()); //$NON-NLS-1$
			//			} // N.B. preferences instance field set means already created
			//			// and initialized (or in process of being initialized)
			return preferences;
		}

		//		if (InternalPlatform.DEBUG_PREFERENCES) {
		//			System.out.println("Loading preferences for plugin " + getDescriptor().getUniqueIdentifier()); //$NON-NLS-1$
		//		} // lazily create preference store
		// important: set preferences instance field to prevent re-entry
		preferences = new Preferences(); // load settings into preference store 
		loadPluginPreferences(); // 1. fill in defaults supplied by this plug-in
		initializeDefaultPluginPreferences(); // 2. override with defaults stored with plug-in
		//			applyInternalPluginDefaultOverrides();
		// 3. override with defaults from primary feature or command line
		//		applyExternalPluginDefaultOverrides();

		//		if (InternalPlatform.DEBUG_PREFERENCES) {
		//			System.out.println("Completed loading preferences for plugin " + getDescriptor().getUniqueIdentifier()); //$NON-NLS-1$
		//		}
		return preferences;
	}
	/**
	 * Loads preferences settings for this plug-in from the plug-in preferences
	 * file in the plug-in's state area. This plug-in must have a preference store
	 * object.
	 * 
	 * @see Preferences#load
	 * @since 2.0
	 */
	private void loadPluginPreferences() {
		// the preferences file is located in the plug-in's state area at a well-known name
		// don't need to create the directory if there are no preferences to load
		File prefFile = InternalPlatform.getDefault().getMetaArea().getPreferenceLocation(getUniqueIdentifier(), false).toFile();		
		if (!prefFile.exists()) {
			// no preference file - that's fine
			if (InternalPlatform.DEBUG_PREFERENCES) {
				System.out.println("Plugin preference file " + prefFile + " not found."); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return;
		}

		if (InternalPlatform.DEBUG_PREFERENCES) {
			System.out.println("Loading preferences from " + prefFile); //$NON-NLS-1$
		}
		// load preferences from file
		SafeFileInputStream in = null;
		try {
			in = new SafeFileInputStream(prefFile);
			preferences.load(in);
		} catch (IOException e) {
			// problems loading preference store - quietly ignore
			if (InternalPlatform.DEBUG_PREFERENCES) {
				System.out.println("IOException encountered loading preference file " + prefFile); //$NON-NLS-1$
				e.printStackTrace();
			}
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// ignore problems with close
					if (InternalPlatform.DEBUG_PREFERENCES) {
						System.out.println("IOException encountered closing preference file " + prefFile); //$NON-NLS-1$
						e.printStackTrace();
					}
				}
			}
		}
		if (InternalPlatform.DEBUG_PREFERENCES) {
			System.out.println("Preferences now set as follows:"); //$NON-NLS-1$
			String[] prefNames = preferences.propertyNames();
			for (int i = 0; i < prefNames.length; i++) {
				String value = preferences.getString(prefNames[i]);
				System.out.println("\t" + prefNames[i] + " = " + value); //$NON-NLS-1$ //$NON-NLS-2$
			}
			prefNames = preferences.defaultPropertyNames();
			for (int i = 0; i < prefNames.length; i++) {
				String value = preferences.getDefaultString(prefNames[i]);
				System.out.println("\tDefault values: " + prefNames[i] + " = " + value); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}
	/**
	 * Saves preferences settings for this plug-in. Does nothing if the preference
	 * store does not need saving.
	 * <p>
	 * Plug-in preferences are <b>not</b> saved automatically on plug-in shutdown.
	 * </p>
	 * 
	 * @see Preferences#store
	 * @see Preferences#needsSaving
	 * @since 2.0
	 */
	public final void savePluginPreferences() {
		if (preferences == null || !preferences.needsSaving()) { 
			// nothing to save
			return;
		} 
		
		// preferences need to be saved
		// the preferences file is located in the plug-in's state area
		// at a well-known name (pref_store.ini)
		File prefFile = InternalPlatform.getDefault().getMetaArea().getPreferenceLocation(getUniqueIdentifier(), true).toFile();
		if (preferences.propertyNames().length == 0) { // there are no preference settings
			// rather than write an empty file, just delete any existing file
			if (InternalPlatform.DEBUG_PREFERENCES) {
				System.out.println("Removing saved preferences from " + prefFile); //$NON-NLS-1$
			}
			if (prefFile.exists()) {
				prefFile.delete(); // don't worry if delete unsuccessful
			}
			return;
		} // write file, overwriting an existing one
		OutputStream out = null;
		try {
			// do it as carefully as we know how so that we don't lose/mangle
			// the setting in times of stress
			out = new SafeFileOutputStream(prefFile);
			preferences.store(out, null);
		} catch (IOException e) { // problems saving preference store - quietly ignore
			if (InternalPlatform.DEBUG_PREFERENCES) {
				System.out.println("IOException writing to preference file " + prefFile); //$NON-NLS-1$
				e.printStackTrace();
			}
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) { // ignore problems with close
					if (InternalPlatform.DEBUG_PREFERENCES) {
						System.out.println("IOException closing preference file " + prefFile); //$NON-NLS-1$
						e.printStackTrace();
					}
				}
			}
		}
	} /**
						 * Initializes the default preferences settings for this plug-in.
						 * <p>
						 * This method is called sometime after the preference store for this
						 * plug-in is created. Default values are never stored in preference
						 * stores; they must be filled in each time. This method provides the
						 * opportunity to initialize the default values.
						 * </p>
						 * <p>
						 * The default implementation of this method does nothing. A subclass that needs
						 * to set default values for its preferences must reimplement this method.
						 * Default values set at a later point will override any default override
						 * settings supplied from outside the plug-in (product configuration or
						 * platform start up).
						 * </p>
						 * 
						 * @since 2.0
						 */
	protected void initializeDefaultPluginPreferences() { // default implementation of this method - spec'd to do nothing
	}

	public File getDataFile(String name) {
		return context.getDataFile(name);
	}

	public IPath getStateLocation() {
		return InternalPlatform.getDefault().getStateLocation(getUniqueIdentifier(), true);
	}

	public Bundle getBundle() {
		return context.getBundle();
	}

	public String getUniqueIdentifier() {
		return getBundle().getGlobalName();
	}

	public Bundle getBundle(String bundleName) {
		return getBundleContext().getBundle(bundleName);
	}

	public boolean isActive(String bundleName) {
		Bundle b = getBundle(bundleName);
		if (b == null)
			return false;
		return (b.getState() & Bundle.ACTIVE) != 0;
	}
	/**
	 * Returns whether this bundle is in debug mode.
	 * By default bundles are not in debug mode.  A bundle can put itself
	 * into debug mode or the user can set an execution option to do so.
	 *
	 * @return whether this bundle is in debug mode
	 */	
	public boolean isDebugging() {
		return debug;
	}
	/**
	 * Sets whether this bundle is in debug mode.
	 * By default bundles are not in debug mode.  A bundle can put itself
	 * into debug mode or the user can set a debug option to do so.
	 *
	 * @param value whether or not this bundle is in debug mode
	 */
	public void setDebugging(boolean value) {
		debug = value;
	}
	
	public boolean getBooleanOption(String option, boolean defaultValue) {
		return InternalPlatform.getDefault().getBooleanOption(option, defaultValue);
	}
	public String getOption(String option) {
		return InternalPlatform.getDefault().getOption(option);
	}
	
	public int getIntegerOption(String option, int defaultValue) {
		return InternalPlatform.getDefault().getIntegerOption(option, defaultValue);
	}
	public void setOption(String option, String value) {
			InternalPlatform.getDefault().setOption(option, value);
		}
}

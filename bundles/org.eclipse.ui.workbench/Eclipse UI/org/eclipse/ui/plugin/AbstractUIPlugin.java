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
package org.eclipse.ui.plugin;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.Workbench;

/**
 * Abstract base class for plug-ins that integrate with the Eclipse platform UI.
 * <p>
 * Subclasses obtain the following capabilities:
 * </p>
 * <p>
 * Preferences
 * <ul>
 * <li> The platform core runtime contains general support for plug-in
 *      preferences (<code>org.eclipse.core.runtime.Preferences</code>). 
 *      This class provides appropriate conversion to the older JFace preference 
 *      API (<code>org.eclipse.jface.preference.IPreferenceStore</code>).</li>
 * <li> The method <code>getPreferenceStore</code> returns the JFace preference
 *      store (cf. <code>Plugin.getPluginPreferences</code> which returns
 *      a core runtime preferences object.</li>
 * <li> Subclasses may reimplement <code>initializeDefaultPreferences</code>
 *      to set up any default values for preferences using JFace API. In this
 *      case, <code>initializeDefaultPluginPreferences</code> should not be
 *      overridden.</li>
 * <li> Subclasses may reimplement
 *      <code>initializeDefaultPluginPreferences</code> to set up any default
 *      values for preferences using core runtime API. In this
 *      case, <code>initializeDefaultPreferences</code> should not be
 *      overridden.</li>
 * <li> Preferences are also saved automatically on plug-in shutdown.
 *      However, saving preferences immediately after changing them is
 *      strongly recommended, since that ensures that preference settings
 *      are not lost even in the event of a platform crash.</li>
 * </ul>
 * Dialogs
 * <ul>
 * <li> The dialog store is read the first time <code>getDialogSettings</code> 
 *      is called.</li>
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
public abstract class AbstractUIPlugin extends Plugin {

	/**
	 * The name of the dialog settings file (value 
	 * <code>"dialog_settings.xml"</code>).
	 */
	private static final String FN_DIALOG_SETTINGS = "dialog_settings.xml"; //$NON-NLS-1$

	/**
	 * Storage for dialog and wizard data; <code>null</code> if not yet
	 * initialized.
	 */
	private DialogSettings dialogSettings = null;

	/**
	 * Storage for preferences.
	 */
	private CompatibilityPreferenceStore preferenceStore;

	/**
	 * The registry for all graphic images; <code>null</code> if not yet
	 * initialized.
	 */
	private ImageRegistry imageRegistry = null;

	/**
	 * Internal implementation of a JFace preference store atop a core runtime
	 * preference store.
	 * 
	 * @since 2.0
	 */
	private class CompatibilityPreferenceStore implements IPersistentPreferenceStore {

		/**
		 * Flag to indicate that the listener has been added.
		 */
		private boolean listenerAdded = false;

		/**
		 * The underlying core runtime preference store; <code>null</code> if it
		 * has not been initialized yet.
		 */
		private Preferences prefs = null;

		/**
		 * Identity list of old listeners (element type: 
		 * <code>org.eclipse.jface.util.IPropertyChangeListener</code>).
		 */
		private ListenerList listeners = new ListenerList();

		/**
		 * Indicates whether property change events should be suppressed
		 * (used in implementation of <code>putValue</code>). Initially
		 * and usually <code>false</code>.
		 * 
		 * @see IPreferenceStore#putValue
		 */
		private boolean silentRunning = false;

		/**
		 * Creates a new instance for the this plug-in.
		 */
		public CompatibilityPreferenceStore() {
			// Important: do not call initialize() here
			// due to heinous reentrancy problems.
		}

		/**
		 * Initializes this preference store.
		 */
		void initialize() {
			// ensure initialization is only done once.
			if (this.prefs != null) {
				return;
			}
			// here's where we first ask for the plug-in's core runtime 
			// preferences;
			// note that this causes this method to be reentered
			this.prefs = getPluginPreferences();
			// avoid adding the listener a second time when reentered
			if (!this.listenerAdded) {
				// register listener that funnels everything to firePropertyChangeEvent
				this
					.prefs
					.addPropertyChangeListener(
						new Preferences
						.IPropertyChangeListener() {
					public void propertyChange(
						Preferences.PropertyChangeEvent event) {
						if (!silentRunning) {
							firePropertyChangeEvent(
								event.getProperty(),
								event.getOldValue(),
								event.getNewValue());
						}
					}
				});
				this.listenerAdded = true;
			}
		}

		/**
		 * Returns the underlying preference store.
		 * 
		 * @return the underlying preference store
		 */
		private Preferences getPrefs() {
			if (prefs == null) {
				// although we try to ensure initialization is done eagerly,
				// this cannot be guaranteed, so ensure it is done here
				initialize();
			}
			return prefs;
		}

		/* (non-javadoc)
		 * Method declared on IPreferenceStore
		 */
		public void addPropertyChangeListener(final IPropertyChangeListener listener) {
			listeners.add(listener);
		}

		/* (non-javadoc)
		 * Method declared on IPreferenceStore
		 */
		public void removePropertyChangeListener(IPropertyChangeListener listener) {
			listeners.remove(listener);
		}

		/* (non-javadoc)
		 * Method declared on IPreferenceStore
		 */
		public void firePropertyChangeEvent(
			String name,
			Object oldValue,
			Object newValue) {

			// efficiently handle case of 0 listeners
			if (listeners.isEmpty()) {
				// no one interested
				return;
			}

			// important: create intermediate array to protect against listeners 
			// being added/removed during the notification
			final Object[] list = listeners.getListeners();
			final PropertyChangeEvent event =
				new PropertyChangeEvent(this, name, oldValue, newValue);
			Platform.run(new SafeRunnable(JFaceResources.getString("PreferenceStore.changeError")) { //$NON-NLS-1$
				public void run() {
					for (int i = 0; i < list.length; i++) {
						((IPropertyChangeListener) list[i]).propertyChange(
							event);
					}
				}
			});

		}

		/* (non-javadoc)
		 * Method declared on IPreferenceStore
		 */
		public boolean contains(String name) {
			return getPrefs().contains(name);
		}

		/* (non-javadoc)
		 * Method declared on IPreferenceStore
		 */
		public boolean getBoolean(String name) {
			return getPrefs().getBoolean(name);
		}

		/* (non-javadoc)
		 * Method declared on IPreferenceStore
		 */
		public boolean getDefaultBoolean(String name) {
			return getPrefs().getDefaultBoolean(name);
		}

		/* (non-javadoc)
		 * Method declared on IPreferenceStore
		 */
		public double getDefaultDouble(String name) {
			return getPrefs().getDefaultDouble(name);
		}

		/* (non-javadoc)
		 * Method declared on IPreferenceStore
		 */
		public float getDefaultFloat(String name) {
			return getPrefs().getDefaultFloat(name);
		}

		/* (non-javadoc)
		 * Method declared on IPreferenceStore
		 */
		public int getDefaultInt(String name) {
			return getPrefs().getDefaultInt(name);
		}

		/* (non-javadoc)
		 * Method declared on IPreferenceStore
		 */
		public long getDefaultLong(String name) {
			return getPrefs().getDefaultLong(name);
		}

		/* (non-javadoc)
		 * Method declared on IPreferenceStore
		 */
		public String getDefaultString(String name) {
			return getPrefs().getDefaultString(name);
		}

		/* (non-javadoc)
		 * Method declared on IPreferenceStore
		 */
		public double getDouble(String name) {
			return getPrefs().getDouble(name);
		}

		/* (non-javadoc)
		 * Method declared on IPreferenceStore
		 */
		public float getFloat(String name) {
			return getPrefs().getFloat(name);
		}

		/* (non-javadoc)
		 * Method declared on IPreferenceStore
		 */
		public int getInt(String name) {
			return getPrefs().getInt(name);
		}

		/* (non-javadoc)
		 * Method declared on IPreferenceStore
		 */
		public long getLong(String name) {
			return getPrefs().getLong(name);
		}

		/* (non-javadoc)
		 * Method declared on IPreferenceStore
		 */
		public String getString(String name) {
			return getPrefs().getString(name);
		}

		/* (non-javadoc)
		 * Method declared on IPreferenceStore
		 */
		public boolean isDefault(String name) {
			return getPrefs().isDefault(name);
		}

		/* (non-javadoc)
		 * Method declared on IPreferenceStore
		 */
		public boolean needsSaving() {
			return getPrefs().needsSaving();
		}

		/* (non-javadoc)
		 * Method declared on IPreferenceStore
		 */
		public void putValue(String name, String value) {
			try {
				// temporarily suppress event notification while setting value
				silentRunning = true;
				getPrefs().setValue(name, value);
			} finally {
				silentRunning = false;
			}
		}

		/* (non-javadoc)
		 * Method declared on IPreferenceStore
		 */
		public void setDefault(String name, double value) {
			getPrefs().setDefault(name, value);
		}

		/* (non-javadoc)
		 * Method declared on IPreferenceStore
		 */
		public void setDefault(String name, float value) {
			getPrefs().setDefault(name, value);
		}

		/* (non-javadoc)
		 * Method declared on IPreferenceStore
		 */
		public void setDefault(String name, int value) {
			getPrefs().setDefault(name, value);
		}

		/* (non-javadoc)
		 * Method declared on IPreferenceStore
		 */
		public void setDefault(String name, long value) {
			getPrefs().setDefault(name, value);
		}

		/* (non-javadoc)
		 * Method declared on IPreferenceStore
		 */
		public void setDefault(String name, String value) {
			getPrefs().setDefault(name, value);
		}

		/* (non-javadoc)
		 * Method declared on IPreferenceStore
		 */
		public void setDefault(String name, boolean value) {
			getPrefs().setDefault(name, value);
		}

		/* (non-javadoc)
		 * Method declared on IPreferenceStore
		 */
		public void setToDefault(String name) {
			getPrefs().setToDefault(name);
		}

		/* (non-javadoc)
		 * Method declared on IPreferenceStore
		 */
		public void setValue(String name, double value) {
			getPrefs().setValue(name, value);
		}

		/* (non-javadoc)
		 * Method declared on IPreferenceStore
		 */
		public void setValue(String name, float value) {
			getPrefs().setValue(name, value);
		}

		/* (non-javadoc)
		 * Method declared on IPreferenceStore
		 */
		public void setValue(String name, int value) {
			getPrefs().setValue(name, value);
		}

		/* (non-javadoc)
		 * Method declared on IPreferenceStore
		 */
		public void setValue(String name, long value) {
			getPrefs().setValue(name, value);
		}

		/* (non-javadoc)
		 * Method declared on IPreferenceStore
		 */
		public void setValue(String name, String value) {
			getPrefs().setValue(name, value);
		}

		/* (non-javadoc)
		 * Method declared on IPreferenceStore
		 */
		public void setValue(String name, boolean value) {
			getPrefs().setValue(name, value);
		}
		/**
		 * @see org.eclipse.jface.preference.IPersistentPreferenceStore#save()
		 */
		public void save() throws IOException {
			AbstractUIPlugin.this.savePreferenceStore();
		}

	}

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
		// Create the preference store lazily.
		if (preferenceStore == null) {
			// must assign field before calling initialize(), since
			// this method can be reentered during initialization
			preferenceStore = new CompatibilityPreferenceStore();
			// force initialization
			preferenceStore.initialize();
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
	 * Initializes a preference store with default preference values 
	 * for this plug-in.
	 * <p>
	 * This method is called after the preference store is initially loaded
	 * (default values are never stored in preference stores).
	 * </p>
	 * <p>
	 * The default implementation of this method does nothing.
	 * Subclasses should reimplement this method if the plug-in has any preferences.
	 * </p>
	 * <p>
	 * A subclass may reimplement this method to set default values for the 
	 * preference store using JFace API. This is the older way of initializing 
	 * default values. If this method is reimplemented, do not override
	 * <code>initializeDefaultPluginPreferences()</code>.
	 * </p>
	 *
	 * @param store the preference store to fill
	 */
	protected void initializeDefaultPreferences(IPreferenceStore store) {
	}

	/**
	 * The <code>AbstractUIPlugin</code> implementation of this 
	 * <code>Plugin</code> method forwards to 
	 * <code>initializeDefaultPreferences(IPreferenceStore)</code>.
	 * <p>
	 * A subclass may reimplement this method to set default values for the core
	 * runtime preference store in the standard way. This is the recommended way
	 * to do this. 
	 * The older <code>initializeDefaultPreferences(IPreferenceStore)</code> method
	 * serves a similar purpose. If this method is reimplemented, do not send super,
	 * and do not override <code>initializeDefaultPreferences(IPreferenceStore)</code>.
	 * </p>
	 * 
	 * @see #initializeDefaultPreferences
	 * @since 2.0
	 */
	protected void initializeDefaultPluginPreferences() {
		// N.B. by the time this method is called, the plug-in has a 
		// core runtime preference store (no default values)

		// call loadPreferenceStore (only) for backwards compatibility with Eclipse 1.0
		loadPreferenceStore();
		// call initializeDefaultPreferences (only) for backwards compatibility 
		// with Eclipse 1.0
		initializeDefaultPreferences(getPreferenceStore());
	}

	/** 
	 * Initializes an image registry with images which are frequently used by the 
	 * plugin.
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
	 * @see #getImageRegistry
	 */
	protected void initializeImageRegistry(ImageRegistry reg) {
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
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8")); //$NON-NLS-1$
				dialogSettings.load(reader);
			} catch (IOException e) {
				// load failed so ensure we have an empty settings
				dialogSettings = new DialogSettings("Workbench"); //$NON-NLS-1$
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
	 * 
	 * @deprecated As of Eclipse 2.0, a basic preference store exists for all
	 * plug-ins. This method now exists only for backwards compatibility.
	 * It is called as the plug-in's preference store is being initialized.
	 * The plug-ins preferences are loaded from the file regardless of what
	 * this method does.
	 */
	protected void loadPreferenceStore() {
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
		final Workbench wb = (Workbench) PlatformUI.getWorkbench();
		if (wb != null) {
			// startup() is not guaranteed to be called in the UI thread,
			// but refreshPluginActions must run in the UI thread, 
			// so use asyncExec.  See bug 6623 for more details.
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					wb.refreshPluginActions(
						getDescriptor().getUniqueIdentifier());
				}
			});
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
			String readWritePath =
				getStateLocation().append(FN_DIALOG_SETTINGS).toOSString();
			dialogSettings.save(readWritePath);
		} catch (IOException e) {
		}
	}

	/**
	 * Saves this plug-in's preference store.
	 * Any problems which arise are silently ignored.
	 * 
	 * @see Plugin#savePluginPreferences
	 * @deprecated As of Eclipse 2.0, preferences exist for all plug-ins. The 
	 * equivalent of this method is <code>Plugin.savePluginPreferences</code>. 
	 * This method now calls <code>savePluginPreferences</code>, and exists only for
	 * backwards compatibility.
	 */
	protected void savePreferenceStore() {
		savePluginPreferences();
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

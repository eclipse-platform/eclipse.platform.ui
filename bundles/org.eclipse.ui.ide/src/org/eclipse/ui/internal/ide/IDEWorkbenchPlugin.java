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

package org.eclipse.ui.internal.ide;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.internal.registry.CapabilityRegistry;
import org.eclipse.ui.internal.registry.MarkerImageProviderRegistry;
import org.eclipse.ui.internal.registry.ProjectImageRegistry;
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
public class IDEWorkbenchPlugin extends AbstractUIPlugin {
	// Default instance of the receiver
	private static IDEWorkbenchPlugin inst;

	// Global workbench ui plugin flag. Only workbench implementation is allowed to use this flag
	// All other plugins, examples, or test cases must *not* use this flag.
	public static boolean DEBUG = false;

	/**
	 * The IDE workbench plugin ID.
	 */
	public static final String IDE_WORKBENCH = "org.eclipse.ui.ide"; //$NON-NLS-1$

	/**
	 * Project image registry; lazily initialized.
	 */	
	private ProjectImageRegistry projectImageRegistry = null;

	/**
	 * Marker image registry; lazily initialized.
	 */	
	private MarkerImageProviderRegistry markerImageProviderRegistry = null;

	/**
	 * Capability registry; lazily initialized.
	 */	
	private CapabilityRegistry capabilityRegistry;

	/**
	 * Create an instance of the IDEWorkbenchPlugin.
	 * The workbench plugin is effectively the "application" for the workbench UI.
	 * The entire UI operates as a good plugin citizen.
	 */
	public IDEWorkbenchPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		inst = this;
	}
				
	/**
	 * Creates an extension.  If the extension plugin has not
	 * been loaded a busy cursor will be activated during the duration of
	 * the load.
	 *
	 * @param element the config element defining the extension
	 * @param classAttribute the name of the attribute carrying the class
	 * @returns the extension object
	 */
	public static Object createExtension(final IConfigurationElement element, final String classAttribute) throws CoreException {
		// If plugin has been loaded create extension.
		// Otherwise, show busy cursor then create extension.
		IPluginDescriptor plugin = element.getDeclaringExtension().getDeclaringPluginDescriptor();
		if (plugin.isPluginActivated()) {
			return element.createExecutableExtension(classAttribute);
		} else {
			final Object[] ret = new Object[1];
			final CoreException[] exc = new CoreException[1];
			BusyIndicator.showWhile(null, new Runnable() {
				public void run() {
					try {
						ret[0] = element.createExecutableExtension(classAttribute);
					} catch (CoreException e) {
						exc[0] = e;
					}
				}
			});
			if (exc[0] != null)
				throw exc[0];
			else
				return ret[0];
		}
	}
	
	/* Return the default instance of the receiver. This represents the runtime plugin.
	 *
	 * @see AbstractPlugin for the typical implementation pattern for plugin classes.
	 */
	public static IDEWorkbenchPlugin getDefault() {
		return inst;
	}

	/**
	 * Return the workspace used by the workbench
	 *
	 * This method is internal to the workbench and must not be called
	 * by any plugins.
	 */
	public static IWorkspace getPluginWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
	
	/**
	 * Log the given status to the ISV log.
	 *
	 * When to use this:
	 *
	 *		This should be used when a PluginException or a
	 *		ExtensionException occur but for which an error
	 *		dialog cannot be safely shown.
	 *
	 *		If you can show an ErrorDialog then do so, and do
	 *		not call this method.
	 *
	 *		If you have a plugin exception or core exception in hand
	 *		call log(String, IStatus)
	 *
	 * This convenience method is for internal use by the Workbench only
	 * and must not be called outside the workbench.
	 *
	 * This method is supported in the event the log allows plugin related
	 * information to be logged (1FTTJKV). This would be done by this method.
	 *
	 * This method is internal to the workbench and must not be called
	 * by any plugins, or examples.
	 *
	 * @param message 	A high level UI message describing when the problem happened.
	 *
	 */
	public static void log(String message) {
		getDefault().getLog().log(StatusUtil.newStatus(Status.ERROR, message, null));
		System.err.println(message);
		//1FTTJKV: ITPCORE:ALL - log(status) does not allow plugin information to be recorded
	}
	
	/**
	 * Log the given status to the ISV log.
	 *
	 * When to use this:
	 *
	 *		This should be used when a PluginException or a
	 *		ExtensionException occur but for which an error
	 *		dialog cannot be safely shown.
	 *
	 *		If you can show an ErrorDialog then do so, and do
	 *		not call this method.
	 *
	 * This convenience method is for internal use by the workbench only
	 * and must not be called outside the workbench.
	 *
	 * This method is supported in the event the log allows plugin related
	 * information to be logged (1FTTJKV). This would be done by this method.
	 *
	 * This method is internal to the workbench and must not be called
	 * by any plugins, or examples.
	 *
	 * @param message 	A high level UI message describing when the problem happened.
	 *					May be null.
	 * @param status  	The status describing the problem.
	 *					Must not be null.
	 *
	 */
	public static void log(String message, IStatus status) {

		//1FTUHE0: ITPCORE:ALL - API - Status & logging - loss of semantic info

		if (message != null) {
			getDefault().getLog().log(StatusUtil.newStatus(IStatus.ERROR, message, null));
			System.err.println(message + "\nReason:"); //$NON-NLS-1$
		}

		getDefault().getLog().log(status);
		System.err.println(status.getMessage());

		//1FTTJKV: ITPCORE:ALL - log(status) does not allow plugin information to be recorded
	}

	/** 
	 * Set default preference values.
	 * This method must be called whenever the preference store is initially loaded
	 * because the default values are not stored in the preference store.
	 */
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		store.setDefault(IDEInternalPreferences.SAVE_ALL_BEFORE_BUILD, false);
		store.setDefault(IDEInternalPreferences.SAVE_INTERVAL, 5); //5 minutes
		store.setDefault(IDEInternalPreferences.REFRESH_WORKSPACE_ON_STARTUP, false);
		store.setDefault(IDEInternalPreferences.EXIT_PROMPT_ON_CLOSE_LAST_WINDOW, true);
		store.setDefault(IDEInternalPreferences.SHOW_TASKS_ON_BUILD, true);
		store.setDefault(IDEInternalPreferences.PROJECT_SWITCH_PERSP_MODE, IDEInternalPreferences.PSPM_PROMPT);
	}

	/**
	 * Return the manager that maps project nature ids to images.
	 */
	public ProjectImageRegistry getProjectImageRegistry() {
		if (projectImageRegistry == null) {
			projectImageRegistry = new ProjectImageRegistry();
			projectImageRegistry.load();
		}
		return projectImageRegistry;
	}

	/**
	 * Returns the marker image provider registry for the workbench.
	 *
	 * @return the marker image provider registry
	 */
	public MarkerImageProviderRegistry getMarkerImageProviderRegistry() {
		if (markerImageProviderRegistry == null) {
			markerImageProviderRegistry = new MarkerImageProviderRegistry();
		}
		return markerImageProviderRegistry;
	}

	/**
	 * Returns the capability registry for the workbench.
	 * 
	 * @return the capability registry
	 */
	public CapabilityRegistry getCapabilityRegistry() {
		if (capabilityRegistry == null) {
			capabilityRegistry = new CapabilityRegistry();
			capabilityRegistry.load();
		}
		return capabilityRegistry;
	}
}

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.custom.BusyIndicator;
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
	public static String IDE_WORKBENCH = "org.eclipse.ui.ide";

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
	/* Answer the manager that maps resource types to a the 
	 * description of the editor to use
	*/

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
	public void startup() throws CoreException {
		/* The plugin org.eclipse.ui has being separed in
		   several plugins. Copy the state files from 
		   org.eclipse.ui to org.eclipse.ui.workbench */
		
		IPath locationPath = getStateLocation();
		File newLocation = locationPath.toFile();
		File oldLocation = new File(newLocation,"..//org.eclipse.ui"); //$NON-NLS-1$
		try {
			oldLocation = oldLocation.getCanonicalFile();
		} catch (IOException e) {}		
		String markerFileName = ".copiedStateFiles_Marker"; //$NON-NLS-1$
		File markerFile = new File(oldLocation,markerFileName);
		if(markerFile.exists())
			return;
			
		try {
			String list[] = newLocation.list();
			if(list != null && list.length != 0)
				return;

			String oldList[] = oldLocation.list();
			if(oldList == null || oldList.length == 0)
				return;

			byte b[] = new byte[1024];
			for (int i = 0; i < oldList.length; i++) {
				String string = oldList[i];
				try {
					File oldFile = new File(oldLocation,string);
					FileInputStream in = new FileInputStream(oldFile);
					FileOutputStream out = new FileOutputStream(new File(newLocation,string));
					int read = in.read(b);
					while(read >= 0) {
						out.write(b,0,read);
						read = in.read(b);
					}
					in.close();
					out.close();
					// Don't delete the old file since the public preferences and some 
					// dialog settings are still read from org.eclipse.ui.
					// See bug 33334. 
					// oldFile.delete();
				} catch (IOException e) {
					new File(newLocation,string).delete();
				}
			}
		} finally {
			try { 
				new FileOutputStream(markerFile).close(); 
			} catch (IOException e) {}
		}
	}

}

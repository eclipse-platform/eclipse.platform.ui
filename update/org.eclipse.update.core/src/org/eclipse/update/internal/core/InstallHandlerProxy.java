/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.core;

import java.io.*;
import java.net.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.osgi.framework.*;

public class InstallHandlerProxy implements IInstallHandler {

	private IFeature feature = null;
	private int type;
	private IInstallHandler handler = null;
	private IStatus savedStatus = null;
	private boolean DEBUG = false;

	private static final String EXT_PLUGIN = "org.eclipse.update.core"; //$NON-NLS-1$
	private static final String UI_PLUGIN = "org.eclipse.ui"; //$NON-NLS-1$
	private static final String EXT_POINT = "installHandlers"; //$NON-NLS-1$

	/**
	 * A class loader that combines a the org.eclipse.update.core plugin class loader with the
	 * org.eclipse.ui class loader (only when UI is active).
	 */
	private static class InstallHandlerClassLoader extends URLClassLoader {
		private Bundle updateCore;
		private Bundle eclipseUI;

		public InstallHandlerClassLoader(URL classpath) {
			super(new URL[] {classpath});
			updateCore = Platform.getBundle(EXT_PLUGIN);
			eclipseUI = Platform.getBundle(UI_PLUGIN);
			if (eclipseUI.getState() != Bundle.ACTIVE) 
				eclipseUI = null;
		}

		public Class loadClass(String className) throws ClassNotFoundException {
			// First check update core plugin loader, then the eclipse ui plugin loader
			Class c = null;
			try {
				c = updateCore.loadClass(className);
			} catch (ClassNotFoundException e) {
				try {
					if(eclipseUI != null)
						c = eclipseUI.loadClass(className);
				} catch (ClassNotFoundException e2) {
				} finally {
				}
			} finally {
			}
			if (c != null)
				return c;
			else
				return super.loadClass(className);
		}

		public URL getResource(String resName) {
			// First check update core plugin loader, then the eclipse ui plugin loader
			URL u = updateCore.getResource(resName);
			if(u == null && eclipseUI != null)
				u = eclipseUI.getResource(resName);
				
			if (u != null)
				return u;
			else
				return super.getResource(resName);
		}
	}
	
	private InstallHandlerProxy() {
	}

	public InstallHandlerProxy(
		int type,
		IFeature feature,
		IInstallHandlerEntry entry,
		InstallMonitor monitor)
		throws CoreException {

		initialize(type, feature, entry, monitor);
	}

	/*
	 * @see IInstallHandler#initialize
	 */
	public void initialize(
		int type,
		IFeature feature,
		IInstallHandlerEntry entry,
		InstallMonitor monitor)
		throws CoreException {

		DEBUG = UpdateCore.DEBUG_SHOW_IHANDLER;
		// validate arguments
		if (feature == null)
			throw new IllegalArgumentException();
		this.feature = feature;
		this.type = type;

		//  check if we have a handler entry specified in the feature.xml
		if (entry == null) {
			if (DEBUG)
				debug("not specified"); //$NON-NLS-1$
			return; // no handler entry
		}

		String library = entry.getLibrary();
		String handlerName = entry.getHandlerName();
		if (handlerName == null || handlerName.trim().equals("")) { //$NON-NLS-1$
			if (DEBUG)
				debug("not specified"); //$NON-NLS-1$
			return; // no handler class spacified in entry
		}
		if (DEBUG) {
			debug("handler=" + handlerName); //$NON-NLS-1$
			debug("path=   " + library); //$NON-NLS-1$
		}

		// get handler instance
		try {
			if (library == null || library.trim().equals("")) //$NON-NLS-1$
				this.handler = getGlobalHandler(handlerName);
			else
				this.handler = getLocalHandler(library, handlerName);
			if (this.handler == null)
				return;
			handler.initialize(type, feature, entry, monitor);
		} catch (ClassNotFoundException e) {
			handleExceptionInInit(
				Policy.bind("InstallHandler.notFound", feature.getLabel()), //$NON-NLS-1$
				e);

		} catch (ClassCastException e) {
			handleExceptionInInit(
				Policy.bind("InstallHandler.invalidHandler", feature.getLabel()), //$NON-NLS-1$
				e);
		} catch (CoreException e) {
			handleExceptionInInit(null, e);
		} catch (Exception e) {
			handleExceptionInInit(
				Policy.bind("InstallHandler.unableToCreateHandler", feature.getLabel()), //$NON-NLS-1$
				e);
		}

	}

	/*
	 * @see IInstallHandler#installInitiated
	 */
	public void installInitiated() throws CoreException {
		if (handler == null)
			return;
		else {
			try {
				if (DEBUG)
					debug("calling installInitiated()"); //$NON-NLS-1$
				handler.installInitiated();
			} catch (Throwable e) {
				handleExceptionInCall(e, feature);
			}
		}
	}

	/*
	 * @see IInstallHandler#allPluginsDownloaded
	 */
	public void pluginsDownloaded(IPluginEntry[] plugins) throws CoreException {
		if (handler == null)
			return;
		else {
			try {
				if (DEBUG)
					debug("calling pluginsDownloaded()"); //$NON-NLS-1$
				handler.pluginsDownloaded(plugins);
			} catch (Throwable e) {
				handleExceptionInCall(e, feature);
			}
		}
	}

	/*
	 * @see IInstallHandler#allPluginsInstalled
	 */
	public void completeInstall(IFeatureContentConsumer consumer)
		throws CoreException {
		if (handler == null)
			return;
		else {
			try {
				if (DEBUG)
					debug("calling completeInstall()"); //$NON-NLS-1$
				handler.completeInstall(consumer);
			} catch (Throwable e) {
				handleExceptionInCall(e, feature);
			}
		}
	}

	/*
	 * @see IInstallHandler#allDataDownloaded
	 */
	public void nonPluginDataDownloaded(
		INonPluginEntry[] nonPluginData,
		IVerificationListener listener)
		throws CoreException {
		if (handler == null)
			return;
		else {
			try {
				if (DEBUG)
					debug("calling nonPluginDataDownloaded()"); //$NON-NLS-1$
				handler.nonPluginDataDownloaded(nonPluginData, listener);
			} catch (Throwable e) {
				handleExceptionInCall(e, feature);
			}
		}
	}

	/*
	 * @see IInstallHandler#installCompleted
	 */
	public void installCompleted(boolean success) throws CoreException {
		if (handler == null)
			return;
		else {
			try {
				if (DEBUG)
					debug("calling installCompleted()"); //$NON-NLS-1$
				handler.installCompleted(success);
			} catch (Throwable e) {
				handleExceptionInCall(e, feature);
			}
		}
	}

	/*
	 * @see IInstallHandler#configureInitiated
	 */
	public void configureInitiated() throws CoreException {
		if (handler == null)
			return;
		else {
			try {
				if (DEBUG)
					debug("calling configureInitiated()"); //$NON-NLS-1$
				handler.configureInitiated();
			} catch (Throwable e) {
				handleExceptionInCall(e, feature);
			}
		}
	}

	/*
	 * @see IInstallHandler#completeConfigure
	 */
	public void completeConfigure() throws CoreException {
		if (handler == null)
			return;
		else {
			try {
				if (DEBUG)
					debug("calling completeConfigure()"); //$NON-NLS-1$
				handler.completeConfigure();
			} catch (Throwable e) {
				handleExceptionInCall(e, feature);
			}
		}
	}

	/*
	 * @see IInstallHandler#configureCompleted
	 */
	public void configureCompleted(boolean success) throws CoreException {
		if (handler == null)
			return;
		else {
			try {
				if (DEBUG)
					debug("calling configureCompleted()"); //$NON-NLS-1$
				handler.configureCompleted(success);
			} catch (Throwable e) {
				handleExceptionInCall(e, feature);
			}
		}
	}

	/*
	 * @see IInstallHandler#unconfigureInitiated
	 */
	public void unconfigureInitiated() throws CoreException {
		if (handler == null)
			return;
		else {
			try {
				if (DEBUG)
					debug("calling unconfigureInitiated()"); //$NON-NLS-1$
				handler.unconfigureInitiated();
			} catch (Throwable e) {
				handleExceptionInCall(e, feature);
			}
		}
	}

	/*
	 * @see IInstallHandler#completeUnconfigure
	 */
	public void completeUnconfigure() throws CoreException {
		if (handler == null)
			return;
		else {
			try {
				if (DEBUG)
					debug("calling completeUnconfigure()"); //$NON-NLS-1$
				handler.completeUnconfigure();
			} catch (Throwable e) {
				handleExceptionInCall(e, feature);
			}
		}
	}

	/*
	 * @see IInstallHandler#unconfigureCompleted
	 */
	public void unconfigureCompleted(boolean success) throws CoreException {
		if (handler == null) {
			if (savedStatus == null)
				return;
			else
				throw new CoreException(savedStatus); // delayed exception
		} else {
			try {
				if (DEBUG)
					debug("calling unconfigureCompleted()"); //$NON-NLS-1$
				handler.unconfigureCompleted(success);
			} catch (Throwable e) {
				handleExceptionInCall(e, feature);
			}
			if (savedStatus != null)
				throw new CoreException(savedStatus); // delayed exception
		}
	}

	/*
	 * @see IInstallHandler#uninstallInitiated
	 */
	public void uninstallInitiated() throws CoreException {
		if (handler == null)
			return;
		else {
			try {
				if (DEBUG)
					debug("calling uninstallInitiated()"); //$NON-NLS-1$
				handler.uninstallInitiated();
			} catch (Throwable e) {
				handleExceptionInCall(e, feature);
			}
		}
	}

	/*
	 * @see IInstallHandler#completeUninstall
	 */
	public void completeUninstall() throws CoreException {
		if (handler == null)
			return;
		else {
			try {
				if (DEBUG)
					debug("calling completeUninstall()"); //$NON-NLS-1$
				handler.completeUninstall();
			} catch (Throwable e) {
				handleExceptionInCall(e, feature);
			}
		}
	}

	/*
	 * @see IInstallHandler#uninstallCompleted
	 */
	public void uninstallCompleted(boolean success) throws CoreException {
		if (handler == null) {
			if (savedStatus == null)
				return;
			else
				throw new CoreException(savedStatus); // delayed exception
		} else {
			try {
				if (DEBUG)
					debug("calling uninstallCompleted()"); //$NON-NLS-1$
				handler.uninstallCompleted(success);
			} catch (Throwable e) {
				handleExceptionInCall(e, feature);
			}
			if (savedStatus != null)
				throw new CoreException(savedStatus); // delayed exception
		}
	}

	/*
	 * common exception handling for initialization
	 */
	private void handleExceptionInInit(String s, Exception e)
		throws CoreException {

		CoreException ce;
		if (e instanceof CoreException)
			ce = (CoreException) e;
		else
			ce = Utilities.newCoreException(s, e);

		if (isUndoAction()) {
			// for "undo" operations, deactivate handler and log error
			String id =
				UpdateCore.getPlugin().getBundle().getSymbolicName();
			IStatus status =
				new Status(IStatus.ERROR, id, 0, "InstallHandler.deactivated", ce);	//$NON-NLS-1$
			UpdateCore.getPlugin().getLog().log(status);
			handler = null; // disable subsequent handler calls
			savedStatus = status;
		} else
			// for "do" operations, hurl ...
			throw ce;
	}

	/*
	 * common exception handling for calls to install handler
	 */
	private void handleExceptionInCall(Throwable e, IFeature feature)
		throws CoreException {

		CoreException ce;
		if (e instanceof CoreException)
			ce = (CoreException) e;
		else
			ce =
				Utilities.newCoreException(
					Policy.bind("InstallHandler.callException", feature.getLabel()), //$NON-NLS-1$
					e);
		
		if (isUndoAction()) {
			// for "undo" operations, deactivate handler and log error
			String id =
				UpdateCore.getPlugin().getBundle().getSymbolicName();
			IStatus status =
				new Status(IStatus.ERROR, id, 0, "InstallHandler.deactivated", ce);	//$NON-NLS-1$
			UpdateCore.getPlugin().getLog().log(status);
			handler = null; // disable subsequent handler calls
			savedStatus = status;
		} else
			// for "do" operations, hurl ...
			throw ce;
	}

	/*
	 * Indicates whether we are doing (install, configure) or 
	 * undoing (uninstall, unconfigure)
	 */
	private boolean isUndoAction() {
		if (this.type == IInstallHandler.HANDLER_ACTION_INSTALL
			|| this.type == IInstallHandler.HANDLER_ACTION_CONFIGURE)
			return false; // causes exception to be thrown and action aborted
		else
			return true; // causes exception to be logged and action continues
	}

	/*
	 * get an instance of handler downloaded as part of the feature
	 */
	private IInstallHandler getLocalHandler(String lib, String name)
		throws Exception {

		// Get baseline URL for handler (relative to feature.xml). For
		// features being installed from a server (eg. http protocol)
		// the URL will most likely be to a local file copy containing the
		// unpacked feature jar.
		ContentReference baseRef =
			feature.getFeatureContentProvider().getFeatureManifestReference(null);
		URL base = null;
		if (baseRef != null)
			base = baseRef.asURL();
		if (base == null)
			throw Utilities.newCoreException(
				Policy.bind("InstallHandler.unableToCreateHandler", this.feature.getLabel()), //$NON-NLS-1$
				null);


		// determine loader class path
		URL cp = new URL(base, lib);
		if (this.type == IInstallHandler.HANDLER_ACTION_UNINSTALL) {
			// check if we are doing uninstall
			// ... need to make temp copy of library (being removed)
			File tempLib = File.createTempFile("tmp", ".jar"); //$NON-NLS-1$ //$NON-NLS-2$
			tempLib.deleteOnExit();
			FileOutputStream fos = null;
			InputStream is = null;
			try {
				fos = new FileOutputStream(tempLib);
				is = UpdateCore.getPlugin().get(cp).getInputStream();
				Utilities.copy(is, fos, null);
			} finally {
				if (fos != null)
					try {
						fos.close();
					} catch (Exception e) {
					}
				if (is != null)
					try {
						is.close();
					} catch (Exception e) {
					}
			}
			cp = tempLib.toURL();
		}

		// create class loader, load and instantiate handler
		ClassLoader loader = new InstallHandlerClassLoader(cp);
		Class clazz = loader.loadClass(name);
		IInstallHandler handler = (IInstallHandler) clazz.newInstance();
		return handler;
	}

	/*
	 * get instance of global handler registered via extension point
	 */
	private IInstallHandler getGlobalHandler(String name) throws Exception {

		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IConfigurationElement[] handlerExtension =
			reg.getConfigurationElementsFor(EXT_PLUGIN, EXT_POINT, name);
		if (handlerExtension == null || handlerExtension.length <= 0)
			throw Utilities.newCoreException(
				Policy.bind("InstallHandler.unableToCreateHandler", this.feature.getLabel()), //$NON-NLS-1$
				null);

		return (IInstallHandler) handlerExtension[0].createExecutableExtension("class"); //$NON-NLS-1$
	}
	
	private void debug(String s) {
		String pfx = (feature==null) ? "" : feature.getVersionedIdentifier().toString(); //$NON-NLS-1$
		System.out.println("InstallHandler["+pfx+"]: " + s); //$NON-NLS-1$ //$NON-NLS-2$
	}
}

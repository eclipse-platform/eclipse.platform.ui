/***************************************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/
package org.eclipse.help.internal;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.help.internal.context.ContextManager;
import org.eclipse.help.internal.index.IndexManager;
import org.eclipse.help.internal.toc.TocManager;
import org.eclipse.help.internal.util.ResourceLocator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

/**
 * Help System Core plug-in
 */
public class HelpPlugin extends Plugin implements IRegistryChangeListener, BundleListener {

	public final static String PLUGIN_ID = "org.eclipse.help"; //$NON-NLS-1$
	// debug options
	public static boolean DEBUG = false;
	public static boolean DEBUG_CONTEXT = false;
	public static boolean DEBUG_PROTOCOLS = false;
	protected static HelpPlugin plugin;
	// private static BundleContext bundleContext;
	private List tocsChangedListeners = new Vector();

	public final static String BASE_TOCS_KEY = "baseTOCS"; //$NON-NLS-1$
	public final static String IGNORED_TOCS_KEY = "ignoredTOCS"; //$NON-NLS-1$

	protected TocManager tocManager;
	protected static Object tocManagerCreateLock = new Object();
	protected ContextManager contextManager;

	protected IndexManager indexManager;

	/**
	 * Logs an Error message with an exception.
	 */
	public static synchronized void logError(String message, Throwable ex) {
		if (message == null)
			message = ""; //$NON-NLS-1$
		Status errorStatus = new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, message, ex);
		logStatus(errorStatus);
	}

	/**
	 * Logs an IStatus
	 */
	private static void logStatus(IStatus errorStatus) {
		HelpPlugin.getDefault().getLog().log(errorStatus);
	}

	/**
	 * Logs a Warning message with an exception.
	 */
	public static synchronized void logWarning(String message) {
		if (HelpPlugin.DEBUG) {
			if (message == null)
				message = ""; //$NON-NLS-1$
			Status warningStatus = new Status(IStatus.WARNING, PLUGIN_ID, IStatus.OK, message, null);
			HelpPlugin.getDefault().getLog().log(warningStatus);
		}
	}

	/**
	 * @return the singleton instance of the plugin
	 */
	public static HelpPlugin getDefault() {
		return plugin;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		Platform.getExtensionRegistry().removeRegistryChangeListener(this);
		context.removeBundleListener(this);
		plugin = null;
		// bundleContext = null;
		super.stop(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		// bundleContext = context;
		context.addBundleListener(this);
		Platform.getExtensionRegistry().addRegistryChangeListener(this, HelpPlugin.PLUGIN_ID);
		// Setup debugging options
		DEBUG = isDebugging();
		if (DEBUG) {
			DEBUG_CONTEXT = "true".equalsIgnoreCase(Platform.getDebugOption(PLUGIN_ID + "/debug/context")); //$NON-NLS-1$ //$NON-NLS-2$
			DEBUG_PROTOCOLS = "true".equalsIgnoreCase(Platform.getDebugOption(PLUGIN_ID + "/debug/protocols")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Used to obtain Toc Naviagiont Manager
	 * 
	 * @return instance of TocManager
	 */
	public static TocManager getTocManager() {
		if (getDefault().tocManager == null) {
			synchronized (tocManagerCreateLock) {
				if (getDefault().tocManager == null) {
					getDefault().tocManager = new TocManager();
				}
			}
		}
		return getDefault().tocManager;
	}

	/**
	 * Used to obtain Context Manager returns an instance of ContextManager
	 */
	public static ContextManager getContextManager() {
		if (getDefault().contextManager == null)
			getDefault().contextManager = new ContextManager();
		return getDefault().contextManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IRegistryChangeListener#registryChanged(org.eclipse.core.runtime.IRegistryChangeEvent)
	 */
	public void registryChanged(IRegistryChangeEvent event) {
		IExtensionDelta[] deltas = event.getExtensionDeltas(HelpPlugin.PLUGIN_ID, TocManager.TOC_XP_NAME);
		if (deltas.length > 0) {
			tocManager = null;
		}
		// notifiy listeners
		if (deltas.length > 0) {
			for (Iterator it = tocsChangedListeners.iterator(); it.hasNext();) {
				((ITocsChangedListener) it.next()).tocsChanged();
			}
		}
	}

	public void addTocsChangedListener(ITocsChangedListener listener) {
		if (!tocsChangedListeners.contains(listener)) {
			tocsChangedListeners.add(listener);
		}
	}

	public void removeTocsChangedListener(ITocsChangedListener listener) {
		tocsChangedListeners.remove(listener);
	}

	public void bundleChanged(BundleEvent event) {
		int type = event.getType();
		if (type == BundleEvent.RESOLVED || type == BundleEvent.UNRESOLVED) {
			ResourceLocator.clearZipCache();
		}
	}

	public static IndexManager getIndexManager() {
		if (getDefault().indexManager == null)
			getDefault().indexManager = new IndexManager();
		return getDefault().indexManager;
	}

}

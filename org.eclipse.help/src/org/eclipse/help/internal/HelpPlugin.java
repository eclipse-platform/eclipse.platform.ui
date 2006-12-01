/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal;

import java.io.InputStream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.help.internal.context.ContextManager;
import org.eclipse.help.internal.extension.ContentExtensionManager;
import org.eclipse.help.internal.index.IndexManager;
import org.eclipse.help.internal.toc.TocManager;
import org.osgi.framework.BundleContext;

/**
 * Help System Core plug-in
 */
public class HelpPlugin extends Plugin {

	public final static String PLUGIN_ID = "org.eclipse.help"; //$NON-NLS-1$
	public final static String HELP_DATA_KEY = "HELP_DATA"; //$NON-NLS-1$
	public final static String BASE_TOCS_KEY = "baseTOCS"; //$NON-NLS-1$
	public final static String IGNORED_TOCS_KEY = "ignoredTOCS"; //$NON-NLS-1$
	public final static String IGNORED_INDEXES_KEY = "ignoredIndexes"; //$NON-NLS-1$

	private static HelpPlugin plugin;
	private static Object tocManagerCreateLock = new Object();
	
	private TocManager tocManager;
	private ContextManager contextManager;
	private ContentExtensionManager contentExtensionManager;
	private IndexManager indexManager;
	private IHelpProvider helpProvider;

	public static void logWarning(String message) {
		Status errorStatus = new Status(IStatus.WARNING, PLUGIN_ID, IStatus.OK, message, null);
		logStatus(errorStatus);
	}

	public static void logError(String message) {
		Status errorStatus = new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, message, null);
		logStatus(errorStatus);
	}

	public static void logError(String message, Throwable ex) {
		if (message == null) {
			message = ""; //$NON-NLS-1$
		}
		Status errorStatus = new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, message, ex);
		logStatus(errorStatus);
	}

	private static synchronized void logStatus(IStatus errorStatus) {
		HelpPlugin.getDefault().getLog().log(errorStatus);
	}

	/**
	 * @return the singleton instance of the plugin
	 */
	public static HelpPlugin getDefault() {
		return plugin;
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

	/**
	 * Used to obtain the ContentExtensionManager
	 */
	public static ContentExtensionManager getContentExtensionManager() {
		if (getDefault().contentExtensionManager == null)
			getDefault().contentExtensionManager = new ContentExtensionManager();
		return getDefault().contentExtensionManager;
	}

	public static IndexManager getIndexManager() {
		if (getDefault().indexManager == null)
			getDefault().indexManager = new IndexManager();
		return getDefault().indexManager;
	}

	/*
	 * Returns the provider responsible for serving help documents.
	 */
	public IHelpProvider getHelpProvider() {
		return helpProvider;
	}
	
	/*
	 * Sets the provider responsible for serving help documents. Called
	 * on startup.
	 */
	public void setHelpProvider(IHelpProvider helpProvider) {
		this.helpProvider = helpProvider;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/*
	 * An interface by which higher plug-ins can serve help content.
	 */
	public static interface IHelpProvider {
		public InputStream getHelpContent(String href, String locale);
	}
}

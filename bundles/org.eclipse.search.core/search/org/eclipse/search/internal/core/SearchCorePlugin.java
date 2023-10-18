/*******************************************************************************
 * Copyright (c) 2023 Red Hat Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.core;

import org.osgi.framework.BundleContext;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;

import org.eclipse.search.internal.core.text.DirtyFileProvider;
import org.eclipse.search.internal.core.text.TextSearchEngineRegistry;

public class SearchCorePlugin extends Plugin {
	/**
	 * Search Plug-in Id (value <code>"org.eclipse.search"</code>).
	 */
	public static final String PLUGIN_ID = "org.eclipse.search.core"; //$NON-NLS-1$
	/** Status code describing an internal error */
	public static final int INTERNAL_ERROR = 1;

	private static SearchCorePlugin fgSearchPlugin;

	private TextSearchEngineRegistry fTextSearchEngineRegistry;
	private DirtyFileProvider fDirtyFileSearchParticipant;
	private DirtyFileSearchParticipantServiceTracker fDirtyFileSearchParticipantTracker;

	/**
	 * @return Returns the search plugin instance.
	 */
	public static SearchCorePlugin getDefault() {
		return fgSearchPlugin;
	}

	public SearchCorePlugin() {
		super();
		Assert.isTrue(fgSearchPlugin == null);
		fgSearchPlugin = this;
		fTextSearchEngineRegistry = null;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		this.fDirtyFileSearchParticipantTracker = new DirtyFileSearchParticipantServiceTracker(context);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// do nothing
	}

	public TextSearchEngineRegistry getTextSearchEngineRegistry() {
		if (fTextSearchEngineRegistry == null) {
			fTextSearchEngineRegistry = new TextSearchEngineRegistry();
		}
		return fTextSearchEngineRegistry;
	}

	public DirtyFileProvider getDirtyFileDiscovery() {
		if (fDirtyFileSearchParticipant == null) {
			this.fDirtyFileSearchParticipantTracker.open();
			fDirtyFileSearchParticipant = this.fDirtyFileSearchParticipantTracker.checkedGetService();
		}
		return fDirtyFileSearchParticipant;
	}

	/**
	 * Log status to platform log
	 * 
	 * @param status
	 *            the status to log
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, INTERNAL_ERROR, SearchCoreMessages.SearchPlugin_internal_error,
				e));
	}

	public static String getID() {
		return PLUGIN_ID;
	}
}

/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.base.remote;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.base.IHelpBaseConstants;
import org.eclipse.help.internal.search.ISearchHitCollector;
import org.eclipse.help.internal.search.ISearchQuery;
import org.eclipse.help.internal.search.QueryTooComplexException;
import org.eclipse.help.internal.util.URLCoder;

/*
 * Manages indexing and searching for all remote help content.
 */
public class RemoteSearchManager {

	private static final String PROTOCOL_HTTP = "http"; //$NON-NLS-1$
	private static final String PATH_SEARCH = "/help/search?phrase="; //$NON-NLS-1$
	private RemoteSearchParser parser;

	/*
	 * Performs a search for remote content.
	 */
	public void search(ISearchQuery searchQuery, ISearchHitCollector collector, IProgressMonitor pm)
			throws QueryTooComplexException {
		
		pm.beginTask("", 100); //$NON-NLS-1$
		try {
			// infocenters ignore remote content
			if (BaseHelpSystem.getMode() != BaseHelpSystem.MODE_INFOCENTER) {
				InputStream in = null;
				try {
					Preferences prefs = HelpBasePlugin.getDefault().getPluginPreferences();
					String host = prefs.getString(IHelpBaseConstants.P_KEY_REMOTE_HELP_SERVER_HOST);
					int port = prefs.getInt(IHelpBaseConstants.P_KEY_REMOTE_HELP_SERVER_PORT);
					if (host != null && host.length() > 0) {
						// fire off the remote search
						URL url = new URL(PROTOCOL_HTTP, host, port, PATH_SEARCH + URLCoder.encode(searchQuery.getSearchWord()));
						in = url.openStream();
						if (parser == null) {
							parser = new RemoteSearchParser();
						}
						// parse the XML-serialized search results
						List hits = parser.parse(in, new SubProgressMonitor(pm, 100));
						collector.addHits(hits, null);
					}
				}
				catch (IOException e) {
					String msg = "I/O error while trying to contact the remote help server"; //$NON-NLS-1$
					HelpBasePlugin.logError(msg, e);
				}
				catch (Throwable t) {
					String msg = "Internal error while reading search results from remote server"; //$NON-NLS-1$
					HelpBasePlugin.logError(msg, t);
				}
				finally {
					if (in != null) {
						try {
							in.close();
						}
						catch (IOException e) {
							// nothing more we can do
						}
					}
				}
			}
		}
		finally {
			pm.done();
		}
	}
}

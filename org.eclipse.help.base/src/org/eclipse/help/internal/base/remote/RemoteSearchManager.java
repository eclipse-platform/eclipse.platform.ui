/***************************************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/
package org.eclipse.help.internal.base.remote;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.search.ISearchHitCollector;
import org.eclipse.help.internal.search.ISearchQuery;
import org.eclipse.help.internal.search.QueryTooComplexException;
import org.eclipse.help.internal.search.SearchHit;
import org.eclipse.help.internal.util.URLCoder;

/*
 * Manages indexing and searching for all remote help content.
 */
public class RemoteSearchManager {

	private static final String PATH_SEARCH = "/search"; //$NON-NLS-1$
	private static final String PARAM_PHRASE = "phrase"; //$NON-NLS-1$
	private static final String PARAM_LANG = "lang"; //$NON-NLS-1$
	private static final String PROTOCOL_HTTP = "http"; //$NON-NLS-1$

	/*
	 * Performs a search for remote content.
	 */
	public void search(ISearchQuery searchQuery, ISearchHitCollector collector, IProgressMonitor pm)
			throws QueryTooComplexException {
		pm.beginTask("", 100); //$NON-NLS-1$

		PreferenceFileHandler prefHandler = new PreferenceFileHandler();
		String host[] = prefHandler.getHostEntries();
		String port[] = prefHandler.getPortEntries();
		String path[] = prefHandler.getPathEntries();
		String [] protocols = prefHandler.getProtocolEntries();
		String isEnabled[] = prefHandler.isEnabled();
		
		try {
			// InfoCenters ignore remote content
			if (RemoteHelp.isEnabled()) {

				int numICs = host.length; // Total number of hosts
				for (int i = 0; i < numICs; i++) {

					if (isEnabled[i].equals("true")) { //$NON-NLS-1$
						InputStream in = null;
						try {
							URL url;
							
							if(protocols[i].equals(PROTOCOL_HTTP))
							{
								url = new URL("http", host[i], new Integer(port[i]).intValue(), path[i] + PATH_SEARCH + '?' + PARAM_PHRASE + '=' + URLCoder.encode(searchQuery.getSearchWord()) + '&' + PARAM_LANG + '=' + searchQuery.getLocale()); //$NON-NLS-1$
								in = url.openStream();
							}
							else
							{
								url = HttpsUtility.getHttpsURL(protocols[i], host[i], port[i], path[i]+ PATH_SEARCH + '?' + PARAM_PHRASE + '=' + URLCoder.encode(searchQuery.getSearchWord()) + '&' + PARAM_LANG + '=' + searchQuery.getLocale()); 
								in = HttpsUtility.getHttpsStream(url);
							}
							
							RemoteSearchParser parser = new RemoteSearchParser();							
							// parse the XML-serialized search results
							List<SearchHit> hits = parser.parse(in, new SubProgressMonitor(pm, 100));
							collector.addHits(hits, null);
						} catch (IOException e) {
							String msg = "I/O error while trying to contact the remote help server"; //$NON-NLS-1$
							HelpBasePlugin.logError(msg, e);
						} catch (Throwable t) {
							String msg = "Internal error while reading search results from remote server"; //$NON-NLS-1$
							HelpBasePlugin.logError(msg, t);
						} finally {
							if (in != null) {
								try {
									in.close();
								} catch (IOException e) {
									// nothing more we can do
								}
							}
						}
					}
				}
			}

		} finally {
			pm.done();
		}
	}
}

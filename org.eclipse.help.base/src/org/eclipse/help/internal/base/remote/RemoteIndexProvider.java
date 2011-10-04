/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.help.AbstractIndexProvider;
import org.eclipse.help.IIndexContribution;
import org.eclipse.help.internal.base.HelpBasePlugin;

/*
 * Provides the TOC data that is located on the remote infocenter, if the system
 * is configured for remote help. If not, returns no contributions.
 */
public class RemoteIndexProvider extends AbstractIndexProvider {

	private static final String PATH_INDEX = "/index"; //$NON-NLS-1$
	private static final String PARAM_LANG = "lang"; //$NON-NLS-1$
	private static final String PROTOCOL_HTTP = "http"; //$NON-NLS-1$
	
	/*
	 * Constructs a new remote index provider, which listens for remote
	 * help preference changes.
	 */
	public RemoteIndexProvider() {
		RemoteHelp.addPreferenceChangeListener(new IPreferenceChangeListener() {
			public void preferenceChange(PreferenceChangeEvent event) {
				contentChanged();
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.AbstractIndexProvider#getIndexContributions(String)
	 */
	public IIndexContribution[] getIndexContributions(String locale) {
		if (RemoteHelp.isEnabled()) {
			List<IIndexContribution> contributions = new ArrayList<IIndexContribution>();
			PreferenceFileHandler handler = new PreferenceFileHandler();
			String isEnabled[] = handler.isEnabled();
			String [] protocol = handler.getProtocolEntries();
			String [] host = handler.getHostEntries();
			String [] port = handler.getPortEntries();
			String [] path = handler.getPathEntries();
			for (int ic = 0; ic < handler.getTotalRemoteInfocenters(); ic++) {
				if (isEnabled[ic].equalsIgnoreCase("true")) { //$NON-NLS-1$
					InputStream in = null;
					try {
						URL url;
						
						if(protocol[ic].equals(PROTOCOL_HTTP))
						{
							url = RemoteHelp.getURL(ic, PATH_INDEX + '?' + PARAM_LANG + '=' + locale);
							in = url.openStream();
						}
						else
						{
							url = HttpsUtility.getHttpsURL(protocol[ic], host[ic], port[ic], path[ic]+PATH_INDEX + '?' + PARAM_LANG + '=' + locale);
							in = HttpsUtility.getHttpsStream(url);
						}
						
						RemoteIndexParser parser = new RemoteIndexParser();
						IIndexContribution[] result = parser.parse(in);
						for (int contrib = 0; contrib < result.length; contrib++) {
							contributions.add(result[contrib]);
						}
					} catch (IOException e) {
						String msg = "I/O error while trying to contact the remote help server"; //$NON-NLS-1$
						HelpBasePlugin.logError(msg, e);
					} catch (Throwable t) {
						String msg = "Internal error while reading index contents from remote server"; //$NON-NLS-1$
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
			return (IIndexContribution[])contributions.toArray(new IIndexContribution[contributions.size()]);
		}
		return new IIndexContribution[0];
	}
}

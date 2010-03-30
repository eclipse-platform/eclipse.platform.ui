/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
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

import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.help.AbstractTocProvider;
import org.eclipse.help.ITocContribution;
import org.eclipse.help.internal.base.HelpBasePlugin;

/*
 * Provides the TOC data that is located on the remote infocenter, if the system
 * is configured for remote help. If not, returns no contributions.
 */
public class RemoteTocProvider extends AbstractTocProvider {

	private static final String PATH_TOC = "/toc"; //$NON-NLS-1$
	private static final String PROTOCOL = "http"; //$NON-NLS-1$
	private static final String PARAM_LANG = "lang"; //$NON-NLS-1$
	private static final String PROTOCOL_HTTPS = "https"; //$NON-NLS-1$

	/*
	 * Constructs a new remote toc provider, which listens for remote
	 * help preference changes.
	 */
	public RemoteTocProvider() {
		RemoteHelp.addPreferenceChangeListener(new IPreferenceChangeListener() {
			public void preferenceChange(PreferenceChangeEvent event) {
				contentChanged();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.AbstractTocProvider#getTocContributions(java.lang.String)
	 */
	public ITocContribution[] getTocContributions(String locale) {

		if (RemoteHelp.isEnabled()) {

			InputStream in = null;

			/*
			 * Loop through remote all the InfoCenters and get their TOCs.
			 * Combine the TOCs into an array of ITocContribution[]
			 */

			PreferenceFileHandler prefHandler = new PreferenceFileHandler();
			// myHandler.getHost
			RemoteTocParser parser = new RemoteTocParser();

			String host[] = prefHandler.getHostEntries();
			String port[] = prefHandler.getPortEntries();
			String path[] = prefHandler.getPathEntries();
			String protocol[] = prefHandler.getProtocolEntries();
			String isEnabled[] = prefHandler.isEnabled();

			ITocContribution[] currentContributions = new ITocContribution[0];
			ITocContribution[] temp = new ITocContribution[0];
			ITocContribution[] totalContributions = new ITocContribution[0];

			int numICs = host.length;
			if (numICs == 0) // No remote InfoCenters in preferences.ini
				return new ITocContribution[0];

			URL url = null;
			String urlStr = ""; //$NON-NLS-1$
			for (int i = numICs-1; i >= 0; i--) {
				if (isEnabled[i].equalsIgnoreCase("true")) { //$NON-NLS-1$
					try {
						
						if(protocol[i].equalsIgnoreCase(PROTOCOL))
						{
							url = new URL(protocol[i], host[i], new Integer(port[i]) .intValue(), 
									path[i] + PATH_TOC + '?' + PARAM_LANG + '=' + locale);
							
							in = url.openStream();
							urlStr = PROTOCOL + "://"+host[i] + ":" + port[i] + path[i]; //$NON-NLS-1$ //$NON-NLS-2$
						}
						else
						{
							in = HttpsUtility.getHttpsInputStream(protocol[i],host[i],port[i],path[i],locale);
							urlStr = PROTOCOL_HTTPS + "://"+host[i] + ":" + port[i] + path[i]; //$NON-NLS-1$ //$NON-NLS-2$
						}

						if (in != null) {
							// pass URL to parser
							currentContributions = parser.parse(in, urlStr);
							/*
							 * Save previous contributed tocs to a temp variable
							 */
							temp = new ITocContribution[totalContributions.length];
							System.arraycopy(totalContributions, 0, temp, 0,
									totalContributions.length);

							/*
							 * Combine current contributed tocs and previous
							 * contributed
							 */

							totalContributions = new ITocContribution[temp.length
									+ currentContributions.length];
							System.arraycopy(temp, 0, totalContributions, 0,
									temp.length);

							System.arraycopy(currentContributions, 0,
									totalContributions, temp.length,
									currentContributions.length);
						}
					} catch (Throwable t) {
				        String msg = "Internal error while reading TOC contents from remote server"; //$NON-NLS-1$
				        HelpBasePlugin.logError(msg, t);
				        RemoteHelp.setError(t);
					} finally {
						if (in != null) {
							try {
								in.close();
								in = null;
							} catch (IOException e) {
								// nothing more we can do
							}
						}
					}
				}
			}

			return totalContributions;

		}
		return new ITocContribution[0];
	}

	
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.AbstractTocProvider#getPriority()
	 */
	public int getPriority() {
		
		int helpOption=PreferenceFileHandler.getEmbeddedHelpOption();
		
		if(helpOption ==PreferenceFileHandler.LOCAL_HELP_ONLY || helpOption==PreferenceFileHandler.LOCAL_HELP_PRIORITY)
			return TOC_FILE_PRIORITY+1;
		else return DEFAULT_PRIORITY-1;
	}
}

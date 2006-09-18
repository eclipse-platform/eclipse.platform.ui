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
	private static final String PARAM_LANG = "lang"; //$NON-NLS-1$
	
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.AbstractTocProvider#getTocContributions(java.lang.String)
	 */
	public ITocContribution[] getTocContributions(String locale) {
		if (RemoteHelp.isEnabled()) {
			InputStream in = null;
			try {
				URL url = RemoteHelp.getURL(PATH_TOC+ '?' + PARAM_LANG + '=' + locale);
				in = url.openStream();
				RemoteTocParser parser = new RemoteTocParser();
				return parser.parse(in);
			}
			catch (IOException e) {
				String msg = "I/O error while trying to contact the remote help server"; //$NON-NLS-1$
				HelpBasePlugin.logError(msg, e);
				RemoteHelp.setError(e);
			}
			catch (Throwable t) {
				String msg = "Internal error while reading TOC contents from remote server"; //$NON-NLS-1$
				HelpBasePlugin.logError(msg, t);
				RemoteHelp.setError(t);
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
		return new ITocContribution[0];
	}
}

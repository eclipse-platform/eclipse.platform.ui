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

import org.eclipse.core.runtime.Preferences;
import org.eclipse.help.ITocContribution;
import org.eclipse.help.ITocProvider;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.base.IHelpBaseConstants;

/*
 * Provides the TOC data that is located on the remote infocenter, if the system
 * is configured for remote help. If not, returns no contributions.
 */
public class RemoteTocProvider implements ITocProvider {

	private static final String PROTOCOL_HTTP = "http"; //$NON-NLS-1$
	private static final String PATH_TOC = "/help/toc"; //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see org.eclipse.help.ITocProvider#getTocContributions(java.lang.String)
	 */
	public ITocContribution[] getTocContributions(String locale) {
		InputStream in = null;
		try {
			Preferences prefs = HelpBasePlugin.getDefault().getPluginPreferences();
			String host = prefs.getString(IHelpBaseConstants.P_KEY_REMOTE_HELP_SERVER_HOST);
			int port = prefs.getInt(IHelpBaseConstants.P_KEY_REMOTE_HELP_SERVER_PORT);
			if (host != null && host.length() > 0) {
				URL url = new URL(PROTOCOL_HTTP, host, port, PATH_TOC);
				in = url.openStream();
				RemoteTocParser parser = new RemoteTocParser();
				return parser.parse(in);
			}
		}
		catch (IOException e) {
			String msg = "I/O error while trying to contact the remote help server"; //$NON-NLS-1$
			HelpBasePlugin.logError(msg, e);
		}
		catch (Throwable t) {
			String msg = "Internal error while reading TOC contents from remote server"; //$NON-NLS-1$
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
		return new ITocContribution[0];
	}
}

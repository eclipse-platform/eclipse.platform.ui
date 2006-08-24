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
import java.net.HttpURLConnection;
import java.net.URL;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.help.IContext;
import org.eclipse.help.IContextProvider;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.base.IHelpBaseConstants;
import org.eclipse.help.internal.util.URLCoder;

/*
 * Provides the context-sensitive help data that is located on the remote
 * infocenter for a particular id, if the system is configured for remote help.
 * If not, returns null.
 */
public class RemoteContextProvider implements IContextProvider {

	private static final String PROTOCOL_HTTP = "http"; //$NON-NLS-1$
	private static final String PATH_CONTEXT = "/help/context?id="; //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see org.eclipse.help.IContextProvider#getContext(java.lang.Object)
	 */
	public IContext getContext(Object target) {
		if (target instanceof String) {
			String id = (String)target;
			InputStream in = null;
			try {
				Preferences prefs = HelpBasePlugin.getDefault().getPluginPreferences();
				String host = prefs.getString(IHelpBaseConstants.P_KEY_REMOTE_HELP_SERVER_HOST);
				int port = prefs.getInt(IHelpBaseConstants.P_KEY_REMOTE_HELP_SERVER_PORT);
				if (host != null && host.length() > 0) {
					URL url = new URL(PROTOCOL_HTTP, host, port, PATH_CONTEXT + URLCoder.encode(id));
					HttpURLConnection connection = (HttpURLConnection)url.openConnection();
					if (connection.getResponseCode() == 200) {
						in = connection.getInputStream();
						RemoteContextParser parser = new RemoteContextParser();
						return parser.parse(in);
					}
				}
			}
			catch (IOException e) {
				String msg = "I/O error while trying to contact the remote help server"; //$NON-NLS-1$
				HelpBasePlugin.logError(msg, e);
			}
			catch (Throwable t) {
				String msg = "Internal error while reading context-sensitive help data from remote server"; //$NON-NLS-1$
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
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.help.IContextProvider#getContextChangeMask()
	 */
	public int getContextChangeMask() {
		return NONE;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.help.IContextProvider#getSearchExpression(java.lang.Object)
	 */
	public String getSearchExpression(Object target) {
		return null;
	}
}

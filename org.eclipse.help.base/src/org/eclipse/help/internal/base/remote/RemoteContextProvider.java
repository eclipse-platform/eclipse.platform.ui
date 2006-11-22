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

import org.eclipse.help.AbstractContextProvider;
import org.eclipse.help.Node;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.dynamic.NodeReader;

/*
 * Provides the context-sensitive help data that is located on the remote
 * infocenter for a particular id, if the system is configured for remote help.
 * If not, returns null.
 */
public class RemoteContextProvider extends AbstractContextProvider {

	private static final String PATH_CONTEXT = "/context"; //$NON-NLS-1$
	private static final String PARAM_ID = "id"; //$NON-NLS-1$
	private static final String PARAM_LANG = "lang"; //$NON-NLS-1$

	private NodeReader reader;
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.AbstractContextProvider#getContext(java.lang.String, java.lang.String)
	 */
	public Node getContext(String id, String locale) {
		if (RemoteHelp.isEnabled()) {
			InputStream in = null;
			try {
				URL url = RemoteHelp.getURL(PATH_CONTEXT + '?' + PARAM_ID + '=' + id + '&' + PARAM_LANG + '=' + locale);
				HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				if (connection.getResponseCode() == 200) {
					in = connection.getInputStream();
					if (reader == null) {
						reader = new NodeReader();
						reader.setIgnoreWhitespaceNodes(true);
					}
					return reader.read(in);
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
	 * @see org.eclipse.help.AbstractContextProvider#getPlugins()
	 */
	public String[] getPlugins() {
		// this is a global provider
		return null;
	}
}

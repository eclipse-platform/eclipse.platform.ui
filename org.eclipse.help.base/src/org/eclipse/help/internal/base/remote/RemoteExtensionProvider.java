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
import org.eclipse.help.AbstractContentExtensionProvider;
import org.eclipse.help.Node;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.dynamic.NodeReader;

public class RemoteExtensionProvider extends AbstractContentExtensionProvider {

	private static final String PATH_EXTENSIONS = "/extension"; //$NON-NLS-1$

	private NodeReader reader;

	public RemoteExtensionProvider() {
		RemoteHelp.addPreferenceChangeListener(new IPreferenceChangeListener() {
			public void preferenceChange(PreferenceChangeEvent event) {
				contentChanged();
			}
		});
	}
	
	public Node[] getContentExtensions(String locale) {
		if (RemoteHelp.isEnabled()) {
			InputStream in = null;
			try {
				URL url = RemoteHelp.getURL(PATH_EXTENSIONS);
				in = url.openStream();
				if (reader == null) {
					reader = new NodeReader();
				}
				Node node = reader.read(in);
				return node.getChildNodes();
			}
			catch (IOException e) {
				String msg = "I/O error while trying to contact the remote help server"; //$NON-NLS-1$
				HelpBasePlugin.logError(msg, e);
			}
			catch (Throwable t) {
				String msg = "Internal error while reading topic extensions from remote server"; //$NON-NLS-1$
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
		return new Node[0];
	}
}

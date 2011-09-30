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
import org.eclipse.help.AbstractContentExtensionProvider;
import org.eclipse.help.IContentExtension;
import org.eclipse.help.internal.UAElement;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.dynamic.DocumentReader;

public class RemoteExtensionProvider extends AbstractContentExtensionProvider {

	private static final String PATH_EXTENSIONS = "/extension"; //$NON-NLS-1$
	private static final String PROTOCOL_HTTP = "http"; //$NON-NLS-1$
	
	private DocumentReader reader;

	public RemoteExtensionProvider() {
		RemoteHelp.addPreferenceChangeListener(new IPreferenceChangeListener() {
			public void preferenceChange(PreferenceChangeEvent event) {
				contentChanged();
			}
		});
	}
	
	public IContentExtension[] getContentExtensions(String locale) {
		if (RemoteHelp.isEnabled()) {
			List<IContentExtension> contributions = new ArrayList<IContentExtension>();
			PreferenceFileHandler handler = new PreferenceFileHandler();
			String isEnabled[] = handler.isEnabled();
			for (int ic = 0; ic < handler.getTotalRemoteInfocenters(); ic++) {
				if (isEnabled[ic].equalsIgnoreCase("true")) { //$NON-NLS-1$
					InputStream in = null;
					try {
						URL url = RemoteHelp.getURL(ic, PATH_EXTENSIONS);
						
						if(url.getProtocol().equalsIgnoreCase(PROTOCOL_HTTP))
						{
							in = url.openStream();
						}
						else
						{
							in = HttpsUtility.getHttpsStream(url);
						}
						
						if (reader == null) {
							reader = new DocumentReader();
						}
						UAElement element = reader.read(in);
						IContentExtension[] children = (IContentExtension[]) element
								.getChildren(IContentExtension.class);
						for (int contrib = 0; contrib < children.length; contrib++) {
							contributions.add(children[contrib]);
						}
					} catch (IOException e) {
						String msg = "I/O error while trying to contact the remote help server"; //$NON-NLS-1$
						HelpBasePlugin.logError(msg, e);
					} catch (Throwable t) {
						String msg = "Internal error while reading topic extensions from remote server"; //$NON-NLS-1$
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
			return contributions.toArray(new IContentExtension[contributions.size()]);
		}
		return new IContentExtension[0];
	}
}

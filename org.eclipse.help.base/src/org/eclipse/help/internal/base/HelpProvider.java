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
package org.eclipse.help.internal.base;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.help.internal.HelpPlugin.IHelpProvider;
import org.eclipse.help.internal.protocols.HelpURLStreamHandler;

/*
 * Provides help document content via the internal application server.
 * The org.eclipse.help plugin should not make any assumptions about
 * where the content comes from.
 */
public class HelpProvider implements IHelpProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.help.internal.HelpPlugin.IHelpProvider#getHelpContent(java.lang.String, java.lang.String)
	 */
	public InputStream getHelpContent(String href, String locale) {
		try {
			URL helpURL = new URL("help", //$NON-NLS-1$
					null, -1, href + "?lang=" + locale, HelpURLStreamHandler.getDefault()); //$NON-NLS-1$
			return helpURL.openStream();
		} catch (IOException ioe) {
			return null;
		}
	}
}

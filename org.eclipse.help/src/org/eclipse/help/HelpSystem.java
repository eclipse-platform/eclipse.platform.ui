/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help;

import java.io.*;
import java.net.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.protocols.*;

/**
 * This class provides general access to help content contributed to the
 * <code>"org.eclipse.help.toc"</code> and
 * <code>"org.eclipse.help.contexts"</code> extension points.
 * <p>
 * This class provides static methods only; it is not intended to be
 * instantiated or subclassed.
 * </p>
 * 
 * @since 3.0
 */
public final class HelpSystem {

	private static boolean fShared;
	
	/**
	 * This class is not intended to be instantiated.
	 */
	private HelpSystem() {
		// do nothing
	}

	/**
	 * Computes and returns context information for the given context id.
	 * 
	 * @param contextId
	 *            the context id
	 * @return the context, or <code>null</code> if none
	 */
	public static IContext getContext(String contextId) {
		return HelpPlugin.getContextManager().getContext(contextId);
	}

	/**
	 * Returns the list of all integrated tables of contents available. Each
	 * entry corresponds of a different help "book".
	 * 
	 * @return an array of TOC's
	 */
	public static IToc[] getTocs() {
		return HelpPlugin.getTocManager().getTocs(Platform.getNL());
	}

	/**
	 * Returns an open input stream on the contents of the specified help
	 * resource. The client is responsible for closing the stream when finished.
	 * 
	 * @param href
	 *            the URL (as a string) of the help resource
	 *            <p>
	 *            Valid href are as described in
	 *            {@link  org.eclipse.help.IHelpResource#getHref IHelpResource.getHref}
	 *            </p>
	 * @return an input stream containing the contents of the help resource, or
	 *         <code>null</code> if the help resource could not be found and
	 *         opened
	 */
	public static InputStream getHelpContent(String href) {
		try {
			// URL helpURL = new URL("help:" + href);
			URL helpURL = new URL("help", //$NON-NLS-1$
					null, -1, href, HelpURLStreamHandler.getDefault());

			return helpURL.openStream();
		} catch (IOException ioe) {
			return null;
		}
	}

	/**
	 * Returns whether or not the help system, in its current mode of operation,
	 * can be shared by multiple (potentially remote) users. This is a hint to
	 * the help system implementation that it should not perform operations that
	 * are specific to the help system's local environment.
	 * 
	 * <p>
	 * For example, when <code>true</code>, the default dynamic content producer
	 * implementation will not perform any filtering based on local system
	 * properties such as operating system or activities.
	 * </p>
	 * <p>
	 * If you are providing your own help implementation that is shared, you
	 * must notify the platform on startup by calling <code>setShared(true)</code>.
	 * </p>
	 * 
	 * @return whether or not the help system can be shared by multiple users
	 * @since 3.2
	 */
	public static boolean isShared() {
		return fShared;
	}
	
	/**
	 * Sets whether or not the help system, in its current mode of operation,
	 * can be shared by multiple (potentially remote) users. This is a hint to
	 * the help system implementation that it should not perform operations that
	 * are specific to the help system's local environment.
	 * 
	 * <p>
	 * By default the help system is flagged as not shared. If you are providing 
	 * your own help implementation that is shared, you must call this on startup
	 * with the parameter <code>true</code>.
	 * </p>
	 * 
	 * @param shared whether or not the help system can be shared by multiple users
	 */
	public static void setShared(boolean shared) {
		fShared = shared;
	}
}

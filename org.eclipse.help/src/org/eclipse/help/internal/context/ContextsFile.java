/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.context;
import java.io.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.util.*;

public class ContextsFile {
	protected String href;
	protected String definingPluginID;
	protected String pluginID;
	/**
	 * Contexts File Constructor
	 */
	protected ContextsFile(String definingPlugin, String href, String plugin) {
		this.href = href;
		this.definingPluginID = definingPlugin;
		this.pluginID = plugin;
	}
	/**
	 * Gets the href
	 * 
	 * @return Returns a String
	 */
	protected String getHref() {
		return href;
	}
	protected InputStream getInputStream() {
		InputStream stream = null;
		try {
			if (definingPluginID != null)
				stream = ResourceLocator.openFromPlugin(definingPluginID, href,
						Platform.getNL());
			else
				stream = new FileInputStream(href);
		} catch (IOException e) {
		}
		return stream;
	}
	/**
	 * Gets the definingPluginID.
	 * 
	 * @return Returns a String
	 */
	public String getDefiningPluginID() {
		return definingPluginID;
	}
	/**
	 * Gets the plugin ID.
	 * 
	 * @return Returns a String
	 */
	public String getPluginID() {
		return pluginID;
	}
	public void build(ContextsBuilder builder) {
		builder.build(this);
	}
}

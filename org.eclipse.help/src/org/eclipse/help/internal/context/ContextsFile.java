/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.context;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.help.internal.util.ResourceLocator;

public class ContextsFile {
	protected String href;
	protected String definingPluginID;
	protected String pluginID;
	/**
	 * Contexts File Constructor
	 */
	public ContextsFile(String definingPlugin, String href, String plugin) {
		this.href = href;
		this.definingPluginID = definingPlugin;
		this.pluginID = plugin;
	}
	/**
	 * Gets the href
	 * 
	 * @return Returns a String
	 */
	public String getHref() {
		return href;
	}
	protected InputStream getInputStream(String locale) {
		InputStream stream = null;
		try {
			if (definingPluginID != null)
				stream = ResourceLocator.openFromPlugin(definingPluginID, href,	locale);
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
	public void build(ContextsBuilder builder, String locale) {
		builder.build(this, locale);
	}
}

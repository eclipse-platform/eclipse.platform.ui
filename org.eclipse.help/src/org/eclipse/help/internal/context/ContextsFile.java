/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.context;
import java.io.*;
import org.eclipse.help.internal.util.ResourceLocator;
public class ContextsFile {
	protected String href;
	protected String definingPluginID;
	protected String pluginID;
	protected String locale;
	/**
	 * Contexts File Constructor
	 */
	protected ContextsFile(String definingPlugin, String href, String plugin, String locale) {
		this.href = href;
		this.definingPluginID = definingPlugin;
		this.pluginID = plugin;
		this.locale=locale;
	}
	/**
	 * Gets the href
	 * @return Returns a String
	 */
	protected String getHref() {
		return href;
	}
	protected InputStream getInputStream() {
		InputStream stream = null;
		try {
			if (definingPluginID != null)
				stream = ResourceLocator.openFromPlugin(definingPluginID, href, locale);
			else
				stream = new FileInputStream(href);
		} catch (IOException e) {
		}
		return stream;
	}
	/**
	* Gets the definingPluginID.
	* @return Returns a String
	*/
	public String getDefiningPluginID() {
		return definingPluginID;
	}
	/**
	 * Gets the plugin ID.
	 * @return Returns a String
	 */
	public String getPluginID() {
		return pluginID;
	}
	public void build(ContextsBuilder builder) {
		builder.build(this);
	}
}
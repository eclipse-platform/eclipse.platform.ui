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
package org.eclipse.help.internal.toc;
import java.io.*;
import java.util.*;

import org.eclipse.help.internal.util.*;

public class TocFile {

	protected Toc toc;

	protected String plugin;
	protected String href;
	protected boolean primary;
	protected String locale;
	protected String extraDir;

	// used for fast access to anchors
	protected Map anchors;

	/**
	 * Toc File Constructor
	 */
	protected TocFile(String plugin, String href, boolean primary,
			String locale, String extraDir) {
		this.plugin = plugin;
		this.href = href;
		this.primary = primary;
		this.locale = locale;
		this.extraDir = extraDir;
	}

	/**
	 * Gets the href
	 * 
	 * @return Returns a String
	 */
	protected String getHref() {
		return href;
	}

	/**
	 * Gets the pluginID
	 * 
	 * @return Returns a String
	 */
	public final String getPluginID() {
		return plugin;
	}

	protected InputStream getInputStream() {
		InputStream stream = null;
		try {
			if (plugin != null)
				stream = ResourceLocator.openFromPlugin(plugin, href, locale);
			else
				stream = new FileInputStream(href);
		} catch (IOException e) {
		}
		return stream;
	}

	/**
	 * Parses file and gets the toc
	 * 
	 * @return Returns a Toc
	 */
	public Toc getToc() {
		return toc;
	}

	/**
	 * Sets the toc on this file. It should happen during parsing
	 */
	public void setToc(Toc toc) {
		this.toc = toc;
	}

	/**
	 * Registers a new anchor.
	 */
	public void addAnchor(Anchor a) {
		if (anchors == null)
			anchors = new HashMap();

		anchors.put(a.getID(), a);
	}

	/**
	 * Returns anchor by id
	 */
	public Anchor getAnchor(String id) {
		if (anchors == null || anchors.get(id) == null)
			return null;
		else
			return (Anchor) anchors.get(id);
	}

	/**
	 * Builds the toc file if needed
	 */
	public void build(TocBuilder builder) {
		builder.buildTocFile(this);
	}

	/**
	 * Used by debugger
	 */
	public String toString() {
		return plugin + "/" + href; //$NON-NLS-1$
	}
	/**
	 * Checks if this file specifies a TOC.
	 * 
	 * @return Returns a boolean
	 */
	public boolean isPrimary() {
		return primary;
	}

	/**
	 * Gets the extraDir.
	 * 
	 * @return Returns a String
	 */
	public String getExtraDir() {
		return extraDir;
	}
	/**
	 * Gets the locale.
	 * 
	 * @return Returns a String
	 */
	public String getLocale() {
		return locale;
	}
}

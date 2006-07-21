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
package org.eclipse.help.internal.toc;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.help.internal.util.ResourceLocator;

/*
 * A TocFile represents an XML toc file contributed via the toc extension point.
 */
public class TocFile {

	private String pluginId;
	private String file;
	private boolean isPrimary;
	private String locale;
	private String extraDir;
	private String category;
	
	public TocFile(String pluginId, String file, boolean isPrimary, String locale, String extradir, String category) {
		this.pluginId = pluginId;
		this.file = file;
		this.isPrimary = isPrimary;
		this.locale = locale;
		this.extraDir = extradir;
		this.category = category;
	}
	
	public String getCategory() {
		return category;
	}

	public String getExtraDir() {
		return extraDir;
	}

	public String getFile() {
		return file;
	}

	public InputStream getInputStream() throws IOException {
		if (pluginId != null) {
			return ResourceLocator.openFromPlugin(pluginId, file, locale);
		}
		else {
			return new FileInputStream(file);
		}
	}
	
	public String getLocale() {
		return locale;
	}

	public String getPluginId() {
		return pluginId;
	}

	public boolean isPrimary() {
		return isPrimary;
	}
}

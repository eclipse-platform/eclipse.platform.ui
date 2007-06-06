/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.configurator;

import java.net.*;


public class BootDescriptor {
	private String id;
	private String version;
	private String[] libs;
	private URL dir;

	public BootDescriptor(String id, String version, String[] libs, URL dir) {
		this.id = id;
		this.version = version;
		this.libs = libs;
		this.dir = dir;
	}

	public String getId() {
		return id;
	}

	public String getVersion() {
		return version;
	}

	public String[] getLibraries() {
		return libs;
	}

	public URL getPluginDirectoryURL() {
		return dir;
	}
}

/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.configurator;

import java.net.*;

import org.eclipse.update.configurator.*;


public class FeatureEntry implements IPlatformConfiguration.IFeatureEntry {
	private String id;
	private String version;
	private String pluginVersion;
	private String application;
	private URL[] root;
	private boolean primary;
	private String pluginIdentifier;

	public FeatureEntry(String id, String version, String pluginIdentifier, String pluginVersion, boolean primary, String application, URL[] root) {
		if (id == null)
			throw new IllegalArgumentException();
		this.id = id;
		this.version = version;
		this.pluginVersion = pluginVersion;
		this.pluginIdentifier = pluginIdentifier;
		this.primary = primary;
		this.application = application;
		this.root = (root == null ? new URL[0] : root);
	}

	public FeatureEntry( String id, String version, String pluginVersion, boolean primary, String application, URL[] root) {
		this(id, version, id, pluginVersion, primary, application, root);
	}

	/*
	 * @see IFeatureEntry#getFeatureIdentifier()
	 */
	public String getFeatureIdentifier() {
		return id;
	}

	/*
	 * @see IFeatureEntry#getFeatureVersion()
	 */
	public String getFeatureVersion() {
		return version;
	}

	/*
	 * @see IFeatureEntry#getFeaturePluginVersion()
	 */
	public String getFeaturePluginVersion() {
		return pluginVersion;
	}

	/*
	 * @see IFeatureEntry#getFeatureApplication()
	 */
	public String getFeatureApplication() {
		return application;
	}

	/*
	 * @see IFeatureEntry#getFeatureRootURLs()
	 */
	public URL[] getFeatureRootURLs() {
		return root;
	}

	/*
	 * @see IFeatureEntry#canBePrimary()
	 */
	public boolean canBePrimary() {
		return primary;
	}
	/*
	 * @see IFeatureEntry#getFeaturePluginIdentifier()
	 */
	public String getFeaturePluginIdentifier() {
		return pluginIdentifier;
	}

}
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
package org.eclipse.update.internal.configurator;

import java.net.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.configurator.*;
import org.osgi.framework.*;
import org.w3c.dom.*;


public class FeatureEntry implements IPlatformConfiguration.IFeatureEntry, IConfigurationConstants, IBundleGroup {
	private String id;
	private String version;
	private String pluginVersion;
	private String application;
	private URL[] root;
	private boolean primary;
	private String pluginIdentifier;
	private String url;

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

	/**
	 * Sets the url string (relative to the site url)
	 * @param url
	 */
	public void setURL(String url) {
		this.url = url;
	}
	
	/**
	 * @return the feature url (relative to the site): features/org.eclipse.platform/
	 */
	public String getURL() {
//		if (url == null)
//			url = FEATURES + "/" + id + "_" + version + "/";
		return url;
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

	public Element toXML(Document doc) {
	
		Element featureElement = doc.createElement(CFG_FEATURE_ENTRY);		
		// write out feature entry settings
		if (getFeatureIdentifier() != null)
			featureElement.setAttribute(CFG_FEATURE_ENTRY_ID, getFeatureIdentifier()); 
		if (canBePrimary())
			featureElement.setAttribute(CFG_FEATURE_ENTRY_PRIMARY, "true");
		if (getFeatureVersion() != null)
			featureElement.setAttribute(CFG_FEATURE_ENTRY_VERSION, getFeatureVersion()); 
		if (getFeaturePluginVersion() != null && !getFeaturePluginVersion().equals(getFeatureVersion()) && getFeaturePluginVersion().length() > 0)
			featureElement.setAttribute(CFG_FEATURE_ENTRY_PLUGIN_VERSION, getFeaturePluginVersion()); 
		if (getFeaturePluginIdentifier() != null && !getFeaturePluginIdentifier().equals(getFeatureIdentifier()))
			featureElement.setAttribute(CFG_FEATURE_ENTRY_PLUGIN_IDENTIFIER, getFeaturePluginIdentifier());
		if (getFeatureApplication() != null)
			featureElement.setAttribute(CFG_FEATURE_ENTRY_APPLICATION, getFeatureApplication());
		if (getURL() != null)
			featureElement.setAttribute(CFG_URL, getURL());
		
		URL[] roots = getFeatureRootURLs();
		for (int i=0; i<roots.length; i++) {
			String root = roots[i].toExternalForm();
			if (root != null && root.trim().length() > 0){
				Element rootElement = doc.createElement(CFG_FEATURE_ENTRY_ROOT);
				rootElement.appendChild(doc.createTextNode(root));
				featureElement.appendChild(rootElement);
			}
		}
		
		return featureElement;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IBundleGroup#getBundles()
	 */
	public Bundle getBundles() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IBundleGroup#getDescription()
	 */
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IBundleGroup#getIdentifier()
	 */
	public String getIdentifier() {
		return id;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IBundleGroup#getName()
	 */
	public String getName() {
		return id + "_" + version;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IBundleGroup#getProperty(java.lang.String)
	 */
	public String getProperty(String key) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IBundleGroup#getProviderName()
	 */
	public String getProviderName() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IBundleGroup#getVersion()
	 */
	public String getVersion() {
		return version;
	}
}
package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.net.URL;
 
public interface IFeature extends IPluginContainer {

	VersionedIdentifier getIdentifier();
	
	ISite getSite();
	
	String getLabel();
	URL getUpdateURL();
	URL getInfoURL();
	URL [] getDiscoveryURLs();
	String getProvider();
	String getDescription();
	boolean isExecutable();
	boolean isInstallable();
}


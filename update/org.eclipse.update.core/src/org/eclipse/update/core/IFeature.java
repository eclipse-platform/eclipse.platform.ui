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
	URL getURL();
	IInfo getUpdateInfo();
	URL getInfoURL();
	IInfo [] getDiscoveryInfos();
	String getProvider();
	IInfo getDescription();
	IInfo getCopyright();
	IInfo getLicense();
	
	boolean isExecutable();
	boolean isInstallable();

	/**
	 * returns a list of *bundles*	that compose teh feature
	 */
	String[] getContentReferences();
	
	/**
	 * install yourself into another feature.
	 */
	void install(IFeature targetFeature);
}


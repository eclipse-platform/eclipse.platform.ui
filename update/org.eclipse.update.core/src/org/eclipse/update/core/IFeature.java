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
	
	/**
	 * The URL that points at the Feature 
	 */
	URL getURL();
	IInfo getUpdateInfo();
	IInfo [] getDiscoveryInfos();
	String getProvider();
	IInfo getDescription();
	IInfo getCopyright();
	IInfo getLicense();
	ICategory[] getCategories();
	String getOS();
	String getWS();
	String getNL();
	URL getImage();
	
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


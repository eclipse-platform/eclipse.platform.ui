package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.net.URL;
import org.eclipse.core.runtime.CoreException;
 
public interface IFeature extends IPluginContainer {

	VersionedIdentifier getIdentifier() throws CoreException ;
	
	ISite getSite();
	
	String getLabel() throws CoreException ;
	
	/**
	 * The URL that points at the Feature 
	 */
	URL getURL();
	IInfo getUpdateInfo() throws CoreException ;
	IInfo [] getDiscoveryInfos() throws CoreException ;
	String getProvider() throws CoreException ;
	IInfo getDescription() throws CoreException ;
	IInfo getCopyright() throws CoreException ;
	IInfo getLicense() throws CoreException ;
	ICategory[] getCategories() throws CoreException ;
	String getOS() throws CoreException ;
	String getWS() throws CoreException ;
	String getNL() throws CoreException ;
	URL getImage() throws CoreException ;
	
	boolean isExecutable();
	boolean isInstallable();

	/**
	 * returns a list of *bundles*	that compose teh feature
	 */
	String[] getContentReferences() throws CoreException ;
	
	/**
	 * install yourself into another feature.
	 */
	void install(IFeature targetFeature) throws CoreException ;
}


package org.eclipse.core.runtime;

import java.net.URL;
import org.osgi.framework.Bundle;

public interface IBundleGroup {

	/**
	 * Returns the identifier of this bundle group.  Bundle groups are uniquely identified by the combination of
	 * their identifier and their version.
	 * @see getVersion()
	 * @return the identifier for this bundle group
	 */
	public String getIdentifier();

	/**
	 * Returns the human-readable name of this bundle group.
	 * @return the human-readable name
	 */
	public String getName();

	/**
	 * Returns the version of this bundle group. Bundle group version strings have the same format as 
	 * bundle versions (i.e., major.minor.service.qualifier).  Bundle groups are uniquely identified 
	 * by the combination of their identifier and their version.
	 * @see getIdentifier
	 * @return the string form of this bundle group's version
	 */
	public String getVersion();
	/**
	 * Returns the resource path to this bundle group's about image file.
	 * @return path to this bundle group's about image file or null if none
	 */
	public URL getAboutImage();
	/**
	 * Returns the resource path to this bundle group's window image file.
	 * @return path to this bundle group's window image file or null if none
	 */
	public URL getWindowImage();
	/**
	 * Returns a text description of this bundle group
	 * @return text description of this bundle group
	 */
	public String getDescription();
	
	/**
	 * Returns the name of the provider of this bundle group.
	 * @return the name of the provider or null if none
	 */
	public String getProviderName();
	
	/**
	 * Returns a list of all bundles supplied by this bundle group.  
	 * @return the bundles supplied by this bundle group
	 */
	public Bundle getBundles();

	/**
	 * Returns the URL string of the welcome page to use for this bundle group.  Note 
	 * that this if for legacy support. Eclipse 3.0 or later systems should use the 
	 * facilities provided by the org.eclipse.ui.intro extension point.
	 * @return the URL string of this bundle group's welcome page or null if none
	 */
	public String getWelcomePage();
	
	/**
	 * Returns a <code>String</code> for the tips and trick href.
	 * 
	 * @return the tips and tricks href, or <code>null</code> if none
	 */
	public String getTipsAndTricksHref();
}

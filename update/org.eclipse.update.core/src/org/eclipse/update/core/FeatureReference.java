package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.model.FeatureReferenceModel;
import org.eclipse.update.core.model.SiteModel;
import org.eclipse.update.internal.core.*;

/**
 * Convenience implementation of a feature reference.
 * <p>
 * This class may be instantiated or subclassed by clients.
 * </p> 
 * @see org.eclipse.update.core.IFeatureReference
 * @see org.eclipse.update.core.model.FeatureReferenceModel
 * @since 2.0
 */
public class FeatureReference extends FeatureReferenceModel implements IFeatureReference, IPlatformEnvironment {

	private List categories;
	private VersionedIdentifier versionId;
	
	//PERF: new instance variable
	private IFeature exactFeature;
 
	/**
	 * Feature reference default constructor
	 */
	public FeatureReference() {
		super();
	}

	/**
	 * Constructor FeatureReference.
	 * @param ref the reference to copy
	 */
	public FeatureReference(IFeatureReference ref) {
		super((FeatureReferenceModel) ref);
		try {
			setURL(ref.getURL());
		} catch (CoreException e) {
			UpdateManagerPlugin.warn("", e);
		}
	}

	/**
	 * Constructor FeatureReference.
	 * @param ref the reference to copy
	 */
	public FeatureReference(FeatureReferenceModel ref) {
		super(ref);
		try {
			setURL(ref.getURL());
		} catch (CoreException e) {
			UpdateManagerPlugin.warn("", e);
		}
	}
	
	/**
	 * Returns the feature this reference points to 
	 *  @return the feature on the Site
	 */
	public IFeature getFeature() throws CoreException {
		
		if (exactFeature!=null)	
			return exactFeature;
		exactFeature = getFeature(this);
		return exactFeature;
	}


	/**
	 * Returns the feature the reference points to 
	 * @param the feature reference
	 * @return the feature on the Site
	 */
	protected IFeature getFeature(IFeatureReference ref) throws CoreException {

		IFeature feature = null;
		String type = getType();
		if (type == null || type.equals("")) { //$NON-NLS-1$
			// ask the Site for the default type
			type = getSite().getDefaultPackagedFeatureType();
		}
		feature = getSite().createFeature(type, ref.getURL());
		return feature;
	}

	/**
	 * Returns the update site for the referenced feature
	 * 
	 * @see IFeatureReference#getSite()
	 * @since 2.0 
	 */
	public ISite getSite() {
		return (ISite) getSiteModel();
	}
	
	/** 
	 * Sets the feature reference URL.
	 * This is typically performed as part of the feature reference creation
	 * operation. Once set, the url should not be reset.
	 * 
	 * @see IFeatureReference#setURL(URL)
	 * @since 2.0 
	 */
	public void setURL(URL url) throws CoreException {
		if (url != null) {
			setURLString(url.toExternalForm());
			try {
				resolve(url, null);
			} catch (MalformedURLException e) {
				throw Utilities.newCoreException(Policy.bind("FeatureReference.UnableToResolveURL", url.toExternalForm()), e);
				//$NON-NLS-1$
			}
		}
	}

	/**
	 * Associates a site with the feature reference.
	 * This is typically performed as part of the feature reference creation
	 * operation. Once set, the site should not be reset.
	 * 
	 * @see IFeatureReference#setSite(ISite)
	 * @since 2.0 
	 */
	public void setSite(ISite site) {
		setSiteModel((SiteModel) site);
	}

	/**
	* Returns the feature identifier.
	* 
	* @see IFeatureReference#getVersionedIdentifier()
	* @since 2.0
	*/
	public VersionedIdentifier getVersionedIdentifier() {

		if (versionId != null)
			return versionId;

		String id = getFeatureIdentifier();
		String ver = getFeatureVersion();
		if (id != null && ver != null) {
			try {
				versionId = new VersionedIdentifier(id, ver);
				return versionId;
			} catch (Exception e) {
				UpdateManagerPlugin.warn("Unable to create versioned identifier:" + id + ":" + ver);
			}
		}

		// we need the exact match or we may have an infinite loop
		versionId = new VersionedIdentifier(getURL().toExternalForm(),null);
		try {
			versionId = getFeature().getVersionedIdentifier();
		} catch (CoreException e){
			UpdateManagerPlugin.warn("",e);
		}
		return versionId;
	}
	
	
	/**
	 * @see org.eclipse.update.core.IFeatureReference#getName()
	 */
	public String getName() {
		if (super.getLabel()!=null) return super.getLabel();
		try {
			return getFeature().getLabel();
		} catch (CoreException e){
			return getVersionedIdentifier().toString();
		}
	}

	/**
	 * Get optional operating system specification as a comma-separated string.
	 *
	 * @see org.eclipse.core.boot.BootLoader
	 * @return the operating system specification string, or <code>null</code>.
	 * @since 2.1
	 */
	public String getOS() {
		if (super.getOS()==null) try {
			return getFeature().getOS();
		} catch (CoreException e) {
			return null;
		}
		return super.getOS();
	}



	/**
	 * Get optional windowing system specification as a comma-separated string.
	 *
	 * @see org.eclipse.core.boot.BootLoader
	 * @return the windowing system specification string, or <code>null</code>.
	 * @since 2.1
	 */
	public String getWS() {
		if (super.getWS()==null) try {
			return getFeature().getWS();
		} catch (CoreException e) {
			return null;
		}
		return super.getWS();
	}



	/**
	 * Get optional system architecture specification as a comma-separated string.
	 *
	 * @see org.eclipse.core.boot.BootLoader
	 * @return the system architecture specification string, or <code>null</code>.
	 * @since 2.1
	 */
	public String getOSArch() {
		if (super.getOSArch()==null) try {
			return getFeature().getOSArch();
		} catch (CoreException e) {
			return null;
		}
		return super.getOSArch();
	}



	/**
	 * Get optional locale specification as a comma-separated string.
	 *
	 * @return the locale specification string, or <code>null</code>.
	 * @since 2.1
	 */
	public String getNL() {
		if(super.getNL()==null) try {
			return getFeature().getNL();
		} catch (CoreException e) {
			return null;
		}
		return super.getNL(); 
	}

	/**
	 * Returns <code>true</code> if this feature is patching another feature,
	 * <code>false</code> otherwise
	 * @return boolean
	 */
	public boolean isPatch() {
		if (super.getPatch()==null) try {
			return getFeature().isPatch();
		} catch (CoreException e) {
			return false;
		}
		return "true".equalsIgnoreCase(super.getPatch());
	}



}
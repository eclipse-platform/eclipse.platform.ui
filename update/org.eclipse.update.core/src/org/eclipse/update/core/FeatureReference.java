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
package org.eclipse.update.core;

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
			UpdateCore.warn("", e);
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
			UpdateCore.warn("", e);
		}
	}

	/**
	 * Returns the feature this reference points to 
	 * @return the feature on the Site
	 * @deprecated
	 */
	public IFeature getFeature() throws CoreException {
		return getFeature(null);
	}

	/**
	 * Returns the feature this reference points to 
	 *  @return the feature on the Site
	 */
	public IFeature getFeature(IProgressMonitor monitor) throws CoreException {

		if (exactFeature != null)
			return exactFeature;
		exactFeature = getFeature(this,monitor);
		return exactFeature;
	}

	/**
	 * Returns the feature the reference points to 
	 * @param the feature reference
	 * @return the feature on the Site
	 */
	protected IFeature getFeature(IFeatureReference ref,IProgressMonitor monitor) throws CoreException {

		IFeature feature = null;
		URL refURL = ref.getURL();
		feature = createFeature(refURL,monitor);
		return feature;
	}

	/*
	 * create an instance of a concrete feature corresponding to this reference
	 */
	private IFeature createFeature(URL url,IProgressMonitor monitor) throws CoreException {
		String type = getType();
		ISite site = getSite();
		// if the site exists, use the site factory
		if (site != null) {
			return site.createFeature(type, url, monitor);
		}
		
		IFeatureFactory factory = FeatureTypeFactory.getInstance().getFactory(type);
		return factory.createFeature(url, site, monitor);
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
				UpdateCore.warn("Unable to create versioned identifier:" + id + ":" + ver);
			}
		}

		// we need the exact match or we may have an infinite loop
		versionId = new VersionedIdentifier(getURL().toExternalForm(), null);
		try {
			versionId = getFeature().getVersionedIdentifier();
		} catch (CoreException e) {
			UpdateCore.warn("", e);
		}
		return versionId;
	}

	/**
	 * @see org.eclipse.update.core.IFeatureReference#getName()
	 */
	public String getName() {
		if (super.getLabel() != null)
			return super.getLabel();
		try {
			return getFeature().getLabel();
		} catch (CoreException e) {
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
		if (super.getOS() == null && getURL()!=null)
			try {
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
		if (super.getWS() == null && getURL()!=null)
			try {
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
		if (super.getOSArch() == null && getURL()!=null)
			try {
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
		if (super.getNL() == null && getURL()!=null)
			try {
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
		if (super.getPatch() == null)
			try {
				return getFeature().isPatch();
			} catch (CoreException e) {
				return false;
			}
		return "true".equalsIgnoreCase(super.getPatch());
	}

}

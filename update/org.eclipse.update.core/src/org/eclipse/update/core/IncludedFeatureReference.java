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

import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.model.*;
import org.eclipse.update.internal.core.*;


/**
 * This is a utility class representing the options of a nested feature.
 * Feature will include other features. This class will represent the options of the inclusion.
 * <p>
 * Clients may instantiate; not intended to be subclassed by clients.
 * </p> 
 * @see org.eclipse.update.core.VersionedIdentifier
 * @since 2.0.1
 */
public class IncludedFeatureReference extends IncludedFeatureReferenceModel implements IIncludedFeatureReference {
	
	private IFeature bestMatchFeature;	 

	/**
	 * Construct a included feature reference
	 * 
	 * @since 2.1
	 */
	public IncludedFeatureReference() {
		super();
	}


	/**
	 * Construct a feature options 
	 * 
	 * @param name string representation of the feature
	 * @param isOptional <code>true</code> if the feature is optional, <code>false</code> otherwise.
	 * @param matchingRule the matching rule
	 * @param searchLocation the location to search for this feature's updates.
	 * @since 2.0.2
	 */
	public IncludedFeatureReference(IIncludedFeatureReference includedFeatureRef) {
		super((IncludedFeatureReferenceModel)includedFeatureRef);
	}

	/**
	 * Constructor IncludedFeatureReference.
	 * @param iFeatureReference
	 */
	public IncludedFeatureReference(IFeatureReference featureReference) {
		super(featureReference);
	}


	/**
	* Method matches.
	* @param identifier
	* @param id
	* @param options
	* @return boolean
	*/
	private boolean matches(VersionedIdentifier baseIdentifier, VersionedIdentifier id) {
		if (baseIdentifier == null || id == null)
			return false;
		if (!id.getIdentifier().equals(baseIdentifier.getIdentifier()))
			return false;

		switch (getMatch()) {
			case IImport.RULE_PERFECT :
				return id.getVersion().isPerfect(baseIdentifier.getVersion());
			case IImport.RULE_COMPATIBLE :
				return id.getVersion().isCompatibleWith(baseIdentifier.getVersion());
			case IImport.RULE_EQUIVALENT :
				return id.getVersion().isEquivalentTo(baseIdentifier.getVersion());
			case IImport.RULE_GREATER_OR_EQUAL :
				return id.getVersion().isGreaterOrEqualTo(baseIdentifier.getVersion());
		}
		UpdateCore.warn("Unknown matching rule:" + getMatch());
		return false;
	}

	
	/*
	 * Method isDisabled.
	 * @return boolean
	 */
	private boolean isDisabled() {
		/*IConfiguredSite cSite = getSite().getConfiguredSite();
		if (cSite==null) return false;
		IFeatureReference[] configured = cSite.getConfiguredFeatures();
		for (int i = 0; i < configured.length; i++) {
			if (this.equals(configured[i])) return false;
		}
		return true;*/
		// FIXME: this code was never executed, should we remove it ?
		return false;
	}
	
	/**
	 * @see org.eclipse.update.core.IIncludedFeatureReference#getFeature(boolean,
	 * IConfiguredSite)
	 * @deprecated
	 */
	public IFeature getFeature(boolean perfectMatch,IConfiguredSite configuredSite) throws CoreException {
		return getFeature(perfectMatch,configuredSite,null);
	}	
	
	/**
	 * @see org.eclipse.update.core.IIncludedFeatureReference#getFeature(boolean,
	 * IConfiguredSite,IProgressMonitor)
	 */
	public IFeature getFeature(boolean perfectMatch,IConfiguredSite configuredSite,IProgressMonitor monitor) throws CoreException {

		// if perfect match is asked or if the feature is disabled
		// we return the exact match 		
		if (perfectMatch || getMatch() == IImport.RULE_PERFECT || isDisabled()) {
			return super.getFeature(monitor);
		} else {
			if (bestMatchFeature == null) {
				// find best match
				if (configuredSite==null)
					configuredSite = getSite().getCurrentConfiguredSite();
				IFeatureReference bestMatchReference = getBestMatch(configuredSite);
				IFeature localBestMatchFeature = getFeature(bestMatchReference,monitor);
				// during reconciliation, we may not have the currentConfiguredSite yet
				// do not preserve the best match
				if (configuredSite==null) return localBestMatchFeature;
				else bestMatchFeature = localBestMatchFeature;
			}
			return bestMatchFeature;
		}
	}
	
	/*
	 * Method getBestMatch.
	 * @param enabledFeatures
	 * @param identifier
	 * @param options
	 * @return Object
	 */
	private IIncludedFeatureReference getBestMatch(IConfiguredSite configuredSite) throws CoreException {
		IncludedFeatureReference newRef = null;

		if (configuredSite==null) return this;
		IFeatureReference[] enabledFeatures = configuredSite.getConfiguredFeatures();

		// find the best feature based on match from enabled features
		for (int ref = 0; ref < enabledFeatures.length; ref++) {
			if (enabledFeatures[ref] != null) {
				VersionedIdentifier id = enabledFeatures[ref].getVersionedIdentifier();
				if (matches(getVersionedIdentifier(), id)) {
					if (newRef == null || id.getVersion().isGreaterThan(newRef.getVersionedIdentifier().getVersion())) {
						newRef = new IncludedFeatureReference(enabledFeatures[ref]);
						newRef.setMatchingRule(getMatch());
						newRef.isOptional(isOptional());
						newRef.setLabel(getLabel());
					}
				}
			}
		}

		if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_WARNINGS){
			UpdateCore.warn("Found best match feature:"+newRef+" for feature reference "+this.getURLString());
		}

		if (newRef != null)
			return newRef;
		else 
			return this;
	}			
	/**
	 * @see org.eclipse.update.core.IFeatureReference#getFeature()
	 * @deprecated
	 */
	public IFeature getFeature() throws CoreException {
		return getFeature(null);
	}
	/**
	 * @see org.eclipse.update.core.IFeatureReference#getFeature
	 * (IProgressMonitor)
	 */
	public IFeature getFeature(IProgressMonitor monitor) throws CoreException {
		return getFeature(false,null,monitor);
	}	
}

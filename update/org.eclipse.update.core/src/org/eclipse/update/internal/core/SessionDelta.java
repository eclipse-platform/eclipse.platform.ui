package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.File;
import java.io.PrintWriter;
import java.util.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.update.configuration.*;
import org.eclipse.update.configuration.IConfiguredSite;
import org.eclipse.update.configuration.ISessionDelta;
import org.eclipse.update.core.*;
import org.eclipse.update.core.IFeatureReference;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.core.model.ModelObject;
import org.eclipse.update.internal.model.InstallChangeParser;

/**
 *
 */
public class SessionDelta extends ModelObject implements ISessionDelta {

	private Date date;
	private List featureReferences;
	private File file;

	/**
	 * Constructor for SessionDelta.
	 */
	public SessionDelta() {
		super();
	}

	/**
	 * @see ISessionDelta#getFeatureReferences()
	 */
	public IFeatureReference[] getFeatureReferences() {
		if (featureReferences == null)
			return new IFeatureReference[0];

		return (IFeatureReference[]) featureReferences.toArray(
			arrayTypeFor(featureReferences));
	}

	/**
	 * @see ISessionDelta#getDate()
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @see ISessionDelta#configureSessionDelta(boolean, IProgressMonitor)
	 */
	public void configureSessionDelta(boolean configure, IProgressMonitor pm) throws CoreException {

		// process all feature reference to configure
		if (configure) {
			// pm.

			// loop through all the configured site
			// find the configuredSite that maintains this featureReference
			// configure the feature
			if (featureReferences != null && featureReferences.size() > 0) {
				IInstallConfiguration currentConfig =
					SiteManager.getLocalSite().getCurrentConfiguration();
				if (currentConfig != null) {
					IConfiguredSite[] configSites = currentConfig.getConfiguredSites();
					for (int i = 0; i < configSites.length; i++) {
						Iterator iterator = featureReferences.iterator();
						while (iterator.hasNext()) {
							IFeatureReference ref = (IFeatureReference) iterator.next();
							ISite site = ref.getSite();
							if(site!=null && site.equals(configSites[i])){
								configSites[i].configure(ref.getFeature());
							}
						}
					}
				}
			}
		}

		// remove the file from the file system
		if (file != null)
			UpdateManagerUtils.removeFromFileSystem(file);
	}

	/**
	 * @see IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}

	/*
	 *
	 */
	public void addReference(IFeatureReference reference) {
		if (featureReferences == null)
			featureReferences = new ArrayList();
		featureReferences.add(reference);
	}

	/*
	 * 
	 */
	public void setCreationDate(Date date) {
		this.date = date;
	}

	/*
	 * Sets the file. 
	 * We will remove the file
	 */
	public void setFile(File file) {
		this.file = file;
	}
	
	}
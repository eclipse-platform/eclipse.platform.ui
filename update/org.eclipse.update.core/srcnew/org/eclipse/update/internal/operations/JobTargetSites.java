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
package org.eclipse.update.internal.operations;
import java.util.*;

import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.operations.*;


/**
 * Hashtable of JobTargetSite's. Keys are IInstallFeatureOperation.
 * JobTargetSites
 */
public class JobTargetSites extends HashMap {

	private IInstallConfiguration config;
	private IInstallFeatureOperation[] jobs;
	private HashSet added;


	/**
	 * Constructor for ReviewPage2
	 */
	public JobTargetSites(IInstallConfiguration config) {
		this.config = config;
	}

	public void setJobs(IInstallFeatureOperation[] jobs) {
		this.jobs = jobs;
	}

	public void computeDefaultTargetSites() {
		clear();
		for (int i = 0; i < jobs.length; i++) {
			JobTargetSite jobSite = new JobTargetSite();
			jobSite.job = jobs[i];
			jobSite.defaultSite =
				UpdateManager.getDefaultTargetSite(config, jobs[i], false);
			jobSite.affinitySite =
				UpdateManager.getAffinitySite(config, jobs[i].getFeature());
			if (jobSite.affinitySite == null)
				jobSite.affinitySite = jobs[i].getTargetSite();
			jobSite.targetSite = computeTargetSite(jobSite);
			put(jobs[i], jobSite);
		}
	}

	public IConfiguredSite computeTargetSite(JobTargetSite jobSite) {
		IConfiguredSite csite =
			jobSite.affinitySite != null ? jobSite.affinitySite : jobSite.defaultSite;
		return (csite == null) ? getFirstTarget(jobSite) : csite;
	}

	
	public boolean getSiteVisibility(IConfiguredSite site, JobTargetSite jobSite) {
		// If affinity site is known, only it should be shown
		if (jobSite.affinitySite != null) {
			// Must compare referenced sites because
			// configured sites themselves may come from 
			// different configurations
			return site.getSite().equals(jobSite.affinitySite.getSite());
		}

		// If this is the default target site, let it show
		if (site.equals(jobSite.defaultSite))
			return true;
			
		// Not the default. If update, show only private sites.
		// If install, allow product site + private sites.
		if (site.isPrivateSite() && site.isUpdatable())
			return true;
			
		if (jobSite.job.getOldFeature() == null && site.isProductSite())
			return true;
			
		return false;
	}


	private IConfiguredSite getFirstTarget(JobTargetSite jobSite) {
		IConfiguredSite firstSite = jobSite.targetSite;
		if (firstSite == null) {
			IConfiguredSite[] sites = config.getConfiguredSites();
			for (int i = 0; i < sites.length; i++) {
				IConfiguredSite csite = sites[i];
				if (getSiteVisibility(csite, jobSite)) {
					firstSite = csite;
					break;
				}
			}
		}
		return firstSite;
	}


	

	private long computeRequiredSizeFor(IConfiguredSite site) {
		long totalSize = 0;
		for (int i = 0; i < jobs.length; i++) {
			JobTargetSite jobSite = (JobTargetSite) get(jobs[i]);
			if (site.equals(jobSite.targetSite)) {
				long jobSize = site.getSite().getInstallSizeFor(jobs[i].getFeature());
				if (jobSize == -1)
					return -1;
				totalSize += jobSize;
			}
		}
		return totalSize;
	}


	public JobTargetSite findPatchedFeature(IFeature patch) {
		for (Iterator enum = this.keySet().iterator(); enum.hasNext();) {
			JobTargetSite jobSite = (JobTargetSite)get(enum.next());
			IFeature target = jobSite.job.getFeature();
			if (!target.equals(patch) && UpdateManager.isPatch(target, patch))
				return jobSite;
		}
		return null;
	}

	public JobTargetSite[] getJobTargetSites() {
		JobTargetSite[] sites = new JobTargetSite[jobs.length];
		for (int i = 0; i < jobs.length; i++) {
			JobTargetSite jobSite = (JobTargetSite)get(jobs[i]);
			sites[i] = jobSite;
		}
		return sites;
	}

	public IConfiguredSite getTargetSite(IInstallFeatureOperation job) {
		IInstallFeatureOperation target = null;
		for (int i = 0; jobs != null && i < jobs.length; i++)
			if (job == jobs[i]) {
				target = jobs[i];
				break;
			}
		if (target != null) {
			JobTargetSite jobSite = (JobTargetSite)get(target);
			if (jobSite != null)
				return jobSite.targetSite;
		}
		return null;
	}
}

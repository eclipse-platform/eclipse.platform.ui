/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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

import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.operations.*;


/**
 * 
 */
public class DuplicateConflictsValidator  {

	private static final String KEY_CONFLICT =
		"DuplicateConflictsDialog.conflict"; //$NON-NLS-1$

	public static class IdEntry {
		IConfiguredSite csite;
		IFeature feature;

		public IdEntry(IFeature feature, IConfiguredSite csite) {
			this.feature = feature;
			this.csite = csite;
			if (csite == null) {
				System.out.println("csite null"); //$NON-NLS-1$
			}
		}
		public boolean isInstallCandidate() {
			return csite != null;
		}
		public IFeature getFeature() {
			return feature;
		}

		public String getIdentifier() {
			return feature.getVersionedIdentifier().getIdentifier();
		}
		public IConfiguredSite getConfiguredSite() {
			if (csite != null)
				return csite;
			return feature.getSite().getCurrentConfiguredSite();
		}
		public boolean sameLevel(IdEntry entry) {
			VersionedIdentifier vid = feature.getVersionedIdentifier();
			VersionedIdentifier evid =
				entry.getFeature().getVersionedIdentifier();
			return vid.equals(evid);
		}
		public String toString() {
			IConfiguredSite configSite = getConfiguredSite();
			String version =
				feature.getVersionedIdentifier().getVersion().toString();
			String location = configSite.getSite().getURL().getFile();
			return Policy.bind(
				KEY_CONFLICT,
				new String[] { version, location });
		}
	}

	public static ArrayList computeDuplicateConflicts(
		IInstallFeatureOperation job,
		IInstallConfiguration config,
		IConfiguredSite targetSite,
		IFeatureReference[] optionalFeatures) {
		Hashtable featureTable = new Hashtable();
		try {
			computePresentState(featureTable, config);
			computeNewFeature(
				job.getFeature(),
				targetSite,
				featureTable,
				optionalFeatures);
			return computeConflicts(featureTable);
		} catch (CoreException e) {
			return null;
		}
	}

	public static ArrayList computeDuplicateConflicts(
		IInstallFeatureOperation[] jobs,
		IInstallConfiguration config) {
		Hashtable featureTable = new Hashtable();
		computePresentState(featureTable, config);
		computeNewFeatures(jobs, featureTable);
		return computeConflicts(featureTable);
	}

	private static ArrayList computeConflicts(Hashtable featureTable) {
		ArrayList result = null;
		for (Enumeration iterator = featureTable.elements();
			iterator.hasMoreElements();
			) {
			ArrayList candidate = (ArrayList) iterator.nextElement();
			if (candidate.size() == 1)
				continue;
			ArrayList conflict = checkForConflict(candidate);
			if (conflict != null) {
				if (result == null)
					result = new ArrayList();
				result.add(conflict);
			}
		}
		return result;
	}

	private static ArrayList checkForConflict(ArrayList candidate) {
		IdEntry firstEntry = null;
		for (int i = 0; i < candidate.size(); i++) {
			IdEntry entry = (IdEntry) candidate.get(i);
			if (firstEntry == null)
				firstEntry = entry;
			else if (!entry.sameLevel(firstEntry))
				return candidate;
		}
		return null;
	}

	private static void computePresentState(
		Hashtable table,
		IInstallConfiguration config) {
		IConfiguredSite[] csites = config.getConfiguredSites();
		for (int i = 0; i < csites.length; i++) {
			IConfiguredSite csite = csites[i];
			IFeatureReference[] refs = csite.getConfiguredFeatures();
			for (int j = 0; j < refs.length; j++) {
				try {
					addEntry(refs[j].getFeature(null), csite, table);
				} catch (CoreException e) {
					// don't let one bad feature stop the loop
				}
			}
		}
	}

	private static void computeNewFeatures(
		IInstallFeatureOperation[] jobs,
		Hashtable featureTable) {
		for (int i = 0; i < jobs.length; i++) {
			IInstallFeatureOperation job = jobs[i];
			IConfiguredSite targetSite = job.getTargetSite();
			IFeature newFeature = job.getFeature();
			try {
				computeNewFeature(newFeature, targetSite, featureTable, null);
			} catch (CoreException e) {
			}
		}
	}

	private static void computeNewFeature(
		IFeature feature,
		IConfiguredSite csite,
		Hashtable table,
		IFeatureReference[] optionalFeatures)
		throws CoreException {
		addEntry(feature, csite, table);
		IIncludedFeatureReference[] irefs =
			feature.getIncludedFeatureReferences();
		for (int i = 0; i < irefs.length; i++) {
			IIncludedFeatureReference iref = irefs[i];
			boolean add = true;

			if (iref.isOptional() && optionalFeatures != null) {
				boolean found = false;
				for (int j = 0; j < optionalFeatures.length; j++) {
					IFeatureReference checked = optionalFeatures[j];
					if (checked.equals(iref)) {
						found = true;
						break;
					}
				}
				add = found;
			}
			if (add)
				computeNewFeature(
					iref.getFeature(null),
					csite,
					table,
					optionalFeatures);
		}
	}

	private static void addEntry(
		IFeature feature,
		IConfiguredSite csite,
		Hashtable featureTable) {
		String id = feature.getVersionedIdentifier().getIdentifier();
		ArrayList entries = (ArrayList) featureTable.get(id);
		if (entries == null) {
			entries = new ArrayList();
			featureTable.put(id, entries);
		}
		IdEntry entry = new IdEntry(feature, csite);
		boolean replaced = false;
		for (int i = 0; i < entries.size(); i++) {
			IdEntry existingEntry = (IdEntry) entries.get(i);
			IConfiguredSite existingSite = existingEntry.getConfiguredSite();
			if (existingSite.equals(entry.getConfiguredSite())) {
				// same site - replace it if not new
				if (entry.isInstallCandidate()) {
					entries.set(i, entry);
					entries.remove(existingEntry);
				}
				replaced = true;
				break;
			}
		}
		if (!replaced)
			entries.add(entry);
	}
}

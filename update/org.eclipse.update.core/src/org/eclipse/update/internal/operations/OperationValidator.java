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

import org.eclipse.core.boot.*;
import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.operations.*;

/**
 *
 */
public class OperationValidator implements IOperationValidator{
	private static final String KEY_ROOT_MESSAGE =
		"ActivityConstraints.rootMessage";
	private static final String KEY_ROOT_MESSAGE_INIT =
		"ActivityConstraints.rootMessageInitial";
	private static final String KEY_CHILD_MESSAGE =
		"ActivityConstraints.childMessage";
	private static final String KEY_PLATFORM = "ActivityConstraints.platform";
	private static final String KEY_PRIMARY = "ActivityConstraints.primary";
	private static final String KEY_OS = "ActivityConstraints.os";
	private static final String KEY_WS = "ActivityConstraints.ws";
	private static final String KEY_ARCH = "ActivityConstraints.arch";
	private static final String KEY_PREREQ = "ActivityConstraints.prereq";
	private static final String KEY_PREREQ_PLUGIN =
		"ActivityConstaints.prereq.plugin";
	private static final String KEY_PREREQ_FEATURE =
		"ActivityConstaints.prereq.feature";
	private static final String KEY_PREREQ_PERFECT =
		"ActivityConstraints.prereqPerfect";
	private static final String KEY_PREREQ_EQUIVALENT =
		"ActivityConstraints.prereqEquivalent";
	private static final String KEY_PREREQ_COMPATIBLE =
		"ActivityConstraints.prereqCompatible";
	private static final String KEY_PREREQ_GREATER =
		"ActivityConstraints.prereqGreaterOrEqual";
	private static final String KEY_PATCH_REGRESSION =
		"ActivityConstraints.patchRegression";
	private static final String KEY_PATCH_UNCONFIGURE =
		"ActivityConstraints.patchUnconfigure";
	private static final String KEY_PATCH_UNCONFIGURE_BACKUP =
		"ActivityConstraints.patchUnconfigureBackup";
	private static final String KEY_PATCH_MISSING_TARGET =
		"ActivityConstraints.patchMissingTarget";
	private static final String KEY_OPTIONAL_CHILD =
		"ActivityConstraints.optionalChild";
	private static final String KEY_CYCLE = "ActivityConstraints.cycle";
	private static final String KEY_CONFLICT = "ActivityConstraints.conflict";
	private static final String KEY_EXCLUSIVE = "ActivityConstraints.exclusive";
	private static final String KEY_WRONG_TIMELINE =
		"ActivityConstraints.timeline";
	private static final String KEY_NO_LICENSE =
		"ActivityConstraints.noLicense";

	/*
	 * Called by UI before performing operation
	 */
	public IStatus validatePendingInstall(IFeature oldFeature, IFeature newFeature) {
		// check initial state
		ArrayList beforeStatus = new ArrayList();
		validateInitialState(beforeStatus);

		// check proposed change
		ArrayList status = new ArrayList();
		validateInstall(oldFeature, newFeature, status);

		// report status
		return createReportStatus(beforeStatus, status);
	}
	
	/*
	 * Called by UI before performing operation
	 */
	public IStatus validatePendingUnconfig(IFeature feature) {
		// check initial state
		ArrayList beforeStatus = new ArrayList();
		validateInitialState(beforeStatus);

		// check proposed change
		ArrayList status = new ArrayList();
		validateUnconfigure(feature, status);

		// report status
		return createReportStatus(beforeStatus, status);
	}
	
	/*
	 * Called by UI before performing operation
	 */
	public IStatus validatePendingConfig(IFeature feature) {
		// check initial state
		ArrayList beforeStatus = new ArrayList();
		validateInitialState(beforeStatus);

		// check proposed change
		ArrayList status = new ArrayList();
		validateConfigure(feature, status);

		// report status
		return createReportStatus(beforeStatus, status);
	}

	/*
	 * Called by UI before processing a delta
	 */
	public IStatus validateSessionDelta(
		ISessionDelta delta,
		IFeatureReference[] deltaRefs) {
		// check initial state
		ArrayList beforeStatus = new ArrayList();
		validateInitialState(beforeStatus);

		// check proposed change
		ArrayList status = new ArrayList();
		switch (delta.getType()) {
			case ISessionDelta.ENABLE :
				validateDeltaConfigure(delta, deltaRefs, status);
				break;
		}

		// report status
		return createReportStatus(beforeStatus, status);
	}

	/*
	 * Called by the UI before doing a revert/ restore operation
	 */
	public IStatus validatePendingRevert(IInstallConfiguration config) {
		// check initial state
		ArrayList beforeStatus = new ArrayList();
		validateInitialState(beforeStatus);

		// check proposed change
		ArrayList status = new ArrayList();
		validateRevert(config, status);

		// report status
		return createReportStatus(beforeStatus, status);
	}

	/*
	 * Called by the UI before doing a batched processing of
	 * several pending changes.
	 */
	public IStatus validatePendingChanges(IInstallFeatureOperation[] jobs) {
		// check initial state
		ArrayList beforeStatus = new ArrayList();
		validateInitialState(beforeStatus);

		// check proposed change
		ArrayList status = new ArrayList();
		validatePendingChanges(jobs, status);

		// report status
		return createReportStatus(beforeStatus, status);
	}
	private IStatus createReportStatus(ArrayList beforeStatus, ArrayList status) {
		// report status
		if (status.size() > 0) {
			if (beforeStatus.size() > 0)
				return createMultiStatus(KEY_ROOT_MESSAGE_INIT, beforeStatus);
			else
				return createMultiStatus(KEY_ROOT_MESSAGE, status);
		}
		return null;
	}

	/*
	 * Check the current state.
	 */
	public IStatus validateCurrentState() {
		// check the state
		ArrayList status = new ArrayList();
		validateInitialState(status);

		// report status
		if (status.size() > 0)
			return createMultiStatus(KEY_ROOT_MESSAGE, status);
		return null;
	}

	/*
	 * Check to see if we are not broken even before we start
	 */
	private static void validateInitialState(ArrayList status) {
		try {
			ArrayList features = computeFeatures();
			checkConstraints(features, status);
		} catch (CoreException e) {
			status.add(e.getStatus());
		}
	}

	/*
	 * handle unconfigure
	 */
	private static void validateUnconfigure(
		IFeature feature,
		ArrayList status) {
		try {
			if (validateUnconfigurePatch(feature, status))
				return;
			ArrayList features = computeFeatures();
			features = computeFeaturesAfterOperation(features, null, feature);
			checkConstraints(features, status);
		} catch (CoreException e) {
			status.add(e.getStatus());
		}
	}

	private static boolean validateUnconfigurePatch(
		IFeature feature,
		ArrayList status)
		throws CoreException {
		if (feature.isPatch()) {
			IInstallConfiguration backup =
				UpdateUtils.getBackupConfigurationFor(feature);
			String msg;
			if (backup != null)
				msg =
					UpdateUtils.getFormattedMessage(
						KEY_PATCH_UNCONFIGURE_BACKUP,
						backup.getLabel());
			else
				msg = UpdateUtils.getString(KEY_PATCH_UNCONFIGURE);
			status.add(createStatus(feature, msg));
			return true;
		}
		return false;
	}

	/*
	 * handle configure
	 */
	private static void validateConfigure(IFeature feature, ArrayList status) {
		try {
			ArrayList features = computeFeatures();
			checkOptionalChildConfiguring(feature, status);
			features = computeFeaturesAfterOperation(features, feature, null);
			checkConstraints(features, status);

		} catch (CoreException e) {
			status.add(e.getStatus());
		}
	}

	/*
	 * handle install and update
	 */
	private static void validateInstall(
		IFeature oldFeature,
		IFeature newFeature,
		ArrayList status) {
		try {
			ArrayList features = computeFeatures();
			if (oldFeature == null && isPatch(newFeature))
				checkUnique(newFeature, features, status);
			features =
				computeFeaturesAfterOperation(features, newFeature, oldFeature);
			checkConstraints(features, status);
			checkLicense(newFeature, status);
		} catch (CoreException e) {
			status.add(e.getStatus());
		}
	}

	/*
	 * handle revert and restore
	 */
	private static void validateRevert(
		IInstallConfiguration config,
		ArrayList status) {
		try {
			// check the timeline and don't bother
			// to check anything else if negative
			if (!checkTimeline(config, status))
				return;
			ArrayList features = computeFeaturesAfterRevert(config);
			checkConstraints(features, status);
			checkRevertConstraints(features, status);

		} catch (CoreException e) {
			status.add(e.getStatus());
		}
	}

	/*
	 * Handle delta addition
	 */
	private static void validateDeltaConfigure(
		ISessionDelta delta,
		IFeatureReference[] deltaRefs,
		ArrayList status) {
		try {
			ArrayList features = computeFeaturesAfterDelta(delta, deltaRefs);
			checkConstraints(features, status);

		} catch (CoreException e) {
			status.add(e.getStatus());
		}
	}

	/*
	 * Handle one-click changes as a batch
	 */
	private static void validatePendingChanges(
		IInstallFeatureOperation[] jobs,
		ArrayList status) {
		try {
			ArrayList features = computeFeatures();
			ArrayList savedFeatures = features;
			int nexclusives = 0;

			// pass 1: see if we can process the entire "batch"
			ArrayList tmpStatus = new ArrayList();
			for (int i = 0; i < jobs.length; i++) {
				IInstallFeatureOperation job = jobs[i];

				IFeature newFeature = job.getFeature();
				IFeature oldFeature = job.getOldFeature();
				checkLicense(newFeature, status);
				if (jobs.length > 1 && newFeature.isExclusive()) {
					nexclusives++;
					status.add(
						createStatus(
							newFeature,
							UpdateUtils.getString(KEY_EXCLUSIVE)));
					continue;
				}
				features =
					computeFeaturesAfterOperation(
						features,
						newFeature,
						oldFeature);
			}
			if (nexclusives > 0)
				return;
			checkConstraints(features, tmpStatus);
			if (tmpStatus.size() == 0) // the whole "batch" is OK
				return;

			// pass 2: we have conflicts
			features = savedFeatures;
			for (int i = 0; i < jobs.length; i++) {
				IInstallFeatureOperation job = jobs[i];
				IFeature newFeature = job.getFeature();
				IFeature oldFeature = job.getOldFeature();

				features =
					computeFeaturesAfterOperation(
						features,
						newFeature,
						oldFeature);
					
				checkConstraints(features, status);
				if (status.size() > 0) {
					IStatus conflict =
						createStatus(
							newFeature,
							UpdateUtils.getString(KEY_CONFLICT));
					status.add(0, conflict);
					return;
				}
			}
		} catch (CoreException e) {
			status.add(e.getStatus());
		}
	}

	/*
	 * Compute a list of configured features
	 */
	private static ArrayList computeFeatures() throws CoreException {
		return computeFeatures(true);
	}
	/*
	 * Compute a list of configured features
	 */
	private static ArrayList computeFeatures(boolean configuredOnly)
		throws CoreException {
		ArrayList features = new ArrayList();
		ILocalSite localSite = SiteManager.getLocalSite();
		IInstallConfiguration config = localSite.getCurrentConfiguration();
		IConfiguredSite[] csites = config.getConfiguredSites();

		for (int i = 0; i < csites.length; i++) {
			IConfiguredSite csite = csites[i];

			IFeatureReference[] crefs;

			if (configuredOnly)
				crefs = csite.getConfiguredFeatures();
			else
				crefs = csite.getSite().getFeatureReferences();
			for (int j = 0; j < crefs.length; j++) {
				IFeatureReference cref = crefs[j];
				IFeature cfeature = cref.getFeature(null);
				features.add(cfeature);
			}
		}

		return features;
	}

	/*
	 * Compute the nested feature subtree starting at the specified base feature
	 */
	private static ArrayList computeFeatureSubtree(
		IFeature top,
		IFeature feature,
		ArrayList features,
		boolean tolerateMissingChildren)
		throws CoreException {
		return computeFeatureSubtree(
			top,
			feature,
			features,
			tolerateMissingChildren,
			null);
	}

	/*
	 * Compute the nested feature subtree starting at the specified base feature
	 */
	private static ArrayList computeFeatureSubtree(
		IFeature top,
		IFeature feature,
		ArrayList features,
		boolean tolerateMissingChildren,
		ArrayList configuredFeatures)
		throws CoreException {

		// check arguments
		if (features == null)
			features = new ArrayList();
		if (top == null)
			return features;
		if (feature == null)
			feature = top;

		// check for <includes> cycle
		if (features.contains(feature)) {
			IStatus status = createStatus(top, UpdateUtils.getString(KEY_CYCLE));
			throw new CoreException(status);
		}

		// return specified base feature and all its children
		features.add(feature);
		IIncludedFeatureReference[] children =
			feature.getIncludedFeatureReferences();
		for (int i = 0; i < children.length; i++) {
			try {
				IFeature child;
				if (configuredFeatures == null)
					child = children[i].getFeature(null);
				else
					child = getBestMatch(children[i], configuredFeatures);
				features =
					computeFeatureSubtree(
						top,
						child,
						features,
						tolerateMissingChildren);
			} catch (CoreException e) {
				if (!children[i].isOptional() && !tolerateMissingChildren)
					throw e;
			}
		}
		return features;
	}
	/*
	 * This method fixes the defect 34241. Included feature reference
	 * of the feature from the remote server always returns 
	 * a perfect match. However, the actual install behaviour is
	 * different if that feature already exists (as disabled) 
	 * while a better one is enabled. This typically happens
	 * through branch updates or patches.
	 * 
	 * To avoid this problem, we need to check if a better feature
	 * that still matches the reference is enabled in the current
	 * configuration. If it is, it will be used instead of the
	 * 'perfect' match. If not, we should default to the old
	 * behaviour.
	 */

	private static IFeature getBestMatch(
		IIncludedFeatureReference ref,
		ArrayList configuredFeatures)
		throws CoreException {
		int match = ref.getMatch();
		ISite site = ref.getSite();
		IConfiguredSite csite = site.getCurrentConfiguredSite();
		// If the feature is from the remote server and
		// it can handle better features, see if you can
		// resolve locally.
		if (csite == null && match != IUpdateConstants.RULE_PERFECT) {
			// there is a chance that there is a better match
			// in the platform
			VersionedIdentifier vid = ref.getVersionedIdentifier();
			PluginVersionIdentifier version = vid.getVersion();

			for (int i = 0; i < configuredFeatures.size(); i++) {
				IFeature feature = (IFeature) configuredFeatures.get(i);
				VersionedIdentifier fvid = feature.getVersionedIdentifier();
				if (fvid.getIdentifier().equals(vid.getIdentifier())) {
					// Feature found in local configuration.
					// Ignore if the same version.
					// Use it if better
					PluginVersionIdentifier fversion = fvid.getVersion();
					if (fversion.isGreaterThan(version)) {
						boolean matches = false;
						switch (match) {
							case IImport.RULE_COMPATIBLE :
								matches =
									fvid.getVersion().isCompatibleWith(
										vid.getVersion());
								break;
							case IImport.RULE_EQUIVALENT :
								matches =
									fvid.getVersion().isEquivalentTo(
										vid.getVersion());
								break;
							case IImport.RULE_GREATER_OR_EQUAL :
								matches =
									fvid.getVersion().isGreaterOrEqualTo(
										vid.getVersion());
								break;
						}
						if (matches)
							return feature;
					}
				}
			}
		}
		// fallback - just get the feature from the reference.
		return ref.getFeature(null);
	}

	private static void checkLicense(IFeature feature, ArrayList status) {
		IURLEntry licenseEntry = feature.getLicense();
		if (licenseEntry != null) {
			String license = licenseEntry.getAnnotation();
			if (license != null && license.trim().length() > 0)
				return;
		}
		status.add(createStatus(feature, UpdateUtils.getString(KEY_NO_LICENSE)));
	}

	/*
	 * Compute a list of features that will be configured after the operation
	 */
	private static ArrayList computeFeaturesAfterOperation(
		ArrayList features,
		IFeature add,
		IFeature remove)
		throws CoreException {

		ArrayList addTree =
			computeFeatureSubtree(
				add,
				null,
				null,
				false,
		/* do not tolerate missing children */
		features);
		ArrayList removeTree =
			computeFeatureSubtree(
				remove,
				null,
				null,
				true /* tolerate missing children */
		);
		if (remove != null) {
			// Patches to features are removed together with
			// those features. Include them in the list.
			contributePatchesFor(removeTree, features, removeTree);
		}

		if (remove != null)
			features.removeAll(removeTree);

		if (add != null)
			features.addAll(addTree);

		return features;
	}

	private static void contributePatchesFor(
		ArrayList removeTree,
		ArrayList features,
		ArrayList result)
		throws CoreException {

		for (int i = 0; i < removeTree.size(); i++) {
			IFeature feature = (IFeature) removeTree.get(i);
			contributePatchesFor(feature, features, result);
		}
	}

	private static void contributePatchesFor(
		IFeature feature,
		ArrayList features,
		ArrayList result)
		throws CoreException {
		for (int i = 0; i < features.size(); i++) {
			IFeature candidate = (IFeature) features.get(i);
			if (UpdateUtils.isPatch(feature, candidate)) {
				ArrayList removeTree =
					computeFeatureSubtree(candidate, null, null, true);
				result.addAll(removeTree);
			}
		}
	}

	/*
	 * Compute a list of features that will be configured after performing the revert
	 */
	private static ArrayList computeFeaturesAfterRevert(IInstallConfiguration config)
		throws CoreException {

		ArrayList list = new ArrayList();
		IConfiguredSite[] csites = config.getConfiguredSites();
		for (int i = 0; i < csites.length; i++) {
			IConfiguredSite csite = csites[i];
			IFeatureReference[] features = csite.getConfiguredFeatures();
			for (int j = 0; j < features.length; j++) {
				list.add(features[j].getFeature(null));
			}
		}
		return list;
	}

	/*
	 * Compute a list of features that will be configured after applying the
	 * specified delta
	 */
	private static ArrayList computeFeaturesAfterDelta(
		ISessionDelta delta,
		IFeatureReference[] deltaRefs)
		throws CoreException {

		if (delta == null || deltaRefs == null)
			deltaRefs = new IFeatureReference[0];
		else if (deltaRefs == null)
			deltaRefs = delta.getFeatureReferences();

		ArrayList features = new ArrayList(); // cumulative results list
		ILocalSite localSite = SiteManager.getLocalSite();
		IInstallConfiguration config = localSite.getCurrentConfiguration();
		IConfiguredSite[] csites = config.getConfiguredSites();

		// compute changes for each site
		for (int i = 0; i < csites.length; i++) {
			IConfiguredSite csite = csites[i];
			ArrayList siteFeatures = new ArrayList();

			// collect currently configured features on site
			IFeatureReference[] crefs = csite.getConfiguredFeatures();
			for (int j = 0; crefs != null && j < crefs.length; j++) {
				IFeatureReference cref = crefs[j];
				IFeature cfeature = cref.getFeature(null);
				siteFeatures.add(cfeature);
			}

			// add deltas for the site			
			for (int j = 0; j < deltaRefs.length; j++) {
				ISite deltaSite = deltaRefs[j].getSite();
				if (deltaSite.equals(csite.getSite())) {
					IFeature dfeature = deltaRefs[j].getFeature(null);
					if (!siteFeatures.contains(dfeature)) // don't add dups
						siteFeatures.add(dfeature);
				}
			}

			// reduce the list if needed	
			IFeature[] array =
				(IFeature[]) siteFeatures.toArray(
					new IFeature[siteFeatures.size()]);
			ArrayList removeTree = new ArrayList();
			for (int j = 0; j < array.length; j++) {
				VersionedIdentifier id1 = array[j].getVersionedIdentifier();
				for (int k = 0; k < array.length; k++) {
					if (j == k)
						continue;
					VersionedIdentifier id2 = array[k].getVersionedIdentifier();
					if (id1.getIdentifier().equals(id2.getIdentifier())) {
						if (id2.getVersion().isGreaterThan(id1.getVersion())) {
							removeTree.add(array[j]);
							siteFeatures.remove(array[j]);
							break;
						}
					}
				}
			}
			// Compute patches that will need to be removed together with 
			// the removed features
			ArrayList patchesTree = new ArrayList();
			contributePatchesFor(removeTree, siteFeatures, patchesTree);
			siteFeatures.removeAll(patchesTree);

			// accumulate site results
			features.addAll(siteFeatures);
		}

		return features;
	}

	/*
	 * Compute a list of plugin entries for the specified features.
	 */
	private static ArrayList computePluginsForFeatures(ArrayList features)
		throws CoreException {
		if (features == null)
			return new ArrayList();

		HashMap plugins = new HashMap();
		for (int i = 0; i < features.size(); i++) {
			IFeature feature = (IFeature) features.get(i);
			IPluginEntry[] entries = feature.getPluginEntries();
			for (int j = 0; j < entries.length; j++) {
				IPluginEntry entry = entries[j];
				plugins.put(entry.getVersionedIdentifier(), entry);
			}
		}
		ArrayList result = new ArrayList();
		result.addAll(plugins.values());
		return result;
	}

	private static boolean isPatch(IFeature feature) {
		IImport[] imports = feature.getImports();
		for (int i = 0; i < imports.length; i++) {
			IImport iimport = imports[i];
			if (iimport.isPatch()) {
				return true;
			}
		}
		return false;
	}

	/*
	 * 
	 */
	private static void checkUnique(
		IFeature feature,
		ArrayList features,
		ArrayList status)
		throws CoreException {
		if (features == null)
			return;
		IIncludedFeatureReference[] irefs =
			feature.getIncludedFeatureReferences();
		for (int i = 0; i < irefs.length; i++) {
			IIncludedFeatureReference iref = irefs[i];
			IFeature ifeature = iref.getFeature(null);
			boolean patch = isPatch(ifeature);
			VersionedIdentifier vid = ifeature.getVersionedIdentifier();
			String id = vid.getIdentifier();
			PluginVersionIdentifier version = vid.getVersion();
			boolean found = false;
			for (int j = 0; j < features.size(); j++) {
				IFeature candidate = (IFeature) features.get(j);
				VersionedIdentifier cvid = candidate.getVersionedIdentifier();
				String cid = cvid.getIdentifier();
				PluginVersionIdentifier cversion = cvid.getVersion();
				if (cid.equals(id)) {
					// The same identifier - this one will
					// be unconfigured. Check if it is lower,
					// otherwise flag.
					found = true;
					// Ignore equal - will be filtered in the download
					if (version.equals(cversion))
						continue;
					// Flag only the case when the installed one is
					// newer than the one that will be installed.
					if (!version.isGreaterThan(cversion)) {
						// Don't allow this.
						String msg =
							UpdateUtils.getFormattedMessage(
								KEY_PATCH_REGRESSION,
								new String[] {
									ifeature.getLabel(),
									version.toString()});
						status.add(createStatus(feature, msg));

					}
				}
			}
			if (!found) {
				// All the features carried in a patch must
				// already be present, unless this feature
				// is a patch itself

				// 30849: optional feature does not
				// need to be present.
				if (!patch && iref.isOptional() == false) {
					String msg =
						UpdateUtils.getFormattedMessage(
							KEY_PATCH_MISSING_TARGET,
							new String[] {
								ifeature.getLabel(),
								version.toString()});
					status.add(createStatus(feature, msg));
				}
			}
			if (patch)
				checkUnique(ifeature, features, status);
		}
	}

	/*
	 * validate constraints
	 */
	private static void checkConstraints(ArrayList features, ArrayList status)
		throws CoreException {
		if (features == null)
			return;

		ArrayList plugins = computePluginsForFeatures(features);

		checkEnvironment(features, status);
		checkPlatformFeature(features, plugins, status);
		checkPrimaryFeature(features, status);
		checkPrereqs(features, plugins, status);
	}

	/*
	 * Verify all features are either portable, or match the current environment
	 */
	private static void checkEnvironment(
		ArrayList features,
		ArrayList status) {

		String os = BootLoader.getOS();
		String ws = BootLoader.getWS();
		String arch = BootLoader.getOSArch();

		for (int i = 0; i < features.size(); i++) {
			IFeature feature = (IFeature) features.get(i);
			ArrayList fos = createList(feature.getOS());
			ArrayList fws = createList(feature.getWS());
			ArrayList farch = createList(feature.getOSArch());

			if (fos.size() > 0) {
				if (!fos.contains(os)) {
					status.add(
						createStatus(feature, UpdateUtils.getString(KEY_OS)));
					continue;
				}
			}

			if (fws.size() > 0) {
				if (!fws.contains(ws)) {
					status.add(
						createStatus(feature, UpdateUtils.getString(KEY_WS)));
					continue;
				}
			}

			if (farch.size() > 0) {
				if (!farch.contains(arch)) {
					status.add(
						createStatus(feature, UpdateUtils.getString(KEY_ARCH)));
					continue;
				}
			}
		}
	}

	/*
	 * Verify we end up with a version of platform configured
	 */
	private static void checkPlatformFeature(
		ArrayList features,
		ArrayList plugins,
		ArrayList status) {

		String[] bootstrapPlugins =
			BootLoader
				.getCurrentPlatformConfiguration()
				.getBootstrapPluginIdentifiers();

		for (int i = 0; i < bootstrapPlugins.length; i++) {
			boolean found = false;
			for (int j = 0; j < plugins.size(); j++) {
				IPluginEntry plugin = (IPluginEntry) plugins.get(j);
				if (bootstrapPlugins[i]
					.equals(plugin.getVersionedIdentifier().getIdentifier())) {
					found = true;
					break;
				}
			}
			if (!found) {
				status.add(
					createStatus(null, UpdateUtils.getString(KEY_PLATFORM)));
				return;
			}
		}
	}

	/*
	 * Verify we end up with a version of primary feature configured
	 */
	private static void checkPrimaryFeature(
		ArrayList features,
		ArrayList status) {

		String featureId =
			BootLoader
				.getCurrentPlatformConfiguration()
				.getPrimaryFeatureIdentifier();

		for (int i = 0; i < features.size(); i++) {
			IFeature feature = (IFeature) features.get(i);
			if (featureId
				.equals(feature.getVersionedIdentifier().getIdentifier()))
				return;
		}

		status.add(createStatus(null, UpdateUtils.getString(KEY_PRIMARY)));
	}

	/*
	 * Verify we do not break prereqs
	 */
	private static void checkPrereqs(
		ArrayList features,
		ArrayList plugins,
		ArrayList status) {

		for (int i = 0; i < features.size(); i++) {
			IFeature feature = (IFeature) features.get(i);
			IImport[] imports = feature.getImports();

			for (int j = 0; j < imports.length; j++) {
				IImport iimport = imports[j];
				// for each import determine plugin or feature, version, match we need
				VersionedIdentifier iid = iimport.getVersionedIdentifier();
				String id = iid.getIdentifier();
				PluginVersionIdentifier version = iid.getVersion();
				boolean featurePrereq =
					iimport.getKind() == IImport.KIND_FEATURE;
				boolean ignoreVersion =
					version.getMajorComponent() == 0
						&& version.getMinorComponent() == 0
						&& version.getServiceComponent() == 0;
				int rule = iimport.getRule();
				if (rule == IImport.RULE_NONE)
					rule = IImport.RULE_COMPATIBLE;

				boolean found = false;

				ArrayList candidates;

				if (featurePrereq)
					candidates = features;
				else
					candidates = plugins;
				for (int k = 0; k < candidates.size(); k++) {
					VersionedIdentifier cid;
					if (featurePrereq) {
						// the candidate is a feature
						IFeature candidate = (IFeature) candidates.get(k);
						// skip self
						if (feature.equals(candidate))
							continue;
						cid = candidate.getVersionedIdentifier();
					} else {
						// the candidate is a plug-in
						IPluginEntry plugin = (IPluginEntry) candidates.get(k);
						cid = plugin.getVersionedIdentifier();
					}
					PluginVersionIdentifier cversion = cid.getVersion();
					if (id.equals(cid.getIdentifier())) {
						// have a candidate
						if (ignoreVersion)
							found = true;
						else if (
							rule == IImport.RULE_PERFECT
								&& cversion.isPerfect(version))
							found = true;
						else if (
							rule == IImport.RULE_EQUIVALENT
								&& cversion.isEquivalentTo(version))
							found = true;
						else if (
							rule == IImport.RULE_COMPATIBLE
								&& cversion.isCompatibleWith(version))
							found = true;
						else if (
							rule == IImport.RULE_GREATER_OR_EQUAL
								&& cversion.isGreaterOrEqualTo(version))
							found = true;
					}
					if (found)
						break;
				}

				if (!found) {
					// report status
					String target =
						featurePrereq
							? UpdateUtils.getString(KEY_PREREQ_FEATURE)
							: UpdateUtils.getString(KEY_PREREQ_PLUGIN);
					String msg =
						UpdateUtils.getFormattedMessage(
							KEY_PREREQ,
							new String[] { target, id });

					if (!ignoreVersion) {
						if (rule == IImport.RULE_PERFECT)
							msg =
								UpdateUtils.getFormattedMessage(
									KEY_PREREQ_PERFECT,
									new String[] {
										target,
										id,
										version.toString()});
						else if (rule == IImport.RULE_EQUIVALENT)
							msg =
								UpdateUtils.getFormattedMessage(
									KEY_PREREQ_EQUIVALENT,
									new String[] {
										target,
										id,
										version.toString()});
						else if (rule == IImport.RULE_COMPATIBLE)
							msg =
								UpdateUtils.getFormattedMessage(
									KEY_PREREQ_COMPATIBLE,
									new String[] {
										target,
										id,
										version.toString()});
						else if (rule == IImport.RULE_GREATER_OR_EQUAL)
							msg =
								UpdateUtils.getFormattedMessage(
									KEY_PREREQ_GREATER,
									new String[] {
										target,
										id,
										version.toString()});
					}
					status.add(createStatus(feature, msg));
				}
			}
		}
	}

	/*
	 * Verify we end up with valid nested features after revert
	 */
	private static void checkRevertConstraints(
		ArrayList features,
		ArrayList status) {

		for (int i = 0; i < features.size(); i++) {
			IFeature feature = (IFeature) features.get(i);
			try {
				computeFeatureSubtree(
					feature,
					null,
					null,
					false /* do not tolerate missing children */
				);
			} catch (CoreException e) {
				status.add(e.getStatus());
			}
		}
	}

	/*
	 * Verify that a parent of an optional child is configured
	 * before we allow the child to be configured as well
	 */

	private static void checkOptionalChildConfiguring(
		IFeature feature,
		ArrayList status)
		throws CoreException {
		ILocalSite localSite = SiteManager.getLocalSite();
		IInstallConfiguration config = localSite.getCurrentConfiguration();
		IConfiguredSite[] csites = config.getConfiguredSites();

		boolean included = false;
		for (int i = 0; i < csites.length; i++) {
			IConfiguredSite csite = csites[i];
			ISiteFeatureReference[] crefs =
				csite.getSite().getFeatureReferences();
			for (int j = 0; j < crefs.length; j++) {
				IFeatureReference cref = crefs[j];
				IFeature cfeature = null;
				try {
					cfeature = cref.getFeature(null);
				} catch (CoreException e) {
					//FIXME: cannot ask 'isOptional' here
					// Ignore missing optional feature.
					/* if (cref.isOptional())
						continue;
					else */
					throw e;
				}
				if (isParent(cfeature, feature, true)) {
					// Included in at least one feature as optional
					included = true;
					if (csite.isConfigured(cfeature)) {
						// At least one feature parent
						// is enabled - it is OK to
						// configure optional child.
						return;
					}
				}
			}
		}
		if (included) {
			// feature is included as optional but
			// no parent is currently configured.
			String msg = UpdateUtils.getString(KEY_OPTIONAL_CHILD);
			status.add(createStatus(feature, msg));
		} else {
			//feature is root - can be configured
		}
	}

	private static boolean isParent(
		IFeature candidate,
		IFeature feature,
		boolean optionalOnly)
		throws CoreException {
		IIncludedFeatureReference[] refs =
			candidate.getIncludedFeatureReferences();
		for (int i = 0; i < refs.length; i++) {
			IIncludedFeatureReference child = refs[i];
			VersionedIdentifier fvid = feature.getVersionedIdentifier();
			VersionedIdentifier cvid = child.getVersionedIdentifier();

			if (fvid.getIdentifier().equals(cvid.getIdentifier()) == false)
				continue;
			// same ID
			PluginVersionIdentifier fversion = fvid.getVersion();
			PluginVersionIdentifier cversion = cvid.getVersion();

			int match = child.getMatch();
			boolean matched = false;

			switch (match) {
				case IUpdateConstants.RULE_EQUIVALENT :
					if (fversion.isEquivalentTo(cversion))
						matched = true;
					break;
				case IUpdateConstants.RULE_COMPATIBLE :
					if (fversion.isCompatibleWith(cversion))
						matched = true;
					break;
				case IUpdateConstants.RULE_GREATER_OR_EQUAL :
					if (fversion.isGreaterOrEqualTo(cversion))
						matched = true;
					break;
				default :
					if (fversion.equals(cversion))
						matched = true;

			}
			if (matched) {
				// included and matched; return true if optionality is not 
				// important or it is and the inclusion is optional
				return optionalOnly == false || child.isOptional();
			}
		}
		return false;
	}

	private static boolean checkTimeline(
		IInstallConfiguration config,
		ArrayList status) {
		try {
			ILocalSite lsite = SiteManager.getLocalSite();
			IInstallConfiguration cconfig = lsite.getCurrentConfiguration();
			if (cconfig.getTimeline() != config.getTimeline()) {
				// Not the same timeline - cannot revert
				String msg =
					UpdateUtils.getFormattedMessage(
						KEY_WRONG_TIMELINE,
						config.getLabel());
				status.add(createStatus(null, msg));
				return false;
			}
		} catch (CoreException e) {
			status.add(e.getStatus());
		}
		return true;
	}

	private static IStatus createMultiStatus(
		String rootKey,
		ArrayList children) {
		IStatus[] carray =
			(IStatus[]) children.toArray(new IStatus[children.size()]);
		String message = UpdateUtils.getString(rootKey);
		return new MultiStatus(
			UpdateCore.getPlugin().getDescriptor().getUniqueIdentifier(),
			IStatus.ERROR,
			carray,
			message,
			null);
	}

	private static IStatus createStatus(IFeature feature, String message) {

		String fullMessage;
		if (feature == null)
			fullMessage = message;
		else {
			PluginVersionIdentifier version =
				feature.getVersionedIdentifier().getVersion();
			fullMessage =
				UpdateUtils.getFormattedMessage(
					KEY_CHILD_MESSAGE,
					new String[] {
						feature.getLabel(),
						version.toString(),
						message });
		}

		return new Status(
			IStatus.ERROR,
			UpdateCore.getPlugin().getDescriptor().getUniqueIdentifier(),
			IStatus.OK,
			fullMessage,
			null);
	}

	private static ArrayList createList(String commaSeparatedList) {
		ArrayList list = new ArrayList();
		if (commaSeparatedList != null) {
			StringTokenizer t =
				new StringTokenizer(commaSeparatedList.trim(), ",");
			while (t.hasMoreTokens()) {
				String token = t.nextToken().trim();
				if (!token.equals(""))
					list.add(token);
			}
		}
		return list;
	}
}
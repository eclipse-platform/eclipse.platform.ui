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
package org.eclipse.update.internal.core;
import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.*;
import org.eclipse.update.internal.model.*;
import org.eclipse.update.configurator.*;

/**
 * This class manages the reconciliation.
 */

public class SiteReconciler extends ModelObject implements IWritable {

	private List newFoundFeatures;
	private Date date;
//	private static final String DEFAULT_INSTALL_CHANGE_NAME = "delta.xml";
	//$NON-NLS-1$	

	/**
	 * 
	 */
	public SiteReconciler(SiteLocal siteLocal) {
//		this.siteLocal = siteLocal;
	}


	/**
	* 
	*/
	/*package */
	URL resolveSiteEntry(IPlatformConfiguration.ISiteEntry newSiteEntry) throws CoreException {
		URL resolvedURL = null;
		try {
			resolvedURL = Platform.resolve(newSiteEntry.getURL());
		} catch (IOException e) {
			throw Utilities.newCoreException(Policy.bind("SiteLocal.UnableToResolve", newSiteEntry.getURL().toExternalForm()), e);
			//$NON-NLS-1$
		}
		return resolvedURL;
	}


//	/*
//	 * Enable feature if:
//	 * This is an optimistic reconciliation OR
//	 * The feature is considered optional by ALL its parents AND at least one of them is enable
//	 * Otherwise disable the feature.
//	 * 
//	 * If all its parent consider the feature as optional but none are enable, 
//	 * do not add in the list of new found features. Just disable it.
//	 */
//	private void configureNewFoundFeature(boolean isOptimistic, ConfigurationPolicy newSitePolicy, ConfigurationPolicy oldSitePolicy, IFeatureReference foundFeature, IFeatureReference[] possibleParents) throws CoreException {
//
//		// TRACE
//		if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_RECONCILER) {
//			String reconciliationType = isOptimistic ? "enable (optimistic)" : "disable (pessimistic)";
//			UpdateCore.debug("This feature is new: " + foundFeature.getURL() + " reconciled as " + reconciliationType);
//		}
//
//		if (isOptimistic) {
//			newSitePolicy.configure(foundFeature, true, false);
//			return;
//		}
//
//		IFeatureReference[] allOptionalParents = UpdateManagerUtils.getParentFeatures(foundFeature, possibleParents, true);
//		IFeatureReference[] allParents = UpdateManagerUtils.getParentFeatures(foundFeature, possibleParents, false);
//
//		// none of my parents consider me as optional OR I have no parents,
//		// consider as root feature
//		if (allOptionalParents.length == 0) {
//			if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_RECONCILER) {
//				UpdateCore.debug("There are no features who consider the feature as optional. Treat as root feature.");
//			}
//			newSitePolicy.unconfigure(foundFeature, true, false);
//			newFoundFeatures.add(foundFeature);
//			return;
//
//		}
//
//		//At least one of my parent considers me non optional
//		// consider root feature
//		if (allParents.length > allOptionalParents.length) {
//			if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_RECONCILER) {
//				UpdateCore.debug("At least one parent considers the feature as NON optional. Treat as root feature.");
//			}
//			newSitePolicy.unconfigure(foundFeature, true, false);
//			newFoundFeatures.add(foundFeature);
//			return;
//		}
//
//		for (int i = 0; i < allOptionalParents.length; i++) {
//			// one parent that consider me optional is enable, enable feature
//			if (oldSitePolicy.isConfigured(allOptionalParents[i])) {
//				if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_RECONCILER) {
//					UpdateCore.debug("Found parent feature:" + allOptionalParents[i] + " as enable: Enable optional child feature:" + foundFeature);
//				}
//				newSitePolicy.configure(foundFeature, true, false);
//				return;
//			}
//		}
//
//		// found parent that consider me optional but they are all disable
//		// unconfigure feature without adding it to the list fo new found features
//		if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_RECONCILER) {
//			UpdateCore.debug("No parents are enable. Disable feature.");
//		}
//		newSitePolicy.unconfigure(foundFeature, true, false);
//
//	}
//
//	/**
//	* Validate we have only one configured feature per site
//	* even if we found multiples
//	* 
//	* If we find 2 features, the one with a higher version is configured
//	* If they have the same version, the first feature is configured
//	* 
//	* DO NOT check across sites [17980]
//	* If Feature1 is installed natively on Site A
//	* If Feature1 is installed on Site B
//	* If Feature1 from SiteA is removed... 
//	*/
//	private void checkConfiguredFeatures(IInstallConfiguration newDefaultConfiguration) throws CoreException {
//
//		IConfiguredSite[] configuredSites = newDefaultConfiguration.getConfiguredSites();
//
//		// each configured site
//		for (int indexConfiguredSites = 0; indexConfiguredSites < configuredSites.length; indexConfiguredSites++) {
//			checkConfiguredFeatures(configuredSites[indexConfiguredSites]);
//		}
//	}

	/**
	 * Validate we have only one configured feature of a specific id
	 * per configured site
	 */
	public static void checkConfiguredFeaturesOld(IConfiguredSite configuredSite) throws CoreException {

		// NOT USED

		ConfiguredSite cSite = (ConfiguredSite) configuredSite;
		IFeatureReference[] configuredFeatures = cSite.getConfiguredFeatures();
		ConfigurationPolicy cPolicy = cSite.getConfigurationPolicy();

		// TRACE
		if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_RECONCILER) {
			UpdateCore.debug("Compare features within :" + configuredSite.getSite().getURL());
		}

		for (int indexConfiguredFeatures = 0; indexConfiguredFeatures < configuredFeatures.length - 1; indexConfiguredFeatures++) {

			IFeatureReference featureToCompare = configuredFeatures[indexConfiguredFeatures];

			// within the configured site
			// compare with the other configured features of this site
			for (int restOfConfiguredFeatures = indexConfiguredFeatures + 1; restOfConfiguredFeatures < configuredFeatures.length; restOfConfiguredFeatures++) {
				int result = compare(featureToCompare, configuredFeatures[restOfConfiguredFeatures]);
				if (result != 0) {
					if (result == 1) {
						cPolicy.unconfigure(configuredFeatures[restOfConfiguredFeatures], true, false);
					}
					if (result == 2) {
						cPolicy.unconfigure(featureToCompare, true, false);
					}
				}
			}
		}
	}

	/**
	 * compare two feature references
	 * returns 0 if the feature are different
	 * returns 1 if the version of feature 1 is greater than the version of feature 2
	 * returns 2 if opposite
	 */
	private static int compare(IFeatureReference featureRef1, IFeatureReference featureRef2) throws CoreException {

		// TRACE
		if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_RECONCILER) {
			UpdateCore.debug("Compare: " + featureRef1 + " && " + featureRef2);
		}

		if (featureRef1 == null)
			return 0;

		IFeature feature1 = null;
		IFeature feature2 = null;
		try {
			feature1 = featureRef1.getFeature(null);
			feature2 = featureRef2.getFeature(null);
		} catch (CoreException e) {
			UpdateCore.warn(null, e);
			return 0;
		}

		if (feature1 == null || feature2 == null) {
			return 0;
		}

		VersionedIdentifier id1 = feature1.getVersionedIdentifier();
		VersionedIdentifier id2 = feature2.getVersionedIdentifier();

		if (id1 == null || id2 == null) {
			return 0;
		}

		if (id1.getIdentifier() != null && id1.getIdentifier().equals(id2.getIdentifier())) {
			PluginVersionIdentifier version1 = id1.getVersion();
			PluginVersionIdentifier version2 = id2.getVersion();
			if (version1 != null) {
				if (version1.isGreaterThan(version2)) {
					return 1;
				} else {
					return 2;
				}
			} else {
				return 2;
			}
		}
		return 0;
	}

//	/*
//	 * 
//	 */
//	private ConfiguredSite createNewConfigSite(IConfiguredSite oldConfiguredSiteToReconcile) throws CoreException {
//		// create a copy of the ConfigSite based on old ConfigSite
//		// this is not a clone, do not copy any features
//		ConfiguredSite cSiteToReconcile = (ConfiguredSite) oldConfiguredSiteToReconcile;
//		SiteModel siteModel = cSiteToReconcile.getSiteModel();
//		int policy = cSiteToReconcile.getConfigurationPolicy().getPolicy();
//
//		// copy values of the old ConfigSite that should be preserved except Features
//		ConfiguredSite newConfigurationSite = (ConfiguredSite) new BaseSiteLocalFactory().createConfigurationSiteModel(siteModel, policy);
//		newConfigurationSite.setUpdatable(cSiteToReconcile.isUpdatable());
//		newConfigurationSite.setEnabled(cSiteToReconcile.isEnabled());
//		newConfigurationSite.setPlatformURLString(cSiteToReconcile.getPlatformURLString());
//
//		return newConfigurationSite;
//	}

	/*
	 * 
	 */
	private IFeatureReference[] getFeatureReferences() {
		if (newFoundFeatures == null || newFoundFeatures.size() == 0)
			return new IFeatureReference[0];

		return (IFeatureReference[]) newFoundFeatures.toArray(arrayTypeFor(newFoundFeatures));
	}

//	/*
//	 * 
//	 */
//	private boolean saveNewFeatures(IInstallConfiguration installConfig) throws CoreException {
//
//		if (getFeatureReferences().length == 0) {
//			UpdateCore.warn("No new features found");
//			return false;
//		}
//
//		// recompute list of new features to only keep root features [16496]
//		IFeatureReference[] refs = getFeatureReferences();
//		newFoundFeatures = new ArrayList();
//		for (int i = 0; i < refs.length; i++) {
//			IFeatureReference[] parents = UpdateManagerUtils.getParentFeatures(refs[i], refs, false);
//			if (parents.length == 0)
//				newFoundFeatures.add(refs[i]);
//		}
//
//
//		if (getFeatureReferences().length == 0) {
//			UpdateCore.warn("No root feature found when saving new features");
//			return false;
//		}
//
//		// remove efixes from the delta that patch disabled feature from the installConfig
//		// bug 71730
//		removeInvalidEfixes(installConfig);
//
//		if (getFeatureReferences().length == 0) {
//			UpdateCore.warn("No new features found after removing invalid efixes");
//			return false;
//		}
//
//
//		date = new Date();
//		String fileName = UpdateManagerUtils.getLocalRandomIdentifier(DEFAULT_INSTALL_CHANGE_NAME, date);
//		IPath path = UpdateCore.getPlugin().getStateLocation();
//		IPath filePath = path.append(fileName);
//		File file = filePath.toFile();
//		// persist list of new features 
//		try {
//			UpdateManagerUtils.Writer writer = UpdateManagerUtils.getWriter(file, "UTF-8");
//			writer.write(this);
//			return true;
//		} catch (UnsupportedEncodingException e) {
//			throw Utilities.newCoreException(Policy.bind("SiteReconciler.UnableToEncodeConfiguration", file.getAbsolutePath()), e);
//			//$NON-NLS-1$
//		} catch (FileNotFoundException e) {
//			throw Utilities.newCoreException(Policy.bind("SiteReconciler.UnableToSaveStateIn", file.getAbsolutePath()), e);
//			//$NON-NLS-1$
//		}
//	}

	/*
	 * @see IWritable#write(int, PrintWriter)
	 */
	public void write(int indent, PrintWriter w) {
		String gap = ""; //$NON-NLS-1$
		for (int i = 0; i < indent; i++)
			gap += " "; //$NON-NLS-1$
		String increment = ""; //$NON-NLS-1$
		for (int i = 0; i < IWritable.INDENT; i++)
			increment += " "; //$NON-NLS-1$		

		// CHANGE tag
		w.print(gap + "<" + InstallChangeParser.CHANGE + " ");
		//$NON-NLS-1$ //$NON-NLS-2$
		long time = (date != null) ? date.getTime() : 0L;
		w.println("date=\"" + time + "\" >"); //$NON-NLS-1$ //$NON-NLS-2$

		// NEW FEATURE
		w.println(gap + increment + "<" + InstallChangeParser.NEW_FEATURE + " >");

		// FEATURE REF
		IFeatureReference[] references = getFeatureReferences();
		String URLFeatureString = null;
		if (references != null) {
			for (int index = 0; index < references.length; index++) {
				IFeatureReference ref = references[index];
				if (ref.getURL() != null) {
					ISite featureSite = ref.getSite();
					URLFeatureString = UpdateManagerUtils.getURLAsString(featureSite.getURL(), ref.getURL());

					w.print(gap + increment + increment + "<" + InstallChangeParser.REFERENCE + " ");
					//$NON-NLS-1$
					w.println("siteURL = \"" + UpdateManagerUtils.Writer.xmlSafe(getURLSiteString(featureSite)) + "\" ");
					//$NON-NLS-1$ //$NON-NLS-2$
					w.println(gap + increment + increment + increment + "featureURL=\"" + UpdateManagerUtils.Writer.xmlSafe(URLFeatureString) + "\" />");
					//$NON-NLS-1$ //$NON-NLS-2$
				}
				w.println(""); //$NON-NLS-1$
			}
		}

		// END NEW FEATURE
		w.println(gap + increment + "</" + InstallChangeParser.NEW_FEATURE + " >");

		// end
		w.println(gap + "</" + InstallChangeParser.CHANGE + ">");
		//$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * Returns the Site URL, attempting to replace it by platform: URL if needed
	 */
	private String getURLSiteString(ISite site) {
		// since 2.0.2 ISite.getConfiguredSite();
		ConfiguredSite cSite = (ConfiguredSite) site.getCurrentConfiguredSite();
		if (cSite != null)
			return cSite.getPlatformURLString();
		return site.getURL().toExternalForm();
	}

//	/*
//	 * return true if the platformBase URL is not the same
//	 * we thought it is. In this case we should reconcile in an optimistic way 
//	 */
//	private boolean platformBaseChanged(IConfiguredSite[] oldConfiguredSites) {
//
//		if (oldConfiguredSites == null) {
//			UpdateCore.warn("No previous configured sites. Optimistic reconciliation.");
//			return true;
//		}
//
//		String platformString = "platform:/base/";
//		URL platformURL = null;
//		try {
//			platformURL = new URL(platformString);
//		} catch (MalformedURLException e) {
//			UpdateCore.warn("Unable to resolve platform:/base/. Check you are running a Platform", e);
//			return true;
//		}
//		URL resolvedCurrentBaseURL = null;
//		try {
//			resolvedCurrentBaseURL = Platform.resolve(platformURL);
//		} catch (IOException e) {
//			UpdateCore.warn("Error while resolving platform:/base/. Check you are running a Platform", e);
//			return true;
//		}
//
//		// find the 'platform:/base/' configuredSite
//		int index = 0;
//		boolean found = false;
//		ConfiguredSite cSite = null;
//		while (!found && index < oldConfiguredSites.length) {
//			if (oldConfiguredSites[index] instanceof ConfiguredSite) {
//				cSite = (ConfiguredSite) oldConfiguredSites[index];
//				if (platformString.equalsIgnoreCase(cSite.getPlatformURLString())) {
//					found = true;
//				}
//			}
//			index++;
//		}
//
//		if (!found) {
//			UpdateCore.warn("Unable to find an old configured site with platform:/base/ as a platform URL");
//			return true;
//		}
//
//		if (cSite == null) {
//			UpdateCore.warn("The configuredSite that contains the platform is null");
//			return true;
//		}
//
//		if (UpdateManagerUtils.sameURL(resolvedCurrentBaseURL, cSite.getSite().getURL())) {
//			UpdateCore.warn("Platform URL found are the same:" + resolvedCurrentBaseURL + " : " + cSite.getSite().getURL());
//			return false;
//		}
//
//		UpdateCore.warn("Platform URL found is different than the one previously saved. Reconcile optimistically:" + resolvedCurrentBaseURL + " : " + cSite.getSite().getURL());
//		return true;
//	}

	/**
	 * Validate the list of configured features eliminating extra
	 * entries (if possible). Make sure we do not leave configured
	 * nested features with "holes" (ie. unconfigured children)
	 */
	public static void checkConfiguredFeatures(IConfiguredSite configuredSite) {

		// Note: if we hit errors in the various computation
		// methods and throw a CoreException, we will not catch it
		// in this method. Consequently we will not attempt to
		// unconfigure any "extra" features because we would 
		// likely get it wrong. The platform will run with extra features
		// configured. The runtime will eliminate extra plugins based
		// on runtime binding rules.

		// determine "proposed" list of configured features
		ConfiguredSite cSite = (ConfiguredSite) configuredSite;
		// debug
		if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_RECONCILER) {
			UpdateCore.debug("Validate configuration of site " + cSite.getSite().getURL());
		}
		IFeatureReference[] configuredRefs = cSite.getConfiguredFeatures();
		ArrayList allPossibleConfiguredFeatures = new ArrayList();
		for (int i = 0; i < configuredRefs.length; i++) {
			try {
				IFeature feature = configuredRefs[i].getFeature(null);
				allPossibleConfiguredFeatures.add(feature);
				// debug
				if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_RECONCILER) {
					UpdateCore.debug("   configured feature " + feature.getVersionedIdentifier().toString());
				}
			} catch (CoreException e) {
				UpdateCore.warn("", e);
			}
		}

		// find top level features
		ArrayList topFeatures = computeTopFeatures(allPossibleConfiguredFeatures);

		// find non efix top level features
		ArrayList topNonEfixFeatures = getNonEfixFeatures(topFeatures);

		// expand non efix top level features (compute full nesting structures).
		ArrayList configuredFeatures = expandFeatures(topNonEfixFeatures, configuredSite);

		// retrieve efixes that patch enable feature
		// they must be kept enabled
		if (topFeatures.size() != topNonEfixFeatures.size()) {
			Map patches = getPatchesAsFeature(allPossibleConfiguredFeatures);
			if (!patches.isEmpty()) {
				// calculate efixes to enable
				List efixesToEnable = getPatchesToEnable(patches, configuredFeatures);
				// add efies to keep enable
				//add them to the enable list
				for (Iterator iter = efixesToEnable.iterator(); iter.hasNext();) {
					IFeature element = (IFeature) iter.next();
					ArrayList expandedEfix = new ArrayList();
					expandEfixFeature(element, expandedEfix, configuredSite);
					configuredFeatures.addAll(expandedEfix);
				}
			}
		}

		// compute extra features
		ArrayList extras = diff(allPossibleConfiguredFeatures, configuredFeatures);

		// unconfigure extra features
		ConfigurationPolicy cPolicy = cSite.getConfigurationPolicy();
		for (int i = 0; i < extras.size(); i++) {
			IFeature feature = (IFeature) extras.get(i);
			IFeatureReference ref = cSite.getSite().getFeatureReference(feature);
			try {
				cPolicy.unconfigure(ref, true, false);
				// debug
				if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_RECONCILER) {
					UpdateCore.debug("Unconfiguring \"extra\" feature " + feature.getVersionedIdentifier().toString());
				}
			} catch (CoreException e) {
				UpdateCore.warn("", e);
			}
		}
	}

	/*
	 *  
	 */
	private static ArrayList computeTopFeatures(ArrayList features) {
		/* map of Feature by VersionedIdentifier */
		Map topFeatures = new HashMap(features.size());
		// start with the features passed in
		for (Iterator it = features.iterator(); it.hasNext();) {
			IFeature f = ((IFeature) it.next());
			topFeatures.put(f.getVersionedIdentifier(), f);
		}
		// remove all features that nest in some other feature
		for (Iterator it = features.iterator(); it.hasNext();) {
			try {
				IIncludedFeatureReference[] children = ((IFeature) it.next()).getIncludedFeatureReferences();
				for (int j = 0; j < children.length; j++) {
					try {
						topFeatures.remove(children[j].getVersionedIdentifier());
					} catch (CoreException e1) {
						if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_WARNINGS)
							UpdateCore.warn("", e1);
					}
				}
			} catch (CoreException e) {
				UpdateCore.warn("", e);
			}
		}
		ArrayList list = new ArrayList();
		list.addAll(topFeatures.values());
		// debug
		if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_RECONCILER) {
			UpdateCore.debug("Computed top-level features");
			for (int i = 0; i < topFeatures.size(); i++) {
				UpdateCore.debug("   " + ((IFeature) list.get(i)).getVersionedIdentifier().toString());
			}
		}
		return list;
	}

	/*
	 * 
	 */
	private static ArrayList expandFeatures(ArrayList features, IConfiguredSite configuredSite) {
		ArrayList result = new ArrayList();

		// expand all top level features
		for (int i = 0; i < features.size(); i++) {
			expandFeature((IFeature) features.get(i), result, configuredSite);
		}

		return result;
	}

	/*
	 * 
	 */
	private static void expandFeature(IFeature feature, ArrayList features, IConfiguredSite configuredSite) {

		// add feature
		if (!features.contains(feature)) {
			features.add(feature);
			// debug
			if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_RECONCILER) {
				UpdateCore.debug("Retaining configured feature " + feature.getVersionedIdentifier().toString());
			}
		}

		// add nested children to the list
		IIncludedFeatureReference[] children = null;
		try {
			children = feature.getIncludedFeatureReferences();
		} catch (CoreException e) {
			UpdateCore.warn("", e);
			return;
		}

		for (int j = 0; j < children.length; j++) {
			IFeature child = null;
			try {
				child = children[j].getFeature(null);
			} catch (CoreException e) {
				if (!UpdateManagerUtils.isOptional(children[j]))
					UpdateCore.warn("", e);
				// 25202 do not return right now, the peer children may be ok
			}
			if (child != null)
				expandFeature(child, features, configuredSite);
		}
	}

	/*
	 * 
	 */
	private static ArrayList diff(ArrayList left, ArrayList right) {
		ArrayList result = new ArrayList();

		// determine difference (left "minus" right)
		for (int i = 0; i < left.size(); i++) {
			IFeature feature = (IFeature) left.get(i);
			if (!right.contains(feature))
				result.add(feature);
		}
		return result;
	}

	/*
	 * get the list of enabled patches
	 */
	private static Map getPatchesAsFeature(ArrayList allConfiguredFeatures) {
		// get all efixes and the associated patched features
		Map patches = new HashMap();
		if (allConfiguredFeatures != null) {
			Iterator iter = allConfiguredFeatures.iterator();
			while (iter.hasNext()) {
				List patchedFeaturesID = new ArrayList();
				IFeature element = (IFeature) iter.next();
				// add the patched feature identifiers
				for (int i = 0; i < element.getImports().length; i++) {
					if (element.getImports()[i].isPatch()) {
						VersionedIdentifier id = element.getImports()[i].getVersionedIdentifier();
						if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_RECONCILER)
							UpdateCore.debug("Found patch " + element + " for feature identifier " + id);
						patchedFeaturesID.add(id);
					}
				}

				if (!patchedFeaturesID.isEmpty()) {
					patches.put(element, patchedFeaturesID);
				}
			}
		}

		return patches;
	}

	/*
	 * retruns the list of pathes-feature who patch enabled features
	 */
	private static List getPatchesToEnable(Map efixes, ArrayList configuredFeatures) {

		ArrayList enabledVersionedIdentifier = new ArrayList();
		Iterator iter = configuredFeatures.iterator();
		while (iter.hasNext()) {
			IFeature element = (IFeature) iter.next();
			enabledVersionedIdentifier.add(element.getVersionedIdentifier());
		}

		// loop through the patches
		List result = new ArrayList();
		iter = efixes.keySet().iterator();
		while (iter.hasNext()) {
			boolean toEnable = false;
			IFeature efixFeature = (IFeature) iter.next();
			List patchedFeatures = (List) efixes.get(efixFeature);
			// loop through the 'patched features identifier' the for this patch
			// see if it the patch patches at least one enable feature
			Iterator patchedFeaturesIter = patchedFeatures.iterator();
			while (patchedFeaturesIter.hasNext() && !toEnable) {
				VersionedIdentifier patchedFeatureID = (VersionedIdentifier) patchedFeaturesIter.next();
				if (enabledVersionedIdentifier.contains(patchedFeatureID)) {
					toEnable = true;
				}
			}

			if (!toEnable) {
				if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_RECONCILER)
				UpdateCore.debug("The Patch " + efixFeature + " does not patch any enabled features: it will be disabled");
			} else {
				if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_RECONCILER)
					UpdateCore.debug("The patch " + efixFeature + " will be enabled.");
				result.add(efixFeature);
			}
		}
		return result;
	}

	/*
	 * returns the feature that are not patches
	 */
	private static ArrayList getNonEfixFeatures(ArrayList topFeatures) {
		Map efixFeatures = getPatchesAsFeature(topFeatures);
		Set keySet = efixFeatures.keySet();
		if (keySet == null || keySet.isEmpty())
			return topFeatures;

		Iterator iter = topFeatures.iterator();
		ArrayList result = new ArrayList();
		while (iter.hasNext()) {
			IFeature element = (IFeature) iter.next();
			if (!keySet.contains(element)) {
				result.add(element);
			}
		}
		return result;
	}


//	/*
//	 * get the map of enabled patches (as feature reference)  or an empty map
//	 */
//	private Map getPatchesAsFeatureReference(List listOfFeatureReferences) {
//		// get all efixes and the associated patched features
//		Map patches = new HashMap();
//		if (listOfFeatureReferences != null) {
//			Iterator iter = listOfFeatureReferences.iterator();
//			while (iter.hasNext()) {
//				List patchedFeaturesID = new ArrayList();
//				IFeatureReference element = (IFeatureReference) iter.next();
//				// add the patched feature identifiers
//				try {
//					IFeature feature = element.getFeature(null);
//					if (feature != null) {
//						IImport[] imports = feature.getImports();
//						for (int i = 0; i < imports.length; i++) {
//							if (imports[i].isPatch()) {
//								VersionedIdentifier id = imports[i].getVersionedIdentifier();
//								if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_RECONCILER)
//								UpdateCore.debug("Found patch " + element + " for feature identifier " + id);
//								patchedFeaturesID.add(id);
//							}
//						}
//					}
//
//					if (!patchedFeaturesID.isEmpty()) {
//						patches.put(element, patchedFeaturesID);
//					}
//				} catch (CoreException e) {
//				}
//			}
//		}
//
//		return patches;
//	}


//	/*
//	 * Removes efixes from new found features if they do not patch an enable feature
//	 * either from the found delta or from the file system
//	 */
//	private void removeInvalidEfixes(IInstallConfiguration installConfig) {
//
//		// disable new found efixes if the feature is not in the list nor enabled features
//		// on the file system
//		Map newFoundEfixesAsReference = getPatchesAsFeatureReference(newFoundFeatures);
//
//		if (newFoundEfixesAsReference.size() > 0) {
//			// retrieve all enabled features on all the sites
//			ArrayList allEnabledFeatures = new ArrayList();
//			IConfiguredSite[] configSites = installConfig.getConfiguredSites();
//			for (int i = 0; i < configSites.length; i++) {
//				IFeatureReference[] references = configSites[i].getConfiguredFeatures();
//				for (int j = 0; j < references.length; j++) {
//					try {
//						allEnabledFeatures.add(references[j].getFeature(null));
//					} catch (CoreException e) {
//					}
//				}
//			}
//
//			// create a List of eFixes Features
//			List arrayOfNewFoundFeatures = new ArrayList();
//			for (Iterator iter = newFoundFeatures.iterator(); iter.hasNext();) {
//				IFeatureReference element = (IFeatureReference) iter.next();
//				try {
//					arrayOfNewFoundFeatures.add(element.getFeature(null));
//				} catch (CoreException e) {
//				}
//			}
//
//			// retrieve the efixes that patch enable features in delta and enabled features on the 
//			// file system
//			List patchesForNewFoundFeatures = getFeatureReferencePatchesToEnable(newFoundEfixesAsReference, allEnabledFeatures);
//			List patchesForEnabledFeatures = getFeatureReferencePatchesToEnable(newFoundEfixesAsReference, arrayOfNewFoundFeatures);
//
//			// IMPORTANT: add efixes first so they will be processed first
//			newFoundFeatures.removeAll(newFoundEfixesAsReference.keySet());
//			newFoundFeatures.addAll(0,patchesForEnabledFeatures);
//			newFoundFeatures.addAll(0,patchesForNewFoundFeatures);
//		}
//	}

//	/*
//	 * retruns the list of pathes-featureReference who patch enabled features
//	 */
//	private List getFeatureReferencePatchesToEnable(Map efixes, List configuredFeatures) {
//
//		ArrayList enabledVersionedIdentifier = new ArrayList();
//		Iterator iter = configuredFeatures.iterator();
//		while (iter.hasNext()) {
//			IFeature element = (IFeature) iter.next();
//			enabledVersionedIdentifier.add(element.getVersionedIdentifier());
//		}
//
//		// loop through the patches
//		List result = new ArrayList();
//		iter = efixes.keySet().iterator();
//		while (iter.hasNext()) {
//			boolean toEnable = false;
//			IFeatureReference efixFeatureReference = (IFeatureReference) iter.next();
//			List patchedFeatures = (List) efixes.get(efixFeatureReference);
//			// loop through the 'patched features identifier' the for this patch
//			// see if it the patch patches at least one enable feature
//			Iterator patchedFeaturesIter = patchedFeatures.iterator();
//			while (patchedFeaturesIter.hasNext() && !toEnable) {
//				VersionedIdentifier patchedFeatureID = (VersionedIdentifier) patchedFeaturesIter.next();
//				if (enabledVersionedIdentifier.contains(patchedFeatureID)) {
//					toEnable = true;
//				}
//			}
//
//			if (!toEnable) {
//				if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_RECONCILER)
//				UpdateCore.debug("The Patch " + efixFeatureReference + " does not patch any enabled features: it will be disabled");
//			} else {
//				if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_RECONCILER)
//				UpdateCore.debug("The patch " + efixFeatureReference + " will be enabled.");
//
//				result.add(efixFeatureReference);
//			}
//		}
//		return result;
//	}

	/*
	 * only enable non-efix children recursively
	 */
	private static void expandEfixFeature(IFeature feature, ArrayList features, IConfiguredSite configuredSite) {

		// add feature
		if (!features.contains(feature)) {
			features.add(feature);
			// debug
			if (UpdateCore.DEBUG && UpdateCore.DEBUG_SHOW_RECONCILER) {
				UpdateCore.debug("Retaining configured feature " + feature.getVersionedIdentifier().toString());
			}
		}

		// add nested children to the list
		IIncludedFeatureReference[] children = null;
		try {
			children = feature.getIncludedFeatureReferences();
		} catch (CoreException e) {
			UpdateCore.warn("", e);
			return;
		}

		for (int j = 0; j < children.length; j++) {
			IFeature child = null;
			try {
				child = children[j].getFeature(null);
			} catch (CoreException e) {
				if (!children[j].isOptional())
					UpdateCore.warn("", e);
				// 25202 do not return right now, the peer children may be ok
			}
			if (child != null){
				if (!UpdateCore.isPatch(child))
					expandEfixFeature(child, features, configuredSite);
			}
		}
	}	
	
}

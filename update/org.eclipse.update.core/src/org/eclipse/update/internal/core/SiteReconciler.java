package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved. 
 */
import java.io.*;
import java.net.URL;
import java.util.*;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.*;
import org.eclipse.update.internal.model.ConfigurationActivityModel;
import org.eclipse.update.internal.model.InstallChangeParser;

/**
 * This class manages the reconciliation.
 */

public class SiteReconciler extends ModelObject implements IWritable {

	private SiteLocal siteLocal;
	private List newFoundFeatures;
	private Date date;
	private static final String DEFAULT_INSTALL_CHANGE_NAME = "delta.xml";
	//$NON-NLS-1$	

	/**
	 * 
	 */
	public SiteReconciler(SiteLocal siteLocal) {
		this.siteLocal = siteLocal;
	}

	/*
	 * Reconciliation is the comparison between the old preserved state and the new one from platform.cfg
	 * 
	 * If the old state contained sites that are not in the new state, the old sites are not added to the state
	 * If the new state contains sites that were not in the old state, configure the site and configure all the found features
	 * If the sites are in both states, verify the features
	 * if the old site contained features that are not in the new site, the features are not added to the site
	 * if the new site contains feature that were not in the old site, configure the new feature
	 * if the feature is in both site (old and new), use old feature state
	 * 
	 * When adding a feature to a site, we will check if the feature is broken or not. 
	 * A feature is broken when at least one of its plugin is not installed on the site.
	 * 
	 * At the end, go over all the site, get the configured features and make sure that if we find duplicates
	 * only one feature is configured
	 * 
	 * returns true if new features have been found during a pessimistic reconcile
	 * otherwise returns false
	 */
	public boolean reconcile(boolean isOptimistic) throws CoreException {

		IPlatformConfiguration platformConfig =
			BootLoader.getCurrentPlatformConfiguration();
		IPlatformConfiguration.ISiteEntry[] newSiteEntries =
			platformConfig.getConfiguredSites();
		IInstallConfiguration newDefaultConfiguration =
			siteLocal.cloneConfigurationSite(null, null, null);
		IConfiguredSite[] oldConfiguredSites = new IConfiguredSite[0];
		newFoundFeatures = new ArrayList();

		// sites from the current configuration
		if (siteLocal.getCurrentConfiguration() != null)
			oldConfiguredSites =
				siteLocal.getCurrentConfiguration().getConfiguredSites();

		// TRACE
		if (UpdateManagerPlugin.DEBUG
			&& UpdateManagerPlugin.DEBUG_SHOW_RECONCILER) {
			for (int i = 0; i < oldConfiguredSites.length; i++) {
				UpdateManagerPlugin.debug(
					"Old Site :" + oldConfiguredSites[i].getSite().getURL());
			}
		}

		// check if sites from the platform are new sites or modified sites
		// if they are new add them, if they are modified, compare them with the old
		// one and add them
		for (int siteIndex = 0;
			siteIndex < newSiteEntries.length;
			siteIndex++) {

			IPlatformConfiguration.ISiteEntry currentSiteEntry =
				newSiteEntries[siteIndex];
			URL resolvedURL = resolveSiteEntry(currentSiteEntry);
			boolean found = false;
			IConfiguredSite currentConfigurationSite = null;

			// TRACE
			if (UpdateManagerPlugin.DEBUG
				&& UpdateManagerPlugin.DEBUG_SHOW_RECONCILER) {
				UpdateManagerPlugin.debug(
					"New Site?:" + resolvedURL);
			}

			// check if SiteEntry has been possibly modified
			// if it was part of the previously known configuredSite
			for (int index = 0;
				index < oldConfiguredSites.length && !found;
				index++) {
				currentConfigurationSite = oldConfiguredSites[index];
				URL currentConfigURL =
					currentConfigurationSite.getSite().getURL();

				if (sameURL(resolvedURL, currentConfigURL)) {
					found = true;
					ConfiguredSite reconciledConfiguredSite =
						reconcile(currentConfigurationSite, isOptimistic);
					reconciledConfiguredSite.setPreviousPluginPath(
						currentSiteEntry.getSitePolicy().getList());
					newDefaultConfiguration.addConfiguredSite(
						reconciledConfiguredSite);
				}
			}

			// old site not found, this is a new site, create it
			if (!found) {
				// TRACE
				if (UpdateManagerPlugin.DEBUG
					&& UpdateManagerPlugin.DEBUG_SHOW_RECONCILER) {
					UpdateManagerPlugin.debug(
						"Configured Site to create:" + resolvedURL);
				}

				ISite site = SiteManager.getSite(resolvedURL);

				//site policy
				IPlatformConfiguration.ISitePolicy sitePolicy =
					currentSiteEntry.getSitePolicy();
				ConfiguredSite configSite =
					(ConfiguredSite) new BaseSiteLocalFactory()
						.createConfigurationSiteModel(
						(SiteModel) site,
						sitePolicy.getType());
				configSite.setPlatformURLString(
					currentSiteEntry.getURL().toExternalForm());
				configSite.setPreviousPluginPath(
					currentSiteEntry.getSitePolicy().getList());

				//the site may not be read-write
				configSite.isUpdatable(
					newSiteEntries[siteIndex].isUpdateable());

				// Add the features to the list of new found features
				// and configure it based on reconciliation type
				IFeatureReference[] newFeaturesRef =
					site.getFeatureReferences();
				for (int i = 0; i < newFeaturesRef.length; i++) {
					FeatureReferenceModel newFeatureRefModel =
						(FeatureReferenceModel) newFeaturesRef[i];

					// TRACE
					if (UpdateManagerPlugin.DEBUG
						&& UpdateManagerPlugin.DEBUG_SHOW_RECONCILER) {
						String reconciliationType =
							isOptimistic
								? "enable (optimistic)"
								: "disable (pessimistic)";
						UpdateManagerPlugin.debug(
							"New Site:New Feature: "
								+ newFeatureRefModel.getURLString()
								+ " as "
								+ reconciliationType);
					}
					
					if (isOptimistic) {
						configSite
							.getConfigurationPolicy()
							.addConfiguredFeatureReference(
							newFeatureRefModel);
					} else {
						configSite
							.getConfigurationPolicy()
							.addUnconfiguredFeatureReference(
							newFeatureRefModel);
						newFoundFeatures.add(newFeatureRefModel);
					}
				}
				newDefaultConfiguration.addConfiguredSite(configSite);
			}
		}

		// verify we do not have 2 features with different version that
		// are configured
		checkConfiguredFeatures(newDefaultConfiguration);

		// add Activity reconciliation
		BaseSiteLocalFactory siteLocalFactory = new BaseSiteLocalFactory();
		ConfigurationActivityModel activity =
			siteLocalFactory.createConfigurationAcivityModel();
		activity.setAction(IActivity.ACTION_RECONCILIATION);
		activity.setDate(new Date());
		activity.setLabel(siteLocal.getLocationURLString());
		((InstallConfiguration) newDefaultConfiguration).addActivityModel(
			activity);

		// add the configuration as the currentConfig
		siteLocal.addConfiguration(newDefaultConfiguration);
		siteLocal.save();

		if (getFeatureReferences().length != 0) {
			saveNewFeatures();
			return true;
		}

		return false;
	}

	/**
	 * 
	 */
	/*package */
	URL resolveSiteEntry(IPlatformConfiguration.ISiteEntry newSiteEntry)
		throws CoreException {
		URL resolvedURL = null;
		try {
			resolvedURL = Platform.resolve(newSiteEntry.getURL());
		} catch (IOException e) {
			throw Utilities.newCoreException(
				Policy.bind(
					"SiteLocal.UnableToResolve",
					newSiteEntry.getURL().toExternalForm()),
				e);
			//$NON-NLS-1$
		}
		return resolvedURL;
	}

	/**
	 * Compare the old state of ConfiguredSite with
	 * the 'real' features we found in Site
	 * 
	 * getSite of ConfiguredSite contains the real features found
	 * 
	 * So if ConfiguredSite.getPolicy has feature A and D as configured and C as unconfigured
	 * And if the Site contains features A,B and C
	 * We have to remove D and Configure B
	 * 
	 * We copy the oldConfig without the Features
	 * Then we loop through the features we found on teh real site
	 * If they didn't exist before we add them as configured
	 * Otherwise we use the old policy and add them to teh new configuration site
	 */
	private ConfiguredSite reconcile(
		IConfiguredSite oldConfiguredSite,
		boolean isOptimistic)
		throws CoreException {

		// TRACE
		if (UpdateManagerPlugin.DEBUG
			&& UpdateManagerPlugin.DEBUG_SHOW_RECONCILER) {
			UpdateManagerPlugin.debug(
				"Configured Site to reconfigure:"
					+ oldConfiguredSite.getSite().getURL());
		}

		ConfiguredSite newConfiguredSite =
			createNewConfigSite(oldConfiguredSite);
		ConfigurationPolicy newSitePolicy =
			newConfiguredSite.getConfigurationPolicy();
		ConfigurationPolicy oldSitePolicy =
			((ConfiguredSite) oldConfiguredSite).getConfigurationPolicy();

		// check the Features that are still on the new version of the Config Site
		// and the new one. Add the new Features as Configured
		List toCheck = new ArrayList();
		ISite site = oldConfiguredSite.getSite();
		IFeatureReference[] foundFeatures = site.getFeatureReferences();
		IFeatureReference[] oldConfiguredFeaturesRef =
			oldConfiguredSite.getFeatureReferences();

		// TRACE
		if (UpdateManagerPlugin.DEBUG
			&& UpdateManagerPlugin.DEBUG_SHOW_RECONCILER) {
			for (int i = 0; i < oldConfiguredFeaturesRef.length; i++) {
				UpdateManagerPlugin.debug(
					"Old feature :" + oldConfiguredFeaturesRef[i].getURL());
			}
		}

		for (int i = 0; i < foundFeatures.length; i++) {
			boolean newFeatureFound = false;
			FeatureReferenceModel currentFeatureRefModel =
				(FeatureReferenceModel) foundFeatures[i];

			// TRACE
			if (UpdateManagerPlugin.DEBUG
				&& UpdateManagerPlugin.DEBUG_SHOW_RECONCILER) {
				UpdateManagerPlugin.debug(
					"New feature? :" + currentFeatureRefModel.getURL());
			}

			// is is a brand new feature ?	
			for (int j = 0; j < oldConfiguredFeaturesRef.length; j++) {
				IFeatureReference oldFeatureRef = oldConfiguredFeaturesRef[j];
				if (oldFeatureRef != null
					&& oldFeatureRef.equals(currentFeatureRefModel)) {
					toCheck.add(oldFeatureRef);
					newFeatureFound = true;
				}
			}

			// new feature found: add as configured if the policy is optimistic
			if (!newFeatureFound) {
				// TRACE
				if (UpdateManagerPlugin.DEBUG
					&& UpdateManagerPlugin.DEBUG_SHOW_RECONCILER) {
					String reconciliationType =
						isOptimistic
							? "enable (optimistic)"
							: "disable (pessimistic)";
					UpdateManagerPlugin.debug(
						"New Feature: "
							+ currentFeatureRefModel.getURLString()
							+ " as "
							+ reconciliationType);
				}
				if (isOptimistic) {
					newSitePolicy.addConfiguredFeatureReference(
						currentFeatureRefModel);
				} else {
					newSitePolicy.addUnconfiguredFeatureReference(
						currentFeatureRefModel);
					newFoundFeatures.add(currentFeatureRefModel);
				}
			}
		}

		// if a feature has been found in new and old state use old state (configured/unconfigured)
		Iterator featureIter = toCheck.iterator();
		while (featureIter.hasNext()) {
			IFeatureReference oldFeatureRef =
				(IFeatureReference) featureIter.next();
			if (oldSitePolicy.isConfigured(oldFeatureRef)) {
				newSitePolicy.addConfiguredFeatureReference(oldFeatureRef);
			} else {
				newSitePolicy.addUnconfiguredFeatureReference(oldFeatureRef);
			}
		}

		return newConfiguredSite;
	}

	/**
	 * Validate we have only one configured feature across the different sites
	 * even if we found multiples
	 * 
	 * If we find 2 features, the one with a higher version is configured
	 * If they have teh same version, the first feature is configured
	 * 
	 * This is a double loop comparison
	 * One look goes from 0 to numberOfConfiguredSites -1
	 * the other from the previous index to numberOfConfiguredSites
	 * 
	 */
	private void checkConfiguredFeatures(IInstallConfiguration newDefaultConfiguration)
		throws CoreException {

		IConfiguredSite[] configuredSites =
			newDefaultConfiguration.getConfiguredSites();

		// each configured site
		for (int indexConfiguredSites = 0;
			indexConfiguredSites < configuredSites.length;
			indexConfiguredSites++) {
			checkConfiguredFeatures(configuredSites[indexConfiguredSites]);
		}

		// Check configured sites between them
		if (configuredSites.length > 1) {
			
			// TRACE
			if (UpdateManagerPlugin.DEBUG
				&& UpdateManagerPlugin.DEBUG_SHOW_RECONCILER) {
				UpdateManagerPlugin.debug(
					"Compare Feature between different sites:");
			}			
			
			for (int indexConfiguredSites = 0;
				indexConfiguredSites < configuredSites.length - 1;
				indexConfiguredSites++) {
					
				IFeatureReference[] configuredFeatures =
					configuredSites[indexConfiguredSites]
						.getConfiguredFeatures();
						
				for (int indexConfiguredFeatures = 0;
					indexConfiguredFeatures < configuredFeatures.length;
					indexConfiguredFeatures++) {
						
					IFeatureReference featureToCompare =
						configuredFeatures[indexConfiguredFeatures];

					// compare with the rest of the configurations
					for (int i = indexConfiguredSites + 1;
						i < configuredSites.length;
						i++) {
						IFeatureReference[] possibleFeatureReference =
							configuredSites[i].getConfiguredFeatures();
						for (int j = 0;
							j < possibleFeatureReference.length;
							j++) {
							int result =
								compare(
									featureToCompare,
									possibleFeatureReference[j]);
							if (result != 0) {
								if (result == 1) {
									FeatureReferenceModel featureRefModel =
										(FeatureReferenceModel) possibleFeatureReference[j];
									((ConfiguredSite) configuredSites[i])
										.getConfigurationPolicy()
										.addUnconfiguredFeatureReference(featureRefModel);
								};
								if (result == 2) {
									FeatureReferenceModel featureRefModel =
										(FeatureReferenceModel) featureToCompare;
									(
										(ConfiguredSite) configuredSites[indexConfiguredSites])
										.getConfigurationPolicy()
										.addUnconfiguredFeatureReference(featureRefModel);
									// do not break, we can continue, even if the feature is unconfigured
									// if we find another feature in another site, we may unconfigure it.
									// but it would have been unconfigured anyway because we confured another version of the same feature
									// so if teh version we find is lower than our version, by transition, it is lower then the feature that unconfigured us
								}
							}
						}
					}
				}

			}
		}
	}

	/**
	 * Validate we have only one configured feature of a specific id
	 * per configured site
	 */
	private void checkConfiguredFeatures(IConfiguredSite configuredSite)
		throws CoreException {

		ConfiguredSite cSite = (ConfiguredSite) configuredSite;
		IFeatureReference[] configuredFeatures = cSite.getConfiguredFeatures();
		ConfigurationPolicy cPolicy = cSite.getConfigurationPolicy();

				// TRACE
		if (UpdateManagerPlugin.DEBUG
			&& UpdateManagerPlugin.DEBUG_SHOW_RECONCILER) {
			UpdateManagerPlugin.debug(
				"Compare features within :"
					+ configuredSite.getSite().getURL());
		}

		for (int indexConfiguredFeatures = 0;
			indexConfiguredFeatures < configuredFeatures.length - 1;
			indexConfiguredFeatures++) {

			IFeatureReference featureToCompare =
				configuredFeatures[indexConfiguredFeatures];

			// within the configured site
			// compare with the other configured features of this site
			for (int restOfConfiguredFeatures = indexConfiguredFeatures + 1;
				restOfConfiguredFeatures < configuredFeatures.length;
				restOfConfiguredFeatures++) {
				int result =
					compare(
						featureToCompare,
						configuredFeatures[restOfConfiguredFeatures]);
				if (result != 0) {
					if (result == 1) {
						cPolicy.addUnconfiguredFeatureReference(
							(FeatureReferenceModel) configuredFeatures[restOfConfiguredFeatures]);
					};
					if (result == 2) {
						cPolicy.addUnconfiguredFeatureReference(
							(FeatureReferenceModel) featureToCompare);
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
	private int compare(
		IFeatureReference featureRef1,
		IFeatureReference featureRef2)
		throws CoreException {
			
		// TRACE
		if (UpdateManagerPlugin.DEBUG
			&& UpdateManagerPlugin.DEBUG_SHOW_RECONCILER) {
			UpdateManagerPlugin.debug(
				"Compare: "
					+ featureRef1
					+ " && "
					+ featureRef2);
		}
					
		if (featureRef1 == null)
			return 0;

		IFeature feature1 = null;
		IFeature feature2 = null;
		try {
			feature1 = featureRef1.getFeature();
			feature2 = featureRef2.getFeature();
		} catch (CoreException e) {
			UpdateManagerPlugin.warn(null,e);			
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

		if (id1.getIdentifier() != null
			&& id1.getIdentifier().equals(id2.getIdentifier())) {
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
	};

	/*
	 * 
	 */
	private ConfiguredSite createNewConfigSite(IConfiguredSite oldConfiguredSiteToReconcile)
		throws CoreException {
		// create a copy of the ConfigSite based on old ConfigSite
		// this is not a clone, do not copy any features
		ConfiguredSite cSiteToReconcile =
			(ConfiguredSite) oldConfiguredSiteToReconcile;
		SiteModel siteModel = cSiteToReconcile.getSiteModel();
		int policy = cSiteToReconcile.getConfigurationPolicy().getPolicy();

		// copy values of the old ConfigSite that should be preserved except Features
		ConfiguredSite newConfigurationSite =
			(ConfiguredSite) new BaseSiteLocalFactory()
				.createConfigurationSiteModel(
				siteModel,
				policy);
		newConfigurationSite.isUpdatable(cSiteToReconcile.isUpdatable());
		newConfigurationSite.setPlatformURLString(
			cSiteToReconcile.getPlatformURLString());

		return newConfigurationSite;
	}

	/*
	 * 
	 */
	private IFeatureReference[] getFeatureReferences() {
		if (newFoundFeatures == null || newFoundFeatures.size() == 0)
			return new IFeatureReference[0];

		return (IFeatureReference[]) newFoundFeatures.toArray(
			arrayTypeFor(newFoundFeatures));
	}

	/*
	 * 
	 */
	private void saveNewFeatures() throws CoreException {

		date = new Date();
		String fileName =
			UpdateManagerUtils.getLocalRandomIdentifier(
				DEFAULT_INSTALL_CHANGE_NAME,
				date);
		IPath path = UpdateManagerPlugin.getPlugin().getStateLocation();
		IPath filePath = path.append(fileName);
		File file = filePath.toFile();
		// persist list of new features 
		try {
			Writer writer = new Writer(file, "UTF8");
			writer.write(this);
		} catch (UnsupportedEncodingException e) {
			throw Utilities.newCoreException(
				Policy.bind(
					"SiteReconciler.UnableToEncodeConfiguration",
					file.getAbsolutePath()),
				e);
			//$NON-NLS-1$
		} catch (FileNotFoundException e) {
			throw Utilities.newCoreException(
				Policy.bind(
					"SiteReconciler.UnableToSaveStateIn",
					file.getAbsolutePath()),
				e);
			//$NON-NLS-1$
		}
	}

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
		w.println(
			gap + increment + "<" + InstallChangeParser.NEW_FEATURE + " >");

		// FEATURE REF
		IFeatureReference[] references = getFeatureReferences();
		String URLFeatureString = null;
		String URLSiteString = null;
		if (references != null) {
			for (int index = 0; index < references.length; index++) {
				IFeatureReference ref = references[index];
				if (ref.getURL() != null) {
					ISite featureSite = ref.getSite();
					URLFeatureString =
						UpdateManagerUtils.getURLAsString(
							featureSite.getURL(),
							ref.getURL());

					w.print(
						gap
							+ increment
							+ increment
							+ "<"
							+ InstallChangeParser.REFERENCE
							+ " ");
					//$NON-NLS-1$
					w.println(
						"siteURL = \""
							+ Writer.xmlSafe(getURLSiteString(featureSite))
							+ "\" ");
					//$NON-NLS-1$ //$NON-NLS-2$
					w.println(
						gap
							+ increment
							+ increment
							+ increment
							+ "featureURL=\""
							+ Writer.xmlSafe(URLFeatureString)
							+ "\" />");
					//$NON-NLS-1$ //$NON-NLS-2$
				}
				w.println(""); //$NON-NLS-1$
			}
		}

		// END NEW FEATURE
		w.println(
			gap + increment + "</" + InstallChangeParser.NEW_FEATURE + " >");

		// end
		w.println(gap + "</" + InstallChangeParser.CHANGE + ">");
		//$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * Returns the Site URL, attempting to replace it by platform: URL if needed
	 */
	private String getURLSiteString(ISite site) {

		IConfiguredSite[] configSites =
			siteLocal.getCurrentConfiguration().getConfiguredSites();
		for (int i = 0; i < configSites.length; i++) {
			if (configSites[i] instanceof ConfiguredSite) {
				ConfiguredSite cSite = (ConfiguredSite) configSites[i];
				if (site.equals(cSite.getSite())) {
					return cSite.getPlatformURLString();
				}
			}
		}
		return site.getURL().toExternalForm();
	}

	/*
	 * Compares two URL for equality
	 * Return false if one of them is null
	 */
	private boolean sameURL(URL url1, URL url2) {
		if (url1 == null)
			return false;
		if (url1.equals(url2))
			return true;

		// check if URL are file: URL as we may
		// have 2 URL pointing to the same featureReference
		// but with different representation
		// (i.e. file:/C;/ and file:C:/)
		if (!"file".equalsIgnoreCase(url1.getProtocol()))
			return false;
		if (!"file".equalsIgnoreCase(url2.getProtocol()))
			return false;

		File file1 = new File(url1.getFile());
		File file2 = new File(url2.getFile());

		if (file1 == null)
			return false;

		return (file1.equals(file2));
	}
}
package org.eclipse.update.internal.ui.forms;

import java.util.ArrayList;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.UpdateUIPlugin;
import org.eclipse.update.internal.ui.model.PendingChange;

/**
 *
 */
public class ActivityConstraints {
	private static final String KEY_ROOT_MESSAGE =
		"ActivityConstraints.rootMessage";
	private static final String KEY_PRIMARY = "ActivityConstraints.primary";
	private static final String KEY_PREREQ = "ActivityConstraints.prereq";
	private static final String KEY_CHILD_MESSAGE =
		"ActivityConstraints.childMessage";

	public static IStatus validatePendingChange(PendingChange job) {
		ArrayList children = new ArrayList();
		switch (job.getJobType()) {
			case PendingChange.UNCONFIGURE :
				validateUnconfigure(job.getFeature(), children);
				break;
			case PendingChange.CONFIGURE :
				validateConfigure(job.getFeature(), children);
				break;
			case PendingChange.INSTALL :
				validateInstall(
					job.getOldFeature(),
					job.getFeature(),
					children);
				break;
		}
		if (children.size() > 0) {
			IStatus[] carray =
				(IStatus[]) children.toArray(new IStatus[children.size()]);
			String message = UpdateUIPlugin.getResourceString(KEY_ROOT_MESSAGE);
			return new MultiStatus(
				UpdateUIPlugin.getPluginId(),
				IStatus.ERROR,
				carray,
				message,
				null);
		}
		return null;
	}

	private static void validateUnconfigure(
		IFeature feature,
		ArrayList children) {
		try {
			// test for the platform feature
			if (isPlatformFeature(feature)) {
				children.add(
					createStatus(
						feature,
						UpdateUIPlugin.getResourceString(KEY_PRIMARY)));
				// That's enough - there is no need to check the rest
				// if we get to here.
				return;
			}
			// test if unconfiguring will invalidate prereqs
			ArrayList otherFeatures = computeOtherFeatures(feature);
			ArrayList plugins = computeUniquePlugins(otherFeatures);
			// Going plug-ins are those that are only listed in the feature
			// that will be unconfigured. Other plug-ins are referenced by
			// other configured features and should not be of our concern.
			ArrayList goingPlugins = new ArrayList();
			computeGoingPlugins(feature, plugins, goingPlugins);
			// See if any of the features depends on the plug-ins that
			// will go away
			for (int i = 0; i < otherFeatures.size(); i++) {
				IFeature otherFeature = (IFeature) otherFeatures.get(i);
				validatePrereqs(otherFeature, goingPlugins, true, children);
			}

		} catch (CoreException e) {
			children.add(e.getStatus());
		}
	}

	private static void validateConfigure(
		IFeature feature,
		ArrayList children) {
		// Check the prereqs
		try {
			ArrayList otherFeatures = computeOtherFeatures(feature);
			ArrayList plugins = computeUniquePlugins(otherFeatures);
			ArrayList goingPlugins = new ArrayList();
			computeGoingPlugins(feature, plugins, goingPlugins);
			validatePrereqs(feature, plugins, false, children);
		} catch (CoreException e) {
			children.add(e.getStatus());
		}
	}

	private static void validateInstall(
		IFeature oldFeature,
		IFeature newFeature,
		ArrayList children) {
		// just check if unconfiguring the old feature will
		// cause anything to break
		if (oldFeature == null) {
			// Installing a new feature is OK as long as
			// all of its prereqs are satisfied. This is
			// tested before we get to this class, so 
			// no need to test again.
			return;
		}
		try {
			ArrayList otherFeatures = computeOtherFeatures(oldFeature);
			ArrayList plugins = computeUniquePlugins(otherFeatures);
			ArrayList goingPlugins = new ArrayList();
			computeGoingPlugins(oldFeature, plugins, goingPlugins);
			// now see if the new version of a feature will
			// bring the going plugins to zero.
			restoreGoingPlugins(newFeature, goingPlugins);
			validatePrereqs(oldFeature, goingPlugins, true, children);
		} catch (CoreException e) {
			children.add(e.getStatus());
		}

	}

	private static ArrayList computeOtherFeatures(IFeature thisFeature)
		throws CoreException {
		ArrayList features = new ArrayList();
		ILocalSite localSite = SiteManager.getLocalSite();
		IInstallConfiguration config = localSite.getCurrentConfiguration();
		IConfiguredSite[] csites = config.getConfiguredSites();

		for (int i = 0; i < csites.length; i++) {
			IConfiguredSite csite = csites[i];
			IFeatureReference[] crefs = csite.getConfiguredFeatures();
			for (int j = 0; j < crefs.length; j++) {
				IFeatureReference cref = crefs[j];
				IFeature cfeature = cref.getFeature();
				if (thisFeature.equals(cfeature))
					continue;
				features.add(cfeature);
			}
		}
		// remove included features so that only the top-level features
		// remain on the list
		IFeature[] array =
			(IFeature[]) features.toArray(new IFeature[features.size()]);
		for (int i = 0; i < array.length; i++) {
			IFeature feature = array[i];
			IFeatureReference[] included =
				feature.getIncludedFeatureReferences();
			for (int j = 0; j < included.length; j++) {
				IFeatureReference ref = included[j];
				IFeature fref = ref.getFeature();
				int index = features.indexOf(fref);
				if (index != -1)
					features.remove(index);
			}
		}
		return features;
	}

	private static void addUniquePlugins(IFeature feature, ArrayList plugins)
		throws CoreException {
		IPluginEntry[] entries = feature.getPluginEntries();
		for (int i = 0; i < entries.length; i++) {
			IPluginEntry entry = entries[i];
			if (!plugins.contains(entry))
				plugins.add(entry);
			IFeatureReference[] included =
				feature.getIncludedFeatureReferences();
			for (int j = 0; j < included.length; j++) {
				IFeature fref = included[j].getFeature();
				addUniquePlugins(fref, plugins);
			}
		}
	}

	private static void computeGoingPlugins(
		IFeature feature,
		ArrayList plugins,
		ArrayList result)
		throws CoreException {
		IPluginEntry[] entries = feature.getPluginEntries();
		// plug-ins that are only referenced by this feature
		// and are not on the list will be disabled (going)
		// when the feature is disabled.
		for (int i = 0; i < entries.length; i++) {
			if (!plugins.contains(entries[i])) {
				result.add(entries[i]);
			}
		}
		IFeatureReference[] included = feature.getIncludedFeatureReferences();
		for (int i = 0; i < included.length; i++) {
			computeGoingPlugins(included[i].getFeature(), plugins, result);
		}
	}

	private static void restoreGoingPlugins(
		IFeature feature,
		ArrayList goingPlugins)
		throws CoreException {
		IPluginEntry[] entries = feature.getPluginEntries();
		// plug-ins that are only referenced by this feature
		// and are not on the list will be disabled (going)
		// when the feature is disabled.
		for (int i = 0; i < entries.length; i++) {
			int index = goingPlugins.indexOf(entries[i]);
			if (index != -1)
				goingPlugins.remove(index);
		}
		IFeatureReference[] included = feature.getIncludedFeatureReferences();
		for (int i = 0; i < included.length; i++) {
			restoreGoingPlugins(included[i].getFeature(), goingPlugins);
		}
	}

	private static void validatePrereqs(
		IFeature feature,
		ArrayList goingPlugins,
		boolean inclusion,
		ArrayList children) {
		IImport[] imports = feature.getImports();
		for (int i = 0; i < imports.length; i++) {
			IImport iimport = imports[i];
			VersionedIdentifier vid = iimport.getVersionedIdentifier();
			String message =
				UpdateUIPlugin.getFormattedMessage(KEY_PREREQ, vid.toString());
			boolean found = false;

			for (int j = 0; j < goingPlugins.size(); j++) {
				IPluginEntry entry = (IPluginEntry) goingPlugins.get(j);
				if (entry.getVersionedIdentifier().equals(vid))
					found = true;
				if (inclusion && found) {
					children.add(createStatus(feature, message));
					break;
				}
			}
			if (!inclusion && !found) {
				children.add(createStatus(feature, message));
			}
		}
	}

	private static boolean isPlatformFeature(IFeature feature)
		throws CoreException {
		String[] bootstrapPlugins =
			BootLoader
				.getCurrentPlatformConfiguration()
				.getBootstrapPluginIdentifiers();
		if (containsBootstrapPlugins(feature, bootstrapPlugins))
			return true;
		// is primary
		return feature.isPrimary();
	}

	private static boolean containsBootstrapPlugins(
		IFeature feature,
		String[] ids)
		throws CoreException {
		IPluginEntry[] entries = feature.getPluginEntries();
		// contains bootstrap plug-ins
		for (int i = 0; i < entries.length; i++) {
			IPluginEntry entry = entries[i];
			String id = entry.getVersionedIdentifier().getIdentifier();
			if (isOnTheList(id, ids))
				return true;
		}
		// test included
		IFeatureReference[] included = feature.getIncludedFeatureReferences();
		for (int i = 0; i < included.length; i++) {
			IFeatureReference ref = included[i];
			if (containsBootstrapPlugins(ref.getFeature(), ids))
				return true;
		}
		return false;
	}

	private static boolean isOnTheList(String id, String[] list) {
		for (int i = 0; i < list.length; i++) {
			if (id.equalsIgnoreCase(list[i]))
				return true;
		}
		return false;
	}

	private static ArrayList computeUniquePlugins(ArrayList otherFeatures)
		throws CoreException {
		ArrayList plugins = new ArrayList();
		for (int i = 0; i < otherFeatures.size(); i++) {
			IFeature otherFeature = (IFeature) otherFeatures.get(i);
			addUniquePlugins(otherFeature, plugins);
		}
		return plugins;
	}

	private static IStatus createStatus(IFeature feature, String message) {
		VersionedIdentifier vid = feature.getVersionedIdentifier();
		String id = vid.getIdentifier();
		PluginVersionIdentifier version = vid.getVersion();
		String fullMessage =
			UpdateUIPlugin.getFormattedMessage(
				KEY_CHILD_MESSAGE,
				new String[] {
					feature.getLabel(),
					version.toString(),
					message });
		return new Status(
			IStatus.ERROR,
			UpdateUIPlugin.getPluginId(),
			IStatus.OK,
			fullMessage,
			null);
	}
}
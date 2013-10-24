/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     René Brandstetter - Bug 419749 - [Workbench] [e4 Workbench] - Remove the deprecated PackageAdmin
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTrackerCustomizer;

/**
 * A simple {@link BundleTrackerCustomizer} which is able to resolve a bundle by its symbolic name.
 */
/*
 * TODO: This implementation can probably be removed after a switch to OSGi 6 which will have the
 * possibility to lookup bundles with the FrameworkWiring#findProviders(Requirement) method.
 */
final class BundleFinder implements BundleTrackerCustomizer<List<Bundle>> {

	/** Map of bundle symbolic name to the corresponding bundles (hint: different versions). */
	private final ConcurrentMap<String, List<Bundle>> trackedBundles = new ConcurrentHashMap<String, List<Bundle>>();

	/**
	 * Resolves the latest bundle with the given bundle symbolic name.
	 * <p>
	 * The latest means the bundle with the highest version.
	 * </p>
	 * @param symbolicName
	 *            the bundle symbolic name
	 * @return the latest bundle with the given bundle symbolic name
	 */
	public Bundle findBundle(String symbolicName) {
		List<Bundle> bundlesWithSameSymName = trackedBundles.get(symbolicName);
		if (bundlesWithSameSymName == null)
			return null;

		List<Bundle> snapshot = new ArrayList<Bundle>(bundlesWithSameSymName);

		switch (snapshot.size()) {
		case 0:
			return null;
		case 1:
			return snapshot.get(0);
		default:
			Collections.sort(snapshot, VersionComperator.INSTANCE); // sort the snapshot by version
			return snapshot.get(0); // the highest is the first entry in the list
		}
	}

	@Override
	public List<Bundle> addingBundle(Bundle bundle, BundleEvent event) {
		String bundleSymName = bundle.getSymbolicName();

		List<Bundle> bundlesWithSameSymName = trackedBundles.get(bundleSymName);
		if (bundlesWithSameSymName == null) {
			bundlesWithSameSymName = new CopyOnWriteArrayList<Bundle>();

			if (trackedBundles.putIfAbsent(bundleSymName, bundlesWithSameSymName) != null) {
				// some other thread has won the race, so we use his List object
				bundlesWithSameSymName = trackedBundles.get(bundleSymName);
			}
		}

		bundlesWithSameSymName.add(bundle);

		// return the list to mark that we want to be informed about other changes of the bundle and
		// to remove the bundle in the #removedBundle() method without a lookup in the
		// #trackedBundles map
		return bundlesWithSameSymName;
	}

	@Override
	public void modifiedBundle(Bundle bundle, BundleEvent event, List<Bundle> bundlesWithSameSymName) {
		// not of interest
	}

	@Override
	public void removedBundle(Bundle bundle, BundleEvent event, List<Bundle> bundlesWithSameSymName) {
		// the object is the list which was returned inside the #addingBundle() method and so we
		// don't need a lookup in the #trackedBundles to find the list
		bundlesWithSameSymName.remove(bundle);
	}

	/**
	 * A simple {@link Comparator} which orders the bundles by their version in ascending order.
	 */
	private static final class VersionComperator implements Comparator<Bundle> {
		/** A Singleton instance of this {@link Comparator} (the compare is done state-less). */
		public static final Comparator<Bundle> INSTANCE = new VersionComperator();

		private VersionComperator() {
		}

		@Override
		public int compare(Bundle bundle1, Bundle bundle2) {
			if (bundle1 == null) {
				return bundle2 == null ? 0 : 1; // null elements at the end of the list
			}

			if (bundle2 == null) {
				return -1; // null elements at the end of the list
			}

			return bundle2.getVersion().compareTo(bundle1.getVersion()); // newest version first
		}
	}
}

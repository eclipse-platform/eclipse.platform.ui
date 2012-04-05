/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.model;

import java.net.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.internal.search.*;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.UpdateUIMessages;
import org.eclipse.update.search.*;

public class DiscoveryFolder extends BookmarkFolder {

    private static final long serialVersionUID = 1L;
    UpdatePolicy updatePolicy = new UpdatePolicy();
	
	public DiscoveryFolder() {
		super(UpdateUIMessages.DiscoveryFolder_name); 
		setModel(UpdateUI.getDefault().getUpdateModel());
	}
	public void initialize() {
		children.clear();
		// check if discovery sites defined by features should be exposed to the user
		if (!UpdateUI.getDefault().getPluginPreferences().getBoolean(UpdateUI.P_DISCOVERY_SITES_ENABLED))
			return;
		
		try {
			URL updateMapURL = UpdateUtils.getUpdateMapURL();
			if (updateMapURL!=null) {
				updatePolicy = new UpdatePolicy();
				// TODO may need to use a proper monitor to be able to cancel connections
				IStatus status = UpdateUtils.loadUpdatePolicy(updatePolicy, updateMapURL, new NullProgressMonitor());
				if (status != null) {
					// log and continue
					UpdateUtils.log(status);
				}
			}
		} catch (CoreException e) {
			// log and continue
			UpdateUtils.log(e.getStatus());
		}
		
		try {
			ILocalSite site = SiteManager.getLocalSite();
			IInstallConfiguration config = site.getCurrentConfiguration();
			IConfiguredSite[] csites = config.getConfiguredSites();
			for (int i = 0; i < csites.length; i++) {
				IConfiguredSite csite = csites[i];
				IFeatureReference[] refs = csite.getConfiguredFeatures();
				for (int j = 0; j < refs.length; j++) {
					IFeatureReference ref = refs[j];
					IFeature feature = ref.getFeature(null);
					IURLEntry[] entries = feature.getDiscoverySiteEntries();
					if (entries.length > 0) {
						// Only add discovery sites of root features
						if (isIncluded(ref, refs))
							continue;
						addBookmarks(feature);
					}
				}
			}
		} catch (CoreException e) {
			UpdateUI.logException(e);
		}
	}
	private boolean isIncluded(
		IFeatureReference ref,
		IFeatureReference[] refs) {
		try {
			VersionedIdentifier vid = ref.getVersionedIdentifier();
			for (int i = 0; i < refs.length; i++) {
				IFeatureReference candidate = refs[i];
				// Ignore self
				if (candidate.equals(ref))
					continue;
				IFeature cfeature = candidate.getFeature(null);
				IFeatureReference[] irefs =
					cfeature.getIncludedFeatureReferences();
				for (int j = 0; j < irefs.length; j++) {
					IFeatureReference iref = irefs[j];
					VersionedIdentifier ivid = iref.getVersionedIdentifier();
					if (ivid.equals(vid)) {
						// bingo - included in at least one feature
						return true;
					}
				}
			}
		} catch (CoreException e) {
		}
		return false;
	}
	private void addBookmarks(IFeature feature) {
		// See if this query site adapter is mapped in the map file
		// to a different URL.
		if (updatePolicy != null && updatePolicy.isLoaded()) {
			IUpdateSiteAdapter mappedSite = updatePolicy.getMappedDiscoverySite(feature.getVersionedIdentifier().getIdentifier());
			if (mappedSite != null) {
				SiteBookmark bookmark =
					new SiteBookmark(mappedSite.getLabel(),	mappedSite.getURL(), false);
				bookmark.setReadOnly(true);
				if (!contains(bookmark))
					internalAdd(bookmark);
				return;
			} else if (!updatePolicy.isFallbackAllowed()) {
				// no match - use original sites if fallback allowed, or nothing.
				return;
			}
		}
		IURLEntry[] entries = feature.getDiscoverySiteEntries();
		for (int i = 0; i < entries.length; i++) {
			IURLEntry entry = entries[i];
			SiteBookmark bookmark =
				new SiteBookmark(
					entry.getAnnotation(),
					entry.getURL(),
					entry.getType() == IURLEntry.WEB_SITE);
			bookmark.setReadOnly(entry.getType() != IURLEntry.WEB_SITE);
			if (!contains(bookmark))
				internalAdd(bookmark);
		}
	}
	private boolean contains(SiteBookmark bookmark) {
		for (int i = 0; i < children.size(); i++) {
			Object o = children.get(i);
			if (o instanceof SiteBookmark) {
				// note: match on URL, not the label
				if (bookmark.getURL().equals(((SiteBookmark) o).getURL()))
					return true;
			}
		}
		return false;
	}
	public Object[] getChildren(Object parent) {
		if (hasChildren() == false)
			initialize();
		return super.getChildren(parent);
	}
}

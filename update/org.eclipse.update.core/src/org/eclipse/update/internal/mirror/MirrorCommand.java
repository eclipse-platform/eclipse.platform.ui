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
package org.eclipse.update.internal.mirror;
import java.net.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.model.*;
import org.eclipse.update.internal.standalone.*;

/**
 * Mirrors a remote site locally.
 */
public class MirrorCommand extends ScriptedCommand {

	private String featureId;
	private String featureVersion;
	private String fromSiteUrl;
	private String toSiteUrl;
	private MirrorSite mirrorSite;

	public MirrorCommand(
		String featureId,
		String featureVersion,
		String fromSiteUrl,
		String toSiteUrl) {
		this.featureId = featureId;
		this.featureVersion = featureVersion;
		this.fromSiteUrl = fromSiteUrl;
		this.toSiteUrl = toSiteUrl;
	}

	/**
	 * true if success
	 */
	public boolean run() {
		if (!validateParameters()) {
			return false;
		}

		try {
			if (getMirroSite() == null)
				return false;

			URL remoteSiteUrl = new URL(fromSiteUrl);
			ISite remoteSite =
				SiteManager.getSite(remoteSiteUrl, new NullProgressMonitor());

			ISiteFeatureReference featureReferencesToMirror[] =
				findFeaturesToMirror(remoteSite);
			if (featureReferencesToMirror.length == 0) {
				System.out.println(
					"No matching features found on " + remoteSiteUrl + ".");
				return false;
			}

			mirrorSite.mirrorAndExpose(
				remoteSite,
				featureReferencesToMirror,
				null);
			System.out.println("Mirror command completed successfully.");
			return true;
		} catch (MalformedURLException e) {
			System.out.println(e);
			e.printStackTrace();
			return false;
		} catch (CoreException ce) {
			IStatus status = ce.getStatus();
			System.out.println(ce.getMessage());
			ce.printStackTrace();
			return false;
		} finally {
			JarContentReference.shutdown();
		}
	}
	private boolean validateParameters() {
		if (fromSiteUrl == null && fromSiteUrl.length() <= 0) {
			System.out.println("from parameter missing.");
			return false;
		}
		try {
			new URL(fromSiteUrl);
		} catch (MalformedURLException mue) {
			System.out.println("from must be valid URL");
			return false;
		}
		if (toSiteUrl == null && toSiteUrl.length() <= 0) {
			System.out.println("to parameter missing.");
			return false;
		}
		try {
			if (!"file".equals(new URL(toSiteUrl).getProtocol())) {
				System.out.println(
					"to parameter must be URL with file protocol");
			}
		} catch (MalformedURLException mue) {
			System.out.println("from must be valid URL");
			return false;
		}
		return true;
	}
	private MirrorSite getMirroSite()
		throws MalformedURLException, CoreException {
		// Create mirror site
		if (mirrorSite == null) {
			if (toSiteUrl != null) {
				URL toSiteUrl = new URL(this.toSiteUrl);
				MirrorSiteFactory factory = new MirrorSiteFactory();
				try {
					mirrorSite = (MirrorSite) factory.createSite(toSiteUrl);
				} catch (InvalidSiteTypeException iste) {
				}
			}
			if (mirrorSite == null) {
				System.out.println(
					"Cannot access site to mirror to: " + toSiteUrl);
				return null;
			}
		}
		return mirrorSite;

	}
	/**
	 * Returns subset of feature references on remote site
	 * as specified by optional featureId and featureVersion
	 * parameters
	 * @param remoteSite
	 * @return ISiteFeatureReference[]
	 * @throws CoreException
	 */
	private ISiteFeatureReference[] findFeaturesToMirror(ISite remoteSite)
		throws CoreException {
		ISiteFeatureReference remoteSiteFeatureReferences[] =
			remoteSite.getRawFeatureReferences();
		SiteFeatureReferenceModel existingFeatureModels[]=mirrorSite.getFeatureReferenceModels();
		Collection featureReferencesToMirror = new ArrayList();

		PluginVersionIdentifier featureVersionIdentifier = null;
		if (featureVersion != null) {
			featureVersionIdentifier =
				new PluginVersionIdentifier(featureVersion);
		}
		for (int i = 0; i < remoteSiteFeatureReferences.length; i++) {
			VersionedIdentifier remoteFeatureVersionedIdentifier =
				remoteSiteFeatureReferences[i].getVersionedIdentifier();

			if (featureId != null
				&& !featureId.equals(
					remoteFeatureVersionedIdentifier.getIdentifier())) {
				// id does not match
				continue;
			}
			if (featureVersionIdentifier != null
				&& !featureVersionIdentifier.isPerfect(
					remoteFeatureVersionedIdentifier.getVersion())) {
				// version does not match
				continue;
			}
			
			for(int e=0; e<existingFeatureModels.length; e++){
				if(existingFeatureModels[e].getVersionedIdentifier().equals(remoteFeatureVersionedIdentifier)){
					System.out.println("Feature "+ remoteFeatureVersionedIdentifier +" already mirrored and exposed.");
					// feature already mirrored and exposed in site.xml
					continue;
				}
			}
			
			
			featureReferencesToMirror.add(remoteSiteFeatureReferences[i]);
			System.out.println(
				"Feature "
					+ remoteSiteFeatureReferences[i].getVersionedIdentifier()
					+ " will be mirrored.");
		}
		return (ISiteFeatureReference[]) featureReferencesToMirror.toArray(
			new ISiteFeatureReference[featureReferencesToMirror.size()]);
	}

}

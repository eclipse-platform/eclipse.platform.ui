/*
 * Created on May 26, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.update.search;

import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.internal.operations.UpdateManager;

/**
 * This class can be added to the update search request
 * to filter out features that are back-level (are
 * older or the same as the features already installed).
 * 
 * @see UpdateSearchRequest
 * @see IUpdateSearchFilter
 */
public class BackLevelFilter implements IUpdateSearchFilter {
	public boolean select(IFeature match) {
		PluginVersionIdentifier matchVid = match.getVersionedIdentifier().getVersion();
		IFeature [] installed = UpdateManager.getInstalledFeatures(match, false);
		if (installed.length==0) return true;
		
		for (int i=0; i<installed.length; i++) {
			PluginVersionIdentifier ivid = installed[i].getVersionedIdentifier().getVersion();
			if (matchVid.isGreaterThan(ivid))
				continue;
			// installed version is the same or newer than
			// the match - filter out
			return false;
		}
		return true;
	}
}

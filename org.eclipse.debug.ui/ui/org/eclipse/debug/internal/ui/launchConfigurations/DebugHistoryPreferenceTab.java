package org.eclipse.debug.internal.ui.launchConfigurations;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


/**
 * Debug favorites and history
 */
public class DebugHistoryPreferenceTab extends LaunchHistoryPreferenceTab {
	
	/**
	 * @see LaunchHistoryPreferenceTab#getFavoritesLabel()
	 */
	protected String getFavoritesLabel() {
		return "Favorite Debug Configurations:";
	}

	/**
	 * @see LaunchHistoryPreferenceTab#getRecentLabel()
	 */
	protected String getRecentLabel() {
		return "Recent Debug Launches:";
	}

	/**
	 * @see LaunchHistoryPreferenceTab#getInitialFavorites()
	 */
	protected ILaunchConfiguration[] getInitialFavorites() {
		LaunchConfigurationHistoryElement[] favs = DebugUIPlugin.getDefault().getDebugFavorites();
		ILaunchConfiguration[] configs = new ILaunchConfiguration[favs.length];
		for (int i = 0; i < favs.length; i++) {
			configs[i] = favs[i].getLaunchConfiguration();
		}
		return configs;
	}

	/**
	 * @see LaunchHistoryPreferenceTab#getIntialRecents()
	 */
	protected ILaunchConfiguration[] getInitialRecents() {
		LaunchConfigurationHistoryElement[] favs = DebugUIPlugin.getDefault().getDebugHistory();
		ILaunchConfiguration[] configs = new ILaunchConfiguration[favs.length];
		for (int i = 0; i < favs.length; i++) {
			configs[i] = favs[i].getLaunchConfiguration();
			if (configs[i] == null) {
				// not using launch configs
				return new ILaunchConfiguration[0];
			}
		}
		return configs;
	}

}

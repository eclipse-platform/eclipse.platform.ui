package org.eclipse.debug.internal.ui.launchConfigurations;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;

/**
 * Debug favorites and history
 */
public class DebugHistoryPreferenceTab extends LaunchHistoryPreferenceTab {
	
	/**
	 * @see LaunchHistoryPreferenceTab#getFavoritesLabel()
	 */
	protected String getFavoritesLabel() {
		return "Favorite Debug Confi&gurations:";
	}

	/**
	 * @see LaunchHistoryPreferenceTab#getRecentLabel()
	 */
	protected String getRecentLabel() {
		return "Recent Debug &Launches:";
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

	/**
	 * @see LaunchHistoryPreferenceTab#getMode()
	 */
	protected String getMode() {
		return ILaunchManager.DEBUG_MODE;
	}
}

package org.eclipse.debug.internal.ui.preferences;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationHistoryElement;

/**
 * Run favorites and history
 */
public class RunHistoryPreferenceTab extends LaunchHistoryPreferenceTab {
	
	/**
	 * @see LaunchHistoryPreferenceTab#getFavoritesLabel()
	 */
	protected String getFavoritesLabel() {
		return DebugPreferencesMessages.getString("RunHistoryPreferenceTab.Favorite_Run_Confi&gurations__1"); //$NON-NLS-1$
	}

	/**
	 * @see LaunchHistoryPreferenceTab#getRecentLabel()
	 */
	protected String getRecentLabel() {
		return DebugPreferencesMessages.getString("RunHistoryPreferenceTab.Recent_Run_&Launches__2"); //$NON-NLS-1$
	}

	/**
	 * @see LaunchHistoryPreferenceTab#getInitialFavorites()
	 */
	protected ILaunchConfiguration[] getInitialFavorites() {
		LaunchConfigurationHistoryElement[] favs = DebugUIPlugin.getLaunchConfigurationManager().getRunFavorites();
		ILaunchConfiguration[] configs = new ILaunchConfiguration[favs.length];
		for (int i = 0; i < favs.length; i++) {
			configs[i] = favs[i].getLaunchConfiguration();
		}
		return configs;
	}

	/**
	 * @see LaunchHistoryPreferenceTab#getInitialRecents()
	 */
	protected ILaunchConfiguration[] getInitialRecents() {
		LaunchConfigurationHistoryElement[] favs = DebugUIPlugin.getLaunchConfigurationManager().getRunHistory();
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
		return ILaunchManager.RUN_MODE;
	}

}

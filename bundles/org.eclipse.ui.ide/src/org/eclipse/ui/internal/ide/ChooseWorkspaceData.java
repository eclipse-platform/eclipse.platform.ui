/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Eric Rizzo - added API to set the list of recent workspaces.
 *     Jan-Ove Weichel <ovi.weichel@gmail.com> - Bug 463039
 *     Jan-Ove Weichel <janove.weichel@vogella.com> - Bug 411578
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import java.io.File;
import java.net.URL;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * This class stores the information behind the "Launch Workspace" dialog. The
 * class is able to read and write itself to a well known configuration file.
 */
public class ChooseWorkspaceData {
	/**
	 * The default max length of the recent workspace mru list.
	 */
	private static final int RECENT_MAX_LENGTH = 10;

	/**
	 * This is the first version of the encode/decode protocol that uses the
	 * configuration area preference store for persistence. The only encoding
	 * done is to convert the recent workspace list into a comma-separated list.
	 */
	private static final int PERS_ENCODING_VERSION_CONFIG_PREFS = 2;

	/**
	 * This is the second version of the encode/decode protocol that uses the
	 * configuration area preferences store for persistence. This version is the
	 * same as the previous version except it uses a \n character to separate
	 * the path entries instead of commas. (see bug 98467)
	 *
	 * @since 3.3.1
	 */
	private static final int PERS_ENCODING_VERSION_CONFIG_PREFS_NO_COMMAS = 3;

	private boolean showDialog = true;

	private boolean showRecentWorkspaces;

	private String initialDefault;

	private String selection;

	private String[] recentWorkspaces;

	/**
	 * Creates a new instance, loading persistent data if its found.
	 */
	public ChooseWorkspaceData(String initialDefault) {
		readPersistedData();
		setInitialDefault(initialDefault);
	}

	/**
	 * Creates a new instance, loading persistent data if its found.
	 */
	public ChooseWorkspaceData(URL instanceUrl) {
		readPersistedData();
		if (instanceUrl != null) {
			setInitialDefault(new File(instanceUrl.getFile()).toString());
		}
	}

	/**
	 * Return the folder to be used as a default if no other information
	 * exists. Does not return null.
	 */
	public String getInitialDefault() {
		if (initialDefault == null) {
			setInitialDefault(System.getProperty("user.dir") //$NON-NLS-1$
					+ File.separator + "workspace"); //$NON-NLS-1$
		}
		return initialDefault;
	}

	/**
	 * Set this data's initialDefault parameter to a properly formatted version
	 * of the argument directory string. The proper format is to the platform
	 * appropriate separator character without meaningless leading or trailing
	 * separator characters.
	 */
	private void setInitialDefault(String dir) {
		if (dir == null || dir.length() <= 0) {
			initialDefault = null;
			return;
		}

		dir = IPath.fromOSString(dir).toOSString();
		while (dir.charAt(dir.length() - 1) == File.separatorChar) {
			dir = dir.substring(0, dir.length() - 1);
		}
		initialDefault = dir;
	}

	/**
	 * Return the currently selected workspace or null if nothing is selected.
	 */
	public String getSelection() {
		return selection;
	}

	/**
	 * Return the currently selected workspace or null if nothing is selected.
	 */
	public boolean getShowDialog() {
		return showDialog;
	}

	/**
	 * Returns whether the "Recent Workspaces" should be shown
	 */
	public boolean isShowRecentWorkspaces() {
		return showRecentWorkspaces;
	}

	/**
	 * Return an array of recent workspaces sorted with the most recently used at
	 * the start.
	 */
	public String[] getRecentWorkspaces() {
		return recentWorkspaces;
	}

	/**
	 * The argument workspace has been selected, update the receiver.  Does not
	 * persist the new values.
	 */
	public void workspaceSelected(String dir) {
		// this just stores the selection, it is not inserted and persisted
		// until the workspace is actually selected
		selection = dir;
	}

	/**
	 * Toggle value of the showDialog persistent setting.
	 */
	public void toggleShowDialog() {
		showDialog = !showDialog;
	}

	/**
	 * Set if the "Recent Workspaces" should be shown
	 */
	public void setShowRecentWorkspaces(boolean showRecentWorkspaces) {
		this.showRecentWorkspaces = showRecentWorkspaces;
	}

	/**
	 * Sets the list of recent workspaces.
	 */
	public void setRecentWorkspaces(String[] workspaces) {
		if (workspaces == null) {
			recentWorkspaces = new String[0];
		} else {
			recentWorkspaces = workspaces;
		}
	}

	/**
	 * Update the persistent store. Call this function after the currently
	 * selected value has been found to be ok.
	 */
	public void writePersistedData() {
		// 1. get config pref node
		Preferences node = ConfigurationScope.INSTANCE.getNode(IDEWorkbenchPlugin.IDE_WORKBENCH);

		// 2. get value for showDialog
		node.putBoolean(
				IDE.Preferences.SHOW_WORKSPACE_SELECTION_DIALOG,
				showDialog);

		// 3. use value of numRecent to create proper length array
		node.putInt(IDE.Preferences.MAX_RECENT_WORKSPACES,
				recentWorkspaces.length);

		// move the new selection to the front of the list
		if (selection != null) {
			File newFolder = new File(selection);
			String oldEntry = recentWorkspaces[0];
			recentWorkspaces[0] = selection;
			for (int i = 1; i < recentWorkspaces.length && oldEntry != null; ++i) {
				File oldFolder = new File (oldEntry);
				// If selection represents a file location we already have, don't store it
				if (newFolder.compareTo(oldFolder) == 0){
					break;
				}
				String tmp = recentWorkspaces[i];
				recentWorkspaces[i] = oldEntry;
				oldEntry = tmp;
			}
		}

		// 4. store values of recent workspaces into array
		String encodedRecentWorkspaces = encodeStoredWorkspacePaths(recentWorkspaces);
		node.put(IDE.Preferences.RECENT_WORKSPACES,
				encodedRecentWorkspaces);

		// 5. store the protocol version used to encode the list
		node.putInt(IDE.Preferences.RECENT_WORKSPACES_PROTOCOL,
				PERS_ENCODING_VERSION_CONFIG_PREFS_NO_COMMAS);

		// 6. store if the "Recent Workspaces" should be shown
		node.putBoolean(IDE.Preferences.SHOW_RECENT_WORKSPACES, showRecentWorkspaces);

		// 7. store the node
		try {
			node.flush();
		} catch (BackingStoreException e) {
			// do nothing
		}
	}

	/**
	 * Return the current (persisted) value of the "showDialog on startup"
	 * preference. Return the global default if the file cannot be accessed.
	 */
	public static boolean getShowDialogValue() {
		ChooseWorkspaceData data = new ChooseWorkspaceData(""); //$NON-NLS-1$
		return data.readPersistedData() ? data.showDialog : true;
	}

	/**
	 * Return the current (persisted) value of the "showDialog on startup"
	 * preference. Return the global default if the file cannot be accessed.
	 */
	public static void setShowDialogValue(boolean showDialog) {
		ChooseWorkspaceData data = new ChooseWorkspaceData(""); //$NON-NLS-1$
		data.showDialog = showDialog;
		data.writePersistedData();
	}

	/**
	 * Look in the config area preference store for the list of recently used
	 * workspaces.
	 *
	 * @return true if the values were successfully retrieved and false
	 *         otherwise
	 */
	public boolean readPersistedData() {
		IPreferenceStore store = new ScopedPreferenceStore(ConfigurationScope.INSTANCE,
				IDEWorkbenchPlugin.IDE_WORKBENCH);

		int protocol = store.getInt(IDE.Preferences.RECENT_WORKSPACES_PROTOCOL);

		// 2. get value for showDialog
		showDialog = store.getBoolean(IDE.Preferences.SHOW_WORKSPACE_SELECTION_DIALOG);

		// 3. use value of numRecent to create proper length array
		int max = store.getInt(IDE.Preferences.MAX_RECENT_WORKSPACES);
		max = Math.max(max, RECENT_MAX_LENGTH);

		// 4. load values of recent workspaces into array
		String workspacePathPref = store.getString(IDE.Preferences.RECENT_WORKSPACES);
		recentWorkspaces = decodeStoredWorkspacePaths(protocol, max, workspacePathPref);

		// 5. get value for showRecentWorkspaces
		showRecentWorkspaces = store.getBoolean(IDE.Preferences.SHOW_RECENT_WORKSPACES);

		return true;
	}

	/**
	 * The the list of recent workspaces must be stored as a string in the preference node.
	 */
	private static String encodeStoredWorkspacePaths(String[] recent) {
		StringBuilder buff = new StringBuilder();

		String path = null;
		for (String recentPath : recent) {
			if (recentPath == null) {
				break;
			}

			// as of 3.3.1 pump this out using newlines instead of commas
			if (path != null) {
				buff.append("\n"); //$NON-NLS-1$
			}

			path = recentPath;
			buff.append(path);
		}

		return buff.toString();
	}

	/**
	 * The the preference for recent workspaces must be converted from the
	 * storage string into an array.
	 */
	private static String[] decodeStoredWorkspacePaths(int protocol, int max, String prefValue) {
		String[] paths = new String[max];
		if (prefValue == null || prefValue.length() <= 0) {
			return paths;
		}

		// if we're using the latest version of the protocol use the newline as a
		// token.  Otherwise use the older comma.
		String tokens = null;
		switch (protocol) {
			case PERS_ENCODING_VERSION_CONFIG_PREFS_NO_COMMAS :
				tokens = "\n"; //$NON-NLS-1$
				break;
			case PERS_ENCODING_VERSION_CONFIG_PREFS :
				tokens = ","; //$NON-NLS-1$
				break;
		}
		if (tokens == null) {
			// Unknown version? corrupt file? we can't log it because we don't
			// have a workspace yet...
			return new String[0];
		}

		StringTokenizer tokenizer = new StringTokenizer(prefValue, tokens);
		for (int i = 0; i < paths.length && tokenizer.hasMoreTokens(); ++i) {
			paths[i] = tokenizer.nextToken();
		}

		return paths;
	}

}

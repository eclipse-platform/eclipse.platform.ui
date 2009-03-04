/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Eric Rizzo - added API to set the list of recent workspaces.
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
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
    private static final int RECENT_MAX_LENGTH = 5;

    /**
     * The directory within the config area that will be used for the
     * receiver's persisted data.
     */
    private static final String PERS_FOLDER = "org.eclipse.ui.ide"; //$NON-NLS-1$

    /**
     * The name of the file within the config area that will be used for
     * the recever's persisted data.
     * @see PERS_FOLDER
     */
    private static final String PERS_FILENAME = "recentWorkspaces.xml"; //$NON-NLS-1$

    /**
     * In the past a file was used to store persist these values.  This file was written
     * with this value as its protocol identifier.
     */
    private static final int PERS_ENCODING_VERSION = 1;

    /**
     * This is the first version of the encode/decode protocol that uses the config area
     * preference store for persistence.  The only encoding done is to convert the recent
     * workspace list into a comma-separated list.
     */
    private static final int PERS_ENCODING_VERSION_CONFIG_PREFS = 2;
    
    /**
	 * This is the second version of the encode/decode protocol that uses the
	 * confi area preferences store for persistence. This version is the same as
	 * the previous version except it uses a \n character to seperate the path
	 * entries instead of commas. (see bug 98467)
	 * 
	 * @since 3.3.1
	 */
	private static final int PERS_ENCODING_VERSION_CONFIG_PREFS_NO_COMMAS = 3;

    private boolean showDialog = true;

    private String initialDefault;

    private String selection;

    private String[] recentWorkspaces;

    // xml tags
    private static interface XML {
        public static final String PROTOCOL = "protocol"; //$NON-NLS-1$

        public static final String VERSION = "version"; //$NON-NLS-1$

        public static final String ALWAYS_ASK = "alwaysAsk"; //$NON-NLS-1$

        public static final String SHOW_DIALOG = "showDialog"; //$NON-NLS-1$

        public static final String WORKSPACE = "workspace"; //$NON-NLS-1$

        public static final String RECENT_WORKSPACES = "recentWorkspaces"; //$NON-NLS-1$

        public static final String MAX_LENGTH = "maxLength"; //$NON-NLS-1$

        public static final String PATH = "path"; //$NON-NLS-1$
    }

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

        dir = new Path(dir).toOSString();
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
		Preferences node = new ConfigurationScope().getNode(IDEWorkbenchPlugin.IDE_WORKBENCH);

		// 2. get value for showDialog
		node.putBoolean(
				IDE.Preferences.SHOW_WORKSPACE_SELECTION_DIALOG,
				showDialog);

		// 3. use value of numRecent to create proper length array
		node.putInt(IDE.Preferences.MAX_RECENT_WORKSPACES,
				recentWorkspaces.length);

		// move the new selection to the front of the list
		if (selection != null) {
			String oldEntry = recentWorkspaces[0];
			recentWorkspaces[0] = selection;
			for (int i = 1; i < recentWorkspaces.length && oldEntry != null; ++i) {
				if (selection.equals(oldEntry)) {
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

		// 6. store the node
		try {
			node.flush();
		} catch (BackingStoreException e) {
			// do nothing
		}
	}

    /**
	 * Look for and read data that might have been persisted from some previous
	 * run. Leave the receiver in a default state if no persistent data is
	 * found.
	 * 
	 * @return true if a file was successfully read and false otherwise
	 */
    private boolean readPersistedData_file() {
	    URL persUrl = null;

	    Location configLoc = Platform.getConfigurationLocation();
	    if (configLoc != null) {
			persUrl = getPersistenceUrl(configLoc.getURL(), false);
		}

	    try {
	        // inside try to get the safe default creation in the finally
	        // clause
	        if (persUrl == null) {
				return false;
			}

	        // E.g.,
	        //	<launchWorkspaceData>
	        //		<protocol version="1"/>
	        //      <alwaysAsk showDialog="1"/>
	        // 		<recentWorkspaces maxLength="5">
	        //			<workspace path="C:\eclipse\workspace0"/>
	        //			<workspace path="C:\eclipse\workspace1"/>
	        //		</recentWorkspaces>
	        //	</launchWorkspaceData>

	        Reader reader = new FileReader(persUrl.getFile());
	        XMLMemento memento = XMLMemento.createReadRoot(reader);
	        if (memento == null || !compatibleFileProtocol(memento)) {
				return false;
			}

	        IMemento alwaysAskTag = memento.getChild(XML.ALWAYS_ASK);
	        showDialog = alwaysAskTag == null ? true : alwaysAskTag.getInteger(
	                XML.SHOW_DIALOG).intValue() == 1;

	        IMemento recent = memento.getChild(XML.RECENT_WORKSPACES);
	        if (recent == null) {
				return false;
			}

	        Integer maxLength = recent.getInteger(XML.MAX_LENGTH);
	        int max = RECENT_MAX_LENGTH;
	        if (maxLength != null) {
				max = maxLength.intValue();
			}

	        IMemento indices[] = recent.getChildren(XML.WORKSPACE);
	        if (indices == null || indices.length <= 0) {
				return false;
			}

	        // if a user has edited maxLength to be shorter than the listed
	        // indices, accept the list (its tougher for them to retype a long
	        // list of paths than to update a max number)
	        max = Math.max(max, indices.length);

	        recentWorkspaces = new String[max];
	        for (int i = 0; i < indices.length; ++i) {
	            String path = indices[i].getString(XML.PATH);
	            if (path == null) {
					break;
				}
	            recentWorkspaces[i] = path;
	        }
	    } catch (IOException e) {
	        // cannot log because instance area has not been set
	        return false;
	    } catch (WorkbenchException e) {
	        // cannot log because instance area has not been set
	        return false;
	    } finally {
	        // create safe default if needed
	        if (recentWorkspaces == null) {
				recentWorkspaces = new String[RECENT_MAX_LENGTH];
			}
	    }

	    return true;
	}

    /**
     * Return the current (persisted) value of the "showDialog on startup"
     * preference. Return the global default if the file cannot be accessed.
     */
    public static boolean getShowDialogValue() {
    	// TODO See the long comment in #readPersistedData -- when the
		//      transition time is over this method can be changed to
    	//      read the preference directly.

        ChooseWorkspaceData data = new ChooseWorkspaceData(""); //$NON-NLS-1$

        // return either the value in the file or true, which is the global
        // default
        return data.readPersistedData() ? data.showDialog : true;
    }

    /**
	 * Return the current (persisted) value of the "showDialog on startup"
	 * preference. Return the global default if the file cannot be accessed.
	 */
	public static void setShowDialogValue(boolean showDialog) {
		// TODO See the long comment in #readPersistedData -- when the
		//      transition time is over this method can be changed to
		//      read the preference directly.

		ChooseWorkspaceData data = new ChooseWorkspaceData(""); //$NON-NLS-1$

		// update the value and write the new settings
		data.showDialog = showDialog;
		data.writePersistedData();
	}

    /**
	 * Look in the config area preference store for the list of recently used
	 * workspaces.
	 * 
	 * NOTE: During the transition phase the file will be checked if no config
	 * preferences are found.
	 * 
	 * @return true if the values were successfully retrieved and false
	 *         otherwise
	 */
	public boolean readPersistedData() {
		IPreferenceStore store = new ScopedPreferenceStore(
				new ConfigurationScope(), IDEWorkbenchPlugin.IDE_WORKBENCH);

		// The old way was to store this information in a file, the new is to
		// use the configuration area preference store. To help users with the
		// transition, this code always looks for values in the preference
		// store; they are used if found. If there aren't any related
		// preferences, then the file method is used instead. This class always
		// writes to the preference store, so the fall-back should be needed no
		// more than once per-user, per-configuration.

		// This code always sets the value of the protocol to a non-zero value
		// (currently at 2).  If the value comes back as the default (0), then
		// none of the preferences were set, revert to the file method.

		int protocol = store
				.getInt(IDE.Preferences.RECENT_WORKSPACES_PROTOCOL);
		if (protocol == IPreferenceStore.INT_DEFAULT_DEFAULT
				&& readPersistedData_file()) {
			return true;
		}

		// 2. get value for showDialog
		showDialog = store
				.getBoolean(IDE.Preferences.SHOW_WORKSPACE_SELECTION_DIALOG);

		// 3. use value of numRecent to create proper length array
		int max = store
				.getInt(IDE.Preferences.MAX_RECENT_WORKSPACES);
		max = Math.max(max, RECENT_MAX_LENGTH);

		// 4. load values of recent workspaces into array
		String workspacePathPref = store
				.getString(IDE.Preferences.RECENT_WORKSPACES);
		recentWorkspaces = decodeStoredWorkspacePaths(protocol, max, workspacePathPref);

		return true;
	}

	/**
	 * The the list of recent workspaces must be stored as a string in the preference node.
	 */
    private static String encodeStoredWorkspacePaths(String[] recent) {
		StringBuffer buff = new StringBuffer();

		String path = null;
		for (int i = 0; i < recent.length; ++i) {
			if (recent[i] == null) {
				break;
			}

			// as of 3.3.1 pump this out using newlines instead of commas
			if (path != null) {
				buff.append("\n"); //$NON-NLS-1$
			}

			path = recent[i];
			buff.append(path);
		}

		return buff.toString();
	}

	/**
	 * The the preference for recent workspaces must be converted from the
	 * storage string into an array.
	 */
    private static String[] decodeStoredWorkspacePaths(int protocol, int max,
			String prefValue) {
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
		if (tokens == null) // unknown version? corrupt file? we can't log it
							// because we dont have a workspace yet...
			return new String[0];
			

		StringTokenizer tokenizer = new StringTokenizer(prefValue, tokens);
		for (int i = 0; i < paths.length && tokenizer.hasMoreTokens(); ++i) {
			paths[i] = tokenizer.nextToken();
		}

		return paths;
	}

    /**
	 * Return true if the protocol used to encode the argument memento is
	 * compatible with the receiver's implementation and false otherwise.
	 */
    private static boolean compatibleFileProtocol(IMemento memento) {
        IMemento protocolMemento = memento.getChild(XML.PROTOCOL);
        if (protocolMemento == null) {
			return false;
		}

        Integer version = protocolMemento.getInteger(XML.VERSION);
        return version != null && version.intValue() == PERS_ENCODING_VERSION;
    }

    /**
     * The workspace data is stored in the well known file pointed to by the result
     * of this method.
     * @param create If the directory and file does not exist this parameter
     *               controls whether it will be created.
     * @return An url to the file and null if it does not exist or could not
     *         be created.
     */
    private static URL getPersistenceUrl(URL baseUrl, boolean create) {
        if (baseUrl == null) {
			return null;
		}

        try {
            // make sure the directory exists
            URL url = new URL(baseUrl, PERS_FOLDER);
            File dir = new File(url.getFile());
            if (!dir.exists() && (!create || !dir.mkdir())) {
				return null;
			}

            // make sure the file exists
            url = new URL(dir.toURL(), PERS_FILENAME);
            File persFile = new File(url.getFile());
            if (!persFile.exists() && (!create || !persFile.createNewFile())) {
				return null;
			}

            return persFile.toURL();
        } catch (IOException e) {
            // cannot log because instance area has not been set
            return null;
        }
    }
}

/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

/**
 * This class stores the information behind the "Launch Workspace" dialog. The
 * class is able to read and write itself to a well known configuration file.
 */
public class ChooseWorkspaceData {
    /**
     * The default max length of the recent workspace mru list.  The values
     * stored in xml (both the max-length parameter and actual size of the
     * list) will supersede this value. 
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

    private static final int PERS_ENCODING_VERSION = 1;

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
        if (instanceUrl != null)
            setInitialDefault(new File(instanceUrl.getFile()).toString());
    }

    /**
     * Return the folder to be used as a default if no other information
     * exists. Does not return null.
     */
    public String getInitialDefault() {
        if (initialDefault == null)
            setInitialDefault(System.getProperty("user.dir") //$NON-NLS-1$
                    + File.separator + "workspace"); //$NON-NLS-1$
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
        while (dir.charAt(dir.length() - 1) == File.separatorChar)
            dir = dir.substring(0, dir.length() - 1);
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
     * Update the persistent store.  Call this function after the currently selected
     * value has been found to be ok.
     */
    public void writePersistedData() {
        Location configLoc = Platform.getConfigurationLocation();
        if (configLoc == null || configLoc.isReadOnly())
            return;

        URL persUrl = getPersistenceUrl(configLoc.getURL(), true);
        if (persUrl == null)
            return;

        // move the new selection to the front of the list
        if (selection != null) {
            String oldEntry = recentWorkspaces[0];
            recentWorkspaces[0] = selection;
            for (int i = 1; i < recentWorkspaces.length && oldEntry != null; ++i) {
                if (selection.equals(oldEntry))
                    break;
                String tmp = recentWorkspaces[i];
                recentWorkspaces[i] = oldEntry;
                oldEntry = tmp;
            }
        }

        Writer writer = null;
        try {
            writer = new FileWriter(persUrl.getFile());

            // E.g.,
            //	<launchWorkspaceData>
            //		<protocol version="1"/>
            //      <alwaysAsk showDialog="1"/>
            // 		<recentWorkspaces maxLength="5">
            //			<workspace path="C:\eclipse\workspace0"/>
            //			<workspace path="C:\eclipse\workspace1"/>
            //		</recentWorkspaces>
            //	</launchWorkspaceData>

            XMLMemento memento = XMLMemento
                    .createWriteRoot("launchWorkspaceData"); //$NON-NLS-1$

            memento.createChild(XML.PROTOCOL).putInteger(XML.VERSION,
                    PERS_ENCODING_VERSION);

            memento.createChild(XML.ALWAYS_ASK).putInteger(XML.SHOW_DIALOG,
                    showDialog ? 1 : 0);

            IMemento recentMemento = memento.createChild(XML.RECENT_WORKSPACES);
            recentMemento.putInteger(XML.MAX_LENGTH, recentWorkspaces.length);
            for (int i = 0; i < recentWorkspaces.length; ++i) {
                if (recentWorkspaces[i] == null)
                    break;
                recentMemento.createChild(XML.WORKSPACE).putString(XML.PATH,
                        recentWorkspaces[i]);
            }
            memento.save(writer);
        } catch (IOException e) {
            IDEWorkbenchPlugin.log("Unable to write recent workspace data", //$NON-NLS-1$
                    StatusUtil.newStatus(IStatus.ERROR,
                            e.getMessage() == null ? "" : e.getMessage(), //$NON-NLS-1$
                            e));
        } finally {
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException e1) {
                    // do nothing
                }
        }
    }

    /**
     * Look for and read data that might have been persisted from some previous
     * run. Leave the receiver in a default state if no persistent data is
     * found.
     * @return true if a file was successfully read and false otherwise
     */
    private boolean readPersistedData() {
        URL persUrl = null;

        Location configLoc = Platform.getConfigurationLocation();
        if (configLoc != null)
            persUrl = getPersistenceUrl(configLoc.getURL(), false);

        try {
            // inside try to get the safe default creation in the finally
            // clause
            if (persUrl == null)
                return false;

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
            if (memento == null || !compatibleProtocol(memento))
                return false;

            IMemento alwaysAskTag = memento.getChild(XML.ALWAYS_ASK);
            showDialog = alwaysAskTag == null ? true : alwaysAskTag.getInteger(
                    XML.SHOW_DIALOG).intValue() == 1;

            IMemento recent = memento.getChild(XML.RECENT_WORKSPACES);
            if (recent == null)
                return false;

            Integer maxLength = recent.getInteger(XML.MAX_LENGTH);
            int max = RECENT_MAX_LENGTH;
            if (maxLength != null)
                max = maxLength.intValue();

            IMemento indices[] = recent.getChildren(XML.WORKSPACE);
            if (indices == null || indices.length <= 0)
                return false;

            // if a user has edited maxLength to be shorter than the listed
            // indices, accept the list (its tougher for them to retype a long
            // list of paths than to update a max number)
            max = Math.max(max, indices.length);

            recentWorkspaces = new String[max];
            for (int i = 0; i < indices.length; ++i) {
                String path = indices[i].getString(XML.PATH);
                if (path == null)
                    break;
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
            if (recentWorkspaces == null)
                recentWorkspaces = new String[RECENT_MAX_LENGTH];
        }

        return true;
    }

    /**
     * Return the current (persisted) value of the "showDialog on startup"
     * preference. Return the global default if the file cannot be accessed.
     */
    public static boolean getShowDialogValue() {
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
        ChooseWorkspaceData data = new ChooseWorkspaceData(""); //$NON-NLS-1$

        // if the file didn't exist, then don't create a new one
        if (!data.readPersistedData())
            return;

        // update the value and write the new settings
        data.showDialog = showDialog;
        data.writePersistedData();
    }

    /**
     * Return true if the protocol used to encode the argument memento is compatible
     * with the receiver's implementation and false otherwise.
     */
    private static boolean compatibleProtocol(IMemento memento) {
        IMemento protocolMemento = memento.getChild(XML.PROTOCOL);
        if (protocolMemento == null)
            return false;

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
        if (baseUrl == null)
            return null;

        try {
            // make sure the directory exists
            URL url = new URL(baseUrl, PERS_FOLDER);
            File dir = new File(url.getFile());
            if (!dir.exists() && (!create || !dir.mkdir()))
                return null;

            // make sure the file exists
            url = new URL(dir.toURL(), PERS_FILENAME);
            File persFile = new File(url.getFile());
            if (!persFile.exists() && (!create || !persFile.createNewFile()))
                return null;

            return persFile.toURL();
        } catch (IOException e) {
            // cannot log because instance area has not been set
            return null;
        }
    }
}
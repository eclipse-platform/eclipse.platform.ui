/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.core;
 
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.preferences.*;
import org.eclipse.team.core.Team;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.ConsoleListeners;
import org.eclipse.team.internal.ccvs.core.client.Command.KSubstOption;
import org.eclipse.team.internal.ccvs.core.client.Command.QuietOption;
import org.eclipse.team.internal.ccvs.core.client.listeners.IConsoleListener;
import org.eclipse.team.internal.ccvs.core.resources.FileModificationManager;
import org.eclipse.team.internal.ccvs.core.util.*;
import org.osgi.framework.BundleContext;

public class CVSProviderPlugin extends Plugin {
	
	// preference names
	public static final String READ_ONLY = "cvs.read.only"; //$NON-NLS-1$

	// external command to run for ext connection method
	public static final String DEFAULT_CVS_RSH = "ssh"; //$NON-NLS-1$
	// external command parameters
	public static final String DEFAULT_CVS_RSH_PARAMETERS = "{host} -l {user}"; //$NON-NLS-1$
	// remote command to run for ext connection method
	public static final String DEFAULT_CVS_SERVER = "cvs"; //$NON-NLS-1$
	// determines if empty directories received from the server should be pruned.
	public static final boolean DEFAULT_PRUNE = true;
	// determines if the user is prompted for confirmation before moving tags during a tag operation.
	public static final boolean DEFAULT_CONFIRM_MOVE_TAG = true;
	// determines if new directories should be discovered during update.
	public static final boolean DEFAULT_FETCH = true;
	// communication timeout with the server
	public static final int DEFAULT_TIMEOUT = 60;
	// file transfer compression level (0 - 9)
	public static final int DEFAULT_COMPRESSION_LEVEL = 0;
	// default text keyword substitution mode
	public static final KSubstOption DEFAULT_TEXT_KSUBST_OPTION = Command.KSUBST_TEXT_EXPAND;

	// cvs plugin extension points and ids
	public static final String ID = "org.eclipse.team.cvs.core"; //$NON-NLS-1$
	
	public static final QualifiedName CVS_WORKSPACE_SUBSCRIBER_ID = new QualifiedName("org.eclipse.team.cvs.ui.cvsworkspace-participant", "syncparticipant"); //$NON-NLS-1$ //$NON-NLS-2$
	public static final String PT_AUTHENTICATOR = "authenticator"; //$NON-NLS-1$
	public static final String PT_CONNECTIONMETHODS = "connectionmethods"; //$NON-NLS-1$
	public static final String PT_FILE_MODIFICATION_VALIDATOR = "filemodificationvalidator"; //$NON-NLS-1$
	
	private QuietOption quietness;
	private int compressionLevel = DEFAULT_COMPRESSION_LEVEL;
	private KSubstOption defaultTextKSubstOption = DEFAULT_TEXT_KSUBST_OPTION;
	private boolean usePlatformLineend = true;
	private int communicationsTimeout = DEFAULT_TIMEOUT;
	private boolean pruneEmptyDirectories = DEFAULT_PRUNE;
	private boolean fetchAbsentDirectories = DEFAULT_FETCH;
	private boolean promptOnFileDelete = true;
	private boolean promptOnFolderDelete = true;
	private boolean replaceUnmanaged = true;
	private boolean repositoriesAreBinary = false;
	private String cvsRshCommand = DEFAULT_CVS_RSH;
	private String cvsRshParameters = DEFAULT_CVS_RSH_PARAMETERS;
	private String cvsServer = DEFAULT_CVS_SERVER;
	private IConsoleListener consoleListener;
	private boolean determineVersionEnabled = true;
	private boolean confirmMoveTagEnabled = true;
	
	private static volatile CVSProviderPlugin instance;
	
	// CVS specific resource delta listeners
	private BuildCleanupListener addDeleteMoveListener;
	private FileModificationManager fileModificationManager;
	private SyncFileChangeListener metaFileSyncListener;

	private static final String REPOSITORIES_STATE_FILE = ".cvsProviderState"; //$NON-NLS-1$
	// version numbers for the state file (a positive number indicates version 1)
	private static final int REPOSITORIES_STATE_FILE_VERSION_2 = -1;
	private static List decoratorEnablementListeners = new ArrayList();
	
	private CVSWorkspaceSubscriber cvsWorkspaceSubscriber;
	
	/**
	 * The identifier for the CVS nature
	 * (value <code>"org.eclipse.team.cvs.core.nature"</code>).
	 * The presence of this nature on a project indicates that it is 
	 * CVS-capable.
	 *
	 * @see org.eclipse.core.resources.IProject#hasNature
	 */
	private static final String NATURE_ID = ID + ".cvsnature"; //$NON-NLS-1$

	// File used to idicate if the workbench was shut down properly or not
	private static final String CRASH_INDICATION_FILE  = ".running"; //$NON-NLS-1$
	private boolean crash;

	public synchronized CVSWorkspaceSubscriber getCVSWorkspaceSubscriber() {
		if (cvsWorkspaceSubscriber == null) {
			cvsWorkspaceSubscriber = new CVSWorkspaceSubscriber(
					CVS_WORKSPACE_SUBSCRIBER_ID, 
					Policy.bind("CVSProviderPlugin.20"),   //$NON-NLS-1$
					Policy.bind("CVSProviderPlugin.21")); //$NON-NLS-1$
		}
		return cvsWorkspaceSubscriber;
	}

	/**
	 * @return
	 */
	public boolean isConfirmMoveTagEnabled() {
		return confirmMoveTagEnabled;
	}

	/**
	 * @param confirmMoveTag
	 */
	public void setConfirmMoveTagEnabled(boolean confirmMoveTag) {
		this.confirmMoveTagEnabled = confirmMoveTag;
	}

	/**
	 * Constructor for CVSProviderPlugin.
	 * @param descriptor
	 */
	public CVSProviderPlugin() {
		super();
		instance = this;
	}
	
	/**
	 * Convenience method for logging CoreExceptions to the plugin log
	 */
	public static void log(CoreException e) {
		log(e.getStatus().getSeverity(), e.getMessage(), e);
	}
	
	/**
	 * Log the given exception along with the provided message and severity indicator
	 */
	public static void log(int severity, String message, Throwable e) {
		log(new Status(severity, ID, 0, message, e));
	}
	
	/**
	 * Log the given status. Do not use this method for the IStatus from a CoreException.
	 * Use<code>log(CoreException)</code> instead so the stack trace is not lost.
	 */
	public static void log(IStatus status) {
		getPlugin().getLog().log(status);
	}

	/**
	 * Returns the singleton plug-in instance.
	 * 
	 * @return the plugin instance
	 */
	public static CVSProviderPlugin getPlugin() {
		return instance;
	}
	
	/**
	 * Answers the repository provider type id for the cvs plugin
	 */
	public static String getTypeId() {
		return NATURE_ID;
	}
	
	/**
	 * Sets the file transfer compression level. (if supported)
	 * Valid levels are: 0 (disabled), 1 (worst/fastest) - 9 (best/slowest)
	 */
	public void setCompressionLevel(int level) {
		compressionLevel = level;
	}

	/**
	 * Gets the file transfer compression level.
	 */
	public int getCompressionLevel() {
		return compressionLevel;
	}
	
	/**
	 * Sets the default keyword substitution mode for text files.
	 */
	public void setDefaultTextKSubstOption(KSubstOption ksubst) {
		defaultTextKSubstOption = ksubst;
	}


	/**
	 * Gets the default keyword substitution mode for text files.
	 */
	public KSubstOption getDefaultTextKSubstOption() {
		return defaultTextKSubstOption;
	}

	/**
	 * Should the CVS adapter prune empty directories
	 */
	public boolean getPruneEmptyDirectories() {
		return pruneEmptyDirectories;
	}

	/**
	 * Set whether the CVS adapter should prune empty directories
	 */
	public void setPruneEmptyDirectories(boolean prune) {
		pruneEmptyDirectories = prune;
	}

	/**
	 * Get the communications timeout value in seconds
	 */
	public int getTimeout() {
		return communicationsTimeout;
	}
	
	/**
	 * Set the timeout value for communications to a value in seconds.
	 * The value must be greater than or equal 0. If is it 0, there is no timeout.
	 */
	public void setTimeout(int timeout) {
		this.communicationsTimeout = Math.max(0, timeout);
	}
	
	/**
	 * Set the quietness option to use with cvs commands.
	 * Can be "", "-q" or "-Q"
	 */
	public void setQuietness(QuietOption option) {
			this.quietness = option;
	}

	/**
	 * Get the quietness option for commands
	 */
	public QuietOption getQuietness() {
		return quietness;
	}
	
	/**
	 * Set the console listener for commands.
	 * @param consoleListener the listener
	 */
	public void setConsoleListener(IConsoleListener consoleListener) {
	    ConsoleListeners.getInstance().addListener(consoleListener);
	}
	
	/**
	 * @see Plugin#start(BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		Policy.localize("org.eclipse.team.internal.ccvs.core.messages"); //$NON-NLS-1$

		// load the state which includes the known repositories
		loadOldState();
		crash = createCrashFile();
		
		// Initialize CVS change listeners. Note tha the report type is important.
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		addDeleteMoveListener = new BuildCleanupListener();
		fileModificationManager = new FileModificationManager();
		metaFileSyncListener = new SyncFileChangeListener();
		workspace.addResourceChangeListener(addDeleteMoveListener, IResourceChangeEvent.POST_BUILD);
		workspace.addResourceChangeListener(metaFileSyncListener, IResourceChangeEvent.POST_CHANGE);
		workspace.addResourceChangeListener(fileModificationManager, IResourceChangeEvent.POST_CHANGE);
		fileModificationManager.registerSaveParticipant();
		
		getCVSWorkspaceSubscriber();
	}
	
	/**
	 * @see Plugin#stop(BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		try {
			savePluginPreferences();
			
			// remove listeners
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			workspace.removeResourceChangeListener(metaFileSyncListener);
			workspace.removeResourceChangeListener(fileModificationManager);
			workspace.removeResourceChangeListener(addDeleteMoveListener);
			
			// remove all of this plugin's save participants. This is easier than having
			// each class that added itself as a participant to have to listen to shutdown.
			workspace.removeSaveParticipant(this);
			
			deleteCrashFile();
		} finally {
			super.stop(context);
		}
	}
		
	/**
	 * @see org.eclipse.core.runtime.Plugin#initializeDefaultPluginPreferences()
	 */
	protected void initializeDefaultPluginPreferences(){
		Preferences store = getPluginPreferences();
		store.setDefault(READ_ONLY, false);
	}
	
	/**
	 * Gets the cvsRshCommand.
	 * @return Returns a String
	 */
	public String getCvsRshCommand() {
		return cvsRshCommand;
	}

	/**
	 * Sets the cvsRshCommand.
	 * @param cvsRshCommand The cvsRshCommand to set
	 */
	public void setCvsRshCommand(String cvsRshCommand) {
		this.cvsRshCommand = cvsRshCommand;
	}

	/**
	 * Returns the cvsRshParameters.
	 * @return String
	 */
	public String getCvsRshParameters() {
		return cvsRshParameters;
	}

	/**
	 * Sets the cvsRshParameters.
	 * @param cvsRshParameters The cvsRshParameters to set
	 */
	public void setCvsRshParameters(String cvsRshParameters) {
		this.cvsRshParameters = cvsRshParameters;
	}
	
	/**
	 * Gets the cvsServer.
	 * @return Returns a String
	 */
	public String getCvsServer() {
		return cvsServer;
	}

	/**
	 * Sets the cvsServer.
	 * @param cvsServer The cvsServer to set
	 */
	public void setCvsServer(String cvsServer) {
		this.cvsServer = cvsServer;
	}

	/**
	 * Gets the etchAbsentDirectories.
	 * @return Returns a boolean
	 */
	public boolean getFetchAbsentDirectories() {
		return fetchAbsentDirectories;
	}

	public boolean getRepositoriesAreBinary() {
		return repositoriesAreBinary;
	}
	
	/**
	 * Sets the fetchAbsentDirectories.
	 * @param etchAbsentDirectories The etchAbsentDirectories to set
	 */
	public void setFetchAbsentDirectories(boolean fetchAbsentDirectories) {
		this.fetchAbsentDirectories = fetchAbsentDirectories;
	}
	
	public boolean getPromptOnFileDelete() {
		return promptOnFileDelete;
	}
	
	public void setPromptOnFileDelete(boolean prompt) {
		promptOnFileDelete = prompt;
	}
	
	public void setRepositoriesAreBinary(boolean binary) {
		repositoriesAreBinary = binary;
	}
	
	public boolean getPromptOnFolderDelete() {
		return promptOnFolderDelete;
	}
	
	public void setPromptOnFolderDelete(boolean prompt) {
		promptOnFolderDelete = prompt;
	}
	
	public static void broadcastDecoratorEnablementChanged(final boolean enabled) {
		ICVSDecoratorEnablementListener[] listeners;
		synchronized(decoratorEnablementListeners) {
			listeners = (ICVSDecoratorEnablementListener[]) decoratorEnablementListeners.toArray(new ICVSDecoratorEnablementListener[decoratorEnablementListeners.size()]);
		}
		for (int i = 0; i < listeners.length; i++) {
			final ICVSDecoratorEnablementListener listener = listeners[i];
			ISafeRunnable code = new ISafeRunnable() {
				public void run() throws Exception {
					listener.decoratorEnablementChanged(enabled);
				}
				public void handleException(Throwable e) {
					// don't log the exception....it is already being logged in Platform#run
				}
			};
			Platform.run(code);
		}
	}
	
	/**
	 * Gets the replaceUnmanaged.
	 * @return Returns a boolean
	 */
	public boolean isReplaceUnmanaged() {
		return replaceUnmanaged;
	}

	/**
	 * Sets the replaceUnmanaged.
	 * @param replaceUnmanaged The replaceUnmanaged to set
	 */
	public void setReplaceUnmanaged(boolean replaceUnmanaged) {
		this.replaceUnmanaged = replaceUnmanaged;
	}

		
	/**
	 * Register to receive notification of repository creation and disposal
	 */
	public void addRepositoryListener(ICVSListener listener) {
		KnownRepositories.getInstance().addRepositoryListener(listener);
	}
	
	/**
	 * Register to receive notification of enablement of sync info decoration requirements. This
	 * can be useful for providing lazy initialization of caches that are only required for decorating
	 * resource with CVS information.
	 */
	public void addDecoratorEnablementListener(ICVSDecoratorEnablementListener listener) {
		synchronized(decoratorEnablementListeners) {
			decoratorEnablementListeners.add(listener);
		}
	}
	
	/**
	 * De-register a listener
	 */
	public void removeRepositoryListener(ICVSListener listener) {
		KnownRepositories.getInstance().removeRepositoryListener(listener);
	}
	
	/**
	 * De-register the decorator enablement listener. 
	 */
	public void removeDecoratorEnablementListener(ICVSDecoratorEnablementListener listener) {
		synchronized(decoratorEnablementListeners) {
			decoratorEnablementListeners.remove(listener);
		}
	}
	
	/** 
	 * Return a list of the know repository locations. This is left
	 * here to isolate the RelEng tools plugin from changes in CVS core.
	 */
	public ICVSRepositoryLocation[] getKnownRepositories() {
		return KnownRepositories.getInstance().getRepositories();
	}

	private void loadOldState() {
		try {
			IPath pluginStateLocation = CVSProviderPlugin.getPlugin().getStateLocation().append(REPOSITORIES_STATE_FILE);
			File file = pluginStateLocation.toFile();
			if (file.exists()) {
				try {
					DataInputStream dis = new DataInputStream(new FileInputStream(file));
					readOldState(dis);
					dis.close();
					// The file is no longer needed as the state is
					// persisted in the user settings
					file.delete();
				} catch (IOException e) {
					throw new TeamException(new Status(Status.ERROR, CVSProviderPlugin.ID, TeamException.UNABLE, Policy.bind("CVSProvider.ioException"), e));  //$NON-NLS-1$
				}
			}
		} catch (TeamException e) {
			Util.logError(Policy.bind("CVSProvider.errorLoading"), e);//$NON-NLS-1$
		}
	}
	
	private void readOldState(DataInputStream dis) throws IOException, CVSException {
		KnownRepositories instance = KnownRepositories.getInstance();
		int count = dis.readInt();
		if (count >= 0) {
			// this is the version 1 format of the state file
			for (int i = 0; i < count; i++) {
				ICVSRepositoryLocation location = instance.getRepository(dis.readUTF());
				instance.addRepository(location, false /* no need to broadcast on startup */);
			}
		} else if (count == REPOSITORIES_STATE_FILE_VERSION_2) {
			count = dis.readInt();
			for (int i = 0; i < count; i++) {
				ICVSRepositoryLocation location = instance.getRepository(dis.readUTF());
				instance.addRepository(location, false /* no need to broadcast on startup */);
				// Read the next field which is no longer used
				dis.readUTF();
			}
		} else {
			Util.logError(Policy.bind("CVSProviderPlugin.unknownStateFileVersion", new Integer(count).toString()), null); //$NON-NLS-1$
		}
	}
		
	public static boolean isText(IFile file) {
		if (CVSProviderPlugin.getPlugin().getRepositoriesAreBinary()) return false;
		return Team.getType(file) == Team.TEXT;
	}
	
	/**
	 * Gets the determineVersionEnabled.
	 * @return boolean
	 */
	public boolean isDetermineVersionEnabled() {
		return determineVersionEnabled;
	}

	/**
	 * Sets the determineVersionEnabled.
	 * @param determineVersionEnabled The determineVersionEnabled to set
	 */
	public void setDetermineVersionEnabled(boolean determineVersionEnabled) {
		this.determineVersionEnabled = determineVersionEnabled;
	}
	
	/**
	 * Returns the fileModificationManager.
	 * @return FileModificationManager
	 */
	public FileModificationManager getFileModificationManager() {
		return fileModificationManager;
	}

	/**
	 * @return boolean
	 */
	public boolean isWatchEditEnabled() {
		return getPluginPreferences().getBoolean(CVSProviderPlugin.READ_ONLY);
	}

	public void setDebugProtocol(boolean value) {
		Policy.DEBUG_CVS_PROTOCOL = value;		
	}
	
	public boolean isDebugProtocol() {
		return Policy.DEBUG_CVS_PROTOCOL;
	}

	/*
	 * Create the crash indicator file. This method returns true if the file
	 * already existed, indicating that a crash occurred on the last invokation of
	 * the platform.
	 */
	private boolean createCrashFile() {
		IPath pluginStateLocation = CVSProviderPlugin.getPlugin().getStateLocation();
		File crashFile = pluginStateLocation.append(CRASH_INDICATION_FILE).toFile();
		if (crashFile.exists()) {
			return true;
		}
		try {
			crashFile.createNewFile();
		} catch (IOException e) {
			CVSProviderPlugin.log(IStatus.ERROR, e.getMessage(), e);
		}
		return false;
	}
	
	private void deleteCrashFile() {
		IPath pluginStateLocation = CVSProviderPlugin.getPlugin().getStateLocation();
		File crashFile = pluginStateLocation.append(CRASH_INDICATION_FILE).toFile();
		crashFile.delete();
	}
	
	public boolean crashOnLastRun() {
		return crash;
	}
	
	/**
	 * Return the CVS preferences node in the instance scope
	 */
	public org.osgi.service.prefs.Preferences getInstancePreferences() {
		IPreferencesService service = Platform.getPreferencesService();
		IEclipsePreferences root = service.getRootNode();
		return root.node(InstanceScope.SCOPE).node(getBundle().getSymbolicName());
	}
	
	/**
	 * @return Returns the usePlatformLineend.
	 */
	public boolean isUsePlatformLineend() {
		return usePlatformLineend;
	}
	/**
	 * @param usePlatformLineend The usePlatformLineend to set.
	 */
	public void setUsePlatformLineend(boolean usePlatformLineend) {
		this.usePlatformLineend = usePlatformLineend;
	}
}

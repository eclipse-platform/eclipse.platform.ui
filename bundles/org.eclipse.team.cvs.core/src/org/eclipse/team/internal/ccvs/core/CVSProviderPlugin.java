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
package org.eclipse.team.internal.ccvs.core;
 
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.Team;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Command.KSubstOption;
import org.eclipse.team.internal.ccvs.core.client.Command.QuietOption;
import org.eclipse.team.internal.ccvs.core.client.listeners.IConsoleListener;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.resources.FileModificationManager;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.AddDeleteMoveListener;
import org.eclipse.team.internal.ccvs.core.util.ProjectDescriptionManager;
import org.eclipse.team.internal.ccvs.core.util.SyncFileChangeListener;
import org.eclipse.team.internal.ccvs.core.util.Util;

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
	public static final String PT_AUTHENTICATOR = "authenticator"; //$NON-NLS-1$
	public static final String PT_CONNECTIONMETHODS = "connectionmethods"; //$NON-NLS-1$
	public static final String PT_FILE_MODIFICATION_VALIDATOR = "filemodificationvalidator"; //$NON-NLS-1$
	
	// Directory to cache file contents
	private static final String CACHE_DIRECTORY = ".cache"; //$NON-NLS-1$
	// Maximum lifespan of local cache file, in milliseconds
	private static final long CACHE_FILE_LIFESPAN = 60*60*1000; // 1hr
	
	private Hashtable cacheFileNames;
	private Hashtable cacheFileTimes;
	private long lastCacheCleanup;
	private int cacheDirSize;
	
	private QuietOption quietness;
	private int compressionLevel = DEFAULT_COMPRESSION_LEVEL;
	private KSubstOption defaultTextKSubstOption = DEFAULT_TEXT_KSUBST_OPTION;
	private int communicationsTimeout = DEFAULT_TIMEOUT;
	private boolean pruneEmptyDirectories = DEFAULT_PRUNE;
	private boolean fetchAbsentDirectories = DEFAULT_FETCH;
	private boolean promptOnFileDelete = true;
	private boolean promptOnFolderDelete = true;
	private boolean showTasksOnAddAndDelete = false;
	private boolean replaceUnmanaged = true;
	private boolean repositoriesAreBinary = false;
	private String cvsRshCommand = DEFAULT_CVS_RSH;
	private String cvsRshParameters = DEFAULT_CVS_RSH_PARAMETERS;
	private String cvsServer = DEFAULT_CVS_SERVER;
	private IConsoleListener consoleListener;
	private boolean determineVersionEnabled = true;
	
	private static CVSProviderPlugin instance;
	
	// CVS specific resource delta listeners
	private IResourceChangeListener preAutoBuildListener;
	private AddDeleteMoveListener addDeleteMoveListener;
	private FileModificationManager fileModificationManager;

	private static final String REPOSITORIES_STATE_FILE = ".cvsProviderState"; //$NON-NLS-1$
	// version numbers for the state file (a positive number indicates version 1)
	private static final int REPOSITORIES_STATE_FILE_VERSION_2 = -1;
	private Map repositories = new HashMap();
	private List repositoryListeners = new ArrayList();
	private static List decoratorEnablementListeners = new ArrayList();
	
	/**
	 * The identifier for the CVS nature
	 * (value <code>"org.eclipse.team.cvs.core.nature"</code>).
	 * The presence of this nature on a project indicates that it is 
	 * CVS-capable.
	 *
	 * @see org.eclipse.core.resources.IProject#hasNature
	 */
	private static final String NATURE_ID = ID + ".cvsnature"; //$NON-NLS-1$

	/**
	 * Constructor for CVSProviderPlugin.
	 * @param descriptor
	 */
	public CVSProviderPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		setPlugin(this);
	}
	
	/**
	 * Convenience method for logging CVSExceptiuons to the plugin log
	 */
	public static void log(TeamException e) {
		// For now, we'll log the status. However we should do more
		log(e.getStatus());
	}
	public static void log(IStatus status) {
		// For now, we'll log the status. However we should do more
		getPlugin().getLog().log(status);
	}

	/**
	 * Returns the singleton plug-in instance.
	 * 
	 * @return the plugin instance
	 */
	public static synchronized CVSProviderPlugin getPlugin() {
		return instance;
	}

	private static synchronized void setPlugin(CVSProviderPlugin plugin) {
		instance = plugin;
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
		this.consoleListener = consoleListener;
	}

	/**
	 * Get the console listener for commands.
	 * @return the consoleListener, or null
	 */
	public IConsoleListener getConsoleListener() {
		return consoleListener;
	}
	
	/**
	 * @see Plugin#startup()
	 */
	public void startup() throws CoreException {
		super.startup();
		Policy.localize("org.eclipse.team.internal.ccvs.core.messages"); //$NON-NLS-1$

		// load the state which includes the known repositories
		loadState();
		
		// Initialize CVS change listeners. Note tha the report type is important.
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		addDeleteMoveListener = new AddDeleteMoveListener();
		fileModificationManager = new FileModificationManager();
		// Group the two PRE_AUTO_BUILD listeners together for efficiency
		final IResourceChangeListener projectDescriptionListener = new ProjectDescriptionManager();
		final IResourceChangeListener metaFileSyncListener = new SyncFileChangeListener();
		preAutoBuildListener = new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				projectDescriptionListener.resourceChanged(event);
				metaFileSyncListener.resourceChanged(event);
			}
		};
		workspace.addResourceChangeListener(preAutoBuildListener, IResourceChangeEvent.PRE_AUTO_BUILD);
		workspace.addResourceChangeListener(addDeleteMoveListener, IResourceChangeEvent.POST_AUTO_BUILD);
		workspace.addResourceChangeListener(fileModificationManager, IResourceChangeEvent.POST_CHANGE);
		fileModificationManager.registerSaveParticipant();
		CVSProviderPlugin.addResourceStateChangeListener(addDeleteMoveListener);
		
		createCacheDirectory();
	}
	
	/**
	 * @see Plugin#shutdown()
	 */
	public void shutdown() throws CoreException {
		super.shutdown();
		
		// save the state which includes the known repositories
		saveState();
		savePluginPreferences();
		
		// remove listeners
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(preAutoBuildListener);
		workspace.removeResourceChangeListener(fileModificationManager);
		workspace.removeResourceChangeListener(addDeleteMoveListener);
		
		// remove all of this plugin's save participants. This is easier than having
		// each class that added itself as a participant to have to listen to shutdown.
		workspace.removeSaveParticipant(this);
		
		deleteCacheDirectory();
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
	
	private static List listeners = new ArrayList();
	
	/*
	 * @see ITeamManager#addResourceStateChangeListener(IResourceStateChangeListener)
	 */
	public static void addResourceStateChangeListener(IResourceStateChangeListener listener) {
		listeners.add(listener);
	}

	/*
	 * @see ITeamManager#removeResourceStateChangeListener(IResourceStateChangeListener)
	 */
	public static void removeResourceStateChangeListener(IResourceStateChangeListener listener) {
		listeners.remove(listener);
	}
	
	public static void broadcastSyncInfoChanges(final IResource[] resources) {
		for(Iterator it=listeners.iterator(); it.hasNext();) {
			final IResourceStateChangeListener listener = (IResourceStateChangeListener)it.next();
			ISafeRunnable code = new ISafeRunnable() {
				public void run() throws Exception {
					listener.resourceSyncInfoChanged(resources);
				}
				public void handleException(Throwable e) {
					// don't log the exception....it is already being logged in Platform#run
				}
			};
			Platform.run(code);
		}
	}
	
	public static void broadcastDecoratorEnablementChanged(final boolean enabled) {
		for(Iterator it=decoratorEnablementListeners.iterator(); it.hasNext();) {
			final ICVSDecoratorEnablementListener listener = (ICVSDecoratorEnablementListener)it.next();
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
	
	public static void broadcastModificationStateChanges(final IResource[] resources) {
		for(Iterator it=listeners.iterator(); it.hasNext();) {
			final IResourceStateChangeListener listener = (IResourceStateChangeListener)it.next();
			ISafeRunnable code = new ISafeRunnable() {
				public void run() throws Exception {
					listener.resourceModified(resources);
				}
				public void handleException(Throwable e) {
					// don't log the exception....it is already being logged in Platform#run
				}
			};
			Platform.run(code);
		}
	}
	protected static void broadcastProjectConfigured(final IProject project) {
		for(Iterator it=listeners.iterator(); it.hasNext();) {
			final IResourceStateChangeListener listener = (IResourceStateChangeListener)it.next();
			ISafeRunnable code = new ISafeRunnable() {
				public void run() throws Exception {
					listener.projectConfigured(project);
				}
				public void handleException(Throwable e) {
					// don't log the exception....it is already being logged in Platform#run
				}
			};
			Platform.run(code);
		}
	}
	protected static void broadcastProjectDeconfigured(final IProject project) {
		for(Iterator it=listeners.iterator(); it.hasNext();) {
			final IResourceStateChangeListener listener = (IResourceStateChangeListener)it.next();
			ISafeRunnable code = new ISafeRunnable() {
				public void run() throws Exception {
					listener.projectDeconfigured(project);
				}
				public void handleException(Throwable e) {
					// don't log the exception....it is already being logged in Platform#run
				}
			};
			Platform.run(code);
		}
	}
	
	/**
	 * Gets the showTasksOnAddAndDelete.
	 * @return Returns a boolean
	 */
	public boolean getShowTasksOnAddAndDelete() {
		return showTasksOnAddAndDelete;
	}

	/**
	 * Sets the showTasksOnAddAndDelete.
	 * @param showTasksOnAddAndDelete The showTasksOnAddAndDelete to set
	 */
	public void setShowTasksOnAddAndDelete(boolean showTasksOnAddAndDelete) {
		this.showTasksOnAddAndDelete = showTasksOnAddAndDelete;
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

	private void createCacheDirectory() {
		try {
			IPath cacheLocation = getStateLocation().append(CACHE_DIRECTORY);
			File file = cacheLocation.toFile();
			if (file.exists()) {
				deleteFile(file);
			}
			file.mkdir();
			cacheFileNames = new Hashtable();
			cacheFileTimes = new Hashtable();
			lastCacheCleanup = -1;
			cacheDirSize = 0;
		} catch (IOException e) {
			log(new Status(IStatus.ERROR, ID, 0, Policy.bind("CVSProviderPlugin.errorCreatingCache", e.getMessage()), e)); //$NON-NLS-1$
		}
	}
			
	private void deleteCacheDirectory() {
		try {
			IPath cacheLocation = getStateLocation().append(CACHE_DIRECTORY);
			File file = cacheLocation.toFile();
			if (file.exists()) {
				deleteFile(file);
			}
			cacheFileNames = cacheFileTimes = null;
			lastCacheCleanup = -1;
			cacheDirSize = 0;
		} catch (IOException e) {
			log(new Status(IStatus.ERROR, ID, 0, Policy.bind("CVSProviderPlugin.errorDeletingCache", e.getMessage()), e)); //$NON-NLS-1$
		}
	}
	
	private void deleteFile(File file) throws IOException {
		if (file.isDirectory()) {
			File[] children = file.listFiles();
			for (int i = 0; i < children.length; i++) {
				deleteFile(children[i]);
			}
		}
		file.delete();
	}
	
	public synchronized File getCacheFileFor(String path) throws IOException {
		String physicalPath;
		if (cacheFileNames.containsKey(path)) {
			/*
			 * cache hit
			 */
			physicalPath = (String)cacheFileNames.get(path);
			registerHit(path);
		} else {
			/*
			 * cache miss
			 */
			physicalPath = String.valueOf(cacheDirSize++);
			cacheFileNames.put(path, physicalPath);
			registerHit(path);
			clearOldCacheEntries();
		}
		return getCacheFileForPhysicalPath(physicalPath);
	}
	private File getCacheFileForPhysicalPath(String physicalPath) throws IOException {
		return new File(getStateLocation().append(CACHE_DIRECTORY).toFile(), physicalPath);
	}
	private void registerHit(String path) {
		cacheFileTimes.put(path, Long.toString(new Date().getTime()));
	}
	private void clearOldCacheEntries() throws IOException {
		long current = new Date().getTime();
		if ((lastCacheCleanup!=-1) && (current - lastCacheCleanup < CACHE_FILE_LIFESPAN)) return;
		Enumeration e = cacheFileTimes.keys();
		while (e.hasMoreElements()) {
			String f = (String)e.nextElement();
			long lastHit = Long.valueOf((String)cacheFileTimes.get(f)).longValue();
			if ((current - lastHit) > CACHE_FILE_LIFESPAN) purgeCacheFile(f);
		}
		
	}
	private void purgeCacheFile(String path) throws IOException {
		File f = getCacheFileForPhysicalPath((String)cacheFileNames.get(path));
		f.delete();
		cacheFileTimes.remove(path);
		cacheFileNames.remove(path);
	}
	
	/*
	 * Add the repository location to the cahced locations
	 */
	private void addToRepositoriesCache(ICVSRepositoryLocation repository) {
		repositories.put(repository.getLocation(), repository);
		Iterator it = repositoryListeners.iterator();
		while (it.hasNext()) {
			ICVSListener listener = (ICVSListener)it.next();
			listener.repositoryAdded(repository);
		}
	}
	
	private void removeFromRepositoriesCache(ICVSRepositoryLocation repository) {
		if (repositories.remove(repository.getLocation()) != null) {
			Iterator it = repositoryListeners.iterator();
			while (it.hasNext()) {
				ICVSListener listener = (ICVSListener)it.next();
				listener.repositoryRemoved(repository);
			}
		}
	}
		
	/**
	 * Register to receive notification of repository creation and disposal
	 */
	public void addRepositoryListener(ICVSListener listener) {
		repositoryListeners.add(listener);
	}
	
	/**
	 * Register to receive notification of enablement of sync info decoration requirements. This
	 * can be useful for providing lazy initialization of caches that are only required for decorating
	 * resource with CVS information.
	 */
	public void addDecoratorEnablementListener(ICVSDecoratorEnablementListener listener) {
		decoratorEnablementListeners.add(listener);
	}
	
	/**
	 * De-register a listener
	 */
	public void removeRepositoryListener(ICVSListener listener) {
		repositoryListeners.remove(listener);
	}
	
	/**
	 * De-register the decorator enablement listener. 
	 */
	public void removeDecoratorEnablementListener(ICVSDecoratorEnablementListener listener) {
		decoratorEnablementListeners.remove(listener);
	}
	
	/**
	 * Create a repository instance from the given properties.
	 * The supported properties are:
	 * 
	 *   connection The connection method to be used
	 *   user The username for the connection
	 *   password The password used for the connection (optional)
	 *   host The host where the repository resides
	 *   port The port to connect to (optional)
	 *   root The server directory where the repository is located
	 * 
	 * The created instance is not known by the provider and it's user information is not cached.
	 * The purpose of the created location is to allow connection validation before adding the
	 * location to the provider.
	 * 
	 * This method will throw a CVSException if the location for the given configuration already
	 * exists.
	 */
	public ICVSRepositoryLocation createRepository(Properties configuration) throws CVSException {
		// Create a new repository location
		CVSRepositoryLocation location = CVSRepositoryLocation.fromProperties(configuration);
		
		// Check the cache for an equivalent instance and if there is one, throw an exception
		CVSRepositoryLocation existingLocation = (CVSRepositoryLocation)repositories.get(location.getLocation());
		if (existingLocation != null) {
			throw new CVSException(new CVSStatus(CVSStatus.ERROR, Policy.bind("CVSProvider.alreadyExists"))); //$NON-NLS-1$
		}

		return location;
	}

	/**
	 * Add the repository to the receiver's list of known repositories. Doing this will enable
	 * password caching accross platform invokations.
	 */
	public void addRepository(ICVSRepositoryLocation repository) throws CVSException {
		// Check the cache for an equivalent instance and if there is one, just update the cache
		CVSRepositoryLocation existingLocation = (CVSRepositoryLocation)repositories.get(repository.getLocation());
		if (existingLocation != null) {
			((CVSRepositoryLocation)repository).updateCache();
		} else {
			// Cache the password and register the repository location
			addToRepositoriesCache(repository);
			((CVSRepositoryLocation)repository).updateCache();
		}
		saveState();
	}
	
	/**
	 * Dispose of the repository location
	 * 
	 * Removes any cached information about the repository such as a remembered password.
	 */
	public void disposeRepository(ICVSRepositoryLocation repository) throws CVSException {
		((CVSRepositoryLocation)repository).dispose();
		removeFromRepositoriesCache(repository);
	}

	/**
	 * Answer whether the provided repository location is known by the provider or not.
	 * The location string corresponds to the Strin returned by ICVSRepositoryLocation#getLocation()
	 */
	public boolean isKnownRepository(String location) {
		return repositories.get(location) != null;
	}
	
	/** 
	 * Return a list of the know repository locations
	 */
	public ICVSRepositoryLocation[] getKnownRepositories() {
		return (ICVSRepositoryLocation[])repositories.values().toArray(new ICVSRepositoryLocation[repositories.size()]);
	}
		
	/**
	 * Get the repository instance which matches the given String. The format of the String is
	 * the same as that returned by ICVSRepositoryLocation#getLocation().
	 * The format is:
	 * 
	 *   connection:user[:password]@host[#port]:root
	 * 
	 * where [] indicates optional and the identier meanings are:
	 * 
	 * 	 connection The connection method to be used
	 *   user The username for the connection
	 *   password The password used for the connection (optional)
	 *   host The host where the repository resides
	 *   port The port to connect to (optional)
	 *   root The server directory where the repository is located
	 * 
	 * It is expected that the instance requested by using this method exists.
	 * If the repository location does not exist, it will be automatically created
	 * and cached with the provider.
	 * 
	 * WARNING: Providing the password as part of the String will result in the password being part
	 * of the location permanently. This means that it cannot be modified by the authenticator. 
	 */
	public ICVSRepositoryLocation getRepository(String location) throws CVSException {
		ICVSRepositoryLocation repository = (ICVSRepositoryLocation)repositories.get(location);
		if (repository == null) {
			repository = CVSRepositoryLocation.fromString(location);
			addToRepositoriesCache(repository);
		}
		return repository;
	}

	private void loadState() {
		try {
			IPath pluginStateLocation = CVSProviderPlugin.getPlugin().getStateLocation().append(REPOSITORIES_STATE_FILE);
			File file = pluginStateLocation.toFile();
			if (file.exists()) {
				try {
					DataInputStream dis = new DataInputStream(new FileInputStream(file));
					readState(dis);
					dis.close();
				} catch (IOException e) {
					throw new TeamException(new Status(Status.ERROR, CVSProviderPlugin.ID, TeamException.UNABLE, Policy.bind("CVSProvider.ioException"), e));  //$NON-NLS-1$
				}
			}  else {
				// If the file did not exist, then prime the list of repositories with
				// the providers with which the projects in the workspace are shared.
				IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
				for (int i = 0; i < projects.length; i++) {
					RepositoryProvider provider = RepositoryProvider.getProvider(projects[i], CVSProviderPlugin.getTypeId());
					if (provider!=null) {
						ICVSFolder folder = (ICVSFolder)CVSWorkspaceRoot.getCVSResourceFor(projects[i]);
						FolderSyncInfo info = folder.getFolderSyncInfo();
						if (info != null) {
							ICVSRepositoryLocation result = getRepository(info.getRoot());
						}
					}
				}
				saveState();
			}
		} catch (TeamException e) {
			Util.logError(Policy.bind("CVSProvider.errorLoading"), e);//$NON-NLS-1$
		}
	}
	private void saveState() {
		try {
			IPath pluginStateLocation = CVSProviderPlugin.getPlugin().getStateLocation();
			File tempFile = pluginStateLocation.append(REPOSITORIES_STATE_FILE + ".tmp").toFile(); //$NON-NLS-1$
			File stateFile = pluginStateLocation.append(REPOSITORIES_STATE_FILE).toFile();
			try {
				DataOutputStream dos = new DataOutputStream(new FileOutputStream(tempFile));
				writeState(dos);
				dos.close();
				if (stateFile.exists()) {
					stateFile.delete();
				}
				boolean renamed = tempFile.renameTo(stateFile);
				if (!renamed) {
					throw new TeamException(new Status(Status.ERROR, CVSProviderPlugin.ID, TeamException.UNABLE, Policy.bind("CVSProvider.rename", tempFile.getAbsolutePath()), null)); //$NON-NLS-1$
				}
			} catch (IOException e) {
				throw new TeamException(new Status(Status.ERROR, CVSProviderPlugin.ID, TeamException.UNABLE, Policy.bind("CVSProvider.save",stateFile.getAbsolutePath()), e)); //$NON-NLS-1$
			}
		} catch (TeamException e) {
			Util.logError(Policy.bind("CVSProvider.errorSaving"), e);//$NON-NLS-1$
		}
	}
	
	private void readState(DataInputStream dis) throws IOException, CVSException {
		int count = dis.readInt();
		if (count >= 0) {
			// this is the version 1 format of the state file
			for (int i = 0; i < count; i++) {
				getRepository(dis.readUTF());
			}
		} else if (count == REPOSITORIES_STATE_FILE_VERSION_2) {
			count = dis.readInt();
			for (int i = 0; i < count; i++) {
				ICVSRepositoryLocation root = getRepository(dis.readUTF());
				String programName = dis.readUTF();
				if (!programName.equals(CVSRepositoryLocation.DEFAULT_REMOTE_CVS_PROGRAM_NAME)) {
					((CVSRepositoryLocation)root).setRemoteCVSProgramName(programName);
				}
			}
		} else {
			Util.logError(Policy.bind("CVSProviderPlugin.unknownStateFileVersion", new Integer(count).toString()), null); //$NON-NLS-1$
		}
	}
	
	private void writeState(DataOutputStream dos) throws IOException {
		// Write the repositories
		dos.writeInt(REPOSITORIES_STATE_FILE_VERSION_2);
		// Write out the repos
		Collection repos = repositories.values();
		dos.writeInt(repos.size());
		Iterator it = repos.iterator();
		while (it.hasNext()) {
			CVSRepositoryLocation root = (CVSRepositoryLocation)it.next();
			dos.writeUTF(root.getLocation());
			dos.writeUTF(root.getRemoteCVSProgramName());
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
	 * Set the program name of the given repository location.
	 * The program name is the expected prefix on server text messages.
	 * Since we extract information out of these messages, we need to
	 * know what prefix to expect. The default is "cvs".
	 */
	public void setCVSProgramName(ICVSRepositoryLocation location, String programName) {
		((CVSRepositoryLocation)location).setRemoteCVSProgramName(programName);
		saveState();
	}
	
	/**
	 * Method getResetTimestampOfFalseChange.
	 * @return boolean
	 */
	public boolean getResetTimestampOfFalseChange() {
		return true;
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

}

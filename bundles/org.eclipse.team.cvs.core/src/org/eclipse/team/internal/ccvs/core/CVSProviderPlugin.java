/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.core;
 
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Command.KSubstOption;
import org.eclipse.team.internal.ccvs.core.client.Command.QuietOption;
import org.eclipse.team.internal.ccvs.core.client.listeners.IConsoleListener;
import org.eclipse.team.internal.ccvs.core.util.AddDeleteMoveListener;
import org.eclipse.team.internal.ccvs.core.util.ProjectDescriptionManager;
import org.eclipse.team.internal.ccvs.core.util.SyncFileChangeListener;
import org.eclipse.team.internal.ccvs.core.util.Util;

public class CVSProviderPlugin extends Plugin {

	// external command to run for ext connection method
	public static final String DEFAULT_CVS_RSH = "ssh"; //$NON-NLS-1$
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
	public static final KSubstOption DEFAULT_TEXT_KSUBST_OPTION = Command.KSUBST_TEXT;

	// cvs plugin extension points and ids
	public static final String ID = "org.eclipse.team.cvs.core"; //$NON-NLS-1$
	public static final String PT_AUTHENTICATOR = "authenticator"; //$NON-NLS-1$
	public static final String PT_CONNECTIONMETHODS = "connectionmethods"; //$NON-NLS-1$
	
	// Directory to cache file contents
	private static final String CACHE_DIRECTORY = ".cache"; //$NON-NLS-1$
		
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
	private String cvsRshCommand = DEFAULT_CVS_RSH;
	private String cvsServer = DEFAULT_CVS_SERVER;
	private IConsoleListener consoleListener;
	
	private static CVSProviderPlugin instance;
	
	// CVS specific resource delta listeners
	private IResourceChangeListener projectDescriptionListener;
	private IResourceChangeListener metaFileSyncListener;
	private AddDeleteMoveListener addDeleteMoveListener;

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
		instance = this;
	}
	
	/**
	 * Convenience method for logging CVSExceptiuons to the plugin log
	 */
	public static void log(TeamException e) {
		// For now, we'll log the status. However we should do more
		instance.getLog().log(e.getStatus());
	}
	public static void log(IStatus status) {
		// For now, we'll log the status. However we should do more
		instance.getLog().log(status);
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
	 * Get the ICVSProvider
	 */
	public static ICVSProvider getProvider() {
		return CVSProvider.getInstance();
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

		// Start the synchronizer first as the startup of CVSProvider may use it.
		CVSProvider.startup();
		
		// Initialize CVS change listeners. Note tha the report type is important.
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		projectDescriptionListener = new ProjectDescriptionManager();
		metaFileSyncListener = new SyncFileChangeListener();
		addDeleteMoveListener = new AddDeleteMoveListener();
		workspace.addResourceChangeListener(projectDescriptionListener, IResourceChangeEvent.PRE_AUTO_BUILD);
		workspace.addResourceChangeListener(metaFileSyncListener, IResourceChangeEvent.PRE_AUTO_BUILD);
		workspace.addResourceChangeListener(addDeleteMoveListener, IResourceChangeEvent.POST_AUTO_BUILD);
		CVSProviderPlugin.getPlugin().addResourceStateChangeListener(addDeleteMoveListener);
		
		createCacheDirectory();
	}
	
	/**
	 * @see Plugin#shutdown()
	 */
	public void shutdown() throws CoreException {
		super.shutdown();
		CVSProvider.shutdown();
		
		// remove listeners
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(projectDescriptionListener);
		workspace.removeResourceChangeListener(metaFileSyncListener);
		workspace.removeResourceChangeListener(addDeleteMoveListener);
		
		deleteCacheDirectory();
	}
		
	/*
	 * Add a resource change listener to the workspace in order to respond to 
	 * resource deletions and moves and to ensure or project desription file is up to date.
	 */
	private void initializeChangeListener() {
		
		// Build a change listener for changes to thr project meta-information
		IResourceChangeListener projectChangeListener = new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				try {
					IResourceDelta root = event.getDelta();
					IResourceDelta[] projectDeltas = root.getAffectedChildren(IResourceDelta.CHANGED);
					for (int i = 0; i < projectDeltas.length; i++) {
						IResourceDelta delta = projectDeltas[i];
						IResource resource = delta.getResource();
						if (resource.getType() == IResource.PROJECT) {
							IProject project = (IProject)resource;
							// Get the team provider for the project and
							RepositoryProvider provider = RepositoryProvider.getProvider(project, getTypeId());
							if(provider==null) continue;
							/* Check if the project description changed. */
							if ((delta.getFlags() & IResourceDelta.DESCRIPTION) != 0) {
								/* The project description changed. Write the file. */
								ProjectDescriptionManager.writeProjectDescriptionIfNecessary((CVSTeamProvider)provider, project, Policy.monitorFor(null));
							}
	
							/* Check if the .vcm_meta file for the project is in the delta. */
							IResourceDelta[] children = delta.getAffectedChildren(IResourceDelta.REMOVED);
							for (int j = 0; j < children.length; j++) {
								IResourceDelta childDelta = children[j];
								IResource childResource = childDelta.getResource();
								if (ProjectDescriptionManager.isProjectDescription(childResource)) {
									ProjectDescriptionManager.writeProjectDescriptionIfNecessary((CVSTeamProvider)provider, project, Policy.monitorFor(null));
								}
							}
						}
					}
				} catch (CVSException ex) {
					Util.logError(Policy.bind("CVSProviderPlugin.cannotUpdateDescription"), ex);  //$NON-NLS-1$
				}
			}
		};
		ResourcesPlugin.getWorkspace().addResourceChangeListener(projectChangeListener, IResourceChangeEvent.POST_AUTO_BUILD);
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
	
	/*
	 * @see ITeamManager#broadcastResourceStateChanges(IResource[])
	 */
	public static void broadcastResourceStateChanges(final IResource[] resources) {
		for(Iterator it=listeners.iterator(); it.hasNext();) {
			final IResourceStateChangeListener listener = (IResourceStateChangeListener)it.next();
			ISafeRunnable code = new ISafeRunnable() {
				public void run() throws Exception {
					listener.resourceStateChanged(resources);
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
	
	public File getCacheFileFor(String path) throws IOException {
		return new File(getStateLocation().append(CACHE_DIRECTORY).toFile(), path);
	}
}


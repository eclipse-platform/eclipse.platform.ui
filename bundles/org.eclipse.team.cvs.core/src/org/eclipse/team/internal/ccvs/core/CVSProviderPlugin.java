package org.eclipse.team.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.team.core.ITeamProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProvider;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.client.Command.QuietOption;
import org.eclipse.team.internal.ccvs.core.resources.EclipseSynchronizer;
import org.eclipse.team.internal.ccvs.core.util.ProjectDescriptionManager;
import org.eclipse.team.internal.ccvs.core.util.ResourceDeltaSyncHandler;
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

	// cvs plugin extension points and ids
	public static final String ID = "org.eclipse.team.cvs.core"; //$NON-NLS-1$
	public static final String PT_AUTHENTICATOR = "authenticator"; //$NON-NLS-1$
	public static final String PT_CONNECTIONMETHODS = "connectionmethods"; //$NON-NLS-1$
	
	private QuietOption quietness;
	private int communicationsTimeout = DEFAULT_TIMEOUT;
	private boolean pruneEmptyDirectories = DEFAULT_PRUNE;
	private boolean fetchAbsentDirectories = DEFAULT_FETCH;
	private String cvsRshCommand = DEFAULT_CVS_RSH;
	private String cvsServer = DEFAULT_CVS_SERVER;
	
	private static CVSProviderPlugin instance;
	
	/**
	 * The identifier for the CVS nature
	 * (value <code>"org.eclipse.team.cvs.core.nature"</code>).
	 * The presence of this nature on a project indicates that it is 
	 * CVS-capable.
	 *
	 * @see org.eclipse.core.resources.IProject#hasNature
	 */
	public static final String NATURE_ID = ID + ".cvsnature"; //$NON-NLS-1$

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
	 * @see Plugin#startup()
	 */
	public void startup() throws CoreException {
		super.startup();
		Policy.localize("org.eclipse.team.internal.ccvs.core.messages"); //$NON-NLS-1$

		// Start the synchronizer first as the startup of CVSProvider may use it.
		EclipseSynchronizer.startup();
		CVSProvider.startup();
		ProjectDescriptionManager.initializeChangeListener();
		ResourceDeltaSyncHandler.startup();
	}
	
	/**
	 * @see Plugin#shutdown()
	 */
	public void shutdown() throws CoreException {
		super.shutdown();
		CVSProvider.shutdown();
		EclipseSynchronizer.shutdown();
		ResourceDeltaSyncHandler.shutdown();
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
							ITeamProvider provider = TeamPlugin.getManager().getProvider(project);
							if (!(provider instanceof CVSTeamProvider)) continue;
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
	 * Sets the etchAbsentDirectories.
	 * @param etchAbsentDirectories The etchAbsentDirectories to set
	 */
	public void setFetchAbsentDirectories(boolean fetchAbsentDirectories) {
		this.fetchAbsentDirectories = fetchAbsentDirectories;
	}

}


package org.eclipse.team.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.internal.FileTypeRegistry;
import org.eclipse.team.core.internal.Policy;
import org.eclipse.team.core.internal.TeamManager;

/**
 * <code>TeamPlugin</code> is the plug-in runtime class for the Team 
 * resource management plugin.
 * <p>
 * This plugin provides a lightweight registration and lookup service for
 * associating projects with providers. The registration mechanism is
 * based on the platform's project natures. Using projects natures allows
 * manipulating a project in a nature-specific way, for example UI 
 * contributions can be made conditional based on the nature of a project.
 * </p>
 * 
 * @see ITeamNature
 * @see ITeamProvider
 * @see ITeamManager
 */
final public class TeamPlugin extends Plugin {

	// The id of the core team plug-in
	public static final String ID = "org.eclipse.team.core";

	// The id of the providers extension point
	public static final String PROVIDER_EXTENSION = "providers";
	
	// The id of the file types extension point
	public static final String FILE_TYPES_EXTENSION = "fileTypes";

	// The team manager - manages relationships between projects and providers
	private static TeamManager manager;

	// The file type registry
	private static FileTypeRegistry registry;
	
	// The one and only plug-in instance
	private static TeamPlugin plugin;	

	/** 
	 * Constructs a plug-in runtime class for the given plug-in descriptor.
	 */
	public TeamPlugin(IPluginDescriptor pluginDescriptor) {
		super(pluginDescriptor);
		plugin = this;
	}
	
	/**
	 * @see Plugin#startup()
	 */
	public void startup() throws CoreException {
		try {
			Policy.localize("org.eclipse.team.core.internal.messages");
			
			manager = new TeamManager();
			manager.startup();
			
			registry = new FileTypeRegistry();
			registry.startup();
		} catch(TeamException e) {
			throw new CoreException(e.getStatus());
		}
	}
	
	/**
	 * @see Plugin#shutdown()
	 */
	public void shutdown() {
		registry.shutdown();
	}
	
	/**
	 * Returns the Team plug-in.
	 *
	 * @return the single instance of this plug-in runtime class
	 */
	public static TeamPlugin getPlugin() {
		return plugin;
	}
	
	/**
	 * Returns the team manager.
	 */
	public static ITeamManager getManager() {
		return manager;
	}
	
	/**
	 * Returns the file type registry.
	 */
	public static IFileTypeRegistry getFileTypeRegistry() {
		return registry;
	}
	
	/**
	 * Returns the plug-in's log
	 */
	public static void log(int severity, String message, Throwable e) {
		plugin.getLog().log(new Status(severity, ID, 0, message, e));
	}
}
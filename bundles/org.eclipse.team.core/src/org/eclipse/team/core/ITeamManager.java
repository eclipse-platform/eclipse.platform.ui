package org.eclipse.team.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;

/**
 * The <code>ITeamManager</code> is the basis for team provider management
 * in the platform.  There is only one team manager per running platform.  All 
 * registered providers exist in the context of this manager. The manager provides 
 * the following services to clients:
 * <p>
 * <ul>
 * 		<li>find the <code>ITeamProvider</code> associated with
 * 		a project.</li>
 * 
 * 		<li>associate one provider nature at a time to a project.</li>
 * 
 * 		<li>remove a project from being associated with a provider's nature.</li>
 * 
 * @see ITeamProvider
 */
public interface ITeamManager {

	/**
	 * Called to associate a project with a given provider. The Team plug-in will remember 
	 * the association between workbench sessions.
	 * <p>
	 * This method allows headless application (e.g. tests, scripts) to create specific providers 
	 * programatically. Refer to the provider's documentation for the properties that are required 
	 * for initializing a provider programatically.</p>
	 * <p>
	 * Example usage:
	 * <pre>
	 * Properties properties = new Properties();
	 * properties.set("location", "http://www.share.com/~team");
	 * properties.set("httpUser", "team");
	 * properties.set("httpPass", "teampass");
	 * ...
	 * plugin.setProvider(project, "org.eclipse.team.providers.someteam_nature", properties, monitor);
	 * </pre></p>
	 * <p>
	 * Warning: A client using this method is hard coding provider specific details into 
	 * their implementations.</p>
	 * 
	 * @param project to associate with a specific provider type identified by the natureId.
	 * @param natureId that identifies the provider to associate with the project.
	 * @param configuration required to initialize the provider. The contents of the configuration
	 * is provider specific. Can be <code>null</code> if the provider is configured using another
	 * mechanism.
	 * 
	 * @exception TeamException if the project cannot be associated with the provider. 
	 * Possible reasons are:
	 * <ul>
	 * 	<li>provider is already associated with a provider. A client must call 
	 * 		  <code>removeProvider</code> before associating a project with 
	 * 		  another provider.</li>
	 * 	<li>provider could not be configured.</li>
	 * </ul></p>
	 */
	public void setProvider(IProject project, String natureId, Properties configuration, IProgressMonitor progress) throws TeamException;
		
	/**
	 * Answers the provider associated with this resource's project. Returns <code>null</code> 
	 * if the project is not associated with a provider.
	 * 
	 * @param resource for which to return its associated provider
	 * 
	 * @return the team provider instance associated with the resource's project, or <code>null</code>
	 * if the resource's project is not associated with a provider.
	 */
	public ITeamProvider getProvider(IResource resource);	
	
	/**
	 * Un-associate this project with its provider. If the project is not associated with
	 * a provider this method has no effect.
	 * 
	 * @param project to remote the associate to its provider.
	 * 
	 * @exception TeamException if the provider cannot be removed from the project. Possible
	 * reasons are:
	 * <ul>
	 * 	<li>error removing the nature id</li>
	 * </ul>
	 */
	public void removeProvider(IProject project, IProgressMonitor progress) throws TeamException;
	
	/** 
	 * Adds the given listener for provider state change events to this workspace.
 	 * Has no effect if an identical listener is already registered for these events.
	 * <p>
	 * Once registered, a listener starts receiving notification of changes to resources states
	 * (e.g. checked in/checked out...) in the workspace the listener continues to receive 
	 * notifications until it is replaced or removed.</p>
	 * <p>
	 * 
	 * @param listener the listener
	 * @see IResourceStateChangeListener
	 * @see #removeResourceStateChangeListener
	 */
	public void addResourceStateChangeListener(IResourceStateChangeListener listener);
	
	/** 
	 * Removes the given resource state change listener from this manager.
	 * Has no effect if an identical listener is not registered.
	 *
	 * @param listener the listener
	 * @see IResourceStateChangeListener
	 * @see #addResourceStateChangeListener
	 */
	public void removeResourceStateChangeListener(IResourceStateChangeListener listener);
	
	/**
	 * Notify listeners about state changes to the given resources.
	 * 
 	 * [Note: The changed state event is purposely vague. For now it is only
	 * a hint to listeners that they should query the provider to determine the
	 * resources new team state.]
	 * 
	 * @param resources that have changed state.
	 */
	public void broadcastResourceStateChanges(IResource[] resources);
	
	/**
	 * Returns the list of global ignore patterns.
	 * 
	 * @return ignore patterns
	 */
	public IIgnoreInfo[] getGlobalIgnore();	
	
	/**
	 * Sets the list of ignore patterns. These are persisted between workspace sessions.
	 * 
	 * @param patterns an array of file name patterns (e.g. *.exe)
	 * @param enabled an array of pattern enablements
	 */
	public void setGlobalIgnore(String[] patterns, boolean[] enabled);
}
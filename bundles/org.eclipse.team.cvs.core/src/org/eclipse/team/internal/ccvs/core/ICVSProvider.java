package org.eclipse.team.ccvs.core;

import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
 
/**
 * The ICVSProvider interface provides access to CVS operations that create repository locations,
 * support retrieval of repository information and import and checkout CVS modules
 */
public interface ICVSProvider {

	/**
	 * Checkout a CVS module.
	 * 
	 * The provided project represents the target project. Any existing contents
	 * may or may not get overwritten. If project is <code>null</code> then a project
	 * will be created based on the provided sourceModule. If soureModule is null, 
	 * then the project name will be used as the module to
	 * check out. If both are absent, an exception is thrown.
	 * 
	 * After the successful completion of this method, the project will exist
	 * and be open.
	 */
	public void checkout(ICVSRepositoryLocation repository, IProject project, String sourceModule, String tag, IProgressMonitor monitor) throws TeamException;

	/**
	 * Checkout a CVS module.
	 * 
	 * The provided project represents the target project. Any existing contents
	 * may or may not get overwritten. If project is <code>null</code> then a project
	 * will be created based on the provided "module" property. If there is no
	 * "module" property, then the project name will be used as the module to
	 * check out. If both are absent, an exception is thrown.
	 * 
	 * After the successful completion of this method, the project will exist
	 * and be open.
	 * 
	 * The supported properties are:
	 * 	 connection The connection method to be used
	 *   user The username for the connection
	 *   password The password used for the connection (optional)
	 *   host The host where the repository resides
	 *   port The port to connect to (optional)
	 *   root The server directory where the repository is located
	 *   module The name of the module to be checked out (optional)
	 *   tag The tag to be used in the checkout request (optional)
	 */
	public void checkout(IProject project, Properties configuration, IProgressMonitor monitor) throws TeamException;
		
	/**
	 * Checkout the remote resources into the local workspace. Each resource will 
	 * be checked out into the corresponding project. If teh corresponding project is
	 * null or if projects is null, the name of the remote resource is used as the name of the project.
	 */
	public void checkout(IRemoteResource[] resources, IProject[] projects, IProgressMonitor monitor) throws TeamException;

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
	 * The created instance will be cached with the provider as a result of the
	 * invokation of this method. When the client is done with the instance, disposeRepository
	 * should be called
	 */
	public ICVSRepositoryLocation createRepository(Properties configuration) throws CVSException;

	/**
	 * Dispose of the repository location
	 * 
	 * Removes any cached information about the repository such as a remembered password.
	 */
	public void disposeRepository(ICVSRepositoryLocation repository) throws CVSException;
	
	/** Return a list of the know repository locations
	 */
	public ICVSRepositoryLocation[] getKnownRepositories();
		
	/**
	 * Get the names of the registered connection methods.
	 */
	public String[] getSupportedConnectionMethods();
	
	/**
	 * Import a project into a CVS repository and then check out a local copy.
	 * 
	 * Consideration: What if the project already exists?
	 * 
	 * The supported properties are:
	 * 	 connection The connection method to be used
	 *   user The username for the connection
	 *   password The password used for the connection (optional)
	 *   host The host where the repository resides
	 *   port The port to connect to (optional)
	 *   root The server directory where the repository is located
	 *   message The message to be attached (optional)
	 *   vendor The vendor tag (optional)
	 *   tag The version tag (optional)
	 */
	public void importAndCheckout(IProject project, Properties configuration, IProgressMonitor monitor) throws TeamException;
}


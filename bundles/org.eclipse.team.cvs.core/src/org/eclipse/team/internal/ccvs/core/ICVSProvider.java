package org.eclipse.team.internal.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.io.PrintStream;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;

/**
 * The ICVSProvider interface provides access to CVS operations that create repository locations,
 * support retrieval of repository information and import and checkout CVS modules
 */
public interface ICVSProvider {

	/**
	 * Register to receive notification of repository creation and disposal
	 */
	public void addRepositoryListener(ICVSListener listener);

	/**
	 * De-register a listener
	 */
	public void removeRepositoryListener(ICVSListener listener);
	
	/**
	 * Checkout a CVS module.
	 * 
	 * The provided project represents the target project. Any existing contents
	 * may or may not get overwritten. If project is <code>null</code> then a project
	 * will be created based on the provided sourceModule. If soureModule is null, 
	 * then the project name will be used as the module to
	 * check out. If both are absent, an exception is thrown.
	 * 
	 * Resources existing in the local file system at the target project location but now 
	 * known to the workbench will be overwritten.
	 * 
	 * After the successful completion of this method, the project will exist
	 * and be open.
	 */
	public void checkout(ICVSRepositoryLocation repository, IProject project, String sourceModule, CVSTag tag, IProgressMonitor monitor) throws TeamException;

	/**
	 * Checkout the remote resources into the local workspace. Each resource will 
	 * be checked out into the corresponding project. If the corresponding project is
	 * null or if projects is null, the name of the remote resource is used as the name of the project.
	 * 
	 * Resources existing in the local file system at the target project location but now 
	 * known to the workbench will be overwritten.
	 */
	public void checkout(ICVSRemoteFolder[] resources, IProject[] projects, IProgressMonitor monitor) throws TeamException;

	/**
	 * Create a remote module in the CVS repository and link the project directory to this remote module.
	 * The contents of the project are not imported.
	 * 
	 * Consideration: What if the project already exists?
	 */
	public void createModule(ICVSRepositoryLocation location, IProject project, String moduleName, IProgressMonitor monitor) throws TeamException;

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
	public ICVSRepositoryLocation createRepository(Properties configuration) throws CVSException;
	
	/**
	 * Add the repository to the receiver's list of known repositories. Doing this will enable
	 * password caching accross platform invokations.
	 */
	public void addRepository(ICVSRepositoryLocation repository) throws CVSException;
	
	/**
	 * Dispose of the repository location
	 * 
	 * Removes any cached information about the repository such as a remembered password.
	 */
	public void disposeRepository(ICVSRepositoryLocation repository) throws CVSException;
	
	/**
	 * Answer whether the provided repository location is known by the provider or not.
	 * The location string corresponds to the Strin returned by ICVSRepositoryLocation#getLocation()
	 */
	public boolean isKnownRepository(String location);
	
	/**
	 * Answer the list of directories that a checkout of the given resources would expand to.
	 * In other words, the returned strings represent the root paths that the given resources would 
	 * be loaded into.
	 */
	public String[] getExpansions(ICVSRemoteFolder[] resources, IProgressMonitor monitor) throws CVSException;
	
	/** 
	 * Return a list of the know repository locations
	 */
	public ICVSRepositoryLocation[] getKnownRepositories();
	
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
	public ICVSRepositoryLocation getRepository(String location) throws CVSException;
	
	/**
	 * Set the sharing for a project to enable it to be used with the CVSTeamProvider.
	 * This method only sets the folder sync info for the project folder and the info
	 * is only set to the provided parameters if there is no sync info already.
	 */
	public void setSharing(IProject project, FolderSyncInfo info, IProgressMonitor monitor) throws TeamException;
	
	/**
	 * Get the names of the registered connection methods.
	 */
	public String[] getSupportedConnectionMethods();
}


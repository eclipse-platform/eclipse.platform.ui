package org.eclipse.team.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.io.PrintStream;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
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
	 * After the successful completion of this method, the project will exist
	 * and be open.
	 */
	public void checkout(ICVSRepositoryLocation repository, IProject project, String sourceModule, CVSTag tag, IProgressMonitor monitor) throws TeamException;

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
	public void checkout(ICVSRemoteFolder[] resources, IProject[] projects, IProgressMonitor monitor) throws TeamException;

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
	 * should be called.
	 */
	public ICVSRepositoryLocation createRepository(Properties configuration) throws CVSException;
	
	/**
	 * Dispose of the repository location
	 * 
	 * Removes any cached information about the repository such as a remembered password.
	 */
	public void disposeRepository(ICVSRepositoryLocation repository) throws CVSException;
	
	/** 
	 * Return a list of the know repository locations
	 */
	public ICVSRepositoryLocation[] getKnownRepositories();
	
	/**
	 * Get the repository location that matches the given properties.
	 * The supported properties are:
	 * 
	 *   connection The connection method to be used
	 *   user The username for the connection
	 *   password The password used for the connection (optional)
	 *   host The host where the repository resides
	 *   port The port to connect to (optional)
	 *   root The server directory where the repository is located
	 * 
	 * If no known repositories mathc the given properties, null is returned.
	 */
	public ICVSRepositoryLocation getRepository(Properties configuration) throws CVSException;
	
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
	public void setSharing(IProject project, ICVSRepositoryLocation location, String remotePath, CVSTag tag, IProgressMonitor monitor) throws TeamException;
	
	/**
	 * Get the names of the registered connection methods.
	 */
	public String[] getSupportedConnectionMethods();
		
	/**
	 * Get the stream to which command message and error output is sent
	 */
	public PrintStream getPrintStream();
	
	/**
	 * Set the print stream to which command message and error output is sent
	 */
	public void setPrintStream(PrintStream out);
}


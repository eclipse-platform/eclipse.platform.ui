package org.eclipse.team.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.resources.IFileModificationValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.*;
import org.eclipse.team.core.internal.Policy;

/**
 * A concrete subclass of <code>RepositoryProvider</code> is created for each
 * project that is shared via a repository. 
 * 
 * @see RepositoryProviderType
 *
 * @since 2.0
 */
public abstract class RepositoryProvider implements IProjectNature {
	
	/**
	 * Default constructor required for the resources plugin to instantiate this class from a
	 * extension definition.
	 */
	public RepositoryProvider() {
	}
	
	/**
	 * Returns a <code>RepositoryProviderType</code> that describes the type of this provider.
	 * 
	 * @return the <code>RepositoryProviderType</code> of this provider
	 * @see RepositoryProviderType
	 */
	abstract public RepositoryProviderType getProviderType();	
	
	/**
	 * Configures the nature for the given project. This method is called after <code>setProject</code>
	 * and before the nature is added to the project. If an exception is generated during configuration
	 * of the project the nature will not be assigned to the project.
	 * 
	 * @throws CoreException if the configuration fails. 
	 */
	abstract public void configureProject() throws CoreException;
	
	/**
	 * Configures the nature for the given project. This method is called after <code>setProject</code> 
	 * and before the nature is added to the project.
	 * <p>
	 * The default behavior for <code>RepositoryProvider</code> subclasses is to fail the configuration
	 * if a provider is already associated with the given project. Subclasses cannot override this method
	 * but must instead override <code>configureProject</code>.
	 * 
	 * @throws CoreException if this method fails. If the configuration fails the nature will not be added 
	 * to the project.
	 * @see IProjectNature#configure
	 */
	final public void configure() throws CoreException {
		RepositoryProvider provider = RepositoryProviderType.getProvider(getProject());
		// Core Bug 11395
		// When configure is called the nature has already been assigned to the project. This check will always
		// fail. Also, if configure fails the nature is still added to the project.
		//if(provider!=null) {
		//	throw new CoreException(new Status(IStatus.ERROR, TeamPlugin.ID, 0, "A provider is already associated with this project: " + provider, null));
		//}
		// Alternate slower check
		RepositoryProviderType[] types = RepositoryProviderType.getAllProviderTypes();
		int count = 0;
		for (int i = 0; i < types.length; i++) {
			if(getProject().getNature(types[i].getID())!=null) {
				count++;
			}
		}
		if(count>1) {
			try {
				TeamPlugin.removeNatureFromProject(getProject(), getProviderType().getID(), null);
			} catch(TeamException e) {
				throw new CoreException(new Status(IStatus.ERROR, TeamPlugin.ID, 0, Policy.bind("RepositoryProvider_Error_removing_nature_from_project___1") + provider, null)); //$NON-NLS-1$
			}
			throw new CoreException(new Status(IStatus.ERROR, TeamPlugin.ID, 0, Policy.bind("RepositoryProvider_Too_many_providers_associated_with_project___2") + provider, null)); //$NON-NLS-1$
		}			
		configureProject();
	}

	/**
	 * Returns <code>true</code> if this provider is of the given type id and <code>false</code>
	 * otherwise.
	 */
	public boolean isOfType(String id) {
		return getProviderType().getID().equals(id);
	}

	/**
	 * Returns an object which implements a set of provider neutral operations for this 
	 * provider. Answers <code>null</code> if the provider does not wish to support these 
	 * operations.
	 * <p>
	 * The <code>StandardOperations</code> class exposes a basic repository model that
	 * providers may implement to allow third-party plugins to perform repository operations
	 * programmatically. For example, a code generation tool may want to get source
	 * files before generating the code, and check-in the results.  If a provider plugin does
	 * not adhere to the <i>semantics</i> of the <code>StandardOperations</code> class
	 * as described, the provider will not be useable programmatically by other third-party 
	 * plugins.</p>
	 * 
	 * @return the repository operations or <code>null</code> if the provider does not
	 * support provider neutral operations.
	 */
	public StandardOperations getStandardOperations() {
		return null;
	}

	/**
	 * Returns an <code>IFileModificationValidator</code> for pre-checking operations 
 	 * that modify the contents of files.
 	 * Returns <code>null</code> if the provider does not wish to participate in
 	 * file modification validation.
 	 * 
	 * @see org.eclipse.core.resources.IFileModificationValidator
	 */
	
	public IFileModificationValidator getFileModificationValidator() {
		return null;
	}
	
	/**
	 * Returns a brief description of this provider. The exact details of the
	 * representation are unspecified and subject to change, but the following
	 * may be regarded as typical:
	 * 
	 * "SampleProject:org.eclipse.team.cvs.provider"
	 * 
	 * @return a string description of this provider
	 */
	public String toString() {
		return getProject().getName() + ":" + getProviderType(); //$NON-NLS-1$
	}
}

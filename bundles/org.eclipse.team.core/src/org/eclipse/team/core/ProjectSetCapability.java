package org.eclipse.team.core;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Provisional
 */

public abstract class ProjectSetCapability {
	/**
	 * Notify the provider that a project set has been created at path.
	 * Only providers identified as having projects in the project set will be
	 * notified.  The project set may or may not be created in a workspace
	 * project (thus may not be a resource).
	 * 
	 * @param File the project set file that was created
	 */	
	public void projectSetCreated(File file, Object context, IProgressMonitor monitor) {
		//default is to do nothing
	}
		
	/**
	 * Returns true if when importing a project set the projects can be created
	 * at a specified file system location different than the default.
	 * 
	 * NOTE: If this method is overriden to return true, then the provider
	 * <b>must</b> also override addToWorkspace(String[], String, IPath, Object,
	 * IProgressMonitor);
	 * 
	 * @return boolean
	 */
	public boolean supportsProjectSetImportRelocation() {
		return false;
	}
	
	/**
	 * This method will only be called if
	 * RepositoryProviderType#supportsProjectSetImportRelocation returns true.
	 * 
	 * For every String in referenceStrings,
	 * create in the workspace a corresponding IProject.  Return an Array of the
	 * resulting IProjects. Result is unspecified in the case where an IProject
	 * of that name already exists. In the case of failure, a TeamException must
	 * be thrown. The opaque strings in referenceStrings are guaranteed to have
	 * been previously produced by IProjectSetSerializer.asReference().
	 * @see IProjectSetSerializer#asReference(IProject[] providerProjects, Object context, IProgressMonitor monitor)
	 * 
	 * @param referenceStrings  an array of referene strings uniquely identifying the projects
	 * @param filename  the name of the file that the references were read from. This is included
	 *   in case the provider needs to deduce relative paths
	 * @param root the root file system path under which the projects should be
	 * created. 
	 * @param context  a UI context object. This object will either be a 
	 *                 com.ibm.swt.widgets.Shell or it will be null.
	 * @param monitor  a progress monitor
	 * @return IProject[]  an array of projects that were created
	 * @throws TeamException
	 */
	public abstract IProject[] addToWorkspace(String[] referenceStrings, String filename, IPath root, Object context, IProgressMonitor monitor) throws TeamException;
}

package org.eclipse.team.core.target;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.team.core.TeamException;

public abstract class TargetProvider {

	private IProject project;
	
	/**
	 * Answers a constant qualified name that uniquely identifies this provider, used to
	 * distinguish this provider from others in the workspace.
	 */
	abstract public QualifiedName getIdentifier();

	/**
	 * Answer a string for displaying this target in prompts, etc.
	 * @return a printable string
	 */	
	public abstract String getDisplayName();
	
	/**
	 * Updates the local resource to have the same content as the corresponding remote
	 * resource. Where the local resource does not exist, this method will create it.
	 * <p>
	 * If the remote resource is a container (e.g. folder or project) this operation is equivalent 
	 * to getting each non-container member of the remote resource, thereby updating the
	 * content of existing local members, creating local members to receive new remote resources,
	 * and deleting local members that no longer have a corresponding remote resource.</p>
	 * <p>
	 * Interrupting the method (via the progress monitor) may lead to partial, but consistent, results.</p>
	 * 
	 * @param resources an array of local resources to update from the corresponding remote
	 * resources.
	 * @param progress a progress monitor to indicate the duration of the operation, or
	 * <code>null</code> if progress reporting is not required.
	 * @throws TeamException if there is a problem getting one or more of the resources.  The
	 * exception will contain multiple statuses, one for each resource in the <code>resources</code>
	 * array. 
	 */
	public abstract void get(IResource[] resources, IProgressMonitor progress) throws TeamException;

	/**
	 * Transfers the content of the local resource to the corresponding remote resource.
	 * <p>
	 * If a remote resource does not exist this method creates a new remote resource with the same content
	 * as the given local resource.  The local resource is said to <i>correspond</i> to the new remote resource.</p>
	 * <p>
	 * @param resources an array of local resources to be put.
	 * @param progress a progress monitor to indicate the duration of the operation, or
	 * <code>null</code> if progress reporting is not required.
	 * @throws TeanException if there is a problem put'ing one or more of the resources.
	 * The exception will contain multiple status', one for each resource in the <code>resources</code>
	 * array.
	 */
	public abstract void put(IResource[] resources, IProgressMonitor progress) throws TeamException;

	/**
	 * Set the project that this is a target provider for.
	 * @param project the project we are to be a provider for
	 */
	public void setProject(IProject project) {
		this.project = project;
	}
	
	/**
	 * Return the project that this is a target provider for.
	 * @return The project this is a provider for.
	 */
	public IProject getProject() {
		return this.project;
	}
}

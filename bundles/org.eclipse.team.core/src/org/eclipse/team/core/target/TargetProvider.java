package org.eclipse.team.core.target;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.team.core.TeamException;

public abstract class TargetProvider {
	/**
	 * Answers the site which this target is associated with
	 * @return a printable string
	 */	
	public abstract Site getSite();

	/**
	 * Answers the path describing the place within the site itself
	 * where the provider stores/retrieves to/from (ie. the target root).
	 * @return a printable string
	 */	
	public abstract IPath getIntrasitePath();

	/**
	 * Answers the full path where the provider stores/retrieves to/from.
	 * The target root is the location + the intra site path.
	 * @return a printable string
	 */	
	public IPath getFullPath() {
		return getSite().getPath().append(getIntrasitePath());
	}

	
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
	
	public abstract void deregister(IProject project);
	
	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return getFullPath().toString();
	}
}

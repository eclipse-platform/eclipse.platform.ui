/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.resources.mapping;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A resource mapping context is provided to a resource mapping when traversing
 * the resources of the mapping. The type of context may determine what resources
 * are included in the traversals of a mapping.
 * <p>
 * There are currently two resource mapping contexts: the local mapping context
 * (represented by the singleton <code>LOCAL_CONTEXT</code),
 * and <code>RemoteResourceMappingContext</code>. Implementors of <code>ResourceMapping</code>
 * should not assume that these are the only valid contexts (in order to allow future 
 * extensibility). Therefore, if the provided context is not of one of the above mentioed types,
 * the implementor can assume that the context is a local context.
 * 
 * <p>
 * NOTE: This API is work in progress and will likely change before the final API freeze.
 * </p>
 * 
 * @since 3.1
 * 
 * @see ResourceMapping
 * @see RemoteResourceMappingContext
 */
public class ResourceMappingContext {

    /**
     * Refresh flag constant (bit mask value 0) indicating that no
     * additional refresh behavior is required.
     */
	public static final int NONE = 0;
    
    /**
     * Refresh flag constant (bit mask value 1) indicating that
     * the mapping will be making use of the contents of the files
     * covered by the traversals being refreshed.
     */
    public static final int FILE_CONTENTS_REQUIRED = 1;
	
	/**
	 * This resource mapping context is used to indicate that the operation
	 * that is requesting the traversals is performing a local operation.
	 * Because the operation is local, the resource mapping is free to be 
	 * as precise as desired about what resources make up the mapping without
	 * concern for performing optimized remote operations.
	 */
	public static final ResourceMappingContext LOCAL_CONTEXT = new ResourceMappingContext();

    /**
	 * Return whether the contents of the corresponding remote differs from the
	 * content of the local file in the context of the current operation. By this
     * we mean that this method will return <code>true</code> if the remote contents differ
     * from the local contents and these remote contents would be fetched as part of the
     * operation associated with the context. 
     * For instance, when updating the workspace to the latest remote state,
     * this method would only return <code>true</code> if the remote contentd have changed
     * since the last time the contents where updated. However, if replace the local contents,
     * the methdo would return <code>true</code> if either the remote contents or the lcoal contents 
     * have changed.
     * <p>
     * This can be used by clients to determine if
	 * they need to fetch the remote contents in order to determine if the
	 * resources that constitute the model element are different in another
	 * location. If the lcoa file exists and the remote file does not, then
     * the contents will be said to differ (i.e. <code>true</code> is returned).
     * 
	 * @param file the local file
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting is not desired
	 * @return whether the contents of the corresponding remote differs from the
	 *         content of the local file
	 * @exception CoreException if the contents could not be compared. Reasons include:
     * <ul>
     * <li>The corresponding remote resource is not a container
     *    (status code will be IResourceStatus.RESOURCE_WRONG_TYPE).</li>
     * </ul>
	 */
	public boolean contentDiffers(IFile file, IProgressMonitor monitor) throws CoreException {
        return false;
    }

	/**
	 * Returns an instance of IStorage in order to allow the
	 * caller to access the contents of the remote that corresponds to the given
	 * local resource. If the remote file does not exist, <code>null</code> is
     * returned. The provided local file handle need not exist locally. A
	 * exception is thrown if the corresponding remote resource is not a file.
	 * 
	 * This method may be long running as a server may need to be contacted to
	 * obtain the contents of the file.
	 * 
	 * @param file the local file
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting is not desired
	 * @return a storage that provides access to the contents of the local
	 *         resource's corresponding remote resource. If the remote file
     *         does not exist, <code>null</code> is returned.
	 * @exception CoreException if the contents could not be fetched. Reasons include:
     * <ul>
     * <li>The corresponding remote resource is not a container
     *    (status code will be IResourceStatus.RESOURCE_WRONG_TYPE).</li>
     * </ul>
	 */
	public IStorage fetchContents(IFile file, IProgressMonitor monitor) throws CoreException {
        return null;
    }

	/**
	 * Returns the list of member resources whose corresponding remote resources
	 * are members of the corresponding remote resource of the given local
	 * container. The container need not exist locally and the result may
	 * include entries that do not exist locally and may not include all local
	 * children. An empty list is returned if the remote resource which
	 * corresponds to the container is empty. A <code>null</code> is
     * returned if the remote does not exist. An exception is thrown if the
	 * corresponding remote is not capable of having members.
	 * 
	 * This method may be long running as a server may need to be contacted to
	 * obtain the members of the containers corresponding remote resource.
	 * 
	 * @param container the local container
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting is not desired
	 * @return a list of member resources whose corresponding remote resources
	 *    are members of the remote counterpart of the given container or
     *    <code>null</code> if the remote does not exist.
	 * @exception CoreException if the members could not be fetched. Reasons include:
	 * <ul>
	 * <li>The corresponding remote resource is not a container
	 *    (status code will be IResourceStatus.RESOURCE_WRONG_TYPE).</li>
	 * </ul>
	 */
	public IResource[] fetchMembers(IContainer container, IProgressMonitor monitor) throws CoreException {
        return null;
    }

    /**
     * Refresh the known remote state for any resources covered by the given traversals.
     * Clients who require the latest remote state should invoke this method before
     * invoking any others of the class. Mappings can use this method as a hint to the
     * context provider of which resources will be required for the mapping to generate
     * the proper set of traversals. 
     * <p>
     * Note that this is really only a hint to the context provider.
     * It is up to implementors to decide, based on the provided traversals, how to efficiently
     * perform the refresh. In the ideal case, calls to <code>contentDiffers</code> and <code>fetchMembers</code>
     * would not need to contact the server after a call to a refresh with appropriate traversals. Also, ideally, 
     * if <code>FILE_CONTENTS_REQUIRED</code> is on of the flags, then the contents for these files will be chached as efficiently
     * as possible so that calls to <code>fetchContents</code> will also not need to contact the server. This may
     * not be possible for all context providers, so clients cannot assume that the above mentioed methods will not
     * be long running. It is still advisably for clients to call <code>refresh</code> with as much details
     * as possible since, in the case where a provider is optimized performance will be much better.
     * @param traversals the resource traversals which indicate which resources are to be refreshed
     * @param flags additional refresh behavior. For instance, if <code>FILE_CONTENTS_REQUIRED</code> 
     *      is one of the flags, this indicates that the client will be accessing the contents of
     *      the files covered by the traversals. <code>NONE</code> shoudl be used when no additional
     *      behavior is required
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting is not desired
	 * @exception CoreException if the members could not be fetched. Reasons include:
	 * <ul>
	 * </ul>
     */
    public void refresh(ResourceTraversal[] traversals, int flags, IProgressMonitor monitor) throws CoreException {
        // Do nothing
    }
}

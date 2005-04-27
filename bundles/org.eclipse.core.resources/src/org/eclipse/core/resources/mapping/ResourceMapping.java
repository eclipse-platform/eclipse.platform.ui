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
import org.eclipse.core.runtime.*;

/**
 * A resource mapping supports the transformation of an application model 
 * object into its underlying file system resources. It provides the
 * bridge between a logical element and the physical resource(s) into which it
 * is stored but does not provide more comprehensive model access or
 * manipulations.
 * <p>
 * Mappings provide two means of model traversal. The {@link #accept} method
 * can be used to visit the resources that constitute the model object. Alternatively,
 * a set or traversals can be obtained by calling {@link #getTraversals}. A traversal
 * contains a set of resources and a depth. This allows clients (such a repository providers)
 * to do optimal traversals of the resources w.r.t. the operation that is being performed
 * on the model object.
 * </p>
 * <p>
 * This class may be subclassed by clients.
 * </p>

 * @see IResource
 * @see ResourceTraversal
 * @since 3.1
 */
public abstract class ResourceMapping extends PlatformObject {

	/**
	 * Returns the application model element associated with this
	 * resource mapping.
	 * 
	 * @return the application model element associated with this
	 * resource mapping.
	 */
	public abstract Object getModelObject();

	/**
	 * Returns the projects that contain the resources that constitute this 
	 * application model.
	 * 
	 * @return the projects
	 */
	public abstract IProject[] getProjects();

	/**
	 * Returns one or more traversals that can be used to access all the
	 * physical resources that constitute the logical resource. A traversal is
	 * simply a set of resources and the depth to which they are to be
	 * traversed. This method returns an array of traversals in order to provide
	 * flexibility in describing the traversals that constitute a model element.
	 * <p>
	 * Subclasses should, when possible, include
	 * all resources that are or may be members of the model element. 
     * For instance, a model element should return the same list of
	 * resources regardless of the existance of the files on the file system.
	 * For example, if a logical resource called "form" maps to "/p1/form.xml"
	 * and "/p1/form.java" then whether form.xml or form.java existed, they
	 * should be returned by this method.
	 *</p><p>
	 * In some cases, it may not be possible for a model element to know all the
	 * resources that may constitute the element without accessing the state of
	 * the model element in another location (e.g. a repository). This method is
	 * provided with a context which, when provided, gives access to
	 * the members of correcponding remote containers and the contenst of
	 * corresponding remote files. This gives the model element the opportunity
	 * to deduce what additional resources should be included in the traversal.
	 * </p>
	 * 
	 * @param context gives access to the state of
	 *            remote resources that correspond to local resources for the
	 *            purpose of determining traversals that adequately cover the
	 *            model element resources given the state of the model element
	 *            in another location. This parameter may be <code>null</code>, in
	 *            which case the implementor can assume that only the local
	 *            resources are of interest to the client.
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting is not desired
	 * @return a set of traversals that cover the resources that constitute the
	 *         model element
	 * @exception CoreException if the traversals could not be obtained.
	 */
	public abstract ResourceTraversal[] getTraversals(ResourceMappingContext context, IProgressMonitor monitor) throws CoreException;

    /**
	 * Accepts the given visitor for the resources in this mapping.
	 * The visitor's {@link IResourceVisitor#visit} method is called for each resource
	 * in this mapping. 
	 * 
     * @param context the traversal context
	 * @param visitor the visitor
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting is not desired
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> This resource does not exist.</li>
	 * <li> The visitor failed with this exception.</li>
	 * </ul>
	 */
    public void accept(ResourceMappingContext context, IResourceVisitor visitor, IProgressMonitor monitor) throws CoreException {
        ResourceTraversal[] traversals = getTraversals(context, monitor);
        for (int i = 0; i < traversals.length; i++) {
            ResourceTraversal traversal = traversals[i];
            traversal.accept(visitor);
        }
    }
}

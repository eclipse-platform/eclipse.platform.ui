/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.model;

 
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.model.IWorkbenchAdapter;
/**
 * BranchCategory is the model element for the branches category
 * for a particular repsitory in the repositories view. Its children
 * are the array of all known branch tags, other than HEAD, for the
 * given repository.
 */
public class BranchCategory extends CVSModelElement implements IAdaptable {
	private ICVSRepositoryLocation repository;
	
	/**
	 * TeamStreamsCategory constructor.
	 */
	public BranchCategory(ICVSRepositoryLocation repository) {
		super();
		this.repository = repository;
	}
	
	/**
	 * Returns an object which is an instance of the given class
	 * associated with this object. Returns <code>null</code> if
	 * no such object can be found.
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) return this;
		return null;
	}
	
	/**
	 * Returns the children of this object.  When this object
	 * is displayed in a tree, the returned objects will be this
	 * element's children.  Returns an empty enumeration if this
	 * object has no children.
	 */
	public Object[] internalGetChildren(Object o, IWorkingSet set, IProgressMonitor monitor) throws CVSException {
		CVSTag[] tags = CVSUIPlugin.getPlugin().getRepositoryManager().getKnownTags(repository, set, CVSTag.BRANCH, monitor);
		CVSTagElement[] branchElements = new CVSTagElement[tags.length];
		for (int i = 0; i < tags.length; i++) {
			branchElements[i] = new CVSTagElement(tags[i], repository);
		}
		return branchElements;
	}
	
	public Object[] internalGetChildren(Object o, IProgressMonitor monitor) throws CVSException {
		return internalGetChildren(o, null, monitor);
	}
	
	/**
	 * Returns an image descriptor to be used for displaying an object in the workbench.
	 * Returns null if there is no appropriate image.
	 *
	 * @param object The object to get an image descriptor for.
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		return CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_BRANCHES_CATEGORY);
	}
	
	/**
	 * Returns the name of this element.  This will typically
	 * be used to assign a label to this object when displayed
	 * in the UI.  Returns an empty string if there is no appropriate
	 * name for this object.
	 *
	 * @param object The object to get a label for.
	 */
	public String getLabel(Object o) {
		return Policy.bind("BranchCategory.Branches_1"); //$NON-NLS-1$
	}
	
	/**
	 * Returns the logical parent of the given object in its tree.
	 * Returns null if there is no parent, or if this object doesn't
	 * belong to a tree.
	 *
	 * @param object The object to get the parent for.
	 */
	public Object getParent(Object o) {
		return repository;
	}
	
	/**
	 * Return the repository the given element belongs to.
	 */
	public ICVSRepositoryLocation getRepository(Object o) {
		return repository;
	}
}

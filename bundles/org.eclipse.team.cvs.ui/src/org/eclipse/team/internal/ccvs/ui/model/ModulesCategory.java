/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.model;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * ModulesCategory is the model element for the mdoules category
 * for a particular repository. Its children are the array of all 
 * modules defined in the CVSROOT/Modules file
 */
public class ModulesCategory extends CVSModelElement implements IAdaptable {
	private ICVSRepositoryLocation repository;

	public ModulesCategory(ICVSRepositoryLocation repository) {
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
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
	 */
	public Object[] fetchChildren(Object o, IProgressMonitor monitor) throws TeamException {
		return repository.members(CVSTag.DEFAULT, true /* module definitions */, monitor);
	}

	/**
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		return CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_PROJECT_VERSION);
	}

	/**
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
	 */
	public String getLabel(Object o) {
		return Policy.bind("ModulesCategory.label"); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
	 */
	public Object getParent(Object o) {
		return repository;
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.ui.model.CVSModelElement#isRemoteElement()
	 */
	public boolean isRemoteElement() {
		return true;
	}

	/**
	 * Returns the repository.
	 * @return ICVSRepositoryLocation
	 */
	public ICVSRepositoryLocation getRepository() {
		return repository;
	}

	public String toString() {
		return getRepository().toString() + " (modules)"; //$NON-NLS-1$
	}
}

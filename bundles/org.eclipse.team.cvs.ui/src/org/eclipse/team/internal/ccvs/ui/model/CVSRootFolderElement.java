/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.model;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;

public class CVSRootFolderElement extends CVSResourceElement {

	ICVSFolder[] roots;

	public CVSRootFolderElement(ICVSFolder[] roots) {
		this.roots = roots;
	}
	
	/**
	 * @see IWorkbenchAdapter#members(Object)
	 */
	public Object[] fetchChildren(Object o, IProgressMonitor monitor) {
		CVSFolderElement[] folders = new CVSFolderElement[roots.length];
		for (int i = 0; i < roots.length; i++) {
			folders[i] = new CVSFolderElement(roots[i], false);
		}
		return folders;
	}

	/**
	 * @see IWorkbenchAdapter#getImageDescriptor(Object)
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
	}
	
	/**
	 * @see CVSResourceElement#getCVSResource()
	 */
	public ICVSResource getCVSResource() {
		return null;
	}
	/**
	 * Returns the roots.
	 * @return ICVSFolder[]
	 */
	public ICVSFolder[] getRoots() {
		return roots;
	}

	/**
	 * Sets the roots.
	 * @param roots The roots to set
	 */
	public void setRoots(ICVSFolder[] roots) {
		this.roots = roots;
	}

}

/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.model;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;

public class CVSRootFolderElement extends CVSResourceElement {

	ICVSFolder[] roots;

	public CVSRootFolderElement(ICVSFolder[] roots) {
		this.roots = roots;
	}
	
	/**
	 * @see IWorkbenchAdapter#getChildren(Object)
	 */
	public Object[] getChildren(Object o) {
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
}

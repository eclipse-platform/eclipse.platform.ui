/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.mapping;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchAdapter;

public class ResourceAndDepth extends WorkbenchAdapter implements IAdaptable {
	Object parent;
	IResource resource;
	int depth;
	
	public ResourceAndDepth(Object parent, IResource member, int depth) {
		this.parent = parent;
		this.resource = member;
		this.depth = depth;
	}

	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class)
			return this;
		return null;
	}
	
	public Object getParent(Object object) {
		return parent;
	}
	
	public Object[] getChildren(Object object) {
		if (resource.getType() == IResource.FILE || depth == IResource.DEPTH_ZERO) {
			return new Object[0];
		}
		List children = new ArrayList();
		try {
			IResource[] members = ((IContainer)resource).members();
			for (int i = 0; i < members.length; i++) {
				IResource member = members[i];
				if (depth == IResource.DEPTH_INFINITE) {
					children.add(new ResourceAndDepth(this, member, IResource.DEPTH_INFINITE));
				} else if (depth == IResource.DEPTH_ONE && member.getType() == IResource.FILE) {
					children.add(new ResourceAndDepth(this, member, IResource.DEPTH_ZERO));
				}
			}
			return children.toArray(new Object[children.size()]);
		} catch (CoreException e) {
			TeamUIPlugin.log(e);
			return new Object[0];
		}
	}

	public int getDepth() {
		return depth;
	}

	public Object getParent() {
		return parent;
	}

	public IResource getResource() {
		return resource;
	}
}
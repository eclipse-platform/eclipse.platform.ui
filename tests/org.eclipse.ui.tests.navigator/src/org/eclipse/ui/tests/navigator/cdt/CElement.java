/*******************************************************************************
 * Copyright (c) 2009 Oakland Software Incorporated and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Oakland Software Incorporated - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.navigator.cdt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

public class CElement implements IAdaptable {

	protected IResource _resource;

	protected CElement _parent;

	protected List _children;

	protected CNavigatorContentProvider _cp;

	public CElement(CNavigatorContentProvider cp, IResource resource, CElement parent) {
		_cp = cp;
		_resource = resource;
		cp._resourceToModel.put(_resource, this);
		_children = new ArrayList();
		if (parent != null)
			parent.addChild(this);
	}

	public IResource getResource() {
		return _resource;
	}

	public void setResource(IResource Resource) {
		_resource = Resource;
	}

	public String getElementName() {
		return "CElement: " + _resource.getName();
	}

	public CElement getParent() {
		return _parent;
	}

	public List getChildren() {
		try {
			if (_children.size() == 0 && _resource instanceof IContainer) {
				IResource members[] = ((IContainer) _resource).members();
				for (int i = 0; i < members.length; i++) {
					if (members[i] instanceof IProject)
						_children.add(members[i]);
					else if (members[i] instanceof IFolder)
						_children.add(new CContainer(_cp, members[i], this));
					else
						_children.add(members[i]);
				}

			}
		} catch (CoreException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
		return _children;
	}

	public void addChild(CElement child) {
		// We only want model objects for the containers
		if (!(child instanceof CContainer))
			_children.add(child.getResource());
		else
			_children.add(child);
	}

	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IResource.class)
			return _resource;
		return null;
	}

	@Override
	public String toString() {
		return getClass().getName() + ": " + _resource;
	}
}

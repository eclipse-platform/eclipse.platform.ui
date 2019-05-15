/*******************************************************************************
 * Copyright (c) 2009, 2015 Oakland Software Incorporated and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Oakland Software Incorporated - initial API and implementation
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 460405
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
			if (_children.isEmpty() && _resource instanceof IContainer) {
				IResource members[] = ((IContainer) _resource).members();
				for (IResource member : members) {
					if (member instanceof IProject) {
						_children.add(member);
					} else if (member instanceof IFolder) {
						_children.add(new CContainer(_cp, member, this));
					} else {
						_children.add(member);
					}
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
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == IResource.class)
			return adapter.cast(_resource);
		return null;
	}

	@Override
	public String toString() {
		return getClass().getName() + ": " + _resource;
	}
}

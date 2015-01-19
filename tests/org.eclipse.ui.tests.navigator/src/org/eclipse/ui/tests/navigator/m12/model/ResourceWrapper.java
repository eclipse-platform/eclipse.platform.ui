/*******************************************************************************
 * Copyright (c) 2009, 2010 Fair Isaac Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.ui.tests.navigator.m12.model;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
public abstract class ResourceWrapper implements IWorkbenchAdapter {
	static final M1Resource[] NO_CHILDREN = new M1Resource[0];
	IResource _resource;
	public ResourceWrapper(IResource resource) {
		_resource = resource;
	}
	public IResource getResource() {
		return _resource;
	}
	@Override
	public Object[] getChildren(Object obj) {
		return NO_CHILDREN;
	}
	public M1Resource[] getChildren() throws CoreException {
		return NO_CHILDREN;
	}
	public boolean hasChildren() throws CoreException {
		M1Resource[] children = getChildren();
		return children.length > 0;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof M1Resource) {
			return _resource.equals(((M1Resource) obj).getResource());
		}
		return false;
	}
	@Override
	public int hashCode() {
		return _resource.hashCode();
	}
	@Override
	public Object getParent(Object obj) {
		return getParent();
	}
	public Object getParent() {
		IResource parent = _resource.getParent();
		ResourceWrapper modelParent = getModelObject(parent);
		return (modelParent == null) ? (Object) parent : modelParent;
	}
	protected abstract ResourceWrapper getModelObject(IResource resource);
	public abstract String getModelId();

	@Override
	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
	}
	@Override
	public String getLabel(Object o) {
		return  _resource.toString();
	}

}

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
package org.eclipse.ui.views.navigator;

import org.eclipse.core.resources.IProject;
import org.eclipse.ui.internal.registry.NavigatorContentDescriptor;
import org.eclipse.ui.model.WorkbenchContentProvider;

/**
 */
public class ProjectContentProvider extends WorkbenchContentProvider implements INavigatorContentProvider {
	String id;
	NavigatorContentHandler provider;
	
	public Object[] getChildren(Object element) {
		NavigatorContentDescriptor descriptor = provider.getParentContentDescriptor(element);
		if (descriptor != null) {
			INavigatorContentProvider contentProvider = provider.createContentProvider(descriptor);
			return contentProvider.getChildren(element);
		}
		return new Object[0];
	}
	public Object[] getElements(Object element) {
		return super.getChildren(element);
	}
	public boolean hasChildren(Object element) {
		if (element instanceof IProject) {
			NavigatorContentDescriptor descriptor = provider.getContentDescriptor(element);
			INavigatorContentProvider contentProvider = provider.getContentProvider(descriptor.getId());
			if (contentProvider == null) return true;
			return contentProvider.getChildren(element).length > 0;
		} else {
			return getChildren(element).length > 0;
		}
	}
	public Object getParent(Object element) {
		if (element instanceof IProject) return ((IProject)element).getParent();
		NavigatorContentDescriptor descriptor = provider.getParentContentDescriptor(element);
		if (descriptor != null) {
			INavigatorContentProvider contentProvider = provider.createContentProvider(descriptor);
			return contentProvider.getParent(element);
		}
		return new Object[0];
	}
	public void init (NavigatorContentHandler provider, String id) {
		this.provider = provider;
		this.id = id;
	}
}
/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.adaptable;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class AdaptableResourceWrapper implements IAdaptable {

	IResource resource;

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == IResource.class) {
			return (T) resource;
		}
		if (adapter == IWorkbenchAdapter.class) {
			return (T) TestAdaptableWorkbenchAdapter.getInstance();
		}
		return null;
	}

	public AdaptableResourceWrapper(IResource wrapped) {
		resource = wrapped;
	}

	public String getLabel() {
		return resource.getName() + " Adapted";
	}

	public AdaptableResourceWrapper getParent() {
		if (resource.getParent() != null) {
			return new AdaptableResourceWrapper(resource.getParent());
		}
		return null;
	}

	public AdaptableResourceWrapper[] getChildren() {
		AdaptableResourceWrapper[] wrappers = new AdaptableResourceWrapper[0];

		if (resource instanceof IContainer) {
			IResource[] children;
			try {
				children = ((IContainer) resource).members();
			} catch (CoreException exception) {
				return wrappers;
			}
			wrappers = new AdaptableResourceWrapper[children.length];
			for (int i = 0; i < children.length; i++) {
				wrappers[i] = new AdaptableResourceWrapper(children[i]);
			}
		}
		return wrappers;
	}
}

/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
 *     Andrey Loskutov <loskutov@gmx.de> - generified interface, bug 461762
 *******************************************************************************/
package org.eclipse.ui.internal.ide.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IContributorResourceAdapter;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.ide.IContributorResourceAdapter2;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Adapter factory which provides a ResourceMapping for a working set
 */
public class WorkingSetAdapterFactory implements IAdapterFactory {

	/*
	 * Adapter for converting a working set to a resource mapping for use by
	 * object contributions.
	 */
	static class ContributorResourceAdapter implements IContributorResourceAdapter2 {

		@Override
		public ResourceMapping getAdaptedResourceMapping(IAdaptable adaptable) {
			if (adaptable instanceof IWorkingSet workingSet) {
				for (IAdaptable currentAdaptable : workingSet.getElements()) {
					ResourceMapping mapping = getContributedResourceMapping(currentAdaptable);
					if (mapping == null) {
						mapping = getResourceMapping(currentAdaptable);
					}
					if (mapping != null) {
						return new WorkingSetResourceMapping(workingSet);
					}
				}
			}
			return null;
		}

		@Override
		public IResource getAdaptedResource(IAdaptable adaptable) {
			// Working sets don't adapt to IResource
			return null;
		}

	}

	static class WorkbenchAdapter implements IWorkbenchAdapter {

		@Override
		public Object[] getChildren(Object o) {
			if (o instanceof IWorkingSet set) {
				return set.getElements();
			}
			return null;
		}

		@Override
		public ImageDescriptor getImageDescriptor(Object o) {
			if (o instanceof IWorkingSet set) {
				return set.getImageDescriptor();
			}
			return null;
		}

		@Override
		public String getLabel(Object o) {
			if (o instanceof IWorkingSet set) {
				return set.getLabel();
			}
			return null;
		}

		@Override
		public Object getParent(Object o) {
			return null;
		}

	}

	private final IContributorResourceAdapter2 contributorResourceAdapter = new ContributorResourceAdapter();

	private final IWorkbenchAdapter workbenchAdapter = new WorkbenchAdapter();

	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adaptableObject instanceof IWorkingSet workingSet) {
			if (adapterType == IContributorResourceAdapter.class) {
				return adapterType.cast(contributorResourceAdapter);
			}
			if (adapterType == IWorkbenchAdapter.class) {
				return adapterType.cast(workbenchAdapter);
			}
			if (adapterType == ResourceMapping.class) {
				for (IAdaptable adaptable : workingSet.getElements()) {
					ResourceMapping mapping = getResourceMapping(adaptable);
					if (mapping != null) {
						return adapterType.cast(new WorkingSetResourceMapping(workingSet));
					}
				}
			}
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class[] { IContributorResourceAdapter2.class, IWorkbenchAdapter.class, ResourceMapping.class };
	}

	static ResourceMapping getResourceMapping(Object o) {
		// First, ask the object directly for a resource mapping
		ResourceMapping mapping = Adapters.adapt(o, ResourceMapping.class);
		if (mapping != null) {
			return mapping;
		}
		// If this fails, ask for a resource and convert to a resource mapping
		IResource resource = Adapters.adapt(o, IResource.class);
		if (resource != null) {
			mapping = Adapters.adapt(resource, ResourceMapping.class);
			if (mapping != null) {
				return mapping;
			}
		}
		return null;
	}

	static ResourceMapping getContributedResourceMapping(IAdaptable element) {
		IContributorResourceAdapter resourceAdapter = Adapters.adapt(element, IContributorResourceAdapter.class);
		if (resourceAdapter != null) {
			if (resourceAdapter instanceof IContributorResourceAdapter2 mappingAdapter) {
				// First, use the mapping contributor adapter to get the mapping
				ResourceMapping mapping = mappingAdapter.getAdaptedResourceMapping(element);
				if (mapping != null) {
					return mapping;
				}
			}
			// Next, use the resource adapter to get a resource and then get
			// the mapping for that resource
			IResource resource = resourceAdapter.getAdaptedResource(element);
			if (resource != null) {
				ResourceMapping mapping = Adapters.adapt(resource, ResourceMapping.class);
				if (mapping != null) {
					return mapping;
				}
			}
		}
		return null;
	}

}

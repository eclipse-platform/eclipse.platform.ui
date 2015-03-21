/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Andrey Loskutov <loskutov@gmx.de> - generified interface, bug 461762
 *******************************************************************************/
package org.eclipse.ui.internal.ide.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.Platform;
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
	class ContributorResourceAdapter implements IContributorResourceAdapter2 {

		@Override
		public ResourceMapping getAdaptedResourceMapping(IAdaptable adaptable) {
			if (adaptable instanceof IWorkingSet) {
				IWorkingSet workingSet = (IWorkingSet) adaptable;
				IAdaptable[] elements = workingSet.getElements();
				for (int i = 0; i < elements.length; i++) {
					IAdaptable element = elements[i];
					ResourceMapping mapping = getContributedResourceMapping(element);
					if (mapping == null) {
						mapping = getResourceMapping(element);
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

	class WorkbenchAdapter implements IWorkbenchAdapter {

		@Override
		public Object[] getChildren(Object o) {
			if (o instanceof IWorkingSet) {
				IWorkingSet set = (IWorkingSet) o;
				return set.getElements();
			}
			return null;
		}

		@Override
		public ImageDescriptor getImageDescriptor(Object o) {
			if (o instanceof IWorkingSet) {
				IWorkingSet set = (IWorkingSet) o;
				return set.getImageDescriptor();
			}
			return null;
		}

		@Override
		public String getLabel(Object o) {
			if (o instanceof IWorkingSet) {
				IWorkingSet set = (IWorkingSet) o;
				return set.getLabel();
			}
			return null;
		}

		@Override
		public Object getParent(Object o) {
			return null;
		}

	}

	private IContributorResourceAdapter2 contributorResourceAdapter = new ContributorResourceAdapter();

	private IWorkbenchAdapter workbenchAdapter = new WorkbenchAdapter();

	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adaptableObject instanceof IWorkingSet) {
			if (adapterType == IContributorResourceAdapter.class) {
				return adapterType.cast(contributorResourceAdapter);
			}
			if (adapterType == IWorkbenchAdapter.class) {
				return adapterType.cast(workbenchAdapter);
			}
			if (adapterType == ResourceMapping.class) {
				IWorkingSet workingSet = (IWorkingSet) adaptableObject;
				IAdaptable[] elements = workingSet.getElements();
				for (int i = 0; i < elements.length; i++) {
					IAdaptable element = elements[i];
					ResourceMapping mapping = getResourceMapping(element);
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
		ResourceMapping mapping = internalGetAdapter(o, ResourceMapping.class);
		if (mapping != null) {
			return mapping;
		}
		// If this fails, ask for a resource and convert to a resource mapping
		IResource resource = internalGetAdapter(o, IResource.class);
		if (resource != null) {
			mapping = internalGetAdapter(resource, ResourceMapping.class);
			if (mapping != null) {
				return mapping;
			}
		}
		return null;
	}

	static ResourceMapping getContributedResourceMapping(IAdaptable element) {
		Object resourceAdapter = internalGetAdapter(element, IContributorResourceAdapter.class);
		if (resourceAdapter != null) {
			if (resourceAdapter instanceof IContributorResourceAdapter2) {
				// First, use the mapping contributor adapter to get the mapping
				IContributorResourceAdapter2 mappingAdapter = (IContributorResourceAdapter2) resourceAdapter;
				ResourceMapping mapping = mappingAdapter.getAdaptedResourceMapping(element);
				if (mapping != null) {
					return mapping;
				}
			}
			if (resourceAdapter instanceof IContributorResourceAdapter) {
				// Next, use the resource adapter to get a resource and then get
				// the mapping for that resource
				IResource resource = ((IContributorResourceAdapter) resourceAdapter).getAdaptedResource(element);
				if (resource != null) {
					ResourceMapping mapping = internalGetAdapter(resource, ResourceMapping.class);
					if (mapping != null) {
						return mapping;
					}
				}
			}
		}
		return null;
	}

	static <T> T internalGetAdapter(Object o, Class<T> adapterType) {
		if (o instanceof IAdaptable) {
			IAdaptable element = (IAdaptable) o;
			T adapted = element.getAdapter(adapterType);
			if (adapted != null) {
				return adapterType.cast(adapted);
			}
		}
		// Fallback to the adapter manager in case the object doesn't
		// implement getAdapter or in the case where the implementation
		// doesn't consult the manager
		return Platform.getAdapterManager().getAdapter(o, adapterType);
	}

}

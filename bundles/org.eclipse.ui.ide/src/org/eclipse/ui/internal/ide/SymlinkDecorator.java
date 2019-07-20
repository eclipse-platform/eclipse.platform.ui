/*******************************************************************************
 * Copyright (C) 2012, 2019 Robin Rosenberg <robin.rosenberg@dewire.com> and others.
 *
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *     Robin Rosenberg <robin.rosenberg@dewire.com> - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 430694
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Bug 548799
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import java.util.Optional;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

/**
 * Decorate symbolic links
 * @since 3.8.200
 */
public class SymlinkDecorator implements ILightweightLabelDecorator {

	private static Optional<ImageDescriptor> SYMLINK;

	static {
		SYMLINK = ResourceLocator.imageDescriptorFromBundle(
				IDEWorkbenchPlugin.IDE_WORKBENCH,
				"$nl$/icons/full/ovr16/symlink_ovr.png"); //$NON-NLS-1$

	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		// empty
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// empty
	}

	@Override
	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof ResourceMapping) {
			element = ((ResourceMapping) element).getModelObject();
		}
		IResource resource = Adapters.adapt(element, IResource.class);
		if (resource != null) {
			ResourceAttributes resourceAttributes = resource.getResourceAttributes();
			if (resourceAttributes != null && resourceAttributes.isSymbolicLink()) {
				SYMLINK.ifPresent(decoration::addOverlay);
			}
		}
	}
}

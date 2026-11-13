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

import java.net.URI;
import java.util.Optional;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.ui.internal.ide.dialogs.IDEResourceInfoUtils;

/**
 * Decorate symbolic links
 * @since 3.8.200
 */
public class SymlinkDecorator implements ILightweightLabelDecorator {

	private static Optional<ImageDescriptor> SYMLINK;
	private static final Optional<ImageDescriptor> LINK_WARNING;

	static {
		SYMLINK = ResourceLocator.imageDescriptorFromBundle(
				IDEWorkbenchPlugin.IDE_WORKBENCH,
				"$nl$/icons/full/ovr16/symlink_ovr.svg"); //$NON-NLS-1$

		LINK_WARNING = ResourceLocator.imageDescriptorFromBundle(IDEWorkbenchPlugin.IDE_WORKBENCH,
				"$nl$/icons/full/ovr16/linkwarn_ovr.svg"); //$NON-NLS-1$
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
			if (resourceAttributes != null) {
				if (resourceAttributes.isSymbolicLink()) {
					SYMLINK.ifPresent(decoration::addOverlay);
					URI location = resource.getLocationURI();
					if (location != null) {
						IFileInfo fileInfo = IDEResourceInfoUtils.getFileInfo(location);
						String linkTarget = fileInfo.getStringAttribute(EFS.ATTRIBUTE_LINK_TARGET);
						if (linkTarget != null && !fileInfo.exists()) {
							LINK_WARNING.ifPresent(t -> decoration.addOverlay(t, IDecoration.TOP_LEFT));
						}
					}
				}
			}
		}
	}
}

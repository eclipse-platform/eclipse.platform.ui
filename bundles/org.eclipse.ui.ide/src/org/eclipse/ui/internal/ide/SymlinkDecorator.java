/*******************************************************************************
 * Copyright (C) 2012, 2013 Robin Rosenberg <robin.rosenberg@dewire.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Decorate symbolic links
 * @since 3.8.200
 */
public class SymlinkDecorator implements ILightweightLabelDecorator {

	private static ImageDescriptor SYMLINK;

	static {
        SYMLINK = AbstractUIPlugin.imageDescriptorFromPlugin(
                IDEWorkbenchPlugin.IDE_WORKBENCH,
                "$nl$/icons/full/ovr16/symlink_ovr.gif"); //$NON-NLS-1$

	}

	public void addListener(ILabelProviderListener listener) {
		// empty
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// empty
	}

	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof ResourceMapping)
			element = ((ResourceMapping) element).getModelObject();
		if (element instanceof IAdaptable)
			element = ((IAdaptable)element).getAdapter(IResource.class);
		if (element instanceof IResource) {
			IResource resource = (IResource) element;
			ResourceAttributes resourceAttributes = resource
					.getResourceAttributes();
			if (resourceAttributes != null
					&& resourceAttributes.isSymbolicLink())
				decoration.addOverlay(SYMLINK);
		}
	}
}

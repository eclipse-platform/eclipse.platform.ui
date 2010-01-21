/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * A VirtualResourceDecorator replaces an element's image, if it is a virtual
 * resource.
 */
public class VirtualResourceDecorator implements ILightweightLabelDecorator {
	private static final ImageDescriptor VIRTUAL_FOLDER;

	static {
		VIRTUAL_FOLDER = AbstractUIPlugin.imageDescriptorFromPlugin(
				IDEWorkbenchPlugin.IDE_WORKBENCH,
				"$nl$/icons/full/ovr16/virt_ovr.gif"); //$NON-NLS-1$
	}

	/**
	 * Creates a new <code>VirtualResourceDecorator</code>.
	 */
	public VirtualResourceDecorator() {
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		// no resources to dispose
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
	 *      java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
	}

	/**
	 * Replaces the resource image, if the given element is a virtual resource.
	 * 
	 * @param element
	 *            element to decorate
	 * @param decoration
	 *            The decoration we are adding to
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(Object,
	 *      IDecoration)
	 */
	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof IFolder && ((IResource) element).isVirtual()) {
			decoration.addOverlay(VIRTUAL_FOLDER, IDecoration.BOTTOM_RIGHT);
		}
	}
}

/*******************************************************************************
 * Copyright (c) 2003, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * Mickael Istria (Red Hat Inc.) Bug 264404 - Problem decorators
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.resources.workbench;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * @since 3.2
 */
public class ResourceExtensionLabelProvider extends WorkbenchLabelProvider implements ICommonLabelProvider {


	@Override
	public void init(ICommonContentExtensionSite aConfig) {
		//init
	}


	@Override
	public String getDescription(Object anElement) {

		if (anElement instanceof IResource) {
			return ((IResource) anElement).getFullPath().makeRelative().toString();
		}
		return null;
	}

	@Override
	protected ImageDescriptor decorateImage(ImageDescriptor input, Object element) {
		ImageDescriptor descriptor = super.decorateImage(input, element);
		if (descriptor == null) {
			return null;
		}
		IResource resource = Adapters.adapt(element, IResource.class);
		if (resource != null && (resource.getType() != IResource.PROJECT || ((IProject) resource).isOpen())) {
			ImageDescriptor overlay = null;
			switch (getHighestProblemSeverity(resource)) {
			case IMarker.SEVERITY_ERROR:
				overlay = PlatformUI.getWorkbench().getSharedImages()
						.getImageDescriptor(ISharedImages.IMG_DEC_FIELD_ERROR);
				break;
			case IMarker.SEVERITY_WARNING:
				overlay = PlatformUI.getWorkbench().getSharedImages()
						.getImageDescriptor(ISharedImages.IMG_DEC_FIELD_WARNING);
				break;
			}
			if (overlay != null) {
				descriptor = new DecorationOverlayIcon(descriptor, overlay, IDecoration.BOTTOM_LEFT);
			}
		}
		return descriptor;
	}


	protected int getHighestProblemSeverity(IResource resource) {
		int problemSeverity = -1;
		try {
			for (IMarker marker : resource.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE)) {
				problemSeverity = Math.max(problemSeverity, marker.getAttribute(IMarker.SEVERITY, -1));
				if (problemSeverity >= IMarker.SEVERITY_ERROR) {
					return problemSeverity;
				}
			}
		} catch (CoreException e) {
			// Mute error to prevent pop-up in case of concurrent modification
			// of markers.
		}
		return problemSeverity;
	}


	@Override
	public void restoreState(IMemento aMemento) {

	}

	@Override
	public void saveState(IMemento aMemento) {
	}
}

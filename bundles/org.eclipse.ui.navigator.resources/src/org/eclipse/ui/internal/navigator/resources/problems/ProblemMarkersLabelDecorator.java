/*******************************************************************************
 * Copyright (C) 2012-2016, Robin Stocker <robin@nibor.org> and others,
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Robin Stocker
 * - initial implementation
 * Mickael Istria (Red Hat Inc.)
 * - [264404] Problems markers as decorators (so in Project Explorer too)
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.resources.problems;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorPlugin;

public class ProblemMarkersLabelDecorator implements ILightweightLabelDecorator, ILabelDecorator {

	private final ResourceManager resourceManager = new LocalResourceManager(
			JFaceResources.getResources());

	public ProblemMarkersLabelDecorator() {
	}

	@Override
	public void dispose() {
		resourceManager.dispose();
	}


	@Override
	public void decorate(Object element, IDecoration decoration) {
		IResource resource = Adapters.adapt(element, IResource.class);
		if (resource != null) {
			int problemSeverity = -1;
			try {
				for (IMarker marker : resource.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE)) {
					problemSeverity = Math.max(problemSeverity, marker.getAttribute(IMarker.SEVERITY, -1));
				}
			} catch (CoreException e) {
				WorkbenchNavigatorPlugin.log(e.getMessage(),
						new Status(IStatus.ERROR, WorkbenchNavigatorPlugin.PLUGIN_ID, e.getMessage(), e));
			}
			if (problemSeverity == IMarker.SEVERITY_ERROR) {
				decoration.addOverlay(PlatformUI.getWorkbench().getSharedImages()
						.getImageDescriptor(ISharedImages.IMG_DEC_FIELD_ERROR), IDecoration.BOTTOM_LEFT);
			} else if (problemSeverity == IMarker.SEVERITY_WARNING) {
				decoration.addOverlay(PlatformUI.getWorkbench().getSharedImages()
						.getImageDescriptor(ISharedImages.IMG_DEC_FIELD_WARNING), IDecoration.BOTTOM_LEFT);
			}
		}
	}

	@Override
	public Image decorateImage(Image image, Object element) {
		IResource resource = Adapters.adapt(element, IResource.class);
		if (resource != null) {
			int problemSeverity = -1;
			try {
				for (IMarker marker : resource.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE)) {
					problemSeverity = Math.max(problemSeverity, marker.getAttribute(IMarker.SEVERITY, -1));
				}
			} catch (CoreException e) {
				WorkbenchNavigatorPlugin.log(e.getMessage(),
						new Status(IStatus.ERROR, WorkbenchNavigatorPlugin.PLUGIN_ID, e.getMessage(), e));
			}
			if (problemSeverity == IMarker.SEVERITY_ERROR)
				return getDecoratedImage(image, ISharedImages.IMG_DEC_FIELD_ERROR);
			else if (problemSeverity == IMarker.SEVERITY_WARNING)
				return getDecoratedImage(image, ISharedImages.IMG_DEC_FIELD_WARNING);
		}
		return null;
	}

	private Image getDecoratedImage(Image base, String teamImageId) {
		ImageDescriptor overlay = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(teamImageId);
		DecorationOverlayIcon decorated = new DecorationOverlayIcon(base, overlay, IDecoration.BOTTOM_LEFT);
		return (Image) this.resourceManager.get(decorated);
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return true;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public String decorateText(String text, Object element) {
		return null;
	}

}
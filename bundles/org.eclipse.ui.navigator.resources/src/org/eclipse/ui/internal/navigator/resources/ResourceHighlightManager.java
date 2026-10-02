/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
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
package org.eclipse.ui.internal.navigator.resources;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * For managing highlight markers on resources.
 */
public final class ResourceHighlightManager {
	public static final String HIGHLIGHT_MARKER_TYPE = "org.eclipse.ui.navigator.resources.highlightMarker"; //$NON-NLS-1$

	private ResourceHighlightManager() {
	}

	/**
	 * Returns whether the given resource is currently highlighted.
	 *
	 * @param resource the resource to test; must not be {@code null}
	 * @return {@code true} if the resource has a highlight marker
	 */
	public static boolean isHighlighted(IResource resource) {
		if (resource == null || !resource.isAccessible()) {
			return false;
		}
		try {
			return resource.findMarkers(HIGHLIGHT_MARKER_TYPE, false, IResource.DEPTH_ZERO).length > 0;
		} catch (CoreException e) {
			return false;
		}
	}

	/**
	 * enables or removes the highlight state of the resource.
	 *
	 * @param resource       the resource to highlight or un-highlight; must not be
	 *                       {@code null}
	 * @param needsHighlight {@code true} to add a highlight marker, {@code false}
	 *                       to remove any existing ones
	 * @throws CoreException if the marker operation fails
	 */
	public static void setHighlighted(IResource resource, boolean needsHighlight) throws CoreException {
		if (resource == null || !resource.isAccessible()) {
			return;
		}
		IMarker[] existing = resource.findMarkers(HIGHLIGHT_MARKER_TYPE, false, IResource.DEPTH_ZERO);
		if (needsHighlight) {
			if (existing.length == 0) {
				IMarker marker = resource.createMarker(HIGHLIGHT_MARKER_TYPE);
				marker.setAttribute(IMarker.MESSAGE, resource.getName());
			}
		} else {
			for (IMarker marker : existing) {
				marker.delete();
			}
		}
	}
}

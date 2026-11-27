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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public final class ResourceBookmarks {

	/**
	 * Marker type of the bookmark markers.
	 */
	public static final String MARKER_TYPE = "org.eclipse.ui.navigator.resources.highlightMarker"; //$NON-NLS-1$

	private ResourceBookmarks() {
	}

	/**
	 * Checks whether the given resource currently carries a bookmark marker or not.
	 */
	public static boolean isBookmarked(IResource resource) {
		if (resource == null || !resource.isAccessible()) {
			return false;
		}
		try {
			return resource.findMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO).length > 0;
		} catch (CoreException e) {
			return false;
		}
	}
}

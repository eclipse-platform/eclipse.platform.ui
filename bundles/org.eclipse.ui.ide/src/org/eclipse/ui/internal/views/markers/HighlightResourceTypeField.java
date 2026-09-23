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
package org.eclipse.ui.internal.views.markers;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.views.markers.MarkerField;
import org.eclipse.ui.views.markers.MarkerItem;

/**
 * MarkerField that shows the resource type (e.g. "Java File", "Project",
 * "Folder") for a highlighted resource marker.
 *
 * @since 3.4
 */
public class HighlightResourceTypeField extends MarkerField {

	/**
	 * The marker id for highlight markers.
	 */
	static final String HIGHLIGHT_MARKER_TYPE = "org.eclipse.ui.navigator.resources.highlightMarker"; //$NON-NLS-1$

	@Override
	public String getValue(MarkerItem item) {
		IMarker marker = item.getMarker();
		if (marker == null) {
			return ""; //$NON-NLS-1$
		}
		return getResourceTypeLabel(marker);
	}

	/**
	 * Returns the type of the resource associated with the given marker (e.g. "java
	 * file", "Project", "Folder").
	 *
	 * @param marker the marker id; must not be null
	 * @return a non-null label string
	 */
	static String getResourceTypeLabel(IMarker marker) {
		IResource resource = marker.getResource();
		if (resource == null) {
			return ""; //$NON-NLS-1$
		}
		return switch (resource.getType()) {
			case IResource.FILE -> getFileTypeName((IFile) resource);
			case IResource.FOLDER -> "Folder"; //$NON-NLS-1$
			case IResource.PROJECT -> "Project"; //$NON-NLS-1$
			case IResource.ROOT -> "Workspace Root"; //$NON-NLS-1$
			default -> "Resource"; //$NON-NLS-1$
		};
	}

	private static String getFileTypeName(IFile file) {
		String ext = file.getFileExtension();
		if (ext == null || ext.isBlank()) {
			return "File"; //$NON-NLS-1$
		}
		return ext + " file"; //$NON-NLS-1$
	}
}

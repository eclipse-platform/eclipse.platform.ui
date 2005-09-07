/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import java.text.DateFormat;
import java.util.Date;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.StatusUtil;

public final class Util {

	public static String getString(ResourceBundle resourceBundle, String key)
			throws IllegalArgumentException {
		if (resourceBundle == null || key == null)
			throw new IllegalArgumentException();

		String value = key;

		try {
			value = resourceBundle.getString(key);
		} catch (MissingResourceException eMissingResource) {
			IDEWorkbenchPlugin.log(eMissingResource.getMessage(), StatusUtil
					.newStatus(IStatus.ERROR, eMissingResource.getMessage(),
							eMissingResource));
		}

		return value != null ? value.trim() : null;
	}

	public static String getProperty(String property, IMarker marker) {
		if (marker == null)
			return ""; //$NON-NLS-1$
		try {
			Object obj = marker.getAttribute(property);
			if (obj != null)
				return obj.toString();
			return ""; //$NON-NLS-1$
		} catch (CoreException e) {
			return ""; //$NON-NLS-1$
		}
	}

	public static String getCreationTime(long timestamp) {
		return DateFormat.getDateTimeInstance(DateFormat.LONG,
				DateFormat.MEDIUM).format(new Date(timestamp));
	}

	public static String getCreationTime(IMarker marker) {
		try {
			return getCreationTime(marker.getCreationTime());
		} catch (CoreException e) {
			return ""; //$NON-NLS-1$
		}
	}

	public static String getContainerName(IMarker marker) {
		IPath path = marker.getResource().getFullPath();
		int n = path.segmentCount() - 1; // n is the number of segments in
											// container, not path
		if (n <= 0)
			return ""; //$NON-NLS-1$
		int len = 0;
		for (int i = 0; i < n; ++i)
			len += path.segment(i).length();
		// account for /'s
		if (n > 1)
			len += n - 1;
		StringBuffer sb = new StringBuffer(len);
		for (int i = 0; i < n; ++i) {
			if (i != 0)
				sb.append('/');
			sb.append(path.segment(i));
		}
		return sb.toString();
	}

	public static String getResourceName(IMarker marker) {
		return marker.getResource().getName();
	}

	public static boolean isEditable(IMarker marker) {
		if (marker == null) {
			return false;
		}
		try {
			return marker.isSubtypeOf(IMarker.BOOKMARK)
					|| (marker.isSubtypeOf(IMarker.TASK) && marker
							.getAttribute(IMarker.USER_EDITABLE, true));
		} catch (CoreException e) {
			return false;
		}
	}

	/**
	 * Return an error status for the given exception.
	 * @param exception
	 * @return IStatus
	 */
	public static IStatus errorStatus(Throwable exception) {
		return new Status(IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH,
				IStatus.ERROR, exception.getLocalizedMessage(), exception);
	}

	private Util() {
		super();
	}
}

/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.internal.markers;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;


public class MarkerUtil {
	
	/**
	 * @param property
	 * @param marker
	 * @return a String representation of the Object associated with the property on the marker or
	 * an empty String if no Object is associated with the property.
	 */
	public static String getProperty(String property, IMarker marker) {
		if (marker == null)
			return ""; //$NON-NLS-1$
		try {
			Object obj = marker.getAttribute(property);
			if (obj != null)
				return obj.toString();
			return ""; //$NON-NLS-1$
		}
		catch (CoreException e) {
			return ""; //$NON-NLS-1$
		}
	}
	
	/**
	 * @param marker
	 * @return a String representation of the marker's creation time.
	 */
	public static String getCreationTime(IMarker marker) {
		try {
			return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM).format(new Date(marker.getCreationTime()));
		} catch (CoreException e) {
			return ""; //$NON-NLS-1$
		}
	}
	
	/**
	 * @param marker
	 * @return a String representation of the marker's resource's container.
	 */
	public static String getContainerName(IMarker marker) {
		IPath path = marker.getResource().getFullPath();
		int n = path.segmentCount() - 1; // n is the number of segments in container, not path
		if (n <= 0)
			return ""; //$NON-NLS-1$
		int len = 0;
		for (int i = 0; i < n; ++i)
			len += path.segment(i).length();
		// account for /'s
		if (n > 1)
			len += n-1;
		StringBuffer sb = new StringBuffer(len);
		for (int i = 0; i < n; ++i) {
			if (i != 0)
				sb.append('/');
			sb.append(path.segment(i));
		}
		return sb.toString();
	}
	
	/**
	 * @param marker
	 * @return a String representation of the marker's associated resource.
	 */
	public static String getResourceName(IMarker marker) {
		return marker.getResource().getName();
	}
	
	/**
	 * @param marker
	 * @return
	 * <ul>
	 * <li><code>true</code> if the marker is user editable.</li>
	 * <li><code></code> if the marker is not user editable.</li>
	 * </ul>
	 */
	public static boolean isEditable(IMarker marker) {
		if (marker == null) {
			return false;
		}
		try {
			return marker.isSubtypeOf(IMarker.BOOKMARK) || 
				   (marker.isSubtypeOf(IMarker.TASK) && 
				   marker.getAttribute(IMarker.USER_EDITABLE, false));
		}
		catch (CoreException e) {
			return false;
		}
	}
	
}

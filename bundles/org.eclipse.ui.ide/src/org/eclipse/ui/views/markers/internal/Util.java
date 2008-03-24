/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import com.ibm.icu.text.DateFormat;
import java.util.Date;
import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.Policy;
import org.eclipse.ui.views.markers.MarkerViewUtil;

/**
 * The Util class is the class of general utilities used by the marker support.
 * 
 */
public final class Util {

	static String EMPTY_STRING = "";//$NON-NLS-1$

	static String TWO_LINE_FEED = "\n\n";//$NON-NLS-1$

	static String LINE_FEED_AND_TAB = "\n\t";//$NON-NLS-1$

	private static DateFormat format;

	static final MarkerNode[] EMPTY_MARKER_ARRAY = new MarkerNode[0];
	
	static final String TYPE_MARKER_GROUPING_ID = "org.eclipse.ui.ide.type"; //$NON-NLS-1$

	/**
	 * Get the propery called property from the marker. If it is not found
	 * return the empty string.
	 * 
	 * @param property
	 * @param marker
	 * @return String
	 */
	public static String getProperty(String property, IMarker marker) {
		if (marker == null || !marker.exists()) {
			return EMPTY_STRING;
		}
		try {
			Object obj = marker.getAttribute(property);
			if (obj != null) {
				return obj.toString();
			}
			return EMPTY_STRING;
		} catch (CoreException e) {
			Policy.handle(e);
			return EMPTY_STRING;
		}
	}

	/**
	 * Get the human readable creation time from the timestamp
	 * 
	 * @param timestamp
	 * @return String
	 */
	public static String getCreationTime(long timestamp) {
		if (format == null) {
			format = DateFormat.getDateTimeInstance(DateFormat.LONG,
					DateFormat.MEDIUM);
		}
		return format.format(new Date(timestamp));
	}

	/**
	 * Get the human readable creation time from the marker.
	 * 
	 * @param marker
	 * @return String
	 */
	public static String getCreationTime(IMarker marker) {
		try {
			return getCreationTime(marker.getCreationTime());
		} catch (CoreException e) {
			Policy.handle(e);
			return EMPTY_STRING;
		}
	}

	/**
	 * Get the name of the container. If the marker has the
	 * MarkerViewUtil#PATH_ATTRIBUTE set use that. Otherwise use the path of the
	 * parent resource.
	 * 
	 * @param marker
	 * @return String
	 */
	public static String getContainerName(IMarker marker) {

		if (!marker.exists())
			return Util.EMPTY_STRING;

		try {
			Object pathAttribute = marker
					.getAttribute(MarkerViewUtil.PATH_ATTRIBUTE);

			if (pathAttribute != null) {
				return pathAttribute.toString();
			}
		} catch (CoreException exception) {
			// Log the exception and fall back.
			Policy.handle(exception);
		}

		IPath path = marker.getResource().getFullPath();
		int n = path.segmentCount() - 1; // n is the number of segments in
		// container, not path
		if (n <= 0) {
			return Util.EMPTY_STRING;
		}
		int len = 0;
		for (int i = 0; i < n; ++i) {
			len += path.segment(i).length();
		}
		// account for /'s
		if (n > 1) {
			len += n - 1;
		}
		StringBuffer sb = new StringBuffer(len);
		for (int i = 0; i < n; ++i) {
			if (i != 0) {
				sb.append('/');
			}
			sb.append(path.segment(i));
		}
		return sb.toString();
	}

	/**
	 * Get the name of the element. If the marker has the
	 * MarkerViewUtil#NAME_ATTRIBUTE set use that. Otherwise use the name of the
	 * resource.
	 * 
	 * @param marker
	 * @return String
	 */
	public static String getResourceName(IMarker marker) {

		if (!marker.exists())
			return Util.EMPTY_STRING;

		try {
			Object nameAttribute = marker
					.getAttribute(MarkerViewUtil.NAME_ATTRIBUTE);

			if (nameAttribute != null) {
				return nameAttribute.toString();
			}
		} catch (CoreException exception) {
			Policy.handle(exception);
		}

		return marker.getResource().getName();
	}

	/**
	 * Return whether or not the marker is editable.
	 * 
	 * @param marker
	 * @return boolean <code>true</code> if it is editable
	 */
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
	 * 
	 * @param exception
	 * @return IStatus
	 */
	public static IStatus errorStatus(Throwable exception) {
		String message = exception.getLocalizedMessage();
		if (message == null) {
			message = EMPTY_STRING;
		}
		return new Status(IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH,
				IStatus.ERROR, message, exception);
	}

	static final int SHORT_DELAY = 100;// The 100 ms short delay for scheduling

	static final int LONG_DELAY = 30000;// The 30s long delay to run without a

	// builder update

	private Util() {
		super();
	}

	/**
	 * Get the image for the severity if it can be identified.
	 * 
	 * @param severity
	 * @return Image or <code>null</code>
	 */
	public static Image getImage(int severity) {

		if (severity == IMarker.SEVERITY_ERROR) {
			return getIDEImage(IDEInternalWorkbenchImages.IMG_OBJS_ERROR_PATH);
		}
		if (severity == IMarker.SEVERITY_WARNING) {
			return getIDEImage(IDEInternalWorkbenchImages.IMG_OBJS_WARNING_PATH);
		}
		if (severity == IMarker.SEVERITY_INFO) {
			return getIDEImage(IDEInternalWorkbenchImages.IMG_OBJS_INFO_PATH);
		}

		return null;
	}

	/**
	 * Get the IDE image at path.
	 * 
	 * @param path
	 * @return Image
	 */
	private static Image getIDEImage(String constantName) {

		return JFaceResources.getResources().createImageWithDefault(
				IDEInternalWorkbenchImages.getImageDescriptor(constantName));

	}

	/**
	 * Get the short name for the container
	 * 
	 * @param marker
	 * @return String
	 */
	public static String getShortContainerName(IMarker marker) {

		if (!marker.exists())
			return Util.EMPTY_STRING;

		try {
			Object pathAttribute = marker
					.getAttribute(MarkerViewUtil.PATH_ATTRIBUTE);

			if (pathAttribute != null) {
				return pathAttribute.toString();
			}
		} catch (CoreException exception) {
			// Log the exception and fall back.
			Policy.handle(exception);
		}

		IResource resource = marker.getResource();
		int type = resource.getType();

		// Cannot be project relative if it is the root or a project
		if (type == IResource.PROJECT) {
			return resource.getName();
		}

		if (type == IResource.ROOT) {
			return MarkerMessages.Util_WorkspaceRoot;
		}

		String result = marker.getResource().getProjectRelativePath()
				.removeLastSegments(1).toOSString();
		if (result.trim().length() == 0) {
			return MarkerMessages.Util_ProjectRoot;
		}
		return result;
	}

	/**
	 * Return whether or not the selection has one element that is concrete.
	 * 
	 * @param selection
	 * @return <true>code</true> if the selection has one element that is
	 *         concrete.
	 */
	static boolean isSingleConcreteSelection(IStructuredSelection selection) {
		if (selection != null && selection.size() == 1) {
			Object first = selection.getFirstElement();
			if (first instanceof MarkerNode) {
				return ((MarkerNode) first).isConcrete();
			}
		}
		return false;
	}

	/**
	 * Return whether or not all of the elements in the selection are concrete.
	 * 
	 * @param selection
	 * @return <true>code</true> if all of the elements are concrete.
	 */
	public static boolean allConcreteSelection(IStructuredSelection selection) {
		if (selection != null && selection.size() > 0) {
			Iterator nodes = selection.iterator();
			while (nodes.hasNext()) {
				if (((MarkerNode) nodes.next()).isConcrete()) {
					continue;
				}
				return false;
			}
			return true;
		}
		return false;
	}
}

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

package org.eclipse.ui.views.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.ide.Policy;
import org.eclipse.ui.internal.views.markers.MarkerSupportInternalUtilities;
import org.eclipse.ui.views.markers.internal.MarkerView;

/**
 * Utility class for showing markers in the marker views.
 */
public class MarkerViewUtil {

	/**
	 * The PATH_ATTRIBUTE is the tag for the attribute on a marker that can be
	 * used to supply the String for the path rather than using the path of the
	 * underlying resource.
	 * 
	 * @see IMarker#getAttribute(java.lang.String)
	 * @since 3.2
	 */
	public static final String PATH_ATTRIBUTE = "org.eclipse.ui.views.markers.path";//$NON-NLS-1$

	/**
	 * The NAME_ATTRIBUTE is the tag for the attribute on a marker that can be
	 * used to supply the String for the name rather than using the name of the
	 * underlying resource.
	 * 
	 * @see IMarker#getAttribute(java.lang.String)
	 * @since 3.2
	 */
	public static final String NAME_ATTRIBUTE = "org.eclipse.ui.views.markers.name";//$NON-NLS-1$

	/**
	 * Returns the id of the view used to show markers of the same type as the
	 * given marker.
	 * 
	 * @param marker
	 *            the marker
	 * @return the view id or <code>null</code> if no appropriate view could
	 *         be determined
	 * @throws CoreException
	 *             if an exception occurs testing the type of the marker
	 * @since 3.0
	 */
	public static String getViewId(IMarker marker) throws CoreException {
		if (marker.isSubtypeOf(IMarker.TASK)) {
			return IPageLayout.ID_TASK_LIST;
		} else if (marker.isSubtypeOf(IMarker.PROBLEM)) {
			return IPageLayout.ID_PROBLEM_VIEW;
		} else if (marker.isSubtypeOf(IMarker.BOOKMARK)) {
			return IPageLayout.ID_BOOKMARKS;
		}
		return null;
	}

	/**
	 * Shows the given marker in the appropriate view in the given page. This
	 * must be called from the UI thread.
	 * 
	 * @param page
	 *            the workbench page in which to show the marker
	 * @param marker
	 *            the marker to show
	 * @param showView
	 *            <code>true</code> if the view should be shown first
	 *            <code>false</code> to only show the marker if the view is
	 *            already showing
	 * @return <code>true</code> if the marker was successfully shown,
	 *         <code>false</code> if not
	 * @since 3.0
	 */
	public static boolean showMarker(IWorkbenchPage page, IMarker marker,
			boolean showView) {

		boolean returnValue = false;
		try {
			String viewId = getViewId(marker);
			if (viewId == null) // Use the problem view by default
				viewId = IPageLayout.ID_PROBLEM_VIEW;

			IViewPart view = showView ? page.showView(viewId) : page
					.findView(viewId);
			if (view != null)
				returnValue = MarkerSupportInternalUtilities.showMarker(view,
						marker);

			// If we have already shown the new one do not open another one
			viewId = getLegacyViewId(marker);
			if (viewId != null) {
				if (returnValue)
					view = page.findView(viewId);
				else
					view = showView ? page.showView(viewId) : page
							.findView(viewId);
			}

			if (view != null && view instanceof MarkerView) {
				StructuredSelection selection = new StructuredSelection(marker);
				MarkerView markerView = (MarkerView) view;
				markerView.setSelection(selection, true);
				returnValue = true;

			}
		} catch (CoreException e) {
			Policy.handle(e);
		}
		return returnValue;
	}

	/**
	 * Returns the id of the view used to show markers of the same type as the
	 * given marker using.legacy support
	 * 
	 * @param marker
	 *            the marker
	 * @return the view id or <code>null</code> if no appropriate view could
	 *         be determined
	 * @throws CoreException
	 *             if an exception occurs testing the type of the marker
	 */
	private static String getLegacyViewId(IMarker marker) throws CoreException {
		String viewId = getViewId(marker);
		if (viewId == null)
			return null;
		return viewId + MarkerSupportInternalUtilities.LEGACY_SUFFIX;
	}

}

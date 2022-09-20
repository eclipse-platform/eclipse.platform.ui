/*******************************************************************************
 * Copyright (c) 2022 Enda O'Brien and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.markers;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.markers.MarkerItem;
import org.eclipse.ui.views.markers.MarkerSupportView;
import org.eclipse.ui.views.markers.internal.MarkerSupportRegistry;

/**
 * @since 3.5
 *
 *        A problem view that uses a VierweFilter to only show red markers. A
 *        red marker is a problem marker with a color attribute set to red.
 */
public class RedProblemMarkerViewView extends MarkerSupportView {
	public static final String ID = "org.eclipse.ui.tests.LimitAndViewerFilterView";

	public static final String MARKER_COLOR_ATTRIBUTE = "COLOR";
	public static final String MARKER_COLOR_RED = "RED";

	/**
	 * create the view using the problem marker generator
	 */

	public RedProblemMarkerViewView() {
		super(MarkerSupportRegistry.PROBLEMS_GENERATOR);
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		addViewerFilter();
	}

	/**
	 * Add a filter that removes all markers other than red markers. i.e. markers
	 * where the value of MARKER_COLOR_ATTRIBUTE is MARKER_COLOR_RED
	 */

	void addViewerFilter() {

		// Get current page
		IWorkbenchPage wp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		RedProblemMarkerViewView view;
		try {
			view = (RedProblemMarkerViewView) wp.showView(org.eclipse.ui.tests.markers.RedProblemMarkerViewView.ID);

			ISelectionProvider provider = view.getSite().getSelectionProvider();

			TreeViewer viewer = (TreeViewer) provider;

			viewer.addFilter(new ViewerFilter() {

				@Override
				public boolean select(Viewer viewer, Object parentElement, Object element) {
					if (element instanceof MarkerItem) {
						MarkerItem item = (MarkerItem) element;

						// keep the group row
						if (item.getMarker() == null) {
							return true;
						}
						return MARKER_COLOR_RED.equals(item.getAttributeValue(MARKER_COLOR_ATTRIBUTE, ""));
					}
					return false;
				}
			});
		} catch (Exception e) {
			throw new RuntimeException();
		}

	}

}

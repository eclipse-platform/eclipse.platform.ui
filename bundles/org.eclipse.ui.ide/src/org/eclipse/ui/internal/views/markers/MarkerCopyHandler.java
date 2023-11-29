/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
 *     Benjamin Cabe <benjamin.cabe@anyware-tech.com> -
 *     	Fix for Bug 222375 [Markers] copy markers from markers view should 'pretty print'
 *******************************************************************************/
package org.eclipse.ui.internal.views.markers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.part.MarkerTransfer;
import org.eclipse.ui.views.markers.MarkerField;
import org.eclipse.ui.views.markers.MarkerViewHandler;

/**
 * MarkerCopyHandler is the handler for the copy action when the markers view is
 * selected.
 *
 * @since 3.4
 */
public class MarkerCopyHandler extends MarkerViewHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		ExtendedMarkersView view = getView(event);
		if (view == null)
			return null;

		setClipboard(view);
		return this;
	}

	/**
	 * Set the workbench clipboard for the markers.
	 */
	private void setClipboard(ExtendedMarkersView view) {

		IMarker[] markers = view.getSelectedMarkers();

		String markerReport = createMarkerReport(view, markers);

		// Place the markers on the clipboard
		Object[] data = new Object[] { markers, markerReport };
		Transfer[] transferTypes = new Transfer[] { MarkerTransfer.getInstance(), TextTransfer.getInstance() };

		view.getClipboard().setContents(data, transferTypes);
	}

	/**
	 * Creates a plain-text report of the selected markers based on predefined
	 * properties.
	 *
	 * @param view
	 *            the view being copied
	 * @return the marker report
	 */
	static String createMarkerReport(ExtendedMarkersView view, IMarker[] markers) {
		StringBuilder report = new StringBuilder();

		MarkerField[] fields = view.getVisibleFields();

		final String NEWLINE = System.lineSeparator();
		final char DELIMITER = '\t';

		// create header
		for (int i = 0; i < fields.length; i++) {
			report.append(fields[i].getColumnHeaderText());
			if (i == fields.length - 1) {
				report.append(NEWLINE);
			} else {
				report.append(DELIMITER);
			}
		}

		for (IMarker marker : markers) {

			for (int j = 0; j < fields.length; j++) {
				report.append(fields[j].getValue(MarkerSupportInternalUtilities
						.newMarkerItem(marker)));
				if (j == fields.length - 1) {
					report.append(NEWLINE);
				} else {
					report.append(DELIMITER);
				}
			}
		}

		return report.toString();
	}
}

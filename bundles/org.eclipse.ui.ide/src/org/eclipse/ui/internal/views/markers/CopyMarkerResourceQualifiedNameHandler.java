/*******************************************************************************
 * Copyright (c) 2016 Martin Karpisek and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Martin Karpisek <martin.karpisek@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.views.markers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.part.MarkerTransfer;
import org.eclipse.ui.views.markers.MarkerViewHandler;

/**
 * Handler to copy from selected marker its resource qualified name into clipboard.
 * In case more then one markers are selected, resource names in clipboard are separated by newline.
 * @since 4.7
 */
public class CopyMarkerResourceQualifiedNameHandler extends MarkerViewHandler {
	/**
	 * Generates for provided markers text which will be in clipboard if this
	 * handler is executed.
	 */
	static String createMarkersReport(final IMarker[] markers) {
		final String NEWLINE = System.getProperty("line.separator"); //$NON-NLS-1$

		final StringBuffer report = new StringBuffer();
		for (int i = 0; i < markers.length; i++) {
			if (i > 0) {
				report.append(NEWLINE);
			}
			report.append(markers[i].getResource().getFullPath());
		}
		return report.toString();
	}

	@Override
	public Object execute(final ExecutionEvent event) {
		final ExtendedMarkersView view = getView(event);
		if (view == null) {
			return null;
		}

		setClipboard(view);
		return this;
	}

	private void setClipboard(final ExtendedMarkersView view) {
		final IMarker[] markers = view.getSelectedMarkers();
		final String markerReport = createMarkersReport(markers);

		Object[] data = new Object[] { markers, markerReport };
		Transfer[] transferTypes = new Transfer[] { MarkerTransfer.getInstance(), TextTransfer.getInstance() };

		view.getClipboard().setContents(data, transferTypes);
	}
}

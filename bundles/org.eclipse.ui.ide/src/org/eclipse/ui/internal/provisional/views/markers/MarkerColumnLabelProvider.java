/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.provisional.views.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerField;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerItem;

/**
 * The MarkerColumnLabelProvider is a label provider for an individual column.
 * 
 * @since 3.4
 * 
 */
public class MarkerColumnLabelProvider extends ColumnLabelProvider {

	MarkerField field;
	private boolean showHelp;

	/**
	 * Create a MarkerViewLabelProvider on a field.
	 * 
	 * @param field
	 * @param showHelp
	 *            <code>true</code> if help availability is to be shown.
	 */
	MarkerColumnLabelProvider(MarkerField field, boolean showHelp) {
		this.field = field;
		this.showHelp = showHelp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		return field.getValue((MarkerItem) element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {

		if (showHelp && element instanceof MarkerEntry) {
			MarkerItem item = (MarkerItem) element;
			IMarker marker = item.getMarker();
			if (marker != null) {
				String contextId = IDE.getMarkerHelpRegistry().getHelp(marker);

				if (contextId != null)
					return JFaceResources.getImage(Dialog.DLG_IMG_HELP);
			}
		}

		return field.getImage((MarkerItem) element);
	}

}

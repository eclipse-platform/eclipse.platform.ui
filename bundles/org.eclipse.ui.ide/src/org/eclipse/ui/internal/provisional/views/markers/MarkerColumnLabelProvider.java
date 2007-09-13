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
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
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
	private boolean showAnnotations;
	private ResourceManager imageManager;

	/**
	 * Create a MarkerViewLabelProvider on a field.
	 * 
	 * @param field
	 * @param decorate
	 *            <code>true</code> if annotations are to be shown.
	 */
	MarkerColumnLabelProvider(MarkerField field, boolean decorate) {
		FieldDecorationRegistry.getDefault();
		this.field = field;
		this.showAnnotations = decorate;
		imageManager = new LocalResourceManager(JFaceResources.getResources());
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

		// if (showAnnotations && element instanceof MarkerEntry) {
		// MarkerItem item = (MarkerItem) element;
		// IMarker marker = item.getMarker();
		// if (marker != null) {
		// String contextId = IDE.getMarkerHelpRegistry().getHelp(marker);
		//
		// if (contextId != null)
		// return JFaceResources.getImage(Dialog.DLG_IMG_HELP);
		// }
		// }

		MarkerItem item = (MarkerItem) element;
		Image image = field.getImage(item);
		ImageDescriptor[] descriptors = new ImageDescriptor[5];
		if (showAnnotations && item.isConcrete()) {
			IMarker marker = item.getMarker();
			//If there is no image get the full image rather than the decorated one
			if (marker != null) {
				String contextId = IDE.getMarkerHelpRegistry().getHelp(marker);
				if (contextId != null) {
					if (image == null)
						image = JFaceResources.getImage(Dialog.DLG_IMG_HELP);
					else
						descriptors[IDecoration.TOP_RIGHT] = IDEInternalWorkbenchImages
								.getImageDescriptor(IDEInternalWorkbenchImages.IMG_MARKERS_HELP_DECORATION);
				}
				if (IDE.getMarkerHelpRegistry().hasResolutions(marker)) {
					if (image == null)
						image = imageManager
								.createImage(IDEInternalWorkbenchImages
										.getImageDescriptor(IDEInternalWorkbenchImages.IMG_ELCL_QUICK_FIX_ENABLED));
					else
						descriptors[IDecoration.BOTTOM_RIGHT] = IDEInternalWorkbenchImages
								.getImageDescriptor(IDEInternalWorkbenchImages.IMG_MARKERS_QUICK_FIX_DECORATION);
				}

				if (descriptors[IDecoration.TOP_RIGHT] != null
						|| descriptors[IDecoration.BOTTOM_RIGHT] != null)
					image = imageManager.createImage(new DecorationOverlayIcon(
							image, descriptors));
			}
		}
		return image;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
	 */
	public void dispose() {
		super.dispose();
		imageManager.dispose();
	}
}

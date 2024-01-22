/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.ui.model;

import java.util.HashMap;
import java.util.Iterator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * A table label provider implementation for showing workbench perspectives
 * (objects of type <code>IPerspectiveDescriptor</code>) in table- and
 * tree-structured viewers.
 * <p>
 * Clients may instantiate this class. It is not intended to be subclassed.
 * </p>
 *
 * @since 3.0
 */
public final class PerspectiveLabelProvider extends LabelProvider implements ITableLabelProvider {

	/**
	 * List of all Image objects this label provider is responsible for.
	 */
	private HashMap<ImageDescriptor, Image> imageCache = new HashMap<>(5);

	/**
	 * Indicates whether the default perspective is visually marked.
	 */
	private boolean markDefault;

	/**
	 * Creates a new label provider for perspectives. The default perspective is
	 * visually marked.
	 */
	public PerspectiveLabelProvider() {
		this(true);
	}

	/**
	 * Creates a new label provider for perspectives.
	 *
	 * @param markDefault <code>true</code> if the default perspective is to be
	 *                    visually marked, and <code>false</code> if the default
	 *                    perspective is not treated as anything special
	 */
	public PerspectiveLabelProvider(boolean markDefault) {
		super();
		this.markDefault = markDefault;
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof IPerspectiveDescriptor) {
			IPerspectiveDescriptor desc = (IPerspectiveDescriptor) element;
			ImageDescriptor imageDescriptor = desc.getImageDescriptor();
			if (imageDescriptor == null) {
				imageDescriptor = WorkbenchImages.getImageDescriptor(ISharedImages.IMG_ETOOL_DEF_PERSPECTIVE);
			}
			Image image = imageCache.get(imageDescriptor);
			if (image == null) {
				image = imageDescriptor.createImage();
				imageCache.put(imageDescriptor, image);
			}
			return image;
		}
		return null;
	}

	@Override
	public void dispose() {
		for (Iterator<Image> i = imageCache.values().iterator(); i.hasNext();) {
			i.next().dispose();
		}
		imageCache.clear();
	}

	@Override
	public String getText(Object element) {
		if (element instanceof IPerspectiveDescriptor) {
			IPerspectiveDescriptor desc = (IPerspectiveDescriptor) element;
			String label = desc.getLabel();
			if (markDefault) {
				String def = PlatformUI.getWorkbench().getPerspectiveRegistry().getDefaultPerspective();
				if (desc.getId().equals(def)) {
					label = NLS.bind(WorkbenchMessages.PerspectivesPreference_defaultLabel, label);
				}
			}
			return label;
		}
		return WorkbenchMessages.PerspectiveLabelProvider_unknown;
	}

	/**
	 * @see ITableLabelProvider#getColumnImage
	 */
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return getImage(element);
	}

	/**
	 * @see ITableLabelProvider#getColumnText
	 */
	@Override
	public String getColumnText(Object element, int columnIndex) {
		return getText(element);
	}
}

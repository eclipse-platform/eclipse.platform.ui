/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Benjamin Muskalla  - bug 77710
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 430603
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.IResourceUtilities;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.swt.util.ISWTResourceUtilities;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * Provides labels for view children.
 */
public class ViewLabelProvider extends ColumnLabelProvider {

	/**
	 * Image descriptor for enabled clear button.
	 */
	private static final String FOLDER_ICON = "org.eclipse.e4.descriptor.folder"; //$NON-NLS-1$

	private Map<String, Image> imageMap = new HashMap<String, Image>();
	private IEclipseContext context;
	private final Color dimmedForeground;

	private EModelService modelService;

	private MWindow window;

	/**
	 * @param context
	 * @param modelService
	 * @param window
	 *            the workbench window
	 * @param dimmedForeground
	 *            the dimmed foreground color to use for views that are already
	 *            open
	 */
	public ViewLabelProvider(IEclipseContext context, EModelService modelService, MWindow window,
			Color dimmedForeground) {
		this.context = context;
		this.modelService = modelService;
		this.window = window;
		this.dimmedForeground = dimmedForeground;
	}

	@Override
	public void dispose() {
		for (Image image : imageMap.values()) {
			image.dispose();
		}
		super.dispose();
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof MPartDescriptor) {
			String iconURI = ((MPartDescriptor) element).getIconURI();
			if (iconURI != null && iconURI.length() > 0) {
				Image image = imageMap.get(iconURI);
				if (image == null) {
					ISWTResourceUtilities resUtils = (ISWTResourceUtilities) context
							.get(IResourceUtilities.class.getName());
					image = resUtils.imageDescriptorFromURI(URI.createURI(iconURI)).createImage();
					imageMap.put(iconURI, image);
				}
				return image;
			}
			return null;
		} else if (element instanceof String) {
			Image image = imageMap.get(FOLDER_ICON);
			if (image == null) {
				ImageDescriptor desc = WorkbenchImages
						.getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
				image = desc.createImage();
				imageMap.put(FOLDER_ICON, desc.createImage());
			}
			return image;
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		String label = WorkbenchMessages.ViewLabel_unknown;
		if (element instanceof String) {
			label = (String) element;
		} else if (element instanceof MPartDescriptor) {
			label = ((MPartDescriptor) element).getLocalizedLabel();
		}
		return label;
	}

	@Override
	public Color getForeground(Object element) {
		if (element instanceof MPartDescriptor) {
			String elementId = ((MPartDescriptor) element).getElementId();
			List<MPart> findElements = modelService.findElements(
					modelService.getActivePerspective(window), elementId, MPart.class, null);

			if (findElements.size() > 0) {
				MPart part = findElements.get(0);

				// if that is a shared part, check the placeholders
				if (window.getSharedElements().contains(part)) {
					List<MPlaceholder> placeholders = modelService.findElements(
							modelService.getActivePerspective(window), elementId, MPlaceholder.class, null);
					for (MPlaceholder mPlaceholder : placeholders) {
						if (mPlaceholder.isVisible() && mPlaceholder.isToBeRendered()) {
							return dimmedForeground;
						}
					}
					return null;
				}
				// not a shared part
				if (part.isVisible() && part.isToBeRendered()) {
					return dimmedForeground;
				}
			}
		}
		return null;
	}
}

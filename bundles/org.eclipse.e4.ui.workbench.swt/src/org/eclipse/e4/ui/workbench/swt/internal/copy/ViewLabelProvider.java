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
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.swt.internal.copy;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.workbench.IResourceUtilities;
import org.eclipse.e4.ui.workbench.swt.util.ISWTResourceUtilities;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

/**
 * Based on org.eclipse.ui.internal.dialogs.ViewLabelProvider.
 */
public class ViewLabelProvider extends ColumnLabelProvider {

	/**
	 * Image descriptor for enabled clear button.
	 */
	private static final String FOLDER_ICON = "org.eclipse.e4.descriptor.folder"; //$NON-NLS-1$

	/**
	 * Get image descriptors for the clear button.
	 */
	static {
		Bundle bundle = org.eclipse.e4.ui.internal.workbench.swt.WorkbenchSWTActivator
				.getDefault().getBundle();
		IPath path = new Path("$nl$/icons/full/obj16/fldr_obj.gif");
		URL url = FileLocator.find(bundle, path, null);
		ImageDescriptor enabledDesc = ImageDescriptor.createFromURL(url);
		if (enabledDesc != null)
			JFaceResources.getImageRegistry().put(FOLDER_ICON, enabledDesc);
	}

	private Map<String, Image> imageMap = new HashMap<String, Image>();

	private IEclipseContext context;

	/**
	 * @param window
	 *            the workbench window
	 * @param dimmedForeground
	 *            the dimmed foreground color to use for views that are already
	 *            open
	 */
	public ViewLabelProvider(IEclipseContext context) {
		this.context = context;
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
					image = resUtils.imageDescriptorFromURI(
							URI.createURI(iconURI)).createImage();
					imageMap.put(iconURI, image);
				}
				return image;
			}
			return null;
		} else if (element instanceof String) {
			Image image = imageMap.get(FOLDER_ICON);
			if (image == null) {
				image = JFaceResources.getImageRegistry()
						.getDescriptor(FOLDER_ICON).createImage();
				imageMap.put(FOLDER_ICON, image);
			}
			return image;
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		String label = WorkbenchSWTMessages.ViewLabel_unknown;
		if (element instanceof String) {
			label = (String) element;
		} else if (element instanceof MPartDescriptor) {
			label = ((MPartDescriptor) element).getLocalizedLabel();
		}
		return label;
	}

	@Override
	public Color getBackground(Object element) {
		return null;
	}

}

package org.eclipse.ui.externaltools.internal.ant.preferences;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.ant.core.Property;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;

/**
 * Label provider for property elements
 */
final class AntPropertiesLabelProvider extends LabelProvider implements ITableLabelProvider {
	private static final String IMG_CLASSPATH = "icons/full/obj16/classpath.gif"; //$NON-NLS-1$;
	private static final String IMG_PROPERTY = "icons/full/obj16/prop_ps.gif"; //$NON-NLS-1$;

	private Image classpathImage;
	private Image fileImage;
	private Image propertyImage;

	/**
	 * Creates an instance.
	 */
	public AntPropertiesLabelProvider() {
	}
	
	/* (non-Javadoc)
	 * Method declared on IBaseLabelProvider.
	 */
	public void dispose() {
		// file image is shared, do not dispose.
		fileImage = null;
		if (classpathImage != null) {
			classpathImage.dispose();
			classpathImage = null;
		}
		if (propertyImage != null) {
			propertyImage.dispose();
			propertyImage = null;
		}
	}
	
	/* (non-Javadoc)
	 * Method declared on ITableLabelProvider.
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		if (element instanceof Property) {
			return getPropertyImage();
		} else {
			return getFileImage();
		}
	}
	
	/* (non-Javadoc)
	 * Method declared on ITableLabelProvider.
	 */
	public String getColumnText(Object element, int columnIndex) {
		return element.toString();
	}

	public Image getFileImage() {
		if (fileImage == null) {
			fileImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
		}
		return fileImage;
	}
	
	public Image getPropertyImage() {
		if (propertyImage == null) {
			ImageDescriptor desc= ExternalToolsPlugin.getDefault().getImageDescriptor(AntPropertiesLabelProvider.IMG_PROPERTY);
			propertyImage = desc.createImage();
		} 
		return propertyImage;
	}
	
	public Image getClasspathImage() {
		if (classpathImage == null) {
			ImageDescriptor desc = ExternalToolsPlugin.getDefault().getImageDescriptor(AntPropertiesLabelProvider.IMG_CLASSPATH);
			classpathImage = desc.createImage();
		}
		return classpathImage;
	}
}
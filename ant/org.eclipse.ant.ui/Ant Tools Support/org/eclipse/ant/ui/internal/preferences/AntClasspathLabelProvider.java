/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.ui.internal.preferences;


import java.net.URL;

import org.eclipse.ant.ui.internal.model.AntUIImages;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Label provider for classpath elements
 */
public class AntClasspathLabelProvider extends LabelProvider implements ITableLabelProvider {
	private static final String IMG_JAR_FILE = "icons/full/obj16/jar_l_obj.gif"; //$NON-NLS-1$;
	private static final String IMG_CLASSPATH = "icons/full/obj16/classpath.gif"; //$NON-NLS-1$;

	private Image classpathImage;
	private Image folderImage;
	private Image jarImage;

	/**
	 * Creates an instance.
	 */
	public AntClasspathLabelProvider() {
	}

	/* (non-Javadoc)
	 * Method declared on IBaseLabelProvider.
	 */
	public void dispose() {
		// Folder image is shared, do not dispose.
		folderImage = null;
		if (jarImage != null) {
			jarImage.dispose();
			jarImage = null;
		}
		if (classpathImage != null) {
			classpathImage.dispose();
			classpathImage = null;
		}
	}

	/* (non-Javadoc)
	 * Method declared on ITableLabelProvider.
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		URL url = (URL) element;
		if (url.getFile().endsWith("/")) { //$NON-NLS-1$
			return getFolderImage();
		} else {
			return getJarImage();
		}
	}

	/* (non-Javadoc)
	 * Method declared on ITableLabelProvider.
	 */
	public String getColumnText(Object element, int columnIndex) {
		return ((URL) element).getFile();
	}

	private Image getFolderImage() {
		if (folderImage == null) {
			folderImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
		}
		return folderImage;
	}

	private Image getJarImage() {
		if (jarImage == null) {
			ImageDescriptor desc = AntUIImages.getImageDescriptor(AntClasspathLabelProvider.IMG_JAR_FILE);
			if (desc != null) {
				jarImage = desc.createImage();
			}
		}
		return jarImage;
	}

	public Image getClasspathImage() {
		if (classpathImage == null) {
			ImageDescriptor desc = AntUIImages.getImageDescriptor(AntClasspathLabelProvider.IMG_CLASSPATH);
			if (desc != null) {
				classpathImage = desc.createImage();
			}
		}
		return classpathImage;
	}
}

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


import org.eclipse.ant.core.Property;
import org.eclipse.ant.ui.internal.model.AntUIImages;
import org.eclipse.ant.ui.internal.model.IAntUIConstants;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Label provider for property elements
 */
final class AntPropertiesLabelProvider extends LabelProvider implements ITableLabelProvider {

	private Image classpathImage;
	private Image fileImage;

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
	}
	
	/* (non-Javadoc)
	 * Method declared on ITableLabelProvider.
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		if (element instanceof Property) {
			return AntUIImages.getImage(IAntUIConstants.IMG_PROPERTY);
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
}

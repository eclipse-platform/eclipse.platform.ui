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
import org.eclipse.ant.ui.internal.model.IAntUIConstants;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Label provider for classpath elements
 */
public class AntClasspathLabelProvider extends LabelProvider implements ITableLabelProvider {

	/* (non-Javadoc)
	 * Method declared on ITableLabelProvider.
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		String file;
		if (element instanceof URL) {
			URL url = (URL) element;
			file= url.getFile();
		} else {
			file= element.toString();
		}
		
		if (file.endsWith("/")) { //$NON-NLS-1$
			return getFolderImage();
		} else {
			return getJarImage();
		}
	}

	/* (non-Javadoc)
	 * Method declared on ITableLabelProvider.
	 */
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof URL) {
			return ((URL) element).getFile();
		} else {
			return super.getText(element);
		}
	}

	private Image getFolderImage() {
		return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
	}

	private Image getJarImage() {
		return AntUIImages.getImage(IAntUIConstants.IMG_JAR_FILE);
	}

	public Image getClasspathImage() {
		return AntUIImages.getImage(IAntUIConstants.IMG_TAB_CLASSPATH);
	}
}

/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.provisional.views.markers;

import java.net.URL;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.util.BundleUtility;

import com.ibm.icu.text.CollationKey;
import com.ibm.icu.text.Collator;

/**
 * MarkerUtilities is the class that maintains constants and functionality used
 * by multiple classes.
 * 
 * @since 3.4
 * 
 */
class MarkerUtilities {

	static final String EMPTY_STRING = ""; //$NON-NLS-1$
	static final String ATTRIBUTE_CLASS = "class"; //$NON-NLS-1$
	static final String ATTRIBUTE_ICON = "icon"; //$NON-NLS-1$
	static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$
	static final String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$
	static final CollationKey EMPTY_COLLATION_KEY = Collator.getInstance()
			.getCollationKey(EMPTY_STRING);
	static final IMarker[] EMPTY_MARKER_ARRAY = new IMarker[0];

	static final MarkerItem[] EMPTY_MARKER_ITEM_ARRAY = new MarkerItem[0];
	

	/**
	 * Create the image at the supplied path.
	 * 
	 * @param completeImagePath
	 * @return Image or <code>null</code>.
	 */
	public static Image createImage(String completeImagePath) {
		URL url = BundleUtility.find(IDEWorkbenchPlugin.getDefault()
				.getBundle().getSymbolicName(), completeImagePath);
		if (url == null)
			return null;
		return IDEWorkbenchPlugin.getDefault().getResourceManager()
				.createImageWithDefault(ImageDescriptor.createFromURL(url));
	}

	/**
	 * Get the IDE image at path.
	 * 
	 * @param path
	 * @return Image
	 */
	private static Image getIDEImage(String constantName) {

		return JFaceResources.getResources().createImageWithDefault(
				IDEInternalWorkbenchImages.getImageDescriptor(constantName));

	}
	/**
	 * Get the image for the supplied severity
	 * 
	 * @param severity
	 * @return {@link Image}
	 */
	public static Image getSeverityImage(int severity) {

		if (severity == IMarker.SEVERITY_ERROR) {
			return getIDEImage(IDEInternalWorkbenchImages.IMG_OBJS_ERROR_PATH);
		}
		if (severity == IMarker.SEVERITY_WARNING) {
			return getIDEImage(IDEInternalWorkbenchImages.IMG_OBJS_WARNING_PATH);
		}
		if (severity == IMarker.SEVERITY_INFO) {
			return getIDEImage(IDEInternalWorkbenchImages.IMG_OBJS_INFO_PATH);
		}

		return null;

	}

}

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
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.provisional.views.markers.api.FilterConfigurationArea;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerFieldFilter;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerItem;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerSupportConstants;
import org.eclipse.ui.internal.util.BundleUtility;

import com.ibm.icu.text.CollationKey;
import com.ibm.icu.text.Collator;

/**
 * MarkerSupportUtilities is the class that maintains constants and functionality used
 * by multiple classes.
 * 
 * @since 3.4
 * 
 */
class MarkerSupportInternalUtilities {

	static final String ATTRIBUTE_CLASS = "class"; //$NON-NLS-1$
	static final CollationKey EMPTY_COLLATION_KEY = Collator.getInstance()
			.getCollationKey(MarkerSupportConstants.EMPTY_STRING);
	static final IMarker[] EMPTY_MARKER_ARRAY = new IMarker[0];

	static final MarkerItem[] EMPTY_MARKER_ITEM_ARRAY = new MarkerItem[0];
	static final IResource[] EMPTY_RESOURCE_ARRAY = new IResource[0];
	static final Object CONTAINS_MODIFIER_TOKEN = new Object();
	static final Object CONTAINS_TEXT_TOKEN = new Object();
	
	/**
	 * The markers quick fix decoration.
	 */
	static final String IMG_MARKERS_QUICK_FIX_DECORATION_PATH = "markers/contassist_ovr.gif"; //$NON-NLS-1$
	/**
	 * The markers help decoration.
	 */
	static final String IMG_MARKERS_HELP_DECORATION_PATH = "markers/help_small.gif"; //$NON-NLS-1$
	
	

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
	 * @param constantName
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
	
	/**
	 * Get the MarkerFieldFilter associated with the filter in group.
	 * 
	 * @param group
	 * @param area
	 * @return MarkerFieldFilter or <code>null</code>
	 */
	public final MarkerFieldFilter getFilter(MarkerFieldFilterGroup group, FilterConfigurationArea area) {
		return group.getFilter(area.getField());
	}

}

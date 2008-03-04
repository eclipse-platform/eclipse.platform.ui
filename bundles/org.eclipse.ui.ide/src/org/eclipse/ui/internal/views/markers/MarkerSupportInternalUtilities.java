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

package org.eclipse.ui.internal.views.markers;

import java.net.URL;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.Policy;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.views.markers.MarkerItem;
import org.eclipse.ui.views.markers.MarkerSupportConstants;
import org.eclipse.ui.views.markers.internal.MarkerGroup;
import org.eclipse.ui.views.markers.internal.MarkerGroupingEntry;

import com.ibm.icu.text.CollationKey;
import com.ibm.icu.text.Collator;

/**
 * MarkerSupportUtilities is the class that maintains constants and
 * functionality used by multiple classes.
 * 
 * @since 3.4
 * 
 */
public class MarkerSupportInternalUtilities {

	static final String ATTRIBUTE_CLASS = "class"; //$NON-NLS-1$
	static final CollationKey EMPTY_COLLATION_KEY = Collator.getInstance()
			.getCollationKey(MarkerSupportConstants.EMPTY_STRING);
	static final IMarker[] EMPTY_MARKER_ARRAY = new IMarker[0];

	static final MarkerSupportItem[] EMPTY_MARKER_ITEM_ARRAY = new MarkerSupportItem[0];
	static final IResource[] EMPTY_RESOURCE_ARRAY = new IResource[0];
	static final Object CONTAINS_MODIFIER_TOKEN = new Object();
	static final Object CONTAINS_TEXT_TOKEN = new Object();

	/**
	 * A constant to map migration to the filter being migrated
	 */
	public static final String MIGRATE_PREFERENCE_CONSTANT = "_MIGRATE"; //$NON-NLS-1$

	/**
	 * Constant for the problem filters migration.
	 */
	public static final String MIGRATE_PROBLEM_FILTERS = IDEInternalPreferences.PROBLEMS_FILTERS
			+ MIGRATE_PREFERENCE_CONSTANT;
	/**
	 * Constant for the task filters migration.
	 */
	public static final String MIGRATE_TASK_FILTERS = IDEInternalPreferences.TASKS_FILTERS
			+ MIGRATE_PREFERENCE_CONSTANT;
	/**
	 * Constant for the bookmark filters migration.
	 */
	public static final String MIGRATE_BOOKMARK_FILTERS = IDEInternalPreferences.BOOKMARKS_FILTERS
			+ MIGRATE_PREFERENCE_CONSTANT;

	/**
	 * The string value of the false value for a boolean attribute.
	 */
	public static final Object VALUE_FALSE = "false"; //$NON-NLS-1$

	/**
	 * The suffix to the view names for the legacy markers views.
	 */
	public static final String LEGACY_SUFFIX = ".old"; //$NON-NLS-1$

	/**
	 * The markers quick fix decoration.
	 */
	public static final String IMG_MARKERS_QUICK_FIX_DECORATION_PATH = "markers/contassist_ovr.gif"; //$NON-NLS-1$
	/**
	 * The markers help decoration.
	 */
	public static final String IMG_MARKERS_HELP_DECORATION_PATH = "markers/help_small.gif"; //$NON-NLS-1$

	/**
	 * The configuration element constant for false
	 */
	static final String FALSE = "false"; //$NON-NLS-1$

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
	 * Return a StatusAdapter for the error
	 * 
	 * @param exception
	 * @return StatusAdapter
	 */
	public static StatusAdapter errorFor(Throwable exception) {
		IStatus status = new Status(IStatus.ERROR,
				IDEWorkbenchPlugin.IDE_WORKBENCH, IStatus.ERROR, exception
						.getLocalizedMessage(), exception);
		return new StatusAdapter(status);
	}

	/**
	 * Return the group value of the item in group.
	 * 
	 * @param group
	 * @param item
	 * @return String
	 */
	public static String getGroupValue(MarkerGroup group, MarkerItem item) {
		if (item.getMarker() == null)
			return ((MarkerSupportItem) item).getDescription();
		try {
			MarkerGroupingEntry groupingEntry = group.findGroupValue(item
					.getMarker().getType(), item.getMarker());
			return groupingEntry.getLabel();
		} catch (CoreException exception) {
			Policy.handle(exception);
			return MarkerSupportConstants.EMPTY_STRING;
		}
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
	 * Get the marker limit for the receiver.
	 * 
	 * @return int
	 */
	static int getMarkerLimit() {

		// If limits are enabled return it. Otherwise return -1
		if (IDEWorkbenchPlugin.getDefault().getPreferenceStore().getBoolean(
				IDEInternalPreferences.USE_MARKER_LIMITS)) {
			return IDEWorkbenchPlugin.getDefault().getPreferenceStore().getInt(
					IDEInternalPreferences.MARKER_LIMITS_VALUE);

		}
		return -1;

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
	 * Create the MarkerItem that wraps marker.
	 * 
	 * @param marker
	 * @return {@link MarkerItem}
	 */
	public static MarkerItem newMarkerItem(IMarker marker) {
		return new MarkerEntry(marker);
	}

	/**
	 * Show the marker in view if possible.
	 * 
	 * @param view
	 * @param marker
	 * @return <code>true</code> if the marker is shown
	 */
	public static boolean showMarker(IViewPart view, IMarker marker) {
		if (view instanceof ExtendedMarkersView) {
			StructuredSelection selection = new StructuredSelection(marker);
			ExtendedMarkersView markerView = (ExtendedMarkersView) view;
			markerView.setSelection(selection, true);
			return true;
		}
		return false;

	}

	/**
	 * Returns the highest severity of the given marker item and all its
	 * children.
	 * 
	 * @param markerItem
	 * @return the severity
	 */
	public static int getHighestSeverity(MarkerItem markerItem) {
		if (markerItem instanceof MarkerCategory) {
			MarkerCategory category = (MarkerCategory) markerItem;
			return category.getHighestSeverity();
		}
		IMarker marker = markerItem.getMarker();
		Assert.isNotNull(marker);
		return marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
	}

	/**
	 * Return the children of the given marker item (may return an array of
	 * length 0)
	 * 
	 * @param markerItem
	 * @return the children
	 */
	public static MarkerItem[] getChildren(MarkerItem markerItem) {
		if (markerItem instanceof MarkerCategory) {
			return ((MarkerCategory) markerItem).getChildren();
		}
		return EMPTY_MARKER_ITEM_ARRAY;
	}
}

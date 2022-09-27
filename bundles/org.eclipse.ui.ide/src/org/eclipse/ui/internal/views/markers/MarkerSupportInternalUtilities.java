/*******************************************************************************
 * Copyright (c) 2007, 2019 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 430694
 ******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import java.net.URL;
import java.text.CollationKey;
import java.text.Collator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.Policy;
import org.eclipse.ui.internal.ide.StatusUtil;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.views.markers.FilterConfigurationArea;
import org.eclipse.ui.views.markers.MarkerField;
import org.eclipse.ui.views.markers.MarkerFieldFilter;
import org.eclipse.ui.views.markers.MarkerItem;
import org.eclipse.ui.views.markers.internal.MarkerGroup;
import org.eclipse.ui.views.markers.internal.MarkerGroupingEntry;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * MarkerSupportUtilities is the class that maintains constants and
 * functionality used by multiple classes.
 *
 * @since 3.4
 *
 */
public class MarkerSupportInternalUtilities {

	static final String ATTRIBUTE_CLASS = "class"; //$NON-NLS-1$

	private static final String ATTRIBUTE_FILTER_CLASS = "filterClass"; //$NON-NLS-1$
	private static final String ATTRIBUTE_FILTER_CONFIGURATION_CLASS = "filterConfigurationClass"; //$NON-NLS-1$

	/**
	 * The icon attribute name from a configuration element.
	 */
	public static final String ATTRIBUTE_ICON = "icon"; //$NON-NLS-1$
	/**
	 * The id attribute name from a configuration element.
	 */
	public static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$

	/**
	 * The name attribute name from a configuration element.
	 */
	public static final String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$
	/**
	 * The name attribute name from a configuration element.
	 */
	public static final String ATTRIBUTE_TYPE = "type"; //$NON-NLS-1$
	static final Object CONTAINS_MODIFIER_TOKEN = new Object();
	static final Object CONTAINS_TEXT_TOKEN = new Object();

	static final CollationKey EMPTY_COLLATION_KEY = Collator.getInstance()
			.getCollationKey(MarkerSupportInternalUtilities.EMPTY_STRING);

	static final MarkerSupportItem[] EMPTY_MARKER_ITEM_ARRAY = new MarkerSupportItem[0];
	static final IResource[] EMPTY_RESOURCE_ARRAY = new IResource[0];

	/**
	 * A reusable empty {@link String}
	 */
	public static final String EMPTY_STRING = ""; //$NON-NLS-1$

	public static final String UNKNOWN_ATRRIBTE_VALUE_STRING = MarkerMessages.Unknown;

	/**
	 * The configuration element constant for false
	 */
	static final String FALSE = "false"; //$NON-NLS-1$

	/**
	 * The markers help decoration.
	 */
	public static final String IMG_MARKERS_HELP_DECORATION_PATH = "markers/help_small.png"; //$NON-NLS-1$
	/**
	 * The markers quick fix decoration.
	 */
	public static final String IMG_MARKERS_QUICK_FIX_DECORATION_PATH = "markers/contassist_ovr.png"; //$NON-NLS-1$

	/**
	 * The string value of the false value for a boolean attribute.
	 */
	public static final Object VALUE_FALSE = "false"; //$NON-NLS-1$

	/**
	 * Create the image at the supplied path.
	 *
	 * @param completeImagePath
	 * @param manager the resource manager to allocate the image in
	 * @return Image or <code>null</code>.
	 */
	static final Image createImage(String completeImagePath, ResourceManager manager) {
		URL url = BundleUtility.find(IDEWorkbenchPlugin.getDefault()
				.getBundle().getSymbolicName(), completeImagePath);
		if (url == null)
			return null;
		return manager.createImageWithDefault(ImageDescriptor.createFromURL(url));
	}

	/**
	 * Return a StatusAdapter for the error
	 *
	 * @param exception
	 * @return StatusAdapter
	 */
	static final StatusAdapter errorFor(Throwable exception) {
		IStatus status = new Status(IStatus.ERROR,
				IDEWorkbenchPlugin.IDE_WORKBENCH, IStatus.ERROR, exception
						.getLocalizedMessage(), exception);
		return new StatusAdapter(status);
	}

	/**
	 * Generate the filter for the receiver from the configurationElement.
	 *
	 * @param field
	 *            the field being generated
	 * @return MarkerFieldFilter or <code>null</code>.
	 */
	static final MarkerFieldFilter generateFilter(MarkerField field) {
		IConfigurationElement configurationElement = field
				.getConfigurationElement();
		try {
			if (configurationElement.getAttribute(ATTRIBUTE_FILTER_CLASS) == null)
				return null;
			Object filter = IDEWorkbenchPlugin.createExtension(
					configurationElement, ATTRIBUTE_FILTER_CLASS);
			if (filter == null)
				return null;
			MarkerFieldFilter fieldFilter = (MarkerFieldFilter) filter;
			fieldFilter.setField(field);
			return fieldFilter;
		} catch (CoreException e) {
			Policy.handle(e);
			return null;
		}
	}

	/**
	 * Create a FilterConfigurationArea for the receiver.
	 *
	 * @param field
	 *            the field with the specified area
	 * @return FilterConfigurationArea or <code>null</code>
	 */
	static final FilterConfigurationArea generateFilterArea(MarkerField field) {
		IConfigurationElement configurationElement = field
				.getConfigurationElement();
		try {
			if (configurationElement
					.getAttribute(ATTRIBUTE_FILTER_CONFIGURATION_CLASS) == null)
				return null;
			FilterConfigurationArea area = (FilterConfigurationArea) IDEWorkbenchPlugin
					.createExtension(configurationElement,
							ATTRIBUTE_FILTER_CONFIGURATION_CLASS);
			if (area != null)
				area.setField(field);
			return area;
		} catch (CoreException e) {
			Policy.handle(e);
			return null;
		}
	}

	/**
	 * Return the children of the given marker item (may return an array of
	 * length 0)
	 *
	 * @param markerItem
	 * @return the children
	 */
	public static final MarkerItem[] getChildren(MarkerItem markerItem) {
		if (markerItem instanceof MarkerCategory) {
			return ((MarkerCategory) markerItem).getChildren();
		}
		return EMPTY_MARKER_ITEM_ARRAY;
	}

	/**
	 * Determine the average width of font used by the control.
	 *
	 * @param control
	 * @return int
	 */
	public static final int getFontWidth(Control control) {
		GC gc = new GC(control.getDisplay());
		int width = (int) gc.getFontMetrics().getAverageCharacterWidth();
		gc.dispose();
		return width;
	}

	/**
	 * Return the group value of the item in group.
	 *
	 * @param group
	 * @param item
	 * @return String
	 */
	public static final String  getGroupValue(MarkerGroup group, MarkerItem item) {
		if (item.getMarker() == null)
			return ((MarkerSupportItem) item).getDescription();
		try {
			MarkerGroupingEntry groupingEntry = group.findGroupValue(item
					.getMarker().getType(), item.getMarker());
			return groupingEntry.getLabel();
		} catch (CoreException exception) {
			Policy.handle(exception);
			return MarkerSupportInternalUtilities.EMPTY_STRING;
		}
	}

	/**
	 * Returns the highest severity of the given marker item and all its
	 * children.
	 *
	 * @param markerItem
	 * @return the severity
	 */
	public static final int getHighestSeverity(MarkerItem markerItem) {
		if (markerItem instanceof MarkerCategory) {
			MarkerCategory category = (MarkerCategory) markerItem;
			return category.getHighestSeverity();
		}
		IMarker marker = markerItem.getMarker();
		Assert.isNotNull(marker);
		return markerItem.getAttributeValue(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
	}

	/**
	 * Return the id for the field.
	 *
	 * @param field
	 * @return String
	 */
	public static final String getId(MarkerField field) {
		return field.getConfigurationElement().getAttribute(
				MarkerSupportInternalUtilities.ATTRIBUTE_ID);
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
	 * Return the severity value for item. A value of -1 indicates
	 * that there is no severity value.
	 *
	 * @param item
	 * @return int
	 */
	static int getSeverity(MarkerItem item) {
		return item.getAttributeValue(IMarker.SEVERITY, -1);
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
	static MarkerItem newMarkerItem(IMarker marker) {
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
		return showMarkers(view, new IMarker[] { marker });
	}

	/**
	 * Show the markers in view if possible.
	 *
	 * @param view
	 *            view where to show the markers
	 * @param markers
	 *            markers to be shown
	 *
	 * @return <code>true</code> if the markers are shown, <code>false</code>
	 *         otherwise
	 */
	public static boolean showMarkers(IViewPart view, IMarker[] markers) {
		if (view instanceof ExtendedMarkersView) {
			StructuredSelection selection = new StructuredSelection(markers);
			ExtendedMarkersView markerView = (ExtendedMarkersView) view;
			markerView.setSelection(selection, true);
			return true;
		}
		return false;

	}

	/**
	 * Log an exception from a markers view.
	 *
	 * @param exception
	 */
	public static void logViewError(Exception exception) {
		handleViewError(exception, StatusManager.LOG);
	}

	/**
	 * Show an exception from a markers view.
	 *
	 * @param exception
	 */
	public static void showViewError(Exception exception) {
		handleViewError(exception, StatusManager.LOG | StatusManager.SHOW);
	}

	/**
	 * @param exception
	 * @param handlingMethod
	 *            StatusManager.LOG, StatusManager.SHOW,etc, bitwise ORed
	 */
	public static void handleViewError(Exception exception, int handlingMethod) {
		if (exception instanceof CoreException) {
			// Check for a nested CoreException
			IStatus status = ((CoreException) exception).getStatus();
			if (status != null
					&& status.getException() instanceof CoreException) {
				exception = (CoreException) status.getException();
				status = ((CoreException) exception).getStatus();
			}

			if (status == null)
				StatusManager.getManager().handle(StatusUtil.newError(exception), handlingMethod);
			else
				StatusManager.getManager().handle(status, handlingMethod);
			return;
		}
		StatusManager.getManager().handle(StatusUtil.newError(exception), handlingMethod);
	}
}

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

package org.eclipse.ui.internal.provisional.views.markers.api;

import java.net.URL;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.provisional.views.markers.MarkerEntry;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * MarkerField is the abstract superclass of the definition of the content
 * providers for columns in a Markers View.
 * 
 * @since 3.4
 * 
 */
public abstract class MarkerField {
	
	private static final String ATTRIBUTE_FILTER_CLASS = "filterClass"; //$NON-NLS-1$
	private static final String ATTRIBUTE_FILTER_CONFIGURATION_CLASS = "filterConfigurationClass"; //$NON-NLS-1$
	
	IConfigurationElement configurationElement;

	/**
	 * Compare item1 and item2 for sorting purposes.
	 * 
	 * @param item1
	 * @param item2
	 * @return Either:
	 *         <li>a negative number if the value of item1 is less than the
	 *         value of item2 for this field.
	 *         <li><code>0</code> if the value of item1 and the value of
	 *         item2 are equal for this field.
	 *         <li>a positive number if the value of item1 is greater than the
	 *         value of item2 for this field.
	 */
	public int compare(MarkerItem item1, MarkerItem item2) {
		return getValue(item1).compareTo(getValue(item2));
	}

	/**
	 * Generate the filter for the receiver from the configurationElement.
	 * 
	 * @return MarkerFieldFilter or <code>null</code>.
	 */
	public MarkerFieldFilter generateFilter() {
		try {
			if (configurationElement.getAttribute(ATTRIBUTE_FILTER_CLASS) == null)
				return null;
			Object filter = IDEWorkbenchPlugin.createExtension(
					configurationElement, ATTRIBUTE_FILTER_CLASS);
			if (filter == null)
				return null;
			MarkerFieldFilter fieldFilter = (MarkerFieldFilter) filter;
			fieldFilter.setField(this);
			return fieldFilter;
		} catch (CoreException e) {
			StatusManager.getManager().handle(e.getStatus());
			return null;
		}
	}

	/**
	 * Create a FilterConfigurationArea for the receiver.
	 * 
	 * @return FilterConfigurationArea or <code>null</code>
	 */
	public FilterConfigurationArea generateFilterArea() {
		try {
			if (configurationElement
					.getAttribute(ATTRIBUTE_FILTER_CONFIGURATION_CLASS) == null)
				return null;
			FilterConfigurationArea area = (FilterConfigurationArea) IDEWorkbenchPlugin
					.createExtension(configurationElement,
							ATTRIBUTE_FILTER_CONFIGURATION_CLASS);
			if (area != null)
				area.setField(this);
			return area;
		} catch (CoreException e) {
			StatusManager.getManager().handle(e.getStatus());
			return null;
		}
	}

	/**
	 * @return The image to be displayed in the column header for this field or
	 *         <code>null<code>.
	 */
	public Image getColumnHeaderImage() {
		String path = configurationElement
				.getAttribute(MarkerSupportConstants.ATTRIBUTE_ICON);
		if (path == null)
			return null;
		URL url = BundleUtility.find(configurationElement.getContributor()
				.getName(), path);
		if (url == null)
			return null;
		return IDEWorkbenchPlugin.getDefault().getResourceManager()
				.createImageWithDefault(ImageDescriptor.createFromURL(url));
	}

	/**
	 * @return The text to be displayed in the column header for this field.
	 */
	public String getColumnHeaderText() {
		return configurationElement.getAttribute(MarkerSupportConstants.ATTRIBUTE_NAME);
	}

	/**
	 * Get the number of characters that should be reserved for the receiver.
	 * 
	 * @param control the control to scale from
	 * @return int
	 */
	public int getDefaultColumnWidth(Control control) {
		return 15 * getFontWidth(control);
	}

	/**
	 * Determine the average width of font used by the control.
	 * @param control
	 * @return int
	 */
	public final int getFontWidth(Control control) {
		GC gc = new GC(control.getDisplay());
		int width = gc.getFontMetrics().getAverageCharWidth();
		gc.dispose();
		return width;
	}

	/**
	 * Return the id for the receiver.
	 * 
	 * @return String
	 */
	public String getId() {
		return configurationElement
				.getAttribute(MarkerSupportConstants.ATTRIBUTE_ID);
	}

	/**
	 * Return the image for the receiver. By default return <code>null</code>.
	 * 
	 * @param item
	 * @return The image value of the object for this particular field to be
	 *         displayed to the user or <code>null<code>.
	 */
	public Image getImage(MarkerItem item) {
		return null;
	}

	/**
	 * Return the value for a marker.
	 * 
	 * @param marker
	 * @return String
	 */
	public String getMarkerValue(IMarker marker) {
		return getValue(new MarkerEntry(marker));
	}

	/**
	 * Get the severity of the element.
	 * 
	 * @param element
	 * @return int
	 */
	public int getSeverity(MarkerItem element) {
		if (element.isConcrete())
			return element.getAttributeValue(IMarker.SEVERITY, -1);
		return 0;
	}

	/**
	 * @param item
	 * @return The String value of the object for this particular field to be
	 *         displayed to the user.
	 */
	public abstract String getValue(MarkerItem item);

	/**
	 * Set the configuration element used by the receiver.
	 * 
	 * @param element
	 */
	public void setConfigurationElement(IConfigurationElement element) {
		configurationElement = element;
	}

	
}

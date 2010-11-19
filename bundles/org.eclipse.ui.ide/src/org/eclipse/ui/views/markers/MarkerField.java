/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.views.markers;

import java.net.URL;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.internal.views.markers.MarkerSupportInternalUtilities;

/**
 * MarkerField is the abstract superclass of the definition of the content
 * providers for columns in a Markers View.
 * 
 * @since 3.4
 * 
 */
public abstract class MarkerField {

	private IConfigurationElement configurationElement;
	private ResourceManager imageManager;
	private ImageRegistry imageRegistry;
	
	/**
	 * Annotate the image with indicators for whether or not help or quick fix
	 * are available.
	 * 
	 * @param item
	 *            the item being decorated
	 * @param image
	 *            the image being overlaid
	 * @return Image
	 */
	public Image annotateImage(MarkerItem item, Image image) {
		ImageDescriptor[] descriptors = new ImageDescriptor[5];
		if (item.getMarker() != null) {
			IMarker marker = item.getMarker();
			// If there is no image get the full image rather than the decorated
			// one
			if (marker != null) {
				String contextId = IDE.getMarkerHelpRegistry().getHelp(marker);
				if (contextId != null) {
					if (image == null)
						image = JFaceResources.getImage(Dialog.DLG_IMG_HELP);
					else{
						descriptors[IDecoration.TOP_RIGHT] =
							getIDEImageDescriptor(MarkerSupportInternalUtilities.IMG_MARKERS_HELP_DECORATION_PATH);
					}
				}
				if (IDE.getMarkerHelpRegistry().hasResolutions(marker)) {
					if (image == MarkerSupportInternalUtilities.getSeverityImage(IMarker.SEVERITY_WARNING)) {
						image = WorkbenchPlugin.getDefault().getSharedImages().getImage(IDEInternalWorkbenchImages.IMG_OBJS_FIXABLE_WARNING);
					} else if (image == MarkerSupportInternalUtilities.getSeverityImage(IMarker.SEVERITY_ERROR)) {
						image = WorkbenchPlugin.getDefault().getSharedImages().getImage(IDEInternalWorkbenchImages.IMG_OBJS_FIXABLE_ERROR);
					} else if (image != null) {
						descriptors[IDecoration.BOTTOM_RIGHT] = getIDEImageDescriptor(MarkerSupportInternalUtilities.IMG_MARKERS_QUICK_FIX_DECORATION_PATH);
					}
					if (image == null) {
						image = WorkbenchPlugin.getDefault().getSharedImages().getImage(IDEInternalWorkbenchImages.IMG_ELCL_QUICK_FIX_ENABLED);
					}
				}

				if (descriptors[IDecoration.TOP_RIGHT] != null
						|| descriptors[IDecoration.BOTTOM_RIGHT] != null)
					image = getImageManager().createImage(
							new DecorationOverlayIcon(image, descriptors));
			}
		}
		return image;

	}

	/**
	 * Get the workbench image with the given path relative to
	 * ICON_PATH from the plugins image registry .
	 * @param relativePath
	 * @return ImageDescriptor
	 */
	ImageDescriptor getIDEImageDescriptor(String relativePath){
		if(imageRegistry==null){
			imageRegistry=IDEWorkbenchPlugin.getDefault().getImageRegistry();
		}
		ImageDescriptor descriptor=imageRegistry.getDescriptor(relativePath);
		if(descriptor==null){
			descriptor=IDEWorkbenchPlugin.getIDEImageDescriptor(relativePath);
			imageRegistry.put(relativePath, descriptor);
		}
		return descriptor;
	}
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
	 * @return The image to be displayed in the column header for this field or
	 *         <code>null<code>.
	 */
	public Image getColumnHeaderImage() {
		String path = configurationElement
				.getAttribute(MarkerSupportInternalUtilities.ATTRIBUTE_ICON);
		if (path == null)
			return null;
		URL url = BundleUtility.find(configurationElement.getContributor()
				.getName(), path);
		if (url == null)
			return null;
		return getImageManager().createImageWithDefault(
				ImageDescriptor.createFromURL(url));
	}

	/**
	 * Return the text to be displayed in the column header for this field.
	 * 
	 * @return String
	 * @see #getColumnTooltipText() this is the default column tooltip text
	 */
	public String getColumnHeaderText() {
		return getName();
	}
	
	/**
	 * Return the name of this field.
	 * 
	 * @return String
	 * @since 3.6
	 */
	public String getName() {
		return configurationElement
				.getAttribute(MarkerSupportInternalUtilities.ATTRIBUTE_NAME);
	}

	/**
	 * Return the text for the column tooltip.
	 * 
	 * @return String
	 * @see #getColumnHeaderText()
	 */
	public String getColumnTooltipText() {
		return getColumnHeaderText();
	}

	/**
	 * Get the configuration element for the receiver. This is used by the
	 * markerSupport internals to retreive the values defined in the extenstion.
	 * 
	 * @return IConfigurationElement
	 */
	public final IConfigurationElement getConfigurationElement() {
		return configurationElement;
	}

	/**
	 * Get the number of characters that should be reserved for the receiver.
	 * 
	 * @param control
	 *            the control to scale from
	 * @return int
	 */
	public int getDefaultColumnWidth(Control control) {
		return 15 * MarkerSupportInternalUtilities.getFontWidth(control);
	}

	/**
	 * Return the editing support for entries for this field. Return null if it
	 * cannot be in-line edited.
	 * 
	 * @param viewer
	 *            the viewer this will be applied to
	 * @return {@link EditingSupport} or <code>null</code>.
	 */
	public EditingSupport getEditingSupport(ColumnViewer viewer) {
		return null;
	}

	/**
	 * Return the image manager used by the receiver.
	 * 
	 * @return ResourceManager
	 */
	protected ResourceManager getImageManager() {
		if (imageManager == null)
			return IDEWorkbenchPlugin.getDefault().getResourceManager();
		return imageManager;
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
	public final void setConfigurationElement(IConfigurationElement element) {
		configurationElement = element;
	}

	/**
	 * Set the imageManager. This is not normally required to be send if using a
	 * {@link MarkerSupportView} as this is done for you.
	 * 
	 * @param manager
	 */
	public final void setImageManager(ResourceManager manager) {
		this.imageManager = manager;
	}

	/**
	 * Update the contents of the cell.
	 * 
	 * @param cell
	 */
	public void update(ViewerCell cell) {
		cell.setText(getValue((MarkerItem) cell.getElement()));
		cell.setImage(null);
	}

}

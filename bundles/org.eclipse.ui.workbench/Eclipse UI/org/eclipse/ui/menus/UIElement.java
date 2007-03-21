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

package org.eclipse.ui.menus;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Allow a command or application to provide feedback to a user through updating
 * a MenuItem or ToolItem. Initially used to update properties for UI elements
 * created by the CommandContributionItem.
 * <p>
 * This class may be extended by clients.
 * </p>
 * 
 * @since 3.3
 */
public abstract class UIElement {

	private IServiceLocator serviceLocator;

	/**
	 * Construct a new instance of this class keyed off of the provided service
	 * locator.
	 * 
	 * @param serviceLocator
	 *            the locator. May not be <code>null</code>.
	 */
	protected UIElement(IServiceLocator serviceLocator)
			throws IllegalArgumentException {
		if (serviceLocator == null)
			throw new IllegalArgumentException();
		this.serviceLocator = serviceLocator;
	}

	/**
	 * Update the label on this UI element.
	 * 
	 * @param text
	 *            The new label to display.
	 */
	public abstract void setText(String text);

	/**
	 * Update the tooltip on this UI element. Tooltips are currently only valid
	 * for toolbar contributions.
	 * 
	 * @param text
	 *            The new tooltip to display.
	 */
	public abstract void setTooltip(String text);

	/**
	 * Update the icon on this UI element.
	 * 
	 * @param desc
	 *            The descriptor for the new icon to display.
	 */
	public abstract void setIcon(ImageDescriptor desc);

	/**
	 * Update the disabled icon on this UI element.
	 * 
	 * @param desc
	 *            The descriptor for the new icon to display.
	 */
	public abstract void setDisabledIcon(ImageDescriptor desc);

	/**
	 * Update the hover icon on this UI element.
	 * 
	 * @param desc
	 *            The descriptor for the new icon to display.
	 */
	public abstract void setHoverIcon(ImageDescriptor desc);

	/**
	 * Update the checked state on this UI element. For example, if this was a
	 * toggle or radio button.
	 * 
	 * @param checked
	 *            true to set toggle on
	 */
	public abstract void setChecked(boolean checked);

	/**
	 * Get the service locator scope in which this UI element resides. May not
	 * be <code>null</code>.
	 * 
	 * <p>
	 * The locator may be used to obtain services that are scoped in the same
	 * way as the {@link UIElement}. Such services include but are not limited
	 * to {@link IWorkbench}, {@link IWorkbenchWindow}, and
	 * {@link IWorkbenchPartSite}. While this method may not return
	 * <code>null</code> requests for any of these particular services may
	 * return <code>null</code>.
	 * </p>
	 * 
	 * @return the service locator for this element
	 * @see IServiceLocator#getService(Class)
	 */
	public final IServiceLocator getServiceLocator() {
		return serviceLocator;
	}

	/**
	 * Set the menu contribution id to use. This is only applicable to menu
	 * contributions that support a drop-down style menu. The default
	 * implementation does nothing.
	 * <p>
	 * Example: element.setDropdownId("org.eclipse.ui.navigate.back.my.menu");
	 * </p>
	 * 
	 * @param id
	 *            used to populate the dropdown menu. Must not be
	 *            <code>null</code>.
	 */
	public void setDropDownId(String id) {
		// This does nothing.
	}
}

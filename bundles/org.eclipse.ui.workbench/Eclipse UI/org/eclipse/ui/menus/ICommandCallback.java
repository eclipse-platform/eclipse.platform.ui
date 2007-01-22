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

/**
 * Allow a command or application to provide feedback to a user through updating
 * a MenuItem or ToolItem. Initially used to update properties for UI elements
 * created by the CommandContributionItem.
 * <p>
 * <strong>PROVISIONAL</strong>. This class or interface has been added as part
 * of a work in progress. There is a guarantee neither that this API will work
 * nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.3
 */
public interface ICommandCallback {
	/**
	 * Update the icon on this UI element.
	 * 
	 * @param desc
	 *            The descriptor for the new icon to display.
	 */
	public void setIcon(ImageDescriptor desc);

	/**
	 * Update the disabled icon on this UI element.
	 * 
	 * @param desc
	 *            The descriptor for the new icon to display.
	 */
	public void setDisabledIcon(ImageDescriptor desc);

	/**
	 * Update the hover icon on this UI element.
	 * 
	 * @param desc
	 *            The descriptor for the new icon to display.
	 */
	public void setHoverIcon(ImageDescriptor desc);

	/**
	 * Update the label on this UI element.
	 * 
	 * @param text
	 *            The new label to display.
	 */
	public void setText(String text);

	/**
	 * Update the tooltip on this UI element. Tooltips are currently only valid
	 * for toolbar contributions.
	 * 
	 * @param text
	 *            The new tooltip to display.
	 */
	public void setTooltip(String text);

	/**
	 * Update the checked state on this UI element. For example, if this was a
	 * toggle or radio button.
	 * 
	 * @param checked
	 *            true to set toggle on
	 */
	public void setChecked(boolean checked);
}

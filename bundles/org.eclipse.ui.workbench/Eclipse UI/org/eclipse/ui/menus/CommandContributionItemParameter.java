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

import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.services.IServiceLocator;

/**
 * A help class for the various parameters that can be used with command
 * contributions. Mandatory parameters are in the constructor, and public fields
 * can be set to fill in other parameters.
 * 
 * @since 3.4
 */
public class CommandContributionItemParameter {
	/**
	 * a service locator that is most appropriate for this contribution.
	 * Typically the local {@link IWorkbenchWindow} or
	 * {@link IWorkbenchPartSite} will be sufficient. Must not be
	 * <code>null</code>.
	 */
	public IServiceLocator serviceLocator;

	/**
	 * The id for this item. May be <code>null</code>. Items without an id
	 * cannot be referenced later.
	 */
	public String id;

	/**
	 * A command id for a defined command. Must not be <code>null</code>.
	 */
	public String commandId;

	/**
	 * A map of strings to strings which represent parameter names to values.
	 * The parameter names must match those in the command definition. May be
	 * <code>null</code>
	 */
	public Map parameters;

	/**
	 * An icon for this item. May be <code>null</code>.
	 */
	public ImageDescriptor icon;

	/**
	 * A disabled icon for this item. May be <code>null</code>.
	 */
	public ImageDescriptor disabledIcon;

	/**
	 * A hover icon for this item. May be <code>null</code>.
	 */
	public ImageDescriptor hoverIcon;

	/**
	 * A label for this item. May be <code>null</code>.
	 */
	public String label;

	/**
	 * A mnemonic for this item to be applied to the label. May be
	 * <code>null</code>.
	 */
	public String mnemonic;

	/**
	 * A tooltip for this item. May be <code>null</code>. Tooltips are
	 * currently only valid for toolbar contributions.
	 */
	public String tooltip;

	/**
	 * The style of this menu contribution. See the CommandContributionItem
	 * STYLE_* contants.
	 */
	public int style;

	/**
	 * The help context id to be applied to this contribution. May be
	 * <code>null</code>
	 */
	public String helpContextId;

	/**
	 * The icon style to use. May be <code>null</code> for default style.
	 * 
	 * @see org.eclipse.ui.commands.ICommandImageService
	 */
	public String iconStyle;

	/**
	 * The visibility tracking for a menu contribution.
	 */
	public boolean visibleEnabled;

	/**
	 * Any number of mode bits, like
	 * {@link CommandContributionItem#MODE_FORCE_TEXT}.
	 */
	public int mode;

	/**
	 * Create the parameter object. Nullable attributes can be set directly.
	 * 
	 * @param serviceLocator
	 *            a service locator that is most appropriate for this
	 *            contribution. Typically the local {@link IWorkbenchWindow} or
	 *            {@link IWorkbenchPartSite} will be sufficient. Must not be
	 *            <code>null</code>.
	 * @param id
	 *            The id for this item. May be <code>null</code>. Items
	 *            without an id cannot be referenced later.
	 * @param commandId
	 *            A command id for a defined command. Must not be
	 *            <code>null</code>.
	 * @param style
	 *            The style of this menu contribution. See the STYLE_* contants.
	 */
	public CommandContributionItemParameter(IServiceLocator serviceLocator,
			String id, String commandId, int style) {
		this.serviceLocator = serviceLocator;
		this.id = id;
		this.commandId = commandId;
		this.style = style;
	}

	/**
	 * Build the parameter object.
	 * <p>
	 * <b>Note:</b> This method should not be called outside the framework.
	 * </p>
	 * 
	 * @param serviceLocator
	 *            a service locator that is most appropriate for this
	 *            contribution. Typically the local {@link IWorkbenchWindow} or
	 *            {@link IWorkbenchPartSite} will be sufficient. Must not be
	 *            <code>null</code>.
	 * @param id
	 *            The id for this item. May be <code>null</code>. Items
	 *            without an id cannot be referenced later.
	 * @param commandId
	 *            A command id for a defined command. Must not be
	 *            <code>null</code>.
	 * @param parameters
	 *            A map of strings to strings which represent parameter names to
	 *            values. The parameter names must match those in the command
	 *            definition. May be <code>null</code>
	 * @param icon
	 *            An icon for this item. May be <code>null</code>.
	 * @param disabledIcon
	 *            A disabled icon for this item. May be <code>null</code>.
	 * @param hoverIcon
	 *            A hover icon for this item. May be <code>null</code>.
	 * @param label
	 *            A label for this item. May be <code>null</code>.
	 * @param mnemonic
	 *            A mnemonic for this item to be applied to the label. May be
	 *            <code>null</code>.
	 * @param tooltip
	 *            A tooltip for this item. May be <code>null</code>. Tooltips
	 *            are currently only valid for toolbar contributions.
	 * @param style
	 *            The style of this menu contribution. See the STYLE_* contants.
	 * @param helpContextId
	 *            the help context id to be applied to this contribution. May be
	 *            <code>null</code>
	 * @param visibleEnabled
	 *            Visibility tracking for the menu contribution.
	 */
	public CommandContributionItemParameter(IServiceLocator serviceLocator,
			String id, String commandId, Map parameters, ImageDescriptor icon,
			ImageDescriptor disabledIcon, ImageDescriptor hoverIcon,
			String label, String mnemonic, String tooltip, int style,
			String helpContextId, boolean visibleEnabled) {
		this.serviceLocator = serviceLocator;
		this.id = id;
		this.commandId = commandId;
		this.parameters = parameters;
		this.icon = icon;
		this.disabledIcon = disabledIcon;
		this.hoverIcon = hoverIcon;
		this.label = label;
		this.mnemonic = mnemonic;
		this.tooltip = tooltip;
		this.style = style;
		this.helpContextId = helpContextId;
		this.visibleEnabled = visibleEnabled;
	}
}
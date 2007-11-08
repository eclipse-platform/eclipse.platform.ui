/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.commands;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.commands.CommandImageManager;
import org.eclipse.ui.services.IDisposable;

/**
 * <p>
 * Provides a look-up facility for images associated with commands.
 * </p>
 * <p>
 * The <em>type</em> of an image indicates the state of the associated command
 * within the user interface. The supported types are: <code>TYPE_DEFAULT</code>
 * (to be used for an enabled command), <code>TYPE_DISABLED</code> (to be used
 * for a disabled command) and <code>TYPE_HOVER</code> (to be used for an
 * enabled command over which the mouse is hovering).
 * </p>
 * <p>
 * The <em>style</em> of an image is an arbitrary string used to distinguish
 * between sets of images associated with a command. For example, a command may
 * appear in the menus as the default style. However, in the toolbar, the
 * command is simply the default action for a toolbar drop down item. As such,
 * perhaps a different image style is appropriate. The classic case is the "Run
 * Last Launched" command, which appears in the menu and the toolbar, but with
 * different icons in each location.
 * </p>
 * <p>
 * We currently support a default image style (none) and an image style of
 * IMAGE_STYLE_TOOLBAR.
 * </p>
 * <p>
 * This interface should not be implemented or extended by clients.  It may 
 * change between now and M6.
 * </p>
 * 
 * @since 3.4
 */
public interface ICommandImageService extends IDisposable {

	/**
	 * The type of image to display in the default case.
	 */
	public static final int TYPE_DEFAULT = CommandImageManager.TYPE_DEFAULT;

	/**
	 * The type of image to display if the corresponding command is disabled.
	 */
	public static final int TYPE_DISABLED = CommandImageManager.TYPE_DISABLED;

	/**
	 * The type of image to display if the mouse is hovering over the command
	 * and the command is enabled.
	 */
	public static final int TYPE_HOVER = CommandImageManager.TYPE_HOVER;

	/**
	 * The default image style. This is provided when no style is requested or
	 * when the requested style is unavailable. (Value is <b>null</b>)
	 */
	public static final String IMAGE_STYLE_DEFAULT = null;

	/**
	 * The image style used for commands in a toolbar. This is useful if you
	 * want the icon for the command in the toolbar to be different than the one
	 * that is displayed with menu items. (Value is <b>toolbar</b>)
	 */
	public static final String IMAGE_STYLE_TOOLBAR = "toolbar"; //$NON-NLS-1$

	/**
	 * Retrieves the default image associated with the given command in the
	 * default style.
	 * 
	 * @param commandId
	 *            The identifier to find; must not be <code>null</code>.
	 * @return An image appropriate for the given command; may be
	 *         <code>null</code>.
	 */
	public ImageDescriptor getImageDescriptor(String commandId);

	/**
	 * Retrieves the image of the given type associated with the given command
	 * in the default style.
	 * 
	 * @param commandId
	 *            The identifier to find; must not be <code>null</code>.
	 * 
	 * @param type
	 *            The type of image to retrieve. This value must be one of the
	 *            <code>TYPE</code> constants defined in this interface.
	 * @return An image appropriate for the given command; <code>null</code>
	 *         if the given image type cannot be found.
	 */
	public ImageDescriptor getImageDescriptor(String commandId, int type);

	/**
	 * Retrieves the image of the given type associated with the given command
	 * in the given style.
	 * 
	 * @param commandId
	 *            The identifier to find; must not be <code>null</code>.
	 * @param type
	 *            The type of image to retrieve. This value must be one of the
	 *            <code>TYPE</code> constants defined in this interface.
	 * @param style
	 *            The style of the image to retrieve; may be <code>null</code>.
	 * @return An image appropriate for the given command; <code>null</code>
	 *         if the given image style and type cannot be found.
	 */
	public ImageDescriptor getImageDescriptor(String commandId, int type,
			String style);

	/**
	 * Retrieves the default image associated with the given command in the
	 * given style.
	 * 
	 * @param commandId
	 *            The identifier to find; must not be <code>null</code>.
	 * @param style
	 *            The style of the image to retrieve; may be <code>null</code>.
	 * @return An image appropriate for the given command; <code>null</code>
	 *         if the given image style cannot be found.
	 */
	public ImageDescriptor getImageDescriptor(String commandId, String style);
}

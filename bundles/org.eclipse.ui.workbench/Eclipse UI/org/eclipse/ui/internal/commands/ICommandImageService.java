/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.commands;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
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
 * This interface should not be implemented or extended by clients.
 * </p>
 * <p>
 * <strong>PROVISIONAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * <p>
 * This class is eventually intended to exist in
 * <code>org.eclipse.ui.commands</code>.
 * </p>
 * 
 * @since 3.2
 */
public interface ICommandImageService extends IDisposable {

	/**
	 * The type of image to display in the default case.
	 */
	public static final int TYPE_DEFAULT = ICommandImageService.TYPE_DEFAULT;

	/**
	 * The type of image to display if the corresponding command is disabled.
	 */
	public static final int TYPE_DISABLED = ICommandImageService.TYPE_DISABLED;

	/**
	 * The type of image to display if the mouse is hovering over the command
	 * and the command is enabled.
	 */
	public static final int TYPE_HOVER = ICommandImageService.TYPE_HOVER;

	/**
	 * Binds a particular image descriptor to a command id, type and style
	 * triple
	 * 
	 * @param commandId
	 *            The identifier of the command to which the image should be
	 *            bound; must not be <code>null</code>.
	 * @param type
	 *            The type of image to retrieve. This value must be one of the
	 *            <code>TYPE</code> constants defined in this class.
	 * @param style
	 *            The style of the image; may be <code>null</code>.
	 * @param descriptor
	 *            The image descriptor. Should not be <code>null</code>.
	 */
	public void bind(String commandId, int type, String style,
			ImageDescriptor descriptor);

	/**
	 * Binds a particular image path to a command id, type and style triple
	 * 
	 * @param commandId
	 *            The identifier of the command to which the image should be
	 *            bound; must not be <code>null</code>.
	 * @param type
	 *            The type of image to retrieve. This value must be one of the
	 *            <code>TYPE</code> constants defined in this class.
	 * @param style
	 *            The style of the image; may be <code>null</code>.
	 * @param url
	 *            The URL to the image. Should not be <code>null</code>.
	 */
	public void bind(String commandId, int type, String style, URL url);

	/**
	 * Generates a style tag that is not currently used for the given command.
	 * This can be used by applications trying to create a unique style for a
	 * new set of images.
	 * 
	 * @param commandId
	 *            The identifier of the command for which a unique style is
	 *            required; must not be <code>null</code>.
	 * @return A style tag that is not currently used; may be <code>null</code>.
	 */
	public String generateUnusedStyle(String commandId);

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

	/**
	 * <p>
	 * Reads the command image information from the registry. This will
	 * overwrite any of the existing information in the command image service.
	 * This method is intended to be called during start-up. When this method
	 * completes, this command image service will reflect the current state of
	 * the registry.
	 * </p>
	 */
	public void readRegistry();
}

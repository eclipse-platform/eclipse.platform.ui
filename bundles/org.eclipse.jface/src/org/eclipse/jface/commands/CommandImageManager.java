/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.commands;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * <p>
 * A central lookup facility for images for commands. Images can be associated
 * with commands using this manager.
 * </p>
 * 
 * @since 3.2
 */
public final class CommandImageManager {

	/**
	 * The type of image to display in the default case.
	 */
	public static final int TYPE_DEFAULT = 0;

	/**
	 * The type of image to display if the corresponding command is disabled.
	 */
	public static final int TYPE_DISABLED = 1;

	/**
	 * The type of image to display if the mouse is hovering over the command
	 * and the command is enabled.
	 */
	public static final int TYPE_HOVER = 2;

	/**
	 * The map of command identifiers (<code>String</code>) to images. The
	 * images are an array indexed by type. The values in the array are either
	 * an <code>ImageDescriptor</code> or a <code>Map</code> of style (<code>String</code>)
	 * to <code>ImageDescriptor</code>.
	 */
	private final Map imagesById = new HashMap();

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
	public final void bind(final String commandId, final int type,
			final String style, final URL url) {

		final ImageDescriptor descriptor = ImageDescriptor.createFromURL(url);
		Object[] images = (Object[]) imagesById.get(commandId);
		if (images == null) {
			images = new Object[3];
			imagesById.put(commandId, images);
		}

		if ((type < 0) || (type >= images.length)) {
			throw new IllegalArgumentException(
					"The type must be one of TYPE_DEFAULT, TYPE_DISABLED and TYPE_HOVER."); //$NON-NLS-1$
		}

		final Object typedImage = images[type];
		if (style == null) {
			if ((typedImage == null) || (typedImage instanceof ImageDescriptor)) {
				images[type] = descriptor;
			} else if (typedImage instanceof Map) {
				final Map styleMap = (Map) typedImage;
				styleMap.put(style, descriptor);
			}
		} else {
			if (typedImage instanceof Map) {
				final Map styleMap = (Map) typedImage;
				styleMap.put(style, descriptor);
			} else if (typedImage instanceof ImageDescriptor) {
				final Map styleMap = new HashMap();
				styleMap.put(null, typedImage);
				styleMap.put(style, descriptor);
				images[type] = descriptor;
			}
		}
	}

	/**
	 * Removes all of the images from this manager.
	 */
	public final void clear() {
		imagesById.clear();
	}

	/**
	 * Retrieves the default image associated with the given command in the
	 * default style.
	 * 
	 * @param commandId
	 *            The identifier to find; must not be <code>null</code>.
	 * @return An image appropriate for the given command; never
	 *         <code>null</code>.
	 */
	public final ImageDescriptor getImageDescriptor(final String commandId) {
		final ImageDescriptor descriptor = getImageDescriptor(commandId,
				TYPE_DEFAULT, null);
		if (descriptor == null) {
			return ImageDescriptor.getMissingImageDescriptor();
		}

		return descriptor;
	}

	/**
	 * Retrieves the image of the given type associated with the given command
	 * in the default style.
	 * 
	 * @param commandId
	 *            The identifier to find; must not be <code>null</code>.
	 * @param type
	 *            The type of image to retrieve. This value must be one of the
	 *            <code>TYPE</code> constants defined in this class.
	 * @return An image appropriate for the given command; <code>null</code>
	 *         if the given image type cannot be found.
	 */
	public final ImageDescriptor getImageDescriptor(final String commandId,
			final int type) {
		return getImageDescriptor(commandId, type, null);
	}

	/**
	 * Retrieves the image of the given type associated with the given command
	 * in the given style.
	 * 
	 * @param commandId
	 *            The identifier to find; must not be <code>null</code>.
	 * @param type
	 *            The type of image to retrieve. This value must be one of the
	 *            <code>TYPE</code> constants defined in this class.
	 * @param style
	 *            The style of the image to retrieve; may be <code>null</code>.
	 * @return An image appropriate for the given command; <code>null</code>
	 *         if the given image style and type cannot be found.
	 */
	public final ImageDescriptor getImageDescriptor(final String commandId,
			final int type, final String style) {
		if (commandId == null)
			throw new NullPointerException();

		final Object[] images = (Object[]) imagesById.get(commandId);
		if (images == null) {
			return null;
		}

		if ((type < 0) || (type >= images.length)) {
			throw new IllegalArgumentException(
					"The type must be one of TYPE_DEFAULT, TYPE_DISABLED and TYPE_HOVER."); //$NON-NLS-1$
		}

		Object typedImage = images[type];

		if (typedImage == null) {
			typedImage = images[TYPE_DEFAULT];
		}

		if (typedImage instanceof ImageDescriptor) {
			return (ImageDescriptor) typedImage;
		}

		if (typedImage instanceof Map) {
			final Map styleMap = (Map) typedImage;
			Object styledImage = styleMap.get(style);
			if (styledImage instanceof ImageDescriptor) {
				return (ImageDescriptor) styledImage;
			}

			if (style != null) {
				styledImage = styleMap.get(null);
				if (styledImage instanceof ImageDescriptor) {
					return (ImageDescriptor) styledImage;
				}
			}
		}

		return null;
	}

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
	public final ImageDescriptor getImageDescriptor(final String commandId,
			final String style) {
		return getImageDescriptor(commandId, TYPE_DEFAULT, style);
	}
}

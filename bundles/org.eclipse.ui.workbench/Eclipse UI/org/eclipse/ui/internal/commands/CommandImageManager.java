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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * <p>
 * A central lookup facility for images for commands. Images can be associated
 * with commands using this manager.
 * </p>
 * <p>
 * Clients may instantiate, but must not extend.
 * </p>
 * <p>
 * <strong>PROVISIONAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * <p>
 * This class is eventually intended to exist in
 * <code>org.eclipse.jface.commands</code>.
 * </p>
 * 
 * @since 3.2
 */
public final class CommandImageManager extends EventManager {

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
	 * Adds a listener to this command image manager. The listener will be
	 * notified when the set of image bindings changes. This can be used to
	 * track the global appearance and disappearance of image bindings.
	 * 
	 * @param listener
	 *            The listener to attach; must not be <code>null</code>.
	 */
	public final void addCommandImageManagerListener(
			final ICommandImageManagerListener listener) {
		addListenerObject(listener);
	}

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
		bind(commandId, type, style, descriptor);
	}

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
	 * @param descriptor
	 *            The image descriptor. Should not be <code>null</code>.
	 */
	public final void bind(final String commandId, final int type,
			final String style, final ImageDescriptor descriptor) {
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

		fireManagerChanged(new CommandImageManagerEvent(this,
				new String[] { commandId }, type, style));
	}

	/**
	 * Removes all of the images from this manager.
	 */
	public final void clear() {
		imagesById.clear();
		if (isListenerAttached()) {
			final String[] commandIds = (String[]) imagesById.keySet().toArray(
					new String[imagesById.size()]);
			fireManagerChanged(new CommandImageManagerEvent(this, commandIds,
					TYPE_DEFAULT, null));
		}
	}

	/**
	 * Notifies all of the listeners to this manager that the image bindings
	 * have changed.
	 * 
	 * @param event
	 *            The event to send to all of the listeners; must not be
	 *            <code>null</code>.
	 */
	private final void fireManagerChanged(final CommandImageManagerEvent event) {
		if (event == null) {
			throw new NullPointerException();
		}

		final Object[] listeners = getListeners();
		for (int i = 0; i < listeners.length; i++) {
			final ICommandImageManagerListener listener = (ICommandImageManagerListener) listeners[i];
			listener.commandImageManagerChanged(event);
		}
	}

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
	public final String generateUnusedStyle(final String commandId) {
		final Object[] existingImages = (Object[]) imagesById.get(commandId);
		if (existingImages == null) {
			return null;
		}

		final Set existingStyles = new HashSet(3);
		for (int type = 0; type < existingImages.length; type++) {
			final Object styledImages = existingImages[type];
			if (styledImages instanceof ImageDescriptor) {
				existingStyles.add(null);
			} else if (styledImages instanceof Map) {
				final Map styleMap = (Map) styledImages;
				existingStyles.addAll(styleMap.keySet());
			}
		}

		if (!existingStyles.contains(null)) {
			return null;
		}

		String generatedStyle = "AUTOGEN:::"; //$NON-NLS-1$
		int index = 0;
		while (existingStyles.contains(generatedStyle)) {
			generatedStyle += (index++ % 10);
		}

		return generatedStyle;
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
		return getImageDescriptor(commandId, TYPE_DEFAULT, null);
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
		if (commandId == null) {
			throw new NullPointerException();
		}

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

	/**
	 * Removes a listener from this command image manager.
	 * 
	 * @param listener
	 *            The listener to be removed; must not be <code>null</code>.
	 */
	public final void removeCommandImageManagerListener(
			final ICommandImageManagerListener listener) {
		removeListenerObject(listener);
	}
}

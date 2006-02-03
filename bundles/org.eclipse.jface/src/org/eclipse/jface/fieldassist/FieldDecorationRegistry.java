/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.fieldassist;

import java.util.HashMap;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;

/**
 * FieldDecorationRegistry is a common registry used to define shared field
 * decorations within an application. Unlike resource registries, the
 * FieldDecorationRegistry does not perform any lifecycle management of the
 * decorations.
 * </p>
 * <p>
 * Clients may specify images for the decorations in several different ways.
 * Images may be described by their image id in a specified
 * {@link ImageRegistry}. In this case, the life cycle of the image is managed
 * by the image registry, and the decoration registry will not attempt to obtain
 * an image from the image registry until the decoration is actually requested.
 * In cases where the client has access to an already-created image, the image
 * itself can be specified when registering the decoration. In this case, the
 * life cycle should be managed by the specifying client.
 * </p>
 * <p>
 * This API is considered experimental. It is still evolving during 3.2 and is
 * subject to change. It is being released to obtain feedback from early
 * adopters.
 * 
 * @see FieldDecoration
 * @see DecoratedField
 * @see ImageRegistry
 * 
 * @since 3.2
 */
public class FieldDecorationRegistry {

	/*
	 * Data structure that holds onto the decoration image info and description,
	 * and can produce a decorator on request.
	 */
	class Entry {
		private String description;

		private String imageId;

		private ImageRegistry imageRegistry;

		private Image image;

		private FieldDecoration decoration;

		Entry(String description, String imageId, ImageRegistry registry) {
			this.description = description;
			this.imageId = imageId;
			this.imageRegistry = registry;
		}

		Entry(String description, Image image) {
			this.description = description;
			this.image = image;
		}

		FieldDecoration getDecoration() {
			if (decoration == null) {
				if (image == null) {
					if (imageRegistry == null)
						imageRegistry = JFaceResources.getImageRegistry();
					image = imageRegistry.get(imageId);
				}
				decoration = new FieldDecoration(image, description);
			}
			// Null out all other fields now that the decoration has an image
			description = null;
			imageId = null;
			imageRegistry = null;
			image = null;

			return decoration;
		}
	}

	/**
	 * Default instance of the registry. Applications may install their own
	 * registry.
	 */
	private static FieldDecorationRegistry defaultInstance;

	/**
	 * Maximum width and height used by decorations in this registry. Clients
	 * may use these values to reserve space in dialogs for decorations or to
	 * adjust layouts so that decorated and non-decorated fields line up.
	 */
	private int maxDecorationWidth, maxDecorationHeight;

	private HashMap /* <String id, FieldDecoration> */decorations = new HashMap();

	/**
	 * Get the default FieldDecorationRegistry.
	 * 
	 * @return the singleton FieldDecorationRegistry that is used to manage
	 *         shared field decorations.
	 */
	public static FieldDecorationRegistry getDefault() {
		if (defaultInstance == null)
			defaultInstance = new FieldDecorationRegistry();
		return defaultInstance;
	}

	/**
	 * Set the default FieldDecorationRegistry.
	 * 
	 * @param defaultRegistry
	 *            the singleton FieldDecorationRegistry that is used to manage
	 *            shared field decorations.
	 */
	public static void setDefault(FieldDecorationRegistry defaultRegistry) {
		defaultInstance = defaultRegistry;
	}

	/**
	 * Construct a FieldDecorationRegistry.
	 * 
	 * @param decorationWidth
	 *            the width in pixels reserved for decorations
	 * @param decorationHeight
	 *            the height in pixels reserved for decorations
	 */
	public FieldDecorationRegistry() {
		maxDecorationWidth = 0;
		maxDecorationHeight = 0;
	}

	/**
	 * Get the maximum width (in pixels) of any decoration retrieved so far in
	 * the registry. This value changes as decorations are added and retrieved.
	 * This value can be used by clients to reserve space or otherwise compute
	 * margins when aligning non-decorated fields with decorated fields.
	 * 
	 * @return decorationWidth the maximum width in pixels of any accessed
	 *         decoration
	 */
	public int geMaximumDecorationWidth() {
		return maxDecorationWidth;
	}

	/**
	 * Get the maximum height (in pixels) of any decoration retrieved so far in
	 * the registry. This value changes as decorations are added and retrieved.
	 * This value can be used by clients to reserve space or otherwise compute
	 * margins when aligning non-decorated fields with decorated fields.
	 * 
	 * 
	 * @return decorationHeight the maximum height in pixels of any accessed
	 *         decoration
	 */
	public int getMaximumDecorationHeight() {
		return maxDecorationHeight;
	}

	/**
	 * Registers a field decoration using the specified id. The lifecyle of the
	 * supplied image should be managed by the client. That is, it will never be
	 * disposed by this registry and the decoration should be removed from the
	 * registry if the image is ever disposed elsewhere.
	 * 
	 * @param id
	 *            the String id used to identify and access the decoration.
	 * @param description
	 *            the String description to be used in the decoration.
	 * @param image
	 *            the image to be used in the decoration
	 */
	public void registerFieldDecoration(String id, String description,
			Image image) {
		decorations.put(id, new Entry(description, image));
	}

	/**
	 * Registers a field decoration using the specified id. An image id of an
	 * image located in the default JFaceResources image registry is supplied.
	 * The image will not be created until the decoration is requested.
	 * 
	 * @param id
	 *            the String id used to identify and access the decoration.
	 * @param description
	 *            the String description to be used in the decoration.
	 * @param imageId
	 *            the id of the image in the JFaceResources image registry that
	 *            is used for this decorator
	 */
	public void registerFieldDecoration(String id, String description,
			String imageId) {
		decorations.put(id, new Entry(description, imageId, JFaceResources
				.getImageRegistry()));
	}

	/**
	 * Registers a field decoration using the specified id. An image id and an
	 * image registry are supplied. The image will not be created until the
	 * decoration is requested.
	 * 
	 * @param id
	 *            the String id used to identify and access the decoration.
	 * @param description
	 *            the String description to be used in the decoration.
	 * @param imageId
	 *            the id of the image in the supplied image registry that is
	 *            used for this decorator
	 * @param imageRegistry
	 *            the registry used to obtain the image
	 */
	public void registerFieldDecoration(String id, String description,
			String imageId, ImageRegistry imageRegistry) {
		decorations.put(id, new Entry(description, imageId, imageRegistry));
	}

	/**
	 * Unregisters the field decoration with the specified id. No lifecycle
	 * management is performed on the decoration's image. This message has no
	 * effect if no field decoration with the specified id was previously
	 * registered.
	 * </p>
	 * <p>
	 * This method need not be called if the registered decoration's image is
	 * managed in an image registry. In that case, leaving the decoration in the
	 * registry will do no harm since the image will remain valid and will be
	 * properly disposed when the application is shut down. This method should
	 * be used in cases where the caller intends to dispose of the image
	 * referred to by the decoration, or otherwise determines that the
	 * decoration should no longer be used.
	 * 
	 * @param id
	 *            the String id of the decoration to be unregistered.
	 */
	public void unregisterFieldDecoration(String id) {
		decorations.put(id, null);
	}

	/**
	 * Returns the field decoration registered by the specified id .
	 * 
	 * @param id
	 *            the String id used to access the decoration.
	 * @return the FieldDecoration with the specified id, or <code>null</code>
	 *         if there is no decoration with the specified id.
	 */
	public FieldDecoration getFieldDecoration(String id) {
		Object entry = decorations.get(id);
		if (entry == null)
			return null;
		FieldDecoration dec = ((Entry) entry).getDecoration();
		Image image = dec.getImage();
		if (image != null) {
			maxDecorationHeight = Math.max(0, image.getBounds().height);
			maxDecorationWidth = Math.max(0, image.getBounds().width);
		}
		return dec;
	}
}
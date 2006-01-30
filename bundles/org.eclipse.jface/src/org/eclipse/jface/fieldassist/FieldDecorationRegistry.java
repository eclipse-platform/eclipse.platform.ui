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

import java.text.MessageFormat;
import java.util.HashMap;

import org.eclipse.swt.graphics.Image;

/**
 * FieldDecorationRegistry is a common registry used to define shared field
 * decorations within an application and to specify the reserved dimensions for
 * field decorations used by the application. Unlike resource registries, the
 * FieldDecorationRegistry does not perform any lifecycle management of the
 * decorations. Clients of this registry are responsible for the lifecycle
 * management of images contained in the decorations. Typically, clients use
 * images already defined in the JFace image registry.
 * </p>
 * <p>
 * This API is considered experimental. It is still evolving during 3.2 and is
 * subject to change. It is being released to obtain feedback from early
 * adopters.
 * 
 * @see FieldDecoration
 * @see DecoratedField
 * 
 * @since 3.2
 */
public class FieldDecorationRegistry {

	/**
	 * Default number of pixels to reserve for a decoration's width.
	 */
	private static int RESERVED_WIDTH = 8;

	/**
	 * Default number of pixels to reserve for a decoration's height.
	 */
	private static int RESERVED_HEIGHT = 8;

	/**
	 * Default instance of the registry. Applications may install their own
	 * registry.
	 */
	private static FieldDecorationRegistry defaultInstance;

	/**
	 * Reserved width and height for decorations. The interpretation of this
	 * value is up to each client. For example, decorated fields use the width
	 * to reserve space next to the field, but do not use the height since the
	 * layout adjusts for varying heights.
	 */
	private int reservedDecorationWidth, reservedDecorationHeight;

	private HashMap /* <String id, FieldDecoration> */decorations = new HashMap();

	/**
	 * Get the default FieldDecorationRegistry.
	 * 
	 * @return the singleton FieldDecorationRegistry that is used to manage
	 *         shared field decorations.
	 */
	public static FieldDecorationRegistry getDefault() {
		if (defaultInstance == null)
			defaultInstance = new FieldDecorationRegistry(RESERVED_WIDTH,
					RESERVED_HEIGHT);
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
	 * Construct a FieldDecorationRegistry whose images conform to the specified
	 * reserved width for decorations.
	 * 
	 * @param decorationWidth
	 *            the width in pixels reserved for decorations
	 * @param decorationHeight
	 *            the height in pixels reserved for decorations
	 */
	public FieldDecorationRegistry(int decorationWidth, int decorationHeight) {
		reservedDecorationWidth = decorationWidth;
		reservedDecorationHeight = decorationHeight;
	}

	/**
	 * Get the width (in pixels) that should always be reserved for field
	 * decorations, regardless of the actual width of any supplied decorations.
	 * This value can be used by clients to reserve space or otherwise compute
	 * margins when aligning non-decorated fields with decorated fields.
	 * 
	 * @return decorationWidth the width in pixels reserved for decorations
	 */
	public int getReservedDecorationWidth() {
		return reservedDecorationWidth;
	}

	/**
	 * Get the height (in pixels) that should always be reserved for field
	 * decorations, regardless of the actual height of any supplied decorations.
	 * This value can be used by clients to reserve space or otherwise compute
	 * margins when aligning non-decorated fields with decorated fields.
	 * 
	 * @return decorationHeight the height in pixels reserved for decorations
	 */
	public int getReservedDecorationHeight() {
		return reservedDecorationHeight;
	}

	/**
	 * Adds a field decoration to the registry using the specified id.
	 * 
	 * @param id
	 *            the String id used to access the decoration.
	 * @param decoration
	 *            the FieldDecoration to add. If a decoration of the specified
	 *            id is already registered, it will be replaced with this
	 *            decoration.
	 */
	public void addFieldDecoration(String id, FieldDecoration decoration) {
		decorations.put(id, decoration);
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
		return (FieldDecoration) decorations.get(id);
	}

	/**
	 * Returns the field decoration registered by the specified id, formatting
	 * its description against the supplied arguments.
	 * 
	 * @param id
	 *            the String id used to access the decoration.
	 * @param arguments
	 *            the array of Objects against which the description text is
	 *            formatted
	 * @return a FieldDecoration with the specified id, whose description text
	 *         is formatted with the specified arguments, or <code>null</code>
	 *         if there is no decoration with the specified id.
	 * 
	 * @see MessageFormat
	 */
	public FieldDecoration getFormattedFieldDecoration(String id,
			Object[] arguments) {
		Object dec = decorations.get(id);
		if (dec == null)
			return null;
		String description = ((FieldDecoration)dec).getDescription();
		Image image = ((FieldDecoration)dec).getImage();
		
		// Attempt the format
		description = MessageFormat.format(description, arguments);
		return new FieldDecoration(image, description);
	}
}
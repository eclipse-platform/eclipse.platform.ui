/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare;

import org.eclipse.swt.graphics.Image;

/**
 * Interface for getting the name, image, and type for an object.
 * <p>
 * These methods are typically used to present an input object in the compare UI
 * (<code>getName</code> and <code>getImage</code>)
 * and for finding a viewer for a given input type (<code>getType</code>).
 * <p>
 * Clients may implement this interface.
 */
public interface ITypedElement {

	/**
	 * Type for a folder input. Folders are comparison
	 * elements that have no contents, only a name and children.
	 */
	public static final String FOLDER_TYPE= "FOLDER";

	/**
	 * Type for an element whose actual type is text.
	 */
	public static final String TEXT_TYPE= "txt";

	/**
	 * Type for an element whose actual type could not
	 * be determined.
	 */
	public static final String UNKNOWN_TYPE= "???";

	/**
	 * Returns the name of this object.
	 * The name is used when displaying this object in the UI.
	 *
	 * @return the name of this object
	 */
	String getName();

	/**
	 * Returns an image for this object.
	 * This image is used when displaying this object in the UI.
	 *
	 * @return the image of this object or <code>null</code> if this type of input has no image
	 */
	Image getImage();

	/**
	 * Returns the type of this object. For objects with a file name
	 * this is typically the file extension. For folders its the constant
	 * <code>FOLDER_TYPE</code>.
	 * The type is used for determining a suitable viewer for this object.
	 *
	 * @return the type of this object
	 */
	String getType();
}

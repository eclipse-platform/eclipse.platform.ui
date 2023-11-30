/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import org.eclipse.swt.graphics.Image;

/**
 * IField is the definition of fields for marker views.
 */
public interface IField {

	/**
	 * @return String the description of the field.
	 */
	String getDescription();

	/**
	 * @return the image associated with the description of the field or <code>null</code>.
	 */
	Image getDescriptionImage();

	/**
	 * @return The text to be displayed in the column header for this field.
	 */
	String getColumnHeaderText();

	/**
	 * @return The image to be displayed in the column header for this field or <code>null</code>.
	 */
	Image getColumnHeaderImage();

	/**
	 * @return The String value of the object for this particular field to be displayed to the user.
	 */
	String getValue(Object obj);

	/**
	 * @return The image value of the object for this particular field to be displayed to the user
	 * or <code>null</code>.
	 */
	Image getImage(Object obj);

	/**
	 * @return Either:
	 * <ul>
	 *  <li>a negative number if the value of obj1 is less than the value of obj2 for this field.
	 *  <li><code>0</code> if the value of obj1 and the value of obj2 are equal for this field.
	 *  <li>a positive number if the value of obj1 is greater than the value of obj2 for this field.
	 * </ul>
	 */
	int compare(Object obj1, Object obj2);

	/**
	 * Get the default direction for the receiver. Return either
	 * {@link TableComparator#ASCENDING } or {@link TableComparator#DESCENDING }
	 * @return int
	 */
	int getDefaultDirection();

	/**
	 * Get the preferred width of the receiver.
	 * @return int
	 */
	int getPreferredWidth();

	/**
	 * Return whether not the receiver is showing.
	 * @return boolean
	 */
	boolean isShowing();

	/**
	 * Set whether or not the receiver is showing.
	 */
	void setShowing(boolean showing);


}

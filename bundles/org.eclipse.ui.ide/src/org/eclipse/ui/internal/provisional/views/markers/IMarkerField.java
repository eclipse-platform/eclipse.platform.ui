/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.provisional.views.markers;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.markers.internal.TableComparator;


/**
 * IMarkerField is the definition of a fields for a marker view.
 *
 */
public interface IMarkerField {
	
	/**
	 * Constant to indicate an ascending sort direction.
	 */
	public static final int ASCENDING = 1;
	/**
	 * Constant to indicate an descending sort direction.
	 */
	public static final int DESCENDING = -1;
	
//
//    /**
//     * @return String the description of the field.
//     */
//    String getDescription();
//
//    /**
//     * @return the image associated with the description of the field or <code>null<code>.
//     */
//    Image getDescriptionImage();

    /**
     * @return The text to be displayed in the column header for this field.
     */
    String getColumnHeaderText();
//
//    /**
//     * @return The image to be displayed in the column header for this field or <code>null<code>.
//     */
//    Image getColumnHeaderImage();
//
    /**
     * @param item
     * @return The String value of the object for this particular field to be displayed to the user.  
     */
    String getValue(MarkerItem item);

    /**
     * @param item
     * @return The image value of the object for this particular field to be displayed to the user
     * or <code>null<code>.
     */
    Image getImage(MarkerItem item);

    /**
     * @param item1
     * @param item2
     * @return Either:
     * 	<li>a negative number if the value of item1 is less than the value of item2 for this field.
     *  <li><code>0</code> if the value of item1 and the value of item2 are equal for this field.
     *  <li>a positive number if the value of item1 is greater than the value of item2 for this field.
     */
    int compare(MarkerItem item1, MarkerItem item2);
    
    /**
     * Get the default direction for the receiver. Return either 
     * {@link TableComparator#ASCENDING } or {@link TableComparator#DESCENDING }
     * @return int
     */
   int getDefaultDirection();
   
   /**
    * Get the column weight. A value of 1 indicates that it should be roughly
    * equal to the other columns.
    * @return float
    */
   float getColumnWeight();

//   /**
//    * Return whether not the receiver is showing.
//    * @return boolean
//    */
//   boolean isShowing();
//   
//   /**
//    * Set whether or not the receiver is showing.
//    * @param showing
//    */
//  void setShowing(boolean showing);
   
 
}

/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

/**
 * Extends {@link IViewPart}, adding the name and status text properties.
 * Prior to 3.0, a view's title was often modified to show both the part
 * name and extra status text.  With this interface, the distinction is
 * made more explicit. 
 * 
 * @since 3.0 
 */
public interface IViewPart2 extends IViewPart {

	/**
     * Returns the name of this part. If this value changes the part must fire a
     * property listener event with <code>PROP_TITLE</code>.
     * 
     * @return the name of this view (not null)
     */
	public String getPartName();
	
	/**
     * Returns the status text of this view. An empty string indicates no status
     * text. If this value changes the part must fire a property listener event
     * with <code>PROP_TITLE</code>.
     * 
     * @return the status text of this view (not null)
     */
	public String getStatusText();
	
}

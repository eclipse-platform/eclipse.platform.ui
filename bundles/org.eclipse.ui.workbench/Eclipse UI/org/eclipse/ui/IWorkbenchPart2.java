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
 * Extends {@link IWorkbenchPart}, adding the name and status text properties.
 * Prior to 3.0, a view's title was often modified to show both the part
 * name and extra status text.  With this interface, the distinction is
 * made more explicit. 
 * 
 * @since 3.0 
 */
public interface IWorkbenchPart2 extends IWorkbenchPart {
	/**
     * Returns the name of this part. If this value changes the part must fire a
     * property listener event with {@link IWorkbenchPart#PROP_NAME}.
     * 
     * @return the name of this view, or the empty string if the name is being managed
     * by the workbench (not <code>null</code>)
     */
	public String getPartName();
	
	/**
     * Returns the status text of this view. An empty string indicates no status
     * text. If this value changes the part must fire a property listener event
     * with {@link IWorkbenchPart#PROP_STATUS_TEXT}.
     * 
     * @return the status text of this view (not <code>null</code>)
     */
	public String getStatusText();
	
}

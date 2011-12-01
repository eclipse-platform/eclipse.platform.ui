/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model.provisional;



/**
 * Request monitor used to collect the number of children for an element in a viewer.
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 3.3
 */
public interface IChildrenCountUpdate extends IViewerUpdate {

	/**
	 * Sets the number of children for this update.
	 * 
	 * @param numChildren number of children
	 */
	public void setChildCount(int numChildren);
}

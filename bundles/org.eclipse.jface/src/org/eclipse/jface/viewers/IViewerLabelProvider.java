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
package org.eclipse.jface.viewers;


/**
 * The IViewerLabelProvider  is the interface that allows for an update to be collected by
 * a set of ViewEntrySettings rather than set on a widget directly.
 * @see IDelayedLabelDecorator
 */
public interface IViewerLabelProvider extends IBaseLabelProvider {

	/**
	 * Update the given label with the information derived from the given element.
	 * @param settings The current settings for the element
	 * @param element the element being displayed
	 */
	public void updateLabel(ViewerLabel settings, Object element);
}

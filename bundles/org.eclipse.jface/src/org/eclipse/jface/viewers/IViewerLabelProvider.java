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
 * Extends <code>IBaseLabelProvider</code> with the methods
 * to update the label given element represented by a ViewerLabel.
 * Used by the TreeViewer and TableViewer to support updates of 
 * labels without setting the widgets contents if there is no change.
 * 
 * @see IDelayedLabelDecorator
 * @see ViewerLabel
 * @since 3.0
 */
public interface IViewerLabelProvider extends IBaseLabelProvider {

	/**
	 * Update the given label with the information derived from the 
	 * given element.
	 * @param settings The current ViewerLabel for the element
	 * @param element the element for which to provide the ViewerLabels
	 * entries.
	 */
	public void updateLabel(ViewerLabel settings, Object element);
}

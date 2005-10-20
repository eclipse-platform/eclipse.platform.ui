/***************************************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/
package org.eclipse.ui.navigator;



/**
 * <p>
 * Handles the filtering responsibilities for extensions of the
 * <b>org.eclipse.wst.common.navigator.views.commonFilter </b> extension point.
 * <p>
 * This interface is experimental and is subject to change.
 * </p>
 */
public interface INavigatorExtensionFilter {


	/**
	 * <p>
	 * Returns an array of the objects that should be displayed in the viewer.
	 * </p>
	 * 
	 * @param aViewer
	 *            The instance of {@link CommonViewer}that is requesting the filtering action
	 * @param aParentElement
	 *            The parent object in the tree that will have children specified by theElements
	 * @param theElements
	 *            The original list of elements as returned by the viewer's content provider
	 * @return A subset of theElements that should be displayed in the viewer.
	 */
	Object[] select(CommonViewer aViewer, Object aParentElement, Object[] theElements);

	/**
	 * <p>
	 * Returns true if the child anElement should be displayed within the viewer aViewer.
	 * </p>
	 * 
	 * @param aViewer
	 *            The instance of {@link CommonViewer}that is requesting the filtering action
	 * @param aParentElement
	 *            The parent object in the tree that has the child anElement
	 * @param anElement
	 *            The child under interrogation of the Filter inquisition
	 * @return True if the child anElement should be displayed in the viewer aViewer as a child of
	 *         aParentElement
	 */
	boolean select(CommonViewer aViewer, Object aParentElement, Object anElement);

}
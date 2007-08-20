/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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
 * A viewer input provider allows a view to translate the active debug
 * context into a viewer input (such that the active debug context does
 * not have to be the input to a viewer). Used in conjunction with a
 * {@link ViewerInputService}.
 * <p>
 * Clients may implement this interface.
 * </p>
 * @since 3.4
 * @see IViewerInputUpdate
 * @see ViewerInputService
 */
public interface IViewerInputProvider {
	
	/**
	 * Asynchronously determine the viewer input to the based on the active
	 * debug context and presentation context.
	 * 
	 * @param update provides details about the request and stores the newly 
	 * 	computed viewer input
	 */
	public void update(IViewerInputUpdate update);
	 

}

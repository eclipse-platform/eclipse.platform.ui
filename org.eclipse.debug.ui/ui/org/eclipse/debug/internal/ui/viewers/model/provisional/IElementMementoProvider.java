/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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
 * Used to save and restore viewer selection/expansion state. A memento
 * provider adapter should be available from a viewer input element
 * in order to support viewer state save/restore.
 * 
 * @since 3.3
 */
public interface IElementMementoProvider {
	
	/**
	 * Creates and stores a memento for the element specified in the request.
	 * The request should be cancelled if a memento is not supported for the
	 * specified element or context.
	 * 
	 * @param request specifies element and provides memento store
	 */
	public void encodeElement(IElementMementoRequest request);
	
	/**
	 * Determines if a memento represents the element specified in the request.
	 * 
	 * @param request specifies element and previously created memento
	 */
	public void compareElement(IElementCompareRequest request);

}

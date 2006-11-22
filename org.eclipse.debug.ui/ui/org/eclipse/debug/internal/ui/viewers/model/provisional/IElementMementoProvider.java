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
 * <p>
 * Each element in a viewer may provide its own memento provider. The
 * input element's memento provider is used for elements that do not provide
 * their own.
 * </p>
 * 
 * @since 3.3
 */
public interface IElementMementoProvider {
	
	/**
	 * Creates and stores a mementos for the elements specified in the requests.
	 * A request should be cancelled if a memento is not supported for the
	 * specified element or context.
	 * 
	 * @param requests specifies elements and provides memento stores
	 */
	public void encodeElements(IElementMementoRequest[] requests);
	
	/**
	 * Determines whether mementos represent associated elements specified in the requests.
	 * 
	 * @param requests specifies each element and previously created memento
	 */
	public void compareElements(IElementCompareRequest[] requests);

}

/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.filebuffers;


/**
 * Interface for parties interested in standardized element changes. These
 * changes are:
 * <ul>
 * <li> dirty state changes
 * <li> content replacements
 * <li> moves
 * <li> deletions
 * </ul>
 * The notifications sent to the element state listeners inform about those standardized,
 * abstract changes. The concrete change applied might differ from the one the listeners
 * are notified about, but should be interpreted as the one the listeners receive.
 */
public interface IElementStateListener2 {
	
	/**
	 * Notifies that the dirty state of the given element has changed.
	 *
	 * @param element the element
	 * @param isDirty the new dirty state
	 */
	void elementDirtyStateChanged(Object element, boolean isDirty);
	
	/**
	 * Notifies that the content of the given element is about to be replaced.
	 *
	 * @param element the element
	 */
	void documentContentAboutToBeReplaced(Object element);
	
	/**
	 * Notifies that the content of the given element has been replaced.
	 *
	 * @param element the element
	 */
	void documentContentReplaced(Object element);

	/**
	 * Notifies that the element has moved. If <code>movedElement</code>
	 * is <code>null</code> it is similar to <code>elementDeleted(originalElement)</code>.
	 *
	 * @param originalElement the element before the move
	 * @param movedElement the element after the move
	 */
	void elementMoved(Object originalElement, Object movedElement);
	
	/**
	 * Notifies that the given element has been deleted.
	 *
	 * @param element the element
	 */
	void elementDeleted(Object element);
	
	/**
	 * Notifies that the state validation of the given element has changed.
	 *
	 * @param element the element
	 * @param isStateValidated the flag indicating whether state validation is done
	 */
	void elementStateValidationChanged(Object element, boolean isStateValidated);
	
	/**
	 * Notifies that the given element is currently being changed. This method may
	 * be sent from a non-ui thread.
	 * 
	 * @param element the element
	 */
	void elementStateChanging(Object element);
	
	/**
	 * Notifies that changing the given element has failed.
	 * 
	 * @param element the element
	 */
	void elementStateChangeFailed(Object element);
}

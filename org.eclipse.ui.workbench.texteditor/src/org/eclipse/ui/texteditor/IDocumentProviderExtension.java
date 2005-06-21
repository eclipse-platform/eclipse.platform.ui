/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.texteditor;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;


/**
 * Extension interface for {@link IDocumentProvider}. It adds the following
 * functions:
 * <ul>
 * <li> dealing with immutable domain elements
 * <li> state validation
 * <li> persistent status of domain element operations
 * <li> extended synchronization support
 * </ul>
 * @since 2.0
 */
public interface IDocumentProviderExtension {

	/**
	 * Returns whether the document provider thinks that the given element is read-only.
	 * If this method returns <code>true</code>, <code>saveDocument</code> could fail.
	 * This method does not say anything about the document constructed from the given
	 * element. If the given element is not connected to this document provider, the return
	 * value is undefined. Document providers are allowed to use a cache to answer this
	 * question, i.e. there can be a difference between the "real" state of the element and
	 * the return value.
	 *
	 * @param element the element
	 * @return <code>true</code> if the given element is read-only, <code>false</code> otherwise
	 */
	boolean isReadOnly(Object element);

	/**
	 * Returns whether the document provider thinks that the given element can persistently be modified.
	 * This is orthogonal to <code>isReadOnly</code> as read-only elements may be modifiable and
	 * writable elements may not be modifiable. If the given element is not connected to this document
	 * provider, the result is undefined. Document providers are allowed to use a cache to answer this
	 * question, i.e. there can be a difference between the "real" state of the element and the return
	 * value.
	 *
	 * @param element the element
	 * @return <code>true</code> if the given element is modifiable, <code>false</code> otherwise
	 */
	boolean isModifiable(Object element);

	/**
	 * Validates the state of the given element. This method  may change the "real" state of the
	 * element. If using, it also updates the internal caches, so that this method may also change
	 * the results returned by <code>isReadOnly</code> and <code>isModifiable</code>. If the
	 * given element is not connected to this document provider, the effect is undefined.
	 *
	 * @param element the element
	 * @param computationContext the context in which the computation is performed, e.g., a SWT shell
	 * @exception CoreException if validating fails
	 */
	void validateState(Object element, Object computationContext) throws CoreException;

	/**
	 * Returns whether the state of the given element has been validated.
	 *
	 * @param element the element
	 * @return <code>true</code> if the state has been validated
	 */
	boolean isStateValidated(Object element);

	/**
	 * Updates the state cache for the given element. This method may change the result returned
	 * by <code>isReadOnly</code> and <code>isModifiable</code>. If the given element is not
	 * connected to this document provider, the effect is undefined.
	 *
	 * @param element the element
	 * @exception CoreException if validating fails
	 */
	void updateStateCache(Object element) throws CoreException;

	/**
	 * Marks the document managed for the given element as savable. I.e.
	 * <code>canBeSaved(element)</code> will return <code>true</code>
	 * afterwards.
	 *
	 * @param element the element
	 */
	void setCanSaveDocument(Object element);

	/**
	 * Returns the status of the given element.
	 *
	 * @param element the element
	 * @return the status of the given element
	 */
	IStatus getStatus(Object element);

	/**
	 * Synchronizes the document provided for the given element with the
	 * given element. After that call <code>getSynchronizationTimeStamp</code>
	 * and <code>getModificationTimeStamp</code> return the same value.
	 *
	 * @param element the element
	 * @exception CoreException  if the synchronization could not be performed
	 */
	void synchronize(Object element) throws CoreException;
}

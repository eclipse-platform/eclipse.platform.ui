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




import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.jface.text.IDocument;



/**
 * A document provider maps between domain elements and documents.
 * A document provider has the following responsibilities:
 * <ul>
 * <li> create an annotation model of a domain model element
 * <li> create and manage a textual representation, i.e., a document, of a domain model element
 * <li> create and save the content of domain model elements based on given documents
 * <li> update the documents this document provider manages for domain model elements 
 *			to changes directly applied to those domain model elements
 * <li> notify all element state listeners about changes directly applied to domain model
 *			elements this document provider manages a document for, i.e. the document 
 *			provider must know which changes of a domain model element are to be interpreted 
 *			as element moves, deletes, etc.
 * </ul>
 * Text editors use document providers to bridge the gap between their input elements and the
 * documents they work on. A single document provider may be shared between multiple editors;
 * the methods take the editors' input elements as a parameter.
 * <p>
 * This interface may be implemented by clients; or subclass the standard 
 * abstract base class <code>AbstractDocumentProvider</code>.</p>
 *
 * @see org.eclipse.jface.text.IDocument
 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider
 */
public interface IDocumentProvider2 {
	
	/**
	 * Adds the given element state listener to this document provider.
	 * Has no effect if an identical listener is already registered.
	 *
	 * @param listener the listener
	 */
	void addElementStateListener(IElementStateListener2 listener);
	
	/**
	 * Removes the given element state listener from this document provider.
	 * Has no affect if an identical listener is not registered.
	 *
	 * @param listener the listener
	 */
	void removeElementStateListener(IElementStateListener2 listener);


	/**
	 * Connects the given element to this document provider. This tells the provider
	 * that caller of this method is interested to work with the document provided for
	 * the given domain model element. By counting the invokations of this method and 
	 * <code>disconnect(Object)</code> this provider can assume to know the
	 * correct number of clients working with the document provided for that 
	 * domain model element. <p>
	 * The given element must not be <code>null</code>.
	 *
	 * @param element the element
	 * @exception CoreException if the textual representation or the annotation model
	 *		of the element could not be created
	 */
	void connect(Object element) throws CoreException;
	
	/**
	 * Disconnects the given element from this document provider. This tells the provider
	 * that the caller of this method is no longer interested in working with the document
	 * provided for the given domain model element. By counting the invokations of 
	 * <code>connect(Object)</code> and of this method this provider can assume to
	 * know the correct number of clients working with the document provided for that 
	 * domain model element. <p>
	 * The given element must not be <code>null</code>.
	 *
	 * @param element the element
	 */
	void disconnect(Object element);
	
	/**
	 * Returns the document for the given element. Usually the document contains
	 * a textual presentation of the content of the element, or is the element itself.
	 *
	 * @param element the element, or <code>null</code>
	 * @return the document, or <code>null</code> if none
	 */
	IDocument getDocument(Object element);
	
	/**
	 * Creates a new empty document for the given element. The document is afterwards not
	 * managed by this provider.
	 * 
	 * @param element the element
	 * @return the newly created document, or <code>null</code> if this is not possible
	 */
	IDocument createEmptyDocument(Object element);
	
	/**
	 * Returns the element that is the origin of the given document. 
	 * 
	 * @param document the given document
	 * @return the element or <code>null</code> if none
	 */
	Object getElement(IDocument document);
	
	/**
	 * Resets the given element's document to its last saved state.
	 * Element state listeners are notified both before (<code>elementContentAboutToBeReplaced</code>)
	 * and after (<code>elementContentReplaced</code>) the content is changed.
	 *
	 * @param element the element, or <code>null</code>
	 */
	void restoreDocument(Object element) throws CoreException;
	
	/**
	 * Saves the given document provided for the given element.
	 *
	 * @param monitor a progress monitor to report progress and request cancelation
	 * @param element the element, or <code>null</code>
	 * @param document the document
	 * @param overwrite indicates whether overwrite should be performed 
	 * 			while saving the given element if necessary
	 * @exception CoreException if document could not be stored to the given element
	 */
	void saveDocument(Object element, boolean overwrite) throws CoreException;
		
	/**
	 * Returns whether the document provided for the given element must be saved.
	 *
	 * @param element the element, or <code>null</code>
	 * @return <code>true</code> if the document must be saved, and
	 *   <code>false</code> otherwise (including the element is <code>null</code>)
	 */
	boolean mustSaveDocument(Object element);
	
	/**
	 * Returns whether the document provided for the given element differs from
	 * its original state which would required that it be saved.
	 *
	 * @param element the element, or <code>null</code>
	 * @return <code>true</code> if the document can be saved, and
	 *   <code>false</code> otherwise (including the element is <code>null</code>)
	 */
	boolean canSaveDocument(Object element);

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
	 * Returns the status of the given element.
	 * 
	 * @param element the element
	 * @return the status of the given element
	 */
	IStatus getStatus(Object element);
	
	/**
	 * Sets this providers progress monitor.
	 * @param progressMonitor
	 */
	void setProgressMonitor(IProgressMonitor progressMonitor);
	
	/**
	 * Returns this providers progress monitor.
	 * @return IProgressMonitor
	 */
	IProgressMonitor getProgressMonitor();
}

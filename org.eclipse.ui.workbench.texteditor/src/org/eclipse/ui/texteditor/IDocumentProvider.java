/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;

/**
 * A document provider maps between domain elements and documents. A document provider has the
 * following responsibilities:
 * <ul>
 * <li>create an annotation model of a domain model element
 * <li>create and manage a textual representation, i.e., a document, of a domain model element
 * <li>create and save the content of domain model elements based on given documents
 * <li>update the documents this document provider manages for domain model elements to changes
 * directly applied to those domain model elements
 * <li>notify all element state listeners about changes directly applied to domain model elements
 * this document provider manages a document for, i.e. the document provider must know which changes
 * of a domain model element are to be interpreted as element moves, deletes, etc.
 * </ul>
 * Text editors use document providers to bridge the gap between their input elements and the
 * documents they work on. A single document provider may be shared between multiple editors; the
 * methods take the editors' input elements as a parameter.
 * <p>
 * This interface may be implemented by clients; or subclass the standard abstract base class
 * <code>AbstractDocumentProvider</code>.
 * </p>
 * <p>
 * In order to provided backward compatibility for clients of <code>IDocumentProvider</code>,
 * extension interfaces are used to provide a means of evolution. The following extension interfaces
 * exist:
 * <ul>
 * <li>{@link org.eclipse.ui.texteditor.IDocumentProviderExtension} since version 2.0 introducing
 * 		state validation, extended read-only handling and synchronization.</li>
 * <li>{@link org.eclipse.ui.texteditor.IDocumentProviderExtension2} since version 2.1 introducing
 * 		adding support for a global progress monitor.</li>
 * <li>{@link org.eclipse.ui.texteditor.IDocumentProviderExtension3} since version 3.0 adding
 * 		a predicate for querying synchronization state.</li>
 * <li>{@link org.eclipse.ui.texteditor.IDocumentProviderExtension4} since version 3.1 adding
 * 		a predicate for querying an element's the content description.</li>
 * <li>{@link org.eclipse.ui.texteditor.IDocumentProviderExtension5} since version 3.2 adding
 * 		the ability to detect a non-synchronized exception.</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.jface.text.IDocument
 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider
 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension
 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension2
 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension3
 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension4
 * @see org.eclipse.ui.texteditor.IDocumentProviderExtension5
 */
public interface IDocumentProvider {

	/**
	 * Connects the given element to this document provider. This tells the provider
	 * that caller of this method is interested to work with the document provided for
	 * the given domain model element. By counting the invocations of this method and
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
	 * provided for the given domain model element. By counting the invocations of
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
	 * Resets the given element's document to its last saved state.
	 * Element state listeners are notified both before (<code>elementContentAboutToBeReplaced</code>)
	 * and after (<code>elementContentReplaced</code>) the content is changed.
	 *
	 * @param element the element, or <code>null</code>
	 * @exception CoreException if document could not be reset for the given element
	 */
	void resetDocument(Object element) throws CoreException;

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
	void saveDocument(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite) throws CoreException;

	/**
	 * Returns the modification stamp of the given element.
	 *
	 * @param element the element
	 * @return the modification stamp of the given element
	 */
	long getModificationStamp(Object element);

	/**
	 * Returns the time stamp of the last synchronization of the given element and its provided
	 * document.
	 * 
	 * @param element the element
	 * @return the synchronization stamp of the given element
	 */
	long getSynchronizationStamp(Object element);

	/**
	 * Returns whether the given element has been deleted.
	 *
	 * @param element the element
	 * @return <code>true</code> if the element has been deleted
	 */
	boolean isDeleted(Object element);

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
	 * Returns the annotation model for the given element.
	 *
	 * @param element the element, or <code>null</code>
	 * @return the annotation model, or <code>null</code> if none
	 */
	IAnnotationModel getAnnotationModel(Object element);

	/**
	 * Informs this document provider about upcoming changes of the given element.
	 * The changes might cause change notifications specific for the type of the given element.
	 * If this provider manages a document for the given element, the document provider
	 * must not change the document because of the notifications received after <code>
	 * aboutToChange</code> has been and before <code>changed</code> is called. In this case,
	 * it is assumed that the document is already up to date, e.g., a save operation is a
	 * typical case. <p>
	 * The concrete nature of the change notification depends on the concrete type of the
	 * given element. If the element is, e.g., an <code>IResource</code> the notification
	 * is a resource delta.
	 *
	 * @param element the element, or <code>null</code>
	 */
	void aboutToChange(Object element);

	/**
	 * Informs this document provider that the given element has been changed.
	 * All notifications have been sent out. If this provider manages a document
	 * for the given element, the document provider  must from now on change the
	 * document on the receipt of change notifications. The concrete nature of the change
	 * notification depends on the concrete type of the given element. If the element is,
	 * e.g., an <code>IResource</code> the notification is a resource delta.
	 *
	 * @param element the element, or <code>null</code>
	 */
	void changed(Object element);

	/**
	 * Adds the given element state listener to this document provider.
	 * Has no effect if an identical listener is already registered.
	 *
	 * @param listener the listener
	 */
	void addElementStateListener(IElementStateListener listener);

	/**
	 * Removes the given element state listener from this document provider.
	 * Has no effect if an identical listener is not registered.
	 *
	 * @param listener the listener
	 */
	void removeElementStateListener(IElementStateListener listener);
}

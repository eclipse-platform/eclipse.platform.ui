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

package org.eclipse.jface.text;

/**
 * Slave documents are documents whose contents is defined in terms of a master
 * document. Thus, slave documents usually reflect a projection of the master document.
 * Slave documents are causally connected to the master document. This means, changes
 * of the master document have immediate effect on the slave document and vice versa.
 * <p>
 * A slave document manager creates slave documents for given master documents, manages the
 * life cycle of the slave documents, and keeps track of the information flow between
 * master and slave documents. The slave document manager defines the construction rules of the
 * slave documents in terms of the master document.</p>
 * <p>
* In order to provided backward compatibility for clients of <code>ISlaveDocumentManager</code>, extension
 * interfaces are used to provide a means of evolution. The following extension interfaces
 * exist:
 * <ul>
 * <li> {@link org.eclipse.jface.text.ISlaveDocumentManagerExtension} since version 3.0 extending the protocol
 *      with an access to all managed slave document for a given master document. </li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.jface.text.IDocument
 * @since 2.1
 */
public interface ISlaveDocumentManager {

	/**
	 * Creates a new slave document for the given master document. The slave document
	 * is causally connected to its master document until <code>freeSlaveDocument</code>
	 * is called. The connection between the newly created slave document and the master
	 * document is managed by this slave document manager.
	 *
	 * @param master the master document
	 * @return the newly created slave document
	 * @see #freeSlaveDocument(IDocument)
	 */
	IDocument createSlaveDocument(IDocument master);

	/**
	 * Frees the given slave document. If the given document is not a slave document known
	 * to this slave document manager, this call does not have any effect. A slave
	 * document is known to this slave document manager if it has been created by
	 * this manager using <code>createSlaveDocument</code>.
	 *
	 * @param slave the slave document to be freed
	 * @see #createSlaveDocument(IDocument)
	 */
	void freeSlaveDocument(IDocument slave);

	/**
	 * Creates a new document information mapping between the given slave document and
	 * its master document. Returns <code>null</code> if the given document is unknown
	 * to this slave document manager.
	 *
	 * @param slave the slave document
	 * @return a document information mapping between the slave document and its master document or
	 * 		<code>null</code>
	 */
	IDocumentInformationMapping createMasterSlaveMapping(IDocument slave);

	/**
	 * Returns the master document of the given slave document or <code>null</code> if the
	 * given document is unknown to this slave document manager.
	 *
	 * @param slave the slave document
	 * @return the master document of the given slave document or <code>null</code>
	 */
	IDocument getMasterDocument(IDocument slave);

	/**
	 * Returns whether the given document is a slave document known to this slave document manager. A slave document
	 * is known to this slave document manager, if the document has been created by this manager.
	 *
	 * @param document the document to be checked whether it is a slave document known to this manager
	 * @return <code>true</code> if the document is a slave document, <code>false</code> otherwise
	 */
	boolean isSlaveDocument(IDocument document);

	/**
	 * Sets the given slave document's auto expand mode. In auto expand mode, a slave document is
	 * automatically adapted to reflect all changes applied to its master document. Assume a master
	 * document contains 30 lines and the slave is defined to contain the lines 11-20. In auto
	 * expand mode, when the master document is changed at line 8, the slave document is expanded to
	 * contain the lines 8-20.
	 * <p>
	 * This call is without effect if the given document is unknown to this slave document manager.
	 * 
	 * @param slave the slave whose auto expand mode should be set
	 * @param autoExpand <code>true</code> for auto expand, <code>false</code> otherwise
	 */
	void setAutoExpandMode(IDocument slave, boolean autoExpand);
}

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
package org.eclipse.core.filebuffers;


import org.eclipse.jface.text.IDocument;


/**
 * Participates in the setup of a text file buffer document.
 * <p>
 * This interface is the expected interface for extensions provided for the
 * <code>"org.eclipse.core.filebuffers.documentSetup"</code> extension point.
 * <p>
 * Participants have to be aware of the existence of other participants. I.e.
 * they should always setup a document in a way that does not interfere with
 * others. E.g., when a participant wants to install partitioning on the
 * document, it must use the
 * {@link org.eclipse.jface.text.IDocumentExtension3} API and choose a unique
 * partitioning id.
 *
 * @since 3.0
 * @see org.eclipse.jface.text.IDocumentExtension3
 */
public interface IDocumentSetupParticipant {

	/**
	 * Sets up the document to be ready for use by a text file buffer.
	 *
	 * @param document the document to be set up
	 */
	void setup(IDocument document);
}

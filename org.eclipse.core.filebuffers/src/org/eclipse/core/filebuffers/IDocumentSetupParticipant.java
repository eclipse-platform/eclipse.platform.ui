/**********************************************************************
Copyright (c) 2000, 2003 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.core.filebuffers;

import org.eclipse.jface.text.IDocument;

/**
 * Participates in the setup of a text file buffer document. Participants have
 * to be aware of the existence of other participants. I.e. should always setup
 * a document in a way that does not interfere with others. E.g., when a
 * participant wants to install partitioning on the document, it should use the
 * <code>IDocumentExtension3</code> API and choose a unique partitioning id.
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

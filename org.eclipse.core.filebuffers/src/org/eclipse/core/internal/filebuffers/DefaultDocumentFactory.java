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
package org.eclipse.core.internal.filebuffers;

import org.eclipse.core.filebuffers.IDocumentFactory;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

/**
 * The default document factory.
 */
public class DefaultDocumentFactory implements IDocumentFactory {

	public DefaultDocumentFactory() {
	}

	/*
	 * @see org.eclipse.core.filebuffers.IDocumentFactory#createDocument()
	 */
	public IDocument createDocument() {
		return new Document();
	}
}

/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.editor.text;

import org.eclipse.core.filebuffers.IDocumentFactory;
import org.eclipse.jface.text.IDocument;

/**
 * The document factory for Ant UI
 */
public class AntDocumentFactory  implements IDocumentFactory {

	public AntDocumentFactory() {
	}
		
	/*
	 * @see org.eclipse.core.filebuffers.IDocumentFactory#createDocument()
	 */
	public IDocument createDocument() {
		return new PartiallySynchronizedDocument();
	}
}

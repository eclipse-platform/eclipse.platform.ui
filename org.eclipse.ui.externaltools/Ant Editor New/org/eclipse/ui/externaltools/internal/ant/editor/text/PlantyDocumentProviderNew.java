/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.ui.externaltools.internal.ant.editor.text;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;

/**
 * PlantyDocumentProvider.java
 */
public class PlantyDocumentProviderNew extends PlantyDocumentProvider {

	public PlantyDocumentProviderNew() {
		super();
	}

	public IDocument createDocument(Object element) throws CoreException {
		PartiallySynchronizedDocument document;
		if (element instanceof IEditorInput) {			
			document= new PartiallySynchronizedDocument();
			if (element instanceof IFileEditorInput){
				IFile file= ((IFileEditorInput)element).getFile();
				document.setLocation(file.getLocation());
			}
			
			if (setDocumentContent(document, (IEditorInput) element, getEncoding(element))) {
				initializeDocument(document);
			}
		} else {
			document= null;
		}
		return document;
	}
	
	private void initializeDocument(IDocument document) {
		IDocumentPartitioner partitioner= createDocumentPartitioner();
		document.setDocumentPartitioner(partitioner);
		partitioner.connect(document);
	}

}

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
package org.eclipse.ui.examples.javaeditor;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.DefaultPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.examples.javaeditor.java.JavaPartitionScanner;

/** 
 * The JavaDocumentProvider provides the IDocuments used by java editors.
 */

public class JavaDocumentProvider extends FileDocumentProvider {

	private final static String[] TYPES= new String[] { JavaPartitionScanner.JAVA_DOC, JavaPartitionScanner.JAVA_MULTILINE_COMMENT };

	private static JavaPartitionScanner fgScanner= null;

	public JavaDocumentProvider() {
		super();
	}
	
	/* (non-Javadoc)
	 * Method declared on AbstractDocumentProvider
	 */
	 protected IDocument createDocument(Object element) throws CoreException {
		IDocument document= super.createDocument(element);
		if (document != null) {
			IDocumentPartitioner partitioner= createJavaPartitioner();
			document.setDocumentPartitioner(partitioner);
			partitioner.connect(document);
		}
		return document;
	}
	
	/**
	 * Return a partitioner for .java files.
	 */
	 private IDocumentPartitioner createJavaPartitioner() {
		return new DefaultPartitioner(getJavaPartitionScanner(), TYPES);
	}
	
	/**
	 * Return a scanner for creating java partitions.
	 */
	 private JavaPartitionScanner getJavaPartitionScanner() {
		if (fgScanner == null)
			fgScanner= new JavaPartitionScanner();
		return fgScanner;
	}
}

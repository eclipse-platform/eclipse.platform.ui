package org.eclipse.ui.examples.javaeditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.DefaultPartitioner;
import org.eclipse.jface.text.rules.RuleBasedPartitioner;
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

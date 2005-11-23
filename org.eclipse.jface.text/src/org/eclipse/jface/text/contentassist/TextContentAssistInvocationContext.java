/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.contentassist;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;

import org.eclipse.jface.contentassist.ContentAssistInvocationContext;

/**
 * A content assist invocation context for documents. The context knows the
 * document, the invocation offset and can lazily compute the identifier
 * prefix preceding the invocation offset. It may know the viewer.
 * <p>
 * Clients may instantiate and subclass.
 * </p>
 * <p>
 * XXX this API is provisional and may change anytime during the course of 3.2
 * </p>
 * 
 * @since 3.2
 */
public class TextContentAssistInvocationContext extends ContentAssistInvocationContext {
	
	/* state */
	private final ITextViewer fViewer;
	private final IDocument fDocument;
	private final int fOffset;
	
	/* cached additional info */
	private CharSequence fPrefix;
	
	/**
	 * Equivalent to
	 * {@linkplain #TextContentAssistInvocationContext(ITextViewer, int) TextContentAssistInvocationContext(viewer, viewer.getSelectedRange().x)}.
	 * 
	 * @param viewer the text viewer that content assist is invoked in
	 */
	public TextContentAssistInvocationContext(ITextViewer viewer) {
		this(viewer, viewer.getSelectedRange().x);
	}

	/**
	 * Creates a new context for the given viewer and offset.
	 * 
	 * @param viewer the text viewer that content assist is invoked in
	 * @param offset the offset into the viewer's document where content assist is invoked at
	 */
	public TextContentAssistInvocationContext(ITextViewer viewer, int offset) {
		Assert.isNotNull(viewer);
		fViewer= viewer;
		fDocument= null;
		fOffset= offset;
	}
	
	protected TextContentAssistInvocationContext() {
		fDocument= null;
		fViewer= null;
		fOffset= -1;
	}
	
	/**
	 * Creates a new context for the given document and offset.
	 * 
	 * @param document the document that content assist is invoked in
	 * @param offset the offset into the document where content assist is invoked at
	 */
	public TextContentAssistInvocationContext(IDocument document, int offset) {
		Assert.isNotNull(document);
		Assert.isTrue(offset >= 0);
		fViewer= null;
		fDocument= document;
		fOffset= offset;
	}
	
	/**
	 * Returns the invocation offset.
	 * 
	 * @return the invocation offset
	 */
	public final int getInvocationOffset() {
		return fOffset;
	}
	
	/**
	 * Returns the viewer, <code>null</code> if not available.
	 * 
	 * @return the viewer, possibly <code>null</code>
	 */
	public final ITextViewer getViewer() {
		return fViewer;
	}
	
	/**
	 * Returns the document that content assist is inoked on, or <code>null</code> if not known.
	 * 
	 * @return the document or <code>null</code>
	 */
	public IDocument getDocument() {
		if (fDocument == null) {
			if (fViewer == null)
				return null;
			return fViewer.getDocument();
		}
		return fDocument;
	}
	
	/**
	 * Computes the identifier (as specified by {@link Character#isJavaIdentifierPart(char)}) that
	 * immediately precedes the invocation offset.
	 * 
	 * @return the prefix preceding the content assist invocation offset, <code>null</code> if there is no document
	 * @throws BadLocationException if accessing the document fails
	 */
	public CharSequence computeIdentifierPrefix() throws BadLocationException {
		if (fPrefix == null) {
			IDocument document= getDocument();
			if (document == null)
				return null;
			int end= getInvocationOffset();
			int start= end;
			while (--start >= 0) {
				if (!Character.isJavaIdentifierPart(document.getChar(start)))
					break;
			}
			start++;
			fPrefix= document.get(start, end - start);
		}
		
		return fPrefix;
	}
	
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		TextContentAssistInvocationContext other= (TextContentAssistInvocationContext) obj;
		return (fViewer == null && other.fViewer == null || fViewer.equals(other.fViewer)) && fOffset == other.fOffset && (fDocument == null && other.fDocument == null || fDocument.equals(other.fDocument));
	}
	
	/*
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return super.hashCode() << 5 | (fViewer == null ? 0 : fViewer.hashCode() << 3) | fOffset;
	}
}

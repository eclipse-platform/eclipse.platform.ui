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
 * A content assist invocation context for text viewers. The context knows the
 * viewer, the invocation offset and can lazily compute the identifier
 * prefix preceding the invocation offset.
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
	 * Returns the viewer.
	 * 
	 * @return the viewer
	 */
	public final ITextViewer getViewer() {
		return fViewer;
	}
	
	/**
	 * Shortcut for <code>getViewer().getDocument()</code>.
	 * 
	 * @return the viewer's document
	 */
	public IDocument getDocument() {
		return getViewer().getDocument();
	}
	
	/**
	 * Computes the identifier (as specified by {@link Character#isJavaIdentifierPart(char)}) that
	 * immediately precedes the invocation offset.
	 * 
	 * @return the prefix preceding the content assist invocation offset
	 * @throws BadLocationException if accessing the document fails
	 */
	public CharSequence computeIdentifierPrefix() throws BadLocationException {
		if (fPrefix == null) {
			IDocument document= getDocument();
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
		return fViewer.equals(other.fViewer) && fOffset == other.fOffset;
	}
	
	/*
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return super.hashCode() << 5 | fViewer.hashCode() << 3 | fOffset;
	}
}

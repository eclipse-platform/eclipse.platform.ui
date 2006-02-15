/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.text.source;

import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.source.ISourceViewer;


/**
 * Text quick assist invocation context.
 * 
 * @since3.2
 */
public final class TextInvocationContext implements IQuickAssistInvocationContext {

	private ISourceViewer fSourceViewer;
	private int fOffset;
	private int fLength;
	
	public TextInvocationContext(ISourceViewer sourceViewer, int offset, int length) {
		fSourceViewer= sourceViewer;
		fOffset= offset;
		fLength= length;
	}

	/*
	 * @see org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext#getOffset()
	 */
	public int getOffset() {
		return fOffset;
	}

	/*
	 * @see org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext#getLength()
	 */
	public int getLength() {
		return fLength;
	}

	/*
	 * @see org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext#getSourceViewer()
	 */
	public ISourceViewer getSourceViewer() {
		return fSourceViewer;
	}
	
}

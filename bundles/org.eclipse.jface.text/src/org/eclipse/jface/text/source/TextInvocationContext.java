/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;

import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;


/**
 * Text quick assist invocation context.
 * <p>
 * Clients may extend this class to add additional context information.
 * </p>
 *
 * @since 3.3
 */
public class TextInvocationContext implements IQuickAssistInvocationContext {

	private ISourceViewer fSourceViewer;
	private int fOffset;
	private int fLength;

	public TextInvocationContext(ISourceViewer sourceViewer, int offset, int length) {
		fSourceViewer= sourceViewer;
		fOffset= offset;
		fLength= length;
	}

	@Override
	public int getOffset() {
		return fOffset;
	}

	@Override
	public int getLength() {
		return fLength;
	}

	@Override
	public ISourceViewer getSourceViewer() {
		return fSourceViewer;
	}

}

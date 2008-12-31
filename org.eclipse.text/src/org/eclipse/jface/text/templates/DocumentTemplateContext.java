/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.templates;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

/**
 * Instances of this class describe the context of a template as a region of
 * a document. That region may be either specified by its offset and length, or
 * by a <code>Position</code> which may or may not be registered with the
 * document.
 * <p>
 * Clients may instantiate and extend this class.
 * </p>
 *
 * @since 3.0
 */
public class DocumentTemplateContext extends TemplateContext {

	/** The text of the document. */
	private final IDocument fDocument;
	/**
	 * The region of the document described by this context. We store a
	 * position since clients may specify the document region as (updateable)
	 * Positions.
	 */
	private final Position fPosition;
	/**
	 * The original offset of this context. Will only be updated by the setter
	 * method.
	 */
	private int fOriginalOffset;
	/**
	 * The original length of this context. Will only be updated by the setter
	 * method.
	 */
	private int fOriginalLength;

	/**
	 * Creates a document template context.
	 *
	 * @param type the context type
	 * @param document the document this context applies to
	 * @param offset the offset of the document region
	 * @param length the length of the document region
	 */
	public DocumentTemplateContext(TemplateContextType type, IDocument document, int offset, int length) {
		this(type, document, new Position(offset, length));
	}

	/**
	 * Creates a document template context. The supplied <code>Position</code>
	 * will be queried to compute the <code>getStart</code> and
	 * <code>getEnd</code> methods, which will therefore answer updated
	 * position data if it is registered with the document.
	 *
	 * @param type the context type
	 * @param document the document this context applies to
	 * @param position the position describing the area of the document which
	 *        forms the template context
	 * @since 3.1
	 */
	public DocumentTemplateContext(TemplateContextType type, IDocument document, Position position) {
		super(type);

		Assert.isNotNull(document);
		Assert.isNotNull(position);
		Assert.isTrue(position.getOffset() <= document.getLength());

		fDocument= document;
		fPosition= position;
		fOriginalOffset= fPosition.getOffset();
		fOriginalLength= fPosition.getLength();
	}

	/**
	 * Returns the document.
	 *
	 * @return the document
	 */
	public IDocument getDocument() {
		return fDocument;
	}

	/**
	 * Returns the completion offset within the string of the context.
	 *
	 * @return the completion offset within the string of the context
	 */
	public int getCompletionOffset() {
		return fOriginalOffset;
	}

	/**
	 * Sets the completion offset.
	 *
	 * @param newOffset the new completion offset
	 */
	protected void setCompletionOffset(int newOffset) {
		fOriginalOffset= newOffset;
		fPosition.setOffset(newOffset);
	}

	/**
	 * Returns the completion length within the string of the context.
	 *
	 * @return the completion length within the string of the context
	 */
	public int getCompletionLength() {
		return fOriginalLength;
	}

	/**
	 * Sets the completion length.
	 *
	 * @param newLength the new completion length
	 */
	protected void setCompletionLength(int newLength) {
		fOriginalLength= newLength;
		fPosition.setLength(newLength);
	}

	/**
	 * Returns the keyword which triggered template insertion.
	 *
	 * @return the keyword which triggered template insertion
	 */
	public String getKey() {
		int offset= getStart();
		int length= getEnd() - offset;
		try {
			return fDocument.get(offset, length);
		} catch (BadLocationException e) {
			return ""; //$NON-NLS-1$
		}
	}

	/**
	 * Returns the beginning offset of the keyword.
	 *
	 * @return the beginning offset of the keyword
	 */
	public int getStart() {
		return fPosition.getOffset();
	}

	/**
	 * Returns the end offset of the keyword.
	 *
	 * @return the end offset of the keyword
	 */
	public int getEnd() {
		return fPosition.getOffset() + fPosition.getLength();
	}

	/*
	 * @see org.eclipse.jface.text.templates.TemplateContext#canEvaluate(org.eclipse.jface.text.templates.Template)
	 */
	public boolean canEvaluate(Template template) {
		return true;
	}

	/*
	 * @see org.eclipse.jface.text.templates.TemplateContext#evaluate(org.eclipse.jface.text.templates.Template)
	 */
	public TemplateBuffer evaluate(Template template) throws BadLocationException, TemplateException {
		if (!canEvaluate(template))
			return null;

		TemplateTranslator translator= new TemplateTranslator();
		TemplateBuffer buffer= translator.translate(template);

		getContextType().resolve(buffer, this);

		return buffer;
	}
}

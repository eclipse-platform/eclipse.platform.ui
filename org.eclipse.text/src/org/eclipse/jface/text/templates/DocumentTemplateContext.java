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
package org.eclipse.jface.text.templates;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * A typical text based document template context.
 * 
 * @since 3.0
 */
public class DocumentTemplateContext extends TemplateContext {

	/** The text of the document. */
	private final IDocument fDocument;
	/** The completion offset. */
	private final int fCompletionOffset;
	/** The completion length. */
	private final int fCompletionLength;

	/**
	 * Creates a document template context.
	 * 
	 * @param type the context type
	 * @param document the document this context applies to
	 * @param completionOffset the completion offset (for usage in content assist)
	 * @param completionLength the completion length
	 */
	public DocumentTemplateContext(ContextType type, IDocument document,	int completionOffset, int completionLength)	{
		super(type);
		
		Assert.isNotNull(document);
		Assert.isTrue(completionOffset >= 0 && completionOffset <= document.getLength());
		Assert.isTrue(completionLength >= 0);
		
		fDocument= document;
		fCompletionOffset= completionOffset;
		fCompletionLength= completionLength;
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
		return fCompletionOffset;	
	}
	
	/**
	 * Returns the completion length within the string of the context.
	 * 
	 * @return the completion length within the string of the context
	 */
	public int getCompletionLength() {
		return fCompletionLength;
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
		return fCompletionOffset;		
	}
	
	/**
	 * Returns the end offset of the keyword.
	 * 
	 * @return the end offset of the keyword
	 */
	public int getEnd() {
		return fCompletionOffset + fCompletionLength;
	}
	
	/**
	 * {@inheritdoc}
	 */
	public boolean canEvaluate(Template template) {
		return true;
	}

	/**
	 * {@inheritdoc}
	 */
	public TemplateBuffer evaluate(Template template) throws BadLocationException {
		if (!canEvaluate(template))
			return null;
		
		TemplateTranslator translator= new TemplateTranslator();
		TemplateBuffer buffer= translator.translate(template);

		getContextType().edit(buffer, this);
		
		return buffer;
	}
}

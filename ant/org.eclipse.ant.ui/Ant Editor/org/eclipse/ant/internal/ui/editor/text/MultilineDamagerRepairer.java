package org.eclipse.ant.internal.ui.editor.text;

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


import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.custom.StyleRange;


/**
 * Derived from org.eclipse.jface.text.rules.DefaultDamagerRepairer.
 * Considers multilines as damage regions even if the document partitioning has not changed.
 *
 * @see org.eclipse.jface.text.rules.DefaultDamagerRepairer
 */
public class MultilineDamagerRepairer implements IPresentationDamager, IPresentationRepairer {

	
	/** The document this object works on */
	protected IDocument fDocument;
	/** The scanner it uses */
	protected ITokenScanner fScanner;
	/** The default text attribute if non is returned as data by the current token */
	protected TextAttribute fDefaultTextAttribute;
	
	/**
	 * Creates a damager/repairer that uses the given scanner and returns the given default 
	 * text attribute if the current token does not carry a text attribute.
	 *
	 * @param scanner the token scanner to be used
	 * @param defaultTextAttribute the text attribute to be returned if none is specified by the current token,
	 * 			may be <code>null</code>
	 */
	public MultilineDamagerRepairer(ITokenScanner scanner, TextAttribute defaultTextAttribute) {
		fScanner= scanner;
		fDefaultTextAttribute= defaultTextAttribute;
	}
	
	/*
	 * @see IPresentationDamager#setDocument(IDocument)
	 * @see IPresentationRepairer#setDocument(IDocument)
	 */
	public void setDocument(IDocument document) {
		fDocument= document;
	}
	
	//---- IPresentationDamager
	
	/**
	 * Returns the end offset of the line that contains the specified offset or
	 * if the offset is inside a line delimiter, the end offset of the next line.
	 *
	 * @param offset the offset whose line end offset must be computed
	 * @return the line end offset for the given offset
	 * @exception BadLocationException if offset is invalid in the current document
	 */
	protected int endOfLineOf(int offset) throws BadLocationException {
		
		IRegion info= fDocument.getLineInformationOfOffset(offset);
		if (offset <= info.getOffset() + info.getLength())
			return info.getOffset() + info.getLength();
			
		int line= fDocument.getLineOfOffset(offset);
		try {
			info= fDocument.getLineInformation(line + 1);
			return info.getOffset() + info.getLength();
		} catch (BadLocationException x) {
			return fDocument.getLength();
		}
	}
	
	/*
	 * @see IPresentationDamager#getDamageRegion(ITypedRegion, DocumentEvent, boolean)
	 */
	public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent e, boolean documentPartitioningChanged) {	
		return partition;
	}
	
	//---- IPresentationRepairer
	
	/*
	 * @see IPresentationRepairer#createPresentation(TextPresentation, ITypedRegion)
	 */
	public void createPresentation(TextPresentation presentation, ITypedRegion region) {
		
		if (fScanner == null) {
			addRange(presentation, region.getOffset(), region.getLength(), fDefaultTextAttribute);
			return;
		}
		
		int lastStart= region.getOffset();
		int length= 0;
		IToken lastToken= Token.UNDEFINED;
		TextAttribute lastAttribute= getTokenTextAttribute(lastToken);
		
		fScanner.setRange(fDocument, lastStart, region.getLength());
		
		while (true) {
			IToken token= fScanner.nextToken();			
			if (token.isEOF())
				break;
			
			TextAttribute attribute= getTokenTextAttribute(token);			
			if (lastAttribute != null && lastAttribute.equals(attribute)) {
				length += fScanner.getTokenLength();
			} else {
				addRange(presentation, lastStart, length, lastAttribute);
				lastToken= token;
				lastAttribute= attribute;
				lastStart= fScanner.getTokenOffset();
				length= fScanner.getTokenLength();						    
			}
		}

		addRange(presentation, lastStart, length, lastAttribute);
	}
	
	/**
	 * Returns a text attribute encoded in the given token. If the token's
	 * data is not <code>null</code> and a text attribute it is assumed that
	 * it is the encoded text attribute. It returns the default text attribute
	 * if there is no encoded text attribute found.
	 *
	 * @param token the token whose text attribute is to be determined
	 * @return the token's text attribute
	 */
	protected TextAttribute getTokenTextAttribute(IToken token) {
		Object data= token.getData();
		if (data instanceof TextAttribute) {
			return (TextAttribute) data;
		}
		return fDefaultTextAttribute;
	}
	
	/**
	 * Adds style information to the given text presentation.
	 *
	 * @param presentation the text presentation to be extended
	 * @param offset the offset of the range to be styled
	 * @param length the length of the range to be styled
	 * @param attr the attribute describing the style of the range to be styled
	 */
	protected void addRange(TextPresentation presentation, int offset, int length, TextAttribute attr) {
		if (attr != null) {
			presentation.addStyleRange(new StyleRange(offset, length, attr.getForeground(), attr.getBackground(), attr.getStyle()));
		}
	}
	
	/**
	 * Configures the scanner's default return token. This is the text attribute
	 * which is returned when none is returned by the current token.
	 */
	public void setDefaultTextAttribute(TextAttribute defaultTextAttribute) {
		fDefaultTextAttribute= defaultTextAttribute;
	}
}
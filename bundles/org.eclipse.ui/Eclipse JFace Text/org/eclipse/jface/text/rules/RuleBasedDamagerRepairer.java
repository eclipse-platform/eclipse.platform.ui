package org.eclipse.jface.text.rules;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.swt.custom.StyleRange;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationRepairer;


/**
 * A standard implementation of a syntax driven presentation damager
 * and presentation repairer. It uses a rule based scanner to scan 
 * the document and to determine its damage and new text presentation.
 * The tokens returned by the rules the scanner is configured with 
 * are supposed to return text attributes as their data.
 *
 * @see IRule
 * @see RuleBasedScanner
 */
public class RuleBasedDamagerRepairer implements IPresentationDamager, IPresentationRepairer {
	
	/** The document this object works on */
	protected IDocument fDocument;
	/** The scanner it uses */
	protected RuleBasedScanner fScanner;
	/** The default text attribute if non is returned as data by the current token */
	protected TextAttribute fDefaultTextAttribute;
	
	/**
	 * Creates a damager/repairer that uses the given scanner and returns the given default 
	 * text attribute if the current token does not carry a text attribute.
	 *
	 * @param scanner the rule based scanner to be used
	 * @param defaultTextAttribute the text attribute to be returned if non is specified by the current token
	 */
	public RuleBasedDamagerRepairer(RuleBasedScanner scanner, TextAttribute defaultTextAttribute) {
		fScanner= scanner;
		fDefaultTextAttribute= defaultTextAttribute;
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
		presentation.addStyleRange(new StyleRange(offset, length, attr.getForeground(), attr.getBackground()));
	}
	//---- IPresentationRepairer
	
	/*
	 * @see IPresentationRepairer#createPresentation
	 */
	public void createPresentation(TextPresentation presentation, ITypedRegion region) {
		
		if (fScanner == null) {
			addRange(presentation, region.getOffset(), region.getLength(), fDefaultTextAttribute);
			return;
		}
		
		int lastStart= region.getOffset();
		int length= 0;
		IToken lastToken= Token.UNDEFINED;
		
		fScanner.setRange(fDocument, lastStart, region.getLength());
		
		while (true) {
			
			IToken token= fScanner.nextToken();
			
			if (token.isEOF()) {
				if (!lastToken.isUndefined() && length != 0)
					addRange(presentation, lastStart, length, getTokenTextAttribute(lastToken));
				break;
			}
			
			if (token.isWhitespace()) {
				length += fScanner.getTokenLength();
				continue;
			}
			
			if (lastToken.isUndefined()) {
				lastToken= token;
				length += fScanner.getTokenLength();
				continue;
			}
			
			if (token != lastToken) {
				addRange(presentation, lastStart, length, getTokenTextAttribute(lastToken));
				lastToken= token;
				lastStart= fScanner.getTokenOffset();
				length= fScanner.getTokenLength();
				continue;
			}
			
			length += fScanner.getTokenLength();
		}
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
	 * @see IPresentationDamager#getDamageRegion
	 */
	public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent e, boolean documentPartitioningChanged) {
		
		if (!documentPartitioningChanged) {
			try {
				
				IRegion info= fDocument.getLineInformationOfOffset(e.getOffset());
				int start= Math.max(partition.getOffset(), info.getOffset());
				
				int end= e.getOffset() + (e.getText() == null ? e.getLength() : e.getText().length());
				
				if (info.getOffset() <= end && end <= info.getOffset() + info.getLength()) {
					// optimize the case of the same line
					end= info.getOffset() + info.getLength();
				} else 
					end= endOfLineOf(end);
					
				end= Math.min(partition.getOffset() + partition.getLength(), end);
				return new Region(start, end - start);
			
			} catch (BadLocationException x) {
			}
		}
		
		return partition;
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
		if (data instanceof TextAttribute) 
			return (TextAttribute) data;
		return fDefaultTextAttribute;
	}
	/*
	 * @see IPresentationDamager#setDocument
	 * @see IPresentationRepairer#setDocument
	 */
	public void setDocument(IDocument document) {
		fDocument= document;
	}
}

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
package org.eclipse.jface.text;

/**
 * ParentChildMapping.java
 */
public class ParentChildMapping  implements IDocumentInformationMapping {
	
	private IDocument fParentDocument;
	private ChildDocument fChildDocument;
	
	
	public ParentChildMapping(ChildDocument childDocument) {
		fParentDocument= childDocument.getParentDocument();
		fChildDocument= childDocument;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentInformationMapping#getCoverage()
	 */
	public IRegion getCoverage() {
		Position p= fChildDocument.getParentDocumentRange();
		return new Region(p.getOffset(), p.getLength());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentInformationMapping#toOriginOffset(int)
	 */
	public int toOriginOffset(int imageOffset) throws BadLocationException {
		int anchorOffset= fChildDocument.getParentDocumentRange().getOffset();
		return anchorOffset + imageOffset;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentInformationMapping#toOriginRegion(org.eclipse.jface.text.IRegion)
	 */
	public IRegion toOriginRegion(IRegion imageRegion) throws BadLocationException {
		int anchorOffset= fChildDocument.getParentDocumentRange().getOffset();
		return new Region(anchorOffset + imageRegion.getOffset(), imageRegion.getLength());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentInformationMapping#toOriginLines(int)
	 */
	public IRegion toOriginLines(int imageLine) throws BadLocationException {
		IRegion imageDocumentRegion= fChildDocument.getLineInformation(imageLine);
		IRegion originDocumentRegion= toOriginRegion(imageDocumentRegion);

		int startLine= fParentDocument.getLineOfOffset(originDocumentRegion.getOffset());
		if (originDocumentRegion.getLength() == 0)
			return new Region(startLine, 0);
			
		int endLine= fParentDocument.getLineOfOffset(originDocumentRegion.getOffset() + originDocumentRegion.getLength() -1);
		return new Region(startLine, endLine - startLine);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentInformationMapping#toOriginLine(int)
	 */
	public int toOriginLine(int imageLine) throws BadLocationException {
		int anchorOffset= fChildDocument.getParentDocumentRange().getOffset();
		return fParentDocument.getLineOfOffset(anchorOffset) + imageLine;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentInformationMapping#toImageOffset(int)
	 */
	public int toImageOffset(int originOffset) throws BadLocationException {
		Position p= fChildDocument.getParentDocumentRange();
		if (p.includes(originOffset))
			return originOffset - p.getOffset();
		return -1;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentInformationMapping#toImageRegion(org.eclipse.jface.text.IRegion)
	 */
	public IRegion toImageRegion(IRegion originRegion) throws BadLocationException {
		
		int offset= originRegion.getOffset();
		int length= originRegion.getLength();
		
		if (length < 0) {
			length= -length;
			offset -= length;
		}
		
		Position p= fChildDocument.getParentDocumentRange();
		if (p.overlapsWith(offset, length)) {

			if (offset < p.getOffset())
				offset= p.getOffset();

			int end= offset + length;
			int e= p.getOffset() + p.getLength();
			if (end > e)
				end= e;
			
			offset -= p.getOffset();
			end -= p.getOffset();
			
			if (originRegion.getLength() < 0)
				return new Region(end, offset - end);
			return new Region(offset, end - offset);
		}
		
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentInformationMapping#toImageLine(int)
	 */
	public int toImageLine(int originLine) throws BadLocationException {
		int anchorOffset= fChildDocument.getParentDocumentRange().getOffset();
		int startLine= fParentDocument.getLineOfOffset(anchorOffset);
		
		int imageLine= originLine - startLine;
		if (imageLine	< 0 || imageLine > fChildDocument.getNumberOfLines())
			return -1;			
		return imageLine;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentInformationMapping#toClosestImageLine(int)
	 */
	public int toClosestImageLine(int originLine) throws BadLocationException {
		int anchorOffset= fChildDocument.getParentDocumentRange().getOffset();
		int startLine= fParentDocument.getLineOfOffset(anchorOffset);

		int imageLine= originLine - startLine;
		if (imageLine < 0)
			return 0;
			
		int maxLine= fChildDocument.getNumberOfLines();
		if (imageLine > maxLine)
			return maxLine;
			
		return imageLine;
	}
}

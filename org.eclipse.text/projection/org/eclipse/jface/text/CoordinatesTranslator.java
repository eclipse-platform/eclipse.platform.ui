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

import java.util.Arrays;
import java.util.Comparator;

/**
 * Implementation of <code>IDocumentInformationMapping</code> matching <code>ProjectionDocument</code> and
 * <code>ProjectionDocumentManager</code>. The parent document is considered the original document, the projection
 * document is considered the image document.<p>
 * This class is for internal use only. 
 * @since 2.1
 */
public class CoordinatesTranslator implements IDocumentInformationMapping {
	
	/** The parent document */
	private IDocument fParentDocument;
	/** The position category used to manage the projected regions of the parent document */
	private String fParentCategory;
	/** The projection document */
	private ProjectionDocument fProjectionDocument;
	/** The position category to manage the fragments of the projection document. */
	private String fProjectionCategory;
	
	/**
	 * Creates a new mapping between the given parent document and the given projection document.
	 * 
	 * @param parent the parent document
	 * @param parentCategory the position category of the parent document used to manage the projected regions
	 * @param projection the projection document
	 * @param projectionCategory the position category of the projection document used to manage the fragments
	 */
	public CoordinatesTranslator(IDocument parent, String parentCategory, ProjectionDocument projection, String projectionCategory) {
		fParentDocument= parent;
		fParentCategory= parentCategory;
		fProjectionDocument= projection;
		fProjectionCategory= projectionCategory;
	}
	
	/*
	 * @see org.eclipse.jface.text.IDocumentInformationMapping#toOriginOffset(int)
	 */
	public int toOriginOffset(int imageOffset) throws BadLocationException {
		Fragment fragment= (Fragment) getPositionOfOffset(fProjectionDocument, ProjectionDocument.FRAGMENT_CATEGORY, imageOffset);
		if (fragment == null) {
			if (imageOffset == 0)
				return 0;
			throw new BadLocationException();
		}

		int relative= imageOffset - fragment.offset;
		return fragment.getOrigin().offset + relative;
	}
	
	/*
	 * @see org.eclipse.jface.text.IDocumentInformationMapping#toOriginRegion(org.eclipse.jface.text.IRegion)
	 */
	public IRegion toOriginRegion(IRegion imageRegion) throws BadLocationException {
		
		int projectionOffset= imageRegion.getOffset();
		int projectionLength= imageRegion.getLength();
		
		if (projectionLength == 0) {
			if (projectionOffset == 0 && projectionLength == fProjectionDocument.getLength())
				return new Region(0, fParentDocument.getLength());
			return new Region(toOriginOffset(projectionOffset), 0);
		}
		
		int o1= toOriginOffset(projectionOffset);
		int o2= toOriginOffset(projectionOffset + projectionLength -1);
		return new Region(o1, o2 - o1 + 1);
	}
	
	/*
	 * @see org.eclipse.jface.text.IDocumentInformationMapping#toOriginLines(int)
	 */
	public IRegion toOriginLines(int imageLine) throws BadLocationException {
		
		IRegion projectionDocumentRegion= fProjectionDocument.getLineInformation(imageLine);
		IRegion parentDocumentRegion= toOriginRegion(projectionDocumentRegion);
		
		int startLine= fParentDocument.getLineOfOffset(parentDocumentRegion.getOffset());
		if (parentDocumentRegion.getLength() == 0)
			return new Region(startLine, 0);
			
		int endLine= fParentDocument.getLineOfOffset(parentDocumentRegion.getOffset() + parentDocumentRegion.getLength() -1);
		return new Region(startLine, endLine - startLine);
	}
	
	/*
	 * @see org.eclipse.jface.text.IDocumentInformationMapping#toOriginLine(int)
	 */
	public int toOriginLine(int imageLine) throws BadLocationException {
		IRegion lines= toOriginLines(imageLine);
		if (lines.getLength() > 0)
			throw new IllegalStateException();
		return lines.getOffset();
	}
	
	/*
	 * @see org.eclipse.jface.text.IDocumentInformationMapping#toImageOffset(int)
	 */
	public int toImageOffset(int originOffset) throws BadLocationException {
		ProjectionPosition projection= (ProjectionPosition) getPositionOfOffset(fParentDocument, ProjectionDocumentManager.PROJECTION_DOCUMENTS, originOffset);
		if (projection != null)
			return translateOffset(projection, originOffset, projection.getFragment());
		// not included
		return -1;
	}
	
	/*
	 * @see org.eclipse.jface.text.IDocumentInformationMapping#toImageRegion(org.eclipse.jface.text.IRegion)
	 */
	public IRegion toImageRegion(IRegion originRegion) throws BadLocationException {
		
		if (originRegion.getLength() == 0) {
			int projectionOffset= toImageOffset(originRegion.getOffset());
			return projectionOffset == -1 ? null : new Region(projectionOffset, 0);
		}	
		
		Position[] positions= getPositionsOfRange(fParentDocument, ProjectionDocumentManager.PROJECTION_DOCUMENTS, originRegion, null);
		if (positions != null && positions.length > 0) {
			ProjectionPosition projection= (ProjectionPosition) positions[0];
			
			int offset= originRegion.getOffset();
			int length= originRegion.getLength();
			
			int delta= projection.getOffset() - offset;
			if (delta > 0) {
				offset += delta;
				length -= delta;
			}
			int start= translateOffset(projection, offset, projection.getFragment());
			
			projection= (ProjectionPosition) positions[positions.length -1];
			int decrease= 0;
			int endOffset= offset + length;
			if (length > 0)
				decrease= 1;
			endOffset -= decrease;
			
			delta= endOffset - (projection.getOffset() + Math.max(projection.getLength() -1, 0));
			if (delta > 0)
				endOffset -= delta;
			
			int end= translateOffset(projection, endOffset, projection.getFragment());
			return new Region(start, end - start + decrease);
		}
		
		return null;
	}
	
	/*
	 * @see org.eclipse.jface.text.IDocumentInformationMapping#toImageLine(int)
	 */
	public int toImageLine(int originLine) throws BadLocationException {
		
		IRegion parentDocumentRegion= fParentDocument.getLineInformation(originLine);
		IRegion projectionDocumentRegion= toImageRegion(parentDocumentRegion);
		if (projectionDocumentRegion == null)
			return -1;
		
		int startLine= fProjectionDocument.getLineOfOffset(projectionDocumentRegion.getOffset());
		if (projectionDocumentRegion.getLength() == 0)
			return startLine;
			
		int endLine= fProjectionDocument.getLineOfOffset(projectionDocumentRegion.getOffset() + projectionDocumentRegion.getLength() -1);
		if (endLine != startLine)
			throw new IllegalStateException();
		
		return startLine;
	}
	
	
	//-----------------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Translates the given offset relative to the given origin position into an
	 * offset relative to the given target position.
	 * 
	 * @param origin the origin position
	 * @param originOffset the offset relative to <code>originOffset</code>
	 * @param target the target position
	 * @return <code>originOffset</code> translated to the given target position
	 */
	private int translateOffset(Position origin, int originOffset, Position target) {
		int relative= originOffset - origin.offset;
		return target.offset + relative;
	}
	
	/**
	 * Returns the position of the given category of the given document that includes the given offset
	 * or <code>null</code> if there is no such position.
	 * 
	 * @param document the document
	 * @param category the position category of <code>document</code>
	 * @param offset the offset into <code>document</code>
	 * @return the position including the given offset or <code>null</code>
	 * @throws BadLocationException if <code>offset</code> is not valid in the given document
	 */
	private Position getPositionOfOffset(IDocument document, String category, int offset) throws BadLocationException {
		try {
			int index= getPositionIndexOfOffset(document, category, offset, 0);
			if (index > -1) {
				Position[] positions= document.getPositions(category);
				return positions[index];
			}	
		} catch (BadPositionCategoryException x) {
		}
		return null;
	}
	
	/**
	 * Returns an array of positions of the given category that cover the given range of the document.
	 * 
	 * @param document the document
	 * @param category the position category of <code>document</code>
	 * @param range the range of <code>document</code>
	 * @param comparator the comparator to sort the array to be returned
	 * @return an array of positions that cover the given range in the given document
	 */
	private Position[] getPositionsOfRange(IDocument document, String category, IRegion range, Comparator comparator) {
		
		int offset= range.getOffset();
		int length= range.getLength();
		
		try {
			
			int start= getPositionIndexOfOffset(document, category, offset, length);
			int end= getPositionIndexOfOffset(document, category, offset + length -1, 1 - length);
					
			if (start > -1 && end > -1) {
				
				Position[]  positions= document.getPositions(category);
				
				if (start == end)
					return new Position[] { positions[start] };
					
				Position[] result= new Position[end - start + 1];
				for (int i= start; i <= end; i++)
					result[i - start]= positions[i];
					
				if (comparator != null)
					Arrays.sort(result, comparator);
					
				return result;
			}
			
		} catch (BadPositionCategoryException e) {
		} catch (BadLocationException e) {
		}
				
		return new Position[0];
	}
	
	/**
	 * Returns the index of the position of the given category of the given document that includes the
	 * given offset. <code>direction</code> indicates the direction into which the algorithm should search.
	 * 
	 * @param document the document
	 * @param category the position category of <code>document</code>
	 * @param offset the offset into <code>document</code>
	 * @param direction the search direction
	 * @return the index of the position
	 * @throws BadPositionCategoryException if <code>category</code> is not valid in <code>document</code>
	 * @throws BadLocationException if <code>offset</code> is not valid in <code>document</code>
	 */
	private int getPositionIndexOfOffset(IDocument document, String category, int offset, int direction ) throws BadPositionCategoryException, BadLocationException{
		
		Position[] positions= document.getPositions(category);
		if (positions != null && positions.length > 0) {
			
			// test for inclusion
			int index= document.computeIndexInCategory(category, offset);
			if (index < positions.length && positions[index].includes(offset))
				return index;
			if (index > 0 && positions[index -1].includes(offset))
				return index -1;
			
			// find next accorrding to direction
			if (direction != 0) {
				if (direction > 0) {
					if (index < positions.length && positions[index].overlapsWith(offset, direction))
						return index;
				} else {
					if (index > 0 && positions[index -1].overlapsWith(offset + direction, -direction))
						return index -1;
				}
			}
		}
				
		return -1;
	}
	
	/*
	 * @see org.eclipse.jface.text.IDocumentInformationMapping#getCoverage()
	 */
	public IRegion getCoverage() {
		Position coverage= fProjectionDocument.getParentDocumentCoverage();
		return new Region(coverage.getOffset(), coverage.getLength());
	}

	/*
	 * @see org.eclipse.jface.text.IDocumentInformationMapping#toClosestImageLine(int)
	 */
	public int toClosestImageLine(int originLine) throws BadLocationException {
		try {
	
			int modelLineOffset= fParentDocument.getLineOffset(originLine);
			int index= fParentDocument.computeIndexInCategory(ProjectionDocumentManager.PROJECTION_DOCUMENTS, modelLineOffset);
			Position[] projections= fParentDocument.getPositions(ProjectionDocumentManager.PROJECTION_DOCUMENTS);
	
			if (index < projections.length) {
				Position p= projections[index -1];
				int delta1= modelLineOffset - (p.getOffset() + p.getLength());
				p= projections[index];
				int delta2= modelLineOffset - (p.getOffset() + p.getLength());
				if (delta1 < delta2) {
					p= projections[index -1];
					originLine= fParentDocument.getLineOfOffset(p.getOffset() + Math.max(p.getLength() -1, 0));
				} else {
					originLine= fParentDocument.getLineOfOffset(p.getOffset());
				}
			} else if (projections.length > 0) {
				Position p= projections[index -1];
				originLine= fParentDocument.getLineOfOffset(p.getOffset() + Math.max(p.getLength() -1, 0));
			} else {
				return 0;
			}
	
			return toImageLine(originLine);
	
		} catch (BadLocationException x) {
		} catch (BadPositionCategoryException x) {
		}
	
		return 0;
	}
}

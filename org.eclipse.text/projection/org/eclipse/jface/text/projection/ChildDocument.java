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
package org.eclipse.jface.text.projection;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;

/**
 * ChildDocument
 */
public class ChildDocument extends ProjectionDocument {
	
	static private class VisibleRegion extends Position {
		
		public VisibleRegion(int offset, int length) {
			super(offset, length);
		}
		
		/**
		 * If offset is the end of the visible region and the length is 0,
		 * the offset is considered overlapping with the visible region.
		 */
		public boolean overlapsWith(int offset, int length) {
			boolean appending= (offset == this.offset + this.length) && length == 0;
			return appending || super.overlapsWith(offset, length);
		}
	}

	/**
	 * @param masterDocument
	 * @param fragmentsCategory
	 * @param fragmentUpdater
	 * @param segmentsCategory
	 */
	public ChildDocument(IDocument masterDocument, String fragmentsCategory, FragmentUpdater fragmentUpdater, String segmentsCategory) {
		super(masterDocument, fragmentsCategory, fragmentUpdater, segmentsCategory);
	}

	/**
	 * @return
	 */
	public IDocument getParentDocument() {
		return getMasterDocument();
	}

	/**
	 * @param offset
	 * @param length
	 */
	public void setParentDocumentRange(int offset, int length) throws BadLocationException {
		try {
			
			Position[] fragments= getFragments();
			for (int i= 0; i < fragments.length; i++) {
				Fragment fragment= (Fragment) fragments[i];
				fMasterDocument.removePosition(fFragmentsCategory, fragment);
				removePosition(fSegmentsCategory, fragment.segment);
			}
			
			Fragment fragment= new Fragment(offset, length);
			Segment segment= new Segment(0, 0);
			segment.fragment= fragment;
			fragment.segment= segment;
			fMasterDocument.addPosition(fFragmentsCategory, fragment);
			addPosition(fSegmentsCategory, segment);
			segment.setLength(length);
			
			getTracker().set(fMasterDocument.get(offset, length));
			
		} catch (BadPositionCategoryException x) {
		}
	}

	/**
	 * @return
	 */
	public Position getParentDocumentRange() {
		IRegion coverage= getProjectionMapping().getCoverage();
		return new VisibleRegion(coverage.getOffset(), coverage.getLength());
	}
}

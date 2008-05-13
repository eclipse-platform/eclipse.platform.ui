/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.projection;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;

/**
 * Implementation of a child document based on
 * {@link org.eclipse.jface.text.projection.ProjectionDocument}. This class
 * exists for compatibility reasons.
 * <p>
 * Internal class. This class is not intended to be used by clients.</p>
 *
 * @since 3.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ChildDocument extends ProjectionDocument {

	/**
	 * Position reflecting a visible region. The exclusive end offset of the position
	 * is considered being overlapping with the visible region.
	 */
	static private class VisibleRegion extends Position {

		/**
		 * Creates a new visible region.
		 *
		 * @param regionOffset the offset of the region
		 * @param regionLength the length of the region
		 */
		public VisibleRegion(int regionOffset, int regionLength) {
			super(regionOffset, regionLength);
		}

		/**
		 * If <code>regionOffset</code> is the end of the visible region and the <code>regionLength == 0</code>,
		 * the <code>regionOffset</code> is considered overlapping with the visible region.
		 *
		 * @see org.eclipse.jface.text.Position#overlapsWith(int, int)
		 */
		public boolean overlapsWith(int regionOffset, int regionLength) {
			boolean appending= (regionOffset == offset + length) && regionLength == 0;
			return appending || super.overlapsWith(regionOffset, regionLength);
		}
	}

	/**
	 * Creates a new child document.
	 *
	 * @param masterDocument @inheritDoc
	 */
	public ChildDocument(IDocument masterDocument) {
		super(masterDocument);
	}

	/**
	 * Returns the parent document of this child document.
	 *
	 * @return the parent document of this child document
	 * @see ProjectionDocument#getMasterDocument()
	 */
	public IDocument getParentDocument() {
		return getMasterDocument();
	}

	/**
	 * Sets the parent document range covered by this child document to the
	 * given range.
	 *
	 * @param offset the offset of the range
	 * @param length the length of the range
	 * @throws BadLocationException if the given range is not valid
	 */
	public void setParentDocumentRange(int offset, int length) throws BadLocationException {
		replaceMasterDocumentRanges(offset, length);
	}

	/**
	 * Returns the parent document range of this child document.
	 *
	 * @return the parent document range of this child document
	 */
	public Position getParentDocumentRange() {
		IRegion coverage= getDocumentInformationMapping().getCoverage();
		return new VisibleRegion(coverage.getOffset(), coverage.getLength());
	}
}

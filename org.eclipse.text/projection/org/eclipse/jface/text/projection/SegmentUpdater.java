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
package org.eclipse.jface.text.projection;


import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.Position;


/**
 * The position updater used to adapt the segments of a projection document to
 * changes of the master document. Depending on the flags set on a segment, a
 * segment is either extended to shifted if an insertion happens at a segment's
 * offset. The last segment is extended if an insert operation happens at the
 * end of the segment.
 *
 * @since 3.0
 */
class SegmentUpdater extends DefaultPositionUpdater {

	private Segment fNextSegment= null;
	private boolean fIsProjectionChange= false;

	/**
	 * Creates the segment updater for the given category.
	 *
	 * @param segmentCategory the position category used for managing the segments of a projection document
	 */
	protected SegmentUpdater(String segmentCategory) {
		super(segmentCategory);
	}

	/*
	 * @see org.eclipse.jface.text.IPositionUpdater#update(org.eclipse.jface.text.DocumentEvent)
	 */
	public void update(DocumentEvent event) {

		Assert.isTrue(event instanceof ProjectionDocumentEvent);
		fIsProjectionChange= ((ProjectionDocumentEvent) event).getChangeType() == ProjectionDocumentEvent.PROJECTION_CHANGE;

		try {

			Position[] category= event.getDocument().getPositions(getCategory());

			fOffset= event.getOffset();
			fLength= event.getLength();
			fReplaceLength= (event.getText() == null ? 0 : event.getText().length());
			fDocument= event.getDocument();

			for (int i= 0; i < category.length; i++) {

				fPosition= category[i];
				Assert.isTrue(fPosition instanceof Segment);

				if (i < category.length - 1)
					fNextSegment= (Segment) category[i + 1];
				else
					fNextSegment= null;

				fOriginalPosition.offset= fPosition.offset;
				fOriginalPosition.length= fPosition.length;

				if (notDeleted())
					adaptToReplace();

			}

		} catch (BadPositionCategoryException x) {
			// do nothing
		}
	}

	/*
	 * @see org.eclipse.jface.text.DefaultPositionUpdater#adaptToInsert()
	 */
	protected void adaptToInsert() {

		Segment segment= (Segment) fPosition;
		int myStart= segment.offset;
		int myEnd= segment.offset + segment.length - (segment.isMarkedForStretch || fNextSegment == null || isAffectingReplace() ? 0 : 1);
		myEnd= Math.max(myStart, myEnd);
		int yoursStart= fOffset;

		try {

			if (myEnd < yoursStart)
				return;

			if (segment.isMarkedForStretch) {
				Assert.isTrue(fIsProjectionChange);
				segment.isMarkedForShift= false;
				if (fNextSegment != null) {
					fNextSegment.isMarkedForShift= true;
					fNextSegment.isMarkedForStretch= false;
				}
			}

			if (fLength <= 0) {

				if (myStart < (yoursStart + (segment.isMarkedForShift ? 0 : 1)))
					fPosition.length += fReplaceLength;
				else
					fPosition.offset += fReplaceLength;

			} else {

				if (myStart <= yoursStart && fOriginalPosition.offset <= yoursStart)
					fPosition.length += fReplaceLength;
				else
					fPosition.offset += fReplaceLength;
			}

		} finally {
			segment.clearMark();
		}
	}
}

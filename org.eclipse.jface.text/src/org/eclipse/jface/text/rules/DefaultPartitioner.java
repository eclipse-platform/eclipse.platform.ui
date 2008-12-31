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
package org.eclipse.jface.text.rules;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IDocumentPartitionerExtension;
import org.eclipse.jface.text.IDocumentPartitionerExtension2;
import org.eclipse.jface.text.IDocumentPartitionerExtension3;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.text.TypedRegion;



/**
 * A standard implementation of a document partitioner. It uses a partition
 * token scanner to scan the document and to determine the document's
 * partitioning. The tokens returned by the scanner are supposed to return the
 * partition type as their data. The partitioner remembers the document's
 * partitions in the document itself rather than maintaining its own data
 * structure.
 *
 * @see IPartitionTokenScanner
 * @since 2.0
 * @deprecated As of 3.1, replaced by {@link org.eclipse.jface.text.rules.FastPartitioner} instead
 */
public class DefaultPartitioner implements IDocumentPartitioner, IDocumentPartitionerExtension, IDocumentPartitionerExtension2, IDocumentPartitionerExtension3 {

	/**
	 * The position category this partitioner uses to store the document's partitioning information.
	 * @deprecated As of 3.0, use <code>getManagingPositionCategories()</code> instead.
	 */
	public final static String CONTENT_TYPES_CATEGORY= "__content_types_category"; //$NON-NLS-1$


	/** The partitioner's scanner */
	protected IPartitionTokenScanner fScanner;
	/** The legal content types of this partitioner */
	protected String[] fLegalContentTypes;
	/** The partitioner's document */
	protected IDocument fDocument;
	/** The document length before a document change occurred */
	protected int fPreviousDocumentLength;
	/** The position updater used to for the default updating of partitions */
	protected DefaultPositionUpdater fPositionUpdater;
	/** The offset at which the first changed partition starts */
	protected int fStartOffset;
	/** The offset at which the last changed partition ends */
	protected int fEndOffset;
	/**The offset at which a partition has been deleted */
	protected int fDeleteOffset;
	/**
	 * The position category this partitioner uses to store the document's partitioning information.
	 * @since 3.0
	 */
	private String fPositionCategory;
	/**
	 * The active document rewrite session.
	 * @since 3.1
	 */
	private DocumentRewriteSession fActiveRewriteSession;
	/**
	 * Flag indicating whether this partitioner has been initialized.
	 * @since 3.1
	 */
	private boolean fIsInitialized= false;

	/**
	 * Creates a new partitioner that uses the given scanner and may return
	 * partitions of the given legal content types.
	 *
	 * @param scanner the scanner this partitioner is supposed to use
	 * @param legalContentTypes the legal content types of this partitioner
	 */
	public DefaultPartitioner(IPartitionTokenScanner scanner, String[] legalContentTypes) {
		fScanner= scanner;
		fLegalContentTypes= TextUtilities.copy(legalContentTypes);
		fPositionCategory= CONTENT_TYPES_CATEGORY + hashCode();
		fPositionUpdater= new DefaultPositionUpdater(fPositionCategory);
	}

	/*
	 * @see org.eclipse.jface.text.IDocumentPartitionerExtension2#getManagingPositionCategories()
	 * @since 3.0
	 */
	public String[] getManagingPositionCategories() {
		return new String[] { fPositionCategory };
	}

	/*
	 * @see org.eclipse.jface.text.IDocumentPartitioner#connect(org.eclipse.jface.text.IDocument)
	 */
	public void connect(IDocument document) {
		connect(document, false);
	}

	/*
	 * @see org.eclipse.jface.text.IDocumentPartitionerExtension3#connect(org.eclipse.jface.text.IDocument, boolean)
	 * @since 3.1
	 */
	public void connect(IDocument document, boolean delayInitialization) {
		Assert.isNotNull(document);
		Assert.isTrue(!document.containsPositionCategory(fPositionCategory));

		fDocument= document;
		fDocument.addPositionCategory(fPositionCategory);

		fIsInitialized= false;
		if (!delayInitialization)
			checkInitialization();
	}

	/*
	 * @since 3.1
	 */
	protected final void checkInitialization() {
		if (!fIsInitialized)
			initialize();
	}

	/**
	 * Performs the initial partitioning of the partitioner's document.
	 */
	protected void initialize() {
		fIsInitialized= true;
		fScanner.setRange(fDocument, 0, fDocument.getLength());

		try {
			IToken token= fScanner.nextToken();
			while (!token.isEOF()) {

				String contentType= getTokenContentType(token);

				if (isSupportedContentType(contentType)) {
					TypedPosition p= new TypedPosition(fScanner.getTokenOffset(), fScanner.getTokenLength(), contentType);
					fDocument.addPosition(fPositionCategory, p);
				}

				token= fScanner.nextToken();
			}
		} catch (BadLocationException x) {
			// cannot happen as offsets come from scanner
		} catch (BadPositionCategoryException x) {
			// cannot happen if document has been connected before
		}
	}

	/*
	 * @see IDocumentPartitioner#disconnect()
	 */
	public void disconnect() {

		Assert.isTrue(fDocument.containsPositionCategory(fPositionCategory));

		try {
			fDocument.removePositionCategory(fPositionCategory);
		} catch (BadPositionCategoryException x) {
			// can not happen because of Assert
		}
	}

	/*
	 * @see IDocumentPartitioner#documentAboutToBeChanged(DocumentEvent)
	 */
	public void documentAboutToBeChanged(DocumentEvent e) {
		if (fIsInitialized) {

			Assert.isTrue(e.getDocument() == fDocument);

			fPreviousDocumentLength= e.getDocument().getLength();
			fStartOffset= -1;
			fEndOffset= -1;
			fDeleteOffset= -1;
		}
	}

	/*
	 * @see IDocumentPartitioner#documentChanged(DocumentEvent)
	 */
	public boolean documentChanged(DocumentEvent e) {
		if (fIsInitialized) {
			IRegion region= documentChanged2(e);
			return (region != null);
		}
		return false;
	}

	/**
	 * Helper method for tracking the minimal region containing all partition changes.
	 * If <code>offset</code> is smaller than the remembered offset, <code>offset</code>
	 * will from now on be remembered. If <code>offset  + length</code> is greater than
	 * the remembered end offset, it will be remembered from now on.
	 *
	 * @param offset the offset
	 * @param length the length
	 */
	private void rememberRegion(int offset, int length) {
		// remember start offset
		if (fStartOffset == -1)
			fStartOffset= offset;
		else if (offset < fStartOffset)
			fStartOffset= offset;

		// remember end offset
		int endOffset= offset + length;
		if (fEndOffset == -1)
			fEndOffset= endOffset;
		else if (endOffset > fEndOffset)
			fEndOffset= endOffset;
	}

	/**
	 * Remembers the given offset as the deletion offset.
	 *
	 * @param offset the offset
	 */
	private void rememberDeletedOffset(int offset) {
		fDeleteOffset= offset;
	}

	/**
	 * Creates the minimal region containing all partition changes using the
	 * remembered offset, end offset, and deletion offset.
	 *
	 * @return the minimal region containing all the partition changes
	 */
	private IRegion createRegion() {
		if (fDeleteOffset == -1) {
			if (fStartOffset == -1 || fEndOffset == -1)
				return null;
			return new Region(fStartOffset, fEndOffset - fStartOffset);
		} else if (fStartOffset == -1 || fEndOffset == -1) {
			return new Region(fDeleteOffset, 0);
		} else {
			int offset= Math.min(fDeleteOffset, fStartOffset);
			int endOffset= Math.max(fDeleteOffset, fEndOffset);
			return new Region(offset, endOffset - offset);
		}
	}

	/*
	 * @see IDocumentPartitionerExtension#documentChanged2(DocumentEvent)
	 * @since 2.0
	 */
	public IRegion documentChanged2(DocumentEvent e) {

		if (!fIsInitialized)
			return null;

		try {

			IDocument d= e.getDocument();
			Position[] category= d.getPositions(fPositionCategory);
			IRegion line= d.getLineInformationOfOffset(e.getOffset());
			int reparseStart= line.getOffset();
			int partitionStart= -1;
			String contentType= null;
			int newLength= e.getText() == null ? 0 : e.getText().length();

			int first= d.computeIndexInCategory(fPositionCategory, reparseStart);
			if (first > 0)	{
				TypedPosition partition= (TypedPosition) category[first - 1];
				if (partition.includes(reparseStart)) {
					partitionStart= partition.getOffset();
					contentType= partition.getType();
					if (e.getOffset() == partition.getOffset() + partition.getLength())
						reparseStart= partitionStart;
					-- first;
				} else if (reparseStart == e.getOffset() && reparseStart == partition.getOffset() + partition.getLength()) {
					partitionStart= partition.getOffset();
					contentType= partition.getType();
					reparseStart= partitionStart;
					-- first;
				} else {
					partitionStart= partition.getOffset() + partition.getLength();
					contentType= IDocument.DEFAULT_CONTENT_TYPE;
				}
			}

			fPositionUpdater.update(e);
			for (int i= first; i < category.length; i++) {
				Position p= category[i];
				if (p.isDeleted) {
					rememberDeletedOffset(e.getOffset());
					break;
				}
			}
			category= d.getPositions(fPositionCategory);

			fScanner.setPartialRange(d, reparseStart, d.getLength() - reparseStart, contentType, partitionStart);

			int lastScannedPosition= reparseStart;
			IToken token= fScanner.nextToken();

			while (!token.isEOF()) {

				contentType= getTokenContentType(token);

				if (!isSupportedContentType(contentType)) {
					token= fScanner.nextToken();
					continue;
				}

				int start= fScanner.getTokenOffset();
				int length= fScanner.getTokenLength();

				lastScannedPosition= start + length - 1;

				// remove all affected positions
				while (first < category.length) {
					TypedPosition p= (TypedPosition) category[first];
					if (lastScannedPosition >= p.offset + p.length ||
							(p.overlapsWith(start, length) &&
							 	(!d.containsPosition(fPositionCategory, start, length) ||
							 	 !contentType.equals(p.getType())))) {

						rememberRegion(p.offset, p.length);
						d.removePosition(fPositionCategory, p);
						++ first;

					} else
						break;
				}

				// if position already exists and we have scanned at least the
				// area covered by the event, we are done
				if (d.containsPosition(fPositionCategory, start, length)) {
					if (lastScannedPosition >= e.getOffset() + newLength)
						return createRegion();
					++ first;
				} else {
					// insert the new type position
					try {
						d.addPosition(fPositionCategory, new TypedPosition(start, length, contentType));
						rememberRegion(start, length);
					} catch (BadPositionCategoryException x) {
					} catch (BadLocationException x) {
					}
				}

				token= fScanner.nextToken();
			}


			// remove all positions behind lastScannedPosition since there aren't any further types
			if (lastScannedPosition != reparseStart) {
				// if this condition is not met, nothing has been scanned because of a deletion
				++ lastScannedPosition;
			}
			first= d.computeIndexInCategory(fPositionCategory, lastScannedPosition);
			category= d.getPositions(fPositionCategory);

			TypedPosition p;
			while (first < category.length) {
				p= (TypedPosition) category[first++];
				d.removePosition(fPositionCategory, p);
				rememberRegion(p.offset, p.length);
			}

		} catch (BadPositionCategoryException x) {
			// should never happen on connected documents
		} catch (BadLocationException x) {
		}

		return createRegion();
	}


	/**
	 * Returns the position in the partitoner's position category which is
	 * close to the given offset. This is, the position has either an offset which
	 * is the same as the given offset or an offset which is smaller than the given
	 * offset. This method profits from the knowledge that a partitioning is
	 * a ordered set of disjoint position.
	 *
	 * @param offset the offset for which to search the closest position
	 * @return the closest position in the partitioner's category
	 */
	protected TypedPosition findClosestPosition(int offset) {

		try {

			int index= fDocument.computeIndexInCategory(fPositionCategory, offset);
			Position[] category= fDocument.getPositions(fPositionCategory);

			if (category.length == 0)
				return null;

			if (index < category.length) {
				if (offset == category[index].offset)
					return (TypedPosition) category[index];
			}

			if (index > 0)
				index--;

			return (TypedPosition) category[index];

		} catch (BadPositionCategoryException x) {
		} catch (BadLocationException x) {
		}

		return null;
	}


	/*
	 * @see IDocumentPartitioner#getContentType(int)
	 */
	public String getContentType(int offset) {
		checkInitialization();

		TypedPosition p= findClosestPosition(offset);
		if (p != null && p.includes(offset))
			return p.getType();

		return IDocument.DEFAULT_CONTENT_TYPE;
	}

	/*
	 * @see IDocumentPartitioner#getPartition(int)
	 */
	public ITypedRegion getPartition(int offset) {
		checkInitialization();

		try {

			Position[] category = fDocument.getPositions(fPositionCategory);

			if (category == null || category.length == 0)
				return new TypedRegion(0, fDocument.getLength(), IDocument.DEFAULT_CONTENT_TYPE);

			int index= fDocument.computeIndexInCategory(fPositionCategory, offset);

			if (index < category.length) {

				TypedPosition next= (TypedPosition) category[index];

				if (offset == next.offset)
					return new TypedRegion(next.getOffset(), next.getLength(), next.getType());

				if (index == 0)
					return new TypedRegion(0, next.offset, IDocument.DEFAULT_CONTENT_TYPE);

				TypedPosition previous= (TypedPosition) category[index - 1];
				if (previous.includes(offset))
					return new TypedRegion(previous.getOffset(), previous.getLength(), previous.getType());

				int endOffset= previous.getOffset() + previous.getLength();
				return new TypedRegion(endOffset, next.getOffset() - endOffset, IDocument.DEFAULT_CONTENT_TYPE);
			}

			TypedPosition previous= (TypedPosition) category[category.length - 1];
			if (previous.includes(offset))
				return new TypedRegion(previous.getOffset(), previous.getLength(), previous.getType());

			int endOffset= previous.getOffset() + previous.getLength();
			return new TypedRegion(endOffset, fDocument.getLength() - endOffset, IDocument.DEFAULT_CONTENT_TYPE);

		} catch (BadPositionCategoryException x) {
		} catch (BadLocationException x) {
		}

		return new TypedRegion(0, fDocument.getLength(), IDocument.DEFAULT_CONTENT_TYPE);
	}

	/*
	 * @see IDocumentPartitioner#computePartitioning(int, int)
	 */
	public ITypedRegion[] computePartitioning(int offset, int length) {
		return computePartitioning(offset, length, false);
	}

	/*
	 * @see IDocumentPartitioner#getLegalContentTypes()
	 */
	public String[] getLegalContentTypes() {
		return TextUtilities.copy(fLegalContentTypes);
	}

	/**
	 * Returns whether the given type is one of the legal content types.
	 *
	 * @param contentType the content type to check
	 * @return <code>true</code> if the content type is a legal content type
	 */
	protected boolean isSupportedContentType(String contentType) {
		if (contentType != null) {
			for (int i= 0; i < fLegalContentTypes.length; i++) {
				if (fLegalContentTypes[i].equals(contentType))
					return true;
			}
		}

		return false;
	}

	/**
	 * Returns a content type encoded in the given token. If the token's
	 * data is not <code>null</code> and a string it is assumed that
	 * it is the encoded content type.
	 *
	 * @param token the token whose content type is to be determined
	 * @return the token's content type
	 */
	protected String getTokenContentType(IToken token) {
		Object data= token.getData();
		if (data instanceof String)
			return (String) data;
		return null;
	}

    /* zero-length partition support */

	/*
	 * @see org.eclipse.jface.text.IDocumentPartitionerExtension2#getContentType(int)
	 * @since 3.0
	 */
	public String getContentType(int offset, boolean preferOpenPartitions) {
		return getPartition(offset, preferOpenPartitions).getType();
	}

	/*
	 * @see org.eclipse.jface.text.IDocumentPartitionerExtension2#getPartition(int)
	 * @since 3.0
	 */
	public ITypedRegion getPartition(int offset, boolean preferOpenPartitions) {
		ITypedRegion region= getPartition(offset);
		if (preferOpenPartitions) {
			if (region.getOffset() == offset && !region.getType().equals(IDocument.DEFAULT_CONTENT_TYPE)) {
				if (offset > 0) {
					region= getPartition(offset - 1);
					if (region.getType().equals(IDocument.DEFAULT_CONTENT_TYPE))
						return region;
				}
				return new TypedRegion(offset, 0, IDocument.DEFAULT_CONTENT_TYPE);
			}
		}
        return region;
	}

	/*
	 * @see org.eclipse.jface.text.IDocumentPartitionerExtension2#computePartitioning(int, int, boolean)
	 * @since 3.0
	 */
	public ITypedRegion[] computePartitioning(int offset, int length, boolean includeZeroLengthPartitions) {
		checkInitialization();
		List list= new ArrayList();

		try {

			int endOffset= offset + length;

			Position[] category= fDocument.getPositions(fPositionCategory);

			TypedPosition previous= null, current= null;
			int start, end, gapOffset;
			Position gap= new Position(0);

			int startIndex= getFirstIndexEndingAfterOffset(category, offset);
			int endIndex= getFirstIndexStartingAfterOffset(category, endOffset);
			for (int i= startIndex; i < endIndex; i++) {

				current= (TypedPosition) category[i];

				gapOffset= (previous != null) ? previous.getOffset() + previous.getLength() : 0;
				gap.setOffset(gapOffset);
				gap.setLength(current.getOffset() - gapOffset);
				if ((includeZeroLengthPartitions && overlapsOrTouches(gap, offset, length)) ||
						(gap.getLength() > 0 && gap.overlapsWith(offset, length))) {
					start= Math.max(offset, gapOffset);
					end= Math.min(endOffset, gap.getOffset() + gap.getLength());
					list.add(new TypedRegion(start, end - start, IDocument.DEFAULT_CONTENT_TYPE));
				}

				if (current.overlapsWith(offset, length)) {
					start= Math.max(offset, current.getOffset());
					end= Math.min(endOffset, current.getOffset() + current.getLength());
					list.add(new TypedRegion(start, end - start, current.getType()));
				}

				previous= current;
			}

			if (previous != null) {
				gapOffset= previous.getOffset() + previous.getLength();
				gap.setOffset(gapOffset);
				gap.setLength(fDocument.getLength() - gapOffset);
				if ((includeZeroLengthPartitions && overlapsOrTouches(gap, offset, length)) ||
						(gap.getLength() > 0 && gap.overlapsWith(offset, length))) {
					start= Math.max(offset, gapOffset);
					end= Math.min(endOffset, fDocument.getLength());
					list.add(new TypedRegion(start, end - start, IDocument.DEFAULT_CONTENT_TYPE));
				}
			}

			if (list.isEmpty())
				list.add(new TypedRegion(offset, length, IDocument.DEFAULT_CONTENT_TYPE));

		} catch (BadPositionCategoryException x) {
		}

		TypedRegion[] result= new TypedRegion[list.size()];
		list.toArray(result);
		return result;
	}

	/**
	 * Returns <code>true</code> if the given ranges overlap with or touch each other.
	 *
	 * @param gap the first range
	 * @param offset the offset of the second range
	 * @param length the length of the second range
	 * @return <code>true</code> if the given ranges overlap with or touch each other
	 * @since 3.0
	 */
	private boolean overlapsOrTouches(Position gap, int offset, int length) {
		return gap.getOffset() <= offset + length && offset <= gap.getOffset() + gap.getLength();
	}

	/**
	 * Returns the index of the first position which ends after the given offset.
	 *
	 * @param positions the positions in linear order
	 * @param offset the offset
	 * @return the index of the first position which ends after the offset
	 *
	 * @since 3.0
	 */
	private int getFirstIndexEndingAfterOffset(Position[] positions, int offset) {
		int i= -1, j= positions.length;
		while (j - i > 1) {
			int k= (i + j) >> 1;
			Position p= positions[k];
			if (p.getOffset() + p.getLength() > offset)
				j= k;
			else
				i= k;
		}
		return j;
	}

	/**
	 * Returns the index of the first position which starts at or after the given offset.
	 *
	 * @param positions the positions in linear order
	 * @param offset the offset
	 * @return the index of the first position which starts after the offset
	 *
	 * @since 3.0
	 */
	private int getFirstIndexStartingAfterOffset(Position[] positions, int offset) {
		int i= -1, j= positions.length;
		while (j - i > 1) {
			int k= (i + j) >> 1;
			Position p= positions[k];
			if (p.getOffset() >= offset)
				j= k;
			else
				i= k;
		}
		return j;
	}

	/*
	 * @see org.eclipse.jface.text.IDocumentPartitionerExtension3#startRewriteSession(org.eclipse.jface.text.DocumentRewriteSession)
	 * @since 3.1
	 */
	public void startRewriteSession(DocumentRewriteSession session) throws IllegalStateException {
		if (fActiveRewriteSession != null)
			throw new IllegalStateException();
		fActiveRewriteSession= session;
	}

	/*
	 * @see org.eclipse.jface.text.IDocumentPartitionerExtension3#stopRewriteSession(org.eclipse.jface.text.DocumentRewriteSession)
	 * @since 3.1
	 */
	public void stopRewriteSession(DocumentRewriteSession session) {
		if (fActiveRewriteSession == session)
			flushRewriteSession();
	}

	/*
	 * @see org.eclipse.jface.text.IDocumentPartitionerExtension3#getActiveRewriteSession()
	 * @since 3.1
	 */
	public DocumentRewriteSession getActiveRewriteSession() {
		return fActiveRewriteSession;
	}

	/**
	 * Flushes the active rewrite session.
	 *
	 * @since 3.1
	 */
	protected final void flushRewriteSession() {
		fActiveRewriteSession= null;

		// remove all position belonging to the partitioner position category
		try {
			fDocument.removePositionCategory(fPositionCategory);
		} catch (BadPositionCategoryException x) {
		}
		fDocument.addPositionCategory(fPositionCategory);

		fIsInitialized= false;
	}
}

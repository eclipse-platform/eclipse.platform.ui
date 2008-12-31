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

package org.eclipse.jface.text.formatter;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.TypedPosition;


/**
 * Standard implementation of <code>IContentFormatter</code>.
 * The formatter supports two operation modes: partition aware and
 * partition unaware. <p>
 * In the partition aware mode, the formatter determines the
 * partitioning of the document region to be formatted. For each
 * partition it determines all document positions  which are affected
 * when text changes are applied to the partition. Those which overlap
 * with the partition are remembered as character positions. These
 * character positions are passed over to the formatting strategy
 * registered for the partition's content type. The formatting strategy
 * returns a string containing the formatted document partition as well
 * as the adapted character positions. The formatted partition replaces
 * the old content of the partition. The remembered document positions
 * are updated with the adapted character positions. In addition, all
 * other document positions are accordingly adapted to the formatting
 * changes.<p>
 * In the partition unaware mode, the document's partitioning is ignored
 * and the document is considered consisting of only one partition of
 * the content type <code>IDocument.DEFAULT_CONTENT_TYPE</code>. The
 * formatting process is similar to the partition aware mode, with the
 * exception of having only one partition.<p>
 * Usually, clients instantiate this class and configure it before using it.
 *
 * @see IContentFormatter
 * @see IDocument
 * @see ITypedRegion
 * @see Position
 */
public class ContentFormatter implements IContentFormatter {

	/**
	 * Defines a reference to either the offset or the end offset of
	 * a particular position.
	 */
	static class PositionReference implements Comparable {

		/** The referenced position */
		protected Position fPosition;
		/** The reference to either the offset or the end offset */
		protected boolean fRefersToOffset;
		/** The original category of the referenced position */
		protected String fCategory;

		/**
		 * Creates a new position reference.
		 *
		 * @param position the position to be referenced
		 * @param refersToOffset <code>true</code> if position offset should be referenced
		 * @param category the category the given position belongs to
		 */
		protected PositionReference(Position position, boolean refersToOffset, String category) {
			fPosition= position;
			fRefersToOffset= refersToOffset;
			fCategory= category;
		}

		/**
		 * Returns the offset of the referenced position.
		 *
		 * @return the offset of the referenced position
		 */
		protected int getOffset() {
			return fPosition.getOffset();
		}

		/**
		 * Manipulates the offset of the referenced position.
		 *
		 * @param offset the new offset of the referenced position
		 */
		protected void setOffset(int offset) {
			fPosition.setOffset(offset);
		}

		/**
		 * Returns the length of the referenced position.
		 *
		 * @return the length of the referenced position
		 */
		protected int getLength() {
			return fPosition.getLength();
		}

		/**
		 * Manipulates the length of the referenced position.
		 *
		 * @param length the new length of the referenced position
		 */
		protected void setLength(int length) {
			fPosition.setLength(length);
		}

		/**
		 * Returns whether this reference points to the offset or end offset
		 * of the references position.
		 *
		 * @return <code>true</code> if the offset of the position is referenced, <code>false</code> otherwise
		 */
		protected boolean refersToOffset() {
			return fRefersToOffset;
		}

		/**
		 * Returns the category of the referenced position.
		 *
		 * @return the category of the referenced position
		 */
		protected String getCategory() {
			return fCategory;
		}

		/**
		 * Returns the referenced position.
		 *
		 * @return the referenced position
		 */
		protected Position getPosition() {
			return fPosition;
		}

		/**
		 * Returns the referenced character position
		 *
		 * @return the referenced character position
		 */
		protected int getCharacterPosition() {
			if (fRefersToOffset)
				return getOffset();
			return getOffset() + getLength();
		}

		/*
		 * @see Comparable#compareTo(Object)
		 */
		public int compareTo(Object obj) {

			if (obj instanceof PositionReference) {
				PositionReference r= (PositionReference) obj;
				return getCharacterPosition() - r.getCharacterPosition();
			}

			throw new ClassCastException();
		}
	}

	/**
	 * The position updater used to update the remembered partitions.
	 *
	 * @see IPositionUpdater
	 * @see DefaultPositionUpdater
	 */
	class NonDeletingPositionUpdater extends DefaultPositionUpdater {

		/**
		 * Creates a new updater for the given category.
		 *
		 * @param category the category
		 */
		protected NonDeletingPositionUpdater(String category) {
			super(category);
		}

		/*
		 * @see DefaultPositionUpdater#notDeleted()
		 */
		protected boolean notDeleted() {
			return true;
		}
	}

	/**
	 * The position updater which runs as first updater on the document's positions.
	 * Used to remove all affected positions from their categories to avoid them
	 * from being regularly updated.
	 *
	 * @see IPositionUpdater
	 */
	class RemoveAffectedPositions implements IPositionUpdater {
		/*
		 * @see IPositionUpdater#update(DocumentEvent)
		 */
		public void update(DocumentEvent event) {
			removeAffectedPositions(event.getDocument());
		}
	}

	/**
	 * The position updater which runs as last updater on the document's positions.
	 * Used to update all affected positions and adding them back to their
	 * original categories.
	 *
	 * @see IPositionUpdater
	 */
	class UpdateAffectedPositions implements IPositionUpdater {

		/** The affected positions */
		private int[] fPositions;
		/** The offset */
		private int fOffset;

		/**
		 * Creates a new updater.
		 *
		 * @param positions the affected positions
		 * @param offset the offset
		 */
		public UpdateAffectedPositions(int[] positions, int offset) {
			fPositions= positions;
			fOffset= offset;
		}

		/*
		 * @see IPositionUpdater#update(DocumentEvent)
		 */
		public void update(DocumentEvent event) {
			updateAffectedPositions(event.getDocument(), fPositions, fOffset);
		}
	}


	/** Internal position category used for the formatter partitioning */
	private final static String PARTITIONING= "__formatter_partitioning"; //$NON-NLS-1$

	/** The map of <code>IFormattingStrategy</code> objects */
	private Map fStrategies;
	/** The indicator of whether the formatter operates in partition aware mode or not */
	private boolean fIsPartitionAware= true;

	/** The partition information managing document position categories */
	private String[] fPartitionManagingCategories;
	/** The list of references to offset and end offset of all overlapping positions */
	private List fOverlappingPositionReferences;
	/** Position updater used for partitioning positions */
	private IPositionUpdater fPartitioningUpdater;
	/**
	 * The document partitioning used by this formatter.
	 * @since 3.0
	 */
	private String fPartitioning;
	/**
	 * The document this formatter works on.
	 * @since 3.0
	 */
	private IDocument fDocument;
	/**
	 * The external partition managing categories.
	 * @since 3.0
	 */
	private String[] fExternalPartitonManagingCategories;
	/**
	 * Indicates whether <code>fPartitionManagingCategories</code> must be computed.
	 * @since 3.0
	 */
	private boolean fNeedsComputation= true;


	/**
	 * Creates a new content formatter. The content formatter operates by default
	 * in the partition-aware mode. There are no preconfigured formatting strategies.
	 * Will use the default document partitioning if not further configured.
	 */
	public ContentFormatter() {
		fPartitioning= IDocumentExtension3.DEFAULT_PARTITIONING;
	}

	/**
	 * Registers a strategy for a particular content type. If there is already a strategy
	 * registered for this type, the new strategy is registered instead of the old one.
	 * If the given content type is <code>null</code> the given strategy is registered for
	 * all content types as is called only once per formatting session.
	 *
	 * @param strategy the formatting strategy to register, or <code>null</code> to remove an existing one
	 * @param contentType the content type under which to register
	 */
	public void setFormattingStrategy(IFormattingStrategy strategy, String contentType) {

		Assert.isNotNull(contentType);

		if (fStrategies == null)
			fStrategies= new HashMap();

		if (strategy == null)
			fStrategies.remove(contentType);
		else
			fStrategies.put(contentType, strategy);
	}

	/**
	 * Informs this content formatter about the names of those position categories
	 * which are used to manage the document's partitioning information and thus should
	 * be ignored when this formatter updates positions.
	 *
	 * @param categories the categories to be ignored
	 * @deprecated incompatible with an open set of document partitionings. The provided information is only used
	 * 		if this formatter can not compute the partition managing position categories.
	 */
	public void setPartitionManagingPositionCategories(String[] categories) {
		fExternalPartitonManagingCategories= TextUtilities.copy(categories);
	}

	/**
	 * Sets the document partitioning to be used by this formatter.
	 *
	 * @param partitioning the document partitioning
	 * @since 3.0
	 */
	public void setDocumentPartitioning(String partitioning) {
		fPartitioning= partitioning;
	}

	/**
	 * Sets the formatter's operation mode.
	 *
	 * @param enable indicates whether the formatting process should be partition ware
	 */
	public void enablePartitionAwareFormatting(boolean enable) {
		fIsPartitionAware= enable;
	}

	/*
	 * @see IContentFormatter#getFormattingStrategy(String)
	 */
	public IFormattingStrategy getFormattingStrategy(String contentType) {

		Assert.isNotNull(contentType);

		if (fStrategies == null)
			return null;

		return (IFormattingStrategy) fStrategies.get(contentType);
	}

	/*
	 * @see IContentFormatter#format(IDocument, IRegion)
	 */
	public void format(IDocument document, IRegion region) {
		fNeedsComputation= true;
		fDocument= document;
		try {

			if (fIsPartitionAware)
				formatPartitions(region);
			else
				formatRegion(region);

		} finally {
			fNeedsComputation= true;
			fDocument= null;
		}
	}

	/**
	 * Determines the partitioning of the given region of the document.
	 * Informs the formatting strategies of each partition about the start,
	 * the process, and the termination of the formatting session.
	 *
	 * @param region the document region to be formatted
	 * @since 3.0
	 */
	private void formatPartitions(IRegion region) {

		addPartitioningUpdater();

		try {

			TypedPosition[] ranges= getPartitioning(region);
			if (ranges != null) {
				start(ranges, getIndentation(region.getOffset()));
				format(ranges);
				stop(ranges);
			}

		} catch (BadLocationException x) {
		}

		removePartitioningUpdater();
	}

	/**
	 * Formats the given region with the strategy registered for the default
	 * content type. The strategy is informed about the start, the process, and
	 * the termination of the formatting session.
	 *
	 * @param region the region to be formatted
	 * @since 3.0
	 */
	private void formatRegion(IRegion region) {

		IFormattingStrategy strategy= getFormattingStrategy(IDocument.DEFAULT_CONTENT_TYPE);
		if (strategy != null) {
			strategy.formatterStarts(getIndentation(region.getOffset()));
			format(strategy, new TypedPosition(region.getOffset(), region.getLength(), IDocument.DEFAULT_CONTENT_TYPE));
			strategy.formatterStops();
		}
	}

	/**
	 * Returns the partitioning of the given region of the document to be formatted.
	 * As one partition after the other will be formatted and formatting will
	 * probably change the length of the formatted partition, it must be kept
	 * track of the modifications in order to submit the correct partition to all
	 * formatting strategies. For this, all partitions are remembered as positions
	 * in a dedicated position category. (As formatting strategies might rely on each
	 * other, calling them in reversed order is not an option.)
	 *
	 * @param region the region for which the partitioning must be determined
	 * @return the partitioning of the specified region
	 * @exception BadLocationException of region is invalid in the document
	 * @since 3.0
	 */
	private TypedPosition[] getPartitioning(IRegion region) throws BadLocationException {

		ITypedRegion[] regions= TextUtilities.computePartitioning(fDocument, fPartitioning, region.getOffset(), region.getLength(), false);
		TypedPosition[] positions= new TypedPosition[regions.length];

		for (int i= 0; i < regions.length; i++) {
			positions[i]= new TypedPosition(regions[i]);
			try {
				fDocument.addPosition(PARTITIONING, positions[i]);
			} catch (BadPositionCategoryException x) {
				// should not happen
			}
		}

		return positions;
	}

	/**
	 * Fires <code>formatterStarts</code> to all formatter strategies
	 * which will be involved in the forthcoming formatting process.
	 *
	 * @param regions the partitioning of the document to be formatted
	 * @param indentation the initial indentation
	 */
	private void start(TypedPosition[] regions, String indentation) {
		for (int i= 0; i < regions.length; i++) {
			IFormattingStrategy s= getFormattingStrategy(regions[i].getType());
			if (s != null)
				s.formatterStarts(indentation);
		}
	}

	/**
	 * Formats one partition after the other using the formatter strategy registered for
	 * the partition's content type.
	 *
	 * @param ranges the partitioning of the document region to be formatted
	 * @since 3.0
	 */
	private void format(TypedPosition[] ranges) {
		for (int i= 0; i < ranges.length; i++) {
			IFormattingStrategy s= getFormattingStrategy(ranges[i].getType());
			if (s != null) {
				format(s, ranges[i]);
			}
		}
	}

	/**
	 * Formats the given region of the document using the specified formatting
	 * strategy. In order to maintain positions correctly, first all affected
	 * positions determined, after all document listeners have been informed about
	 * the coming change, the affected positions are removed to avoid that they
	 * are regularly updated. After all position updaters have run, the affected
	 * positions are updated with the formatter's information and added back to
	 * their categories, right before the first document listener is informed about
	 * that a change happened.
	 *
	 * @param strategy the strategy to be used
	 * @param region the region to be formatted
	 * @since 3.0
	 */
	private void format(IFormattingStrategy strategy, TypedPosition region) {
		try {

			final int offset= region.getOffset();
			int length= region.getLength();

			String content= fDocument.get(offset, length);
			final int[] positions= getAffectedPositions(offset, length);
			String formatted= strategy.format(content, isLineStart(offset), getIndentation(offset), positions);

			if (formatted != null && !formatted.equals(content)) {

				IPositionUpdater first= new RemoveAffectedPositions();
				fDocument.insertPositionUpdater(first, 0);
				IPositionUpdater last= new UpdateAffectedPositions(positions, offset);
				fDocument.addPositionUpdater(last);

				fDocument.replace(offset, length, formatted);

				fDocument.removePositionUpdater(first);
				fDocument.removePositionUpdater(last);
			}

		} catch (BadLocationException x) {
			// should not happen
		}
	}

	/**
	 * Fires <code>formatterStops</code> to all formatter strategies which were
	 * involved in the formatting process which is about to terminate.
	 *
	 * @param regions the partitioning of the document which has been formatted
	 */
	private void stop(TypedPosition[] regions) {
		for (int i= 0; i < regions.length; i++) {
			IFormattingStrategy s= getFormattingStrategy(regions[i].getType());
			if (s != null)
				s.formatterStops();
		}
	}

	/**
	 * Installs those updaters which the formatter needs to keep track of the partitions.
	 * @since 3.0
	 */
	private void addPartitioningUpdater() {
		fPartitioningUpdater= new NonDeletingPositionUpdater(PARTITIONING);
		fDocument.addPositionCategory(PARTITIONING);
		fDocument.addPositionUpdater(fPartitioningUpdater);
	}

	/**
	 * Removes the formatter's internal position updater and category.
	 *
	 * @since 3.0
	 */
	private void removePartitioningUpdater() {

		try {

			fDocument.removePositionUpdater(fPartitioningUpdater);
			fDocument.removePositionCategory(PARTITIONING);
			fPartitioningUpdater= null;

		} catch (BadPositionCategoryException x) {
			// should not happen
		}
	}

	/**
	 * Returns the partition managing position categories for the formatted document.
	 *
	 * @return the position managing position categories
	 * @since 3.0
	 */
	private String[] getPartitionManagingCategories() {
		if (fNeedsComputation) {
			fNeedsComputation= false;
			fPartitionManagingCategories= TextUtilities.computePartitionManagingCategories(fDocument);
			if (fPartitionManagingCategories == null)
				fPartitionManagingCategories= fExternalPartitonManagingCategories;
		}
		return fPartitionManagingCategories;
	}

	/**
	 * Determines whether the given document position category should be ignored
	 * by this formatter's position updating.
	 *
	 * @param category the category to check
	 * @return <code>true</code> if the category should be ignored, <code>false</code> otherwise
	 */
	private boolean ignoreCategory(String category) {

		if (PARTITIONING.equals(category))
			return true;

		String[] categories= getPartitionManagingCategories();
		if (categories != null) {
			for (int i= 0; i < categories.length; i++) {
				if (categories[i].equals(category))
					return true;
			}
		}

		return false;
	}

	/**
	 * Determines all embracing, overlapping, and follow up positions
	 * for the given region of the document.
	 *
	 * @param offset the offset of the document region to be formatted
	 * @param length the length of the document to be formatted
	 * @since 3.0
	 */
	private void determinePositionsToUpdate(int offset, int length) {

		String[] categories= fDocument.getPositionCategories();
		if (categories != null) {
			for (int i= 0; i < categories.length; i++) {

				if (ignoreCategory(categories[i]))
					continue;

				try {

					Position[] positions= fDocument.getPositions(categories[i]);

					for (int j= 0; j < positions.length; j++) {

						Position p= positions[j];
						if (p.overlapsWith(offset, length)) {

							if (offset < p.getOffset())
								fOverlappingPositionReferences.add(new PositionReference(p, true, categories[i]));

							if (p.getOffset() + p.getLength() < offset + length)
								fOverlappingPositionReferences.add(new PositionReference(p, false, categories[i]));
						}
					}

				} catch (BadPositionCategoryException x) {
					// can not happen
				}
			}
		}
	}

	/**
	 * Returns all offset and the end offset of all positions overlapping with the
	 * specified document range.
	 *
	 * @param offset the offset of the document region to be formatted
	 * @param length the length of the document to be formatted
	 * @return all character positions of the interleaving positions
	 * @since 3.0
	 */
	private int[] getAffectedPositions(int offset, int length) {

		fOverlappingPositionReferences= new ArrayList();

		determinePositionsToUpdate(offset, length);

		Collections.sort(fOverlappingPositionReferences);

		int[] positions= new int[fOverlappingPositionReferences.size()];
		for (int i= 0; i < positions.length; i++) {
			PositionReference r= (PositionReference) fOverlappingPositionReferences.get(i);
			positions[i]= r.getCharacterPosition() - offset;
		}

		return positions;
	}

	/**
	 * Removes the affected positions from their categories to avoid
	 * that they are invalidly updated.
	 *
	 * @param document the document
	 */
	private void removeAffectedPositions(IDocument document) {
		int size= fOverlappingPositionReferences.size();
		for (int i= 0; i < size; i++) {
			PositionReference r= (PositionReference) fOverlappingPositionReferences.get(i);
			try {
				document.removePosition(r.getCategory(), r.getPosition());
			} catch (BadPositionCategoryException x) {
				// can not happen
			}
		}
	}

	/**
	 * Updates all the overlapping positions. Note, all other positions are
	 * automatically updated by their document position updaters.
	 *
	 * @param document the document to has been formatted
	 * @param positions the adapted character positions to be used to update the document positions
	 * @param offset the offset of the document region that has been formatted
	 */
	protected void updateAffectedPositions(IDocument document, int[] positions, int offset) {

		if (document != fDocument)
			return;

		if (positions.length == 0)
			return;

		for (int i= 0; i < positions.length; i++) {

			PositionReference r= (PositionReference) fOverlappingPositionReferences.get(i);

			if (r.refersToOffset())
				r.setOffset(offset + positions[i]);
			else
				r.setLength((offset + positions[i]) - r.getOffset());

			Position p= r.getPosition();
			String category= r.getCategory();
			if (!document.containsPosition(category, p.offset, p.length)) {
				try {
					if (positionAboutToBeAdded(document, category, p))
						document.addPosition(r.getCategory(), p);
				} catch (BadPositionCategoryException x) {
					// can not happen
				} catch (BadLocationException x) {
					// should not happen
				}
			}

		}

		fOverlappingPositionReferences= null;
	}

	/**
	 * The given position is about to be added to the given position category of the given document. <p>
	 * This default implementation return <code>true</code>.
	 *
	 * @param document the document
	 * @param category the position category
	 * @param position the position that will be added
	 * @return <code>true</code> if the position can be added, <code>false</code> if it should be ignored
	 */
	protected boolean positionAboutToBeAdded(IDocument document, String category, Position position) {
		return true;
	}

	/**
	 * Returns the indentation of the line of the given offset.
	 *
	 * @param offset the offset
	 * @return the indentation of the line of the offset
	 * @since 3.0
	 */
	private String getIndentation(int offset) {

		try {
			int start= fDocument.getLineOfOffset(offset);
			start= fDocument.getLineOffset(start);

			int end= start;
			char c= fDocument.getChar(end);
			while ('\t' == c || ' ' == c)
				c= fDocument.getChar(++end);

			return fDocument.get(start, end - start);
		} catch (BadLocationException x) {
		}

		return ""; //$NON-NLS-1$
	}

	/**
	 * Determines whether the offset is the beginning of a line in the given document.
	 *
	 * @param offset the offset
	 * @return <code>true</code> if offset is the beginning of a line
	 * @exception BadLocationException if offset is invalid in document
	 * @since 3.0
	 */
	private boolean isLineStart(int offset) throws BadLocationException {
		int start= fDocument.getLineOfOffset(offset);
		start= fDocument.getLineOffset(start);
		return (start == offset);
	}
}

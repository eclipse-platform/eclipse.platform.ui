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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.TypedPosition;

/**
 * Content formatter for edit-based formatting strategies.
 * <p>
 * Two kinds of formatting strategies can be registered with this formatter:
 * <ul>
 * <li>one master formatting strategy for the default content type</li>
 * <li>one formatting strategy for each non-default content type</li>
 * </ul>
 * The master formatting strategy always formats the whole region to be
 * formatted in the first pass. In a second pass, all partitions of the region
 * to be formatted that are not of master content type are formatted using the
 * slave formatting strategy registered for the underlying content type. All
 * formatting strategies must implement {@link IFormattingStrategyExtension}.
 * <p>
 * Regions to be formatted with the master formatting strategy always have
 * an offset aligned to the line start. Regions to be formatted with slave formatting
 * strategies are aligned on partition boundaries.
 *
 * @see IFormattingStrategyExtension
 * @since 3.0
 */
public class MultiPassContentFormatter implements IContentFormatter, IContentFormatterExtension {

	/**
	 * Position updater that shifts otherwise deleted positions to the next
	 * non-whitespace character. The length of the positions are truncated to
	 * one if the position was shifted.
	 */
	protected class NonDeletingPositionUpdater extends DefaultPositionUpdater {

		/**
		 * Creates a new non-deleting position updater.
		 *
		 * @param category The position category to update its positions
		 */
		public NonDeletingPositionUpdater(final String category) {
			super(category);
		}

		/*
		 * @see org.eclipse.jface.text.DefaultPositionUpdater#notDeleted()
		 */
		protected final boolean notDeleted() {

			if (fOffset < fPosition.offset && (fPosition.offset + fPosition.length < fOffset + fLength)) {

				int offset= fOffset + fLength;
				if (offset < fDocument.getLength()) {

					try {

						boolean moved= false;
						char character= fDocument.getChar(offset);

						while (offset < fDocument.getLength() && Character.isWhitespace(character)) {

							moved= true;
							character= fDocument.getChar(offset++);
						}

						if (moved)
							offset--;

					} catch (BadLocationException exception) {
						// Can not happen
					}

					fPosition.offset= offset;
					fPosition.length= 0;
				}
			}
			return true;
		}
	}

	/** The master formatting strategy */
	private IFormattingStrategyExtension fMaster= null;
	/** The partitioning of this content formatter */
	private final String fPartitioning;
	/** The slave formatting strategies */
	private final Map fSlaves= new HashMap();
	/** The default content type */
	private final String fType;

	/**
	 * Creates a new content formatter.
	 *
	 * @param partitioning the document partitioning for this formatter
	 * @param type the default content type
	 */
	public MultiPassContentFormatter(final String partitioning, final String type) {
		fPartitioning= partitioning;
		fType= type;
	}

	/*
	 * @see org.eclipse.jface.text.formatter.IContentFormatterExtension#format(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.formatter.IFormattingContext)
	 */
	public final void format(final IDocument medium, final IFormattingContext context) {

		context.setProperty(FormattingContextProperties.CONTEXT_MEDIUM, medium);

		final Boolean document= (Boolean)context.getProperty(FormattingContextProperties.CONTEXT_DOCUMENT);
		if (document == null || !document.booleanValue()) {

			final IRegion region= (IRegion)context.getProperty(FormattingContextProperties.CONTEXT_REGION);
			if (region != null) {
				try {
					formatMaster(context, medium, region.getOffset(), region.getLength());
				} finally {
					formatSlaves(context, medium, region.getOffset(), region.getLength());
				}
			}
		} else {
			try {
				formatMaster(context, medium, 0, medium.getLength());
			} finally {
				formatSlaves(context, medium, 0, medium.getLength());
			}
		}
	}

	/*
	 * @see org.eclipse.jface.text.formatter.IContentFormatter#format(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.IRegion)
	 */
	public final void format(final IDocument medium, final IRegion region) {

		final FormattingContext context= new FormattingContext();

		context.setProperty(FormattingContextProperties.CONTEXT_DOCUMENT, Boolean.FALSE);
		context.setProperty(FormattingContextProperties.CONTEXT_REGION, region);

		format(medium, context);
	}

	/**
	 * Formats the document specified in the formatting context with the master
	 * formatting strategy.
	 * <p>
	 * The master formatting strategy covers all regions of the document. The
	 * offset of the region to be formatted is aligned on line start boundaries,
	 * whereas the end index of the region remains the same. For this formatting
	 * type the document partitioning is not taken into account.
	 *
	 * @param context The formatting context to use
	 * @param document The document to operate on
	 * @param offset The offset of the region to format
	 * @param length The length of the region to format
	 */
	protected void formatMaster(final IFormattingContext context, final IDocument document, int offset, int length) {

		try {

			final int delta= offset - document.getLineInformationOfOffset(offset).getOffset();
			offset -= delta;
			length += delta;

		} catch (BadLocationException exception) {
			// Do nothing
		}

		if (fMaster != null) {

			context.setProperty(FormattingContextProperties.CONTEXT_PARTITION, new TypedPosition(offset, length, fType));

			fMaster.formatterStarts(context);
			fMaster.format();
			fMaster.formatterStops();
		}
	}

	/**
	 * Formats the document specified in the formatting context with the
	 * formatting strategy registered for the content type.
	 * <p>
	 * For this formatting type only slave strategies are used. The region to be
	 * formatted is aligned on partition boundaries of the underlying content
	 * type. The exact formatting strategy is determined by the underlying
	 * content type of the document partitioning.
	 *
	 * @param context The formatting context to use
	 * @param document The document to operate on
	 * @param offset The offset of the region to format
	 * @param length The length of the region to format
	 * @param type The content type of the region to format
	 */
	protected void formatSlave(final IFormattingContext context, final IDocument document, final int offset, final int length, final String type) {

		final IFormattingStrategyExtension strategy= (IFormattingStrategyExtension)fSlaves.get(type);
		if (strategy != null) {

			context.setProperty(FormattingContextProperties.CONTEXT_PARTITION, new TypedPosition(offset, length, type));

			strategy.formatterStarts(context);
			strategy.format();
			strategy.formatterStops();
		}
	}

	/**
	 * Formats the document specified in the formatting context with the slave
	 * formatting strategies.
	 * <p>
	 * For each content type of the region to be formatted in the document
	 * partitioning, the registered slave formatting strategy is used to format
	 * that particular region. The region to be formatted is aligned on
	 * partition boundaries of the underlying content type. If the content type
	 * is the document's default content type, nothing happens.
	 *
	 * @param context The formatting context to use
	 * @param document The document to operate on
	 * @param offset The offset of the region to format
	 * @param length The length of the region to format
	 */
	protected void formatSlaves(final IFormattingContext context, final IDocument document, final int offset, final int length) {

		Map partitioners= new HashMap(0);
		try {

			final ITypedRegion[] partitions= TextUtilities.computePartitioning(document, fPartitioning, offset, length, false);

			if (!fType.equals(partitions[0].getType()))
				partitions[0]= TextUtilities.getPartition(document, fPartitioning, partitions[0].getOffset(), false);

			if (partitions.length > 1) {

				if (!fType.equals(partitions[partitions.length - 1].getType()))
					partitions[partitions.length - 1]= TextUtilities.getPartition(document, fPartitioning, partitions[partitions.length - 1].getOffset(), false);
			}

			String type= null;
			ITypedRegion partition= null;

			partitioners= TextUtilities.removeDocumentPartitioners(document);

			for (int index= partitions.length - 1; index >= 0; index--) {

				partition= partitions[index];
				type= partition.getType();

				if (!fType.equals(type))
					formatSlave(context, document, partition.getOffset(), partition.getLength(), type);
			}

		} catch (BadLocationException exception) {
			// Should not happen
		} finally {
			TextUtilities.addDocumentPartitioners(document, partitioners);
		}
	}

	/*
	 * @see org.eclipse.jface.text.formatter.IContentFormatter#getFormattingStrategy(java.lang.String)
	 */
	public final IFormattingStrategy getFormattingStrategy(final String type) {
		return null;
	}

	/**
	 * Registers a master formatting strategy.
	 * <p>
	 * The strategy may already be registered with a certain content type as
	 * slave strategy. The master strategy is registered for the default content
	 * type of documents. If a master strategy has already been registered, it
	 * is overridden by the new one.
	 *
	 * @param strategy The master formatting strategy, must implement
	 * 	{@link IFormattingStrategyExtension}
	 */
	public final void setMasterStrategy(final IFormattingStrategy strategy) {
		Assert.isTrue(strategy instanceof IFormattingStrategyExtension);
		fMaster= (IFormattingStrategyExtension) strategy;
	}

	/**
	 * Registers a slave formatting strategy for a certain content type.
	 * <p>
	 * The strategy may already be registered as master strategy. An
	 * already registered slave strategy for the specified content type
	 * will be replaced. However, the same strategy may be registered with
	 * several content types. Slave strategies cannot be registered for the
	 * default content type of documents.
	 *
	 * @param strategy The slave formatting strategy
	 * @param type The content type to register this strategy with,
	 * 	must implement {@link IFormattingStrategyExtension}
	 */
	public final void setSlaveStrategy(final IFormattingStrategy strategy, final String type) {
		Assert.isTrue(strategy instanceof IFormattingStrategyExtension);
		if (!fType.equals(type))
			fSlaves.put(type, strategy);
	}
}

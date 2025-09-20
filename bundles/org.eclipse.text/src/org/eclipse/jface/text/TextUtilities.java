/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.AbstractLineTracker.DelimiterInfo;

/**
 * A collection of text functions.
 * <p>
 * This class is neither intended to be instantiated nor subclassed.
 * </p>
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class TextUtilities {

	/**
	 * Default line delimiters used by the text functions of this class.
	 */
	// Note: nextDelimiter implementation is sensitive to element order
	public final static String[] DELIMITERS= new String[] { "\n", "\r", "\r\n" }; //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$

	/**
	 * Default line delimiters used by these text functions.
	 *
	 * @deprecated use DELIMITERS instead
	 */
	@Deprecated
	public final static String[] fgDelimiters= DELIMITERS;



	/**
	 * Determines which one of default line delimiters appears first in the list. If none of them the
	 * hint is returned.
	 *
	 * @param text the text to be checked
	 * @param hint the line delimiter hint
	 * @return the line delimiter
	 */
	public static String determineLineDelimiter(String text, String hint) {
		String delimiter = nextDelimiter(text, 0).delimiter;
		return delimiter != null ? delimiter : hint;
	}

	/**
	 * Returns the starting position and the index of the first matching search string in the given
	 * text that is greater than the given offset. If more than one search string matches with the
	 * same starting position then the longest one is returned.
	 *
	 * @param searchStrings the strings to search for
	 * @param text the text to be searched
	 * @param offset the offset at which to start the search
	 * @return an <code>int[]</code> with two elements where the first is the starting offset, the
	 *         second the index of the found search string in the given <code>searchStrings</code>
	 *         array, returns <code>[-1, -1]</code> if no match exists
	 * @deprecated use {@link MultiStringMatcher#indexOf(CharSequence, int, String...)} instead.
	 *             Notable differences:
	 *             <ul>
	 *             <li>new matcher indexOf does not allow negative offsets (old matcher treated them
	 *             as <code>0</code>)</li>
	 *             <li>new matcher indexOf will tolerate <code>null</code> and empty search strings
	 *             (old accepted empty but throw on <code>null</code>)</li>
	 *             <li>new matcher indexOf will <b>not</b> match empty string (old matched empty if
	 *             nothing else matched)</li>
	 *             </ul>
	 *             For the common case of searching the next default {@link #DELIMITERS delimiter}
	 *             use the optimized {@link #nextDelimiter(CharSequence, int)} method instead.
	 */
	@Deprecated
	public static int[] indexOf(String[] searchStrings, String text, int offset) {
		// For compatibility this will throw a NullPointerException like the old implementation
		// (instead of an IllegalArgumentException what would be the result from MultiStringMatcher.indexOf)
		// and mimic the strange result for empty search string match from the old method.
		Objects.requireNonNull(searchStrings);
		for (String searchString : searchStrings) {
			Objects.requireNonNull(searchString);
		}
		if (offset < 0) {
			offset = 0; // for compatibility with old implementation
		}
		final MultiStringMatcher.Match match= MultiStringMatcher.indexOf(text, offset, searchStrings);
		if (match != null) {
			for (int i= 0; i < searchStrings.length; i++) {
				if (match.getText().equals(searchStrings[i])) {
					return new int[] { match.getOffset(), i };
				}
			}
		} else {
			// no match must check for empty search strings and mimic old return value
			// search reversed because we want the last empty search string
			for (int i= searchStrings.length - 1; i >= 0; i--) {
				if (searchStrings[i].length() == 0) {
					return new int[] { 0, i };
				}
			}
		}
		return new int[] { -1, -1 };
	}

	/**
	 * Returns the index of the longest search string with which the given text ends or
	 * <code>-1</code> if none matches.
	 *
	 * @param searchStrings the strings to search for
	 * @param text the text to search
	 * @return the index in <code>searchStrings</code> of the longest string with which <code>text</code> ends or <code>-1</code>
	 */
	public static int endsWith(String[] searchStrings, String text) {

		int index= -1;

		for (int i= 0; i < searchStrings.length; i++) {
			if (text.endsWith(searchStrings[i])) {
				if (index == -1 || searchStrings[i].length() > searchStrings[index].length()) {
					index= i;
				}
			}
		}

		return index;
	}

	/**
	 * Returns the index of the longest search string with which the given text starts or <code>-1</code>
	 * if none matches.
	 *
	 * @param searchStrings the strings to search for
	 * @param text the text to search
	 * @return the index in <code>searchStrings</code> of the longest string with which <code>text</code> starts or <code>-1</code>
	 */
	public static int startsWith(String[] searchStrings, String text) {

		int index= -1;

		for (int i= 0; i < searchStrings.length; i++) {
			if (text.startsWith(searchStrings[i])) {
				if (index == -1 || searchStrings[i].length() > searchStrings[index].length()) {
					index= i;
				}
			}
		}

		return index;
	}

	/**
	 * Returns the index of the first compare string that equals the given text or <code>-1</code>
	 * if none is equal.
	 *
	 * @param compareStrings the strings to compare with
	 * @param text the text to check
	 * @return the index of the first equal compare string or <code>-1</code>
	 */
	public static int equals(String[] compareStrings, String text) {
		for (int i= 0; i < compareStrings.length; i++) {
			if (text.equals(compareStrings[i])) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Returns a document event which is an accumulation of a list of document events,
	 * <code>null</code> if the list of documentEvents is empty.
	 * The document of the document events are ignored.
	 *
	 * @param unprocessedDocument the document to which the document events would be applied
	 * @param documentEvents the list of document events to merge
	 * @return returns the merged document event
	 * @throws BadLocationException might be thrown if document is not in the correct state with respect to document events
	 */
	public static DocumentEvent mergeUnprocessedDocumentEvents(IDocument unprocessedDocument, List<? extends DocumentEvent> documentEvents) throws BadLocationException {

		if (documentEvents.isEmpty()) {
			return null;
		}

		final Iterator<? extends DocumentEvent> iterator= documentEvents.iterator();
		final DocumentEvent firstEvent= iterator.next();

		// current merged event
		final IDocument document= unprocessedDocument;
		int offset= firstEvent.getOffset();
		int length= firstEvent.getLength();
		final StringBuilder text= new StringBuilder(firstEvent.getText() == null ? "" : firstEvent.getText()); //$NON-NLS-1$

		while (iterator.hasNext()) {

			final int delta= text.length() - length;

			final DocumentEvent event= iterator.next();
			final int eventOffset= event.getOffset();
			final int eventLength= event.getLength();
			final String eventText= event.getText() == null ? "" : event.getText(); //$NON-NLS-1$

			// event is right from merged event
			if (eventOffset > offset + length + delta) {
				final String string= document.get(offset + length, (eventOffset - delta) - (offset + length));
				text.append(string);
				text.append(eventText);

				length= (eventOffset - delta) + eventLength - offset;

			// event is left from merged event
			} else if (eventOffset + eventLength < offset) {
				final String string= document.get(eventOffset + eventLength, offset - (eventOffset + eventLength));
				text.insert(0, string);
				text.insert(0, eventText);

				length= offset + length - eventOffset;
				offset= eventOffset;

			// events overlap each other
			} else {
				final int start= Math.max(0, eventOffset - offset);
				final int end= Math.min(text.length(), eventLength + eventOffset - offset);
				text.replace(start, end, eventText);

				offset= Math.min(offset, eventOffset);
				final int totalDelta= delta + eventText.length() - eventLength;
				length= text.length() - totalDelta;
			}
		}

		return new DocumentEvent(document, offset, length, text.toString());
	}

	/**
	 * Returns a document event which is an accumulation of a list of document events,
	 * <code>null</code> if the list of document events is empty.
	 * The document events being merged must all refer to the same document, to which
	 * the document changes have been already applied.
	 *
	 * @param documentEvents the list of document events to merge
	 * @return returns the merged document event
	 * @throws BadLocationException might be thrown if document is not in the correct state with respect to document events
	 */
	public static DocumentEvent mergeProcessedDocumentEvents(List<? extends DocumentEvent> documentEvents) throws BadLocationException {

		if (documentEvents.isEmpty()) {
			return null;
		}

		final ListIterator<? extends DocumentEvent> iterator= documentEvents.listIterator(documentEvents.size());
		final DocumentEvent firstEvent= iterator.previous();

		// current merged event
		final IDocument document= firstEvent.getDocument();
		int offset= firstEvent.getOffset();
		int length= firstEvent.getLength();
		int textLength= firstEvent.getText() == null ? 0 : firstEvent.getText().length();

		while (iterator.hasPrevious()) {

			final int delta= length - textLength;

			final DocumentEvent event= iterator.previous();
			final int eventOffset= event.getOffset();
			final int eventLength= event.getLength();
			final int eventTextLength= event.getText() == null ? 0 : event.getText().length();

			// event is right from merged event
			if (eventOffset > offset + textLength + delta) {
				length= (eventOffset - delta) - (offset + textLength) + length + eventLength;
				textLength= (eventOffset - delta) + eventTextLength - offset;

			// event is left from merged event
			} else if (eventOffset + eventTextLength < offset) {
				length= offset - (eventOffset + eventTextLength) + length + eventLength;
				textLength= offset + textLength - eventOffset;
				offset= eventOffset;

			// events overlap each other
			} else {
				final int start= Math.max(0, eventOffset - offset);
				final int end= Math.min(length, eventTextLength + eventOffset - offset);
				length += eventLength - (end - start);

				offset= Math.min(offset, eventOffset);
				final int totalDelta= delta + eventLength - eventTextLength;
				textLength= length - totalDelta;
			}
		}

		final String text= document.get(offset, textLength);
		return new DocumentEvent(document, offset, length, text);
	}

	/**
	 * Removes all connected document partitioners from the given document and stores them
	 * under their partitioning name in a map. This map is returned. After this method has been called
	 * the given document is no longer connected to any document partitioner.
	 *
	 * @param document the document
	 * @return the map containing the removed partitioners
	 */
	public static Map<String, IDocumentPartitioner> removeDocumentPartitioners(IDocument document) {
		Map<String, IDocumentPartitioner> partitioners= new HashMap<>();
		if (document instanceof IDocumentExtension3 extension3) {
			String[] partitionings= extension3.getPartitionings();
			for (String partitioning : partitionings) {
				IDocumentPartitioner partitioner= extension3.getDocumentPartitioner(partitioning);
				if (partitioner != null) {
					extension3.setDocumentPartitioner(partitioning, null);
					partitioner.disconnect();
					partitioners.put(partitioning, partitioner);
				}
			}
		} else {
			IDocumentPartitioner partitioner= document.getDocumentPartitioner();
			if (partitioner != null) {
				document.setDocumentPartitioner(null);
				partitioner.disconnect();
				partitioners.put(IDocumentExtension3.DEFAULT_PARTITIONING, partitioner);
			}
		}
		return partitioners;
	}

	/**
	 * Connects the given document with all document partitioners stored in the given map under
	 * their partitioning name. This method cleans the given map.
	 *
	 * @param document the document
	 * @param partitioners the map containing the partitioners to be connected
	 * @since 3.0
	 */
	public static void addDocumentPartitioners(IDocument document, Map<String, ? extends IDocumentPartitioner> partitioners) {
		if (document instanceof IDocumentExtension3 extension3) {
			for (Entry<String, ? extends IDocumentPartitioner> entry : partitioners.entrySet()) {
				String partitioning= entry.getKey();
				IDocumentPartitioner partitioner= entry.getValue();
				partitioner.connect(document);
				extension3.setDocumentPartitioner(partitioning, partitioner);
			}
			partitioners.clear();
		} else {
			IDocumentPartitioner partitioner= partitioners.get(IDocumentExtension3.DEFAULT_PARTITIONING);
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
	}

	/**
	 * Returns the content type at the given offset of the given document.
	 *
	 * @param document the document
	 * @param partitioning the partitioning to be used
	 * @param offset the offset
	 * @param preferOpenPartitions <code>true</code> if precedence should be
	 *        given to a open partition ending at <code>offset</code> over a
	 *        closed partition starting at <code>offset</code>
	 * @return the content type at the given offset of the document
	 * @throws BadLocationException if offset is invalid in the document
	 * @since 3.0
	 */
	public static String getContentType(IDocument document, String partitioning, int offset, boolean preferOpenPartitions) throws BadLocationException {
		if (document instanceof IDocumentExtension3 extension3) {
			try {
				return extension3.getContentType(partitioning, offset, preferOpenPartitions);
			} catch (BadPartitioningException x) {
				return IDocument.DEFAULT_CONTENT_TYPE;
			}
		}

		return document.getContentType(offset);
	}

	/**
	 * Returns the partition of the given offset of the given document.
	 *
	 * @param document the document
	 * @param partitioning the partitioning to be used
	 * @param offset the offset
	 * @param preferOpenPartitions <code>true</code> if precedence should be
	 *        given to a open partition ending at <code>offset</code> over a
	 *        closed partition starting at <code>offset</code>
	 * @return the content type at the given offset of this viewer's input
	 *         document
	 * @throws BadLocationException if offset is invalid in the given document
	 * @since 3.0
	 */
	public static ITypedRegion getPartition(IDocument document, String partitioning, int offset, boolean preferOpenPartitions) throws BadLocationException {
		if (document instanceof IDocumentExtension3 extension3) {
			try {
				return extension3.getPartition(partitioning, offset, preferOpenPartitions);
			} catch (BadPartitioningException x) {
				return new TypedRegion(0, document.getLength(), IDocument.DEFAULT_CONTENT_TYPE);
			}
		}

		return document.getPartition(offset);
	}

	/**
	 * Computes and returns the partitioning for the given region of the given
	 * document for the given partitioning name.
	 *
	 * @param document the document
	 * @param partitioning the partitioning name
	 * @param offset the region offset
	 * @param length the region length
	 * @param includeZeroLengthPartitions whether to include zero-length partitions
	 * @return the partitioning for the given region of the given document for
	 *         the given partitioning name
	 * @throws BadLocationException if the given region is invalid for the given
	 *         document
	 * @since 3.0
	 */
	public static ITypedRegion[] computePartitioning(IDocument document, String partitioning, int offset, int length, boolean includeZeroLengthPartitions) throws BadLocationException {
		if (document instanceof IDocumentExtension3 extension3) {
			try {
				return extension3.computePartitioning(partitioning, offset, length, includeZeroLengthPartitions);
			} catch (BadPartitioningException x) {
				return new ITypedRegion[0];
			}
		}

		return document.computePartitioning(offset, length);
	}

	/**
	 * Computes and returns the partition managing position categories for the
	 * given document or <code>null</code> if this was impossible.
	 *
	 * @param document the document
	 * @return the partition managing position categories or <code>null</code>
	 * @since 3.0
	 */
	public static String[] computePartitionManagingCategories(IDocument document) {
		if (document instanceof IDocumentExtension3 extension3) {
			String[] partitionings= extension3.getPartitionings();
			if (partitionings != null) {
				Set<String> categories= new HashSet<>();
				for (String partitioning : partitionings) {
					IDocumentPartitioner p= extension3.getDocumentPartitioner(partitioning);
					if (p instanceof IDocumentPartitionerExtension2 extension2) {
						String[] c= extension2.getManagingPositionCategories();
						if (c != null) {
							Collections.addAll(categories, c);
						}
					}
				}
				String[] result= new String[categories.size()];
				categories.toArray(result);
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the default line delimiter for the given document. This is
	 * {@link IDocumentExtension4#getDefaultLineDelimiter()} if available.
	 * Otherwise, this is either the delimiter of the first line, or the platform line delimiter if it is
	 * a legal line delimiter, or the first one of the legal line delimiters. The default line delimiter should be used when performing document
	 * manipulations that span multiple lines.
	 *
	 * @param document the document
	 * @return the document's default line delimiter
	 * @since 3.0
	 */
	public static String getDefaultLineDelimiter(IDocument document) {
		String lineDelimiter= null;

		if (document instanceof IDocumentExtension4) {
			lineDelimiter= ((IDocumentExtension4) document).getDefaultLineDelimiter();
			if (lineDelimiter != null) {
				return lineDelimiter;
			}
		}

		try {
			lineDelimiter= document.getLineDelimiter(0);
		} catch (BadLocationException x) {
			// usually impossible for the first line
		}

		if (lineDelimiter != null) {
			return lineDelimiter;
		}

		String sysLineDelimiter= System.lineSeparator();
		String[] delimiters= document.getLegalLineDelimiters();
		Assert.isTrue(delimiters.length > 0);
		for (String delimiter : delimiters) {
			if (delimiter.equals(sysLineDelimiter)) {
				lineDelimiter= sysLineDelimiter;
				break;
			}
		}

		if (lineDelimiter == null) {
			lineDelimiter= delimiters[0];
		}

		return lineDelimiter;
	}

	/**
	 * Returns <code>true</code> if the two regions overlap. Returns <code>false</code> if one of the
	 * arguments is <code>null</code>.
	 *
	 * @param left the left region
	 * @param right the right region
	 * @return <code>true</code> if the two regions overlap, <code>false</code> otherwise
	 * @since 3.0
	 */
	public static boolean overlaps(IRegion left, IRegion right) {

		if (left == null || right == null) {
			return false;
		}

		int rightEnd= right.getOffset() + right.getLength();
		int leftEnd= left.getOffset()+ left.getLength();

		if (right.getLength() > 0) {
			if (left.getLength() > 0) {
				return left.getOffset() < rightEnd && right.getOffset() < leftEnd;
			}
			return  right.getOffset() <= left.getOffset() && left.getOffset() < rightEnd;
		}

		if (left.getLength() > 0) {
			return left.getOffset() <= right.getOffset() && right.getOffset() < leftEnd;
		}

		return left.getOffset() == right.getOffset();
	}

	/**
	 * Returns a copy of the given string array.
	 *
	 * @param array the string array to be copied
	 * @return a copy of the given string array or <code>null</code> when <code>array</code> is <code>null</code>
	 * @since 3.1
	 */
	public static String[] copy(String[] array) {
		if (array != null) {
			String[] copy= new String[array.length];
			System.arraycopy(array, 0, copy, 0, array.length);
			return copy;
		}
		return null;
	}

	/**
	 * Returns a copy of the given integer array.
	 *
	 * @param array the integer array to be copied
	 * @return a copy of the given integer array or <code>null</code> when <code>array</code> is <code>null</code>
	 * @since 3.1
	 */
	public static int[] copy(int[] array) {
		if (array != null) {
			int[] copy= new int[array.length];
			System.arraycopy(array, 0, copy, 0, array.length);
			return copy;
		}
		return null;
	}

	/**
	 * Search for the first standard line delimiter in text starting at given offset. Standard line
	 * delimiters are those defined in {@link #DELIMITERS}. This is a faster variant of the equal
	 *
	 * <pre>
	 * MultiStringMatcher.indexOf(TextUtilities.DELIMITERS, text, offset)
	 * </pre>
	 *
	 * @param text the text to be searched. Not <code>null</code>.
	 * @param offset the offset in text at which to start the search
	 * @return a {@link DelimiterInfo}. If no delimiter was found
	 *         {@link DelimiterInfo#delimiterIndex} is <code>-1</code> and
	 *         {@link DelimiterInfo#delimiter} is <code>null</code>.
	 * @since 3.10
	 */
	public static DelimiterInfo nextDelimiter(CharSequence text, int offset) {
		final DelimiterInfo info= new DelimiterInfo();
		char ch;
		final int length= text.length();
		for (int i= offset; i < length; i++) {
			ch= text.charAt(i);
			if (ch == '\r') {
				info.delimiterIndex= i;
				if (i + 1 < length && text.charAt(i + 1) == '\n') {
					info.delimiter= DELIMITERS[2];
					break;
				}
				info.delimiter= DELIMITERS[1];
				break;
			} else if (ch == '\n') {
				info.delimiterIndex= i;
				info.delimiter= DELIMITERS[0];
				break;
			}
		}
		if (info.delimiter == null) {
			info.delimiterIndex= -1;
		} else {
			info.delimiterLength= info.delimiter.length();
		}
		return info;
	}
}

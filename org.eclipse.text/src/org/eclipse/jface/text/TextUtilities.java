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

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


/**
 * Collection of text functions.
 */
public class TextUtilities {
	
	/**
	 * Default line delimiters used by these text functions.
	 */
	public final static String[] fgDelimiters= new String[] { "\n", "\r", "\r\n" }; //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
	
	
	/**
	 * Determines which one of default line delimiters appears first in the list. If none of them the
	 * hint is returned.
	 * 
	 * @param text the text to be checked
	 * @param hint the line delimiter hint
	 */
	public static String determineLineDelimiter(String text, String hint) {
		try {
			int[] info= indexOf(fgDelimiters, text, 0);
			return fgDelimiters[info[1]];
		} catch (ArrayIndexOutOfBoundsException x) {
		}
		return hint;
	}
	
	/**
	 * Returns the starting position and the index of the longest matching search string
	 * in the given text that is greater than the given offset. Returns <code>[-1, -1]</code>
	 * if no match can be found.
	 * 
	 * @param searchStrings the strings to search for
	 * @param text the text to be searched
	 * @param offset the offset at which to start the search
	 * @return an <code>int[]</code> with two elements" the first is the starting offset, the second the index of the found
	 * 		search string in the given <code>searchStrings</code> array, returns <code>[-1, -1]</code> if no match exists
	 */
	public static int[] indexOf(String[] searchStrings, String text, int offset) {
		
		int[] result= { -1, -1 };
		int zeroIndex= -1;
		
		for (int i= 0; i < searchStrings.length; i++) {
			
			int length= searchStrings[i].length();
			
			if (length == 0) {
				zeroIndex= i;
				continue;
			}
			
			int index= text.indexOf(searchStrings[i], offset);
			if (index >= 0) {
				
				if (result[0] == -1) {
					result[0]= index;
					result[1]= i;
				} else if (index < result[0]) {
					result[0]= index;
					result[1]= i;
				} else if (index == result[0] && length > searchStrings[result[1]].length()) {
					result[0]= index;
					result[1]= i;
				}
			}
		}
		
		if (zeroIndex > -1 && result[0] == -1) {
			result[0]= 0;
			result[1]= zeroIndex;
		}
		
		return result;
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
				if (index == -1 || searchStrings[i].length() > searchStrings[index].length())
					index= i;
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
				if (index == -1 || searchStrings[i].length() > searchStrings[index].length())
					index= i;
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
			if (text.equals(compareStrings[i]))
				return i;
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
	public static DocumentEvent mergeUnprocessedDocumentEvents(IDocument unprocessedDocument, List documentEvents) throws BadLocationException {

		if (documentEvents.size() == 0)
			return null;

		final Iterator iterator= documentEvents.iterator();
		final DocumentEvent firstEvent= (DocumentEvent) iterator.next();

		// current merged event
		final IDocument document= unprocessedDocument;		
		int offset= firstEvent.getOffset();
		int length= firstEvent.getLength();
		final StringBuffer text= new StringBuffer(firstEvent.getText() == null ? "" : firstEvent.getText()); //$NON-NLS-1$

		while (iterator.hasNext()) {

			final int delta= text.length() - length;

			final DocumentEvent event= (DocumentEvent) iterator.next();
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
			
			// events overlap eachother				
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
	public static DocumentEvent mergeProcessedDocumentEvents(List documentEvents) throws BadLocationException {

		if (documentEvents.size() == 0)
			return null;

		final ListIterator iterator= documentEvents.listIterator(documentEvents.size());
		final DocumentEvent firstEvent= (DocumentEvent) iterator.previous();

		// current merged event
		final IDocument document= firstEvent.getDocument();		
		int offset= firstEvent.getOffset();
		int length= firstEvent.getLength();
		int textLength= firstEvent.getText() == null ? 0 : firstEvent.getText().length();

		while (iterator.hasPrevious()) {

			final int delta= length - textLength;

			final DocumentEvent event= (DocumentEvent) iterator.previous();
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

			// events overlap eachother				
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
	
}

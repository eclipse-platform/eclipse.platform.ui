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

package org.eclipse.jface.text.source;

import java.io.IOException;


/**
 * A tag handler is responsible to
 * - handle the attributes for the tags it supports
 * - translate the tag sequence including attributes to another language
 * - back-translate relative line offsets.
 * <p>
 * Tag handlers are used by translators via tag handler factories.</p>
 * <p>
 * XXX: This is work in progress and can change anytime until API for 3.0 is frozen.
 * </p>
 * @see org.eclipse.jface.text.source.ITranslator
 * @see org.eclipse.jface.text.source.ITagHandlerFactory
 * @since 3.0
 */
public interface ITagHandler {
	
	/**
	 * Tells whether this handler can handle the given tag.
	 * 
	 * @param tag the tag to check
	 * @return <code>true</code> if this handler handles the given tag
	 */
	boolean canHandleTag(String tag);
	
	/**
	 * Tells whether this handler can handle the given text. Most
	 * likely the handler will check if the text contains a tag
	 * that he can handle.
	 * 
	 * @param tag the text to check
	 * @return <code>true</code> if this handler handles the given text
	 */
	boolean canHandleText(String text);
	
	/**
	 * Adds an attribute to this tag handler.
	 * 
	 * @param name				the name of the attribute
	 * @param value			the attribute value
	 * @param sourceLineNumber the line number of the attribute in the source or <code>-1</code> if unknown
	 */
	void addAttribute(String name, String value, int sourceLineNumber);
	
	/**
	 * Resets this handler and sets the current tag to the given tag.
	 * A handler can handle more than one tag but only one tag at a time.
	 * <p>
	 * Resetting the handler clears the attributes.</p>
	 * 
	 * @param tag the tag to check
	 * @return <code>true</code> if this handler handles the given tag
	 */
	void reset(String tag);
	
	/**
	 * Writes the tag and line mapping information to the 
	 * given translator result collector.
	 * 
	 * @param resultCollector the translator's result collector
	 * @param sourceLineNumber the line number of the attribute in the source or <code>-1</code> if unknown
	 * @throws IOException
	 */
	void processEndTag(ITranslatorResultCollector resultCollector, int sourceLineNumber) throws IOException;
	
	/**
	 * Computes the offset in the source line that corresponds
	 * to the given offset in the translated line. 
	 * 
	 * @param sourceLineLine			the source line
	 * @param translatedLine			the translated line
	 * @param offsetInTranslatedLine	the offset in the translated line
	 * @return the offset in the source line or <code>-1</code> if
	 * 			it was not possible to compute the offset
	 */
	int backTranslateOffsetInLine(String sourceLine, String translatedLine, int offsetInTranslatedLine);
}

/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.text.formatter;

import org.eclipse.jface.text.IDocument;

/**
 * Extension interface for <code>IContentFormatter</code>.
 * <p>
 * Updates the content formatter to be able to pass <code>IFormattingContext</code>
 * context objects to <code>IFormattingStrategyExtension<code> objects
 * operating in context based mode.
 * <p>
 * Clients using context based formatting call the method
 * <code>format(IDocument, IFormattingContext)</code> with a properly
 * initialized formatting context.<br>
 * The formatting context must be set up according to the desired formatting mode:
 * <ul>
 * <li>For whole document formatting set the property <code>CONTEXT_DOCUMENT</code>. This is
 * equivalent to setting <code>CONTEXT_REGION</code> with a region spanning the whole document.</li>
 * <li>For multiple region formatting set the property <code>CONTEXT_REGION</code>. Note that 
 * the content formatter automatically aligns the region to a block selected region, and if the region spans
 * multiple partitions, it also completes eventual partitions covered only partially by the region.</li>
 * </ul>
 * Depending on the registered formatting strategies, more context information must
 * be passed in the formatting context, like e.g. <code>CONTEXT_PREFERENCES</code>.
 * <p>
 * Note that in context based mode the content formatter is fully reentrant, but not
 * thread-safe.
 * <p>
 * 
 * @see IFormattingContext
 * @see FormattingContextProperties
 * @since 3.0
 */
public interface IContentFormatterExtension {

	/**
	 * Formats the given region of the specified document.
	 * <p>
	 * The formatter may safely assume that it is the only subject that
	 * modifies the document at this point in time. This method is fully
	 * reentrant, but not thread-safe.
	 * <p>
	 * The formatting process performed by <code>format(IDocument, IFormattingContext)</code>
	 * happens as follows:
	 * <ul>
	 * <li>In a first pass the content formatter formats the range of the
	 * document to be formatted by using the master formatting strategy. This
	 * happens regardless of the content type of the underlying partition.
	 * </li>
	 * <li>In the second pass, the range is formatted again, this time using
	 * the registered slave formatting strategies. For each partition contained
	 * in the range to be formatted, the content formatter determines its
	 * content type and formats the partition with the correct formatting
	 * strategy.
	 * </li>
	 * 
	 * @param document
	 *                  The document to be formatted
	 * @param context
	 *                   The formatting context to pass to the formatting strategies.
	 *                   This argument must not be <code>null</code>.
	 */
	void format(IDocument document, IFormattingContext context);
}

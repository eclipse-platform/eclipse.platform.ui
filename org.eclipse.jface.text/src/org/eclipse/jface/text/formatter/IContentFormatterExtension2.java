/*****************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *****************************************************************************/

package org.eclipse.jface.text.formatter;

import org.eclipse.jface.text.IDocument;

/**
 * Extension interface for <code>IContentFormatter</code>.
 * <p>
 * Updates the content formatter to be able to pass <code>IFormattingContext</code> 
 * context objects to <code>IFormattingStrategy<code> objects.
 * <p>
 * The formatting context must be set up according to the desired formatting mode:
 * <ul>
 * <li>For whole document formatting set the property <code>CONTEXT_DOCUMENT</code>.</li>
 * <li>For single partition formatting set the property <code>CONTEXT_PARTITION</code>.</li>
 * <li>For multiple region formatting set the property <code>CONTEXT_REGION</code>.</li>
 * </ul>
 * 
 * @see IFormattingContext
 * @see FormattingContextProperties
 * @since 3.0
 */
public interface IContentFormatterExtension2 {

	/**
	 * Formats the given region of the specified document.The formatter may safely 
	 * assume that it is the only subject that modifies the document at this point in time.
	 *
	 * @param document The document to be formatted
	 * @param context The formatting context to pass to the formatting strategies.
	 * This argument must not be <code>null</code>. Most formatting strategies only
	 * require the <code>CONTEXT_DOCUMENT</code> to be set. Depending
	 * on the registered formatting strategies, more properties can be required. 
	 */
	void format(IDocument document, IFormattingContext context);
}

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

import java.util.LinkedList;
import java.util.Map;

import org.eclipse.swt.custom.StyledText;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;

/**
 * Abstract formatting strategy for context based content formatting.
 * <p>
 * This strategy implements <code>IFormattingStrategyExtension</code>. It must be
 * registered with a content formatter implementing <code>IContentFormatterExtension2<code>
 * to take effect.
 * 
 * @see IContentFormatterExtension2
 * @see IFormattingStrategyExtension
 * @since 3.0
 */
public abstract class ContextBasedFormattingStrategy implements IFormattingStrategy, IFormattingStrategyExtension {

	/**
	 * Returns the line delimiter used in the document.
	 * 
	 * @param document Document to get the used line delimiter from
	 * @return The line delimiter used in the document
	 */
	protected static String getLineDelimiter(final IDocument document) {

		String delimiter= null;

		try {
			delimiter= document.getLineDelimiter(0);
		} catch (BadLocationException exception) {
			// Should not happen
		}

		if (delimiter == null) {

			final String system= System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			final String[] delimiters= document.getLegalLineDelimiters();

			for (int index= 0; index < delimiters.length; index++) {

				if (delimiters[index].equals(system)) {
					delimiter= system;
					break;
				}
			}

			if (delimiter == null)
				delimiter= delimiters.length > 0 ? delimiters[0] : system;
		}
		return delimiter;
	}

	/** The current preferences to apply */
	private Map fCurrentPreference= null;

	/** The preferences to apply during formatting */
	private final LinkedList fPreferences= new LinkedList();

	/** The source viewer to operate on */
	private final ISourceViewer fViewer;

	/**
	 * Creates a new abstract formatting strategy.
	 * 
	 * @param viewer ISourceViewer to operate on
	 */
	public ContextBasedFormattingStrategy(final ISourceViewer viewer) {
		fViewer= viewer;
	}

	/*
	 * @see org.eclipse.jface.text.formatter.IFormattingStrategyExtension#format()
	 */
	public void format() {

		final StyledText text= fViewer.getTextWidget();

		Assert.isLegal(text != null && !text.isDisposed());
		Assert.isLegal(fPreferences.size() > 0);

		fCurrentPreference= (Map)fPreferences.removeFirst();
	}

	/*
	 * @see org.eclipse.jface.text.formatter.IFormattingStrategy#format(java.lang.String, boolean, java.lang.String, int[])
	 */
	public String format(String content, boolean isLineStart, String indent, int[] positions) {
		// Do nothing
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.formatter.IFormattingStrategyExtension#formatterStarts(org.eclipse.jface.text.formatter.IFormattingContext)
	 */
	public void formatterStarts(IFormattingContext context) {

		final FormattingContext current= (FormattingContext)context;

		fPreferences.addLast(current.getProperty(FormattingContextProperties.CONTEXT_PREFERENCES));
	}

	/*
	 * @see IFormattingStrategy#formatterStarts(String)
	 */
	public void formatterStarts(String initialIndentation) {
		// Do nothing
	}

	/*
	 * @see org.eclipse.jface.text.formatter.IFormattingStrategyExtension#formatterStops()
	 */
	public void formatterStops() {
		fPreferences.clear();

		fCurrentPreference= null;
	}

	/**
	 * Returns the preferences to use during formatting.
	 * 
	 * @return The formatting preferences
	 */
	public final Map getPreferences() {
		return fCurrentPreference;
	}

	/**
	 * Returns the source viewer to operate on.
	 * 
	 * @return The source viewer to operate on
	 */
	public final ISourceViewer getViewer() {
		return fViewer;
	}
}

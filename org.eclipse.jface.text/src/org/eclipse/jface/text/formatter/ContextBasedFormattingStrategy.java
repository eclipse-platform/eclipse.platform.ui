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

import org.eclipse.jface.text.source.ISourceViewer;

/**
 * Formatting strategy for context based content formatting.
 * <p>
 * 
 * @see IContentFormatterExtension2
 * @see IFormattingStrategyExtension
 * @since 3.0
 */
public abstract class ContextBasedFormattingStrategy implements IFormattingStrategy, IFormattingStrategyExtension {

	/** The current preferences for formatting */
	private Map fCurrentPreferences= null;

	/** The list of preferences for initiated the formatting steps */
	private final LinkedList fPreferences= new LinkedList();

	/** The source viewer to operate on */
	private ISourceViewer fViewer;

	/**
	 * Creates a new context based formatting strategy.
	 */
	public ContextBasedFormattingStrategy() {
	}

	/**
	 * Creates a new context based formatting strategy.
	 * 
	 * TODO: remove
	 * 
	 * @param viewer
	 *                   The source viewer to operate on
	 * 
	 * @deprecated Use {@link ContextBasedFormattingStrategy#ContextBasedFormattingStrategy()}
	 * instead. Also set and use the property value of {@link FormattingContextProperties#CONTEXT_MEDIUM})
	 * in order to access the document. Consider migration to {@link MultiPassContentFormatter}. To be removed.
	 */
	public ContextBasedFormattingStrategy(final ISourceViewer viewer) {
		fViewer= viewer;
	}

	/*
	 * @see org.eclipse.jface.text.formatter.IFormattingStrategyExtension#format()
	 */
	public void format() {
		fCurrentPreferences= (Map)fPreferences.removeFirst();
	}

	/*
	 * @see org.eclipse.jface.text.formatter.IFormattingStrategy#format(java.lang.String, boolean, java.lang.String, int[])
	 */
	public String format(String content, boolean start, String indentation, int[] positions) {
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.formatter.IFormattingStrategyExtension#formatterStarts(org.eclipse.jface.text.formatter.IFormattingContext)
	 */
	public void formatterStarts(final IFormattingContext context) {
		fPreferences.addLast(context.getProperty(FormattingContextProperties.CONTEXT_PREFERENCES));
	}

	/*
	 * @see IFormattingStrategy#formatterStarts(String)
	 */
	public void formatterStarts(final String indentation) {
		// Do nothing
	}

	/*
	 * @see org.eclipse.jface.text.formatter.IFormattingStrategyExtension#formatterStops()
	 */
	public void formatterStops() {
		fPreferences.clear();

		fCurrentPreferences= null;
	}

	/**
	 * Returns the preferences used for the current formatting step.
	 * 
	 * @return The preferences for the current formatting step
	 */
	public final Map getPreferences() {
		return fCurrentPreferences;
	}

	/**
	 * Returns the source viewer where the formatting happens.
	 * 
	 * TODO: remove
	 * 
	 * @return The source viewer where the formatting happens.
	 * 
	 * @deprecated Set and use property value of
	 * {@link FormattingContextProperties#CONTEXT_MEDIUM}) in
	 * order to access the document. Consider migration to
	 * {@link MultiPassContentFormatter}. To be removed.
	 */
	public final ISourceViewer getViewer() {
		return fViewer;
	}
}

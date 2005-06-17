/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.text.formatter;

import java.util.LinkedList;
import java.util.Map;

/**
 * Formatting strategy for context based content formatting. Retrieves the preferences
 * set on the formatting context's {@link FormattingContextProperties#CONTEXT_PREFERENCES}
 * property and makes them available to subclasses.
 * <p>
 *
 * @since 3.0
 */
public abstract class ContextBasedFormattingStrategy implements IFormattingStrategy, IFormattingStrategyExtension {

	/** The current preferences for formatting */
	private Map fCurrentPreferences= null;

	/** The list of preferences for initiated the formatting steps */
	private final LinkedList fPreferences= new LinkedList();

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
}

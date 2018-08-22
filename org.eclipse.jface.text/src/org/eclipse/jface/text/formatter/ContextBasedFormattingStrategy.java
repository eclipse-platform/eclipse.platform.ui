/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
	private Map<String, String> fCurrentPreferences= null;

	/** The list of preferences for initiated the formatting steps */
	private final LinkedList<Map<String, String>> fPreferences= new LinkedList<>();

	@Override
	public void format() {
		fCurrentPreferences= fPreferences.removeFirst();
	}

	@Override
	public String format(String content, boolean start, String indentation, int[] positions) {
		return null;
	}

	@Override
	public void formatterStarts(final IFormattingContext context) {
		@SuppressWarnings("unchecked")
		Map<String, String> prefs= (Map<String, String>) context.getProperty(FormattingContextProperties.CONTEXT_PREFERENCES);
		fPreferences.addLast(prefs);
	}

	@Override
	public void formatterStarts(final String indentation) {
		// Do nothing
	}

	@Override
	public void formatterStops() {
		fPreferences.clear();

		fCurrentPreferences= null;
	}

	/**
	 * Returns the preferences used for the current formatting step.
	 *
	 * @return The preferences for the current formatting step
	 */
	public final Map<String, String> getPreferences() {
		return fCurrentPreferences;
	}
}

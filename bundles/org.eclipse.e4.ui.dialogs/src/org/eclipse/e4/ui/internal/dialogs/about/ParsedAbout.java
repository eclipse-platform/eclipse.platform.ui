/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Ralf Heydenreich - Bug 559694
 *******************************************************************************/
package org.eclipse.e4.ui.internal.dialogs.about;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Holds the information for text appearing in the about dialog
 */
public class ParsedAbout {
	private final String text;
	private final List<HyperlinkRange> ranges = new ArrayList<>();
	private final List<String> links = new ArrayList<>();

	/**
	 * Creates a new about item
	 */
	ParsedAbout(String text, List<HyperlinkRange> linkRanges, List<String> links) {
		this.text = text;
		this.ranges.addAll(linkRanges);
		this.links.addAll(links);
	}

	/**
	 * Returns the link ranges (character locations)
	 */
	public List<HyperlinkRange> linkRanges() {
		return ranges;
	}

	/**
	 * Returns the text to display
	 */
	public String text() {
		return text;
	}

	/**
	 * Returns the {@link Optional} that contains a link at the given offset (if
	 * there is one), otherwise returns empty {@link Optional}.
	 */
	public Optional<String> linkAt(int offset) {
		// Check if there is a link at the offset
		for (int i = 0; i < ranges.size(); i++) {
			if (ranges.get(i).contains(offset)) {
				return Optional.of(links.get(i));
			}
		}
		return Optional.empty();
	}
}

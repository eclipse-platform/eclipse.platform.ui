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
public class AboutItem {
	private final String text;

	private final List<HyperlinkRange> linkRanges = new ArrayList<>();

	private final List<String> hrefs;

	/**
	 * Creates a new about item
	 */
	public AboutItem(String text, List<HyperlinkRange> linkRanges, List<String> links) {
		this.text = text;
		this.linkRanges.addAll(linkRanges);
		this.hrefs = links;
	}

	/**
	 * Returns the link ranges (character locations)
	 */
	public List<HyperlinkRange> getLinkRanges() {
		return linkRanges;
	}

	/**
	 * Returns the text to display
	 */
	public String getText() {
		return text;
	}

	/**
	 * Returns true if a link is present at the given character location
	 */
	public boolean isLinkAt(int offset) {
		// Check if there is a link at the offset
		Optional<HyperlinkRange> potentialMatch = linkRanges.stream().filter(r -> r.contains(offset)).findAny();
		return potentialMatch.isPresent();
	}

	/**
	 * Returns the link at the given offset (if there is one), otherwise returns
	 * <code>null</code>.
	 */
	public Optional<String> getLinkAt(int offset) {
		// Check if there is a link at the offset
		for (int i = 0; i < linkRanges.size(); i++) {
			if (linkRanges.get(i).contains(offset)) {
				return Optional.of(hrefs.get(i));
			}
		}
		return Optional.empty();
	}
}

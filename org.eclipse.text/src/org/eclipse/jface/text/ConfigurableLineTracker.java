/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *     Thomas Wolf - Bug 545252: improved search performance for multiple delimiters
 *******************************************************************************/
package org.eclipse.jface.text;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.MultiStringMatcher.Match;


/**
 * Standard implementation of a generic
 * {@link org.eclipse.jface.text.ILineTracker}.
 * <p>
 * The line tracker can be configured with the set of legal line delimiters.
 * Line delimiters are unconstrained. The line delimiters are used to compute
 * the tracker's line structure. In the case of overlapping line delimiters, the
 * longest line delimiter is given precedence of the shorter ones.
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ConfigurableLineTracker extends AbstractLineTracker {

	/** The strings which are considered being the line delimiter */
	private final String[] fDelimiters;
	/** A predefined delimiter information which is always reused as return value */
	private final DelimiterInfo fDelimiterInfo= new DelimiterInfo();
	/** Util to search the configured line delimiters in text. <code>null</code> if only one delimiter is used. */
	private final MultiStringMatcher fMatcher;

	/**
	 * Creates a standard line tracker for the given line delimiters.
	 *
	 * @param legalLineDelimiters the tracker's legal line delimiters,
	 *		may not be <code>null</code> and must be longer than 0
	 */
	public ConfigurableLineTracker(String[] legalLineDelimiters) {
		Assert.isTrue(legalLineDelimiters != null && legalLineDelimiters.length > 0);
		fDelimiters= TextUtilities.copy(legalLineDelimiters);
		fMatcher= legalLineDelimiters.length > 1 ? MultiStringMatcher.create(legalLineDelimiters) : null;
	}

	@Override
	public String[] getLegalLineDelimiters() {
		return TextUtilities.copy(fDelimiters);
	}

	@Override
	protected DelimiterInfo nextDelimiterInfo(String text, int offset) {
		if (fMatcher != null) {
			Match m = fMatcher.indexOf(text, offset);
			if (m == null) {
				return null;
			}
			fDelimiterInfo.delimiterIndex= m.getOffset();
			fDelimiterInfo.delimiter= m.getText();
		} else {
			int index= text.indexOf(fDelimiters[0], offset);
			if (index == -1)
				return null;
			fDelimiterInfo.delimiterIndex= index;
			fDelimiterInfo.delimiter= fDelimiters[0];
		}

		fDelimiterInfo.delimiterLength= fDelimiterInfo.delimiter.length();
		return fDelimiterInfo;
	}
}

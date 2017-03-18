/*******************************************************************************
 * Copyright (c) 2006, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 500661, 492180
 *******************************************************************************/

package org.eclipse.ui.internal.quickaccess;


import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @since 3.3
 *
 */
public abstract class QuickAccessElement {

	static final String separator = " - "; //$NON-NLS-1$

	private static final int[][] EMPTY_INDICES = new int[0][0];
	private QuickAccessProvider provider;

	/**
	 * @param provider
	 */
	public QuickAccessElement(QuickAccessProvider provider) {
		super();
		this.provider = provider;
	}

	/**
	 * Returns the label to be displayed to the user.
	 *
	 * @return the label
	 */
	public abstract String getLabel();

	/**
	 * Returns the image descriptor for this element.
	 *
	 * @return an image descriptor, or null if no image is available
	 */
	public abstract ImageDescriptor getImageDescriptor();

	/**
	 * Returns the id for this element. The id has to be unique within the
	 * QuickAccessProvider that provided this element.
	 *
	 * @return the id
	 */
	public abstract String getId();

	/**
	 * Executes the associated action for this element.
	 */
	public abstract void execute();

	/**
	 * Return the label to be used for sorting elements.
	 *
	 * @return the sort label
	 */
	public String getSortLabel() {
		return getLabel();
	}

	/**
	 * @return Returns the provider.
	 */
	public QuickAccessProvider getProvider() {
		return provider;
	}

	private static final String WS_START = "^\\s+"; //$NON-NLS-1$
	private static final String WS_END = "\\s+$"; //$NON-NLS-1$
	private static final String ANY_WS = "\\s+"; //$NON-NLS-1$
	private static final String EMPTY_STR = ""; //$NON-NLS-1$
	private static final String PAR_START = "\\("; //$NON-NLS-1$
	private static final String PAR_END = "\\)"; //$NON-NLS-1$
	private static final String ONE_CHAR = ".?"; //$NON-NLS-1$

	// whitespaces filter and patterns
	private String wsFilter;
	private Pattern wsPattern;

	/**
	 * Get the existing {@link Pattern} for the given filter, or create a new
	 * one. The generated pattern will replace whitespaces with * to match all.
	 *
	 * @param filter
	 * @return
	 */
	private Pattern getWhitespacesPattern(String filter) {
		if (wsPattern == null || !filter.equals(wsFilter)) {
			wsFilter = filter;
			String sFilter = filter.replaceFirst(WS_START, EMPTY_STR).replaceFirst(WS_END, EMPTY_STR)
					.replaceAll(PAR_START, ONE_CHAR).replaceAll(PAR_END, ONE_CHAR);
			sFilter = String.format(".*(%s).*", sFilter.replaceAll(ANY_WS, ").*(")); //$NON-NLS-1$//$NON-NLS-2$
			wsPattern = safeCompile(sFilter);
		}
		return wsPattern;
	}

	// wildcard filter and patterns
	private String wcFilter;
	private Pattern wcPattern;

	/**
	 * Get the existing {@link Pattern} for the given filter, or create a new
	 * one. The generated pattern will handle '*' and '?' wildcards.
	 *
	 * @param filter
	 * @return
	 */
	private Pattern getWildcardsPattern(String filter) {
		if (wcPattern == null || !filter.equals(wcFilter)) {
			wcFilter = filter;
			String sFilter = filter.replaceFirst(WS_START, EMPTY_STR).replaceFirst(WS_END, EMPTY_STR)
					.replaceAll(PAR_START, ONE_CHAR).replaceAll(PAR_END, ONE_CHAR);
			// replace '*' and '?' with their matchers ").*(" and ").?("
			StringBuilder sb = new StringBuilder();
			for(int i=0; i<sFilter.length(); i++) {
				char c = sFilter.charAt(i);
				if(c=='*'||c=='?') {
					sb.append(").").append(c).append("("); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					sb.append(c);
				}
			}
			sFilter = String.format(".*(%s).*", sb.toString()); //$NON-NLS-1$
			//
			wcPattern = safeCompile(sFilter);
		}
		return wcPattern;
	}

	/**
	 * A safe way to compile some unknown pattern, avoids possible
	 * {@link PatternSyntaxException}. If the pattern can't be compiled, some
	 * not matching pattern will be returned.
	 *
	 * @param pattern
	 *            some pattern to compile, not null
	 * @return a {@link Pattern} object compiled from given input or a dummy
	 *         pattern which do not match anything
	 */
	private Pattern safeCompile(String pattern) {
		try {
			return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
		} catch (Exception e) {
			// A "bell" special character: should not match anything we can get
			return Pattern.compile("\\a"); //$NON-NLS-1$
		}
	}

	int i = 0;
	/**
	 * If this element is a match (partial, complete, camel case, etc) to the
	 * given filter, returns a {@link QuickAccessEntry}. Otherwise returns
	 * <code>null</code>;
	 *
	 * @param filter
	 *            filter for matching
	 * @param providerForMatching
	 *            the provider that will own the entry
	 * @return a quick access entry or <code>null</code>
	 */
	public QuickAccessEntry match(String filter,
			QuickAccessProvider providerForMatching) {
		String sortLabel = getLabel();
		// first occurrence of filter
		int index = sortLabel.toLowerCase().indexOf(filter);
		if (index != -1) {
			int quality = sortLabel.toLowerCase().equals(filter) ? QuickAccessEntry.MATCH_PERFECT
					: (sortLabel.toLowerCase().startsWith(filter) ? QuickAccessEntry.MATCH_EXCELLENT
							: QuickAccessEntry.MATCH_GOOD);
			return new QuickAccessEntry(this, providerForMatching,
					new int[][] { { index, index + filter.length() - 1 } },
 EMPTY_INDICES, quality);
		}
		Pattern p;
		if (filter.contains("*") || filter.contains("?")) { //$NON-NLS-1$ //$NON-NLS-2$
			// check for wildcards
			p = getWildcardsPattern(filter);
		} else {
			// check for whitespaces
			p = getWhitespacesPattern(filter);
		}
		Matcher m = p.matcher(sortLabel);
		// if matches, return an entry and highlight the match
		if (m.matches()) {
			int groupCount = m.groupCount();
			int[][] indices = new int[groupCount][];
			for (int i = 0; i < groupCount; i++) {
				int nGrp = i + 1;
				// capturing group
				indices[i] = new int[] { m.start(nGrp), m.end(nGrp) - 1 };
			}
			// return match and list of indices
			int quality = QuickAccessEntry.MATCH_EXCELLENT;
			return new QuickAccessEntry(this, providerForMatching,
					indices,
					EMPTY_INDICES, quality );
		}
		//
		String combinedLabel = (providerForMatching.getName() + " " + getLabel()); //$NON-NLS-1$
		index = combinedLabel.toLowerCase().indexOf(filter);
		if (index != -1) {
			int lengthOfElementMatch = index + filter.length()
					- providerForMatching.getName().length() - 1;
			if (lengthOfElementMatch > 0) {
				return new QuickAccessEntry(this, providerForMatching,
						new int[][] { { 0, lengthOfElementMatch - 1 } },
 new int[][] { { index,
						index + filter.length() - 1 } }, QuickAccessEntry.MATCH_GOOD);
			}
			return new QuickAccessEntry(this, providerForMatching,
					EMPTY_INDICES, new int[][] { { index,
 index + filter.length() - 1 } }, QuickAccessEntry.MATCH_GOOD);
		}
		String camelCase = CamelUtil.getCamelCase(sortLabel);
		index = camelCase.indexOf(filter);
		if (index != -1) {
			int[][] indices = CamelUtil.getCamelCaseIndices(sortLabel, index, filter
					.length());
			return new QuickAccessEntry(this, providerForMatching, indices,
 EMPTY_INDICES,
					QuickAccessEntry.MATCH_GOOD);
		}
		String combinedCamelCase = CamelUtil.getCamelCase(combinedLabel);
		index = combinedCamelCase.indexOf(filter);
		if (index != -1) {
			String providerCamelCase = CamelUtil.getCamelCase(providerForMatching
					.getName());
			int lengthOfElementMatch = index + filter.length()
					- providerCamelCase.length();
			if (lengthOfElementMatch > 0) {
				return new QuickAccessEntry(
						this,
						providerForMatching,
						CamelUtil.getCamelCaseIndices(sortLabel, 0, lengthOfElementMatch),
						CamelUtil.getCamelCaseIndices(providerForMatching.getName(),
 index,
								filter.length() - lengthOfElementMatch),
						QuickAccessEntry.MATCH_GOOD);
			}
			return new QuickAccessEntry(this, providerForMatching,
					EMPTY_INDICES, CamelUtil.getCamelCaseIndices(providerForMatching
.getName(), index,
							filter.length()), QuickAccessEntry.MATCH_GOOD);
		}
		return null;
	}
}

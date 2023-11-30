/*******************************************************************************
 * Copyright (c) 2004, 2015, 2017 IBM Corporation and others.
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
 *     Sascha Becher - Bug 186404 - Update PatternFilter API to allow extensions
 *     Lucas Bullen (Red Hat Inc.) - [Bug 203792] filter should support multiple keywords
 *******************************************************************************/
package org.eclipse.ui.dialogs;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.internal.misc.TextMatcher;

/**
 * A filter used in conjunction with <code>FilteredTree</code>. In order to
 * determine if a node should be filtered it uses the content and label provider
 * of the tree to do pattern matching on its children. This causes the entire
 * tree structure to be realized. Note that the label provider must implement
 * ILabelProvider.
 *
 * @see org.eclipse.ui.dialogs.FilteredTree
 * @since 3.2
 */
public class PatternFilter extends ViewerFilter {
	/*
	 * Cache of filtered elements in the tree
	 */
	private Map cache = new HashMap();

	/*
	 * Maps parent elements to TRUE or FALSE
	 */
	private Map foundAnyCache = new HashMap();

	private boolean useCache = false;

	/**
	 * Whether to include a leading wildcard for all provided patterns. A trailing
	 * wildcard is always included.
	 */
	private boolean includeLeadingWildcard = false;

	/**
	 * The string pattern matcher used for this pattern filter.
	 */
	private TextMatcher matcher;

	private boolean useEarlyReturnIfMatcherIsNull = true;

	private static Object[] EMPTY = new Object[0];

	@Override
	public Object[] filter(Viewer viewer, Object parent, Object[] elements) {
		// we don't want to optimize if we've extended the filter ... this
		// needs to be addressed in 3.4
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=186404
		if (matcher == null && useEarlyReturnIfMatcherIsNull) {
			return elements;
		}

		if (!useCache) {
			return super.filter(viewer, parent, elements);
		}

		Object[] filtered = (Object[]) cache.get(parent);
		if (filtered == null) {
			Boolean foundAny = (Boolean) foundAnyCache.get(parent);
			if (foundAny != null && !foundAny.booleanValue()) {
				filtered = EMPTY;
			} else {
				filtered = super.filter(viewer, parent, elements);
			}
			cache.put(parent, filtered);
		}
		return filtered;
	}

	/**
	 * Returns true if any of the elements makes it through the filter. This method
	 * uses caching if enabled; the computation is done in computeAnyVisible.
	 *
	 * @param elements the elements (must not be an empty array)
	 * @return true if any of the elements makes it through the filter.
	 */
	private boolean isAnyVisible(Viewer viewer, Object parent, Object[] elements) {
		if (matcher == null) {
			return true;
		}

		if (!useCache) {
			return computeAnyVisible(viewer, elements);
		}

		Object[] filtered = (Object[]) cache.get(parent);
		if (filtered != null) {
			return filtered.length > 0;
		}
		Boolean foundAny = (Boolean) foundAnyCache.get(parent);
		if (foundAny == null) {
			foundAny = computeAnyVisible(viewer, elements) ? Boolean.TRUE : Boolean.FALSE;
			foundAnyCache.put(parent, foundAny);
		}
		return foundAny.booleanValue();
	}

	/**
	 * Returns true if any of the elements makes it through the filter.
	 *
	 * @param viewer   the viewer
	 * @param elements the elements to test
	 * @return <code>true</code> if any of the elements makes it through the filter
	 */
	private boolean computeAnyVisible(Viewer viewer, Object[] elements) {
		boolean elementFound = false;
		for (int i = 0; i < elements.length && !elementFound; i++) {
			Object element = elements[i];
			elementFound = isElementVisible(viewer, element);
		}
		return elementFound;
	}

	@Override
	public final boolean select(Viewer viewer, Object parentElement, Object element) {
		return isElementVisible(viewer, element);
	}

	/**
	 * Sets whether a leading wildcard should be attached to each pattern string.
	 *
	 * @param includeLeadingWildcard Whether a leading wildcard should be added.
	 */
	public final void setIncludeLeadingWildcard(final boolean includeLeadingWildcard) {
		this.includeLeadingWildcard = includeLeadingWildcard;
	}

	/**
	 * The pattern string for which this filter should select elements in the
	 * viewer.
	 *
	 * @param patternString the pattern string.
	 */
	public void setPattern(String patternString) {
		// these 2 strings allow the PatternFilter to be extended in
		// 3.3 - https://bugs.eclipse.org/bugs/show_bug.cgi?id=186404
		if ("org.eclipse.ui.keys.optimization.true".equals(patternString)) { //$NON-NLS-1$
			useEarlyReturnIfMatcherIsNull = true;
			return;
		} else if ("org.eclipse.ui.keys.optimization.false".equals(patternString)) { //$NON-NLS-1$
			useEarlyReturnIfMatcherIsNull = false;
			return;
		}
		clearCaches();
		if (patternString == null || patternString.isEmpty()) {
			matcher = null;
		} else {
			String pattern = patternString;
			if (!patternString.endsWith(" ")) //$NON-NLS-1$
				pattern += "*"; //$NON-NLS-1$
			if (includeLeadingWildcard) {
				pattern = "*" + pattern; //$NON-NLS-1$
			}
			matcher = new TextMatcher(pattern, true, false);
		}
	}

	/**
	 * Clears the caches used for optimizing this filter. Needs to be called
	 * whenever the tree content changes.
	 */
	/* package */ void clearCaches() {
		cache.clear();
		foundAnyCache.clear();
	}

	/**
	 * Answers whether the given String matches the pattern.
	 *
	 * @param string the String to test
	 *
	 * @return whether the string matches the pattern
	 */
	private boolean match(String string) {
		if (matcher == null) {
			return true;
		}
		return matcher.match(string);
	}

	/**
	 * Answers whether the given element is a valid selection in the filtered tree.
	 * For example, if a tree has items that are categorized, the category itself
	 * may not be a valid selection since it is used merely to organize the
	 * elements.
	 *
	 * @param element the element to check
	 * @return true if this element is eligible for automatic selection
	 */
	public boolean isElementSelectable(Object element) {
		return element != null;
	}

	/**
	 * Answers whether the given element in the given viewer matches the filter
	 * pattern. This is a default implementation that will show a leaf element in
	 * the tree based on whether the provided filter text matches the text of the
	 * given element's text, or that of it's children (if the element has any).
	 *
	 * Subclasses may override this method.
	 *
	 * @param viewer  the tree viewer in which the element resides
	 * @param element the element in the tree to check for a match
	 *
	 * @return true if the element matches the filter pattern
	 */
	public boolean isElementVisible(Viewer viewer, Object element) {
		return isParentMatch(viewer, element) || isLeafMatch(viewer, element);
	}

	/**
	 * Check if the parent (category) is a match to the filter text. The default
	 * behavior returns true if the element has at least one child element that is a
	 * match with the filter text.
	 *
	 * Subclasses may override this method.
	 *
	 * @param viewer  the viewer that contains the element
	 * @param element the tree element to check
	 * @return true if the given element has children that matches the filter text
	 */
	protected boolean isParentMatch(Viewer viewer, Object element) {
		if (viewer instanceof AbstractTreeViewer
				&& ((AbstractTreeViewer) viewer).getContentProvider() instanceof ITreeContentProvider) {
			Object[] children = ((ITreeContentProvider) ((AbstractTreeViewer) viewer).getContentProvider())
					.getChildren(element);

			return children != null && children.length > 0 && isAnyVisible(viewer, element, children);
		}
		return false;
	}

	/**
	 * Check if the current (leaf) element is a match with the filter text. The
	 * default behavior checks that the label of the element is a match.
	 *
	 * Subclasses should override this method.
	 *
	 * @param viewer  the viewer that contains the element
	 * @param element the tree element to check
	 * @return true if the given element's label matches the filter text
	 */
	protected boolean isLeafMatch(Viewer viewer, Object element) {
		String labelText = ((ILabelProvider) ((ContentViewer) viewer).getLabelProvider()).getText(element);

		if (labelText == null) {
			return false;
		}
		return wordMatches(labelText);
	}

	/**
	 * Return whether or not if any of the words in text satisfy the match critera.
	 *
	 * @param text the text to match
	 * @return boolean <code>true</code> if one of the words in text satisifes the
	 *         match criteria.
	 */
	protected boolean wordMatches(String text) {
		if (text == null) {
			return false;
		}

		// If the whole text matches we are all set
		if (match(text)) {
			return true;
		}

		// Otherwise check if any of the words of the text matches
		String[] words = TextMatcher.getWords(text);
		for (String word : words) {
			if (!match(word)) {
				return false;
			}
		}
		return words.length > 0;
	}

	/**
	 * Can be called by the filtered tree to turn on caching.
	 *
	 * @param useCache The useCache to set.
	 */
	void setUseCache(boolean useCache) {
		this.useCache = useCache;
	}
}

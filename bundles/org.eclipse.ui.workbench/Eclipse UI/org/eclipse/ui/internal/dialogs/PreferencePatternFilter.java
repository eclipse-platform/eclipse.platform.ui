/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
 *     Jan-Hendrik Diederich, Bredex GmbH - bug 201052
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.internal.preferences.WorkbenchPreferenceExtensionNode;

/**
 * A class that handles filtering preference node items based on a supplied
 * matching string.
 *
 * @since 3.2
 *
 */
public class PreferencePatternFilter extends PatternFilter {

	/**
	 * this cache is needed because
	 * WorkbenchPreferenceExtensionNode.getKeywordLabels() is expensive. When it
	 * tracks keyword changes effectively than this cache can be removed.
	 */
	private Map<WorkbenchPreferenceExtensionNode, Collection<String>> keywordCache = new HashMap<>();

	/**
	 * Create a new instance of a PreferencePatternFilter
	 */
	public PreferencePatternFilter() {
		super();
	}

	/*
	 * Return true if the given Object matches with any possible keywords that have
	 * been provided. Currently this is only applicable for preference and property
	 * pages.
	 */
	private String[] getKeywords(Object element) {
		if (element instanceof WorkbenchPreferenceExtensionNode) {
			WorkbenchPreferenceExtensionNode workbenchNode = (WorkbenchPreferenceExtensionNode) element;

			Collection<String> keywordCollection = keywordCache.get(element);
			if (keywordCollection == null) {
				keywordCollection = workbenchNode.getKeywordLabels();
				keywordCache.put(workbenchNode, keywordCollection);
			}
			return keywordCollection.toArray(new String[keywordCollection.size()]);
		}
		return new String[0];
	}

	@Override
	public boolean isElementSelectable(Object element) {
		return element instanceof WorkbenchPreferenceExtensionNode;
	}

	@Override
	public boolean isElementVisible(Viewer viewer, Object element) {
		if (WorkbenchActivityHelper.restrictUseOf(element))
			return false;

		// Preference nodes are not differentiated based on category since
		// categories are selectable nodes.
		if (isLeafMatch(viewer, element)) {
			return true;
		}

		ITreeContentProvider contentProvider = (ITreeContentProvider) ((TreeViewer) viewer).getContentProvider();
		IPreferenceNode node = (IPreferenceNode) element;
		Object[] children = contentProvider.getChildren(node);
		// Will return true if any subnode of the element matches the search
		if (filter(viewer, element, children).length > 0) {
			return true;
		}
		return false;
	}

	@Override
	protected boolean isLeafMatch(Viewer viewer, Object element) {
		IPreferenceNode node = (IPreferenceNode) element;
		String text = node.getLabelText();

		if (wordMatches(text)) {
			return true;
		}

		// Also need to check the keywords
		for (String keyword : getKeywords(node)) {
			if (wordMatches(keyword)) {
				return true;
			}
		}
		return false;
	}

}

/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.internal.preferences.WorkbenchPreferenceExtensionNode;

/**
 * A class that handles filtering preference node items based on a supplied
 * matching string.
 * 
 * @since 3.2
 * 
 */
public class PreferencePatternFilter extends PatternItemFilter {

	/**
	 * this cache is needed because
	 * WorkbenchPreferenceExtensionNode.getKeywordLabels() is expensive. When it
	 * tracks keyword changes effectivly than this cache can be removed.
	 */
	private Map keywordCache = new HashMap();

	/**
	 * Create a new instance of a PreferencePatternFilter
	 * 
	 * @param isMatchItem
	 */
	public PreferencePatternFilter(boolean isMatchItem) {
		super(isMatchItem);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		ITreeContentProvider contentProvider = (ITreeContentProvider) ((TreeViewer) viewer)
				.getContentProvider();

		IPreferenceNode node = (IPreferenceNode) element;
		Object[] children = contentProvider.getChildren(node);
		String text = node.getLabelText();

		if (wordMatches(text))
			return true;

		if (matchItem) {
			// Will return true if any subnode of the element matches the search
			if (filter(viewer, element, children).length > 0)
				return true;
		}
		return keywordMatches(node);
	}

	/*
	 * Return true if the given Object matches with any possible keywords that
	 * have been provided. Currently this is only applicable for preference and
	 * property pages.
	 */
	private boolean keywordMatches(Object element) {
		if (element instanceof WorkbenchPreferenceExtensionNode) {
			WorkbenchPreferenceExtensionNode workbenchNode = (WorkbenchPreferenceExtensionNode) element;

			Collection keywordCollection = (Collection) keywordCache
					.get(element);
			if (keywordCollection == null) {
				keywordCollection = workbenchNode.getKeywordLabels();
				keywordCache.put(element, keywordCollection);
			}
			if (keywordCollection.isEmpty())
				return false;
			Iterator keywords = keywordCollection.iterator();
			while (keywords.hasNext()) {
				if (wordMatches((String) keywords.next()))
					return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.dialogs.PatternFilter#isElementSelectable(java.lang.Object)
	 */
	protected boolean isElementSelectable(Object element) {
		return element instanceof WorkbenchPreferenceExtensionNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.dialogs.PatternFilter#isElementMatch(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object)
	 */
	protected boolean isElementMatch(Viewer viewer, Object element) {
		IPreferenceNode node = (IPreferenceNode) element;
		String text = node.getLabelText();

		if (wordMatches(text))
			return true;

		return keywordMatches(node);
	}
}

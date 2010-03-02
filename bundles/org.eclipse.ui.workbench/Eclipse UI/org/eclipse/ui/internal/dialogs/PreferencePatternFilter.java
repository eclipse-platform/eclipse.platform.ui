/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jan-Hendrik Diederich, Bredex GmbH - bug 201052
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
	 * tracks keyword changes effectivly than this cache can be removed.
	 */
	private Map keywordCache = new HashMap();

	/**
	 * Create a new instance of a PreferencePatternFilter
	 * 
	 * @param isMatchItem
	 */
	public PreferencePatternFilter() {
		super();
	}

	/*
	 * Return true if the given Object matches with any possible keywords that
	 * have been provided. Currently this is only applicable for preference and
	 * property pages.
	 */
	private String[] getKeywords(Object element) {
		List keywordList = new ArrayList();
		if (element instanceof WorkbenchPreferenceExtensionNode) {
			WorkbenchPreferenceExtensionNode workbenchNode = (WorkbenchPreferenceExtensionNode) element;

			Collection keywordCollection = (Collection) keywordCache
					.get(element);
			if (keywordCollection == null) {
				keywordCollection = workbenchNode.getKeywordLabels();
				keywordCache.put(element, keywordCollection);
			}
			if (!keywordCollection.isEmpty()){
				Iterator keywords = keywordCollection.iterator();
				while (keywords.hasNext()) {
					keywordList.add(keywords.next());
				}
			}
		}
		return (String[]) keywordList.toArray(new String[keywordList.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.dialogs.PatternFilter#isElementSelectable(java.lang.Object)
	 */
	public boolean isElementSelectable(Object element) {
		return element instanceof WorkbenchPreferenceExtensionNode;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.PatternFilter#isElementVisible(org.eclipse.jface.viewers.Viewer, java.lang.Object)
	 */
	public boolean isElementVisible(Viewer viewer, Object element) {
	    if (WorkbenchActivityHelper.restrictUseOf(
	            element))
	        return false;
	    
		// Preference nodes are not differentiated based on category since 
		// categories are selectable nodes.
		if (isLeafMatch(viewer, element)) {
			return true;
		}

		ITreeContentProvider contentProvider = (ITreeContentProvider) ((TreeViewer) viewer)
				.getContentProvider();
		IPreferenceNode node = (IPreferenceNode) element;
		Object[] children = contentProvider.getChildren(node);
		// Will return true if any subnode of the element matches the search
		if (filter(viewer, element, children).length > 0) {
			return true;
		}		
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.PatternFilter#isLeafMatch(org.eclipse.jface.viewers.Viewer, java.lang.Object)
	 * 
	 */
	protected boolean isLeafMatch(Viewer viewer, Object element) {
		IPreferenceNode node = (IPreferenceNode) element;
		String text = node.getLabelText();

		if (wordMatches(text)) {
			return true;
		}

		// Also need to check the keywords
		String[] keywords = getKeywords(node);
		for (int i = 0; i < keywords.length; i++){
			if (wordMatches(keywords[i])) {
				return true;
			}
		}
		return false;
	}

}

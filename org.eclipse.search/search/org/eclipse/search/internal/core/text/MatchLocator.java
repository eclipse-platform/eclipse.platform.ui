/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.core.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import org.eclipse.core.resources.IResourceProxy;

import org.eclipse.search.internal.ui.SearchMessages;

/**
 * A class finding matches within a file.
 * @since 3.0
 */
public class MatchLocator {
	
	private Matcher fMatcher;
	
	public MatchLocator(Pattern pattern) {
		fMatcher= pattern.matcher(""); //$NON-NLS-1$
	}
	
	public MatchLocator(String pattern, boolean isCaseSensitive, boolean isRegexSearch) throws PatternSyntaxException {
		this(PatternConstructor.createPattern(pattern, isCaseSensitive, isRegexSearch));		
	}
	
	public boolean isEmpty() {
		return fMatcher.pattern().pattern().length() == 0;
	}
	
	public void locateMatches(IProgressMonitor progressMonitor, CharSequence searchInput, ITextSearchResultCollector collector, IResourceProxy proxy) throws CoreException {
		fMatcher.reset(searchInput);
		int k= 0;
		while (fMatcher.find()) {
			int start= fMatcher.start();
			int end= fMatcher.end();
			if (end != start) { // don't report 0-length matches
				collector.accept(proxy, start, end - start);
			}
			if (k++ == 20) {
				if (progressMonitor.isCanceled()) {
					throw new OperationCanceledException(SearchMessages.TextSearchVisitor_canceled); 
				}
				k= 0;
			}
		}
	}
}

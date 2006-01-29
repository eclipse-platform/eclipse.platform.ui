/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 *******************************************************************************/

package org.eclipse.search2.internal.ui.text2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;

import org.eclipse.search.internal.core.text.PatternConstructor;

import org.eclipse.search.core.text.TextSearchScope;

public class RetrieverSearchScope extends TextSearchScope {
	private boolean fIsCaseSensitive= false;
	private boolean fVisitDerived= false;

	private IResource[] fRoots;
	private HashSet fFileExtensions= new HashSet();
	private ArrayList fFileMatchers= new ArrayList();


	/**
	 * Creates a search scope for resources below a root matching one of the
	 * given file patterns.
	 * 
	 * @param root
	 *            the subtree of resources to be considered
	 * @param filePatterns
	 *            an array of eclipse style patterns
	 * @param isCaseSensitive
	 *            flag that indicates whether the file patterns are case
	 *            sensitive or not.
	 */
	public RetrieverSearchScope(IResource root, String[] filePatterns, boolean isCaseSensitive) {
		this(new IResource[] {root}, filePatterns, isCaseSensitive);
	}

	/**
	 * Creates a search scope for resources below several roots matching one of
	 * the given file patterns.
	 * 
	 * @param roots
	 *            the subtree of resources to be considered
	 * @param filePatterns
	 *            an array of eclipse style patterns
	 * @param isCaseSensitive
	 *            flag that indicates whether the file patterns are case
	 *            sensitive or not.
	 */
	public RetrieverSearchScope(IResource[] roots, String[] filePatterns, boolean isCaseSensitive) {
		fIsCaseSensitive= isCaseSensitive; // do this before compiling the patterns!
		compileFilePatterns(filePatterns);

		ArrayList openRoots= new ArrayList();
		for (int i= 0; i < roots.length; i++) {
			IResource root= roots[i];
			if (root.getProject().isOpen()) {
				openRoots.add(root);
			}
		}
		fRoots= (IResource[]) openRoots.toArray(new IResource[openRoots.size()]);
	}

	private void compileFilePatterns(String[] filePatterns) {
		fFileMatchers.clear();
		fFileExtensions.clear();
		if (filePatterns != null) {
			for (int i= 0; i < filePatterns.length; i++) {
				compileFilePattern(filePatterns[i], fFileExtensions, fFileMatchers);
			}
		}
	}

	private void compileFilePattern(String pattern, HashSet extensions, ArrayList matchers) {
		pattern= pattern.trim();
		int pos= pattern.lastIndexOf('*');
		if (pos == 0) {
			pos= pattern.lastIndexOf('.');
			if (pos == 1) {
				String suffix= pattern.substring(2);
				String literal= PatternConstructor.appendAsRegEx(false, suffix, new StringBuffer()).toString();
				String strMatcher= PatternConstructor.appendAsRegEx(true, suffix, new StringBuffer()).toString();
				if (literal.equals(strMatcher)) {
					if (!fIsCaseSensitive) {
						suffix= suffix.toUpperCase();
					}
					extensions.add(suffix);
					return;
				}
			}
		}
		Pattern regex= PatternConstructor.createPattern(pattern, false, true, fIsCaseSensitive, false);
		matchers.add(regex.matcher("")); //$NON-NLS-1$
	}

	public boolean contains(IResourceProxy proxy) {
		if (!fVisitDerived && proxy.isDerived()) {
			return false; // all resources in a derived folder are considered to be derived, see bug 103576
		}

		switch (proxy.getType()) {
			case IResource.PROJECT:
				return ((IProject) proxy.requestResource()).isOpen();

			case IResource.FILE:
				return containsFile(proxy);
		}
		return true;
	}

	private boolean containsFile(IResourceProxy proxy) {
		String name= proxy.getName();
		int iDot= name.lastIndexOf('.');
		String ext= null;
		if (iDot >= 0) {
			ext= name.substring(iDot + 1);
			if (!fIsCaseSensitive) {
				ext= ext.toUpperCase();
			}
			if (fFileExtensions.contains(ext)) {
				return true;
			}
		}
		for (int i= 0; i < fFileMatchers.size(); i++) {
			Matcher m= (Matcher) fFileMatchers.get(i);
			m.reset(name);
			if (m.matches()) {
				return true;
			}
		}
		return false;
	}

	public void setVisitDerived(boolean visitDerived) {
		fVisitDerived= visitDerived;
	}

	public IResource[] getRoots() {
		return fRoots;
	}
}

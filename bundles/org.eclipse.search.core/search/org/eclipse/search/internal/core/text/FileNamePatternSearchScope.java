/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.search.internal.core.text;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;

import org.eclipse.search.core.text.TextSearchScope;

public class FileNamePatternSearchScope extends TextSearchScope {

	/**
	 * Returns a scope for the given resources.
	 * @param description description of the scope
	 * @param resources the resources to be contained
	 * @param includeDerived specifies if derived resources are included or not
	 * @return a scope for the given resources.
	 */
	public static FileNamePatternSearchScope newSearchScope(String description, IResource[] resources, boolean includeDerived) {
		return new FileNamePatternSearchScope(description, removeRedundantEntries(resources, includeDerived), includeDerived);
	}

	private static final boolean IS_CASE_SENSITIVE_FILESYSTEM = !new File("Temp").equals(new File("temp")); //$NON-NLS-1$ //$NON-NLS-2$

	private final String fDescription;
	private final IResource[] fRootElements;

	private final Set<String> fFileNamePatterns;
	private Matcher fFileNameMatcher;

	private boolean fVisitDerived;

	private FileNamePatternSearchScope(String description, IResource[] resources, boolean visitDerived) {
		Assert.isNotNull(description);
		fDescription= description;
		fRootElements= resources;
		fFileNamePatterns=  new HashSet<>(3);
		fFileNameMatcher= null;
		fVisitDerived= visitDerived;
	}

	/**
	 * Returns the description of the scope
	 * @return the description of the scope
	 */
	public String getDescription() {
		return fDescription;
	}

	@Override
	public IResource[] getRoots() {
		return fRootElements;
	}

	@Override
	public boolean contains(IResourceProxy proxy) {
		if (!fVisitDerived && proxy.isDerived()) {
			return false; // all resources in a derived folder are considered to be derived, see bug 103576
		}

		if (proxy.getType() == IResource.FILE) {
			return matchesFileName(proxy.getName());
		}
		return true;
	}

	/**
	 * Adds an file name pattern to the scope.
	 *
	 * @param pattern the pattern
	 */
	public void addFileNamePattern(String pattern) {
		if (fFileNamePatterns.add(pattern)) {
			fFileNameMatcher= null; // clear cache
		}
	}

	public void setFileNamePattern(Pattern pattern) {
		fFileNameMatcher= pattern.matcher(""); //$NON-NLS-1$
	}


	public Pattern getFileNamePattern() {
		return getFileNameMatcher().pattern();
	}

	/**
	 * Returns if derived resources are included in the scope.
	 *
	 * @return if set derived resources are included in the scope.
	 */
	public boolean isIncludeDerived() {
		return fVisitDerived;
	}


	private Matcher getFileNameMatcher() {
		if (fFileNameMatcher == null) {
			Pattern pattern;
			if (fFileNamePatterns.isEmpty()) {
				pattern= Pattern.compile(".*"); //$NON-NLS-1$
			} else {
				String[] patternStrings= fFileNamePatterns.toArray(new String[fFileNamePatterns.size()]);
				pattern= PatternConstructor.createPattern(patternStrings, IS_CASE_SENSITIVE_FILESYSTEM);
			}
			fFileNameMatcher= pattern.matcher(""); //$NON-NLS-1$
		}
		return fFileNameMatcher;
	}

	/**
	 * Tests if a file name matches to the file name patterns contained in the scope
	 * @param fileName The file name to test
	 * @return returns true if the file name is matching to a file name pattern
	 */
	private boolean matchesFileName(String fileName) {
		return getFileNameMatcher().reset(fileName).matches();
	}

	/**
	 * Returns a description for the file name patterns in the scope
	 * @return the description of the scope
	 */
	public String getFileNamePatternDescription() {
		String[] ext= fFileNamePatterns.toArray(new String[fFileNamePatterns.size()]);
		Arrays.sort(ext);
		return String.join(", ", ext); //$NON-NLS-1$
	}


	private static IResource[] removeRedundantEntries(IResource[] elements, boolean includeDerived) {
		ArrayList<IResource> res= new ArrayList<>();
		for (IResource curr : elements) {
			addToList(res, curr, includeDerived);
		}
		return res.toArray(new IResource[res.size()]);
	}

	private static void addToList(ArrayList<IResource> res, IResource curr, boolean includeDerived) {
		if (!includeDerived && curr.isDerived(IResource.CHECK_ANCESTORS)) {
			return;
		}
		IPath currPath= curr.getFullPath();
		for (int k= res.size() - 1; k >= 0 ; k--) {
			IResource other= res.get(k);
			IPath otherPath= other.getFullPath();
			if (otherPath.isPrefixOf(currPath)) {
				return;
			}
			if (currPath.isPrefixOf(otherPath)) {
				res.remove(k);
			}
		}
		res.add(curr);
	}

}

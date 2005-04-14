/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.util.Assert;

import org.eclipse.ui.IWorkingSet;

import org.eclipse.search.internal.core.text.PatternConstructor;
import org.eclipse.search.internal.ui.SearchMessages;

public class SearchScope {

	/**
	 * Returns a workspace scope.
	 * @return a workspace scope.
	 */
	public static SearchScope newWorkspaceScope() {
		return new SearchScope(SearchMessages.WorkspaceScope, new IResource[] { ResourcesPlugin.getWorkspace().getRoot() }); 
	}
	
	/**
	 * Returns a scope for the given resources.
	 * @param description description of the scope
	 * @param resources the resources to be contained
	 * @return a scope for the given resources.
	 */
	public static SearchScope newSearchScope(String description, IResource[] resources) {
		return new SearchScope(description, removeRedundantEntries(resources));
	}

	/**
	 * Returns a scope for the given working sets
	 * @param description description of the scope
	 * @param workingSets the working sets to be contained
	 * @return a scope for the given working sets
	 */
	public static SearchScope newSearchScope(String description, IWorkingSet[] workingSets) {
		return new SearchScope(description, convertToResources(workingSets));
	}
	
	private static final boolean IS_CASE_SENSITIVE_FILESYSTEM = !new File("Temp").equals(new File("temp")); //$NON-NLS-1$ //$NON-NLS-2$
	
	private String fDescription;
	private final IResource[] fRootElements;
	
	private Set fFileNamePatterns= new HashSet(3);
	private Matcher[] fFileNameMatchers= null;

	private SearchScope(String description, IResource[] resources) {
		Assert.isNotNull(description);
		fDescription= description;
		fRootElements= resources;
	}
	
	/**
	 * Returns the description of the scope
	 * @return the description of the scope
	 */
	public String getDescription() {
		return fDescription;
	}
	
	/**
	 * Returns the root elements of this scope
	 * @return the root elements of this scope
	 */
	public IResource[] getRootElements() {
		return fRootElements;
	}
		
	/**
	 * Adds an file name pattern  to the scope.
	 * @param pattern
	 */
	public void addFileNamePattern(String pattern) {
		if (fFileNamePatterns.add(pattern)) {
			fFileNameMatchers= null; // clear cache
		}
	}

	private Matcher[] getFileNameMatchers() {
		if (fFileNameMatchers == null) {
			fFileNameMatchers= new Matcher[fFileNamePatterns.size()];
			int i= 0;
			for (Iterator iter= fFileNamePatterns.iterator(); iter.hasNext();) {
				String ext= (String) iter.next();
				Pattern pattern= PatternConstructor.createPattern(ext, IS_CASE_SENSITIVE_FILESYSTEM, false); 
				fFileNameMatchers[i++]= pattern.matcher(""); //$NON-NLS-1$
			}
		}
		return fFileNameMatchers;
	}
	
	/**
	 * Tests if a file name matches to the file name patterns contained in the scope
	 * @param fileName The file name to test
	 * @return returns true if the file name is matching to a file name pattern
	 */
	public boolean matchesFileName(String fileName) {
 		Matcher[] matchers= getFileNameMatchers();
		for (int i= 0; i < matchers.length; i++) {
			if (matchers[i].reset(fileName).matches()) {
				return true;
			}
		}
		return matchers.length == 0;
	}
		
	/**
	 * Returns a description for the file name patterns in the scope
	 * @return the description of the scope
	 */
	public String getFileNamePatternDescription() {
		String[] ext= (String[]) fFileNamePatterns.toArray(new String[fFileNamePatterns.size()]);
		Arrays.sort(ext);
		StringBuffer buf= new StringBuffer();
		for (int i= 0; i < ext.length; i++) {
			if (i > 0) {
				buf.append(", "); //$NON-NLS-1$
			}
			buf.append(ext[i]);
		}
		return buf.toString();
	}

	
	private static IResource[] removeRedundantEntries(IResource[] elements) {
		ArrayList res= new ArrayList();
		for (int i= 0; i < elements.length; i++) {
			IResource curr= elements[i];
			addToList(res, curr);
		}
		return (IResource[])res.toArray(new IResource[res.size()]);
	}

	private static IResource[] convertToResources(IWorkingSet[] workingSets) {
		ArrayList res= new ArrayList();
		for (int i= 0; i < workingSets.length; i++) {
			IAdaptable[] elements= workingSets[i].getElements();
			for (int k= 0; k < elements.length; k++) {
				IResource curr= (IResource) elements[k].getAdapter(IResource.class);
				if (curr != null) {
					addToList(res, curr);
				}
			}
		}
		return (IResource[]) res.toArray(new IResource[res.size()]);
	}
	
	private static void addToList(ArrayList res, IResource curr) {
		IPath currPath= curr.getFullPath();
		for (int k= res.size() - 1; k >= 0 ; k--) {
			IResource other= (IResource) res.get(k);
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

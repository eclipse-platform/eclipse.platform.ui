/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.editor3x;

import java.util.List;

import org.eclipse.e4.tools.emf.ui.common.IClassContributionProvider;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameRequestor;
import org.eclipse.pde.internal.core.util.PDEJavaHelper;

public class PDEClassContributionProvider implements IClassContributionProvider {
	private SearchEngine searchEngine;
	public PDEClassContributionProvider() {
		searchEngine = new SearchEngine();
	}
	
	@SuppressWarnings("restriction")
	public void findContribution(Filter filter,  final ContributionResultHandler handler) {
		System.err.println("Searching for: " + filter.namePattern);
		
		IJavaSearchScope scope = PDEJavaHelper.getSearchScope(filter.project);
		
		char[] packageName = null;
		char[] typeName = null;
		String currentContent = filter.namePattern;
		int index = currentContent.lastIndexOf('.');

		if (index == -1) {
			// There is no package qualification
			// Perform the search only on the type name
			typeName = currentContent.toCharArray();
		} else if ((index + 1) == currentContent.length()) {
			// There is a package qualification and the last character is a
			// dot
			// Perform the search for all types under the given package
			// Pattern for all types
			typeName = "".toCharArray(); //$NON-NLS-1$
			// Package name without the trailing dot
			packageName = currentContent.substring(0, index).toCharArray();
		} else {
			// There is a package qualification, followed by a dot, and 
			// a type fragment
			// Type name without the package qualification
			typeName = currentContent.substring(index + 1).toCharArray();
			// Package name without the trailing dot
			packageName = currentContent.substring(0, index).toCharArray();
		}
		
//		char[] packageName = "at.bestsolution.e4.handlers".toCharArray();
//		char[] typeName = "*".toCharArray();
		
		TypeNameRequestor req = new TypeNameRequestor() {
			public void acceptType(int modifiers, char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames, String path) {
				// Accept search results from the JDT SearchEngine
				String cName = new String(simpleTypeName);
				String pName = new String(packageName);
				String label = cName + " - " + pName; //$NON-NLS-1$
				String content = pName + "." + cName; //$NON-NLS-1$
				System.err.println("Found: " + label + " => " + pName);
				
				ContributionData data = new ContributionData(null, content, "Java", null);
				handler.result(data);
				
				//Image image = (Flags.isInterface(modifiers)) ? PDEPluginImages.get(PDEPluginImages.OBJ_DESC_GENERATE_INTERFACE) : PDEPluginImages.get(PDEPluginImages.OBJ_DESC_GENERATE_CLASS);
				//addProposalToCollection(c, startOffset, length, label, content, image);
			}
		};
		
		try {
			searchEngine.searchAllTypeNames(
					packageName, 
					SearchPattern.R_EXACT_MATCH, 
					typeName, 
					SearchPattern.R_PREFIX_MATCH, 
					IJavaSearchConstants.CLASS, 
					scope, 
					req, 
					IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}

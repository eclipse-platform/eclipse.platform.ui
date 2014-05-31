/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Steven Spungin <steven@spungin.tv> - Bug 424730, Bug 436281, Bug 436280
 ******************************************************************************/
package org.eclipse.e4.tools.emf.editor3x;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.tools.emf.ui.common.IClassContributionProvider;
import org.eclipse.e4.tools.emf.ui.common.ResourceSearchScope;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
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
	public void findContribution(final Filter filter, final ContributionResultHandler handler) {
		boolean followReferences = true;
		if (filter.getSearchScope().contains(ResourceSearchScope.PROJECT) && !filter.getSearchScope().contains(ResourceSearchScope.REFERENCES)) {
			followReferences = false;
		}

		IJavaSearchScope scope = null;
		if (followReferences == false){
			IJavaProject javaProject = JavaCore.create(filter.project);
			IPackageFragmentRoot[] roots;
			try {
				roots = javaProject.getPackageFragmentRoots();
				scope = SearchEngine.createJavaSearchScope(roots, false);
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}else{
			// filter.project may be null in the live editor
			scope = filter.project != null ? PDEJavaHelper
					.getSearchScope(filter.project) : SearchEngine
					.createWorkspaceScope();
		}
		char[] packageName = null;
		char[] typeName = null;
		String currentContent = filter.namePattern;
		int index = currentContent.lastIndexOf('.');

		if (index == -1) {
			// There is no package qualification
			// Perform the search only on the type name
			typeName = currentContent.toCharArray();
			if( currentContent.startsWith("*") ) {
				if( ! currentContent.endsWith("*") ) {
					currentContent += "*";
				}
				typeName = currentContent.toCharArray();
				packageName = "*".toCharArray();
			}
			
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
			@Override
			public void acceptType(int modifiers, char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames, String path) {
				// Accept search results from the JDT SearchEngine
				String cName = new String(simpleTypeName);
				String pName = new String(packageName);
//				String label = cName + " - " + pName; //$NON-NLS-1$
				String content = pName.length() == 0 ? cName : pName + "." + cName; //$NON-NLS-1$
				
//				System.err.println("Found: " + label + " => " + pName + " => " + path);
				
				IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
				
				if( resource != null ) {
					IProject project = resource.getProject();
					IFile f = project.getFile("/META-INF/MANIFEST.MF");
					
					if( f != null && f.exists() ) {
						BufferedReader r = null;
						try {
							InputStream s = f.getContents();
							r = new BufferedReader(new InputStreamReader(s));
							String line;
							while( (line = r.readLine()) != null ) {
								if( line.startsWith("Bundle-SymbolicName:") ) {
									int start = line.indexOf(':');
									int end = line.indexOf(';');
									if( end == -1 ) {
										end = line.length();
									}
									ContributionData data = new ContributionData(line.substring(start+1,end).trim(), content, "Java", null);
									handler.result(data);
									break;
								}
							}
								
						} catch (CoreException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} finally {
							if( r != null ) {
								try {
									r.close();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}								
							}
						}
					}
				}
				
				
				//Image image = (Flags.isInterface(modifiers)) ? PDEPluginImages.get(PDEPluginImages.OBJ_DESC_GENERATE_INTERFACE) : PDEPluginImages.get(PDEPluginImages.OBJ_DESC_GENERATE_CLASS);
				//addProposalToCollection(c, startOffset, length, label, content, image);
			}
		};
		
		try {
			searchEngine.searchAllTypeNames(
					packageName, 
					SearchPattern.R_PATTERN_MATCH, 
					typeName, 
					SearchPattern.R_PATTERN_MATCH | SearchPattern.R_CAMELCASE_MATCH, 
					IJavaSearchConstants.CLASS, 
					scope, 
					req, 
					IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		handler.moreResults(0, filter);
	}

}

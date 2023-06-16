/*******************************************************************************
 * Copyright (c) 2010, 2023 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 * Steven Spungin <steven@spungin.tv> - Bug 424730, Bug 436281, Bug 436280
 ******************************************************************************/
package org.eclipse.e4.tools.emf.editor3x;

import java.io.BufferedReader;
import java.io.IOException;
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

@SuppressWarnings("restriction")
public class PDEClassContributionProvider implements IClassContributionProvider {
	private final SearchEngine searchEngine;

	public PDEClassContributionProvider() {
		searchEngine = new SearchEngine();
	}

	@Override
	public void findContribution(final Filter filter, final ContributionResultHandler handler) {
		boolean followReferences = true;
		if (filter.getSearchScope().contains(ResourceSearchScope.PROJECT)
				&& !filter.getSearchScope().contains(ResourceSearchScope.REFERENCES)) {
			followReferences = false;
		}

		IJavaSearchScope scope = null;
		if (followReferences == false) {
			final IJavaProject javaProject = JavaCore.create(filter.project);
			IPackageFragmentRoot[] roots;
			try {
				roots = javaProject.getPackageFragmentRoots();
				scope = SearchEngine.createJavaSearchScope(roots, false);
			} catch (final JavaModelException e) {
				e.printStackTrace();
			}
		} else {
			// filter.project may be null in the live editor
			scope = filter.project != null ? PDEJavaHelper
					.getSearchScope(filter.project) : SearchEngine
					.createWorkspaceScope();
		}
		char[] packageName = null;
		char[] typeName = null;
		String currentContent = filter.namePattern;
		final int index = currentContent.lastIndexOf('.');

		if (index == -1) {
			// There is no package qualification
			// Perform the search only on the type name
			typeName = currentContent.toCharArray();
			if (currentContent.startsWith("*")) { //$NON-NLS-1$
				if (!currentContent.endsWith("*")) { //$NON-NLS-1$
					currentContent += "*"; //$NON-NLS-1$
				}
				typeName = currentContent.toCharArray();
				packageName = "*".toCharArray(); //$NON-NLS-1$
			}

		} else if (index + 1 == currentContent.length()) {
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

		// char[] packageName = "at.bestsolution.e4.handlers".toCharArray();
		// char[] typeName = "*".toCharArray();

		final TypeNameRequestor req = new TypeNameRequestor() {
			@Override
			public void acceptType(int modifiers, char[] packageName, char[] simpleTypeName,
					char[][] enclosingTypeNames, String path) {
				// 474841 compute name considering inner classes
				final boolean isEnclosed = enclosingTypeNames != null && enclosingTypeNames.length > 0;
				final String ePrefix = isEnclosed ? new String(enclosingTypeNames[0]) + "$" : ""; //$NON-NLS-1$//$NON-NLS-2$
				// Accept search results from the JDT SearchEngine
				final String cName = ePrefix + new String(simpleTypeName);
				final String pName = new String(packageName);
				//				String label = cName + " - " + pName; //$NON-NLS-1$
				final String content = pName.length() == 0 ? cName : pName + "." + cName; //$NON-NLS-1$

				// System.err.println("Found: " + label + " => " + pName + " => " + path);

				final IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);

				if (resource != null) {
					final IProject project = resource.getProject();
					final IFile f = project.getFile("/META-INF/MANIFEST.MF"); //$NON-NLS-1$

					if (f != null && f.exists()) {
						try (BufferedReader r = new BufferedReader(new InputStreamReader(f.getContents()))) {
							String line;
							while ((line = r.readLine()) != null) {
								if (line.startsWith("Bundle-SymbolicName:")) { //$NON-NLS-1$
									final int start = line.indexOf(':');
									int end = line.indexOf(';');
									if (end == -1) {
										end = line.length();
									}
									final ContributionData data = new ContributionData(line.substring(start + 1, end)
											.trim(), content, "Java", null); //$NON-NLS-1$
									handler.result(data);
									break;
								}
							}

						} catch (final CoreException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (final IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

				// Image image = (Flags.isInterface(modifiers)) ?
				// PDEPluginImages.get(PDEPluginImages.OBJ_DESC_GENERATE_INTERFACE) :
				// PDEPluginImages.get(PDEPluginImages.OBJ_DESC_GENERATE_CLASS);
				// addProposalToCollection(c, startOffset, length, label, content, image);
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
		} catch (final JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		handler.moreResults(0, filter);
	}

}

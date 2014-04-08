/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Steven Spungin <steven@spungin.tv> - Bug 424730
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.common;

import org.eclipse.core.resources.IProject;

public interface IClassContributionProvider {
	public class ContributionData {
		public final String bundleName;
		public final String className;
		public final String sourceType;
		public final String iconPath;

		/**
		 * This is used if the resource is not contained in a referenced bundle,
		 * not a bundle, or not a project. It can be a jar file or a folder.
		 */
		public String installLocation;
		/**
		 * Path to the resource relative to the installLocation
		 */
		public String resourceRelativePath;

		public ContributionData(String bundleName, String className, String sourceType, String iconPath) {
			this.bundleName = bundleName;
			this.className = className;
			this.sourceType = sourceType;
			this.iconPath = iconPath;
		}
	}

	public class Filter {
		public final IProject project;
		public final String namePattern;
		public int maxResults;
		public Object userData;

		public Filter(IProject project, String namePattern) {
			this.project = project;
			this.namePattern = namePattern;
		}
	}

	public interface ContributionResultHandler {
		public void result(ContributionData data);

		/**
		 * This method is called at the end of a search. If the search
		 * terminated because maxResults was exceeded, the hint parameter will
		 * contain the number of additional results, or -1 if there are an
		 * unspecified number of additional results.
		 *
		 * No there are no additional results, hint will be 0.
		 */
		public void moreResults(int hint, Filter filter);
	}

	public void findContribution(Filter filter, ContributionResultHandler handler);
}

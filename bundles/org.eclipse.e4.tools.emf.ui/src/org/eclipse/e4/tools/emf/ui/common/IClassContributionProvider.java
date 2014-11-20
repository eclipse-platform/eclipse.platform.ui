/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 * Steven Spungin <steven@spungin.tv> - Bug 424730, Bug 436281
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.common;

import java.util.EnumSet;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

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

		// These items were added for extended filter functionality
		private List<String> bundles;
		private List<String> packages;
		private List<String> locations;
		private EnumSet<ResourceSearchScope> searchScope = EnumSet.noneOf(ResourceSearchScope.class);
		private boolean includeNonBundles;
		private IProgressMonitor progressMonitor;
		/**
		 * This member can be null. It provides a way for the provider to signal
		 * various state changes.
		 */
		private IProviderStatusCallback providerStatusCallback;

		public Filter(IProject project, String namePattern) {
			this.project = project;
			this.namePattern = namePattern;
		}

		/**
		 * Sets the bundles to filter on
		 *
		 * @param filterBundles
		 */
		public void setBundles(List<String> filterBundles) {
			bundles = filterBundles;
		}

		public void setPackages(List<String> filterPackages) {
			packages = filterPackages;
		}

		public List<String> getBundles() {
			return bundles;
		}

		public List<String> getPackages() {
			return packages;
		}

		public List<String> getLocations() {
			return locations;
		}

		/**
		 * Sets the installed project locations to filter on
		 *
		 * @param locations
		 */
		public void setLocations(List<String> locations) {
			this.locations = locations;
		}

		public EnumSet<ResourceSearchScope> getSearchScope() {
			return searchScope;
		}

		/**
		 * Sets the scope for the search.
		 *
		 * @param searchScope
		 */
		public void setSearchScope(EnumSet<ResourceSearchScope> searchScope) {
			this.searchScope = searchScope;
		}

		/**
		 * Include plain old java projects, resource projects, and others in
		 * searches. No manifest required.
		 *
		 * @param includeNonBundles
		 */
		public void setIncludeNonBundles(boolean includeNonBundles) {
			this.includeNonBundles = includeNonBundles;
		}

		public boolean isIncludeNonBundles() {
			return includeNonBundles;
		}

		public IProgressMonitor getProgressMonitor() {
			return progressMonitor;
		}

		public void setProgressMonitor(IProgressMonitor progressMonitor) {
			this.progressMonitor = progressMonitor;
		}

		public IProviderStatusCallback getProviderStatusCallback() {
			return providerStatusCallback;
		}

		public void setProviderStatusCallback(IProviderStatusCallback providerStatusCallback) {
			this.providerStatusCallback = providerStatusCallback;
		}

	}

	public interface ContributionResultHandler {
		// hints for flags parameter
		/**
		 * There are more results to display, but the amount is not known
		 */
		static public final int MORE_UNKNOWN = -1;
		/**
		 * The operation was canceled. There may be additional results.
		 */
		static public final int MORE_CANCELED = -2;

		public void result(ContributionData data);

		/**
		 * This method is called at the end of a search. If the search
		 * terminated because maxResults was exceeded, the hint parameter will
		 * contain the number of additional results, or MORE_UNKNOWN if there
		 * are an unspecified number of additional results.
		 *
		 * If the search was cancelled, the hint will be MORE_CANCELED
		 *
		 * If there are no additional results, hint will be 0
		 */
		public void moreResults(int hint, Filter filter);
	}

	public void findContribution(Filter filter, ContributionResultHandler handler);
}

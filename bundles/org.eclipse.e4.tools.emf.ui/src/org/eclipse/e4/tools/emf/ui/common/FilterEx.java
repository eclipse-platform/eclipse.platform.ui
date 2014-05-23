/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 424730, Bug 435625
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.common;

import java.util.EnumSet;
import java.util.List;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.tools.emf.ui.common.IClassContributionProvider.Filter;

/**
 * An extension to the Filter class, allowing for bundles, packages, and
 * locations to be specified.
 *
 * @author Steven Spungin
 *
 */
public class FilterEx extends Filter {

	private List<String> bundles;
	private List<String> packages;
	private List<String> locations;
	private EnumSet<ResourceSearchScope> searchScope = EnumSet.noneOf(ResourceSearchScope.class);
	private boolean includeNonBundles;
	private IProgressMonitor progressMonitor;

	public FilterEx(IProject project, String regNamePattern) {
		super(project, regNamePattern);
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

	/**
	 * Sets the packages to filter on
	 *
	 * @return
	 */
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

}

/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.util.*;
import org.eclipse.core.internal.utils.Cache;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeMatcher;
import org.eclipse.core.runtime.content.IContentTypeManager.ISelectionPolicy;

/**
 * Manages project-specific content type behavior.
 * 
 * @see ContentDescriptionManager
 * @see ISelectionPolicy
 * @since 3.1
 */
public class ProjectContentTypes {

	/**
	 * A project-aware content type selection policy.   
	 */
	private class ProjectContentTypeSelectionPolicy implements ISelectionPolicy {

		private Project project;

		public ProjectContentTypeSelectionPolicy(Project project) {
			this.project = project;
		}

		public IContentType[] select(IContentType[] candidates, boolean fileName, boolean content) {
			return ProjectContentTypes.this.select(project, candidates, fileName, content);
		}

	}

	private Cache contentTypesPerProject;
	private Workspace workspace;

	public ProjectContentTypes(Workspace workspace) {
		this.workspace = workspace;
		// keep cache small
		this.contentTypesPerProject = new Cache(5, 30, 0.4);
	}

	private Set getAssociatedContentTypes(Project project) {
		final ResourceInfo info = project.getResourceInfo(false, false);
		if (info == null)
			// the project has been deleted
			return null;
		final String projectName = project.getName();
		synchronized (contentTypesPerProject) {
			Cache.Entry entry = contentTypesPerProject.getEntry(projectName);
			if (entry != null)
				// we have an entry...
				if (entry.getTimestamp() == info.getContentId())
					// ...and it is not stale, so just return it
					return (Set) entry.getCached();
			// no cached information found, have to collect associated content types  
			Set result = collectAssociatedContentTypes(project);
			if (entry == null)
				// there was no entry before - create one
				entry = contentTypesPerProject.addEntry(projectName, result, info.getContentId());
			else {
				// just update the existing entry
				entry.setTimestamp(info.getContentId());
				entry.setCached(result);
			}
			return result;
		}
	}

	IContentTypeMatcher getMatcherFor(Project project) throws CoreException {
		ProjectInfo info = (ProjectInfo) project.getResourceInfo(false, false);
		//fail if project has been deleted concurrently
		if (info == null)
			project.checkAccessible(project.getFlags(info));
		IContentTypeMatcher matcher = info.getMatcher();
		if (matcher != null)
			return matcher;
		matcher = Platform.getContentTypeManager().getMatcher(new ProjectContentTypeSelectionPolicy(project), null);
		info.setMatcher(matcher);
		return matcher;
	}

	/**
	 * Collect content types associated to the natures configured for the given project.
	 */
	private Set collectAssociatedContentTypes(Project project) {
		String[] enabledNatures = workspace.getNatureManager().getEnabledNatures(project);
		if (enabledNatures.length == 0)
			return Collections.EMPTY_SET;
		Set related = new HashSet(enabledNatures.length);
		for (int i = 0; i < enabledNatures.length; i++) {
			ProjectNatureDescriptor descriptor = (ProjectNatureDescriptor) workspace.getNatureDescriptor(enabledNatures[i]);
			if (descriptor == null)
				// no descriptor found for the nature, skip it
				continue;
			String[] natureContentTypes = descriptor.getContentTypeIds();
			for (int j = 0; j < natureContentTypes.length; j++)
				// collect associate content types
				related.add(natureContentTypes[j]);
		}
		return related;
	}

	/**
	 * Implements project specific, nature-based selection policy. No content types are vetoed.
	 * 
	 * The criteria for this policy is as follows:
	 * <ol>
	 * <li>associated content types should appear before non-associated content types</li>
	 * <li>otherwise, relative ordering should be preserved.</li>
	 * </ol>
	 * 
	 *  @see ISelectionPolicy
	 */
	final IContentType[] select(Project project, IContentType[] candidates, boolean fileName, boolean content) {
		// since no vetoing is done here, don't go further if there is nothing to sort
		if (candidates.length < 2)
			return candidates;
		final Set associated = getAssociatedContentTypes(project);
		if (associated == null || associated.isEmpty())
			// project has no content types associated
			return candidates;
		int associatedCount = 0;
		for (int i = 0; i < candidates.length; i++)
			// is it an associated content type?
			if (associated.contains(candidates[i].getId())) {
				// need to move it to the right spot (unless all types visited so far are associated as well)
				if (associatedCount < i) {
					final IContentType promoted = candidates[i];
					// move all non-associated content types before it one one position up...
					for (int j = i; j > associatedCount; j--)
						candidates[j] = candidates[j - 1];
					// ...so there is an empty spot for the content type we are promoting
					candidates[associatedCount] = promoted;
				}
				associatedCount++;
			}
		return candidates;
	}
}
/*******************************************************************************
 * Copyright (c) 2023, 2026 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui.text;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.ui.text.Match;
import org.eclipse.search.ui.text.MatchFilter;

public class OuterProjectFileFilter extends MatchFilter {

	/**
	 * Remembers for the files of the reported matches whether they are filtered.
	 * <p>
	 * The filter state is evaluated for every single match, but
	 * {@link org.eclipse.core.resources.IWorkspaceRoot#findFilesForLocationURI(URI)}
	 * iterates over all projects of the workspace and is therefore much too
	 * expensive to be called once per match: a file usually has many matches.
	 * </p>
	 * <p>
	 * The keys are the file handles held by the matches ({@link Match#getElement()}
	 * ), and the values don't reference them, so the remembered states are garbage
	 * collected together with the search result they were computed for: the filter,
	 * which is shared by all file search results, doesn't keep them alive.
	 * </p>
	 * <p>
	 * Outdated states are discarded by replacing the whole map. A state that is
	 * computed while the map is replaced is put into the replaced map and is
	 * therefore never seen again, so an invalidation cannot be lost.
	 * </p>
	 */
	private volatile Map<IFile, Boolean> filterStates = newFilterStates();

	private final AtomicBoolean isListening = new AtomicBoolean();

	/**
	 * The files representing a location only change if projects are added, removed,
	 * opened, closed or moved.
	 */
	private final IResourceChangeListener projectChangeListener = event -> {
		if (affectsProjects(event.getDelta())) {
			filterStates = newFilterStates();
		}
	};

	private static Map<IFile, Boolean> newFilterStates() {
		return Collections.synchronizedMap(new WeakHashMap<>());
	}

	private void ensureListeningToProjectChanges() {
		if (!isListening.get() && isListening.compareAndSet(false, true)) {
			ResourcesPlugin.getWorkspace().addResourceChangeListener(projectChangeListener,
					IResourceChangeEvent.POST_CHANGE);
		}
	}

	private static boolean affectsProjects(IResourceDelta delta) {
		if (delta == null) {
			return false;
		}
		for (IResourceDelta projectDelta : delta.getAffectedChildren()) {
			if (projectDelta.getKind() != IResourceDelta.CHANGED) {
				return true; // project added or removed
			}
			int flags= projectDelta.getFlags();
			if ((flags & (IResourceDelta.OPEN | IResourceDelta.DESCRIPTION | IResourceDelta.MOVED_FROM
					| IResourceDelta.MOVED_TO | IResourceDelta.LOCAL_CHANGED | IResourceDelta.REPLACED)) != 0) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean filters(Match match) {
		if (!(match instanceof FileMatch fileMatch)) {
			return false;
		}
		IFile file= fileMatch.getFile();
		// the listener is registered before the state of the workspace is read, so
		// that every change that invalidates the computed state is reported
		ensureListeningToProjectChanges();
		Map<IFile, Boolean> states= filterStates;
		Boolean isFiltered= states.get(file);
		if (isFiltered == null) {
			// computed without holding a lock: it may be computed twice for a file,
			// but it must not block the other search threads
			isFiltered= Boolean.valueOf(computeFilters(file));
			states.put(file, isFiltered);
		}
		return isFiltered.booleanValue();
	}

	private static boolean computeFilters(IFile file) {
		URI locationUri= file.getLocationURI();
		if (locationUri == null) {
			return false;
		}
		Optional<IFile> innermostFile= Arrays
				.stream(file.getWorkspace().getRoot().findFilesForLocationURI(locationUri)) //
				// Don't consider the content of a closed project for filtering
				// because the matches there cannot be shown
				.filter(aFile -> aFile.getProject().isAccessible())
				// shortest workspace (project relative) full path means most
				// nested project
				.min(Comparator.comparingInt(aFile -> aFile.getFullPath().segments().length));
		return innermostFile.isPresent() && !file.equals(innermostFile.get());
	}

	@Override
	public String getName() {
		return SearchMessages.TextSearchInnermostProjectFilter_name;
	}

	@Override
	public String getDescription() {
		return SearchMessages.TextSearchInnermostProjectFilter_description;
	}

	@Override
	public String getActionLabel() {
		return SearchMessages.TextSearchInnermostProjectFilter_action_label;
	}

	@Override
	public String getID() {
		return "filter_innermost_project"; //$NON-NLS-1$
	}

}

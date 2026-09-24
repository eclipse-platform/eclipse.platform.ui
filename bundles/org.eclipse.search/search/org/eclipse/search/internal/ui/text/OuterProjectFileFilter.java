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

import org.eclipse.core.runtime.IPath;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProjectDescription;
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
	 * The keys are the file handles held by the matches
	 * ({@link Match#getElement()}), and the values don't reference them, so the
	 * remembered states are garbage collected together with the search result they
	 * were computed for: the filter doesn't keep them alive.
	 * </p>
	 * <p>
	 * The state doesn't depend on the filter instance, so it is shared by all of
	 * them: the states are computed for all file search results anyway and one
	 * single resource change listener is sufficient to invalidate them. They are
	 * however not shared by subsequent searches: {@link #clearRememberedStates()}
	 * discards them whenever a search is started, so that a state can never be
	 * reused by a search that was started after the workspace has changed in a way
	 * the listener cannot detect.
	 * </p>
	 * <p>
	 * Outdated states are discarded by replacing the whole map. A state that is
	 * computed while the map is replaced is put into the replaced map and is
	 * therefore never seen again, so an invalidation cannot be lost.
	 * </p>
	 */
	private static volatile Map<IFile, Boolean> filterStates = newFilterStates();

	private static final Object listenerLock = new Object();

	/**
	 * Whether {@link #PROJECT_CHANGE_LISTENER} is registered. Written only while
	 * {@link #listenerLock} is held and only <em>after</em> the registration, so
	 * that a thread which reads <code>true</code> is guaranteed to be notified
	 * about the changes that happen from now on.
	 */
	private static volatile boolean isListening;

	/**
	 * The files representing a location change if projects are added, removed,
	 * opened, closed or moved, and if linked resources are created, changed or
	 * removed.
	 * <p>
	 * The links are stored in the description of their project, which is written
	 * whenever a link is added, changed or removed, so a change of the description
	 * file is a sufficient (and cheap to detect) indication: the whole delta must
	 * not be visited in the notification thread. The same is true for resource
	 * filters and for virtual folders.
	 * </p>
	 * <p>
	 * Not reported is the creation or deletion of a file below a linked folder,
	 * although such a file can be represented by more than one project as well.
	 * Invalidating the states for every added or removed file would discard them
	 * whenever a build or a refresh touches the workspace, which would defeat the
	 * purpose of remembering them. Since the states are remembered for all search
	 * results and are keyed by the file handles, which are equal for all searches,
	 * such a state must not be able to outlive the search it was computed for:
	 * {@link #clearRememberedStates()} discards them whenever a search is started.
	 * </p>
	 */
	private static final IResourceChangeListener PROJECT_CHANGE_LISTENER = event -> {
		if (affectsProjects(event.getDelta())) {
			filterStates = newFilterStates();
		}
	};

	/**
	 * The links, filters and the location of a project are stored in this file.
	 */
	private static final IPath PROJECT_DESCRIPTION_FILE = IPath.fromOSString(IProjectDescription.DESCRIPTION_FILE_NAME);

	private static Map<IFile, Boolean> newFilterStates() {
		return Collections.synchronizedMap(new WeakHashMap<>());
	}

	/**
	 * Discards all remembered filter states, so that they are computed again for
	 * the matches that are reported from now on. Called when a file search is
	 * started: the states are shared by all search results and are keyed by the
	 * file handles the matches hold, which are equal for all searches, so a state
	 * must not be reused by a subsequent search.
	 * <p>
	 * This is what makes the changes that {@link #PROJECT_CHANGE_LISTENER} cannot
	 * detect - a file added or removed below a linked folder - recoverable by
	 * running the search again.
	 * </p>
	 */
	public static void clearRememberedStates() {
		filterStates= newFilterStates();
	}

	/**
	 * Registers the resource change listener on the first evaluation of a match and
	 * returns only after it is registered, so that no filter state is ever computed
	 * and remembered while the changes that invalidate it are not reported yet. The
	 * listener is registered only once and is never removed: it is as long-living
	 * as this class and doesn't hold on to any state.
	 */
	private static void ensureListeningToProjectChanges() {
		// the volatile read avoids locking and a contended write for every match
		if (isListening) {
			return;
		}
		synchronized (listenerLock) {
			if (!isListening) {
				ResourcesPlugin.getWorkspace().addResourceChangeListener(PROJECT_CHANGE_LISTENER,
						IResourceChangeEvent.POST_CHANGE);
				// published after the registration: a thread that skips the lock
				// because it reads 'true' has already missed no notification
				isListening= true;
			}
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
			// linked resources, filters and virtual folders are stored in the
			// description of their project: looking for that single file is much
			// cheaper than visiting the whole delta
			if (projectDelta.findMember(PROJECT_DESCRIPTION_FILE) != null) {
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
		// that every change that invalidates the computed state is reported: the
		// state of the workspace is read after the changes this thread may miss
		ensureListeningToProjectChanges();
		Map<IFile, Boolean> states= filterStates;
		Boolean isFiltered= states.get(file);
		if (isFiltered == null) {
			// computed without holding a lock: it may be computed twice for a file,
			// but it must not block the other search threads
			isFiltered= Boolean.valueOf(computeIsFiltered(file));
			states.put(file, isFiltered);
		}
		return isFiltered.booleanValue();
	}

	private static boolean computeIsFiltered(IFile file) {
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

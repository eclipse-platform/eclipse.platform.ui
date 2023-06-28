/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search2.internal.ui.text;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.WorkspaceJob;

import org.eclipse.core.filebuffers.IFileBuffer;

import org.eclipse.jface.text.Position;

import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.Match;

import org.eclipse.search2.internal.ui.InternalSearchUI;

public class MarkerHighlighter extends Highlighter {
	private final IFile fFile;
	private final Map<Match, IMarker> fMatchesToAnnotations;
	private final Object addHighlightsJobFamily;
	private volatile boolean fDisposed;
	private WorkspaceJob fRemoveAllJob;
	private WorkspaceJob fContentReplacedJob;

	public MarkerHighlighter(IFile file) {
		fFile= file;
		fMatchesToAnnotations= new HashMap<>();
		fDisposed = false;
		addHighlightsJobFamily = new Object();
	}

	@Override
	public void addHighlights(final Match[] matches) {
		WorkspaceJob addHighlightsJob = new MarkerHighlighterWorkspaceJob("Adding highlights", fFile) { //$NON-NLS-1$
			@Override
			void runOperation(IProgressMonitor monitor) {
				addHighlightsInternal(matches, monitor);
			}

			@Override
			public boolean belongsTo(Object family) {
				return family == addHighlightsJobFamily || super.belongsTo(family);
			}
		};
		addHighlightsJob.schedule();
	}

	private void addHighlightsInternal(final Match[] matches, IProgressMonitor monitor) {
		try {
			SearchPlugin.getWorkspace().run((IWorkspaceRunnable) submonitor -> {
				if (fDisposed) {
					return;
				}
				for (Match match : matches) {
					if (monitor.isCanceled() || submonitor.isCanceled()) {
						return;
					}
					IMarker marker;
					marker = createMarker(match);
					if (marker != null)
						fMatchesToAnnotations.put(match, marker);
				}
			}, fFile, IWorkspace.AVOID_UPDATE, null);
		} catch (CoreException e) {
			// just log the thing. There's nothing we can do anyway.
			SearchPlugin.log(e.getStatus());
		}
	}

	private IMarker createMarker(Match match) throws CoreException {
		Position position= InternalSearchUI.getInstance().getPositionTracker().getCurrentPosition(match);
		if (position == null) {
			if (match.getOffset() < 0 || match.getLength() < 0)
				return null;
			position= new Position(match.getOffset(), match.getLength());
		} else {
			// need to clone position, can't have it twice in a document.
			position= new Position(position.getOffset(), position.getLength());
		}

		HashMap<String, Integer> attributes= new HashMap<>(4);
		if (match.getBaseUnit() == Match.UNIT_CHARACTER) {
			attributes.put(IMarker.CHAR_START, Integer.valueOf(position.getOffset()));
			attributes.put(IMarker.CHAR_END, Integer.valueOf(position.getOffset()+position.getLength()));
		} else {
			attributes.put(IMarker.LINE_NUMBER, Integer.valueOf(position.getOffset()));
		}
		IMarker marker = match.isFiltered() ? fFile.createMarker(SearchPlugin.FILTERED_SEARCH_MARKER, attributes)
				: fFile.createMarker(NewSearchUI.SEARCH_MARKER, attributes);

		return marker;
	}

	@Override
	public void removeHighlights(Match[] matches) {
		WorkspaceJob removeHighlightsJob = new MarkerHighlighterWorkspaceJob("Removing highlights", fFile) { //$NON-NLS-1$
			@Override
			void runOperation(IProgressMonitor monitor) {
				removeHighlightsInternal(matches);
			}
		};
		// don't cancel the job, as we want to previous highlights removal to go
		// through
		removeHighlightsJob.schedule();
	}

	private void removeHighlightsInternal(Match[] matches) {
		for (Match match : matches) {
			IMarker marker= fMatchesToAnnotations.remove(match);
			if (marker != null) {
				try {
					marker.delete();
				} catch (CoreException e) {
					// just log the thing. There's nothing we can do anyway.
					SearchPlugin.log(e);
				}
			}
		}
	}

	@Override
	public  void removeAll() {
		cancelAddingHighlights();
		if (fRemoveAllJob == null) {
			fRemoveAllJob = new MarkerHighlighterWorkspaceJob("Removing all search highlights", fFile) { //$NON-NLS-1$
				@Override
				void runOperation(IProgressMonitor monitor) {
					removeAllInternal();
				}
			};
		}
		fRemoveAllJob.cancel();
		fRemoveAllJob.schedule();
	}

	private void removeAllInternal() {
		try {
			fFile.deleteMarkers(NewSearchUI.SEARCH_MARKER, true, IResource.DEPTH_INFINITE);
			fFile.deleteMarkers(SearchPlugin.FILTERED_SEARCH_MARKER, true, IResource.DEPTH_INFINITE);
			fMatchesToAnnotations.clear();
		} catch (CoreException e) {
			// just log the thing. There's nothing we can do anyway.
			SearchPlugin.log(e.getStatus());
		}
	}

	@Override
	protected void handleContentReplaced(IFileBuffer buffer) {
		if (!buffer.getLocation().equals(fFile.getFullPath()))
			return;
		if (fContentReplacedJob == null) {
			fContentReplacedJob = new MarkerHighlighterWorkspaceJob("Updating search highlights", fFile) { //$NON-NLS-1$
				@Override
				void runOperation(IProgressMonitor monitor) {
					handleContentReplacedInternal(monitor);
				}
			};
		}
		fContentReplacedJob.cancel();
		fContentReplacedJob.schedule();
	}

	private void handleContentReplacedInternal(IProgressMonitor monitor) {
		if (fDisposed) {
			return;
		}
		Match[] matches= new Match[fMatchesToAnnotations.size()];
		fMatchesToAnnotations.keySet().toArray(matches);
		removeAllInternal();
		addHighlightsInternal(matches, monitor);
	}

	@Override
	public void dispose() {
		fDisposed = true;
		cancelAddingHighlights();
		super.dispose();
	}

	private void cancelAddingHighlights() {
		if (fContentReplacedJob != null) {
			fContentReplacedJob.cancel();
		}
		Job.getJobManager().cancel(addHighlightsJobFamily);
	}

	static abstract class MarkerHighlighterWorkspaceJob extends WorkspaceJob {

		public MarkerHighlighterWorkspaceJob(String jobName, IFile file) {
			super(jobName);
			setRule(file);
			setSystem(true);
		}

		@Override
		public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			runOperation(monitor);
			return Status.OK_STATUS;
		}

		@Override
		public boolean belongsTo(Object family) {
			return family == MarkerHighlighter.class;
		}

		abstract void runOperation(IProgressMonitor monitor) throws CoreException;
	}
}

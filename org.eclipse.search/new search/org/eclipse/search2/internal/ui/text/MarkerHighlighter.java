/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search2.internal.ui.text;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;

import org.eclipse.core.filebuffers.IFileBuffer;

import org.eclipse.jface.text.Position;

import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.Match;

import org.eclipse.search2.internal.ui.InternalSearchUI;

public class MarkerHighlighter extends Highlighter {
	private IFile fFile;
	private Map<Match, IMarker> fMatchesToAnnotations;

	public MarkerHighlighter(IFile file) {
		fFile= file;
		fMatchesToAnnotations= new HashMap<>();
	}

	@Override
	public void addHighlights(final Match[] matches) {
		try {
			SearchPlugin.getWorkspace().run(new IWorkspaceRunnable() {
				@Override
				public void run(IProgressMonitor monitor) throws CoreException {
					for (Match match : matches) {
						IMarker marker;
						marker = createMarker(match);
						if (marker != null)
							fMatchesToAnnotations.put(match, marker);
					}
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
		IMarker marker= match.isFiltered()
			? fFile.createMarker(SearchPlugin.FILTERED_SEARCH_MARKER)
			: fFile.createMarker(NewSearchUI.SEARCH_MARKER);
		HashMap<String, Integer> attributes= new HashMap<>(4);
		if (match.getBaseUnit() == Match.UNIT_CHARACTER) {
			attributes.put(IMarker.CHAR_START, Integer.valueOf(position.getOffset()));
			attributes.put(IMarker.CHAR_END, Integer.valueOf(position.getOffset()+position.getLength()));
		} else {
			attributes.put(IMarker.LINE_NUMBER, Integer.valueOf(position.getOffset()));
		}
		marker.setAttributes(attributes);
		return marker;
	}

	@Override
	public void removeHighlights(Match[] matches) {
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
		Match[] matches= new Match[fMatchesToAnnotations.keySet().size()];
		fMatchesToAnnotations.keySet().toArray(matches);
		removeAll();
		addHighlights(matches);
	}
}

/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.search.internal.ui.text;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.ui.IQueryListener;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.Match;

public class SearchResultUpdater implements IResourceChangeListener, IQueryListener {
	private AbstractTextSearchResult fResult;

	public SearchResultUpdater(AbstractTextSearchResult result) {
		fResult= result;
		NewSearchUI.addQueryListener(this);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta= event.getDelta();
		if (delta != null)
			handleDelta(delta);
	}

	private void handleDelta(IResourceDelta d) {
		try {
			d.accept(delta -> {
				switch (delta.getKind()) {
					case IResourceDelta.ADDED :
						return false;
					case IResourceDelta.REMOVED :
						IResource res= delta.getResource();
						if (res instanceof IFile) {
							Match[] matches= fResult.getMatches(res);
							fResult.removeMatches(matches);
						}
						break;
					case IResourceDelta.CHANGED :
						// handle changed resource
						break;
				}
				return true;
			});
		} catch (CoreException e) {
			SearchPlugin.log(e);
		}
	}

	@Override
	public void queryAdded(ISearchQuery query) {
		// don't care
	}

	@Override
	public void queryRemoved(ISearchQuery query) {
		if (fResult.equals(query.getSearchResult())) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
			NewSearchUI.removeQueryListener(this);
		}
	}

	@Override
	public void queryStarting(ISearchQuery query) {
		// don't care
	}

	@Override
	public void queryFinished(ISearchQuery query) {
		// don't care
	}
}

/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.search;

import java.lang.reflect.*;
import java.text.*;
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.search.*;
import org.eclipse.help.internal.workingset.*;
import org.eclipse.help.ui.internal.*;
import org.eclipse.help.ui.internal.util.*;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.search.ui.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.*;
import org.eclipse.ui.help.*;

/**
 * Help Search Operation.
 */
public class SearchOperation extends WorkspaceModifyOperation {
	// Images
	private static final ImageDescriptor IMAGE_DSCR_SEARCH =
		ImageDescriptor.createFromURL(
			WorkbenchResources.getImagePath(
				IHelpUIConstants.IMAGE_FILE_SEARCH));
	private static final ImageDescriptor IMAGE_DSCR_TOPIC =
		ImageDescriptor.createFromURL(
			WorkbenchResources.getImagePath(IHelpUIConstants.IMAGE_FILE_TOPIC));
	// Resource we will be using
	private static final IResource resource =
		ResourcesPlugin.getWorkspace().getRoot();
	private static ImageRegistry imgRegistry = null;
	private SearchQueryData queryData = null;
	/**
	 * SearchOperation constructor.
	 * @param data SearchQueryData
	 */
	public SearchOperation(SearchQueryData data) {
		if (imgRegistry == null) {
			imgRegistry = WorkbenchHelpPlugin.getDefault().getImageRegistry();
			imgRegistry.put(
				IHelpUIConstants.IMAGE_KEY_SEARCH,
				IMAGE_DSCR_SEARCH);
			imgRegistry.put(IHelpUIConstants.IMAGE_KEY_TOPIC, IMAGE_DSCR_TOPIC);
		}
		queryData = data;
	}
	/**
	 * @see WorkspaceModifyOperation#execute(IProgressMonitor)
	 */
	protected void execute(IProgressMonitor monitor)
		throws CoreException, InvocationTargetException, InterruptedException {
		try {
			WorkingSet[] workingSets = null; // no filtering
			if (queryData.isBookFiltering()) {
				IWorkingSet[] allWorkingSets =
					queryData.getSelectedWorkingSets();
				ArrayList helpWorkingSets = new ArrayList();
				WorkingSetManager wsmgr =
					HelpSystem.getWorkingSetManager(queryData.getLocale());
				for (int i = 0; i < allWorkingSets.length; i++) {
					WorkingSet ws = wsmgr.getWorkingSet(allWorkingSets[i].getName());
					if (ws != null) {
						helpWorkingSets.add(ws);
					}
				}
				workingSets =
					(WorkingSet[]) helpWorkingSets.toArray(
						new WorkingSet[helpWorkingSets.size()]);
			}
			SearchResults results =
				new SearchResults(
					workingSets,
					queryData.getMaxHits(),
					queryData.getLocale());
			HelpSystem.getSearchManager().search(
				queryData.getSearchQuery(),
				results,
				monitor);
			displayResults(results.getSearchHits());
		} catch(QueryTooComplexException qe){
			displayResults(new SearchHit[0]);
		} catch (OperationCanceledException oce) {
			// allowed, no logging
			monitor.done();
			throw oce;
		} catch (Exception e) {
			WorkbenchHelpPlugin.logError(
				WorkbenchResources.getString("WE021"),
				e);
		}
		monitor.done();
	}
	private void displayResults(SearchHit[] searchHits) {
		ISearchResultView sView = SearchUI.getSearchResultView();
		if (sView != null)
			sView
				.searchStarted(
					(IActionGroupFactory) null,
					WorkbenchResources.getString(
						"singleSearchResult",
						queryData.getSearchWord()),
					WorkbenchResources.getString(
						"multipleSearchResult",
						queryData.getSearchWord(),
						"{0}"),
					IMAGE_DSCR_SEARCH,
					IHelpUIConstants.RESULTS_PAGE_ID,
					new LabelProvider() {
			public String getText(Object element) {
				if (element instanceof ISearchResultViewEntry)
					try {
						ISearchResultViewEntry entry =
							(ISearchResultViewEntry) element;
						return (String) entry.getSelectedMarker().getAttribute(
							IHelpUIConstants.HIT_MARKER_ATTR_LABEL);
					} catch (CoreException ce) {
					}
				return "";
			}
			public Image getImage(Object element) {
				if (element instanceof ISearchResultViewEntry)
					return imgRegistry.get(IHelpUIConstants.IMAGE_KEY_TOPIC);
				return null;
			}
		}, new org.eclipse.jface.action.Action() {
			public void run() {
				ISearchResultView view = SearchUI.getSearchResultView();
				view.getSelection();
				ISelection selection = view.getSelection();
				Object element = null;
				if (selection instanceof IStructuredSelection)
					element =
						((IStructuredSelection) selection).getFirstElement();
				if (element instanceof ISearchResultViewEntry) {
					ISearchResultViewEntry entry =
						(ISearchResultViewEntry) element;
					try {
						IHelp ihelp = WorkbenchHelp.getHelpSupport();
						if (ihelp instanceof DefaultHelp)
							((DefaultHelp) ihelp).displaySearch(
								queryData.toURLQuery(),
								(String) entry
									.getSelectedMarker()
									.getAttribute(
									IHelpUIConstants.HIT_MARKER_ATTR_HREF));
					} catch (Exception e) {
						System.out.println(e);
					}
				}
			}
		}, new IGroupByKeyComputer() {
			public Object computeGroupByKey(IMarker marker) {
				try {
					if (marker
						.getAttribute(IHelpUIConstants.HIT_MARKER_ATTR_HREF)
						!= null)
						return marker.getAttribute(
							IHelpUIConstants.HIT_MARKER_ATTR_HREF);
				} catch (CoreException ce) {
				}
				return "UNKNOWN";
			}
		}, this);
		// Delete all previous results
		try {
			resource.deleteMarkers(
				IHelpUIConstants.HIT_MARKER_ID,
				true,
				IResource.DEPTH_INFINITE);
		} catch (CoreException ex) {
		}
		createResultsMarkers(searchHits, sView);
		sView.searchFinished();
	}
	private void createResultsMarkers(
		SearchHit[] searchHits,
		ISearchResultView sView) {
		for (int i = 0; i < searchHits.length; i++) {
			try {
				IMarker marker = null;
				marker = resource.createMarker(IHelpUIConstants.HIT_MARKER_ID);
				marker.setAttribute(
					IHelpUIConstants.HIT_MARKER_ATTR_HREF,
					searchHits[i].getHref());
				marker.setAttribute(
					IHelpUIConstants.HIT_MARKER_ATTR_RESULTOF,
					queryData.toURLQuery());

				// Use Score percentage and label as topic label
				float score = searchHits[i].getScore();
				NumberFormat percentFormat = NumberFormat.getPercentInstance();
				String scoreString = percentFormat.format(score);
				String label = scoreString + " " + searchHits[i].getLabel();
				marker.setAttribute(
					IHelpUIConstants.HIT_MARKER_ATTR_LABEL,
					label);
				marker.setAttribute(
					IHelpUIConstants.HIT_MARKER_ATTR_ORDER,
					new Integer(i).toString());
				sView.addMatch(
					searchHits[i].getLabel(),
					marker.getAttribute(IHelpUIConstants.HIT_MARKER_ATTR_HREF),
					resource,
					marker);
			} catch (CoreException ce) {
			}
		}
	}
}

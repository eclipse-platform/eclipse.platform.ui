/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.ui.internal.search;

import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.*;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.search.*;
import org.eclipse.help.internal.ui.*;
import org.eclipse.help.internal.ui.util.WorkbenchResources;
import org.eclipse.help.internal.util.Logger;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.search.ui.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Help Search Operation.
 */
public class SearchOperation extends WorkspaceModifyOperation {
	// Images
	private static final ImageDescriptor IMAGE_DSCR_SEARCH =
		ImageDescriptor.createFromURL(
			WorkbenchResources.getImagePath(IHelpUIConstants.IMAGE_KEY_SEARCH));
	private static final ImageDescriptor IMAGE_DSCR_TOPIC =
		ImageDescriptor.createFromURL(
			WorkbenchResources.getImagePath(IHelpUIConstants.IMAGE_KEY_TOPIC));
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
			imgRegistry.put(IHelpUIConstants.IMAGE_KEY_SEARCH, IMAGE_DSCR_SEARCH);
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
			Collection scope = null;
			if (queryData.isBookFiltering()) {
				scope = new ArrayList();
				Collection books = queryData.getSelectedBooks();
				for (Iterator it = books.iterator(); it.hasNext();) {
					scope.add(((IToc) it.next()).getHref());
				}
			}
			SearchResults results =
				new SearchResults(scope, queryData.getMaxHits(), queryData.getLocale());
			HelpSystem.getSearchManager().search(queryData, results, monitor);
			displayResults(results.getSearchHits());
		} catch (OperationCanceledException oce){
			// allowed, no logging
		} catch (Exception e) {
			Logger.logError(WorkbenchResources.getString("WE021"), e);
		}
		monitor.done();
	}
	private void displayResults(SearchHit[] searchHits) {
		ISearchResultView sView = SearchUI.getSearchResultView();
		if (sView != null)
			sView
				.searchStarted(
					IHelpUIConstants.RESULTS_PAGE_ID,
					WorkbenchResources.getString("singleSearchResult", queryData.getSearchWord()),
					WorkbenchResources.getString(
						"multipleSearchResult",
						queryData.getSearchWord(),
						"{0}"),
					IMAGE_DSCR_SEARCH,
					(IContextMenuContributor)null,
					new LabelProvider() {
			public String getText(Object element) {
				if (element instanceof ISearchResultViewEntry)
					try {
						ISearchResultViewEntry entry = (ISearchResultViewEntry) element;
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
					element = ((IStructuredSelection) selection).getFirstElement();
				if (element instanceof ISearchResultViewEntry) {
					ISearchResultViewEntry entry = (ISearchResultViewEntry) element;
					try {
						IHelp ihelp = WorkbenchHelp.getHelpSupport();
						if (ihelp instanceof DefaultHelp)
							((DefaultHelp) ihelp).displaySearch(
								queryData.toURLQuery(),
								(String) entry.getSelectedMarker().getAttribute(
									IHelpUIConstants.HIT_MARKER_ATTR_HREF));
					} catch (Exception e) {
						System.out.println(e);
					}
				}
			}
		}, new IGroupByKeyComputer() {
			public Object computeGroupByKey(IMarker marker) {
				try {
					if (marker.getAttribute(IHelpUIConstants.HIT_MARKER_ATTR_HREF) != null)
						return marker.getAttribute(IHelpUIConstants.HIT_MARKER_ATTR_HREF);
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
				marker.setAttribute(IHelpUIConstants.HIT_MARKER_ATTR_LABEL, label);
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
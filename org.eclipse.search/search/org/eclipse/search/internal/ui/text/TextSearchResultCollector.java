/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui.text;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionGroup;

import org.eclipse.search.ui.IActionGroupFactory;
import org.eclipse.search.ui.ISearchResultView;
import org.eclipse.search.ui.SearchUI;

import org.eclipse.search.internal.core.text.ITextSearchResultCollector;
import org.eclipse.search.internal.ui.SearchManager;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.internal.ui.util.FileLabelProvider;

public class TextSearchResultCollector implements ITextSearchResultCollector {
	
	private static final String MATCH= SearchMessages.getString("SearchResultCollector.match"); //$NON-NLS-1$
	private static final String MATCHES= SearchMessages.getString("SearchResultCollector.matches"); //$NON-NLS-1$
	private static final String DONE= SearchMessages.getString("SearchResultCollector.done"); //$NON-NLS-1$

	private IProgressMonitor fMonitor;
	private ISearchResultView fView;
	private TextSearchOperation fOperation;
	private int fMatchCount= 0;
	private Integer[] fMessageFormatArgs= new Integer[1];
	

	private class TextSearchActionGroupFactory implements IActionGroupFactory {
		public ActionGroup createActionGroup(ISearchResultView part) {
			return new TextSearchActionGroup(part);
		}
	}
	
	private class TextSearchActionGroup extends ActionGroup {
	
		private ISelectionProvider fSelectionProvider;		
	
		public TextSearchActionGroup(IViewPart part) {
			Assert.isNotNull(part);
			fSelectionProvider= part.getSite().getSelectionProvider();
		}
		
		public void fillContextMenu(IMenuManager menu) {
			if (!isTextSearch())
				return;
			// view must exist if we create a context menu for it.
			ISearchResultView view= SearchUI.getSearchResultView();
			IStructuredSelection selection= null;
			if (getContext().getSelection() instanceof IStructuredSelection)
				selection= (IStructuredSelection)getContext().getSelection();
			else
				selection= StructuredSelection.EMPTY;
			ReplaceAction replaceAll= new ReplaceAction(view.getSite(), (List)getContext().getInput());
			if (replaceAll.isEnabled())
				menu.add(replaceAll);
			ReplaceAction replaceSelected= new ReplaceAction(view.getSite(), selection);
			if (replaceSelected.isEnabled())
				menu.add(replaceSelected);
		}
		
		private boolean isTextSearch() {
			IRunnableWithProgress operation= SearchManager.getDefault().getCurrentSearch().getOperation();
			if (operation instanceof TextSearchOperation) {
				String pattern= ((TextSearchOperation)operation).getPattern();
				return pattern != null && pattern.length() > 0;
			}
			return false;
		}
	}	
			
	/**
	 * Returns the progress monitor used to setup and report progress.
	 */
	public IProgressMonitor getProgressMonitor() {
		return fMonitor;
	}
	
	void setProgressMonitor(IProgressMonitor pm) {
		fMonitor= pm;
	}
	
	/**
	 * Called before the actual search starts.
	 */
	public void aboutToStart() throws CoreException {
		fView= SearchUI.getSearchResultView();
		fMatchCount= 0;
		if (fView != null) {
			fView.searchStarted(
				new TextSearchActionGroupFactory(),
				fOperation.getSingularLabel(),
				fOperation.getPluralLabelPattern(),
				fOperation.getImageDescriptor(),
				TextSearchPage.EXTENSION_POINT_ID,
				new FileLabelProvider(FileLabelProvider.SHOW_LABEL_PATH),
				new GotoMarkerAction(),
				new GroupByKeyComputer(),
				fOperation);
		}
	}
	 
	/**
	 * Accepts the given search result.
	 */
	public void accept(final IResource resource, String line, int start, int length, final int lineNumber) throws CoreException {
		IMarker marker= resource.createMarker(SearchUI.SEARCH_MARKER);
		HashMap attributes= new HashMap(4);
		attributes.put(SearchUI.LINE, line);
		attributes.put(IMarker.CHAR_START, new Integer(start));
		attributes.put(IMarker.CHAR_END, new Integer(start + length));
		attributes.put(IMarker.LINE_NUMBER, new Integer(lineNumber));
		marker.setAttributes(attributes);
		
		String description= resource.getFullPath().lastSegment();
		if (description == null)
			description= "";  //$NON-NLS-1$

		fView.addMatch(description, resource, resource, marker);
		
		fMatchCount++;
		if (!getProgressMonitor().isCanceled())
			getProgressMonitor().subTask(getFormattedMatchesString(fMatchCount));
		
	}
	
	/**
	 * Called when the search has ended.
	 */
	public void done() {
		if (!getProgressMonitor().isCanceled()) {
			String matchesString= getFormattedMatchesString(fMatchCount);
			getProgressMonitor().setTaskName(MessageFormat.format(DONE, new String[]{matchesString}));
		}

		if (fView != null)
			fView.searchFinished();
			
		// Cut no longer unused references because the collector might be re-used
		fView= null;
		fMonitor= null;
	}

	void setOperation(TextSearchOperation operation) {
		fOperation= operation;
	}

	private String getFormattedMatchesString(int count) {
		if (fMatchCount == 1)
			return MATCH;
		fMessageFormatArgs[0]= new Integer(count);
		return MessageFormat.format(MATCHES, fMessageFormatArgs);

	}
}
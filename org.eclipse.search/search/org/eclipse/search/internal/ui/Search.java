/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.search.ui.IActionGroupFactory;
import org.eclipse.search.ui.IContextMenuContributor;
import org.eclipse.search.ui.IGroupByKeyComputer;
import org.eclipse.search.ui.ISearchResultViewEntry;

import org.eclipse.search.internal.ui.util.ExceptionHandler;
/**
 * @deprecated old search
 */
public class Search extends Object {
	private String fPageId;
	private String fSingularLabel;
	private String fPluralLabelPattern;
	private ImageDescriptor fImageDescriptor;
	private ILabelProvider fLabelProvider;
	private ISelection fSelection;
	private ArrayList fResults;
	private IAction fGotoMarkerAction;
	private IContextMenuContributor fContextMenuContributor;
	private IActionGroupFactory fActionGroupFactory;
	private IGroupByKeyComputer	fGroupByKeyComputer;
	private IRunnableWithProgress fOperation;


	public Search(String pageId, String singularLabel, String pluralLabelPattern, ILabelProvider labelProvider, ImageDescriptor imageDescriptor, IAction gotoMarkerAction, IActionGroupFactory groupFactory, IGroupByKeyComputer groupByKeyComputer, IRunnableWithProgress operation) {
		fPageId= pageId;
		fSingularLabel= singularLabel;
		fPluralLabelPattern= pluralLabelPattern;
		fImageDescriptor= imageDescriptor;
		fLabelProvider= labelProvider;
		fGotoMarkerAction= gotoMarkerAction;
		fActionGroupFactory= groupFactory;
		fGroupByKeyComputer= groupByKeyComputer;
		fOperation= operation;
		
		if (fPluralLabelPattern == null)
			fPluralLabelPattern= ""; //$NON-NLS-1$
	}

	public Search(String pageId, String singularLabel, String pluralLabelPattern, ILabelProvider labelProvider, ImageDescriptor imageDescriptor, IAction gotoMarkerAction, IContextMenuContributor contextMenuContributor, IGroupByKeyComputer groupByKeyComputer, IRunnableWithProgress operation) {
		fPageId= pageId;
		fSingularLabel= singularLabel;
		fPluralLabelPattern= pluralLabelPattern;
		fImageDescriptor= imageDescriptor;
		fLabelProvider= labelProvider;
		fGotoMarkerAction= gotoMarkerAction;
		fContextMenuContributor= contextMenuContributor;
		fGroupByKeyComputer= groupByKeyComputer;
		fOperation= operation;
		
		if (fPluralLabelPattern == null)
			fPluralLabelPattern= ""; //$NON-NLS-1$
	}

	/**
	 * Returns the full description of the search.
	 * The description set by the client where
	 * {0} will be replaced by the match count.
	 */
	String getFullDescription() {
		if (fSingularLabel != null && getItemCount() == 1)
			return fSingularLabel;

		// try to replace "{0}" with the match count
		int i= fPluralLabelPattern.lastIndexOf("{0}"); //$NON-NLS-1$
		if (i < 0)
			return fPluralLabelPattern;
		return fPluralLabelPattern.substring(0, i) + getItemCount()+ fPluralLabelPattern.substring(Math.min(i + 3, fPluralLabelPattern.length()));
	}

	/**
	 * Returns a short description of the search.
	 * Cuts off after 30 characters and adds ...
	 * The description set by the client where
	 * {0} will be replaced by the match count.
	 */
	String getShortDescription() {
		String text= getFullDescription();
		int separatorPos= text.indexOf(" - "); //$NON-NLS-1$
		if (separatorPos < 1)
			return text.substring(0, Math.min(50, text.length())) + "..."; // use first 50 characters //$NON-NLS-1$
		if (separatorPos < 30)
			return text;	// don't cut
		if (text.charAt(0) == '"')
			return text.substring(0, Math.min(30, text.length())) + "...\" - " + text.substring(Math.min(separatorPos + 3, text.length())); //$NON-NLS-1$
		return text.substring(0, Math.min(30, text.length())) + "... - " + text.substring(Math.min(separatorPos + 3, text.length())); //$NON-NLS-1$
	}
	/** Image used when search is displayed in a list */
	ImageDescriptor getImageDescriptor() {
		return fImageDescriptor;
	}

	int getItemCount() {
		int count= 0;
		Iterator iter= getResults().iterator();
		while (iter.hasNext())
			count += ((ISearchResultViewEntry)iter.next()).getMatchCount();
		return count;
	}

	List getResults() {
		if (fResults == null)
			return new ArrayList();
		return fResults;
	}

	ILabelProvider getLabelProvider() {
		return fLabelProvider;
	}

	void searchAgain() {
		if (fOperation == null)
			return;
		Shell shell= SearchPlugin.getActiveWorkbenchShell();
		boolean isAutoBuilding= SearchPlugin.setAutoBuilding(false);
		try {
			new ProgressMonitorDialog(shell).run(true, true, fOperation);
		} catch (InvocationTargetException ex) {
			ExceptionHandler.handle(ex, shell, SearchMessages.Search_Error_search_title, SearchMessages.Search_Error_search_message); 
		} catch(InterruptedException e) {
		} finally {
			SearchPlugin.setAutoBuilding(isAutoBuilding);
		}
	}
	
	boolean isSameSearch(Search search) {
		return search != null && search.getOperation() == fOperation && fOperation != null;
	}
	
	void backupMarkers() {
		Iterator iter= getResults().iterator();
		while (iter.hasNext()) {
			((SearchResultViewEntry)iter.next()).backupMarkers();
		}
	}

	String getPageId() {
		return fPageId;
	}
	
	IGroupByKeyComputer getGroupByKeyComputer() {
		return fGroupByKeyComputer;
	}

	public IRunnableWithProgress getOperation() {
		return fOperation;
	}

	IAction getGotoMarkerAction() {
		return fGotoMarkerAction;
	}

	IContextMenuContributor getContextMenuContributor() {
		return fContextMenuContributor;
	}
	
	IActionGroupFactory getActionGroupFactory() {
		return fActionGroupFactory;
	}
	
	public void removeResults() {
		fResults= null;
	}
	
	void setResults(ArrayList results) {
		Assert.isNotNull(results);
		fResults= results;
	}

	ISelection getSelection() {
		return fSelection;
	}

	void setSelection(ISelection selection) {
		fSelection= selection;
	}
}


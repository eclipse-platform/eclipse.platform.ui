/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ILabelProvider;

import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.search.internal.ui.util.ExceptionHandler;
import org.eclipse.search.ui.IContextMenuContributor;
import org.eclipse.search.ui.IGroupByKeyComputer;
import org.eclipse.search.ui.ISearchResultViewEntry;

class Search extends Object {
	private String fPageId;
	private String fDescription;
	private ImageDescriptor fImageDescriptor;
	private ILabelProvider fLabelProvider;
	private ArrayList fResults;
	private IAction fGotoMarkerAction;
	private IContextMenuContributor fContextMenuContributor;
	private IGroupByKeyComputer	fGroupByKeyComputer;
	private IRunnableWithProgress fOperation;


	public Search(String pageId, String description, ILabelProvider labelProvider, ImageDescriptor imageDescriptor, IAction gotoMarkerAction, IContextMenuContributor contextMenuContributor, IGroupByKeyComputer groupByKeyComputer, IRunnableWithProgress operation) {
		fPageId= pageId;
		fDescription= description;
		fImageDescriptor= imageDescriptor;
		fLabelProvider= labelProvider;
		fGotoMarkerAction= gotoMarkerAction;
		fContextMenuContributor= contextMenuContributor;
		fGroupByKeyComputer= groupByKeyComputer;
		fOperation= operation;
	}
	/**
	 * Returns the full description of the search.
	 * The description set by the client where
	 * {0} will be replaced by the match count.
	 */
	String getFullDescription() {
		if (fDescription == null)
			return "";

		String text= fDescription;
		int i= fDescription.lastIndexOf("{0}");
		if (i != -1) {
			// replace "{0}" with the match count
			int count= getItemCount();
			text= fDescription.substring(0, i);
			text += count;
			// cut away last 's' if count is 1
			if (count == 1 && fDescription.lastIndexOf('s') == (fDescription.length() - 1))
				text += fDescription.substring(i + 3, fDescription.length() - 1);
			else
			 	text += fDescription.substring(i + 3);
		}
		return text;
	}
	/**
	 * Returns a short description of the search.
	 * Cuts off after 30 characters and adds ...
	 * The description set by the client where
	 * {0} will be replaced by the match count.
	 */
	String getShortDescription() {
		if (fDescription == null)
			return "";

		String text= fDescription;
		int i= fDescription.lastIndexOf("{0}");
		if (i != -1) {
			// replace "{0}" with the match count
			int count= getItemCount();
			// minimize length infront of " - " to 20 and add ...
			if (i > 20 + 3) {
				if (fDescription.indexOf('"') == 0 && fDescription.indexOf('"', 1) == i - 4)
					text= fDescription.substring(0, 21) + "\"... - ";
				else
					text= fDescription.substring(0, 20) + "... - ";
			}
			else
				text= fDescription.substring(0, i);
			text += count;
			// cut away last 's' if count is 1
			if (count == 1 && fDescription.lastIndexOf('s') == (fDescription.length() - 1))
				text += fDescription.substring(i + 3, fDescription.length() - 1);
			else
			 	text += fDescription.substring(i + 3);
		}
		else {
			// minimize length to 30 and add ...
			if (fDescription.length() > 30)
				text= fDescription.substring(0, 30) + "... ";
		}
		return text;
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
		ResourceBundle bundle= SearchPlugin.getResourceBundle();
		IWorkspaceDescription workspaceDesc= SearchPlugin.getWorkspace().getDescription();
		boolean isAutoBuilding= workspaceDesc.isAutoBuilding();
		if (isAutoBuilding)
			// disable auto-build during search operation
			SearchPlugin.setAutoBuilding(false);
		try {
			new ProgressMonitorDialog(shell).run(true, true, fOperation);
		} catch (InvocationTargetException ex) {
			ExceptionHandler.handle(ex, shell, bundle, "Search.Error.search.");
		} catch(InterruptedException e) {
		} finally {
			if (isAutoBuilding)
				// enable auto-building again
				SearchPlugin.setAutoBuilding(true);
		}
	}
	
	boolean isSameSearch(Search search) {
		return search != null && search.getOperation() == fOperation;
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

	IRunnableWithProgress getOperation() {
		return fOperation;
	}

	IAction getGotoMarkerAction() {
		return fGotoMarkerAction;
	}

	IContextMenuContributor getContextMenuContributor() {
		return fContextMenuContributor;
	}
	
	public void removeResults() {
		fResults= null;
	}
	
	void setResults(ArrayList results) {
		Assert.isNotNull(results);
		fResults= results;
	}
}


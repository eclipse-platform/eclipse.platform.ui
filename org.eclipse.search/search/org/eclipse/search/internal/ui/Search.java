package org.eclipse.search.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000, 2001
 */
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

	/** Description of the search */
	String getDescription() {
		return fDescription;
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
			workspaceDesc.setAutoBuilding(false);
			try {
				SearchPlugin.getWorkspace().setDescription(workspaceDesc);
			}
			catch (CoreException ex) {
				ExceptionHandler.handle(ex, shell, bundle, "Search.Error.setDescription.");
			}				
		try {
			new ProgressMonitorDialog(shell).run(true, true, fOperation);
		} catch (InvocationTargetException ex) {
			ExceptionHandler.handle(ex, shell, bundle, "Search.Error.search.");
		} catch(InterruptedException e) {
		} finally {
			if (isAutoBuilding)
				// enable auto-building again
				workspaceDesc= SearchPlugin.getWorkspace().getDescription();
				workspaceDesc.setAutoBuilding(true);
				try {
					SearchPlugin.getWorkspace().setDescription(workspaceDesc);
				}
				catch (CoreException ex) {
					ExceptionHandler.handle(ex, shell, bundle, "Search.Error.setDescription.");
				}				
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


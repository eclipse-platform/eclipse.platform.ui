/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Michael Fraenkel (fraenkel@us.ibm.com) - contributed a fix for:
 *       o New search view sets incorrect title
 *         (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=60966)
 *******************************************************************************/
package org.eclipse.search.internal.ui;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.Assert;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.CellEditorActionHandler;
import org.eclipse.ui.part.ViewPart;

import org.eclipse.search.ui.IActionGroupFactory;
import org.eclipse.search.ui.IContextMenuContributor;
import org.eclipse.search.ui.IGroupByKeyComputer;
import org.eclipse.search.ui.ISearchResultView;

/**
 * @deprecated old search
 */
public class SearchResultView extends ViewPart implements ISearchResultView {


	private static Map fgLabelProviders= new HashMap(5);

	private SearchResultViewer fViewer;
	private Map fResponse;
	private IMemento fMemento;
	private IPropertyChangeListener fPropertyChangeListener;
	private CellEditorActionHandler fCellEditorActionHandler;
	private SelectAllAction fSelectAllAction;

	/*
	 * Implements method from IViewPart.
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		fMemento= memento;
	}

	/*
	 * Implements method from IViewPart.
	 */
	public void saveState(IMemento memento) {
		if (fViewer == null) {
			// part has not been created
			if (fMemento != null) //Keep the old state;
				memento.putMemento(fMemento);
			return;
		}
		fViewer.saveState(memento);
	}

	/**
	 * Creates the search list inner viewer.
	 * @param parent the parent
	 */
	public void createPartControl(Composite parent) {
		Assert.isTrue(fViewer == null);
		fViewer= new SearchResultViewer(this, parent);
		if (fMemento != null)
			fViewer.restoreState(fMemento);
		fMemento= null;
		SearchManager.getDefault().addSearchChangeListener(fViewer);
		fViewer.init();

		// Add selectAll action handlers.
		fCellEditorActionHandler = new CellEditorActionHandler(getViewSite().getActionBars());
		fSelectAllAction= new SelectAllAction();
		fSelectAllAction.setViewer(fViewer);
		fCellEditorActionHandler.setSelectAllAction(fSelectAllAction);

		fillActionBars(getViewSite().getActionBars());

		fPropertyChangeListener= new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (SearchPreferencePage.POTENTIAL_MATCH_FG_COLOR.equals(event.getProperty()) || SearchPreferencePage.EMPHASIZE_POTENTIAL_MATCHES.equals(event.getProperty()))
					if (fViewer != null)
						fViewer.updatedPotentialMatchFgColor();
			}
		};

		SearchPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(fPropertyChangeListener);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(fViewer.getControl(), SearchPlugin.getDefault().getSearchViewHelpContextId());
	}

	/**
	 * Returns the search result viewer.
	 * @return the search result viewer.
	 */
	public SearchResultViewer getViewer() {
		return fViewer;
	}

	//---- IWorkbenchPart ------------------------------------------------------


	public void setFocus() {
		fViewer.getControl().setFocus();
	}

	public void dispose() {
		if (fViewer != null) {
			SearchManager.getDefault().removeSearchChangeListener(fViewer);
			fViewer= null;
		}
		if (fPropertyChangeListener != null)
			SearchPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(fPropertyChangeListener);
		if (fCellEditorActionHandler != null) {
			fCellEditorActionHandler.dispose();
			fCellEditorActionHandler= null;
		}
		super.dispose();
	}

	protected void setContentDescription(String title) {
		super.setContentDescription(title);
	}

	protected void setTitleToolTip(String text) {
		super.setTitleToolTip(text);
	}

	//---- Adding Action to Toolbar -------------------------------------------

	private void fillActionBars(IActionBars actionBars) {
		IToolBarManager toolBar= actionBars.getToolBarManager();
		fillToolBar(toolBar);
		actionBars.updateActionBars();

		// Add selectAll action handlers.
		actionBars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), fSelectAllAction);
	}

	private void fillToolBar(IToolBarManager tbm) {
		fViewer.fillToolBar(tbm);
	}

	ILabelProvider getLabelProvider(String pageId) {
		if (pageId != null)
			return (ILabelProvider)fgLabelProviders.get(pageId);
		return null;
	}

	public ILabelProvider getLabelProvider() {
		if (fViewer == null)
			return null;
		IBaseLabelProvider labelProvider= fViewer.getLabelProvider();
		if (labelProvider == null)
			return null;

		return ((SearchResultLabelProvider)labelProvider).getLabelProvider();
	}

	private void setGotoMarkerAction(final IAction gotoMarkerAction) {
		// Make sure we are doing it in the right thread.
		getDisplay().syncExec(new Runnable() {
			public void run() {
				getViewer().setGotoMarkerAction(gotoMarkerAction);
			}
		});
	}


	Display getDisplay() {
		return fViewer.getControl().getDisplay();
	}


	//---- ISearchResultView --------------------------------------------------


	/*
	 * Implements method from ISearchResultView
	 */
	public ISelection getSelection() {
		return fViewer.getSelection();
	}

	/*
	 * Implements method from ISearchResultView
	 */
	public void searchStarted(
				IActionGroupFactory		groupFactory,
				String					singularLabel,
				String					pluralLabelPattern,
				ImageDescriptor			imageDescriptor,
				String					pageId,
				ILabelProvider			labelProvider,
				IAction					gotoAction,
				IGroupByKeyComputer		groupByKeyComputer,
				IRunnableWithProgress	operation) {


		Assert.isNotNull(pageId);
		Assert.isNotNull(pluralLabelPattern);
		Assert.isNotNull(gotoAction);

		fResponse= new HashMap(500);
		setGotoMarkerAction(gotoAction);

		ILabelProvider oldLabelProvider= (ILabelProvider)fgLabelProviders.get(pageId);
		if (oldLabelProvider != null)
			oldLabelProvider.dispose();
		fgLabelProviders.put(pageId, labelProvider);

		SearchManager.getDefault().addNewSearch(
			new Search(
				pageId,
				singularLabel,
				pluralLabelPattern,
				null,
				imageDescriptor,
				fViewer.getGotoMarkerAction(),
				groupFactory,
				groupByKeyComputer,
				operation));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchResultView#searchStarted(java.lang.String, java.lang.String, org.eclipse.jface.resource.ImageDescriptor, org.eclipse.search.ui.IContextMenuContributor, org.eclipse.jface.viewers.ILabelProvider, org.eclipse.jface.action.IAction, org.eclipse.search.ui.IGroupByKeyComputer, org.eclipse.jface.operation.IRunnableWithProgress)
	 */
	public void searchStarted(
				String					pageId,
				String					label,
				ImageDescriptor			imageDescriptor,
				IContextMenuContributor contributor,
				ILabelProvider			labelProvider,
				IAction					gotoAction,
				IGroupByKeyComputer		groupByKeyComputer,
				IRunnableWithProgress	operation) {

		searchStarted(pageId, null, label, imageDescriptor, contributor, labelProvider, gotoAction, groupByKeyComputer, operation);
	}

	/*
	 * Implements method from ISearchResultView
	 * @deprecated	As of build > 20020514
	 */
	public void searchStarted(
				String					pageId,
				String					singularLabel,
				String					pluralLabelPattern,
				ImageDescriptor			imageDescriptor,
				IContextMenuContributor contributor,
				ILabelProvider			labelProvider,
				IAction					gotoAction,
				IGroupByKeyComputer		groupByKeyComputer,
				IRunnableWithProgress	operation) {


		Assert.isNotNull(pageId);
		Assert.isNotNull(pluralLabelPattern);
		Assert.isNotNull(gotoAction);

		fResponse= new HashMap(500);
		setGotoMarkerAction(gotoAction);

		ILabelProvider oldLabelProvider= (ILabelProvider)fgLabelProviders.get(pageId);
		if (oldLabelProvider != null)
			oldLabelProvider.dispose();
		fgLabelProviders.put(pageId, labelProvider);

		SearchManager.getDefault().addNewSearch(
			new Search(
				pageId,
				singularLabel,
				pluralLabelPattern,
				null,
				imageDescriptor,
				fViewer.getGotoMarkerAction(),
				contributor,
				groupByKeyComputer,
				operation));
	}

	/*
	 * Implements method from ISearchResultView
	 */
	public void addMatch(String description, Object groupByKey, IResource resource, IMarker marker) {
		SearchResultViewEntry entry= (SearchResultViewEntry)fResponse.get(groupByKey);
		if (entry == null) {
			entry= new SearchResultViewEntry(groupByKey, resource);
			fResponse.put(groupByKey, entry);
		}
		entry.add(marker);
	}


	/*
	 * Implements method from ISearchResultView
	 */
	public void searchFinished() {
		SearchManager.getDefault().searchFinished(new ArrayList(fResponse.values()));
		fResponse= null;
	}
}

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
package org.eclipse.update.internal.ui.views;

import java.util.ArrayList;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.search.*;

/**
 * 
 */

public class SearchResultView
	extends BaseTableView
	implements ISelectionListener {
	private Action showSearchAction;
	private ModelListener modelListener;
	private SearchObject currentSearch;
	private boolean selectionActive=false;
	private static final String KEY_C_FEATURE =
		"SearchResultView.column.feature";
	private static final String KEY_C_PROVIDER =
		"SearchResultView.column.provider";
	private static final String KEY_C_SITE = "SearchResultView.column.site";
	private static final String KEY_C_VERSION =
		"SearchResultView.column.version";
	private static final String KEY_C_SIZE = "SearchResultView.column.size";
	private static final String KEY_UNKNOWN_SIZE =
		"SearchResultView.column.sizeUnknown";
	private static final String KEY_TITLE = "SearchResultView.title";
	private static final String KEY_SHOW_SEARCH_LABEL =
		"SearchResultView.showSearch.label";
	private static final String KEY_SHOW_SEARCH_TOOLTIP =
		"SearchResultView.showSearch.tooltip";

	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content 
	 * (like Task List, for example).
	 */

	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			if (currentSearch == null)
				return new Object[0];
			ArrayList result = new ArrayList();
			Object[] sites = currentSearch.getChildren(currentSearch);
			for (int i = 0; i < sites.length; i++) {
				SearchResultSite site = (SearchResultSite) sites[i];
				Object[] results = site.getChildren(site);
				for (int j = 0; j < results.length; j++) {
					result.add(results[j]);
				}
			}
			return result.toArray();
		}
	}
	class ViewLabelProvider
		extends LabelProvider
		implements ITableLabelProvider {
		public String getColumnText(Object obj, int col) {
			if (obj instanceof IFeatureAdapter) {
				IFeature feature;
				try {
					feature = ((IFeatureAdapter) obj).getFeature(null);
				} catch (CoreException e) {
					if (col == 0)
						return getText(obj);
					return "";
				}
				ISite site = ((IFeatureAdapter) obj).getSite();
				switch (col) {
					case 0 :
						return feature.getLabel();
					case 1 :
						return feature
							.getVersionedIdentifier()
							.getVersion()
							.toString();
					case 2 :
						return feature.getProvider();
					case 3 :
						return site.getURL().toString();
					case 4 :
						long size = feature.getDownloadSize();
						if (size == -1)
							return UpdateUI.getString(
								KEY_UNKNOWN_SIZE);
						else
							return feature.getDownloadSize() + "KB";
				}
			}
			return getText(obj);
		}
		public Image getColumnImage(Object obj, int index) {
			if (index == 0)
				return getImage(obj);
			return null;
		}
		public Image getImage(Object obj) {
			return UpdateUI.getDefault().getLabelProvider().get(
				UpdateUIImages.DESC_FEATURE_OBJ);
		}
	}
	class NameSorter extends ViewerSorter {
	}

	class ModelListener implements IUpdateModelChangedListener {
		public void objectsAdded(Object parent, Object[] children) {
			if (parent instanceof SearchResultSite) {
				getTableViewer().add(children);
				updateTitle();
			}
		}
		public void objectsRemoved(Object parent, Object[] children) {
		}
		public void objectChanged(Object object, String property) {
			if (object instanceof SearchObject) {
				if (SearchObject.P_REFRESH.equals(property)) {
					getViewer().refresh();
					updateTitle();
				}
			}
		}
	}

	/**
	 * The constructor.
	 */
	public SearchResultView() {
		UpdateUI.getDefault().getLabelProvider().connect(this);
		modelListener = new ModelListener();
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void initProviders() {
		TableViewer viewer = getTableViewer();
		createColumns();
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(ResourcesPlugin.getWorkspace());
		WorkbenchHelp.setHelp(
			getControl(),
			"org.eclipse.update.ui.SearchResultView");
	}

	protected void partControlCreated() {
		super.partControlCreated();
		UpdateModel model = UpdateUI.getDefault().getUpdateModel();
		model.addUpdateModelChangedListener(modelListener);
		hookSelectionListener(true);
	}

	private void hookSelectionListener(boolean add) {
		IWorkbenchPage page = UpdateUI.getActivePage();
		if (page != null) {
			if (add)
				page.addSelectionListener(this);
			else
				page.removeSelectionListener(this);
		}
	}

	private void createColumns() {
		Table table = getTableViewer().getTable();
		table.setHeaderVisible(true);

		TableColumn column = new TableColumn(table, SWT.NULL);
		column.setText(UpdateUI.getString(KEY_C_FEATURE));

		column = new TableColumn(table, SWT.NULL);
		column.setText(UpdateUI.getString(KEY_C_VERSION));

		column = new TableColumn(table, SWT.NULL);
		column.setText(UpdateUI.getString(KEY_C_PROVIDER));

		column = new TableColumn(table, SWT.NULL);
		column.setText(UpdateUI.getString(KEY_C_SITE));

		column = new TableColumn(table, SWT.NULL);
		column.setText(UpdateUI.getString(KEY_C_SIZE));

		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(100, 200, true));
		layout.addColumnData(new ColumnWeightData(100, 50, true));
		layout.addColumnData(new ColumnWeightData(100, 100, true));
		layout.addColumnData(new ColumnWeightData(100, 100, true));
		layout.addColumnData(new ColumnWeightData(50, true));
		table.setLayout(layout);
	}

	public void dispose() {
		UpdateUI.getDefault().getLabelProvider().disconnect(this);
		UpdateModel model = UpdateUI.getDefault().getUpdateModel();
		model.removeUpdateModelChangedListener(modelListener);
		hookSelectionListener(false);
		super.dispose();
	}

	protected void fillContextMenu(IMenuManager manager) {
		manager.add(showSearchAction);
		manager.add(new Separator("additions"));
	}

	protected void fillActionBars(IActionBars bars) {
		IToolBarManager manager = bars.getToolBarManager();
		manager.add(showSearchAction);
	}

	protected void makeActions() {
		showSearchAction = new Action() {
			public void run() {
				try {
					IWorkbenchPage page = UpdateUI.getActivePage();
					DetailsView dview =
						(DetailsView) page.showView(
							UpdatePerspective.ID_DETAILS);
					dview.showPage(DetailsView.SEARCH_PAGE, currentSearch);
				} catch (PartInitException e) {
					UpdateUI.logException(e);
				}
			}
		};
		showSearchAction.setText(
			UpdateUI.getString(KEY_SHOW_SEARCH_LABEL));
		showSearchAction.setToolTipText(
			UpdateUI.getString(KEY_SHOW_SEARCH_TOOLTIP));
		showSearchAction.setImageDescriptor(
			UpdateUIImages.DESC_SHOW_SEARCH);
	}

	public void setCurrentSearch(SearchObject currentSearch) {
		this.currentSearch = currentSearch;
		getViewer().setInput(currentSearch);
		updateTitle();
	}

	private void updateTitle() {
		if (currentSearch == null)
			setTitle(getSite().getRegisteredName());
		else {
			int count = getTableViewer().getTable().getItemCount();
			String title =
				UpdateUI.getFormattedMessage(
					KEY_TITLE,
					new String[] {
						getSite().getRegisteredName(),
						getSearchLabel(currentSearch),
						"" + count });
			setTitle(title);
		}
	}

	private String getSearchLabel(SearchObject searchObject) {
		SearchCategoryDescriptor sdesc =
			SearchCategoryRegistryReader.getDefault().getDescriptor(
				searchObject.getCategoryId());
		ISearchCategory category = sdesc.createCategory();
		if (category != null)
			return category.getCurrentSearch();
		else
			return searchObject.getName();
	}

	public void setSelectionActive(boolean active) {
		if (active)
			getSite().setSelectionProvider(getViewer());
		else
			getSite().setSelectionProvider(null);
		updateTitle();
		this.selectionActive = active;
	}
	public boolean isSelectionActive() {
		return selectionActive;
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() == 1) {
				Object obj = ssel.getFirstElement();
				if (obj instanceof SearchObject)
					setCurrentSearch((SearchObject) obj);
			}
		}
	}
}
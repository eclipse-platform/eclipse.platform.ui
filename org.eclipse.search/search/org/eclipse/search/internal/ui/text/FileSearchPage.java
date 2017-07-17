/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Juerg Billeter, juergbi@ethz.ch - 47136 Search view should show match objects
 *     Ulrich Etter, etteru@ethz.ch - 47136 Search view should show match objects
 *     Roman Fuchs, fuchsro@ethz.ch - 47136 Search view should show match objects
 *******************************************************************************/
package org.eclipse.search.internal.ui.text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.views.navigator.NavigatorDragAdapter;

import org.eclipse.search.internal.ui.Messages;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;

import org.eclipse.search2.internal.ui.OpenSearchPreferencesAction;


public class FileSearchPage extends AbstractTextSearchViewPage implements IAdaptable {

	public static class DecoratorIgnoringViewerSorter extends ViewerComparator {
		private final ILabelProvider fLabelProvider;

		public DecoratorIgnoringViewerSorter(ILabelProvider labelProvider) {
			fLabelProvider= labelProvider;
		}

		@Override
		public int category(Object element) {
			if (element instanceof IContainer) {
				return 1;
			}
			return 2;
		}

	    @Override
		public int compare(Viewer viewer, Object e1, Object e2) {
	        int cat1 = category(e1);
	        int cat2 = category(e2);

	        if (cat1 != cat2) {
				return cat1 - cat2;
			}

	        if (e1 instanceof LineElement && e2 instanceof LineElement) {
				LineElement m1= (LineElement) e1;
				LineElement m2= (LineElement) e2;
        		return m1.getOffset() - m2.getOffset();
	        }

	        String name1= fLabelProvider.getText(e1);
	        String name2= fLabelProvider.getText(e2);
	        if (name1 == null)
	            name1 = "";//$NON-NLS-1$
	        if (name2 == null)
	            name2 = "";//$NON-NLS-1$
			int result= getComparator().compare(name1, name2);
			return result;
	    }
	}

	private static final String KEY_SORTING= "org.eclipse.search.resultpage.sorting"; //$NON-NLS-1$
	private static final String KEY_LIMIT= "org.eclipse.search.resultpage.limit"; //$NON-NLS-1$

	private static final int DEFAULT_ELEMENT_LIMIT = 1000;

	private ActionGroup fActionGroup;
	private IFileSearchContentProvider fContentProvider;
	private int fCurrentSortOrder;
	private SortAction fSortByNameAction;
	private SortAction fSortByPathAction;


	private static final String[] SHOW_IN_TARGETS= new String[] { IPageLayout.ID_RES_NAV };
	private  static final IShowInTargetList SHOW_IN_TARGET_LIST= new IShowInTargetList() {
		@Override
		public String[] getShowInTargetIds() {
			return SHOW_IN_TARGETS;
		}
	};

	public FileSearchPage() {
		fSortByNameAction= new SortAction(SearchMessages.FileSearchPage_sort_name_label, this, FileLabelProvider.SHOW_LABEL_PATH);
		fSortByPathAction= new SortAction(SearchMessages.FileSearchPage_sort_path_label, this, FileLabelProvider.SHOW_PATH_LABEL);

		setElementLimit(Integer.valueOf(DEFAULT_ELEMENT_LIMIT));
	}

	@Override
	public void setElementLimit(Integer elementLimit) {
		super.setElementLimit(elementLimit);
		int limit= elementLimit.intValue();
		getSettings().put(KEY_LIMIT, limit);
	}

	@Override
	public StructuredViewer getViewer() {
		return super.getViewer();
	}

	private void addDragAdapters(StructuredViewer viewer) {
		Transfer[] transfers= new Transfer[] { ResourceTransfer.getInstance() };
		int ops= DND.DROP_COPY | DND.DROP_LINK;
		viewer.addDragSupport(ops, transfers, new NavigatorDragAdapter(viewer));
	}

	@Override
	protected void configureTableViewer(TableViewer viewer) {
		viewer.setUseHashlookup(true);
		FileLabelProvider innerLabelProvider= new FileLabelProvider(this, fCurrentSortOrder);
		viewer.setLabelProvider(new DecoratingFileSearchLabelProvider(innerLabelProvider));
		viewer.setContentProvider(new FileTableContentProvider(this));
		viewer.setComparator(new DecoratorIgnoringViewerSorter(innerLabelProvider));
		fContentProvider= (IFileSearchContentProvider) viewer.getContentProvider();
		addDragAdapters(viewer);
	}

	@Override
	protected void configureTreeViewer(TreeViewer viewer) {
		viewer.setUseHashlookup(true);
		FileLabelProvider innerLabelProvider= new FileLabelProvider(this, FileLabelProvider.SHOW_LABEL);
		viewer.setLabelProvider(new DecoratingFileSearchLabelProvider(innerLabelProvider));
		viewer.setContentProvider(new FileTreeContentProvider(this, viewer));
		viewer.setComparator(new DecoratorIgnoringViewerSorter(innerLabelProvider));
		fContentProvider= (IFileSearchContentProvider) viewer.getContentProvider();
		addDragAdapters(viewer);
		viewer.addTreeListener(new ITreeViewerListener() {
			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
			}

			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				autoExpand(viewer, event.getElement());
			}
		});
	}

	/**
	 * Performs an auto-expansion starting at the given element in the viewer.
	 * As long as the object only has one unexpanded child, auto-expand will
	 * expand that child. It stops expanding children once there is actually a
	 * choice for the user to make.
	 *
	 * @param viewer
	 *            the viewer to perform auto-expansion on
	 * @param toExpand
	 *            the viewer entry at which expansion should start
	 */
	private void autoExpand(TreeViewer viewer, Object toExpand) {
		final ITreeContentProvider contentProvider = (ITreeContentProvider) viewer.getContentProvider();
		Display.getCurrent().asyncExec(() -> {
			if (viewer.getControl().isDisposed()) {
				return;
			}
			Object current = toExpand;
			while (current != null) {
				Object childToExpand = null;
				Object[] children = contentProvider.getChildren(current);
				if (children != null && children.length == 1) {
					childToExpand = children[0];
				}
				if (childToExpand != null) {
					if (!viewer.getExpandedState(childToExpand)) {
						viewer.setExpandedState(childToExpand, true);
					}
				}
				current = childToExpand;
			}
		});
	}

	@Override
	protected void showMatch(Match match, int offset, int length, boolean activate) throws PartInitException {
		IFile file= (IFile) match.getElement();
		IWorkbenchPage page= getSite().getPage();
		if (offset >= 0 && length != 0) {
			openAndSelect(page, file, offset, length, activate);
		} else {
			open(page, file, activate);
		}
	}

	@Override
	protected void handleOpen(OpenEvent event) {
		if (showLineMatches()) {
			Object firstElement= ((IStructuredSelection)event.getSelection()).getFirstElement();
			if (firstElement instanceof IFile) {
				if (getDisplayedMatchCount(firstElement) == 0) {
					try {
						open(getSite().getPage(), (IFile)firstElement, false);
					} catch (PartInitException e) {
						ErrorDialog.openError(getSite().getShell(), SearchMessages.FileSearchPage_open_file_dialog_title, SearchMessages.FileSearchPage_open_file_failed, e.getStatus());
					}
					return;
				}
			}
		}
		super.handleOpen(event);
		Object firstElement = ((IStructuredSelection) event.getSelection()).getFirstElement();
		if (firstElement == null) {
			return;
		}
		Viewer viewer = event.getViewer();
		if (viewer instanceof TreeViewer) {
			TreeViewer treeViewer = (TreeViewer) viewer;

			if (treeViewer.getExpandedState(firstElement)) {
				autoExpand(treeViewer, firstElement);
			}
		}
	}

	@Override
	protected void fillContextMenu(IMenuManager mgr) {
		super.fillContextMenu(mgr);
		addSortActions(mgr);
		fActionGroup.setContext(new ActionContext(getSite().getSelectionProvider().getSelection()));
		fActionGroup.fillContextMenu(mgr);
		FileSearchQuery query= (FileSearchQuery) getInput().getQuery();
		if (query.getSearchString().length() > 0) {
			IStructuredSelection selection= (IStructuredSelection) getViewer().getSelection();
			if (!selection.isEmpty()) {
				ReplaceAction replaceSelection= new ReplaceAction(getSite().getShell(), (FileSearchResult)getInput(), selection.toArray());
				replaceSelection.setText(SearchMessages.ReplaceAction_label_selected);
				mgr.appendToGroup(IContextMenuConstants.GROUP_REORGANIZE, replaceSelection);

			}
			ReplaceAction replaceAll= new ReplaceAction(getSite().getShell(), (FileSearchResult)getInput(), null);
			replaceAll.setText(SearchMessages.ReplaceAction_label_all);
			mgr.appendToGroup(IContextMenuConstants.GROUP_REORGANIZE, replaceAll);
		}
	}

	private void addSortActions(IMenuManager mgr) {
		if (getLayout() != FLAG_LAYOUT_FLAT)
			return;
		MenuManager sortMenu= new MenuManager(SearchMessages.FileSearchPage_sort_by_label);
		sortMenu.add(fSortByNameAction);
		sortMenu.add(fSortByPathAction);

		fSortByNameAction.setChecked(fCurrentSortOrder == fSortByNameAction.getSortOrder());
		fSortByPathAction.setChecked(fCurrentSortOrder == fSortByPathAction.getSortOrder());

		mgr.appendToGroup(IContextMenuConstants.GROUP_VIEWER_SETUP, sortMenu);
	}

	@Override
	public void setViewPart(ISearchResultViewPart part) {
		super.setViewPart(part);
		fActionGroup= new NewTextSearchActionGroup(part);
	}

	@Override
	public void init(IPageSite site) {
		super.init(site);
		IMenuManager menuManager = site.getActionBars().getMenuManager();
		menuManager.appendToGroup(IContextMenuConstants.GROUP_PROPERTIES, new OpenSearchPreferencesAction());
	}

	@Override
	public void dispose() {
		fActionGroup.dispose();
		super.dispose();
	}

	@Override
	protected void elementsChanged(Object[] objects) {
		if (fContentProvider != null)
			fContentProvider.elementsChanged(objects);
	}

	@Override
	protected void clear() {
		if (fContentProvider != null)
			fContentProvider.clear();
	}

	public void setSortOrder(int sortOrder) {
		fCurrentSortOrder= sortOrder;
		DecoratingFileSearchLabelProvider lpWrapper= (DecoratingFileSearchLabelProvider)getViewer().getLabelProvider();
		((FileLabelProvider)lpWrapper.getStyledStringProvider()).setOrder(sortOrder);
		getViewer().refresh();
		getSettings().put(KEY_SORTING, fCurrentSortOrder);
	}

	@Override
	public void restoreState(IMemento memento) {
		super.restoreState(memento);
		try {
			fCurrentSortOrder= getSettings().getInt(KEY_SORTING);
		} catch (NumberFormatException e) {
			fCurrentSortOrder= fSortByNameAction.getSortOrder();
		}
		int elementLimit= DEFAULT_ELEMENT_LIMIT;
		try {
			elementLimit= getSettings().getInt(KEY_LIMIT);
		} catch (NumberFormatException e) {
		}
		if (memento != null) {
			Integer value= memento.getInteger(KEY_SORTING);
			if (value != null)
				fCurrentSortOrder= value.intValue();

			value= memento.getInteger(KEY_LIMIT);
			if (value != null)
				elementLimit= value.intValue();
		}
		setElementLimit(Integer.valueOf(elementLimit));
	}
	@Override
	public void saveState(IMemento memento) {
		super.saveState(memento);
		memento.putInteger(KEY_SORTING, fCurrentSortOrder);
		memento.putInteger(KEY_LIMIT, getElementLimit().intValue());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (IShowInTargetList.class.equals(adapter)) {
			return (T) SHOW_IN_TARGET_LIST;
		}

		if (adapter == IShowInSource.class) {
			ISelectionProvider selectionProvider= getSite().getSelectionProvider();
			if (selectionProvider == null)
				return null;

			ISelection selection= selectionProvider.getSelection();
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection structuredSelection= ((StructuredSelection)selection);
				final Set<Object> newSelection= new HashSet<>(structuredSelection.size());
				Iterator<?> iter= structuredSelection.iterator();
				while (iter.hasNext()) {
					Object element= iter.next();
					if (element instanceof LineElement)
						element= ((LineElement)element).getParent();
					newSelection.add(element);
				}

				return (T) new IShowInSource() {
					@Override
					public ShowInContext getShowInContext() {
						return new ShowInContext(null, new StructuredSelection(new ArrayList<>(newSelection)));
					}
				};
			}
			return null;
		}

		return null;
	}

	@Override
	public String getLabel() {
		String label= super.getLabel();
		StructuredViewer viewer= getViewer();
		if (viewer instanceof TableViewer) {
			TableViewer tv= (TableViewer) viewer;

			AbstractTextSearchResult result= getInput();
			if (result != null) {
				int itemCount= ((IStructuredContentProvider) tv.getContentProvider()).getElements(getInput()).length;
				if (showLineMatches()) {
					int matchCount= getInput().getMatchCount();
					if (itemCount < matchCount) {
						return Messages.format(SearchMessages.FileSearchPage_limited_format_matches, new Object[]{label, Integer.valueOf(itemCount), Integer.valueOf(matchCount)});
					}
				} else {
					int fileCount= getInput().getElements().length;
					if (itemCount < fileCount) {
						return Messages.format(SearchMessages.FileSearchPage_limited_format_files, new Object[]{label, Integer.valueOf(itemCount), Integer.valueOf(fileCount)});
					}
				}
			}
		}
		return label;
	}

	@Override
	public int getDisplayedMatchCount(Object element) {
		if (showLineMatches()) {
			if (element instanceof LineElement) {
				LineElement lineEntry= (LineElement) element;
				return lineEntry.getNumberOfMatches(getInput());
			}
			return 0;
		}
		return super.getDisplayedMatchCount(element);
	}

	@Override
	public Match[] getDisplayedMatches(Object element) {
		if (showLineMatches()) {
			if (element instanceof LineElement) {
				LineElement lineEntry= (LineElement) element;
				return lineEntry.getMatches(getInput());
			}
			return new Match[0];
		}
		return super.getDisplayedMatches(element);
	}

	@Override
	protected void evaluateChangedElements(Match[] matches, Set<Object> changedElements) {
		if (showLineMatches()) {
			for (Match match : matches) {
				changedElements.add(((FileMatch) match).getLineElement());
			}
		} else {
			super.evaluateChangedElements(matches, changedElements);
		}
	}

	private boolean showLineMatches() {
		AbstractTextSearchResult input= getInput();
		return getLayout() == FLAG_LAYOUT_TREE && input != null && !((FileSearchQuery) input.getQuery()).isFileNameSearch();
	}

}

/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui.text;

import java.text.Collator;
import java.text.MessageFormat;
import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.DelegatingDragAdapter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;

import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.SearchPreferencePage;


public class FileSearchPage extends AbstractTextSearchViewPage implements IAdaptable {
	
	public static class DecoratorIgnoringViewerSorter extends ViewerSorter {
		private final ILabelProvider fLabelProvider;

		public DecoratorIgnoringViewerSorter(ILabelProvider labelProvider) {
			super(null); // lazy initialization
			fLabelProvider= labelProvider;
		}
		
	    public int compare(Viewer viewer, Object e1, Object e2) {
	        String name1= fLabelProvider.getText(e1);
	        String name2= fLabelProvider.getText(e2);
	        if (name1 == null)
	            name1 = "";//$NON-NLS-1$
	        if (name2 == null)
	            name2 = "";//$NON-NLS-1$
	        return getCollator().compare(name1, name2);
	    }
	    
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ViewerSorter#getCollator()
		 */
		public final Collator getCollator() {
			if (collator == null) {
				collator= Collator.getInstance();
			}
			return collator;
		}
	}
	
	private static final String KEY_SORTING= "org.eclipse.search.resultpage.sorting"; //$NON-NLS-1$

	private ActionGroup fActionGroup;
	private IFileSearchContentProvider fContentProvider;
	private int fCurrentSortOrder;
	private SortAction fSortByNameAction;
	private SortAction fSortByPathAction;
	
	private EditorOpener fEditorOpener= new EditorOpener();

		
	private static final String[] SHOW_IN_TARGETS= new String[] { IPageLayout.ID_RES_NAV };
	private  static final IShowInTargetList SHOW_IN_TARGET_LIST= new IShowInTargetList() {
		public String[] getShowInTargetIds() {
			return SHOW_IN_TARGETS;
		}
	};

	private IPropertyChangeListener fPropertyChangeListener;
	public FileSearchPage() {
		fSortByNameAction= new SortAction(SearchMessages.FileSearchPage_sort_name_label, this, FileLabelProvider.SHOW_LABEL_PATH); 
		fSortByPathAction= new SortAction(SearchMessages.FileSearchPage_sort_path_label, this, FileLabelProvider.SHOW_PATH_LABEL); 

		fPropertyChangeListener= new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (SearchPreferencePage.LIMIT_TABLE.equals(event.getProperty()) || SearchPreferencePage.LIMIT_TABLE_TO.equals(event.getProperty()))
					if (getViewer() instanceof TableViewer) {
						getViewPart().updateLabel();
						getViewer().refresh();
					}
			}
		};
		SearchPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(fPropertyChangeListener);
		
}
	
	public StructuredViewer getViewer() {
		return super.getViewer();
	}
	
	private void addDragAdapters(StructuredViewer viewer) {
		Transfer[] transfers= new Transfer[] { ResourceTransfer.getInstance() };
		int ops= DND.DROP_COPY | DND.DROP_LINK;
		
		DelegatingDragAdapter adapter= new DelegatingDragAdapter();
		adapter.addDragSourceListener(new ResourceTransferDragAdapter(viewer));
		
		viewer.addDragSupport(ops, transfers, adapter);
	}	

	protected void configureTableViewer(TableViewer viewer) {
		viewer.setUseHashlookup(true);
		FileLabelProvider innerLabelProvider= new FileLabelProvider(this, fCurrentSortOrder);
		viewer.setLabelProvider(new DecoratingLabelProvider(innerLabelProvider, PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator()));
		viewer.setContentProvider(new FileTableContentProvider(this));
		viewer.setSorter(new DecoratorIgnoringViewerSorter(innerLabelProvider));
		fContentProvider= (IFileSearchContentProvider) viewer.getContentProvider();
		addDragAdapters(viewer);
	}

	protected void configureTreeViewer(TreeViewer viewer) {
		viewer.setUseHashlookup(true);
		FileLabelProvider innerLabelProvider= new FileLabelProvider(this, FileLabelProvider.SHOW_LABEL);
		viewer.setLabelProvider(new DecoratingLabelProvider(innerLabelProvider, PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator()));
		viewer.setContentProvider(new FileTreeContentProvider(viewer));
		viewer.setSorter(new DecoratorIgnoringViewerSorter(innerLabelProvider));
		fContentProvider= (IFileSearchContentProvider) viewer.getContentProvider();
		addDragAdapters(viewer);
	}

	protected void showMatch(Match match, int offset, int length, boolean activate) throws PartInitException {
		IFile file= (IFile) match.getElement();
		IEditorPart editor= fEditorOpener.open(match, activate);
		if (offset != 0 && length != 0) {
			if (editor instanceof ITextEditor) {
				ITextEditor textEditor= (ITextEditor) editor;
				textEditor.selectAndReveal(offset, length);
			} else if (editor != null) {
				showWithMarker(editor, file, offset, length);
			}
		}
	}
	
	private void showWithMarker(IEditorPart editor, IFile file, int offset, int length) throws PartInitException {
		IMarker marker= null;
		try {
			marker= file.createMarker(NewSearchUI.SEARCH_MARKER);
			HashMap attributes= new HashMap(4);
			attributes.put(IMarker.CHAR_START, new Integer(offset));
			attributes.put(IMarker.CHAR_END, new Integer(offset + length));
			marker.setAttributes(attributes);
			IDE.gotoMarker(editor, marker);
		} catch (CoreException e) {
			throw new PartInitException(SearchMessages.FileSearchPage_error_marker, e); 
		} finally {
			if (marker != null)
				try {
					marker.delete();
				} catch (CoreException e) {
					// ignore
				}
		}
	}

	protected void fillContextMenu(IMenuManager mgr) {
		super.fillContextMenu(mgr);
		addSortActions(mgr);
		fActionGroup.setContext(new ActionContext(getSite().getSelectionProvider().getSelection()));
		fActionGroup.fillContextMenu(mgr);
		FileSearchQuery query= (FileSearchQuery) getInput().getQuery();
		if (!"".equals(query.getSearchString())) { //$NON-NLS-1$
		ReplaceAction2 replaceAction= new ReplaceAction2(this, (IStructuredSelection) getViewer().getSelection());
		if (replaceAction.isEnabled())
			mgr.appendToGroup(IContextMenuConstants.GROUP_REORGANIZE, replaceAction);
				
		ReplaceAction2 replaceAll= new ReplaceAction2(this);
		if (replaceAll.isEnabled())
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

	public void setViewPart(ISearchResultViewPart part) {
		super.setViewPart(part);
		fActionGroup= new NewTextSearchActionGroup(part);
	}
	
	public void dispose() {
		fActionGroup.dispose();
		SearchPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(fPropertyChangeListener);
		super.dispose();
	}

	protected void elementsChanged(Object[] objects) {
		if (fContentProvider != null)
			fContentProvider.elementsChanged(objects);
	}

	protected void clear() {
		if (fContentProvider != null)
			fContentProvider.clear();
	}

	public void setSortOrder(int sortOrder) {
		fCurrentSortOrder= sortOrder;
		DecoratingLabelProvider lpWrapper= (DecoratingLabelProvider) getViewer().getLabelProvider();
		((FileLabelProvider) lpWrapper.getLabelProvider()).setOrder(sortOrder);
		getViewer().refresh();
		getSettings().put(KEY_SORTING, fCurrentSortOrder);
	}
	
	public void restoreState(IMemento memento) {
		super.restoreState(memento);
		try {
			fCurrentSortOrder= getSettings().getInt(KEY_SORTING);
		} catch (NumberFormatException e) {
			fCurrentSortOrder= fSortByNameAction.getSortOrder();
		}
		if (memento != null) {
			Integer value= memento.getInteger(KEY_SORTING);
			if (value != null)
				fCurrentSortOrder= value.intValue();
		}
	}
	public void saveState(IMemento memento) {
		super.saveState(memento);
		memento.putInteger(KEY_SORTING, fCurrentSortOrder);
	}
	
	public Object getAdapter(Class adapter) {
		if (IShowInTargetList.class.equals(adapter)) {
			return SHOW_IN_TARGET_LIST;
		}
		return null;
	}
	
	public String getLabel() {
		String label= super.getLabel();
		StructuredViewer viewer= getViewer();
		if (viewer instanceof TableViewer) {
			TableViewer tv= (TableViewer) viewer;

			AbstractTextSearchResult result= getInput();
			if (result != null) {
				int itemCount= ((IStructuredContentProvider) tv.getContentProvider()).getElements(getInput()).length;
				int fileCount= getInput().getElements().length;
				if (itemCount < fileCount) {
					String format= SearchMessages.FileSearchPage_limited_format; 
					return MessageFormat.format(format, new Object[]{label, new Integer(itemCount), new Integer(fileCount)});
				}
			}
		}
		return label;
	}

}

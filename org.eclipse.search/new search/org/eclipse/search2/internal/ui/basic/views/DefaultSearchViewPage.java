package org.eclipse.search2.internal.ui.basic.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import org.eclipse.jface.text.Position;

import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.part.Page;

import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultPage;
import org.eclipse.search.ui.ISearchResultPageContainer;
import org.eclipse.search.ui.ISearchResultPresentation;
import org.eclipse.search.ui.text.ISearchElementPresentation;
import org.eclipse.search.ui.text.ITextSearchResult;
import org.eclipse.search.ui.text.Match;

import org.eclipse.search.internal.ui.CopyToClipboardAction;

import org.eclipse.search2.internal.ui.SearchMessages;
import org.eclipse.search2.internal.ui.InternalSearchUI;
import org.eclipse.search2.internal.ui.text.AnnotationManager;

public class DefaultSearchViewPage extends Page implements ISearchResultPage {
	private StructuredViewer fViewer;
	private Composite fViewerContainer;
	private ISearchResultPageContainer fViewPart;
	private AnnotationManager fAnnotationManager;
	
	private MenuManager fMenu;
	
	// Actions
	private Action fCopyToClipboardAction;
	private Action fRemoveResultsAction;
	private Action fRemoveAllResultsAction;
	private SortDropDownActon fSortDropDownAction;
	private Action fShowNextAction;
	private Action fShowPreviousAction;
	private ToggleModeAction fToggleModeAction;
	
	private List fSortOrder;
	private int fSortDirection= SORT_NOT;
	private ISearchElementPresentation fElementPresentation;
	private ISearchResultPresentation fSearchPresentation;
	private int fCurrentMatchIndex= 0;
	
	private SearchResultModel fModel;

	static final int SORT_NOT= 0;
	static final int SORT_ASCENDING= 1;
	static final int SORT_DESCENDING= 2;

	class SearchResultsSorter extends ViewerSorter {

		public int compare(Viewer viewer, Object elementOne, Object elementTwo) {
			if (fSortDirection == SORT_ASCENDING)
				return internalCompare(elementOne, elementTwo);
			else if (fSortDirection == SORT_DESCENDING)
				return -internalCompare(elementOne, elementTwo);
			return 0;
		}

		private int internalCompare(Object left, Object right) {
			String[] sortOrder= getSortOrder();
			ISearchElementPresentation adapter= getSearchResultCategoryAdapter();
			for (int i= 0; i < sortOrder.length; i++) {
				String leftAttribute= getAttribute(left, sortOrder[i], adapter);
				String rightAttribute= getAttribute(right, sortOrder[i], adapter);
				return leftAttribute.compareToIgnoreCase(rightAttribute);
			}
			return 0;
		}

		private String getAttribute(Object element, String attribute, ISearchElementPresentation adaper) {
			return adaper.getAttribute(element, attribute);
		}
	}

	/**
	 * The constructor.
	 */
	public DefaultSearchViewPage() {
		fRemoveAllResultsAction= new RemoveAllResultsAction(this);
		fRemoveResultsAction= new RemoveMatchAction(this);
		fSortDropDownAction= new SortDropDownActon(this);
		fShowNextAction= new ShowNextResultAction(this);
		fShowPreviousAction= new ShowPreviousResultAction(this);
		fToggleModeAction= new ToggleModeAction(this);
		fSortOrder= new ArrayList();
		fAnnotationManager= new AnnotationManager();
	}
		
	
	/**
	 * Pop-up menu: name of group for additional actions (value <code>"group.additions"</code>).
	 */	
	public static final String GROUP_ADDITIONS= "additions"; //$NON-NLS-1$
	
	public static void createStandardGroups(IMenuManager menu) {
		if (!menu.isEmpty())
			return;
		menu.add(new Separator(IContextMenuConstants.GROUP_NEW));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_GOTO));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_OPEN));
		menu.add(new Separator(IContextMenuConstants.GROUP_SHOW));
		menu.add(new Separator(IContextMenuConstants.GROUP_BUILD));
		menu.add(new Separator(IContextMenuConstants.GROUP_REORGANIZE));
		menu.add(new Separator(IContextMenuConstants.GROUP_REMOVE_MATCHES));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_GENERATE));
		menu.add(new Separator(IContextMenuConstants.GROUP_SEARCH));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(new Separator(IContextMenuConstants.GROUP_VIEWER_SETUP));
		menu.add(new Separator(IContextMenuConstants.GROUP_PROPERTIES));
	}
	

	/**
	 * This is a callback that will allow us to create the viewer and
	 * initialize it.
	 */
	public void createControl(Composite parent) {
		fMenu= new MenuManager("#PopUp"); //$NON-NLS-1$

		fMenu.setRemoveAllWhenShown(true);
		fMenu.setParent(getSite().getActionBars().getMenuManager());
		fMenu.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				createStandardGroups(mgr);
				fillContextMenu(mgr);
				fViewPart.fillContextMenu(mgr);
			}

			private void fillContextMenu(IMenuManager mgr) {
				ActionGroup group= fElementPresentation.getActionGroup();
				group.setContext(new ActionContext(fViewer.getSelection()));
				group.fillContextMenu(mgr);
				group.setContext(null);
				mgr.appendToGroup(IContextMenuConstants.GROUP_ADDITIONS, fCopyToClipboardAction);
				mgr.appendToGroup(IContextMenuConstants.GROUP_SHOW, fShowNextAction);
				mgr.appendToGroup(IContextMenuConstants.GROUP_SHOW, fShowPreviousAction);
				mgr.appendToGroup(IContextMenuConstants.GROUP_REMOVE_MATCHES, fRemoveResultsAction);
				mgr.appendToGroup(IContextMenuConstants.GROUP_REMOVE_MATCHES, fRemoveAllResultsAction);
			
				MenuManager sortMenu= new MenuManager(fSortDropDownAction.getText());
				sortMenu.setVisible(true);
				fSortDropDownAction.fillMenu(sortMenu);
				mgr.appendToGroup(IContextMenuConstants.GROUP_VIEWER_SETUP, sortMenu);
				mgr.appendToGroup(IContextMenuConstants.GROUP_VIEWER_SETUP, fToggleModeAction);
			}
		});
		
	
		
		fViewerContainer= new Composite(parent, SWT.NULL);
		fViewerContainer.setSize(100, 100);
		fViewerContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		fViewerContainer.setLayout(new FillLayout());
		createViewer(fViewerContainer, false);
	}
	
	void toggleMode() {
		ISelection selection= fViewer.getSelection();
		boolean newMode= !isFlatMode();
		ISearchResult result= disconnectViewer();
		fViewer.getControl().dispose();
		fViewer= null;
		createViewer(fViewerContainer, newMode);
		fViewerContainer.layout(true);
		initializeSortOrder();
		connectViewer(result);
		fViewer.setSelection(selection, true);
	}

	boolean isFlatMode() {
		return fViewer instanceof TableViewer;
	}


	private void createViewer(Composite parent, boolean flatMode) {
		if (flatMode) {
			fViewer= new SearchResultsTableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		} else { 
			fViewer= new SearchResultsTreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		}
		fViewer.setContentProvider(new SearchResultTreeContentProvider(this));
		fViewer.setLabelProvider(new DelegatingLabelProvider(this));
		fViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				if (!showCurrentMatch())
					gotoNextMatch();
			}
		});
		
		fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				fCurrentMatchIndex= 0;
			}
		});
		
		Menu menu= fMenu.createContextMenu(fViewer.getControl());
		fViewer.getControl().setMenu(menu);

		getSite().setSelectionProvider(fViewer);
		// Register menu
		getSite().registerContextMenu(fViewPart.getViewSite().getId(), fMenu, fViewer);
	}


	public void setFocus() {
		Control control= fViewer.getControl();
		if (control != null && !control.isDisposed())
			control.setFocus();
	}

	public Control getControl() {
		return fViewerContainer;
	}

	public void setInput(ISearchResult search, Object viewState) {
		disconnectViewer();
		fAnnotationManager.setSearchResult(null);
		if (search != null) {
			setElementPresentation(((ITextSearchResult)search));	
			setSearchResultPresentation(((ITextSearchResult)search));
			connectViewer(search);
			
			fAnnotationManager.setSearchResult((ITextSearchResult) search);
			
			if (viewState instanceof ISelection)
				fViewer.setSelection((ISelection) viewState, true);
			else
				gotoNextMatch();
		}
	}
	
	private void connectViewer(ISearchResult search) {
		fCopyToClipboardAction= new CopyToClipboardAction(fViewer);
		createModel(search);
		fViewer.setInput(fModel);
		setSortOrder();
	}


	private ITextSearchResult disconnectViewer() {
		if (fModel != null) {
			ITextSearchResult result= fModel.getResult();
			fModel.dispose();
			fModel= null;
			return result;
		}
		return null;
	}


	private void createModel(ISearchResult search) {
		if (fViewer instanceof TreeViewer)
			fModel= new SearchResultTreeModel(this, (ITextSearchResult)search);
		else
			fModel= new SearchResultTableModel(this, (ITextSearchResult)search);
	}


	private void showMatch(final Match match) {
		final ISearchElementPresentation adapter= getSearchResultCategoryAdapter();
		ISafeRunnable runnable= new ISafeRunnable() {
			public void handleException(Throwable exception) {
				if (exception instanceof PartInitException) {
					PartInitException pie= (PartInitException) exception;
					ErrorDialog.openError(getSite().getShell(), SearchMessages.getString("DefaultSearchViewPage.show_match"), SearchMessages.getString("DefaultSearchViewPage.error.no_editor"), pie.getStatus());  //$NON-NLS-1$ //$NON-NLS-2$
				}
			}

			public void run() throws PartInitException {
				Position currentPosition= InternalSearchUI.getInstance().getPositionTracker().getCurrentPosition(match);
				if (currentPosition != null) {
					adapter.showMatch(match.getElement(), currentPosition.getOffset(), currentPosition.getLength());
				} else {
					adapter.showMatch(match.getElement(), match.getOffset(), match.getLength());
				}
			}
		};
		Platform.run(runnable);
	}

	ITextSearchResult getCurrentSearch() {
		if (fModel != null)
			return fModel.getResult();
		return null;
	}

	ISearchElementPresentation getSearchResultCategoryAdapter() {
		return fElementPresentation;
	}

	String[] getSortOrder() {
		String[] strings= new String[fSortOrder.size()];
		return (String[]) fSortOrder.toArray(strings);
	}

	void setSortAttribute(String attribute) {
		fSortOrder.remove(attribute);
		fSortOrder.add(0, attribute);
		setSortOrder();
	}

	void cycleSortAttribute() {
		if (fSortOrder.size() > 1)
			fSortOrder.add(fSortOrder.remove(0));
		setSortOrder();
	}

	private void setSortOrder() {
		ITextSearchResult currentSearch= getCurrentSearch();
		if (currentSearch != null) {
			String[] sortOrder= getSortOrder();
				ISearchElementPresentation adapter= getSearchResultCategoryAdapter();
				if (adapter != null) {
					adapter.setSortOrder(sortOrder, isFlatMode());
				}
		}
		fViewer.refresh();
	}


	void gotoNextMatch() {
		if (fModel.getResult().getMatchCount() < 1)
			return;
		fCurrentMatchIndex++;
		Match nextMatch= getCurrentMatch();
		while (nextMatch == null) {
			navigateNext(true);
			fCurrentMatchIndex= 0;
			nextMatch= getCurrentMatch();
		}
		showCurrentMatch();
	}

	void gotoPreviousMatch() {
		if (fModel.getResult().getMatchCount() < 2)
			return;
		fCurrentMatchIndex--;
		Match nextMatch= getCurrentMatch();
		while (nextMatch == null) {
			navigateNext(false);
			fCurrentMatchIndex= fModel.getResult().getMatchCount(getFirstSelectedElement())-1;
			nextMatch= getCurrentMatch();
		}
		showCurrentMatch();
	}
	
	void navigateNext(boolean forward) {
		if (fViewer instanceof INavigate) {
			((INavigate)fViewer).navigateNext(forward);
		}
	}
	
	public boolean showCurrentMatch() {
		Match currentMatch= getCurrentMatch();
		if (currentMatch != null) {
			showMatch(currentMatch);
			return true;
		} else {
			return false;
		}
	}
	
	Match getCurrentMatch() {
		Object element= getFirstSelectedElement();
		if (element != null) {
			Match[] matches= fModel.getResult().getMatches(element);
			if (fCurrentMatchIndex >= 0 && fCurrentMatchIndex < matches.length)
				return matches[fCurrentMatchIndex];
		}
		return null;
	}
	
	private Object getFirstSelectedElement() {
		IStructuredSelection selection= (IStructuredSelection) fViewer.getSelection();
		if (selection.size() > 0)
			return selection.getFirstElement();
		return null;
	}
	
	public void dispose() {
		disconnectViewer();
		if (fElementPresentation != null)
			fElementPresentation.dispose();
	
		super.dispose();
	}

	int getSortDirection() {
		return fSortDirection;
	}

	void setSortDirection(int direction) {
		if (direction == fSortDirection)
			return;
		fSortDirection= direction;
		if (direction == SORT_NOT) {
			fViewer.setSorter(null);
			fViewer.refresh();
		} else {
			setSortOrder();
			fViewer.setSorter(new SearchResultsSorter());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.Page#makeContributions(org.eclipse.jface.action.IMenuManager, org.eclipse.jface.action.IToolBarManager, org.eclipse.jface.action.IStatusLineManager)
	 */
	public void makeContributions(
		IMenuManager menuManager,
		IToolBarManager toolBarManager,
		IStatusLineManager statusLineManager) {
		super.makeContributions(menuManager, toolBarManager, statusLineManager);
		
		IToolBarManager tbm= toolBarManager;
		tbm.appendToGroup("ViewSpecificGroup", fShowNextAction); //$NON-NLS-1$
		tbm.appendToGroup("ViewSpecificGroup", fShowPreviousAction); //$NON-NLS-1$
		tbm.appendToGroup("ViewSpecificGroup", fRemoveResultsAction); //$NON-NLS-1$
		tbm.appendToGroup("ViewSpecificGroup", fRemoveAllResultsAction); //$NON-NLS-1$
		tbm.appendToGroup("ViewSpecificGroup", fSortDropDownAction); //$NON-NLS-1$
	}


	private void setElementPresentation(ITextSearchResult result) {
		if (fElementPresentation != null)
			fElementPresentation.dispose();
		fElementPresentation= result.createElementPresentation(fViewPart);
		initializeSortOrder();
	}
	
	private void setSearchResultPresentation(ITextSearchResult result) {
		if (fSearchPresentation != null)
			fSearchPresentation.dispose();
		fSearchPresentation= result.createPresentation(fViewPart);
		initializeSortOrder();
	}
	

	private void initializeSortOrder() {
		fSortOrder.clear();
		String[] attributes= getSearchResultCategoryAdapter().getSortingAtributes(isFlatMode());
		for (int i= 0; i < attributes.length; i++) {
			if (!fSortOrder.contains(attributes[i]))
				fSortOrder.add(attributes[i]);
		}
	}


	/**
	 * @return Returns the model.
	 */
	SearchResultModel getModel() {
		return fModel;
	}


	void removeCurrentMatch() {
		Match match= getCurrentMatch();
		if (match != null)
			fModel.getResult().removeMatch(match);
	}

	// viewer update methods
	void handleInsert(final Object parent, final Object child) {
		((TreeViewer)fViewer).add(parent, child);
	}
	
	void handleRemove(final Object child) {
		if (fViewer instanceof TreeViewer) {
			((TreeViewer)fViewer).remove(child);		
		} else if (fViewer instanceof TableViewer)
			((TableViewer)fViewer).remove(child);		
	}

	void handleUpdate(final Object child) {
		fViewer.update(child, null);
	}

	public void refresh() {
		fViewer.refresh();
	}


	public void handleInsert(Object child) {
		if (fViewer instanceof TableViewer) {
			((TableViewer)fViewer).add(child);
		}
	}


	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchResultsPage#setViewPart(org.eclipse.ui.IViewPart)
	 */
	public void setViewPart(ISearchResultPageContainer part) {
		fViewPart= part;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.search2.ui.ISearchResultsPage#getUIState()
	 */
	public Object getUIState() {
		return fViewer.getSelection();
	}
	
}
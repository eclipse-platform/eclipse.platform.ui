/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.ui.text;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.internal.ui.CopyToClipboardAction;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.SearchPluginImages;
import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.search.ui.IQueryListener;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.ISearchResultPage;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.search2.internal.ui.InternalSearchUI;
import org.eclipse.search2.internal.ui.SearchMessages;
import org.eclipse.search2.internal.ui.basic.views.INavigate;
import org.eclipse.search2.internal.ui.basic.views.RemoveAllResultsAction;
import org.eclipse.search2.internal.ui.basic.views.RemoveMatchAction;
import org.eclipse.search2.internal.ui.basic.views.SearchResultsTableViewer;
import org.eclipse.search2.internal.ui.basic.views.SearchResultsTreeViewer;
import org.eclipse.search2.internal.ui.basic.views.SetLayoutAction;
import org.eclipse.search2.internal.ui.basic.views.ShowNextResultAction;
import org.eclipse.search2.internal.ui.basic.views.ShowPreviousResultAction;
import org.eclipse.search2.internal.ui.text.AnnotationManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.PageBook;
/**
 * An abstract superclass for classes showing
 * <code>AbstractTextSearchResult</code> instances. This class assumes that
 * the input element (@see AbstractTextSearchViewPage#setInput(ISearchResult, Object)) is a subclass of
 * <code>AbstractTextSearchResult</code>. This results page supports a tree
 * and/or a table presentation of search results. Subclasses can determine which
 * presentations they want to support at construction time. Subclasses must
 * customize the viewers for each presentation with a label provider and a
 * content provider. <br>
 * @see #configureTableViewer(TableViewer)
 * @see #configureTreeViewer(TreeViewer))
 * 
 * Changes in the search result are handled by updating the viewer in the
 * <code>elementsChanged()</code> and <code>clear()</code> methods.
 */
public abstract class AbstractTextSearchViewPage extends Page implements ISearchResultPage {
	private static final String KEY_LAYOUT = "org.eclipse.search.resultpage.layout"; //$NON-NLS-1$
	private StructuredViewer fViewer;
	private Composite fViewerContainer;
	private Control fBusyLabel;
	private PageBook fPagebook;
	private boolean fIsBusyShown;
	private ISearchResultViewPart fViewPart;
	private Set fBatchedUpdates;
	private ISearchResultListener fListener;
	private IQueryListener fQueryListener;
	private MenuManager fMenu;
	// Actions
	private Action fCopyToClipboardAction;
	private Action fRemoveResultsAction;
	private Action fRemoveAllResultsAction;
	private Action fShowNextAction;
	private Action fShowPreviousAction;
	private SetLayoutAction fFlatAction;
	private SetLayoutAction fHierarchicalAction;
	private int fCurrentLayout;
	private int fCurrentMatchIndex = 0;
	private String fId;
	private int fSupportedLayouts;
	/**
	 * Flag denoting tree layout.
	 */
	public static final int FLAG_LAYOUT_FLAT = 1;
	/**
	 * Flag denoting flat list layout.
	 */
	public static final int FLAG_LAYOUT_TREE = 2;
	/**
	 * This constructor must be passed a combination of layout flags combined
	 * with bitwise or. At least one flag musst be passed in (i.e. 0 is not a
	 * permitted value).
	 * 
	 * @param supportedLayouts
	 *            Flags determining which layout options this page supports.
	 *            Must not be 0.
	 * @see #FLAG_LAYOUT_FLAT
	 * @see #FLAG_LAYOUT_TREE
	 */
	protected AbstractTextSearchViewPage(int supportedLayouts) {
		fSupportedLayouts = supportedLayouts;
		initLayout();
		fRemoveAllResultsAction = new RemoveAllResultsAction(this);
		fRemoveResultsAction = new RemoveMatchAction(this);
		fShowNextAction = new ShowNextResultAction(this);
		fShowPreviousAction = new ShowPreviousResultAction(this);
		createLayoutActions();
		fBatchedUpdates = new HashSet();
		fListener = new ISearchResultListener() {
			public void searchResultChanged(SearchResultEvent e) {
				handleSearchResultsChanged(e);
			}
		};
	}
	/**
	 *  
	 */
	private void initLayout() {
		if (supportsFlatLayout())
			fCurrentLayout = FLAG_LAYOUT_FLAT;
		else
			fCurrentLayout = FLAG_LAYOUT_TREE;
	}
	/**
	 * Constructs this page with the default layout flags.
	 * 
	 * @see #AbstractTextSearchViewPage(int)
	 */
	protected AbstractTextSearchViewPage() {
		this(FLAG_LAYOUT_FLAT | FLAG_LAYOUT_TREE);
	}
	private void createLayoutActions() {
		if (countBits(fSupportedLayouts) > 1) {
			fFlatAction = new SetLayoutAction(
					this,
					SearchMessages
							.getString("AbstractTextSearchViewPage.flat_layout.label"),
					SearchMessages
							.getString("AbstractTextSearchViewPage.flat_layout.tooltip"),
					FLAG_LAYOUT_FLAT); //$NON-NLS-1$ //$NON-NLS-2$
			fHierarchicalAction = new SetLayoutAction(
					this,
					SearchMessages
							.getString("AbstractTextSearchViewPage.hierarchical_layout.label"),
					SearchMessages
							.getString("AbstractTextSearchViewPage.hierarchical_layout.tooltip"),
					FLAG_LAYOUT_TREE); //$NON-NLS-1$ //$NON-NLS-2$
			SearchPluginImages.setImageDescriptors(fFlatAction,
					SearchPluginImages.T_LCL,
					SearchPluginImages.IMG_LCL_SEARCH_FLAT_LAYOUT);
			SearchPluginImages.setImageDescriptors(fHierarchicalAction,
					SearchPluginImages.T_LCL,
					SearchPluginImages.IMG_LCL_SEARCH_HIERARCHICAL_LAYOUT);
		}
	}
	private int countBits(int layoutFlags) {
		int bitCount = 0;
		for (int i = 0; i < 32; i++) {
			if (layoutFlags % 2 == 1)
				bitCount++;
			layoutFlags >>= 1;
		}
		return bitCount;
	}
	private boolean supportsFlatLayout() {
		return isLayoutSupported(FLAG_LAYOUT_FLAT);
	}
	/**
	 * Gets a dialog settings object for this search result page. There will be
	 * one dialog settings object per search result page id.
	 * 
	 * @see #getID()
	 * @return The dialog settings for this search result page.
	 */
	protected IDialogSettings getSettings() {
		IDialogSettings parent = SearchPlugin.getDefault().getDialogSettings();
		IDialogSettings settings = parent.getSection(getID());
		if (settings == null)
			settings = parent.addNewSection(getID());
		return settings;
	}
	/**
	 * {@inheritDoc}
	 */
	public void setID(String id) {
		fId = id;
	}
	/**
	 * {@inheritDoc}
	 */
	public String getID() {
		return fId;
	}
	/**
	 * Opens an editor on the given element and selects the given range of text.
	 * The location of matches are automatically updated when a file is editor
	 * through the file buffer infrastructure (@see
	 * org.eclipse.core.filebuffers.ITextFileBufferManager). When a file buffer
	 * is saved, the current positions are written back to the match.
	 * 
	 * @param match
	 *            The match to show
	 * @param currentOffset
	 *            The current start offset of the match
	 * @param currentLength
	 *            The current length of the selection
	 * @throws PartInitException
	 *             If an editor can't be opened.
	 */
	protected abstract void showMatch(Match match, int currentOffset,
			int currentLength) throws PartInitException;
	/**
	 * This method is called whenever the set of matches for the given elements
	 * changes. This method is guaranteed to be called in the UI thread. Note
	 * that this notification is asynchronous. i.e. further changes may have
	 * occured by the time this method is called. They will be described in a
	 * future call.
	 * 
	 * @param objects
	 *            Array of objects that has to be refreshed.
	 */
	protected abstract void elementsChanged(Object[] objects);
	/**
	 * This method is called whenever all elements have been removed from the
	 * shown <code>AbstractSearchResult</code>. This method is guaranteed to
	 * be called in the UI thread. Note that this notification is asynchronous.
	 * i.e. further changes may have occured by the time this method is called.
	 * They will be described in a future call.
	 */
	protected abstract void clear();
	/**
	 * Configures the given viewer. Implementers have to set at least a content
	 * provider and a label provider. This method may be called if the page was
	 * constructed with the flag <code>FLAG_LAYOUT_TREE</code>.
	 * 
	 * @param viewer
	 *            The viewer to be configured
	 */
	protected abstract void configureTreeViewer(TreeViewer viewer);
	/**
	 * Configures the given viewer. Implementers have to set at least a content
	 * provider and a label provider. This method may be called if the page was
	 * constructed with the flag <code>FLAG_LAYOUT_FLAT</code>.
	 * 
	 * @param viewer
	 *            The viewer to be configured
	 */
	protected abstract void configureTableViewer(TableViewer viewer);
	private static void createStandardGroups(IContributionManager menu) {
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
	 * Fills the context menu for this page. Subclasses may override this
	 * method.
	 * 
	 * @param tbm
	 */
	protected void fillContextMenu(IMenuManager mgr) {
		mgr.appendToGroup(IContextMenuConstants.GROUP_ADDITIONS,
				fCopyToClipboardAction);
		mgr.appendToGroup(IContextMenuConstants.GROUP_SHOW, fShowNextAction);
		mgr
				.appendToGroup(IContextMenuConstants.GROUP_SHOW,
						fShowPreviousAction);
		if (getCurrentMatch() != null)
			mgr.appendToGroup(IContextMenuConstants.GROUP_REMOVE_MATCHES,
					fRemoveResultsAction);
		mgr.appendToGroup(IContextMenuConstants.GROUP_REMOVE_MATCHES,
				fRemoveAllResultsAction);
	}
	/**
	 * {@inheritDoc}
	 */
	public void createControl(Composite parent) {
		fQueryListener = createQueryListener();
		fMenu = new MenuManager("#PopUp"); //$NON-NLS-1$
		fMenu.setRemoveAllWhenShown(true);
		fMenu.setParent(getSite().getActionBars().getMenuManager());
		fMenu.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				createStandardGroups(mgr);
				fillContextMenu(mgr);
				fViewPart.fillContextMenu(mgr);
			}
		});
		fPagebook = new PageBook(parent, SWT.NULL);
		fPagebook.setLayoutData(new GridData(GridData.FILL_BOTH));
		fBusyLabel = createBusyControl();
		fViewerContainer = new Composite(fPagebook, SWT.NULL);
		fViewerContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		fViewerContainer.setSize(100, 100);
		fViewerContainer.setLayout(new FillLayout());
		createViewer(fViewerContainer, fCurrentLayout);
		showBusyLabel(fIsBusyShown);
		NewSearchUI.addQueryListener(fQueryListener);
	}
	private Control createBusyControl() {
		Table busyLabel = new Table(fPagebook, SWT.NULL);
		TableItem item = new TableItem(busyLabel, SWT.NULL);
		item.setText(SearchMessages
				.getString("AbstractTextSearchViewPage.searching.label")); //$NON-NLS-1$
		busyLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return busyLabel;
	}
	private IQueryListener createQueryListener() {
		final Runnable runnable = new Runnable() {
			public void run() {
				updateBusyLabel();
			}
		};
		return new IQueryListener() {
			public void queryAdded(ISearchQuery query) {
				// ignore
			}
			public void queryRemoved(ISearchQuery query) {
				// ignore
			}
			public void queryStarting(ISearchQuery query) {
				asyncExec(runnable);
			}
			public void queryFinished(ISearchQuery query) {
				asyncExec(runnable);
			}
		};
	}
	private void updateBusyLabel() {
		AbstractTextSearchResult result = getInput();
		boolean shouldShowBusy = result != null
				&& NewSearchUI.isQueryRunning(result.getQuery())
				&& result.getMatchCount() == 0;
		if (shouldShowBusy == fIsBusyShown)
			return;
		fIsBusyShown = shouldShowBusy;
		showBusyLabel(fIsBusyShown);
	}
	private void showBusyLabel(boolean shouldShowBusy) {
		if (shouldShowBusy)
			fPagebook.showPage(fBusyLabel);
		else
			fPagebook.showPage(fViewerContainer);
	}
	/**
	 * Determines whether a certain layout is supported by this search result
	 * page.
	 * 
	 * @see #AbstractTextSearchViewPage(int)
	 * @param layout
	 * @return whether the given layout is suppported.
	 */
	public boolean isLayoutSupported(int layout) {
		return (layout & fSupportedLayouts) == layout;
	}
	/**
	 * Sets the layout of this search result page. The layout must be on of
	 * <code>FLAG_LAYOUT_FLAT</code> or <code>FLAG_LAYOUT_TREE</code>.
	 * <code>layout</code> must be one of the values passed during
	 * construction of this search result page.
	 * 
	 * @see #isLayoutSupported(int)
	 */
	public void setLayout(int layout) {
		Assert.isTrue(countBits(layout) == 1);
		Assert.isTrue(isLayoutSupported(layout));
		if (countBits(fSupportedLayouts) < 2)
			return;
		if (fCurrentLayout == layout)
			return;
		fCurrentLayout = layout;
		ISelection selection = fViewer.getSelection();
		ISearchResult result = disconnectViewer();
		fViewer.getControl().dispose();
		fViewer = null;
		createViewer(fViewerContainer, layout);
		fViewerContainer.layout(true);
		connectViewer(result);
		fViewer.setSelection(selection, true);
		getSettings().put(KEY_LAYOUT, layout);
	}
	private void updateLayoutActions() {
		if (fFlatAction != null)
			fFlatAction.setChecked(fCurrentLayout == fFlatAction.getLayout());
		if (fHierarchicalAction != null)
			fHierarchicalAction
					.setChecked(fCurrentLayout == fHierarchicalAction
							.getLayout());
	}
	/**
	 * Return the layout this page is currently using.
	 * 
	 * @see #FLAG_LAYOUT_FLAT
	 * @see #FLAG_LAYOUT_TREE
	 * @return The layout this page is currently using.
	 */
	public int getLayout() {
		return fCurrentLayout;
	}
	private void createViewer(Composite parent, int layout) {
		if ((layout & FLAG_LAYOUT_FLAT) != 0) {
			TableViewer viewer = new SearchResultsTableViewer(parent, SWT.MULTI
					| SWT.H_SCROLL | SWT.V_SCROLL);
			fViewer = viewer;
			configureTableViewer(viewer);
		} else if ((layout & FLAG_LAYOUT_TREE) != 0) {
			TreeViewer viewer = new SearchResultsTreeViewer(parent, SWT.MULTI
					| SWT.H_SCROLL | SWT.V_SCROLL);
			fViewer = viewer;
			configureTreeViewer(viewer);
		}
		IToolBarManager tbm = getSite().getActionBars().getToolBarManager();
		tbm.removeAll();
		createStandardGroups(tbm);
		fillToolbar(tbm);
		tbm.update(false);
		fViewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				boolean hasCurrentMatch = showCurrentMatch();
				if (event.getViewer() instanceof TreeViewer
						&& event.getSelection() instanceof IStructuredSelection) {
					TreeViewer tv = (TreeViewer) event.getViewer();
					Object element = ((IStructuredSelection) event
							.getSelection()).getFirstElement();
					tv.setExpandedState(element, !tv.getExpandedState(element));
					return;
				} else if (!hasCurrentMatch) {
					gotoNextMatch();
				}
			}
		});
		fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				fCurrentMatchIndex = 0;
			}
		});
		Menu menu = fMenu.createContextMenu(fViewer.getControl());
		fViewer.getControl().setMenu(menu);
		getSite().setSelectionProvider(fViewer);
		// Register menu
		getSite().registerContextMenu(fViewPart.getViewSite().getId(), fMenu,
				fViewer);
		updateLayoutActions();
	}
	/**
	 * {@inheritDoc}
	 */
	public void setFocus() {
		Control control = fViewer.getControl();
		if (control != null && !control.isDisposed())
			control.setFocus();
	}
	/**
	 * {@inheritDoc}
	 */
	public Control getControl() {
		return fPagebook;
	}
	/**
	 * {@inheritDoc}
	 */
	public void setInput(ISearchResult search, Object viewState) {
		ISearchResult oldSearch = disconnectViewer();
		if (oldSearch != null)
			oldSearch.removeListener(fListener);
		AnnotationManager.searchResultActivated(getSite().getWorkbenchWindow(),
				(AbstractTextSearchResult) search);
		if (search != null) {
			search.addListener(fListener);
			connectViewer(search);
			if (viewState instanceof ISelection)
				fViewer.setSelection((ISelection) viewState, true);
			else
				gotoNextMatch();
		}
		updateBusyLabel();
	}
	/**
	 * {@inheritDoc}
	 */
	public Object getUIState() {
		return fViewer.getSelection();
	}
	private void connectViewer(ISearchResult search) {
		fCopyToClipboardAction = new CopyToClipboardAction(fViewer);
		fViewer.setInput(search);
	}
	private ISearchResult disconnectViewer() {
		ISearchResult result = (ISearchResult) fViewer.getInput();
		fViewer.setInput(null);
		return result;
	}
	/**
	 * Returns the viewer currently used in this page.
	 * 
	 * @return The currently used viewer or <code>null</code> if none has been
	 *         created yet.
	 */
	protected StructuredViewer getViewer() {
		return fViewer;
	}
	private void showMatch(final Match match) {
		ISafeRunnable runnable = new ISafeRunnable() {
			public void handleException(Throwable exception) {
				if (exception instanceof PartInitException) {
					PartInitException pie = (PartInitException) exception;
					ErrorDialog
							.openError(
									getSite().getShell(),
									SearchMessages
											.getString("DefaultSearchViewPage.show_match"),
									SearchMessages
											.getString("DefaultSearchViewPage.error.no_editor"),
									pie.getStatus()); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			public void run() throws Exception {
				Position currentPosition = InternalSearchUI.getInstance()
						.getPositionTracker().getCurrentPosition(match);
				if (currentPosition != null) {
					showMatch(match, currentPosition.getOffset(),
							currentPosition.getLength());
				} else {
					showMatch(match, match.getOffset(), match.getLength());
				}
			}
		};
		Platform.run(runnable);
	}
	/**
	 * Returns the currently shown result.
	 * 
	 * @see AbstractTextSearchViewPage#setInput(ISearchResult, Object)
	 * @return The previously set result or <code>null</code>
	 */
	public AbstractTextSearchResult getInput() {
		if (fViewer != null)
			return (AbstractTextSearchResult) fViewer.getInput();
		return null;
	}
	/**
	 * Selects the element corresponding to the next match and shows the match
	 * in an editor. Note that this will cycle back to the first match after the
	 * last match.
	 */
	public void gotoNextMatch() {
		fCurrentMatchIndex++;
		Match nextMatch = getCurrentMatch();
		if (nextMatch == null) {
			navigateNext(true);
			fCurrentMatchIndex = 0;
		}
		showCurrentMatch();
	}
	/**
	 * Selects the element corresponding to the previous match and shows the
	 * match in an editor. Note that this will cycle back to the last match
	 * after the first match.
	 */
	public void gotoPreviousMatch() {
		fCurrentMatchIndex--;
		Match nextMatch = getCurrentMatch();
		if (nextMatch == null) {
			navigateNext(false);
			fCurrentMatchIndex = getInput().getMatchCount(
					getFirstSelectedElement()) - 1;
		}
		showCurrentMatch();
	}
	private void navigateNext(boolean forward) {
		if (fViewer instanceof INavigate) {
			((INavigate) fViewer).navigateNext(forward);
		}
	}
	private boolean showCurrentMatch() {
		Match currentMatch = getCurrentMatch();
		if (currentMatch != null) {
			showMatch(currentMatch);
			return true;
		} else {
			return false;
		}
	}
	/**
	 * Returns the currently selected match.
	 * 
	 * @return The selected match or null if none are selected.
	 */
	public Match getCurrentMatch() {
		Object element = getFirstSelectedElement();
		if (element != null) {
			Match[] matches = getInput().getMatches(element);
			if (fCurrentMatchIndex >= 0 && fCurrentMatchIndex < matches.length)
				return matches[fCurrentMatchIndex];
		}
		return null;
	}
	private Object getFirstSelectedElement() {
		IStructuredSelection selection = (IStructuredSelection) fViewer
				.getSelection();
		if (selection.size() > 0)
			return selection.getFirstElement();
		return null;
	}
	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
		//disconnectViewer();
		super.dispose();
		NewSearchUI.removeQueryListener(fQueryListener);
	}
	/**
	 * {@inheritDoc}
	 */
	public void init(IPageSite pageSite) {
		super.init(pageSite);
		addLayoutActions(pageSite.getActionBars().getMenuManager());
	}
	/**
	 * Fills the toolbar contribution for this page. Subclasses may override
	 * this method.
	 * 
	 * @param tbm
	 */
	protected void fillToolbar(IToolBarManager tbm) {
		tbm.appendToGroup(IContextMenuConstants.GROUP_SHOW, fShowNextAction); //$NON-NLS-1$
		tbm
				.appendToGroup(IContextMenuConstants.GROUP_SHOW,
						fShowPreviousAction); //$NON-NLS-1$
		tbm.appendToGroup(IContextMenuConstants.GROUP_REMOVE_MATCHES,
				fRemoveResultsAction); //$NON-NLS-1$
		tbm.appendToGroup(IContextMenuConstants.GROUP_REMOVE_MATCHES,
				fRemoveAllResultsAction); //$NON-NLS-1$
		IActionBars actionBars = getSite().getActionBars();
		if (actionBars != null) {
			actionBars.setGlobalActionHandler(ActionFactory.NEXT.getId(),
					fShowNextAction);
			actionBars.setGlobalActionHandler(ActionFactory.PREVIOUS.getId(),
					fShowPreviousAction);
		}
	}
	private void addLayoutActions(IMenuManager menuManager) {
		if (fFlatAction != null)
			menuManager.add(fFlatAction);
		if (fHierarchicalAction != null)
			menuManager.add(fHierarchicalAction);
	}
	/**
	 * { @inheritDoc }
	 */
	public void setViewPart(ISearchResultViewPart part) {
		fViewPart = part;
	}
	// multithreaded update handling.
	private synchronized void handleSearchResultsChanged(
			final SearchResultEvent e) {
		if (e instanceof MatchEvent) {
			MatchEvent me = (MatchEvent) e;
			postUpdate(me.getMatches());
			AbstractTextSearchResult result = (AbstractTextSearchResult) me
					.getSearchResult();
			if (result.getMatchCount() == 1)
				asyncExec(new Runnable() {
					public void run() {
						navigateNext(true);
					}
				});
		} else if (e instanceof RemoveAllEvent) {
			postClear();
		}
	}
	private synchronized void postUpdate(Match[] matches) {
		for (int i = 0; i < matches.length; i++) {
			fBatchedUpdates.add(matches[i].getElement());
		}
		if (fBatchedUpdates.size() == 1) {
			asyncExec(new Runnable() {
				public void run() {
					runBatchedUpdates();
				}
			});
		}
	}
	private void runBatchedUpdates() {
		synchronized (this) {
			elementsChanged(fBatchedUpdates.toArray());
			fBatchedUpdates.clear();
			updateBusyLabel();
		}
	}
	private void postClear() {
		asyncExec(new Runnable() {
			public void run() {
				runClear();
			}
		});
	}
	private void runClear() {
		synchronized (this) {
			fBatchedUpdates.clear();
			updateBusyLabel();
		}
		clear();
	}
	private void asyncExec(Runnable runnable) {
		Control control = getControl();
		if (control != null && !control.isDisposed()) {
			Display currentDisplay = Display.getCurrent();
			if (currentDisplay == null
					|| !currentDisplay.equals(control.getDisplay()))
				// meaning we're not executing on the display thread of the
				// control
				control.getDisplay().asyncExec(runnable);
			else
				runnable.run();
		}
	}
	/**
	 * Subclasses may override.
	 * { @inheritDoc }
	 */
	public void restoreState(IMemento memento) {
		if (countBits(fSupportedLayouts) > 1) {
			try {
				fCurrentLayout = getSettings().getInt(KEY_LAYOUT);
				// workaround because the saved value may be 0
				if (fCurrentLayout == 0)
					initLayout();
			} catch (NumberFormatException e) {
				// ignore, signals no value stored.
			}
			if (memento != null) {
				Integer layout = memento.getInteger(KEY_LAYOUT);
				if (layout != null) {
					fCurrentLayout = layout.intValue();
					// workaround because the saved value may be 0
					if (fCurrentLayout == 0)
						initLayout();
				}
			}
		}
	}
	/**
	 * Subclasses my override.
	 * { @inheritDoc }
	 */
	public void saveState(IMemento memento) {
		if (countBits(fSupportedLayouts) > 1) {
			memento.putInteger(KEY_LAYOUT, fCurrentLayout);
		}
	}
}
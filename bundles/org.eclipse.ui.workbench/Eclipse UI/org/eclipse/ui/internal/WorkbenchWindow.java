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

package org.eclipse.ui.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CBanner;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.commands.ActionHandler;
import org.eclipse.ui.commands.HandlerSubmission;
import org.eclipse.ui.commands.IHandler;
import org.eclipse.ui.contexts.IWorkbenchContextSupport;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.internal.misc.Policy;
import org.eclipse.ui.internal.misc.UIStats;
import org.eclipse.ui.internal.progress.ProgressRegion;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IActionSet;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;

/**
 * A window within the workbench.
 */
public class WorkbenchWindow extends ApplicationWindow implements IWorkbenchWindow {

	private int number;
	private PageList pageList = new PageList();
	private PageListenerList pageListeners = new PageListenerList();
	private PerspectiveListenerListOld perspectiveListeners = new PerspectiveListenerListOld();
	private WWinPerspectiveService perspectiveService = new WWinPerspectiveService(this);
	private WWinPartService partService = new WWinPartService(this);
	private ActionPresentation actionPresentation;
	private WWinActionBars actionBars;

	private boolean updateDisabled = true;
	private boolean closing = false;
	private boolean shellActivated = false;

	private FastViewBar fastViewBar;

	private PerspectiveBarManager perspectiveBar;
	private Menu perspectiveBarMenu;
	
	private TrimLayout defaultLayout; 
	
	ProgressRegion progressRegion;

	private Label noOpenPerspective;
	private Rectangle normalBounds;
	private boolean asMaximizedState = false;

	private CBanner topBar;
	
	/**
	 * The composite under which workbench pages create their controls.
	 * 
	 * @since 3.0
	 */
	private Composite pageComposite;

	/**
	 * Bit flags indication which submenus (New, Show Views, ...) this
	 * window contains. Initially none.
	 * 
	 * @since 3.0
	 */
	private int submenus = 0x00;

	/**
	 * Object for configuring this workbench window. Lazily initialized to
	 * an instance unique to this window.
	 *  
	 * @since 3.0
	 */
	private WorkbenchWindowConfigurer windowConfigurer = null;

	// constants for shortcut bar group ids 
	static final String GRP_PAGES = "pages"; //$NON-NLS-1$
	static final String GRP_PERSPECTIVES = "perspectives"; //$NON-NLS-1$
	static final String GRP_FAST_VIEWS = "fastViews"; //$NON-NLS-1$

	// static fields for inner classes.
	static final int VGAP = 0;
	static final int CLIENT_INSET = 3;
	static final int BAR_SIZE = 23;

	/**
	 * Constant (bit mask) indicating which the Show View submenu is
	 * probably present somewhere in this window.
	 * 
	 * @see #addSubmenu
	 * @since 3.0
	 */
	public static final int SHOW_VIEW_SUBMENU = 0x01;

	/**
	 * Constant (bit mask) indicating which the Open Perspective submenu is
	 * probably present somewhere in this window.
	 * 
	 * @see #addSubmenu
	 * @since 3.0
	 */
	public static final int OPEN_PERSPECTIVE_SUBMENU = 0x02;

	/**
	 * Constant (bit mask) indicating which the New Wizard submenu is
	 * probably present somewhere in this window.
	 * 
	 * @see #addSubmenu
	 * @since 3.0
	 */
	public static final int NEW_WIZARD_SUBMENU = 0x04;

	/**
	 * Remembers that this window contains the given submenu.
	 * 
	 * @param type the type of submenu, one of: 
	 * {@link #NEW_WIZARD_SUBMENU NEW_WIZARD_SUBMENU},
	 * {@link #OPEN_PERSPECTIVE_SUBMENU OPEN_PERSPECTIVE_SUBMENU},
	 * {@link #SHOW_VIEW_SUBMENU SHOW_VIEW_SUBMENU}
	 * @see #containsSubmenu
	 * @since 3.0
	 */
	public void addSubmenu(int type) {
		submenus |= type;
	}

	/**
	 * Checks to see if this window contains the given type of submenu.
	 * 
	 * @param type the type of submenu, one of: 
	 * {@link #NEW_WIZARD_SUBMENU NEW_WIZARD_SUBMENU},
	 * {@link #OPEN_PERSPECTIVE_SUBMENU OPEN_PERSPECTIVE_SUBMENU},
	 * {@link #SHOW_VIEW_SUBMENU SHOW_VIEW_SUBMENU}
	 * @return <code>true</code> if window contains submenu,
	 * <code>false</code> otherwise
	 * @see #addSubmenu
	 * @since 3.0
	 */
	public boolean containsSubmenu(int type) {
		return ((submenus & type) != 0);
	}

	/**
	 * Constant indicating that all the actions bars should be
	 * filled.
	 * 
	 * @since 3.0
	 */
	private static final int FILL_ALL_ACTION_BARS =
		WorkbenchAdvisor.FILL_MENU_BAR
			| WorkbenchAdvisor.FILL_COOL_BAR
			| WorkbenchAdvisor.FILL_STATUS_LINE;

	/**
	 * Creates and initializes a new workbench window.
	 * 
	 * @param number the number for the window
	 */
	public WorkbenchWindow(int number) {
		super(null);
		this.number = number;
		
		// Make sure there is a workbench. This call will throw
		// an exception if workbench not created yet. 
		PlatformUI.getWorkbench();
		
		// Add contribution managers that are exposed to other plugins.
		addMenuBar();
		addCoolBar(SWT.FLAT);
		addStatusLine();

		actionPresentation = new ActionPresentation(this);

		// let the application do further configuration
		getAdvisor().preWindowOpen(getWindowConfigurer());
		
		// set the shell style
		setShellStyle(getWindowConfigurer().getShellStyle());
		
		// Fill the action bars	
		getAdvisor().fillActionBars(
			this,
			getWindowConfigurer().getActionBarConfigurer(),
			FILL_ALL_ACTION_BARS);
	}

	/**
	 * Return the style bits for the shortcut bar.
	 * @return int
	 */
	protected int perspectiveBarStyle() {
		return SWT.FLAT | SWT.WRAP | SWT.RIGHT | SWT.HORIZONTAL;
	}	

	private TrimDropTarget trimDropTarget;
	private boolean coolBarVisible = true;
	private boolean perspectiveBarVisible = true;
	private boolean statusLineVisible = true;
		
	private Map actionSetHandlersByCommandId = new HashMap();
	private Map globalActionHandlersByCommandId = new HashMap();
	List handlerSubmissions = new ArrayList();
	
	void registerActionSets(IActionSet[] actionSets) {
		actionSetHandlersByCommandId.clear();
		
		for (int i = 0; i < actionSets.length; i++)
			if (actionSets[i] instanceof PluginActionSet) {
				PluginActionSet pluginActionSet = (PluginActionSet) actionSets[i];
				IAction[] pluginActions = pluginActionSet.getPluginActions();

				for (int j = 0; j < pluginActions.length; j++) {
					IAction pluginAction = pluginActions[j];
					String commandId = pluginAction.getActionDefinitionId();

					if (commandId != null)			
						actionSetHandlersByCommandId.put(commandId, new ActionHandler(pluginAction));
				}
			}
			
		setHandlersByCommandId();
	}

	void registerGlobalAction(IAction globalAction) {		
		String commandId = globalAction.getActionDefinitionId();

		if (commandId != null)
			globalActionHandlersByCommandId.put(commandId, new ActionHandler(globalAction));
	
		setHandlersByCommandId();
	}

	void setHandlersByCommandId() {
		Map handlersByCommandId = new HashMap();
		handlersByCommandId.putAll(actionSetHandlersByCommandId);
		handlersByCommandId.putAll(globalActionHandlersByCommandId);		
		List newHandlerSubmissions = new ArrayList();
		
		for (Iterator iterator = handlersByCommandId.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String commandId = (String) entry.getKey();
            IHandler handler = (IHandler) entry.getValue();
            newHandlerSubmissions.add(new HandlerSubmission(null, this, commandId, handler, 5));
        }
		
		Workbench.getInstance().getCommandSupport().removeHandlerSubmissions(this.handlerSubmissions);
		this.handlerSubmissions = newHandlerSubmissions;
		Workbench.getInstance().getCommandSupport().addHandlerSubmissions(this.handlerSubmissions);		
	}	
	
	/*
	 * Adds an listener to the part service.
	 */
	public void addPageListener(IPageListener l) {
		pageListeners.addPageListener(l);
	}
	/*
	 * Adds an listener to the perspective service.
	 *
	 * NOTE: Internally, please use getPerspectiveService instead.
	 */
	public void addPerspectiveListener(org.eclipse.ui.IPerspectiveListener l) {
		perspectiveListeners.addPerspectiveListener(l);
	}
	
	/**
	 * add a shortcut for the page.
	 */
	void addPerspectiveShortcut(IPerspectiveDescriptor perspective, WorkbenchPage workbenchPage) {
	    if (perspectiveBar != null) {
			perspectiveBar.add(new PerspectiveBarContributionItem(perspective, workbenchPage));
			perspectiveBar.update(false);
			perspectiveBar.getControl().getParent().layout();
	    }
	}
	
	/**
	 * Configures this window to have a perspecive bar.
	 * Does nothing if it already has one.
	 */
	protected void addPerspectiveBar(int style) {
	    Assert.isTrue(perspectiveBar == null);
		perspectiveBar = new PerspectiveBarManager(style);
		perspectiveBar.add(new PerspectiveBarNewContributionItem(this));	
	}
	
	/**
	 * Close the window.
	 * 
	 * Assumes that busy cursor is active.
	 */
	private boolean busyClose() {
		// Whether the window was actually closed or not
		boolean windowClosed = false;

		// Setup internal flags to indicate window is in
		// progress of closing and no update should be done.
		closing = true;
		updateDisabled = true;

		try {
			// Only do the check if it is OK to close if we are not closing
			// via the workbench as the workbench will check this itself.
			Workbench workbench = getWorkbenchImpl();
			int count = workbench.getWorkbenchWindowCount();
			if (!workbench.isClosing() && count <= 1) {
				windowClosed = workbench.close();
			} else {
				if (okToClose()) {
					windowClosed = hardClose();
				}
			}
		} finally {
			if (!windowClosed) {
				// Reset the internal flags if window was not closed.
				closing = false;
				updateDisabled = false;
			}
		}

		return windowClosed;
	}
	/**
	 * Opens a new page. Assumes that busy cursor is active.
	 * <p>
	 * <b>Note:</b> Since release 2.0, a window is limited to contain at most
	 * one page. If a page exist in the window when this method is used, then
	 * another window is created for the new page.  Callers are strongly
	 * recommended to use the <code>IWorkbench.openPerspective</code> APIs to
	 * programmatically show a perspective.
	 * </p>
	 */
	protected IWorkbenchPage busyOpenPage(String perspID, IAdaptable input)
		throws WorkbenchException {
		IWorkbenchPage newPage = null;

		if (pageList.isEmpty()) {
			newPage = new WorkbenchPage(this, perspID, input);
			pageList.add(newPage);
			firePageOpened(newPage);
			setActivePage(newPage);
		} else {
			IWorkbenchWindow window = getWorkbench().openWorkbenchWindow(perspID, input);
			newPage = window.getActivePage();
		}

		return newPage;
	}
    
	/**
	 * @see Window
	 */
	public int open() {
	    getAdvisor().postWindowCreate(getWindowConfigurer());
		int result = super.open();
		getWorkbenchImpl().fireWindowOpened(this);
		getAdvisor().postWindowOpen(getWindowConfigurer());
		return result;
	}

	/* (non-Javadoc)
	 * Method declared on Window.
	 */
	protected boolean canHandleShellCloseEvent() {
		if (!super.canHandleShellCloseEvent()) {
			return false;
		}
		// let the advisor veto the user's explicit request to close the window
		return getAdvisor().preWindowShellClose(getWindowConfigurer());
	}

	/**
	 * @see IWorkbenchWindow
	 */
	public boolean close() {
		final boolean[] ret = new boolean[1];
		BusyIndicator.showWhile(null, new Runnable() {
			public void run() {
				ret[0] = busyClose();
			}
		});
		return ret[0];
	}

	protected boolean isClosing() {
		return closing || getWorkbenchImpl().isClosing();
	}
	/**
	 * Return whether or not the coolbar layout is locked.
	 */
	protected boolean isCoolBarLocked() {
		return getCoolBarManager().getLockLayout();
	}

	/**
	 * Close all of the pages.
	 */
	private void closeAllPages() {
		// Deactivate active page.
		setActivePage(null);

		// Clone and deref all so that calls to getPages() returns
		// empty list (if call by pageClosed event handlers)
		PageList oldList = pageList;
		pageList = new PageList();

		// Close all.
		Iterator enum = oldList.iterator();
		while (enum.hasNext()) {
			WorkbenchPage page = (WorkbenchPage) enum.next();
			firePageClosed(page);
			page.dispose();
		}
		if (!closing)
			showEmptyWindowMessage();
	}
	/**
	 * Save and close all of the pages.
	 */
	public void closeAllPages(boolean save) {
		if (save) {
			boolean ret = saveAllPages(true);
			if (!ret)
				return;
		}
		closeAllPages();
	}
	/**
	 * closePerspective method comment.
	 */
	protected boolean closePage(IWorkbenchPage in, boolean save) {
		// Validate the input.
		if (!pageList.contains(in))
			return false;
		WorkbenchPage oldPage = (WorkbenchPage) in;

		// Save old perspective.
		if (save && oldPage.isSaveNeeded()) {
			if (!oldPage.saveAllEditors(true))
				return false;
		}

		// If old page is activate deactivate.
		boolean oldIsActive = (oldPage == getActiveWorkbenchPage());
		if (oldIsActive)
			setActivePage(null);

		// Close old page. 
		pageList.remove(oldPage);
		firePageClosed(oldPage);
		oldPage.dispose();

		// Activate new page.
		if (oldIsActive) {
			IWorkbenchPage newPage = pageList.getNextActive();
			if (newPage != null)
				setActivePage(newPage);
		}
		if (!closing && pageList.isEmpty())
			showEmptyWindowMessage();
		return true;
	}
	private void showEmptyWindowMessage() {
		Composite parent = getPageComposite();
		if (noOpenPerspective == null) {
			noOpenPerspective = new Label(parent, SWT.NONE);
			noOpenPerspective.setText(WorkbenchMessages.getString("WorkbenchWindow.noPerspective")); //$NON-NLS-1$
			noOpenPerspective.setBounds(parent.getClientArea());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);

		String title = getWindowConfigurer().basicGetTitle();
		if (title != null) {
			shell.setText(title);
		}
		
		WorkbenchHelp.setHelp(shell, IHelpContextIds.WORKBENCH_WINDOW);

		trackShellActivation(shell);
		trackShellResize(shell);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.ApplicationWindow#createTrimWidgets(org.eclipse.swt.widgets.Shell)
	 */
	protected void createTrimWidgets(Shell shell) {
	    // do nothing -- trim widgets are created in createDefaultContents
	}

	/**
	 * Creates and remembers the client composite, under which workbench
	 * pages create their controls.
	 * 
	 * @since 3.0
	 */
	protected Composite createPageComposite(Composite parent) {
	    pageComposite = new Composite(parent, SWT.NONE);
	    return pageComposite;
	}
	
	
    /**
     * Creates the contents of the workbench window, including
     * trim controls and the client composite.
     * This MUST create the client composite via a call to
     * <code>createClientComposite</code>.
     * 
     * @since 3.0
     */
    protected Control createContents(Composite parent) {
        // we know from Window.create that the parent is a Shell.
        getAdvisor().createWindowContents(getWindowConfigurer(), (Shell) parent);
        // the page composite must be set by createWindowContents
        Assert.isNotNull(pageComposite, "createWindowContents must call configurer.createPageComposite"); //$NON-NLS-1$
        return pageComposite;
    }
    
    /**
     * Creates the default contents and layout of the shell. 
     * 
     * @param shell the shell
     */
	protected void createDefaultContents(final Shell shell) {
		defaultLayout = new TrimLayout();
		defaultLayout.setSpacing(5, 5, 2, 2);
		shell.setLayout(defaultLayout);
	    
	    Menu menuBar = getMenuBarManager().createMenuBar(shell);
	    if (getWindowConfigurer().getShowMenuBar()) {
			shell.setMenuBar(menuBar);
		}
	    
		topBar = new CBanner(shell, SWT.NONE);

		Control coolBar = createCoolBarControl(topBar);
		// need to resize the shell, not just the coolbar's immediate
		// parent, if the coolbar wants to grow or shrink
        coolBar.addListener(SWT.Resize, new Listener() {
            public void handleEvent(Event event) {
                shell.layout();
            }
        });
		if (getWindowConfigurer().getShowCoolBar()) {
			topBar.setLeft(getCoolBarControl());
		}

		addPerspectiveBar(perspectiveBarStyle());
		createPerspectiveBar(topBar);
		perspectiveBar.setBanner(topBar);
		if (getWindowConfigurer().getShowPerspectiveBar()) {
			topBar.setRight(perspectiveBar.getControl());
		}

		createStatusLine(shell);
		
		fastViewBar = new FastViewBar(this);
		fastViewBar.createControl(shell);

		createProgressIndicator(shell);

		trimDropTarget = new TrimDropTarget(shell, this);		
		DragUtil.addDragTarget(shell, trimDropTarget);
		
		// If the user clicks on toolbar, status bar, or shortcut bar
		// hide the fast view.
		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				WorkbenchPage currentPage = getActiveWorkbenchPage();
				if (currentPage != null) {
					if (event.type == SWT.MouseDown) {
						if (event.widget instanceof ToolBar) {
							// Ignore mouse down on actual tool bar buttons
							Point pt = new Point(event.x, event.y);
							ToolBar toolBar = (ToolBar) event.widget;
							if (toolBar.getItem(pt) != null)
								return;
						}
						currentPage.toggleFastView(null);
					}
				}
			}
		};
		topBar.addListener(SWT.MouseDown, listener);
		getCoolBarControl().addListener(SWT.MouseDown, listener);
		Control[] children = ((Composite) getStatusLineManager().getControl()).getChildren();
		for (int i = 0; i < children.length; i++) {
			if (children[i] != null)
				children[i].addListener(SWT.MouseDown, listener);
		}
		//fastViewBar.getControl().addListener(SWT.MouseDown, listener);
		perspectiveBar.getControl().addListener(SWT.MouseDown, listener);
		
		
		// Create the client composite area (where page content goes).
		createPageComposite(shell);
		
		setLayoutDataForContents();
	}
	
    /**
	 * Create the perspective toolbar control
	 */
	private void createPerspectiveBar(Composite parent) {

	    // TODO: encapsulate the listener and showPerspectiveBarPopup in PerspectiveBar

	    Listener listener = new Listener() {
			public void handleEvent(Event event) {
				if (event.type == SWT.MenuDetect) {
					showPerspectiveBarPopup(new Point(event.x, event.y));
				}
			}
		};

		perspectiveBar.createControl(parent);
		
		perspectiveBar.getControl().addListener(SWT.MenuDetect, listener);
		
	}
	
	/**
	 * Returns the shortcut for a page.
	 */
	/* protected */
	IContributionItem findPerspectiveShortcut(
		IPerspectiveDescriptor perspective,
		WorkbenchPage page) {
	    if (perspectiveBar == null) {
	        return null;
	    }
		IContributionItem[] array = perspectiveBar.getItems();
		int length = array.length;
		for (int i = 0; i < length; i++) {
			IContributionItem item = array[i];
						
			if (item instanceof PerspectiveBarContributionItem)
				if (((PerspectiveBarContributionItem) item).handles(perspective, page))
					return item;
		}
		return null;
	}
	/**
	 * Fires page activated
	 */
	private void firePageActivated(IWorkbenchPage page) {
		pageListeners.firePageActivated(page);
		partService.pageActivated(page);
	}
	/**
	 * Fires page closed
	 */
	private void firePageClosed(IWorkbenchPage page) {
		pageListeners.firePageClosed(page);
		partService.pageClosed(page);
	}
	/**
	 * Fires page opened
	 */
	private void firePageOpened(IWorkbenchPage page) {
		pageListeners.firePageOpened(page);
		partService.pageOpened(page);
	}
	/**
	 * Fires perspective activated
	 */
	void firePerspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		perspectiveListeners.firePerspectiveActivated(page, perspective);
		perspectiveService.firePerspectiveActivated(page, perspective);
	}
	/**
	 * Fires perspective changed
	 */
	void firePerspectiveChanged(
		IWorkbenchPage page,
		IPerspectiveDescriptor perspective,
		String changeId) {
		perspectiveListeners.firePerspectiveChanged(page, perspective, changeId);
		perspectiveService.firePerspectiveChanged(page, perspective, changeId);
	}
	/**
	 * Fires perspective closed
	 */
	void firePerspectiveClosed(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		perspectiveService.firePerspectiveClosed(page, perspective);
	}
	/**
	 * Fires perspective opened
	 */
	void firePerspectiveOpened(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		perspectiveService.firePerspectiveOpened(page, perspective);
	}
	/**
	 * Returns the action bars for this window.
	 */
	public WWinActionBars getActionBars() {
		if (actionBars == null) {
			actionBars = new WWinActionBars(this);
		}
		return actionBars;
	}

	/**
	 * Returns the active page.
	 *
	 * @return the active page
	 */
	public IWorkbenchPage getActivePage() {
		return pageList.getActive();
	}
	/**
	 * Returns the active workbench page.
	 *
	 * @return the active workbench page
	 */
	/* package */
	WorkbenchPage getActiveWorkbenchPage() {
		return pageList.getActive();
	}
	/**
	 * Returns the page composite, under which the window's pages create
	 * their controls.
	 */
	protected Composite getPageComposite() {
		return pageComposite;
	}
	/**
	 * Answer the menu manager for this window.
	 */
	public MenuManager getMenuManager() {
		return getMenuBarManager();
	}
	/**
	 * Returns the number.  This corresponds to a page number in a window or a
	 * window number in the workbench.
	 */
	public int getNumber() {
		return number;
	}
	/**
	 * Returns an array of the pages in the workbench window.
	 *
	 * @return an array of pages
	 */
	public IWorkbenchPage[] getPages() {
		return pageList.getPages();
	}
	/**
	 * @see IWorkbenchWindow
	 */
	public IPartService getPartService() {
		return partService;
	}

	/**
	 * Returns the layout for the shell.
	 * 
	 * @return the layout for the shell
	 */
	protected Layout getLayout() {
		return null;
	}

	/**
	 * @see IWorkbenchWindow
	 */
	public IPerspectiveService getPerspectiveService() {
		return perspectiveService;
	}

	/**
	 * @see IWorkbenchWindow
	 */
	public ISelectionService getSelectionService() {
		return partService.getSelectionService();
	}
	/**
	 * Returns <code>true</code> when the window's shell
	 * is activated, <code>false</code> when it's shell is
	 * deactivated
	 * 
	 * @return boolean <code>true</code> when shell activated,
	 * 		<code>false</code> when shell deactivated
	 */
	public boolean getShellActivated() {
		return shellActivated;
	}

	/**
	 * Returns the status line manager for this window (if it has one).
	 *
	 * @return the status line manager, or <code>null</code> if
	 *   this window does not have a status line
	 * @see #addStatusLine
	 */
	public StatusLineManager getStatusLineManager() {
		return super.getStatusLineManager();
	}

	/**
	 * @see IWorkbenchWindow
	 */
	public IWorkbench getWorkbench() {
		return PlatformUI.getWorkbench();
	}
	public String getToolbarLabel(String actionSetId) {
		ActionSetRegistry registry = WorkbenchPlugin.getDefault().getActionSetRegistry();
		IActionSetDescriptor actionSet = registry.findActionSet(actionSetId);
		if (actionSet != null) {
			return actionSet.getLabel();
		} else {
			if (IWorkbenchActionConstants.TOOLBAR_FILE.equalsIgnoreCase(actionSetId))
				return WorkbenchMessages.getString("WorkbenchWindow.FileToolbar"); //$NON-NLS-1$
			if (IWorkbenchActionConstants.TOOLBAR_NAVIGATE.equalsIgnoreCase(actionSetId))
				return WorkbenchMessages.getString("WorkbenchWindow.NavigateToolbar"); //$NON-NLS-1$
		}
		return null;
	}
	/**
	 * Unconditionally close this window. Assumes the proper
	 * flags have been set correctly (e.i. closing and updateDisabled)
	 */
	private boolean hardClose() {
		boolean result;
		try {
			// Clear the action sets, fix for bug 27416.
			actionPresentation.clearActionSets();
			closeAllPages();
			// let the application do further deconfiguration
			getAdvisor().postWindowClose(getWindowConfigurer());
			getWorkbenchImpl().fireWindowClosed(this);
		} finally {
			result = super.close();
		}
		return result;
	}
	/**
	 * @see IWorkbenchWindow
	 */
	public boolean isApplicationMenu(String menuID) {
		// delegate this question to the workbench advisor
		return getAdvisor().isApplicationMenu(getWindowConfigurer(), menuID);
	}

	/**
	 * Return whether or not the given id matches the id of the coolitems that
	 * the application creates.
	 */
	/* package */
	boolean isWorkbenchCoolItemId(String id) {
		return windowConfigurer.containsCoolItem(id);
	}
	/**
	 * Locks/unlocks the CoolBar for the workbench.
	 * 
	 * @param lock whether the CoolBar should be locked or unlocked
	 */
	/* package */
	void lockCoolBar(boolean lock) {
		getCoolBarManager().setLockLayout(lock);
	}
	/**
	 * Called when this window is about to be closed.
	 *
	 * Subclasses may overide to add code that returns <code>false</code> 
	 * to prevent closing under certain conditions.
	 */
	public boolean okToClose() {
		// Save all of the editors.
		if (!getWorkbenchImpl().isClosing())
			if (!saveAllPages(true))
				return false;
		return true;
	}
	/**
	 * Opens a new page.
	 * <p>
	 * <b>Note:</b> Since release 2.0, a window is limited to contain at most
	 * one page. If a page exist in the window when this method is used, then
	 * another window is created for the new page.  Callers are strongly
	 * recommended to use the <code>IWorkbench.openPerspective</code> APIs to
	 * programmatically show a perspective.
	 * </p>
	 */
	public IWorkbenchPage openPage(final String perspId, final IAdaptable input)
		throws WorkbenchException {
		Assert.isNotNull(perspId);

		// Run op in busy cursor.
		final Object[] result = new Object[1];
		BusyIndicator.showWhile(null, new Runnable() {
			public void run() {
				try {
					result[0] = busyOpenPage(perspId, input);
				} catch (WorkbenchException e) {
					result[0] = e;
				}
			}
		});

		if (result[0] instanceof IWorkbenchPage)
			return (IWorkbenchPage) result[0];
		else if (result[0] instanceof WorkbenchException)
			throw (WorkbenchException) result[0];
		else
			throw new WorkbenchException(WorkbenchMessages.getString("WorkbenchWindow.exceptionMessage")); //$NON-NLS-1$
	}
	/**
	 * Opens a new page. 
	 * <p>
	 * <b>Note:</b> Since release 2.0, a window is limited to contain at most
	 * one page. If a page exist in the window when this method is used, then
	 * another window is created for the new page.  Callers are strongly
	 * recommended to use the <code>IWorkbench.openPerspective</code> APIs to
	 * programmatically show a perspective.
	 * </p>
	 */
	public IWorkbenchPage openPage(IAdaptable input) throws WorkbenchException {
		String perspId = getWorkbenchImpl().getPerspectiveRegistry().getDefaultPerspective();
		return openPage(perspId, input);
	}

	/*
	 * Removes an listener from the part service.
	 */
	public void removePageListener(IPageListener l) {
		pageListeners.removePageListener(l);
	}
	/*
	 * Removes an listener from the perspective service.
	 *
	 * NOTE: Internally, please use getPerspectiveService instead.
	 */
	public void removePerspectiveListener(org.eclipse.ui.IPerspectiveListener l) {
		perspectiveListeners.removePerspectiveListener(l);
	}
	/**
	 * Remove the shortcut for a page.
	 */
	/* package */
	void removePerspectiveShortcut(IPerspectiveDescriptor perspective, WorkbenchPage page) {
		IContributionItem item = findPerspectiveShortcut(perspective, page);
		if (item != null) {
			perspectiveBar.remove(item);
			perspectiveBar.update(false);
			topBar.layout(true);
		}
	}
	private IStatus unableToRestorePage(IMemento pageMem) {
		String pageName = pageMem.getString(IWorkbenchConstants.TAG_LABEL);
		if (pageName == null)
			pageName = ""; //$NON-NLS-1$
		return new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, WorkbenchMessages.format("WorkbenchWindow.unableToRestorePerspective", new String[] { pageName }), //$NON-NLS-1$
		null);
	}
	/**
	 * @see IPersistable.
	 */
	public IStatus restoreState(IMemento memento, IPerspectiveDescriptor activeDescriptor) {
		Assert.isNotNull(getShell());

		MultiStatus result = new MultiStatus(PlatformUI.PLUGIN_ID, IStatus.OK, WorkbenchMessages.getString("WorkbenchWindow.problemsRestoringWindow"), null); //$NON-NLS-1$

		// Read window's bounds and state.
		Rectangle displayBounds = getShell().getDisplay().getBounds();
		Rectangle shellBounds = new Rectangle(0, 0, 0, 0);
		
		IMemento fastViewMem = memento.getChild(IWorkbenchConstants.TAG_FAST_VIEW_DATA);
		if (fastViewMem != null) {
		    if (fastViewBar != null) {
		        fastViewBar.restoreState(fastViewMem);
		    }
		}
		Integer bigInt = memento.getInteger(IWorkbenchConstants.TAG_X);
		shellBounds.x = bigInt == null ? 0 : bigInt.intValue();
		bigInt = memento.getInteger(IWorkbenchConstants.TAG_Y);
		shellBounds.y = bigInt == null ? 0 : bigInt.intValue();
		bigInt = memento.getInteger(IWorkbenchConstants.TAG_WIDTH);
		shellBounds.width = bigInt == null ? 0 : bigInt.intValue();
		bigInt = memento.getInteger(IWorkbenchConstants.TAG_HEIGHT);
		shellBounds.height = bigInt == null ? 0 : bigInt.intValue();
		if (!shellBounds.isEmpty()) {
			if (!shellBounds.intersects(displayBounds)) {
				Rectangle clientArea = getShell().getDisplay().getClientArea();
				shellBounds.x = clientArea.x;
				shellBounds.y = clientArea.y;
			}
			getShell().setBounds(shellBounds);
		}
		if ("true".equals(memento.getString(IWorkbenchConstants.TAG_MAXIMIZED))) { //$NON-NLS-1$
			getShell().setMaximized(true);
		}
		if ("true".equals(memento.getString(IWorkbenchConstants.TAG_MINIMIZED))) { //$NON-NLS-1$
			//		getShell().setMinimized(true);
		}

		// Restore the cool bar order by creating all the tool bar contribution items
		// This needs to be done before pages are created to ensure proper canonical creation
		// of cool items
		if (getCoolBarManager() != null) {
			CoolBarManager coolBarMgr = getCoolBarManager();
			IMemento coolBarMem = memento.getChild(IWorkbenchConstants.TAG_COOLBAR_LAYOUT);
			if (coolBarMem != null) {
				// Check if the layout is locked
				Integer lockedInt = coolBarMem.getInteger(IWorkbenchConstants.TAG_LOCKED);
				if ((lockedInt != null) && (lockedInt.intValue() == 1)) {
					coolBarMgr.setLockLayout(true);
				}else {
					coolBarMgr.setLockLayout(false);
				}
				// The new layout of the cool bar manager
				ArrayList coolBarLayout = new ArrayList();
				// Traverse through all the cool item in the memento
				IMemento contributionMems[] =
					coolBarMem.getChildren(IWorkbenchConstants.TAG_COOLITEM);
				for (int i = 0; i < contributionMems.length; i++) {
					IMemento contributionMem = contributionMems[i];
					String type = contributionMem.getString(IWorkbenchConstants.TAG_ITEM_TYPE);
					if (type == null) {
						// Do not recognize that type
						continue;
					}
					String id = contributionMem.getString(IWorkbenchConstants.TAG_ID);
					
					// Prevent duplicate items from being read back in.
					IContributionItem existingItem = coolBarMgr.find(id);
					if ((id != null) && (existingItem != null)) {
					    if (Policy.DEBUG_TOOLBAR_DISPOSAL) {
					        System.out.println("Not loading duplicate cool bar item: " + id); //$NON-NLS-1$
					    }
					    coolBarLayout.add(existingItem);
					    continue;
					}
					IContributionItem newItem = null;
					if (type.equals(IWorkbenchConstants.TAG_TYPE_SEPARATOR)) {
						if (id != null) {
							newItem = new Separator(id);
						} else {
							newItem = new Separator();
						}
					} else if (id != null) {
					    if (type.equals(IWorkbenchConstants.TAG_TYPE_GROUPMARKER)) {
						newItem = new GroupMarker(id);

					    } else if (type.equals(IWorkbenchConstants.TAG_TYPE_TOOLBARCONTRIBUTION) 
					            || type.equals(IWorkbenchConstants.TAG_TYPE_PLACEHOLDER)) {

						// Get Width and height
						Integer width = contributionMem.getInteger(IWorkbenchConstants.TAG_ITEM_X);
						Integer height = contributionMem.getInteger(IWorkbenchConstants.TAG_ITEM_Y);
						// Look for the object in the current cool bar manager
						IContributionItem oldItem = coolBarMgr.find(id);
						// If a tool bar contribution item already exists for this id then use the old object
					        if (oldItem != null) {
					            newItem = oldItem;
						} else {
							newItem =
								new ToolBarContributionItem(
										new ToolBarManager(coolBarMgr.getStyle()),
										id);
					            if (type.equals(IWorkbenchConstants.TAG_TYPE_PLACEHOLDER)) {
					                ToolBarContributionItem newToolBarItem = (ToolBarContributionItem) newItem;
					                if (height != null) {
					                    newToolBarItem.setCurrentHeight(height.intValue());
					                }
					                if (width != null) {
					                    newToolBarItem.setCurrentWidth(width.intValue());
					                }
					                newItem = new PlaceholderContributionItem(newToolBarItem);
					            }
							// make it invisible by default
							newItem.setVisible(false);
							// Need to add the item to the cool bar manager so that its canonical order can be preserved
							IContributionItem refItem =
								findAlphabeticalOrder(
										IWorkbenchActionConstants.MB_ADDITIONS,
										id,
										coolBarMgr);
							if (refItem != null) {
								coolBarMgr.insertAfter(refItem.getId(), newItem);
							}else {
								coolBarMgr.add(newItem);
							}
						}
						// Set the current height and width
					        if ((width != null) && (newItem instanceof ToolBarContributionItem)) {
							((ToolBarContributionItem) newItem).setCurrentWidth(width.intValue());
						}
					        if ((height != null) && (newItem instanceof ToolBarContributionItem)) {
							((ToolBarContributionItem) newItem).setCurrentHeight(height.intValue());
						}
					}
					}
					// Add new item into cool bar manager
					if (newItem != null) {
						coolBarLayout.add(newItem);
						newItem.setParent(coolBarMgr);
						coolBarMgr.markDirty();
					}
				}
				
				// We need to check if we have everything we need in the layout.
				final ArrayList finalLayout = new ArrayList();
				IContributionItem[] existingItems = coolBarMgr.getItems();
				for (int i = 0; i < existingItems.length; i++) {
			        IContributionItem existingItem = existingItems[i];
			        
			        /* This line shouldn't be necessary, but is here for
			         * robustness.
			         */
			        if (existingItem == null) {
			            continue;
			        }
			        
				    boolean found = false;
				    Iterator layoutItemItr = coolBarLayout.iterator();
				    while (layoutItemItr.hasNext()) {
				        IContributionItem layoutItem = (IContributionItem) layoutItemItr.next();
				        if ((layoutItem != null) && (layoutItem.equals(existingItem))) {
				            found = true;
				            break;
				        }
				    }
				    
				    if (!found) {
				        if (existingItem != null) {
				            finalLayout.add(existingItem);
				        }
				    }
				}
				
				// Set the cool bar layout to the given layout.
				finalLayout.addAll(coolBarLayout);
				coolBarMgr.setLayout(finalLayout);
			} else {
				// For older workbenchs
				coolBarMem = memento.getChild(IWorkbenchConstants.TAG_TOOLBAR_LAYOUT);
				if (coolBarMem != null) {
					// Restore an older layout
					restoreOldCoolBar(coolBarMem);
				}
			}
		}

		// Recreate each page in the window. 
		IWorkbenchPage newActivePage = null;
		IMemento[] pageArray = memento.getChildren(IWorkbenchConstants.TAG_PAGE);
		for (int i = 0; i < pageArray.length; i++) {
			IMemento pageMem = pageArray[i];
			String strFocus = pageMem.getString(IWorkbenchConstants.TAG_FOCUS);
			if (strFocus == null || strFocus.length() == 0)
				continue;

			// Get the input factory.
			IAdaptable input = null;
			IMemento inputMem = pageMem.getChild(IWorkbenchConstants.TAG_INPUT);
			if (inputMem != null) {
				String factoryID = inputMem.getString(IWorkbenchConstants.TAG_FACTORY_ID);
				if (factoryID == null) {
					WorkbenchPlugin.log("Unable to restore page - no input factory ID."); //$NON-NLS-1$
					result.add(unableToRestorePage(pageMem));
					continue;
				}
				try {
					UIStats.start(UIStats.RESTORE_WORKBENCH, "WorkbenchPageFactory"); //$NON-NLS-1$
					IElementFactory factory = PlatformUI.getWorkbench().getElementFactory(factoryID);
					if (factory == null) {
						WorkbenchPlugin.log("Unable to restore page - cannot instantiate input factory: " + factoryID); //$NON-NLS-1$
						result.add(unableToRestorePage(pageMem));
						continue;
					}
	
					// Get the input element.
					input = factory.createElement(inputMem);
					if (input == null) {
						WorkbenchPlugin.log("Unable to restore page - cannot instantiate input element: " + factoryID); //$NON-NLS-1$
						result.add(unableToRestorePage(pageMem));
						continue;
					}
				} finally {
					UIStats.end(UIStats.RESTORE_WORKBENCH, "WorkbenchPageFactory"); //$NON-NLS-1$
				}
			}
			// Open the perspective.
			WorkbenchPage newPage = null;
			try {
				newPage = new WorkbenchPage(this, input);
				result.add(newPage.restoreState(pageMem, activeDescriptor));
				pageList.add(newPage);
				firePageOpened(newPage);
			} catch (WorkbenchException e) {
				WorkbenchPlugin.log("Unable to restore perspective - constructor failed."); //$NON-NLS-1$
				result.add(e.getStatus());
				continue;
			}

			if (strFocus != null && strFocus.length() > 0)
				newActivePage = newPage;
		}

		// If there are no pages create a default.
		if (pageList.isEmpty()) {
			try {
				String defPerspID =
					getWorkbenchImpl().getPerspectiveRegistry().getDefaultPerspective();
				WorkbenchPage newPage =
					new WorkbenchPage(this, defPerspID, getAdvisor().getDefaultPageInput());
				pageList.add(newPage);
				firePageOpened(newPage);
			} catch (WorkbenchException e) {
				WorkbenchPlugin.log("Unable to create default perspective - constructor failed."); //$NON-NLS-1$
				result.add(e.getStatus());
				String productName = WorkbenchPlugin.getDefault().getProductName();
				if (productName == null) {
					productName = ""; //$NON-NLS-1$
				}
				getShell().setText(productName);
			}
		}

		// Set active page.
		if (newActivePage == null)
			newActivePage = (IWorkbenchPage) pageList.getNextActive();

		setActivePage(newActivePage);

		return result;
	}
	
	/**
	 * Restores cool item order from an old workbench.
	 */
	private boolean restoreOldCoolBar(IMemento coolbarMem) {
		// Make sure the tag exist
		if (coolbarMem == null) {
			return false;
		}
		CoolBarManager coolBarMgr = getCoolBarManager();
		// Check to see if layout is locked
		Integer locked = coolbarMem.getInteger(IWorkbenchConstants.TAG_LOCKED);
		boolean state = (locked != null) && (locked.intValue() == 1);
		coolBarMgr.setLockLayout(state);
		
		// Get the visual layout
		IMemento visibleLayout = coolbarMem.getChild(IWorkbenchConstants.TAG_TOOLBAR_LAYOUT);
		ArrayList visibleWrapIndicies = new ArrayList();
		ArrayList visibleItems = new ArrayList();
		if (visibleLayout != null) {
			if (readLayout(visibleLayout,visibleItems,visibleWrapIndicies) == false ) {
				return false;
			}	
		}
		// Get the remembered layout
		IMemento rememberedLayout = coolbarMem.getChild(IWorkbenchConstants.TAG_LAYOUT);
		ArrayList rememberedWrapIndicies = new ArrayList();
		ArrayList rememberedItems = new ArrayList();
		if (rememberedLayout != null) {
			if (readLayout(rememberedLayout,rememberedItems,rememberedWrapIndicies) == false ) {
				return false;
			}	
		}

		// Create the objects
		if (visibleItems != null) {
			// Merge remembered layout into visible layout
			if (rememberedItems != null) {		
				// Traverse through all the remembered items
				int currentIndex = 0;
				for (Iterator i = rememberedItems.iterator(); i.hasNext();currentIndex++) {
					String id = (String)i.next();
					int index = -1;
					for(Iterator iter=visibleItems.iterator();iter.hasNext();) {
						String visibleId = (String)iter.next();
						if (visibleId.equals(id)) {
							index = visibleItems.indexOf(visibleId);
							break;
						}
					}
					// The item is not in the visible list
					if (index == -1) {
						int insertAt = Math.max(0,Math.min(currentIndex,visibleItems.size()));
						boolean separateLine = false;
						// Check whether this item is on a separate line
						for(Iterator iter=rememberedWrapIndicies.iterator();iter.hasNext();) {
							Integer wrapIndex = (Integer)iter.next();
							if (wrapIndex.intValue() <= insertAt) {
								insertAt = visibleItems.size();
								// Add new wrap index for this Item
								visibleWrapIndicies.add(new Integer(insertAt));
								separateLine = true;
							}
						}
						// Add item to array list
						visibleItems.add(insertAt,id);
						// If the item was not on a separate line then adjust the visible wrap indicies
						if (!separateLine) {
							// Adjust visible wrap indicies
							for(int j=0; j < visibleWrapIndicies.size();j++) {
								Integer index2 = (Integer)visibleWrapIndicies.get(j);
								if (index2.intValue() >= insertAt) {
									visibleWrapIndicies.set(j, new Integer(index2.intValue() + 1));				
								}
							}
						}
					}
				}
			}
			// The new layout of the cool bar manager
			ArrayList coolBarLayout = new ArrayList(visibleItems.size());
			// Add all visible items to the layout object
			for (Iterator i = visibleItems.iterator(); i.hasNext();) {
				String id = (String)i.next();
				// Look for the object in the current cool bar manager
				IContributionItem oldItem = null;
				IContributionItem newItem = null;
				if (id != null) {
					oldItem = coolBarMgr.find(id);
				}
				// If a tool bar contribution item already exists for this id then use the old object
				if (oldItem instanceof ToolBarContributionItem) {
					newItem = (ToolBarContributionItem) oldItem;
				} else {
					newItem =
						new ToolBarContributionItem(
								new ToolBarManager(coolBarMgr.getStyle()),
								id);
					// make it invisible by default
					newItem.setVisible(false);
					// Need to add the item to the cool bar manager so that its canonical order can be preserved
					IContributionItem refItem =
						findAlphabeticalOrder(
								IWorkbenchActionConstants.MB_ADDITIONS,
								id,
								coolBarMgr);
					if (refItem != null) {
						coolBarMgr.insertAfter(refItem.getId(), newItem);
					}else {
						coolBarMgr.add(newItem);
					}
				}
				// Add new item into cool bar manager
				if (newItem != null) {
					coolBarLayout.add(newItem);
					newItem.setParent(coolBarMgr);
					coolBarMgr.markDirty();
				}
			}
			
			// Add separators to the displayed Items data structure
			int offset = 0;
			for(int i=1; i < visibleWrapIndicies.size(); i++) {
				int insertAt = ((Integer)visibleWrapIndicies.get(i)).intValue() + offset;
				coolBarLayout.add(insertAt,new Separator(CoolBarManager.USER_SEPARATOR));
				offset++;
			}
			
			// Add any group markers in their appropriate places
			IContributionItem[] items = coolBarMgr.getItems();
			for (int i=0; i < items.length; i++) {
				IContributionItem item = items[i];
				if (item.isGroupMarker()) {
					coolBarLayout.add(Math.max(Math.min(i,coolBarLayout.size()), 0),item);
				}
			}
			
			coolBarMgr.setLayout(coolBarLayout);
		}
		return true;
	}
	
	/**
	 * Helper method used for restoring an old cool bar layout. This method reads the memento
	 * and populatates the item id's and wrap indicies.
	 */
	private boolean readLayout(IMemento memento, ArrayList itemIds, ArrayList wrapIndicies) {
		// Get the Wrap indicies
		IMemento [] wraps = memento.getChildren(IWorkbenchConstants.TAG_ITEM_WRAP_INDEX);
		if (wraps == null) return false;
		for (int i = 0; i < wraps.length; i++) {
			IMemento wrapMem = wraps[i];
			Integer index = wrapMem.getInteger(IWorkbenchConstants.TAG_INDEX);
			if (index == null) return false;
			wrapIndicies.add(index);
		}
		// Get the Item ids
		IMemento [] savedItems = memento.getChildren(IWorkbenchConstants.TAG_ITEM);
		if (savedItems == null) return false;
		for (int i = 0; i < savedItems.length; i++) {
			IMemento savedMem = savedItems[i];
			String id = savedMem.getString(IWorkbenchConstants.TAG_ID);
			if (id == null) return false;
			itemIds.add(id);
		}
		return true;
	}

	/**
	 * Returns the contribution item that the given contribution item should be inserted after.
	 * 
	 * @param startId the location to start looking alphabetically.
	 * @param itemId the target item id.
	 * @param mgr the contribution manager.
	 * @return the contribution item that the given items should be returned after.
	 */
	private IContributionItem findAlphabeticalOrder(
		String startId,
		String itemId,
		IContributionManager mgr) {
		IContributionItem[] items = mgr.getItems();
		int insertIndex = 0;

		// look for starting point
		while (insertIndex < items.length) {
			IContributionItem item = items[insertIndex];
			if (item.getId().equals(startId))
				break;
			++insertIndex;
		}

		// Find the index that this item should be inserted in
		for (int i = insertIndex + 1; i < items.length; i++) {
			IContributionItem item = items[i];
			String testId = item.getId();

			if (item.isGroupMarker())
				break;

			if (itemId != null) {
				if (itemId.compareTo(testId) < 1)
					break;
			}
			insertIndex = i;
		}
		if ( insertIndex >= items.length) {
			return null;
		}
		return items[insertIndex];
	}

	/* (non-Javadoc)
	 * Method declared on IRunnableContext.
	 */
	public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable)
		throws InvocationTargetException, InterruptedException {
		IWorkbenchContextSupport contextSupport = getWorkbench().getContextSupport();
		final boolean keyFilterEnabled = contextSupport.isKeyFilterEnabled();

		Control fastViewBarControl = getFastViewBar() == null ? null : getFastViewBar().getControl();
		boolean fastViewBarWasEnabled = fastViewBarControl == null ? false : fastViewBarControl.getEnabled();

		Control perspectiveBarControl = getPerspectiveBar() == null ? null : getPerspectiveBar().getControl();
		boolean perspectiveBarWasEnabled = perspectiveBarControl == null ? false : perspectiveBarControl.getEnabled();

		try {
			if (fastViewBarControl != null && !fastViewBarControl.isDisposed())
			    fastViewBarControl.setEnabled(false);

			if (perspectiveBarControl != null && !perspectiveBarControl.isDisposed())
			    perspectiveBarControl.setEnabled(false);

			if (keyFilterEnabled)
				contextSupport.setKeyFilterEnabled(false);

			super.run(fork, cancelable, runnable);
		} finally {
			if (fastViewBarControl != null && !fastViewBarControl.isDisposed())
			    fastViewBarControl.setEnabled(fastViewBarWasEnabled);

			if (perspectiveBarControl != null && !perspectiveBarControl.isDisposed())
			    perspectiveBarControl.setEnabled(perspectiveBarWasEnabled);

			if (keyFilterEnabled)
				contextSupport.setKeyFilterEnabled(true);
		}
	}
	/**
	 * Save all of the pages.  Returns true if the operation succeeded.
	 */
	private boolean saveAllPages(boolean bConfirm) {
		boolean bRet = true;
		Iterator enum = pageList.iterator();
		while (bRet && enum.hasNext()) {
			WorkbenchPage page = (WorkbenchPage) enum.next();
			bRet = page.saveAllEditors(bConfirm);
		}
		return bRet;
	}
	/**
	 * @see IPersistable
	 */
	public IStatus saveState(IMemento memento) {

		MultiStatus result = new MultiStatus(PlatformUI.PLUGIN_ID, IStatus.OK, WorkbenchMessages.getString("WorkbenchWindow.problemsSavingWindow"), null); //$NON-NLS-1$

		// Save the window's state and bounds.
		if (getShell().getMaximized() || asMaximizedState) {
			memento.putString(IWorkbenchConstants.TAG_MAXIMIZED, "true"); //$NON-NLS-1$
		}
		if (getShell().getMinimized()) {
			memento.putString(IWorkbenchConstants.TAG_MINIMIZED, "true"); //$NON-NLS-1$
		}
		if (normalBounds == null) {
			normalBounds = getShell().getBounds();
		}
		IMemento fastViewBarMem = memento.createChild(IWorkbenchConstants.TAG_FAST_VIEW_DATA);
		if (fastViewBar != null) {
		    fastViewBar.saveState(fastViewBarMem);
		}
		
		memento.putInteger(IWorkbenchConstants.TAG_X, normalBounds.x);
		memento.putInteger(IWorkbenchConstants.TAG_Y, normalBounds.y);
		memento.putInteger(IWorkbenchConstants.TAG_WIDTH, normalBounds.width);
		memento.putInteger(IWorkbenchConstants.TAG_HEIGHT, normalBounds.height);

		/// Save the order of the cool bar contribution items
		if (getCoolBarManager() != null) {
			getCoolBarManager().refresh();
			IMemento coolBarMem = memento.createChild(IWorkbenchConstants.TAG_COOLBAR_LAYOUT);
			if (getCoolBarManager().getLockLayout() == true) {
				coolBarMem.putInteger(IWorkbenchConstants.TAG_LOCKED,1);
			}else {
				coolBarMem.putInteger(IWorkbenchConstants.TAG_LOCKED,0);
			}
			IContributionItem[] items = getCoolBarManager().getItems();
			for (int i = 0; i < items.length; i++) {
				IMemento coolItemMem = coolBarMem.createChild(IWorkbenchConstants.TAG_COOLITEM);
				IContributionItem item = items[i];
				// The id of the contribution item
				if (item.getId() != null) {
					coolItemMem.putString(IWorkbenchConstants.TAG_ID, item.getId());
				}
				// Write out type and size if applicable
				if (item.isSeparator()) {
					coolItemMem.putString(
							IWorkbenchConstants.TAG_ITEM_TYPE,
							IWorkbenchConstants.TAG_TYPE_SEPARATOR);
				} else if (item.isGroupMarker() && !item.isSeparator()) {
					coolItemMem.putString(
							IWorkbenchConstants.TAG_ITEM_TYPE,
							IWorkbenchConstants.TAG_TYPE_GROUPMARKER);
				} else {
				    if (item instanceof PlaceholderContributionItem) {
					coolItemMem.putString(
							IWorkbenchConstants.TAG_ITEM_TYPE,
				                IWorkbenchConstants.TAG_TYPE_PLACEHOLDER);
				    } else {
				        // Store the identifier.
				        coolItemMem.putString(
				                IWorkbenchConstants.TAG_ITEM_TYPE,
							IWorkbenchConstants.TAG_TYPE_TOOLBARCONTRIBUTION);
				}
					
					/* Retrieve a reasonable approximation of the height and
					 * width, if possible.
					 */
					final int height;
					final int width;
					if (item instanceof ToolBarContributionItem) {
					    ToolBarContributionItem toolBarItem = (ToolBarContributionItem) item;
					    toolBarItem.saveWidgetState();
					    height = toolBarItem.getCurrentHeight();
					    width = toolBarItem.getCurrentWidth();
					} else if (item instanceof PlaceholderContributionItem) {
					    PlaceholderContributionItem placeholder = (PlaceholderContributionItem) item;
					    height = placeholder.getHeight();
					    width = placeholder.getWidth();
					} else {
					    height = -1;
					    width = -1;
			}
					
					// Store the height and width.
					coolItemMem.putInteger(IWorkbenchConstants.TAG_ITEM_X, width);
					coolItemMem.putInteger(IWorkbenchConstants.TAG_ITEM_Y, height); 
		}
			}
		}
		

		// Save each page.
		Iterator enum = pageList.iterator();
		while (enum.hasNext()) {
			WorkbenchPage page = (WorkbenchPage) enum.next();

			// Save perspective.
			IMemento pageMem = memento.createChild(IWorkbenchConstants.TAG_PAGE);
			pageMem.putString(IWorkbenchConstants.TAG_LABEL, page.getLabel());
			result.add(page.saveState(pageMem));
			
			if (page == getActiveWorkbenchPage()) {
				pageMem.putString(IWorkbenchConstants.TAG_FOCUS, "true"); //$NON-NLS-1$
			}

			// Get the input.
			IAdaptable input = page.getInput();
			if (input != null) {
				IPersistableElement persistable =
					(IPersistableElement) input.getAdapter(IPersistableElement.class);
				if (persistable == null) {
					WorkbenchPlugin.log(
						"Unable to save page input: " //$NON-NLS-1$
							+ input
							+ ", because it does not adapt to IPersistableElement"); //$NON-NLS-1$
				}
				else {
					// Save input.	
					IMemento inputMem = pageMem.createChild(IWorkbenchConstants.TAG_INPUT);
					inputMem.putString(IWorkbenchConstants.TAG_FACTORY_ID, persistable.getFactoryId());
					persistable.saveState(inputMem);
				}
			}
		}
		return result;
	}
	/**
	 * Select the shortcut for a perspective.
	 */
	/* package */
	void selectPerspectiveShortcut(
		IPerspectiveDescriptor perspective,
		WorkbenchPage page,
		boolean selected) {
		IContributionItem item = findPerspectiveShortcut(perspective, page);
		
		if (item != null)
			((PerspectiveBarContributionItem) item).setSelection(selected);
	}
	/**
	 * Sets the active page within the window.
	 *
	 * @param page identifies the new active page.
	 */
	public void setActivePage(final IWorkbenchPage in) {
		if (getActiveWorkbenchPage() == in)
			return;

		// 1FVGTNR: ITPUI:WINNT - busy cursor for switching perspectives
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				// Deactivate old persp.
				WorkbenchPage currentPage = getActiveWorkbenchPage();
				if (currentPage != null) {
					currentPage.onDeactivate();
				}

				// Activate new persp.
				if (in == null || pageList.contains(in))
					pageList.setActive(in);
				WorkbenchPage newPage = pageList.getActive();
				if (newPage != null) {
					newPage.onActivate();
					firePageActivated(newPage);
					if (newPage.getPerspective() != null)
						firePerspectiveActivated(newPage, newPage.getPerspective());
				}

				if (isClosing())
					return;

				updateDisabled = false;

				// Update action bars ( implicitly calls updateActionBars() )
				updateActionSets();
				if (perspectiveBar != null) {
				    perspectiveBar.update(false);
				}
				getMenuManager().update(IAction.TEXT);

				if (noOpenPerspective != null && in != null) {
					noOpenPerspective.dispose();
					noOpenPerspective = null;
				}
			}
		});
	}

	/**
	 * Shows the popup menu for a page item in the shortcut bar.
	 */
	private void showPerspectiveBarPopup(Point pt) {

		// Get the tool item under the mouse.
		ToolBar toolBar = perspectiveBar.getControl();
		ToolItem toolItem = toolBar.getItem(toolBar.toControl(pt));
		if (toolItem == null)
			return;

		// Get the action for the tool item.
		Object data = toolItem.getData();

		if (!(data instanceof PerspectiveBarContributionItem))
			return;
		
			// The perspective bar menu is created lazily here.
			// Its data is set (each time) to the tool item, which refers to the SetPagePerspectiveAction
			// which in turn refers to the page and perspective.
			// It is important not to refer to the action, the page or the perspective directly
			// since otherwise the menu hangs on to them after they are closed.
			// By hanging onto the tool item instead, these references are cleared when the
			// corresponding page or perspective is closed.
			// See bug 11282 for more details on why it is done this way.
			if (perspectiveBarMenu == null) {
				Menu menu = new Menu(toolBar);
				MenuItem menuItem = new MenuItem(menu, SWT.NONE);
				menuItem.setText(WorkbenchMessages.getString("WorkbenchWindow.close")); //$NON-NLS-1$
				menuItem.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						ToolItem perspectiveToolItem = (ToolItem) perspectiveBarMenu.getData();
						if (perspectiveToolItem != null && !perspectiveToolItem.isDisposed()) {
							PerspectiveBarContributionItem item =
								(PerspectiveBarContributionItem) perspectiveToolItem.getData();
							item.getPage().closePerspective(item.getPerspective(), true);
						}
					}
				});
				menuItem = new MenuItem(menu, SWT.NONE);
				menuItem.setText(WorkbenchMessages.getString("WorkbenchWindow.closeAll")); //$NON-NLS-1$
				menuItem.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						ToolItem perspectiveToolItem = (ToolItem) perspectiveBarMenu.getData();
						if (perspectiveToolItem != null && !perspectiveToolItem.isDisposed()) {
							PerspectiveBarContributionItem item =
								(PerspectiveBarContributionItem) perspectiveToolItem.getData();
							item.getPage().closeAllPerspectives();
						}
					}
				});
				perspectiveBarMenu = menu;
			}
			perspectiveBarMenu.setData(toolItem);

			// Show popup menu.
			if (perspectiveBarMenu != null) {
				perspectiveBarMenu.setLocation(pt.x, pt.y);
				perspectiveBarMenu.setVisible(true);
			}
	}

	/**
	 * Returns whether or not children exist for the Window's toolbar control.
	 * Overridden for coolbar support.
	 * <p>
	 * @return boolean true if children exist, false otherwise
	 */
	protected boolean toolBarChildrenExist() {
		CoolBar coolBarControl = (CoolBar) getCoolBarControl();
		return coolBarControl.getItemCount() > 0;
	}
	/**
	 * Hooks a listener to track the activation and
	 * deactivation of the window's shell. Notifies
	 * the active part and editor of the change
	 */
	private void trackShellActivation(Shell shell) {
		shell.addShellListener(new ShellAdapter() {
			public void shellActivated(ShellEvent event) {
				shellActivated = true;
				getWorkbenchImpl().setActivatedWindow(WorkbenchWindow.this);
				WorkbenchPage currentPage = getActiveWorkbenchPage();
				if (currentPage != null) {
					IWorkbenchPart part = currentPage.getActivePart();
					if (part != null) {
						PartSite site = (PartSite) part.getSite();
						site.getPane().shellActivated();
					}
					IEditorPart editor = currentPage.getActiveEditor();
					if (editor != null) {
						PartSite site = (PartSite) editor.getSite();
						site.getPane().shellActivated();
					}
					getWorkbenchImpl().fireWindowActivated(WorkbenchWindow.this);
				}
			}
			public void shellDeactivated(ShellEvent event) {
				shellActivated = false;
				WorkbenchPage currentPage = getActiveWorkbenchPage();
				if (currentPage != null) {
					IWorkbenchPart part = currentPage.getActivePart();
					if (part != null) {
						PartSite site = (PartSite) part.getSite();
						site.getPane().shellDeactivated();
					}
					IEditorPart editor = currentPage.getActiveEditor();
					if (editor != null) {
						PartSite site = (PartSite) editor.getSite();
						site.getPane().shellDeactivated();
					}
					getWorkbenchImpl().fireWindowDeactivated(WorkbenchWindow.this);
				}
			}
		});
	}
	/**
	 * Hooks a listener to track the resize of the window's
	 * shell. Stores the new bounds if in normal state - that
	 * is, not in minimized or maximized state)
	 */
	private void trackShellResize(Shell newShell) {
		newShell.addControlListener(new ControlAdapter() {
			public void controlMoved(ControlEvent e) {
				saveBounds();
			}

			public void controlResized(ControlEvent e) {
				saveBounds();
			}

			private void saveBounds() {
				Shell shell = getShell();
				if (shell == null)
					return;
				if (shell.isDisposed())
					return;
				if (shell.getMinimized())
					return;
				if (shell.getMaximized()) {
					asMaximizedState = true;
					return;
				}
				asMaximizedState = false;
				normalBounds = shell.getBounds();
			}
		});
	}

	/**
	 * update the action bars.
	 */
	public void updateActionBars() {
		if (updateDisabled)
			return;
		// updateAll required in order to enable accelerators on pull-down menus
		getMenuBarManager().updateAll(false);
		getCoolBarManager().update(false);
		getStatusLineManager().update(false);
	}
	/**
	 * Update the visible action sets. This method is typically called
	 * from a page when the user changes the visible action sets
	 * within the prespective.  
	 */
	public void updateActionSets() {
		if (updateDisabled)
			return;

		WorkbenchPage currentPage = getActiveWorkbenchPage();
		if (currentPage == null)
			actionPresentation.clearActionSets();
		else {
			if (getCoolBarManager() != null) {
				getCoolBarManager().refresh();
			}
			actionPresentation.setActionSets(currentPage.getActionSets());
		}
		updateActionBars();

		// hide the launch menu if it is empty
		String path =
			IWorkbenchActionConstants.M_WINDOW
				+ IWorkbenchActionConstants.SEP
				+ IWorkbenchActionConstants.M_LAUNCH;
		IMenuManager manager = getMenuBarManager().findMenuUsingPath(path);
		IContributionItem item = getMenuBarManager().findUsingPath(path);
		
		// TODO remove: updateActiveActions();
		IActionSet actionSets[] = actionPresentation.getActionSets();
		registerActionSets(actionSets);

		if (manager == null || item == null)
			return;
		item.setVisible(manager.getItems().length >= 2);
		// there is a separator for the additions group thus >= 2
	}
	/**
	 * Updates the shorcut item
	 */
	/* package */
	void updatePerspectiveShortcut(
		IPerspectiveDescriptor oldDesc,
		IPerspectiveDescriptor newDesc,
		WorkbenchPage page) {
		if (updateDisabled)
			return;

		IContributionItem item = findPerspectiveShortcut(oldDesc, page);
		
		if (item != null)
			((PerspectiveBarContributionItem) item).update(newDesc);
	}

	/**
	 * Return whether or not to show the progress indicator.
	 * @return boolan
	 */
	boolean showProgressIndicator() {
		return PlatformUI.getWorkbench().getPreferenceStore().getBoolean(
			IWorkbenchConstants.SHOW_PROGRESS_INDICATOR);
	}

	/**
	 * Create the progress indicator for the receiver.
	 * @param shell	the parent shell
	 */
	private void createProgressIndicator(Shell shell) {
		if (getWindowConfigurer().getShowProgressIndicator()) {
			progressRegion = new ProgressRegion();
			progressRegion.createContents(shell,this);
		}

	}
	class PageList {
		//List of pages in the order they were created;
		private List pagesInCreationOrder;
		//List of pages where the top is the last activated.
		private List pageInActivationOrder;
		// The page explicitly activated
		private Object active;

		public PageList() {
			pagesInCreationOrder = new ArrayList(4);
			pageInActivationOrder = new ArrayList(4);
		}
		public boolean add(Object object) {
			pagesInCreationOrder.add(object);
			pageInActivationOrder.add(0, object);
			//It will be moved to top only when activated.
			return true;
		}
		public Iterator iterator() {
			return pagesInCreationOrder.iterator();
		}
		public boolean contains(Object object) {
			return pagesInCreationOrder.contains(object);
		}
		public boolean remove(Object object) {
			if (active == object)
				active = null;
			pageInActivationOrder.remove(object);
			return pagesInCreationOrder.remove(object);
		}
		public boolean isEmpty() {
			return pagesInCreationOrder.isEmpty();
		}
		public IWorkbenchPage[] getPages() {
			int nSize = pagesInCreationOrder.size();
			IWorkbenchPage[] retArray = new IWorkbenchPage[nSize];
			pagesInCreationOrder.toArray(retArray);
			return retArray;
		}
		public void setActive(Object page) {
			if (active == page)
				return;

			active = page;

			if (page != null) {
				pageInActivationOrder.remove(page);
				pageInActivationOrder.add(page);
			}
		}
		public WorkbenchPage getActive() {
			return (WorkbenchPage) active;
		}
		public WorkbenchPage getNextActive() {
			if (active == null) {
				if (pageInActivationOrder.isEmpty())
					return null;
				else
					return (WorkbenchPage) pageInActivationOrder.get(pageInActivationOrder.size() - 1);
			} else {
				if (pageInActivationOrder.size() < 2)
					return null;
				else
					return (WorkbenchPage) pageInActivationOrder.get(pageInActivationOrder.size() - 2);
			}
		}
	}

	/**
	 * Returns the unique object that applications use to configure this window.
	 * <p>
	 * IMPORTANT This method is declared package-private to prevent regular
	 * plug-ins from downcasting IWorkbenchWindow to WorkbenchWindow and getting
	 * hold of the workbench window configurer that would allow them to tamper
	 * with the workbench window. The workbench window configurer is available
	 * only to the application.
	 * </p>
	 */
	/* package - DO NOT CHANGE */
	WorkbenchWindowConfigurer getWindowConfigurer() {
		if (windowConfigurer == null) {
			// lazy initialize
			windowConfigurer = new WorkbenchWindowConfigurer(this);
			windowConfigurer.init();
		}
		return windowConfigurer;
	}

	/**
	 * Returns the workbench advisor. Assumes the workbench
	 * has been created already.
	 * <p>
	 * IMPORTANT This method is declared private to prevent regular
	 * plug-ins from downcasting IWorkbenchWindow to WorkbenchWindow and getting
	 * hold of the workbench advisor that would allow them to tamper with the
	 * workbench. The workbench advisor is internal to the application.
	 * </p>
	 */
	private /* private - DO NOT CHANGE */
	WorkbenchAdvisor getAdvisor() {
		return getWorkbenchImpl().getAdvisor();
	}

	/*
	 * Returns the IWorkbench implementation.
	 */
	private Workbench getWorkbenchImpl() {
		return Workbench.getInstance();
	}

	/**
	 * Creates a clone copy of the current action bars
	 * @param configurer location of managers
	 * @param flags indicate which actions to load and whether its a proxy fill
	 */
	public void fillActionBars(IActionBarConfigurer configurer, int flags) {
		getAdvisor().fillActionBars(this, configurer, flags);
	}


	/**
	 * The <code>WorkbenchWindow</code> implementation of this method has the same
	 * logic as <code>Window</code>'s implementation, but without the resize check.
	 * We don't want to skip setting the bounds if the shell has been resized since a
	 * free resize event occurs on Windows when the menubar is set in configureShell. 
	 */
	protected void initializeBounds() {
		Point size = getInitialSize();
		Point location = getInitialLocation(size);
		getShell().setBounds(getConstrainedShellBounds(new Rectangle(location.x, location.y, size.x, size.y)));
	}
	
	/**
	 * The <code>WorkbenchWindow</code> implementation of this method
	 * delegates to the window configurer.
	 *  
	 * @since 3.0
	 */
	protected Point getInitialSize() {
		return getWindowConfigurer().getInitialSize();
	}
	
	/**
	 * @param visible whether the cool bar should be shown.  This is only 
	 * applicable if the window configurer also wishes either the cool bar to be
	 * visible.
	 * @since 3.0 
	 */	
	public void setCoolBarVisible(boolean visible) {
		boolean oldValue = coolBarVisible;
		coolBarVisible = visible;
		if (oldValue != coolBarVisible) {
			updateLayoutDataForContents();
		}
	}
	
	/** 
	 * @return whether the cool bar should be shown.  This is only 
	 * applicable if the window configurer also wishes either the cool bar to be
	 * visible.
	 * @since 3.0
	 */
	public boolean getCoolBarVisible() {
		return coolBarVisible;
	}
	
	/**
	 * @param visible whether the perspective bar should be shown.  This is only 
	 * applicable if the window configurer also wishes either the perspective 
	 * bar to be visible.
	 * @since 3.0 
	 */	
	public void setPerspectiveBarVisible(boolean visible) {
		boolean oldValue = perspectiveBarVisible;
		perspectiveBarVisible = visible;
		if (oldValue != perspectiveBarVisible) {
			updateLayoutDataForContents();
		}
	}
	
	/** 
	 * @return whether the perspective bar should be shown.  This is only 
	 * applicable if the window configurer also wishes either the perspective 
	 * bar to be visible.
	 * @since 3.0
	 */
	public boolean getPerspectiveBarVisible() {
		return perspectiveBarVisible;
	}	
	
	/**
	 * @param visible whether the perspective bar should be shown.  This is only 
	 * applicable if the window configurer also wishes either the perspective 
	 * bar to be visible.
	 * @since 3.0 
	 */	
	public void setStatusLineVisible(boolean visible) {
		boolean oldValue = statusLineVisible;
		statusLineVisible = visible;
		if (oldValue != statusLineVisible) {
			updateLayoutDataForContents();
		}
	}
	
	/** 
	 * @return whether the perspective bar should be shown.  This is only 
	 * applicable if the window configurer also wishes either the perspective 
	 * bar to be visible.
	 * @since 3.0
	 */
	public boolean getStatusLineVisible() {
		return statusLineVisible;
	}	
	/** 
	 * @since 3.0
	 */
	private void updateLayoutDataForContents() {
		// @issue this is not ideal; coolbar and perspective shortcuts should be
		//   separately configurable
		if ((getCoolBarVisible() && getWindowConfigurer().getShowCoolBar()) || (getPerspectiveBarVisible() && getWindowConfigurer().getShowPerspectiveBar())) {
			defaultLayout.addTrim(topBar, SWT.TOP, null);
			topBar.setVisible(true);
		}
		else {
			defaultLayout.removeTrim(topBar);
			topBar.setVisible(false);
		}

		if (getStatusLineVisible() && getWindowConfigurer().getShowStatusLine()) {
			defaultLayout.addTrim(getStatusLineManager().getControl(), SWT.BOTTOM, null);
			getStatusLineManager().getControl().setVisible(true);
		}
		else {
			defaultLayout.removeTrim(getStatusLineManager().getControl());
			getStatusLineManager().getControl().setVisible(false);
		}
		
		if (getWindowConfigurer().getShowProgressIndicator()) {
			if (progressRegion.getControl().getLayoutData() == null) {
				TrimLayoutData animationData = new TrimLayoutData(false, 
						progressRegion.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).x, 
						getStatusLineManager().getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
	
				progressRegion.getControl().setLayoutData(animationData);
			}
			defaultLayout.addTrim(progressRegion.getControl(), SWT.BOTTOM, null);
			progressRegion.getControl().setVisible(true);
		}
		else {
			if (progressRegion != null) {
				defaultLayout.removeTrim(progressRegion.getControl());
				progressRegion.getControl().setVisible(false);
			}
		}
		defaultLayout.setCenterControl(getPageComposite());	
	}
	
	/**
	 * Set the layout data for the contents of the window.
	 */
	private void setLayoutDataForContents() {
		updateLayoutDataForContents();
		
		if (getWindowConfigurer().getShowFastViewBars() && fastViewBar != null) {
			fastViewBar.addDockingListener(new IChangeListener() {
				public void update(boolean changed) {
					Control reference = null;
					int side = fastViewBar.getSide(); 
					
					fastViewBar.getControl().setLayoutData(new TrimLayoutData(side != SWT.BOTTOM, SWT.DEFAULT, SWT.DEFAULT));
					
					if (side == SWT.BOTTOM && getWindowConfigurer().getShowStatusLine()) {
						reference = getStatusLineManager().getControl();	
					}
					
					defaultLayout.addTrim(fastViewBar.getControl(), side, reference);
					WorkbenchPage page = getActiveWorkbenchPage(); 
					
					if (page != null) {
						Perspective persp = page.getActivePerspective();
						IViewReference activeFastView = persp.getActiveFastView(); 
						if (activeFastView != null) {
							persp.setActiveFastView(null);
							persp.setActiveFastView(activeFastView);
						}
					}
					
					getShell().layout();
				}
			});
		}
	}
	
	/**
	 * Returns the fast view bar.
	 */
	public FastViewBar getFastViewBar() {
		return fastViewBar;
	}
	
	/**
	 * Returns the perspective bar.
	 */
	public PerspectiveBarManager getPerspectiveBar() {
		return perspectiveBar;
	}

	public Object getAdapter(Class adapter) {
	    return null;
	}
	
//for dynamic UI
	protected ActionPresentation getActionPresentation() {
		return actionPresentation;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.ApplicationWindow#showTopSeperator()
	 */
	protected boolean showTopSeperator() {
		return false;
	}
	
	/**
	 * Close the floating window just for this instance.
	 */
	public void closeFloatingWindow(){
		progressRegion.getAnimationItem().closeFloatingWindow();
	}
	
    /**
     * Delegate to the presentation factory.
     * 
     * @see org.eclipse.jface.window.ApplicationWindow#createStatusLineManager
     * @since 3.0
     */
    protected StatusLineManager createStatusLineManager() {
        // @issue ApplicationWindow and WorkbenchWindow should allow full IStatusLineManager
        return (StatusLineManager) getWindowConfigurer().getPresentationFactory().createStatusLineManager();
    }
    
    /**
     * Delegate to the presentation factory.
     * 
     * @see org.eclipse.jface.window.ApplicationWindow#createStatusLine
     * @since 3.0
     */
    protected void createStatusLine(Shell shell) {
        getWindowConfigurer().getPresentationFactory().createStatusLineControl(getStatusLineManager(), shell);
    }

    /**
     * Updates the fast view bar, if present.
     * TODO: The fast view bar should update itself as necessary.
     * All calls to this should be cleaned up.
     * 
     * @since 3.0
     */
    public void updateFastViewBar() {
		if (getFastViewBar() != null) {
		    getFastViewBar().update(true);
		}
    }
}
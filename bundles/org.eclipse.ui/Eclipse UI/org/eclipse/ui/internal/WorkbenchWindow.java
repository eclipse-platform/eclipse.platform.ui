package org.eclipse.ui.internal;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
**********************************************************************/
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.*;
import org.eclipse.ui.help.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.ui.internal.misc.*;
import org.eclipse.ui.internal.model.WorkbenchAdapter;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.actions.OpenNewWindowMenu;
import org.eclipse.ui.actions.OpenNewPageMenu;
import org.eclipse.jface.action.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.window.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * A window within the workbench.
 */
public class WorkbenchWindow extends ApplicationWindow
	implements IWorkbenchWindow
{
	private int number;
	private Workbench workbench;
	private PageList pageList = new PageList();
	private PageListenerList pageListeners = new PageListenerList();
	private PerspectiveListenerListOld perspectiveListeners = new PerspectiveListenerListOld();
	private IPartDropListener partDropListener;
	private WWinPerspectiveService perspectiveService = new WWinPerspectiveService(this);
	private WWinKeyBindingService keyBindingService;
	private WWinPartService partService = new WWinPartService(this);
	private IMemento deferredRestoreState;
	private ActionPresentation actionPresentation;
	private WWinActionBars actionBars;
	private Label separator2;
	private Label separator3;
	private ToolBarManager shortcutBar;
	private ShortcutBarPart shortcutBarPart;
	private ShortcutBarPartDragDrop shortcutDND;
	private WorkbenchActionBuilder builder;
	private boolean updateDisabled = true;
	private boolean closing = false;
	private boolean shellActivated = false;
	private String workspaceLocation;
	private Menu shortcutBarMenu;
	// temporary, work in progress for CoolBars
	private CoolBarManager coolBarManager;
	
	final private String TAG_INPUT = "input";//$NON-NLS-1$
	final private String TAG_LAYOUT = "layout";//$NON-NLS-1$
	final private String TAG_FOCUS = "focus";//$NON-NLS-1$
	final private String TAG_FACTORY_ID = "factoryID";//$NON-NLS-1$
	final protected String GRP_PAGES = "pages";//$NON-NLS-1$
	final protected String GRP_PERSPECTIVES = "perspectives";//$NON-NLS-1$
	final protected String GRP_FAST_VIEWS = "fastViews";//$NON-NLS-1$

	// static fields for inner classes.
	static final int VGAP= 0;
	static final int CLIENT_INSET = 3;
	static final int BAR_SIZE = 23;

	/**
	 * The window toolbar must relayout whenever an update occurs, as items are
	 * added and removed dynamically.
	 */
	class WindowToolBarManager extends ToolBarManager {
		public WindowToolBarManager(int style) {
			super(style);
		}
		protected void relayout(ToolBar toolBar, int oldCount, int newCount) {
			Composite parent= toolBar.getParent();
			parent.layout();
		} 
	}      
	/**
	 * This vertical layout supports a fixed size Toolbar area, a separator line,
	 * the variable size content area,
	 * and a fixed size status line.
	 */
	class WorkbenchWindowLayout extends Layout {
	
		protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) 
		{
			if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT)
				return new Point(wHint, hHint);
				
			Point result= new Point(0, 0);
			Control[] ws= composite.getChildren();
			for (int i= 0; i < ws.length; i++) {
				Control w= ws[i];
				boolean skip = false;
				if (w == getToolBarControl()) {
					skip = true;
					result.y+= BAR_SIZE;  
				} else if (w == shortcutBar.getControl()) {
					skip = true;
				} 
				if (!skip) {
					Point e= w.computeSize(wHint, hHint, flushCache);
					result.x= Math.max(result.x, e.x);
					result.y+= e.y + VGAP;
				}
			}

			result.x += BAR_SIZE; // For shortcut bar.
			if (wHint != SWT.DEFAULT)
				result.x= wHint;
			if (hHint != SWT.DEFAULT)
				result.y= hHint;
			return result;
		}

		protected void layout(Composite composite, boolean flushCache) 
		{
			Rectangle clientArea= composite.getClientArea();
			// Loop through the children.  
			// Expected order == sep1, toolbar, status, sep2, shortcuts, sep3, client
			Control[] ws= composite.getChildren();
			for (int i= 0; i < ws.length; i++) {
				Control w= ws[i];
				if (i == 0 || w == separator2) { // Separators
					Point e= w.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
					w.setBounds(clientArea.x, clientArea.y, clientArea.width, e.y);
					clientArea.y+= e.y;
					clientArea.height-= e.y;
				} else if (w == getToolBarControl()) {
					int height = BAR_SIZE;
					if (toolBarChildrenExist()) { 
						Point e = w.computeSize(clientArea.width, SWT.DEFAULT, flushCache);
						height = e.y;
					}
					w.setBounds(clientArea.x, clientArea.y, clientArea.width, height);
					clientArea.y+= height;
					clientArea.height-= height;
				} else if (getStatusLineManager() != null && w == getStatusLineManager().getControl()) {
					int width = BAR_SIZE;
					if (shortcutBar != null) {
						Widget widget = shortcutBar.getControl();
						if (widget != null) {
							if (widget instanceof ToolBar) {
								ToolBar bar = (ToolBar) widget;
								if (bar.getItemCount() > 0) {
									ToolItem item = bar.getItem(0);
									width = item.getWidth();
									Rectangle trim = bar.computeTrim(0,0,width,width);
									width = trim.width + 2; // Add 2 pixels around shortcut bar
								}
							}
						}	
					}	
					Point e= w.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
					w.setBounds(clientArea.x + width, clientArea.y+clientArea.height-e.y, clientArea.width - width, e.y);
					clientArea.height-= e.y + VGAP;
				} else if (w == shortcutBar.getControl()) {
					int width = BAR_SIZE;
					if (w instanceof ToolBar) {
						ToolBar bar = (ToolBar) w;
						if (bar.getItemCount() > 0) {
							ToolItem item = bar.getItem(0);
							width = item.getWidth();
							Rectangle trim = bar.computeTrim(0,0,width,width);
							width = trim.width + 2; // Add 2 pixels around shortcut bar
						}
					}
					w.setBounds(clientArea.x, clientArea.y, width, clientArea.height);
					clientArea.x+= width + VGAP;
					clientArea.width-= width + VGAP;
				} else if (w == separator3) {
					Point e= w.computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
					w.setBounds(clientArea.x, clientArea.y, e.x, clientArea.height);
					clientArea.x+= e.x;
				} else {
					// Must be client.
					// Inset client area by 3 pixels 
					w.setBounds(clientArea.x + CLIENT_INSET, clientArea.y + CLIENT_INSET + VGAP, clientArea.width - ( 2 * CLIENT_INSET), clientArea.height - VGAP - (2 * CLIENT_INSET));
				}
			}
		}
	}
	
/**
 * WorkbenchWindow constructor comment.
 * @param workbench Workbench
 */
public WorkbenchWindow(Workbench workbench, int number) {
	super(null);
	this.workbench = workbench;
	this.number = number;
	
	// Setup window.
	addMenuBar();
	
	IPreferenceStore store = workbench.getPreferenceStore();
	if(store.getBoolean("ENABLE_COOL_BARS")) //$NON-NLS-1$
		addCoolBar(SWT.FLAT);
	else
		addToolBar(SWT.FLAT | SWT.WRAP);
		
	addStatusLine();
	addShortcutBar(SWT.FLAT | SWT.WRAP | SWT.VERTICAL);

	// Add actions.
	actionPresentation = new ActionPresentation(this);
	builder = new WorkbenchActionBuilder();
	builder.buildActions(this);
	
	// include the workspace location in the title 
	// if it is NOT the default location and the command line
	// option -showlocation is specified
	String[] args = Platform.getCommandLineArgs();
	boolean show = false;
	String location = null;
	for (int i = 0; i < args.length; i++) {
		if ("-showlocation".equals(args[i])) { //$NON-NLS-1$
			show = true;
		} else if ("-data".equals(args[i])) { //$NON-NLS-1$
			location = Platform.getLocation().toOSString();
		}
		if (show && location != null) {
			workspaceLocation = location;
			break;
		}
	}
	
	this.partDropListener = new IPartDropListener() {
		public void dragOver(PartDropEvent e) {
			WorkbenchPage page = getActiveWorkbenchPage();
			Perspective persp = page.getActivePerspective();
			PerspectivePresentation presentation = persp.getPresentation();
			presentation.onPartDragOver(e);
		};
		public void drop(PartDropEvent e) {
			WorkbenchPage page = getActiveWorkbenchPage();
			Perspective persp = page.getActivePerspective();
			PerspectivePresentation presentation = persp.getPresentation();
			presentation.onPartDrop(e);
		};
	};
}
/**
 * Configures this window to have a cool bar.
 * Does nothing if it already has one.
 * This method must be called before this window's shell is created.
 */
protected void addCoolBar(int style) {
	if ((getShell() == null) && (coolBarManager == null)) {
		coolBarManager = createCoolBarManager(style);
	}
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
/* package */ void addPerspectiveShortcut(Perspective perspective, WorkbenchPage page) {
	SetPagePerspectiveAction action = new SetPagePerspectiveAction(perspective, page);
	shortcutBar.appendToGroup(GRP_PERSPECTIVES, action);
	shortcutBar.update(false);
}
/**
 * Configures this window to have a shortcut bar.
 * Does nothing if it already has one.
 * This method must be called before this window's shell is created.
 */
protected void addShortcutBar(int style) {
	if ((getShell() == null) && (shortcutBar == null)) {
		shortcutBar = new ToolBarManager(style);
	}
}
/**
 * Close the window.
 * 
 * Assumes that busy cursor is active.
 */
private boolean busyClose() {
	// Only do the check if it is OK to close if we are not closing via the
	// workbench as the workbench will call this itself
	closing = true;
	updateDisabled = true;
	int count = workbench.getWorkbenchWindowCount();
	if (count <= 1 && !workbench.isClosing())
		return workbench.close();
	else {
		if (!okToClose())
			return false;
		return hardClose();
	}
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
	throws WorkbenchException 
{
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
	int r = super.open();
	workbench.fireWindowOpened(this);
	return r;
}
/**
 * @see IWorkbenchWindow
 */
public boolean close() {
	final boolean [] ret = new boolean[1];
	BusyIndicator.showWhile(null, new Runnable() {
		public void run() {
			ret[0] = busyClose();
		}
	});
	return ret[0];
}

private boolean isClosing() {
	return closing || workbench.isClosing();
}

/**
 * Close all of the pages.
 */
private void closeAllPages() 
{
	// Deactivate active page.
	setActivePage(null);

	// Clone and deref all so that calls to getPages() returns
	// empty list (if call by pageClosed event handlers)
	PageList oldList = pageList;
	pageList = new PageList();

	// Close all.
	Iterator enum = oldList.iterator();
	while (enum.hasNext()) {
		WorkbenchPage page = (WorkbenchPage)enum.next();
		firePageClosed(page);
		page.dispose();
	}
}
/**
 * Save and close all of the pages.
 */
public void closeAllPages(boolean save) {
	if (save) {
		boolean ret = saveAllPages(true);
		if (!ret) return;
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
	WorkbenchPage oldPage = (WorkbenchPage)in;

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

	return true;
}
/**
 * Sets the ApplicationWindows's content layout.
 * This vertical layout supports a fixed size Toolbar area, a separator line,
 * the variable size content area,
 * and a fixed size status line.
 */
protected void configureShell(Shell shell) {
	super.configureShell(shell);
	shell.setLayout(new WorkbenchWindowLayout());
	shell.setSize(800, 600);
	separator2 = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
	createShortcutBar(shell);
	separator3 = new Label(shell, SWT.SEPARATOR | SWT.VERTICAL);

	WorkbenchHelp.setHelp(shell, IHelpContextIds.WORKBENCH_WINDOW);

	trackShellActivation(shell);
	
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
						ToolBar toolBar = (ToolBar)event.widget;
						if (toolBar.getItem(pt) != null)
							return;
					}
					currentPage.toggleFastView(null);
				}
			}
		}
	};
	getToolBarControl().addListener(SWT.MouseDown, listener);
	Control[] children = ((Composite)getStatusLineManager().getControl()).getChildren();
	for (int i = 0; i < children.length; i++) {
		if (children[i] != null)
			children[i].addListener(SWT.MouseDown, listener);
	}
	getShortcutBar().getControl().addListener(SWT.MouseDown, listener);
}
/**
 * Create the shortcut toolbar control
 */
private void createShortcutBar(Shell shell) {
	// Create control.
	if (shortcutBar == null)
		return;
	shortcutBar.createControl(shell);

	// Define shortcut part.  This is for drag and drop.
	shortcutBarPart = new ShortcutBarPart(shortcutBar);
	
	// Add right mouse button support.
	ToolBar tb = shortcutBar.getControl();
	tb.addMouseListener(new MouseAdapter() {
		public void mouseDown(MouseEvent e) {
			if (e.button == 3)
				showShortcutBarPopup(e);
		}
	});
}
/* (non-Javadoc)
 * Method declared on ApplicationWindow.
 */

protected MenuManager createMenuManager() {
	final MenuManager result = super.createMenuManager();
	result.setOverrides(new IContributionManagerOverrides() {
		public Integer getAccelerator(IContributionItem item) {
			if(getKeyBindingService().acceleratorsAllowed())
				return null;
			return new Integer(0);
		}
		public String getAcceleratorText(IContributionItem item) {
			if(getKeyBindingService().acceleratorsAllowed())
				return null;
			if(!(item instanceof ActionContributionItem))
				return null;
			ActionContributionItem aci = (ActionContributionItem)item;
			String defId = aci.getAction().getActionDefinitionId();
			if(defId == null)
				return null;
			return getKeyBindingService().getAcceleratorText(defId);
		}
		public String getText(IContributionItem item) {
			if(getKeyBindingService().acceleratorsAllowed())
				return null;
			if(!(item instanceof MenuManager))
				return null;
			MenuManager itemManager = (MenuManager)item;
			if(itemManager.getParent() != result)
				return null;		
			String text = itemManager.getMenu().getParentItem().getText();
			int index = text.indexOf('&');
			if (index < 0)
				return null;
			else if (index == 0)
				return text.substring(1);
			else
				return text.substring(0, index) + text.substring(index + 1);
		}
		public Boolean getEnabled(IContributionItem item) {
			return null;
		}
	});
	return result;
}
/* (non-Javadoc)
 * Method declared on ApplicationWindow.
 */
protected ToolBarManager createToolBarManager(int style) {
	return new WindowToolBarManager(style);
}
/**
 * Creates the cool bar manager.
 * </p>
 * @return a CoolBarManager
 */
protected CoolBarManager createCoolBarManager(int style) {
	return new CoolBarManager(style);
}
/**
 * Creates the control for the tool bar manager.  Overridden
 * to support CoolBars.
 * </p>
 * @return a Control
 */
protected Control createToolBarControl(Shell shell) {
	IToolBarManager manager = getToolsManager();
	if (manager instanceof ToolBarManager) {
		return ((ToolBarManager)manager).createControl(shell);
	} else if (manager instanceof CoolBarManager) {
		return ((CoolBarManager)manager).createControl(shell);
	}
	return null;
}
/**
 * Enables fast view icons to be dragged and dropped using the given IPartDropListener.
 */
/*package*/ void enableDragShortcutBarPart() {
	Control control = shortcutBarPart.getControl();
	if (control != null && shortcutDND == null) {
		// Only one ShortcutBarPartDragDrop per WorkbenchWindow.
		shortcutDND = new ShortcutBarPartDragDrop(shortcutBarPart, control);
		// Add the listener only once.
		shortcutDND.addDropListener(partDropListener);
	}
}
/**
 * Returns the shortcut for a page.
 */
/* protected */ IContributionItem findPerspectiveShortcut(Perspective perspective, WorkbenchPage page) {
	IContributionItem[] array = shortcutBar.getItems();
	int length = array.length;
	for (int i = 0; i < length; i++) {
		IContributionItem item = array[i];
		if (item instanceof ActionContributionItem) {
			IAction action = ((ActionContributionItem)item).getAction();
			if (action instanceof SetPagePerspectiveAction) {
				SetPagePerspectiveAction sp = (SetPagePerspectiveAction)action;
				if (sp.handles(perspective, page))
					return item;	
			}
		}
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
void firePerspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
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
public IActionBars getActionBars() {
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
/* package */ WorkbenchPage getActiveWorkbenchPage() {
	return pageList.getActive();
}
/**
 * Get the workbench client area.
 */
protected Composite getClientComposite() {
	return (Composite)getContents();
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
 * Returns the key binding service in use.
 * 
 * @return the key binding service in use.
 * @since 2.0
 */
public WWinKeyBindingService getKeyBindingService() {
	if (keyBindingService == null) {
		keyBindingService = new WWinKeyBindingService(this);
		updateActiveActions();
	}
	return keyBindingService;	
}
/**
 * Re-register the action sets actions in the keybinding service.
 */
private void updateActiveActions() {
	if(keyBindingService == null) {
		getKeyBindingService();
	} else {
		IActionSet sets[] = actionPresentation.getActionSets();
		keyBindingService.registerActionSets(sets);
	}
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
 * Returns the shortcut bar.
 */
public ToolBarManager getShortcutBar() {
	return shortcutBar;
}
/**
 * Returns the PartDragDrop for the shortcut bar part.
 */
/*package*/ShortcutBarPartDragDrop getShortcutDND() {
	return shortcutDND;	
}
/**
 * Returns the status line manager for this window (if it has one).
 *
 *
 * @return the status line manager, or <code>null</code> if
 *   this window does not have a status line
 * @see #addStatusLine
 */
protected StatusLineManager getStatusLineManager() {
	return super.getStatusLineManager();
}
/**
 * Returns tool bar control for the window. Overridden
 * to support CoolBars.
 * </p>
 * @return a Control
 */
protected Control getToolBarControl() {
	IToolBarManager manager = getToolsManager();
	if (manager instanceof ToolBarManager) {
		return ((ToolBarManager)manager).getControl();
	}
	if (manager instanceof CoolBarManager) {
		return ((CoolBarManager)manager).getControl();
	}
	return null;
}
/**
 * Returns the tool bar manager for this window (if it has one).
 * Introduced to support different types of IToolBarManagers.
 * 
 * @return the IToolBarManager, or <code>null</code> if
 *   this window does not have a ToolBar or CoolBar.
 * @see #addToolBar, #addCoolBar
 */
public IToolBarManager getToolsManager() {
	if (coolBarManager != null) return coolBarManager;
	return getToolBarManager();
}
/**
 * @see IWorkbenchWindow
 */
public IWorkbench getWorkbench() {
	return workbench;
}
/**
 * Unconditionally close this window.
 */
private boolean hardClose() {
	try {
		closing = true;
		updateDisabled = true;
		closeAllPages();
		builder.dispose();
		if(keyBindingService != null)
			keyBindingService.dispose();
		workbench.fireWindowClosed(this);
	} finally {
		return super.close();
	}
}
/**
 * @see IWorkbenchWindow
 */
public boolean isApplicationMenu(String menuID) {
	return WorkbenchActionBuilder.isContainerMenu(menuID);
}
/**
 * Called when this window is about to be closed.
 *
 * Subclasses may overide to add code that returns <code>false</code> 
 * to prevent closing under certain conditions.
 */
public boolean okToClose() {
	// Save all of the editors.
	if(!workbench.isClosing())
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
	throws WorkbenchException 
{
	Assert.isNotNull(perspId);
	
	// Run op in busy cursor.
	final Object [] result = new Object[1];
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
		return (IWorkbenchPage)result[0];
	else if (result[0] instanceof WorkbenchException)
		throw (WorkbenchException)result[0];
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
public IWorkbenchPage openPage(IAdaptable input)
	throws WorkbenchException 
{
	String perspId = workbench.getPerspectiveRegistry().getDefaultPerspective();
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
/* package */ void removePerspectiveShortcut(Perspective perspective, WorkbenchPage page) {
	IContributionItem item = findPerspectiveShortcut(perspective, page);
	if (item != null) {
		shortcutBar.remove(item);
		shortcutBar.update(false);
	}
}
/**
 * @see IPersistable.
 */
public void restoreState(IMemento memento) {
	Assert.isNotNull(getShell());
	
	// Read the bounds.
	if("true".equals(memento.getString("maximized"))) {//$NON-NLS-2$//$NON-NLS-1$
		getShell().setMaximized(true);
	} else {
		Integer bigInt;
		bigInt = memento.getInteger(IWorkbenchConstants.TAG_X);
		int x = bigInt.intValue();
		bigInt = memento.getInteger(IWorkbenchConstants.TAG_Y);
		int y = bigInt.intValue();
		bigInt = memento.getInteger(IWorkbenchConstants.TAG_WIDTH);
		int width = bigInt.intValue();
		bigInt = memento.getInteger(IWorkbenchConstants.TAG_HEIGHT);
		int height = bigInt.intValue();
		// Set the bounds.
		getShell().setBounds(x, y, width, height);
	}

	// Recreate each page in the window. 
	IWorkbenchPage newActivePage = null;
	IMemento [] pageArray = memento.getChildren(IWorkbenchConstants.TAG_PAGE);
	for (int i = 0; i < pageArray.length; i ++) {
		IMemento pageMem = pageArray[i];

		// Get the input factory.
		IMemento inputMem = pageMem.getChild(IWorkbenchConstants.TAG_INPUT);
		String factoryID = inputMem.getString(IWorkbenchConstants.TAG_FACTORY_ID);
		if (factoryID == null) {
			WorkbenchPlugin.log("Unable to restore page - no input factory ID.");//$NON-NLS-1$
			continue;
		}
		IElementFactory factory = WorkbenchPlugin.getDefault().getElementFactory(factoryID);
		if (factory == null) {
			WorkbenchPlugin.log("Unable to restore pagee - cannot instantiate input factory: " + factoryID);//$NON-NLS-1$
			continue;
		}
			
		// Get the input element.
		IAdaptable input = factory.createElement(inputMem);
		if (input == null) {
			WorkbenchPlugin.log("Unable to restore page - cannot instantiate input element: " + factoryID);//$NON-NLS-1$
			continue;
		}

		// Open the perspective.
		WorkbenchPage result = null;
		try {
			result = new WorkbenchPage(this, pageMem, input);
			pageList.add(result);
			pageListeners.firePageOpened(result);
		} catch (WorkbenchException e) {
			WorkbenchPlugin.log("Unable to restore perspective - constructor failed.");//$NON-NLS-1$
			continue;
		}

		// Check for focus.
		String strFocus = pageMem.getString(IWorkbenchConstants.TAG_FOCUS);
		if (strFocus != null && strFocus.length() > 0)
			newActivePage = result;
	}

	// If there are no pages create a default.
	if (pageList.isEmpty()) {
		try {
			IContainer root = WorkbenchPlugin.getPluginWorkspace().getRoot();
			String defPerspID = workbench.getPerspectiveRegistry().getDefaultPerspective();
			WorkbenchPage result = new WorkbenchPage(this, defPerspID, root);
			pageList.add(result);
			pageListeners.firePageOpened(result);
		} catch (WorkbenchException e) {
			WorkbenchPlugin.log("Unable to create default perspective - constructor failed.");//$NON-NLS-1$
			getShell().setText(workbench.getProductInfo().getName());
			return;
		}
	}
		
	// Set active page.
	if (newActivePage == null)
		newActivePage = (IWorkbenchPage)pageList.getNextActive();
	setActivePage(newActivePage);
}
/**
 * Save all of the pages.  Returns true if the operation succeeded.
 */
private boolean saveAllPages(boolean bConfirm) 
{
	boolean bRet = true;
	Iterator enum = pageList.iterator();
	while (bRet && enum.hasNext()) {
		WorkbenchPage page = (WorkbenchPage)enum.next();
		bRet = page.saveAllEditors(bConfirm);
	}
	return bRet;
}
/**
 * @see IPersistable
 */
public void saveState(IMemento memento) {
	
	// Save the bounds.
	if(getShell().getMaximized()) {
		memento.putString("maximized","true");//$NON-NLS-2$//$NON-NLS-1$
	} else {
		Rectangle bounds = getShell().getBounds();
		memento.putInteger(IWorkbenchConstants.TAG_X, bounds.x);
		memento.putInteger(IWorkbenchConstants.TAG_Y, bounds.y);
		memento.putInteger(IWorkbenchConstants.TAG_WIDTH, bounds.width);
		memento.putInteger(IWorkbenchConstants.TAG_HEIGHT, bounds.height);
	}

	// Save each page.
	Iterator enum = pageList.iterator();
	while (enum.hasNext()) 
	{
		WorkbenchPage page = (WorkbenchPage)enum.next();
		
		// Get the input.
		IAdaptable input = page.getInput();
		if (input == null) {
			WorkbenchPlugin.log("Unable to save page input: " + page);//$NON-NLS-1$
			continue;
		}
		IPersistableElement persistable = (IPersistableElement)input.getAdapter(IPersistableElement.class);
		if (persistable == null) {
			WorkbenchPlugin.log("Unable to save page input: " + input);//$NON-NLS-1$
			continue;
		}

		// Save perspective.
		IMemento pageMem = memento.createChild(IWorkbenchConstants.TAG_PAGE);
		page.saveState(pageMem);
		
		if (page == getActiveWorkbenchPage()) {
			pageMem.putString(IWorkbenchConstants.TAG_FOCUS, "true");//$NON-NLS-1$
		}
		
		// Save input.
		IMemento inputMem = pageMem.createChild(IWorkbenchConstants.TAG_INPUT);
		inputMem.putString(TAG_FACTORY_ID, persistable.getFactoryId());
		persistable.saveState(inputMem);
	}
}
/**
 * Select the shortcut for a perspective.
 */
/* package */ void selectPerspectiveShortcut(Perspective perspective, WorkbenchPage page, boolean selected) {
	IContributionItem item = findPerspectiveShortcut(perspective, page);
	if (item != null) {
		IAction action = ((ActionContributionItem)item).getAction();
		action.setChecked(selected);
	}
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

			if(isClosing())
				return;
				
			updateDisabled = false;
					
			// Update action bars ( implicitly calls updateActionBars() )
			updateTitle();
			updateActionSets();
			shortcutBar.update(false);
		}
	});
}
/**
 * Shows the popup menu for a page item in the shortcut bar.
 */
private void showShortcutBarPopup(MouseEvent e) {
	// Get the tool item under the mouse.
	Point pt = new Point(e.x, e.y);
	ToolBar toolBar = shortcutBar.getControl();
	ToolItem toolItem = toolBar.getItem(pt);
	if (toolItem == null)
		return;

	// Get the action for the tool item.
	Object data = toolItem.getData();
	if (!(data instanceof ActionContributionItem))
		return;
	IAction action = ((ActionContributionItem) data).getAction();
	if (action instanceof SetPagePerspectiveAction) {
		// The shortcut bar menu is created lazily here.
		// Its data is set (each time) to the tool item, which refers to the SetPagePerspectiveAction
		// which in turn refers to the page and perspective.
		// It is important not to refer to the action, the page or the perspective directly
		// since otherwise the menu hangs on to them after they are closed.
		// By hanging onto the tool item instead, these references are cleared when the
		// corresponding page or perspective is closed.
		// See bug 11282 for more details on why it is done this way.
		if (shortcutBarMenu == null) {
			Menu menu = new Menu(toolBar);
			MenuItem menuItem = new MenuItem(menu, SWT.NONE);
			menuItem.setText(WorkbenchMessages.getString("WorkbenchWindow.close")); //$NON-NLS-1$
			menuItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					ToolItem toolItem = (ToolItem) shortcutBarMenu.getData();
					if (toolItem != null && !toolItem.isDisposed()) {
						ActionContributionItem item = (ActionContributionItem) toolItem.getData();
						SetPagePerspectiveAction action = (SetPagePerspectiveAction) item.getAction();
						action.getPage().closePerspective(action.getPerspective(), true);
					}
				}
			});
			menuItem = new MenuItem(menu, SWT.NONE);
			menuItem.setText(WorkbenchMessages.getString("WorkbenchWindow.closeAll")); //$NON-NLS-1$
			menuItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					ToolItem toolItem = (ToolItem) shortcutBarMenu.getData();
					if (toolItem != null && !toolItem.isDisposed()) {
						ActionContributionItem item = (ActionContributionItem) toolItem.getData();
						SetPagePerspectiveAction action = (SetPagePerspectiveAction) item.getAction();
						action.getPage().closeAllPerspectives(true);
					}
				}
			});
			shortcutBarMenu = menu;
		}
		shortcutBarMenu.setData(toolItem);
	}
	
	
	// Show popup menu.
	if (shortcutBarMenu != null) {
		pt = toolBar.toDisplay(pt);
		shortcutBarMenu.setLocation(pt.x, pt.y);
		shortcutBarMenu.setVisible(true);
	}
}
/**
 * Answer whether or not children exist for the Application WIndow's
 * toolbar control.  Overridden to support CoolBars.
 * <p>
 * @return boolean true if children exist, false otherwise
 */
protected boolean toolBarChildrenExist() {
	Control toolControl = getToolBarControl();
	if (toolControl instanceof ToolBar) {
		return ((ToolBar)toolControl).getItemCount() > 0;
	}
	if (toolControl instanceof CoolBar) {
		return ((CoolBar)toolControl).getItemCount() > 0;
	}
	return false;
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
				workbench.fireWindowActivated(WorkbenchWindow.this);
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
				workbench.fireWindowDeactivated(WorkbenchWindow.this);
			}
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
	getToolsManager().update(false);
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
	else
		actionPresentation.setActionSets(currentPage.getActionSets());
	updateActionBars();

	// hide the launch menu if it is empty
	String path = IWorkbenchActionConstants.M_WINDOW + IWorkbenchActionConstants.SEP + IWorkbenchActionConstants.M_LAUNCH;
	IMenuManager manager = getMenuBarManager().findMenuUsingPath(path);
	IContributionItem item = getMenuBarManager().findUsingPath(path);
	updateActiveActions();
	if (manager == null || item == null)
		return;
	item.setVisible(manager.getItems().length >= 2);  // there is a separator for the additions group thus >= 2
}
/**
 * Updates the shorcut item
 */
/* package */ void updatePerspectiveShortcut(Perspective perspective, WorkbenchPage page) {
	if(updateDisabled)
		return;
		
	IContributionItem item = findPerspectiveShortcut(perspective, page);
	if (item != null) {
		SetPagePerspectiveAction action = (SetPagePerspectiveAction)((ActionContributionItem)item).getAction();
		action.update();
		if (page == getActiveWorkbenchPage())
			updateTitle();
	}
}
/**
 * Updates the window title.
 */
public void updateTitle() {
	if(updateDisabled)
		return;
		
	String title = workbench.getProductInfo().getName();
	if (workspaceLocation != null)
		title = WorkbenchMessages.format("WorkbenchWindow.shellTitle", new Object[] {title, workspaceLocation}); //$NON-NLS-1$
	
	WorkbenchPage currentPage = getActiveWorkbenchPage();
	if (currentPage != null) {
		IPerspectiveDescriptor persp = currentPage.getPerspective();
		String label = ""; //$NON-NLS-1$
		if (persp != null)
			label = persp.getLabel();
		IAdaptable input = currentPage.getInput();
		if((input != null) && (!input.equals(ResourcesPlugin.getWorkspace().getRoot())))
			label = currentPage.getLabel();
		title = WorkbenchMessages.format("WorkbenchWindow.shellTitle", new Object[] {label, title}); //$NON-NLS-1$
	}
	getShell().setText(title);	
}

class PageList {
	//List of pages in the order they were created;
	private List pageList;
	//List of pages where the top is the last activated.
 	private List pageStack;
 	// The page explicitly activated
 	private Object active;
 	
	public PageList() {
		pageList = new ArrayList(4);
 		pageStack = new ArrayList(4);
	}
	public boolean add(Object object) {
		pageList.add(object);
		pageStack.add(0,object); //It will be moved to top only when activated.
		return true;
	}
	public Iterator iterator() {
		return pageList.iterator();
	}
	public boolean contains(Object object) {
		return pageList.contains(object);
	}
	public boolean remove(Object object) {
		if (active == object)
			active = null;
		pageStack.remove(object);
		return pageList.remove(object);
	}
	public boolean isEmpty() {
		return pageList.isEmpty();
	}
	public IWorkbenchPage[] getPages() {
		int nSize = pageList.size();
		IWorkbenchPage [] retArray = new IWorkbenchPage[nSize];
		pageList.toArray(retArray);
		return retArray;
	}
	public void setActive(Object page) {
		if (active == page)
			return;

		active = page;
	
		if (page != null) {
			pageStack.remove(page);
			pageStack.add(page);
		}
	}
	public WorkbenchPage getActive() {
		return (WorkbenchPage) active;
	}
	public WorkbenchPage getNextActive() {
		if (active == null) {
			if (pageStack.isEmpty())
				return null;
			else
				return (WorkbenchPage)pageStack.get(pageStack.size() - 1);
		} else {
			if (pageStack.size() < 2)
				return null;
			else
				return (WorkbenchPage)pageStack.get(pageStack.size() - 2);
		}
	}
}
}

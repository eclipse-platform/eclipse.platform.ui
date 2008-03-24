/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.internal.provisional.action.ICoolBarManager2;
import org.eclipse.jface.internal.provisional.action.IToolBarContributionItem;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CBanner;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ActiveShellExpression;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IPersistable;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.contexts.IWorkbenchContextSupport;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.StartupThreading.StartupRunnable;
import org.eclipse.ui.internal.actions.CommandAction;
import org.eclipse.ui.internal.contexts.ContextAuthority;
import org.eclipse.ui.internal.dialogs.CustomizePerspectiveDialog;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.dnd.SwtUtil;
import org.eclipse.ui.internal.handlers.ActionCommandMappingService;
import org.eclipse.ui.internal.handlers.IActionCommandMappingService;
import org.eclipse.ui.internal.intro.IIntroConstants;
import org.eclipse.ui.internal.layout.CacheWrapper;
import org.eclipse.ui.internal.layout.ITrimManager;
import org.eclipse.ui.internal.layout.IWindowTrim;
import org.eclipse.ui.internal.layout.LayoutUtil;
import org.eclipse.ui.internal.layout.TrimLayout;
import org.eclipse.ui.internal.menus.IActionSetsListener;
import org.eclipse.ui.internal.menus.LegacyActionPersistence;
import org.eclipse.ui.internal.menus.TrimBarManager2;
import org.eclipse.ui.internal.menus.TrimContributionManager;
import org.eclipse.ui.internal.misc.Policy;
import org.eclipse.ui.internal.misc.UIListenerLogging;
import org.eclipse.ui.internal.misc.UIStats;
import org.eclipse.ui.internal.presentations.DefaultActionBarPresentationFactory;
import org.eclipse.ui.internal.progress.ProgressRegion;
import org.eclipse.ui.internal.provisional.application.IActionBarConfigurer2;
import org.eclipse.ui.internal.provisional.presentations.IActionBarPresentationFactory;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.UIExtensionTracker;
import org.eclipse.ui.internal.services.IServiceLocatorCreator;
import org.eclipse.ui.internal.services.ServiceLocator;
import org.eclipse.ui.internal.tweaklets.Tweaklets;
import org.eclipse.ui.internal.tweaklets.WorkbenchImplementation;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.menus.MenuUtil;
import org.eclipse.ui.presentations.AbstractPresentationFactory;

/**
 * A window within the workbench.
 */
public class WorkbenchWindow extends ApplicationWindow implements
		IWorkbenchWindow {

	private WorkbenchWindowAdvisor windowAdvisor;

	private ActionBarAdvisor actionBarAdvisor;

	private int number;

	private PageList pageList = new PageList();

	private PageListenerList pageListeners = new PageListenerList();

	private PerspectiveListenerList perspectiveListeners = new PerspectiveListenerList();

	private WWinPartService partService = new WWinPartService(this);

	private ActionPresentation actionPresentation;

	private WWinActionBars actionBars;

	private boolean updateDisabled = true;

	private boolean closing = false;

	private boolean shellActivated = false;

	private FastViewBar fastViewBar;

	private PerspectiveSwitcher perspectiveSwitcher = null;

	private TrimLayout defaultLayout;

	ProgressRegion progressRegion = null;

	// Legacy (3.2) contribution handling
	private TrimBarManager2 trimMgr2 = null;
	
	// 3.3 Trim Contribution handling
	private TrimContributionManager trimContributionMgr = null;
	
	/**
	 * The map of services maintained by the workbench window. These services
	 * are initialized during workbench window during the
	 * {@link #configureShell(Shell)}.
	 */
	private final ServiceLocator serviceLocator;

	private HeapStatus heapStatus;

	private WindowTrimProxy heapStatusTrim = null;

	private boolean emptyWindowContentsCreated = false;

	private Control emptyWindowContents;

	private Rectangle normalBounds;

	private boolean asMaximizedState = false;

	private CBanner topBar;

	private IWindowTrim topBarTrim;

	// Previous shell size. Used to prevent the CBanner from triggering
	// redundant layouts
	private Point lastShellSize = new Point(0, 0);

	/**
	 * The composite under which workbench pages create their controls.
	 * 
	 * @since 3.0
	 */
	private Composite pageComposite;

	/**
	 * Bit flags indication which submenus (New, Show Views, ...) this window
	 * contains. Initially none.
	 * 
	 * @since 3.0
	 */
	private int submenus = 0x00;

	/**
	 * Object for configuring this workbench window. Lazily initialized to an
	 * instance unique to this window.
	 * 
	 * @since 3.0
	 */
	private WorkbenchWindowConfigurer windowConfigurer = null;
	
	/**
	 * List of generic property listeners.
	 * 
	 * @since 3.3
	 */
	private ListenerList genericPropertyListeners = new ListenerList();

	private ShellPool detachedWindowShells;
	
	static final String TEXT_DELIMITERS = TextProcessor.getDefaultDelimiters() + "-"; //$NON-NLS-1$

	// constants for shortcut bar group ids
	static final String GRP_PAGES = "pages"; //$NON-NLS-1$

	static final String GRP_PERSPECTIVES = "perspectives"; //$NON-NLS-1$

	static final String GRP_FAST_VIEWS = "fastViews"; //$NON-NLS-1$

	// static fields for inner classes.
	static final int VGAP = 0;

	static final int CLIENT_INSET = 3;

	static final int BAR_SIZE = 23;

	/**
	 * Coolbar visibility change property.
	 * 
	 * @since 3.3
	 */
	public static final String PROP_COOLBAR_VISIBLE = "coolbarVisible"; //$NON-NLS-1$
	
	/**
	 * Perspective bar visibility change property.
	 * 
	 * @since 3.3
	 */
	public static final String PROP_PERSPECTIVEBAR_VISIBLE = "perspectiveBarVisible"; //$NON-NLS-1$
	
	/**
	 * Constant (bit mask) indicating which the Show View submenu is probably
	 * present somewhere in this window.
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
	 * Constant (bit mask) indicating which the New Wizard submenu is probably
	 * present somewhere in this window.
	 * 
	 * @see #addSubmenu
	 * @since 3.0
	 */
	public static final int NEW_WIZARD_SUBMENU = 0x04;

	/**
	 * Remembers that this window contains the given submenu.
	 * 
	 * @param type
	 *            the type of submenu, one of:
	 *            {@link #NEW_WIZARD_SUBMENU NEW_WIZARD_SUBMENU},
	 *            {@link #OPEN_PERSPECTIVE_SUBMENU OPEN_PERSPECTIVE_SUBMENU},
	 *            {@link #SHOW_VIEW_SUBMENU SHOW_VIEW_SUBMENU}
	 * @see #containsSubmenu
	 * @since 3.0
	 */
	public void addSubmenu(int type) {
		submenus |= type;
	}

	/**
	 * Checks to see if this window contains the given type of submenu.
	 * 
	 * @param type
	 *            the type of submenu, one of:
	 *            {@link #NEW_WIZARD_SUBMENU NEW_WIZARD_SUBMENU},
	 *            {@link #OPEN_PERSPECTIVE_SUBMENU OPEN_PERSPECTIVE_SUBMENU},
	 *            {@link #SHOW_VIEW_SUBMENU SHOW_VIEW_SUBMENU}
	 * @return <code>true</code> if window contains submenu,
	 *         <code>false</code> otherwise
	 * @see #addSubmenu
	 * @since 3.0
	 */
	public boolean containsSubmenu(int type) {
		return ((submenus & type) != 0);
	}

	/**
	 * Constant indicating that all the actions bars should be filled.
	 * 
	 * @since 3.0
	 */
	private static final int FILL_ALL_ACTION_BARS = ActionBarAdvisor.FILL_MENU_BAR
			| ActionBarAdvisor.FILL_COOL_BAR
			| ActionBarAdvisor.FILL_STATUS_LINE;

	/**
	 * Creates and initializes a new workbench window.
	 * 
	 * @param number
	 *            the number for the window
	 */
	public WorkbenchWindow(int number) {
		super(null);
		this.number = number;

		// Make sure there is a workbench. This call will throw
		// an exception if workbench not created yet.
		final IWorkbench workbench = PlatformUI.getWorkbench();
		IServiceLocatorCreator slc = (IServiceLocatorCreator) workbench
				.getService(IServiceLocatorCreator.class);
		this.serviceLocator = (ServiceLocator) slc
				.createServiceLocator(workbench, null);
		initializeDefaultServices();

		// Add contribution managers that are exposed to other plugins.
		addMenuBar();
		addCoolBar(SWT.NONE);  // style is unused
		addStatusLine();

		// register with the tracker
		getExtensionTracker()
				.registerHandler(
						actionSetHandler,
						ExtensionTracker
								.createExtensionPointFilter(getActionSetExtensionPoint()));

		fireWindowOpening();

		// set the shell style
		setShellStyle(getWindowConfigurer().getShellStyle());

		// Fill the action bars
		fillActionBars(FILL_ALL_ACTION_BARS);
	}

	/**
	 * Return the action set extension point.
	 * 
	 * @return the action set extension point
	 * @since 3.1
	 */
	private IExtensionPoint getActionSetExtensionPoint() {
		return Platform.getExtensionRegistry().getExtensionPoint(
				PlatformUI.PLUGIN_ID, IWorkbenchRegistryConstants.PL_ACTION_SETS);
	}

	/**
	 * Return the style bits for the shortcut bar.
	 * 
	 * @return int
	 */
	protected int perspectiveBarStyle() {
		return SWT.FLAT | SWT.WRAP | SWT.RIGHT | SWT.HORIZONTAL;
	}

	private TrimDropTarget trimDropTarget;

	private boolean coolBarVisible = true;

	private boolean perspectiveBarVisible = true;
	
	private boolean fastViewBarVisible = true;

	private boolean statusLineVisible = true;

	private IWindowTrim statusLineTrim = null;

	/**
	 * The handlers for global actions that were last submitted to the workbench
	 * command support. This is a map of command identifiers to
	 * <code>ActionHandler</code>. This map is never <code>null</code>,
	 * and is never empty as long as at least one global action has been
	 * registered.
	 */
	private Map globalActionHandlersByCommandId = new HashMap();

	/**
	 * The list of handler submissions submitted to the workbench command
	 * support. This list may be empty, but it is never <code>null</code>.
	 */
	private List handlerActivations = new ArrayList();

	/**
	 * The number of large updates that are currently going on. If this is
	 * number is greater than zero, then UI updateActionBars is a no-op.
	 * 
	 * @since 3.1
	 */
	private int largeUpdates = 0;

	private IExtensionTracker tracker;

	private IExtensionChangeHandler actionSetHandler = new IExtensionChangeHandler() {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler#addExtension(org.eclipse.core.runtime.dynamichelpers.IExtensionTracker,
		 *      org.eclipse.core.runtime.IExtension)
		 */
		public void addExtension(IExtensionTracker tracker, IExtension extension) {
			// this assumes that the workbench-level tracker will have already
			// updated the registry

			ArrayList setsToActivate = new ArrayList();
			// look for all new sets that are on by default. Examine the tracker
			// at the workbench level to see what descriptors are registered
			// against this extension
			Object[] registeredObjects = getWorkbench().getExtensionTracker()
					.getObjects(extension);
			for (int i = 0; i < registeredObjects.length; i++) {
				if (registeredObjects[i] instanceof IActionSetDescriptor) {
					IActionSetDescriptor desc = (IActionSetDescriptor) registeredObjects[i];
					if (desc.isInitiallyVisible()) {
						setsToActivate.add(desc);
					}
				}
			}

			// if none of the new sets are marked as initially visible, abort.
			if (setsToActivate.isEmpty()) {
				return;
			}

			IActionSetDescriptor[] descriptors = (IActionSetDescriptor[]) setsToActivate
					.toArray(new IActionSetDescriptor[setsToActivate.size()]);

			WorkbenchPage page = getActiveWorkbenchPage();
			if (page != null) {
				Perspective[] perspectives = page.getOpenInternalPerspectives();

				for (int i = 0; i < perspectives.length; i++) {
					perspectives[i].turnOnActionSets(descriptors);
				}
			}

			updateActionSets();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler#removeExtension(org.eclipse.core.runtime.IExtension,
		 *      java.lang.Object[])
		 */
		public void removeExtension(IExtension extension, Object[] objects) {
			// remove the contributions from the window bars and dispose of the
			// actions
			for (int i = 0; i < objects.length; i++) {
				if (objects[i] instanceof PluginActionSetBuilder.Binding) {
					PluginActionSetBuilder.Binding binding = (PluginActionSetBuilder.Binding) objects[i];
					binding.builder.removeActionExtensions(binding.set,
							binding.window);
					binding.set.dispose();
				}
			}

			// update all opened perspectives
			Perspective[] perspectives = getActiveWorkbenchPage()
					.getOpenInternalPerspectives();
			boolean updateNeeded = true;
			IContextService contextService = (IContextService) getService(IContextService.class);
			try {
				contextService.activateContext(ContextAuthority.DEFER_EVENTS);
				
				for (int i = 0; i < perspectives.length; i++) {
					for (int j = 0; j < objects.length; j++) {
						if (objects[j] instanceof IActionSetDescriptor) {
							perspectives[i]
									.removeActionSet((IActionSetDescriptor) objects[j]);
							getActionPresentation().removeActionSet(
									(IActionSetDescriptor) objects[j]);
						}
					}
				}
			} finally {
				contextService.activateContext(ContextAuthority.SEND_EVENTS);
			}

			if (updateNeeded) {
				// refresh the window
				updateActionSets();
			}
		}
	};

	void registerGlobalAction(IAction globalAction) {
		String commandId = globalAction.getActionDefinitionId();

		if (commandId != null) {
			final Object value = globalActionHandlersByCommandId.get(commandId);
			if (value instanceof ActionHandler) {
				// This handler is about to get clobbered, so dispose it.
				final ActionHandler handler = (ActionHandler) value;
				handler.dispose();
			}

			if (globalAction instanceof CommandAction) {
				final String actionId = globalAction.getId();
				if (actionId != null) {
					final IActionCommandMappingService mappingService = (IActionCommandMappingService) serviceLocator
							.getService(IActionCommandMappingService.class);
					mappingService.map(actionId, commandId);
				}
			} else {
				globalActionHandlersByCommandId.put(commandId,
						new ActionHandler(globalAction));
			}
		}

		submitGlobalActions();
	}

	/**
	 * <p>
	 * Submits the action handlers for action set actions and global actions.
	 * Global actions are given priority, so that if a global action and an
	 * action set action both handle the same command, the global action is
	 * given priority.
	 * </p>
	 * <p>
	 * These submissions are submitted as <code>Priority.LEGACY</code>, which
	 * means that they are the lowest priority. This means that if a higher
	 * priority submission handles the same command under the same conditions,
	 * that that submission will become the handler.
	 * </p>
	 */
	void submitGlobalActions() {
		final IHandlerService handlerService = (IHandlerService) getWorkbench().getService(IHandlerService.class);

		/*
		 * Mash the action sets and global actions together, with global actions
		 * taking priority.
		 */
		Map handlersByCommandId = new HashMap();
		handlersByCommandId.putAll(globalActionHandlersByCommandId);

		List newHandlers = new ArrayList(handlersByCommandId.size());

		Iterator existingIter = handlerActivations.iterator();
		while (existingIter.hasNext()) {
			IHandlerActivation next = (IHandlerActivation) existingIter.next();

			String cmdId = next.getCommandId();

			Object handler = handlersByCommandId.get(cmdId);
			if (handler == next.getHandler()) {
				handlersByCommandId.remove(cmdId);
				newHandlers.add(next);
			} else {
				handlerService.deactivateHandler(next);
			}
		}

		final Shell shell = getShell();
		if (shell != null) {
			final Expression expression = new ActiveShellExpression(shell);
			for (Iterator iterator = handlersByCommandId.entrySet().iterator(); iterator
					.hasNext();) {
				Map.Entry entry = (Map.Entry) iterator.next();
				String commandId = (String) entry.getKey();
				IHandler handler = (IHandler) entry.getValue();
				newHandlers.add(handlerService.activateHandler(commandId,
						handler, expression));
			}
		}

		handlerActivations = newHandlers;
	}
	
	/**
	 * Add a generic property listener.
	 * 
	 * @param listener the listener to add
	 * @since 3.3
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		genericPropertyListeners.add(listener);
	}
	
	/**
	 * Removes a generic property listener.
	 * 
	 * @param listener the listener to remove 
	 * @since 3.3
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		genericPropertyListeners.remove(listener);
	}
	
	private void firePropertyChanged(final String property, final Object oldValue, final Object newValue) {
		PropertyChangeEvent event = new PropertyChangeEvent(this, property, oldValue, newValue);
		Object[] listeners = genericPropertyListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			IPropertyChangeListener listener = (IPropertyChangeListener) listeners[i];
			listener.propertyChange(event);
		}
	}

	/*
	 * Adds an listener to the part service.
	 */
	public void addPageListener(IPageListener l) {
		pageListeners.addPageListener(l);
	}

	/**
	 * @see org.eclipse.ui.IPageService
	 */
	public void addPerspectiveListener(org.eclipse.ui.IPerspectiveListener l) {
		perspectiveListeners.addPerspectiveListener(l);
	}

	/**
	 * Configures this window to have a perspecive bar. Does nothing if it
	 * already has one.
	 */
	protected void addPerspectiveBar(int style) {
		Assert.isTrue(perspectiveSwitcher == null);
		perspectiveSwitcher = new PerspectiveSwitcher(this, topBar, style);
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
			// also check for starting - if the first window dies on startup
			// then we'll need to open a default window.
			if (!workbench.isStarting()
					&& !workbench.isClosing()
					&& count <= 1
					&& workbench.getWorkbenchConfigurer()
							.getExitOnLastWindowClose()) {
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

		if (windowClosed && tracker != null) {
			tracker.close();
		}

		return windowClosed;
	}

	/**
	 * Opens a new page. Assumes that busy cursor is active.
	 * <p>
	 * <b>Note:</b> Since release 2.0, a window is limited to contain at most
	 * one page. If a page exist in the window when this method is used, then
	 * another window is created for the new page. Callers are strongly
	 * recommended to use the <code>IWorkbench.openPerspective</code> APIs to
	 * programmatically show a perspective.
	 * </p>
	 */
	protected IWorkbenchPage busyOpenPage(String perspID, IAdaptable input)
			throws WorkbenchException {
		IWorkbenchPage newPage = null;

		if (pageList.isEmpty()) {
			newPage = ((WorkbenchImplementation) Tweaklets
					.get(WorkbenchImplementation.KEY)).createWorkbenchPage(this, perspID, input);
			pageList.add(newPage);
			firePageOpened(newPage);
			setActivePage(newPage);
		} else {
			IWorkbenchWindow window = getWorkbench().openWorkbenchWindow(
					perspID, input);
			newPage = window.getActivePage();
		}

		return newPage;
	}

	/**
	 * @see Window
	 */
	public int open() {
		if (getPages().length == 0) {
			showEmptyWindowContents();
		}
		fireWindowCreated();
		getWindowAdvisor().openIntro();
		int result = super.open();

		// It's time for a layout ... to insure that if TrimLayout
		// is in play, it updates all of the trim it's responsible
		// for. We have to do this before updating in order to get
		// the PerspectiveBar management correct...see defect 137334
		getShell().layout();
		
		fireWindowOpened();
		if (perspectiveSwitcher != null) {
			perspectiveSwitcher.updatePerspectiveBar();
			perspectiveSwitcher.updateBarParent();
		}
		
		return result;
	}

	/*
	 * (non-Javadoc) Method declared on Window.
	 */
	protected boolean canHandleShellCloseEvent() {
		if (!super.canHandleShellCloseEvent()) {
			return false;
		}
		// let the advisor or other interested parties
		// veto the user's explicit request to close the window
		return fireWindowShellClosing();
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
        ICoolBarManager cbm = getCoolBarManager2(); 
		return cbm != null && cbm.getLockLayout();
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
		Iterator itr = oldList.iterator();
		while (itr.hasNext()) {
			WorkbenchPage page = (WorkbenchPage) itr.next();
			firePageClosed(page);
			page.dispose();
		}
		if (!closing) {
			showEmptyWindowContents();
		}
	}

	/**
	 * Save and close all of the pages.
	 */
	public void closeAllPages(boolean save) {
		if (save) {
			boolean ret = saveAllPages(true);
			if (!ret) {
				return;
			}
		}
		closeAllPages();
	}

	/**
	 * closePerspective method comment.
	 */
	protected boolean closePage(IWorkbenchPage in, boolean save) {
		// Validate the input.
		if (!pageList.contains(in)) {
			return false;
		}
		WorkbenchPage oldPage = (WorkbenchPage) in;

		// Save old perspective.
		if (save && oldPage.isSaveNeeded()) {
			if (!oldPage.saveAllEditors(true)) {
				return false;
			}
		}

		// If old page is activate deactivate.
		boolean oldIsActive = (oldPage == getActiveWorkbenchPage());
		if (oldIsActive) {
			setActivePage(null);
		}

		// Close old page.
		pageList.remove(oldPage);
		firePageClosed(oldPage);
		oldPage.dispose();

		// Activate new page.
		if (oldIsActive) {
			IWorkbenchPage newPage = pageList.getNextActive();
			if (newPage != null) {
				setActivePage(newPage);
			}
		}
		if (!closing && pageList.isEmpty()) {
			showEmptyWindowContents();
		}
		return true;
	}

	private void showEmptyWindowContents() {
		if (!emptyWindowContentsCreated) {
			Composite parent = getPageComposite();
			emptyWindowContents = getWindowAdvisor().createEmptyWindowContents(
					parent);
			emptyWindowContentsCreated = true;
			// force the empty window composite to be layed out
			((StackLayout) parent.getLayout()).topControl = emptyWindowContents;
			parent.layout();
		}
	}

	private void hideEmptyWindowContents() {
		if (emptyWindowContentsCreated) {
			if (emptyWindowContents != null) {
				emptyWindowContents.dispose();
				emptyWindowContents = null;
				getPageComposite().layout();
			}
			emptyWindowContentsCreated = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);

		detachedWindowShells = new ShellPool(shell, SWT.TOOL | SWT.TITLE
				| SWT.MAX | SWT.RESIZE | getDefaultOrientation());

		String title = getWindowConfigurer().basicGetTitle();
		if (title != null) {
			shell.setText(TextProcessor.process(title, TEXT_DELIMITERS)); 
		}

		final IWorkbench workbench = getWorkbench();
		workbench.getHelpSystem().setHelp(shell,
				IWorkbenchHelpContextIds.WORKBENCH_WINDOW);

//		initializeDefaultServices();
		final IContextService contextService = (IContextService) getWorkbench().getService(IContextService.class);
		contextService.registerShell(shell, IContextService.TYPE_WINDOW);

		trackShellActivation(shell);
		trackShellResize(shell);
	}

	/* package */ShellPool getDetachedWindowPool() {
		return detachedWindowShells;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.ApplicationWindow#createTrimWidgets(org.eclipse.swt.widgets.Shell)
	 */
	protected void createTrimWidgets(Shell shell) {
		// do nothing -- trim widgets are created in createDefaultContents
	}

	/**
	 * Creates and remembers the client composite, under which workbench pages
	 * create their controls.
	 * 
	 * @since 3.0
	 */
	protected Composite createPageComposite(Composite parent) {
		pageComposite = new Composite(parent, SWT.NONE);
		// use a StackLayout instead of a FillLayout (see bug 81460 [Workbench]
		// (regression) Close all perspectives, open Java perspective, layout
		// wrong)
		pageComposite.setLayout(new StackLayout());
		return pageComposite;
	}

	/**
	 * Creates the contents of the workbench window, including trim controls and
	 * the client composite. This MUST create the client composite via a call to
	 * <code>createClientComposite</code>.
	 * 
	 * @since 3.0
	 */
	protected Control createContents(Composite parent) {
		// we know from Window.create that the parent is a Shell.
		getWindowAdvisor().createWindowContents((Shell) parent);
		// the page composite must be set by createWindowContents
		Assert
				.isNotNull(pageComposite,
						"createWindowContents must call configurer.createPageComposite"); //$NON-NLS-1$
		return pageComposite;
	}

	/**
	 * If the perspective bar is drawn on the top right corner of the window,
	 * then this method changes its appearance from curved to square. This
	 * should have its own preference, but for now it piggy-backs on the
	 * SHOW_TRADITIONAL_STYLE_TABS preference.
	 * 
	 * @param square
	 *            true for a square banner and false otherwise
	 */
	public void setBannerCurve(boolean square) {
		if (topBar != null) {
			topBar.setSimple(square);
		}
	}

	/**
	 * Creates the default contents and layout of the shell.
	 * 
	 * @param shell
	 *            the shell
	 */
	protected void createDefaultContents(final Shell shell) {
		defaultLayout = new TrimLayout();
		shell.setLayout(defaultLayout);

		Menu menuBar = getMenuBarManager().createMenuBar(shell);
		if (getWindowConfigurer().getShowMenuBar()) {
			shell.setMenuBar(menuBar);
		}

		// Create the CBanner widget which parents both the Coolbar
		// and the perspective switcher, and supports some configurations
		// on the left right and bottom
		topBar = new CBanner(shell, SWT.NONE);
		topBarTrim = new WindowTrimProxy(topBar,
				"org.eclipse.ui.internal.WorkbenchWindow.topBar", //$NON-NLS-1$  
				WorkbenchMessages.TrimCommon_Main_TrimName, SWT.NONE, true);

		// the banner gets a curve along with the new tab style
		// TODO create a dedicated preference for this
		setBannerCurve(PrefUtil.getAPIPreferenceStore().getBoolean(
				IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS));

		CacheWrapper coolbarCacheWrapper = new CacheWrapper(topBar);

		final Control coolBar = createCoolBarControl(coolbarCacheWrapper
				.getControl());
		// need to resize the shell, not just the coolbar's immediate
		// parent, if the coolbar wants to grow or shrink

		coolBar.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				// If the user is dragging the sash then we will need to force
				// a resize. However, if the coolbar was resized programatically
				// then everything is already layed out correctly. There is no
				// direct way to tell the difference between these cases,
				// however
				// we take advantage of the fact that dragging the sash does not
				// change the size of the shell, and only force another layout
				// if the shell size is unchanged.
				Rectangle clientArea = shell.getClientArea();

				if (lastShellSize.x == clientArea.width
						&& lastShellSize.y == clientArea.height) {
					LayoutUtil.resize(coolBar);
				}

				lastShellSize.x = clientArea.width;
				lastShellSize.y = clientArea.height;
			}
		});

		if (getWindowConfigurer().getShowCoolBar()) {
			topBar.setLeft(coolbarCacheWrapper.getControl());
		}

		createStatusLine(shell);

		fastViewBar = new FastViewBar(this);
		fastViewBar.createControl(shell);

		if (getWindowConfigurer().getShowPerspectiveBar()) {
			addPerspectiveBar(perspectiveBarStyle());
			perspectiveSwitcher.createControl(shell);
		}

		createProgressIndicator(shell);

		if (getShowHeapStatus()) {
			createHeapStatus(shell);
		}
		
		// Insert any contributed trim into the layout
		// Legacy (3.2) trim
		trimMgr2 = new TrimBarManager2(this);
		
		// 3.3 Trim contributions
		trimContributionMgr = new TrimContributionManager(this);
		
		trimDropTarget = new TrimDropTarget(shell, this);
		DragUtil.addDragTarget(shell, trimDropTarget);
		DragUtil.addDragTarget(null, trimDropTarget);

		// Create the client composite area (where page content goes).
		createPageComposite(shell);

		setLayoutDataForContents();
		// System.err.println(defaultLayout.displayTrim());
	}

	/**
	 * Returns whether the heap status indicator should be shown.
	 * 
	 * @return <code>true</code> to show the heap status indicator,
	 *         <code>false</code> otherwise
	 */
	private boolean getShowHeapStatus() {
		return // Show if the preference is set or debug option is on
		PrefUtil.getAPIPreferenceStore().getBoolean(
				IWorkbenchPreferenceConstants.SHOW_MEMORY_MONITOR)
				|| Boolean.valueOf(
						Platform.getDebugOption(PlatformUI.PLUGIN_ID
								+ "/perf/showHeapStatus")).booleanValue(); //$NON-NLS-1$
	}

	/**
	 * Creates the controls for the heap status indicator.
	 * 
	 * @param parent
	 *            the parent composite
	 */
	private void createHeapStatus(Composite parent) {
		heapStatus = new HeapStatus(parent, PrefUtil
				.getInternalPreferenceStore());

		// Subclass the trim to allow closing...
		heapStatusTrim = new WindowTrimProxy(heapStatus,
				"org.eclipse.ui.internal.HeapStatus", //$NON-NLS-1$
				WorkbenchMessages.TrimCommon_HeapStatus_TrimName, SWT.BOTTOM
						| SWT.TOP) {

			public void handleClose() {
				getControl().dispose();
			}

			public boolean isCloseable() {
				return true;
			}
		};
	}

    /**
	 * <p>
	 * Returns a new menu manager for this workbench window. This menu manager
	 * will just be a proxy to the new command-based menu service.
	 * </p>
	 * <p>
	 * Subclasses may override this method to customize the menu manager.
	 * </p>
	 * 
	 * @return a menu manager for this workbench window; never <code>null</code>.
	 */
	protected MenuManager createMenuManager() {
		return super.createMenuManager();
	}

	/**
	 * Set the perspective bar location
	 * 
	 * @param location
	 *            the location to place the bar
	 */
	public void setPerspectiveBarLocation(String location) {
		if (perspectiveSwitcher != null) {
			perspectiveSwitcher.setPerspectiveBarLocation(location);
		}
	}

	/**
	 * Notifies interested parties (namely the advisor) that the window is about
	 * to be opened.
	 * 
	 * @since 3.1
	 */
	private void fireWindowOpening() {
		// let the application do further configuration
		getWindowAdvisor().preWindowOpen();
	}

	/**
	 * Notifies interested parties (namely the advisor) that the window has been
	 * restored from a previously saved state.
	 * 
	 * @throws WorkbenchException
	 *             passed through from the advisor
	 * @since 3.1
	 */
	void fireWindowRestored() throws WorkbenchException {
		StartupThreading.runWithWorkbenchExceptions(new StartupRunnable() {
			public void runWithException() throws Throwable {
				getWindowAdvisor().postWindowRestore();
			}
		});
	}

	/**
	 * Notifies interested parties (namely the advisor) that the window has been
	 * created.
	 * 
	 * @since 3.1
	 */
	private void fireWindowCreated() {
		getWindowAdvisor().postWindowCreate();
	}

	/**
	 * Notifies interested parties (namely the advisor and the window listeners)
	 * that the window has been opened.
	 * 
	 * @since 3.1
	 */
	private void fireWindowOpened() {
		getWorkbenchImpl().fireWindowOpened(this);
		getWindowAdvisor().postWindowOpen();
	}

	/**
	 * Notifies interested parties (namely the advisor) that the window's shell
	 * is closing. Allows the close to be vetoed.
	 * 
	 * @return <code>true</code> if the close should proceed,
	 *         <code>false</code> if it should be canceled
	 * @since 3.1
	 */
	private boolean fireWindowShellClosing() {
		return getWindowAdvisor().preWindowShellClose();
	}

	/**
	 * Notifies interested parties (namely the advisor and the window listeners)
	 * that the window has been closed.
	 * 
	 * @since 3.1
	 */
	private void fireWindowClosed() {
		// let the application do further deconfiguration
		getWindowAdvisor().postWindowClose();
		getWorkbenchImpl().fireWindowClosed(this);
	}

	/**
	 * Fires page activated
	 */
	private void firePageActivated(IWorkbenchPage page) {
		String label = null; // debugging only
		if (UIStats.isDebugging(UIStats.NOTIFY_PAGE_LISTENERS)) {
			label = "activated " + page.getLabel(); //$NON-NLS-1$
		}
		try {
			UIStats.start(UIStats.NOTIFY_PAGE_LISTENERS, label);
			UIListenerLogging.logPageEvent(this, page,
					UIListenerLogging.WPE_PAGE_ACTIVATED);
			pageListeners.firePageActivated(page);
			partService.pageActivated(page);
		} finally {
			UIStats.end(UIStats.NOTIFY_PAGE_LISTENERS, page.getLabel(), label);
		}
	}

	/**
	 * Fires page closed
	 */
	private void firePageClosed(IWorkbenchPage page) {
		String label = null; // debugging only
		if (UIStats.isDebugging(UIStats.NOTIFY_PAGE_LISTENERS)) {
			label = "closed " + page.getLabel(); //$NON-NLS-1$
		}
		try {
			UIStats.start(UIStats.NOTIFY_PAGE_LISTENERS, label);
			UIListenerLogging.logPageEvent(this, page,
					UIListenerLogging.WPE_PAGE_CLOSED);
			pageListeners.firePageClosed(page);
			partService.pageClosed(page);
		} finally {
			UIStats.end(UIStats.NOTIFY_PAGE_LISTENERS, page.getLabel(), label);
		}

	}

	/**
	 * Fires page opened
	 */
	private void firePageOpened(IWorkbenchPage page) {
		String label = null; // debugging only
		if (UIStats.isDebugging(UIStats.NOTIFY_PAGE_LISTENERS)) {
			label = "opened " + page.getLabel(); //$NON-NLS-1$
		}
		try {
			UIStats.start(UIStats.NOTIFY_PAGE_LISTENERS, label);
			UIListenerLogging.logPageEvent(this, page,
					UIListenerLogging.WPE_PAGE_OPENED);
			pageListeners.firePageOpened(page);
			partService.pageOpened(page);
		} finally {
			UIStats.end(UIStats.NOTIFY_PAGE_LISTENERS, page.getLabel(), label);
		}
	}

	/**
	 * Fires perspective activated
	 */
	void firePerspectiveActivated(IWorkbenchPage page,
			IPerspectiveDescriptor perspective) {
		UIListenerLogging.logPerspectiveEvent(this, page, perspective,
				UIListenerLogging.PLE_PERSP_ACTIVATED);
		perspectiveListeners.firePerspectiveActivated(page, perspective);
	}

	/**
	 * Fires perspective deactivated.
	 * 
	 * @since 3.2
	 */
	void firePerspectivePreDeactivate(IWorkbenchPage page,
			IPerspectiveDescriptor perspective) {
		UIListenerLogging.logPerspectiveEvent(this, page, perspective,
				UIListenerLogging.PLE_PERSP_PRE_DEACTIVATE);
		perspectiveListeners.firePerspectivePreDeactivate(page, perspective);
	}
	
	/**
	 * Fires perspective deactivated.
	 * 
	 * @since 3.1
	 */
	void firePerspectiveDeactivated(IWorkbenchPage page,
			IPerspectiveDescriptor perspective) {
		UIListenerLogging.logPerspectiveEvent(this, page, perspective,
				UIListenerLogging.PLE_PERSP_DEACTIVATED);
		perspectiveListeners.firePerspectiveDeactivated(page, perspective);
	}

	/**
	 * Fires perspective changed
	 */
	public void firePerspectiveChanged(IWorkbenchPage page,
			IPerspectiveDescriptor perspective, String changeId) {
		// Some callers call this even when there is no active perspective.
		// Just ignore this case.
		if (perspective != null) {
			UIListenerLogging.logPerspectiveChangedEvent(this, page,
					perspective, null, changeId);
			perspectiveListeners.firePerspectiveChanged(page, perspective,
					changeId);
		}
	}

	/**
	 * Fires perspective changed for an affected part
	 */
	public void firePerspectiveChanged(IWorkbenchPage page,
			IPerspectiveDescriptor perspective,
			IWorkbenchPartReference partRef, String changeId) {
		// Some callers call this even when there is no active perspective.
		// Just ignore this case.
		if (perspective != null) {
			UIListenerLogging.logPerspectiveChangedEvent(this, page,
					perspective, partRef, changeId);
			perspectiveListeners.firePerspectiveChanged(page, perspective,
					partRef, changeId);
		}
	}

	/**
	 * Fires perspective closed
	 */
	void firePerspectiveClosed(IWorkbenchPage page,
			IPerspectiveDescriptor perspective) {
		UIListenerLogging.logPerspectiveEvent(this, page, perspective,
				UIListenerLogging.PLE_PERSP_CLOSED);
		perspectiveListeners.firePerspectiveClosed(page, perspective);
	}

	/**
	 * Fires perspective opened
	 */
	void firePerspectiveOpened(IWorkbenchPage page,
			IPerspectiveDescriptor perspective) {
		UIListenerLogging.logPerspectiveEvent(this, page, perspective,
				UIListenerLogging.PLE_PERSP_OPENED);
		perspectiveListeners.firePerspectiveOpened(page, perspective);
	}

	/**
	 * Fires perspective saved as.
	 * 
	 * @since 3.1
	 */
	void firePerspectiveSavedAs(IWorkbenchPage page,
			IPerspectiveDescriptor oldPerspective,
			IPerspectiveDescriptor newPerspective) {
		UIListenerLogging.logPerspectiveSavedAs(this, page, oldPerspective,
				newPerspective);
		perspectiveListeners.firePerspectiveSavedAs(page, oldPerspective,
				newPerspective);
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
	 * Returns the page composite, under which the window's pages create their
	 * controls.
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
	 * Returns the number. This corresponds to a page number in a window or a
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
	public ISelectionService getSelectionService() {
		return partService.getSelectionService();
	}

	/**
	 * Returns <code>true</code> when the window's shell is activated,
	 * <code>false</code> when it's shell is deactivated
	 * 
	 * @return boolean <code>true</code> when shell activated,
	 *         <code>false</code> when shell deactivated
	 */
	public boolean getShellActivated() {
		return shellActivated;
	}

	/**
	 * Returns the status line manager for this window (if it has one).
	 * 
	 * @return the status line manager, or <code>null</code> if this window
	 *         does not have a status line
	 * @see ApplicationWindow#addStatusLine
	 */
	public StatusLineManager getStatusLineManager() {
		return super.getStatusLineManager();
	}

	private IWindowTrim getStatusLineTrim() {
		if (statusLineTrim == null) {
			statusLineTrim = new WindowTrimProxy(
					getStatusLineManager().getControl(),
					"org.eclipse.jface.action.StatusLineManager", //$NON-NLS-1$
					WorkbenchMessages.TrimCommon_StatusLine_TrimName, SWT.NONE,
					true);
		}
		return statusLineTrim;
	}

	/**
	 * @see IWorkbenchWindow
	 */
	public IWorkbench getWorkbench() {
		return PlatformUI.getWorkbench();
	}

	public String getToolbarLabel(String actionSetId) {
		ActionSetRegistry registry = WorkbenchPlugin.getDefault()
				.getActionSetRegistry();
		IActionSetDescriptor actionSet = registry.findActionSet(actionSetId);
		if (actionSet != null) {
			return actionSet.getLabel();
		}

		if (IWorkbenchActionConstants.TOOLBAR_FILE
				.equalsIgnoreCase(actionSetId)) {
			return WorkbenchMessages.WorkbenchWindow_FileToolbar;
		}

		if (IWorkbenchActionConstants.TOOLBAR_NAVIGATE
				.equalsIgnoreCase(actionSetId)) {
			return WorkbenchMessages.WorkbenchWindow_NavigateToolbar;
		}

		return null;
	}

	/**
	 * Unconditionally close this window. Assumes the proper flags have been set
	 * correctly (e.i. closing and updateDisabled)
	 */
	private boolean hardClose() {
		boolean result;
		try {
			// Clear the action sets, fix for bug 27416.
            getActionPresentation().clearActionSets();

			// Remove the handler submissions. Bug 64024.
			final IWorkbench workbench = getWorkbench();
			final IHandlerService handlerService = (IHandlerService) workbench.getService(IHandlerService.class);
			handlerService.deactivateHandlers(handlerActivations);
			final Iterator activationItr = handlerActivations.iterator();
			while (activationItr.hasNext()) {
				final IHandlerActivation activation = (IHandlerActivation) activationItr
						.next();
				activation.getHandler().dispose();
			}
			handlerActivations.clear();
			globalActionHandlersByCommandId.clear();

			// Remove the enabled submissions. Bug 64024.
			final IContextService contextService = (IContextService) workbench.getService(IContextService.class);
			contextService.unregisterShell(getShell());

			closeAllPages();

			fireWindowClosed();
			
			// time to wipe our our populate
			IMenuService menuService = (IMenuService) workbench
					.getService(IMenuService.class);
			menuService
					.releaseContributions(((ContributionManager) getActionBars()
							.getMenuManager()));
			ICoolBarManager coolbar = getActionBars().getCoolBarManager();
			if (coolbar != null) {
				menuService
						.releaseContributions(((ContributionManager) coolbar));
			}

			getActionBarAdvisor().dispose();
			getWindowAdvisor().dispose();
			detachedWindowShells.dispose();

			// Bring down all of the services.
			serviceLocator.dispose();

			// Null out the progress region. Bug 64024.
			progressRegion = null;
			
			// Remove drop targets
			DragUtil.removeDragTarget(null, trimDropTarget);
			DragUtil.removeDragTarget(getShell(), trimDropTarget);
			trimDropTarget = null;
			
			if (trimMgr2 != null) {
				trimMgr2.dispose();
				trimMgr2 = null;
			}
			
			if (trimContributionMgr != null) {
				trimContributionMgr.dispose();
				trimContributionMgr = null;
			}
		} finally {
			result = super.close();
		}
		return result;
	}

	/**
	 * @see IWorkbenchWindow
	 */
	public boolean isApplicationMenu(String menuID) {
		// delegate this question to the action bar advisor
		return getActionBarAdvisor().isApplicationMenu(menuID);
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
	 * @param lock
	 *            whether the CoolBar should be locked or unlocked
	 */
	/* package */
	void lockCoolBar(boolean lock) {
        getCoolBarManager2().setLockLayout(lock);
	}

	/**
	 * Makes the window visible and frontmost.
	 */
	void makeVisible() {
		Shell shell = getShell();
		if (shell != null && !shell.isDisposed()) {
			// see bug 96700 and bug 4414 for a discussion on the use of open()
			// here
			shell.open();
		}
	}

	/**
	 * Called when this window is about to be closed.
	 * 
	 * Subclasses may overide to add code that returns <code>false</code> to
	 * prevent closing under certain conditions.
	 */
	public boolean okToClose() {
		// Save all of the editors.
		if (!getWorkbenchImpl().isClosing()) {
			if (!saveAllPages(true)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Opens a new page.
	 * <p>
	 * <b>Note:</b> Since release 2.0, a window is limited to contain at most
	 * one page. If a page exist in the window when this method is used, then
	 * another window is created for the new page. Callers are strongly
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

		if (result[0] instanceof IWorkbenchPage) {
			return (IWorkbenchPage) result[0];
		} else if (result[0] instanceof WorkbenchException) {
			throw (WorkbenchException) result[0];
		} else {
			throw new WorkbenchException(
					WorkbenchMessages.WorkbenchWindow_exceptionMessage);
		}
	}

	/**
	 * Opens a new page.
	 * <p>
	 * <b>Note:</b> Since release 2.0, a window is limited to contain at most
	 * one page. If a page exist in the window when this method is used, then
	 * another window is created for the new page. Callers are strongly
	 * recommended to use the <code>IWorkbench.openPerspective</code> APIs to
	 * programmatically show a perspective.
	 * </p>
	 */
	public IWorkbenchPage openPage(IAdaptable input) throws WorkbenchException {
		String perspId = getWorkbenchImpl().getPerspectiveRegistry()
				.getDefaultPerspective();
		return openPage(perspId, input);
	}

	/*
	 * Removes an listener from the part service.
	 */
	public void removePageListener(IPageListener l) {
		pageListeners.removePageListener(l);
	}

	/**
	 * @see org.eclipse.ui.IPageService
	 */
	public void removePerspectiveListener(org.eclipse.ui.IPerspectiveListener l) {
		perspectiveListeners.removePerspectiveListener(l);
	}

	private IStatus unableToRestorePage(IMemento pageMem) {
		String pageName = pageMem.getString(IWorkbenchConstants.TAG_LABEL);
		if (pageName == null) {
			pageName = ""; //$NON-NLS-1$
		}
		return new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, NLS.bind(
				WorkbenchMessages.WorkbenchWindow_unableToRestorePerspective,
				pageName), null);
	}

	public IStatus restoreState(IMemento memento,
			IPerspectiveDescriptor activeDescriptor) {
		Assert.isNotNull(getShell());

		final MultiStatus result = new MultiStatus(PlatformUI.PLUGIN_ID, IStatus.OK,
				WorkbenchMessages.WorkbenchWindow_problemsRestoringWindow, null);

		// Restore the window advisor state.
		IMemento windowAdvisorState = memento
				.getChild(IWorkbenchConstants.TAG_WORKBENCH_WINDOW_ADVISOR);
		if (windowAdvisorState != null) {
			result.add(getWindowAdvisor().restoreState(windowAdvisorState));
		}

		// Restore actionbar advisor state.
		IMemento actionBarAdvisorState = memento
				.getChild(IWorkbenchConstants.TAG_ACTION_BAR_ADVISOR);
		if (actionBarAdvisorState != null) {
			result.add(getActionBarAdvisor()
					.restoreState(actionBarAdvisorState));
		}

		// Read window's bounds and state.
		final Rectangle [] displayBounds = new Rectangle[1];
		StartupThreading.runWithoutExceptions(new StartupRunnable() {

			public void runWithException() {
				displayBounds[0] = getShell().getDisplay().getBounds();
				
			}});
		final Rectangle shellBounds = new Rectangle(0, 0, 0, 0);

		final IMemento fastViewMem = memento
				.getChild(IWorkbenchConstants.TAG_FAST_VIEW_DATA);
		if (fastViewMem != null) {
			if (fastViewBar != null) {
				StartupThreading.runWithoutExceptions(new StartupRunnable() {

					public void runWithException() {
						fastViewBar.restoreState(fastViewMem);
					}});
				
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
			StartupThreading.runWithoutExceptions(new StartupRunnable() {

				public void runWithException() {
					if (!shellBounds.intersects(displayBounds[0])) {
						Rectangle clientArea = getShell().getDisplay().getClientArea();
						shellBounds.x = clientArea.x;
						shellBounds.y = clientArea.y;
					}
					getShell().setBounds(shellBounds);
				}});
		}
		if ("true".equals(memento.getString(IWorkbenchConstants.TAG_MAXIMIZED))) { //$NON-NLS-1$
			StartupThreading.runWithoutExceptions(new StartupRunnable() {

				public void runWithException() {
					getShell().setMaximized(true);
				}});
			
		}
		if ("true".equals(memento.getString(IWorkbenchConstants.TAG_MINIMIZED))) { //$NON-NLS-1$
			// getShell().setMinimized(true);
		}

		// restore the width of the perspective bar
		if (perspectiveSwitcher != null) {
			perspectiveSwitcher.restoreState(memento);
		}

		// Restore the cool bar order by creating all the tool bar contribution
		// items
		// This needs to be done before pages are created to ensure proper
		// canonical creation
		// of cool items
		final ICoolBarManager2 coolBarMgr = (ICoolBarManager2) getCoolBarManager2();
        if (coolBarMgr != null) {
			IMemento coolBarMem = memento
					.getChild(IWorkbenchConstants.TAG_COOLBAR_LAYOUT);
			if (coolBarMem != null) {
				// Check if the layout is locked
				final Integer lockedInt = coolBarMem
						.getInteger(IWorkbenchConstants.TAG_LOCKED);
				StartupThreading.runWithoutExceptions(new StartupRunnable(){

					public void runWithException() {
						if ((lockedInt != null) && (lockedInt.intValue() == 1)) {
							coolBarMgr.setLockLayout(true);
						} else {
							coolBarMgr.setLockLayout(false);
						}
					}});
				
				// The new layout of the cool bar manager
				ArrayList coolBarLayout = new ArrayList();
				// Traverse through all the cool item in the memento
				IMemento contributionMems[] = coolBarMem
						.getChildren(IWorkbenchConstants.TAG_COOLITEM);
				for (int i = 0; i < contributionMems.length; i++) {
					IMemento contributionMem = contributionMems[i];
					String type = contributionMem
							.getString(IWorkbenchConstants.TAG_ITEM_TYPE);
					if (type == null) {
						// Do not recognize that type
						continue;
					}
					String id = contributionMem
							.getString(IWorkbenchConstants.TAG_ID);

					// Prevent duplicate items from being read back in.
					IContributionItem existingItem = coolBarMgr.find(id);
					if ((id != null) && (existingItem != null)) {
						if (Policy.DEBUG_TOOLBAR_DISPOSAL) {
							System.out
									.println("Not loading duplicate cool bar item: " + id); //$NON-NLS-1$
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
						if (type
								.equals(IWorkbenchConstants.TAG_TYPE_GROUPMARKER)) {
							newItem = new GroupMarker(id);

						} else if (type
								.equals(IWorkbenchConstants.TAG_TYPE_TOOLBARCONTRIBUTION)
								|| type
										.equals(IWorkbenchConstants.TAG_TYPE_PLACEHOLDER)) {

							// Get Width and height
							Integer width = contributionMem
									.getInteger(IWorkbenchConstants.TAG_ITEM_X);
							Integer height = contributionMem
									.getInteger(IWorkbenchConstants.TAG_ITEM_Y);
							// Look for the object in the current cool bar
							// manager
							IContributionItem oldItem = coolBarMgr.find(id);
							// If a tool bar contribution item already exists
							// for this id then use the old object
							if (oldItem != null) {
								newItem = oldItem;
							} else {
								IActionBarPresentationFactory actionBarPresentation = getActionBarPresentationFactory();
								newItem = actionBarPresentation.createToolBarContributionItem(
										actionBarPresentation.createToolBarManager(), id);
								if (type
										.equals(IWorkbenchConstants.TAG_TYPE_PLACEHOLDER)) {
									IToolBarContributionItem newToolBarItem = (IToolBarContributionItem) newItem;
									if (height != null) {
										newToolBarItem.setCurrentHeight(height
												.intValue());
									}
									if (width != null) {
										newToolBarItem.setCurrentWidth(width
												.intValue());
									}
									newItem = new PlaceholderContributionItem(
											newToolBarItem);
								}
								// make it invisible by default
								newItem.setVisible(false);
								// Need to add the item to the cool bar manager
								// so that its canonical order can be preserved
								IContributionItem refItem = findAlphabeticalOrder(
										IWorkbenchActionConstants.MB_ADDITIONS,
										id, coolBarMgr);
								if (refItem != null) {
									coolBarMgr.insertAfter(refItem.getId(),
											newItem);
								} else {
									coolBarMgr.add(newItem);
								}
							}
							// Set the current height and width
							if ((width != null)
									&& (newItem instanceof IToolBarContributionItem)) {
								((IToolBarContributionItem) newItem)
										.setCurrentWidth(width.intValue());
							}
							if ((height != null)
									&& (newItem instanceof IToolBarContributionItem)) {
								((IToolBarContributionItem) newItem)
										.setCurrentHeight(height.intValue());
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

					/*
					 * This line shouldn't be necessary, but is here for
					 * robustness.
					 */
					if (existingItem == null) {
						continue;
					}

					boolean found = false;
					Iterator layoutItemItr = coolBarLayout.iterator();
					while (layoutItemItr.hasNext()) {
						IContributionItem layoutItem = (IContributionItem) layoutItemItr
								.next();
						if ((layoutItem != null)
								&& (layoutItem.equals(existingItem))) {
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
				final IContributionItem[] itemsToSet = new IContributionItem[finalLayout
						.size()];
				finalLayout.toArray(itemsToSet);
				StartupThreading.runWithoutExceptions(new StartupRunnable() {

					public void runWithException() {
						coolBarMgr.setItems(itemsToSet);
					}});
				
			} else {
				// For older workbenchs
				coolBarMem = memento
						.getChild(IWorkbenchConstants.TAG_TOOLBAR_LAYOUT);
				if (coolBarMem != null) {
					// Restore an older layout
					restoreOldCoolBar(coolBarMem);
				}
			}
		}

		// Recreate each page in the window.
		IWorkbenchPage newActivePage = null;
		IMemento[] pageArray = memento
				.getChildren(IWorkbenchConstants.TAG_PAGE);
		for (int i = 0; i < pageArray.length; i++) {
			final IMemento pageMem = pageArray[i];
			String strFocus = pageMem.getString(IWorkbenchConstants.TAG_FOCUS);
			if (strFocus == null || strFocus.length() == 0) {
				continue;
			}

			// Get the input factory.
			final IAdaptable [] input = new IAdaptable[1];
			final IMemento inputMem = pageMem.getChild(IWorkbenchConstants.TAG_INPUT);
			if (inputMem != null) {
				final String factoryID = inputMem
						.getString(IWorkbenchConstants.TAG_FACTORY_ID);
				if (factoryID == null) {
					WorkbenchPlugin
							.log("Unable to restore page - no input factory ID."); //$NON-NLS-1$
					result.add(unableToRestorePage(pageMem));
					continue;
				}
				try {
					UIStats.start(UIStats.RESTORE_WORKBENCH,
							"WorkbenchPageFactory"); //$NON-NLS-1$
					StartupThreading
							.runWithoutExceptions(new StartupRunnable() {

								public void runWithException() throws Throwable {
									IElementFactory factory = PlatformUI
											.getWorkbench().getElementFactory(
													factoryID);
									if (factory == null) {
										WorkbenchPlugin
												.log("Unable to restore page - cannot instantiate input factory: " + factoryID); //$NON-NLS-1$
										result
												.add(unableToRestorePage(pageMem));
										return;
									}

									// Get the input element.
									input[0] = factory.createElement(inputMem);
								}
							});
					
					if (input[0] == null) {
						WorkbenchPlugin
								.log("Unable to restore page - cannot instantiate input element: " + factoryID); //$NON-NLS-1$
						result.add(unableToRestorePage(pageMem));
						continue;
					}
				} finally {
					UIStats.end(UIStats.RESTORE_WORKBENCH, factoryID,
							"WorkbenchPageFactory"); //$NON-NLS-1$
				}
			}
			// Open the perspective.
			final IAdaptable finalInput = input[0];
			final WorkbenchPage [] newPage = new WorkbenchPage[1];
			try {
				StartupThreading.runWithWorkbenchExceptions(new StartupRunnable(){

					public void runWithException() throws WorkbenchException {
						newPage[0] = ((WorkbenchImplementation) Tweaklets
								.get(WorkbenchImplementation.KEY)).createWorkbenchPage(WorkbenchWindow.this, finalInput);
					}});
				
				result.add(newPage[0].restoreState(pageMem, activeDescriptor));
				pageList.add(newPage[0]);
				StartupThreading.runWithoutExceptions(new StartupRunnable() {

					public void runWithException() throws Throwable {
						firePageOpened(newPage[0]);
					}});
				
			} catch (WorkbenchException e) {
				WorkbenchPlugin
						.log(
								"Unable to restore perspective - constructor failed.", e); //$NON-NLS-1$
				result.add(e.getStatus());
				continue;
			}

			if (strFocus != null && strFocus.length() > 0) {
				newActivePage = newPage[0];
			}
		}

		// If there are no pages create a default.
		if (pageList.isEmpty()) {
			try {
				final String defPerspID = getWorkbenchImpl().getPerspectiveRegistry()
						.getDefaultPerspective();
				if (defPerspID != null) {
					final WorkbenchPage [] newPage = new WorkbenchPage[1];
					StartupThreading.runWithWorkbenchExceptions(new StartupRunnable() {
						
						public void runWithException() throws Throwable {
							newPage[0] = ((WorkbenchImplementation) Tweaklets
									.get(WorkbenchImplementation.KEY)).createWorkbenchPage(WorkbenchWindow.this, defPerspID,
											getDefaultPageInput());
						}});
					
					pageList.add(newPage[0]);
					StartupThreading.runWithoutExceptions(new StartupRunnable() {

						public void runWithException() throws Throwable {
							firePageOpened(newPage[0]);
						}});
				}
			} catch (WorkbenchException e) {
				WorkbenchPlugin
						.log(
								"Unable to create default perspective - constructor failed.", e); //$NON-NLS-1$
				result.add(e.getStatus());
				String productName = WorkbenchPlugin.getDefault()
						.getProductName();
				if (productName == null) {
					productName = ""; //$NON-NLS-1$
				}
				getShell().setText(productName);
			}
		}

		// Set active page.
		if (newActivePage == null) {
			newActivePage = pageList.getNextActive();
		}
		final IWorkbenchPage myPage = newActivePage;
		StartupThreading.runWithoutExceptions(new StartupRunnable() {

			public void runWithException() throws Throwable {
				setActivePage(myPage);
			}});
		

		final IMemento introMem = memento.getChild(IWorkbenchConstants.TAG_INTRO);
		if (introMem != null) {
			StartupThreading.runWithoutExceptions(new StartupRunnable() {

				public void runWithException() throws Throwable {
					getWorkbench()
							.getIntroManager()
							.showIntro(
									WorkbenchWindow.this,
									Boolean
											.valueOf(
													introMem
															.getString(IWorkbenchConstants.TAG_STANDBY))
											.booleanValue());
				}
			});

		}
		
		// Only restore the trim state if we're using the default layout
		if (defaultLayout != null) {
			// Restore the trim state. We pass in the 'root'
			// memento since we have to check for pre-3.2
			// state.
			result.add(restoreTrimState(memento));
		}
		
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
        ICoolBarManager2 coolBarMgr = (ICoolBarManager2) getCoolBarManager2();
		// Check to see if layout is locked
		Integer locked = coolbarMem.getInteger(IWorkbenchConstants.TAG_LOCKED);
		boolean state = (locked != null) && (locked.intValue() == 1);
		coolBarMgr.setLockLayout(state);

		// Get the visual layout
		IMemento visibleLayout = coolbarMem
				.getChild(IWorkbenchConstants.TAG_TOOLBAR_LAYOUT);
		ArrayList visibleWrapIndicies = new ArrayList();
		ArrayList visibleItems = new ArrayList();
		if (visibleLayout != null) {
			if (readLayout(visibleLayout, visibleItems, visibleWrapIndicies) == false) {
				return false;
			}
		}
		// Get the remembered layout
		IMemento rememberedLayout = coolbarMem
				.getChild(IWorkbenchConstants.TAG_LAYOUT);
		ArrayList rememberedWrapIndicies = new ArrayList();
		ArrayList rememberedItems = new ArrayList();
		if (rememberedLayout != null) {
			if (readLayout(rememberedLayout, rememberedItems,
					rememberedWrapIndicies) == false) {
				return false;
			}
		}

		// Create the objects
		if (visibleItems != null) {
			// Merge remembered layout into visible layout
			if (rememberedItems != null) {
				// Traverse through all the remembered items
				int currentIndex = 0;
				for (Iterator i = rememberedItems.iterator(); i.hasNext(); currentIndex++) {
					String id = (String) i.next();
					int index = -1;
					for (Iterator iter = visibleItems.iterator(); iter
							.hasNext();) {
						String visibleId = (String) iter.next();
						if (visibleId.equals(id)) {
							index = visibleItems.indexOf(visibleId);
							break;
						}
					}
					// The item is not in the visible list
					if (index == -1) {
						int insertAt = Math.max(0, Math.min(currentIndex,
								visibleItems.size()));
						boolean separateLine = false;
						// Check whether this item is on a separate line
						for (Iterator iter = rememberedWrapIndicies.iterator(); iter
								.hasNext();) {
							Integer wrapIndex = (Integer) iter.next();
							if (wrapIndex.intValue() <= insertAt) {
								insertAt = visibleItems.size();
								// Add new wrap index for this Item
								visibleWrapIndicies.add(new Integer(insertAt));
								separateLine = true;
							}
						}
						// Add item to array list
						visibleItems.add(insertAt, id);
						// If the item was not on a separate line then adjust
						// the visible wrap indicies
						if (!separateLine) {
							// Adjust visible wrap indicies
							for (int j = 0; j < visibleWrapIndicies.size(); j++) {
								Integer index2 = (Integer) visibleWrapIndicies
										.get(j);
								if (index2.intValue() >= insertAt) {
									visibleWrapIndicies.set(j, new Integer(
											index2.intValue() + 1));
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
				String id = (String) i.next();
				// Look for the object in the current cool bar manager
				IContributionItem oldItem = null;
				IContributionItem newItem = null;
				if (id != null) {
					oldItem = coolBarMgr.find(id);
				}
				// If a tool bar contribution item already exists for this id
				// then use the old object
				if (oldItem instanceof IToolBarContributionItem) {
					newItem = oldItem;
				} else {
					IActionBarPresentationFactory actionBarPresentaiton = getActionBarPresentationFactory();
					newItem = actionBarPresentaiton.createToolBarContributionItem(
									actionBarPresentaiton.createToolBarManager(), id);
					// make it invisible by default
					newItem.setVisible(false);
					// Need to add the item to the cool bar manager so that its
					// canonical order can be preserved
					IContributionItem refItem = findAlphabeticalOrder(
							IWorkbenchActionConstants.MB_ADDITIONS, id,
							coolBarMgr);
					if (refItem != null) {
						coolBarMgr.insertAfter(refItem.getId(), newItem);
					} else {
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
			for (int i = 1; i < visibleWrapIndicies.size(); i++) {
				int insertAt = ((Integer) visibleWrapIndicies.get(i))
						.intValue()
						+ offset;
				coolBarLayout.add(insertAt, new Separator(
						CoolBarManager.USER_SEPARATOR));
				offset++;
			}

			// Add any group markers in their appropriate places
			IContributionItem[] items = coolBarMgr.getItems();
			for (int i = 0; i < items.length; i++) {
				IContributionItem item = items[i];
				if (item.isGroupMarker()) {
					coolBarLayout.add(Math.max(Math
							.min(i, coolBarLayout.size()), 0), item);
				}
			}
			IContributionItem[] itemsToSet = new IContributionItem[coolBarLayout
					.size()];
			coolBarLayout.toArray(itemsToSet);
			coolBarMgr.setItems(itemsToSet);
		}
		return true;
	}

	/**
	 * Helper method used for restoring an old cool bar layout. This method
	 * reads the memento and populatates the item id's and wrap indicies.
	 */
	private boolean readLayout(IMemento memento, ArrayList itemIds,
			ArrayList wrapIndicies) {
		// Get the Wrap indicies
		IMemento[] wraps = memento
				.getChildren(IWorkbenchConstants.TAG_ITEM_WRAP_INDEX);
		if (wraps == null) {
			return false;
		}
		for (int i = 0; i < wraps.length; i++) {
			IMemento wrapMem = wraps[i];
			Integer index = wrapMem.getInteger(IWorkbenchConstants.TAG_INDEX);
			if (index == null) {
				return false;
			}
			wrapIndicies.add(index);
		}
		// Get the Item ids
		IMemento[] savedItems = memento
				.getChildren(IWorkbenchConstants.TAG_ITEM);
		if (savedItems == null) {
			return false;
		}
		for (int i = 0; i < savedItems.length; i++) {
			IMemento savedMem = savedItems[i];
			String id = savedMem.getString(IWorkbenchConstants.TAG_ID);
			if (id == null) {
				return false;
			}
			itemIds.add(id);
		}
		return true;
	}

	/**
	 * Returns the contribution item that the given contribution item should be
	 * inserted after.
	 * 
	 * @param startId
	 *            the location to start looking alphabetically.
	 * @param itemId
	 *            the target item id.
	 * @param mgr
	 *            the contribution manager.
	 * @return the contribution item that the given items should be returned
	 *         after.
	 */
	private IContributionItem findAlphabeticalOrder(String startId,
			String itemId, IContributionManager mgr) {
		IContributionItem[] items = mgr.getItems();
		int insertIndex = 0;

		// look for starting point
		while (insertIndex < items.length) {
			IContributionItem item = items[insertIndex];
			if (item.getId() != null && item.getId().equals(startId)) {
				break;
			}
			++insertIndex;
		}

		// Find the index that this item should be inserted in
		for (int i = insertIndex + 1; i < items.length; i++) {
			IContributionItem item = items[i];
			String testId = item.getId();

			if (item.isGroupMarker()) {
				break;
			}

			if (itemId != null && testId != null) {
				if (itemId.compareTo(testId) < 1) {
					break;
				}
			}
			insertIndex = i;
		}
		if (insertIndex >= items.length) {
			return null;
		}
		return items[insertIndex];
	}

	/*
	 * (non-Javadoc) Method declared on IRunnableContext.
	 */
	public void run(boolean fork, boolean cancelable,
			IRunnableWithProgress runnable) throws InvocationTargetException,
			InterruptedException {
		IWorkbenchContextSupport contextSupport = getWorkbench()
				.getContextSupport();
		final boolean keyFilterEnabled = contextSupport.isKeyFilterEnabled();

		Control fastViewBarControl = getFastViewBar() == null ? null
				: getFastViewBar().getControl();
		boolean fastViewBarWasEnabled = fastViewBarControl == null ? false
				: fastViewBarControl.getEnabled();

		Control perspectiveBarControl = getPerspectiveBar() == null ? null
				: getPerspectiveBar().getControl();
		boolean perspectiveBarWasEnabled = perspectiveBarControl == null ? false
				: perspectiveBarControl.getEnabled();

		// Cache for any diabled trim controls
		List disabledControls = null;
		
		try {
			if (fastViewBarControl != null && !fastViewBarControl.isDisposed()) {
				fastViewBarControl.setEnabled(false);
			}

			if (perspectiveBarControl != null
					&& !perspectiveBarControl.isDisposed()) {
				perspectiveBarControl.setEnabled(false);
			}

			if (keyFilterEnabled) {
				contextSupport.setKeyFilterEnabled(false);
			}

			// Disable all trim -except- the StatusLine
			if (defaultLayout != null)
				disabledControls = defaultLayout.disableTrim(getStatusLineTrim());

			super.run(fork, cancelable, runnable);
		} finally {
			if (fastViewBarControl != null && !fastViewBarControl.isDisposed()) {
				fastViewBarControl.setEnabled(fastViewBarWasEnabled);
			}

			if (perspectiveBarControl != null
					&& !perspectiveBarControl.isDisposed()) {
				perspectiveBarControl.setEnabled(perspectiveBarWasEnabled);
			}

			if (keyFilterEnabled) {
				contextSupport.setKeyFilterEnabled(true);
			}
			
			// Re-enable any disabled trim
			if (defaultLayout != null && disabledControls != null)
				defaultLayout.enableTrim(disabledControls);
		}
	}

	/**
	 * Save all of the pages. Returns true if the operation succeeded.
	 */
	private boolean saveAllPages(boolean bConfirm) {
		boolean bRet = true;
		Iterator itr = pageList.iterator();
		while (bRet && itr.hasNext()) {
			WorkbenchPage page = (WorkbenchPage) itr.next();
			bRet = page.saveAllEditors(bConfirm);
		}
		return bRet;
	}

	/**
	 * @see IPersistable
	 */
	public IStatus saveState(IMemento memento) {

		MultiStatus result = new MultiStatus(PlatformUI.PLUGIN_ID, IStatus.OK,
				WorkbenchMessages.WorkbenchWindow_problemsSavingWindow, null);

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
		IMemento fastViewBarMem = memento
				.createChild(IWorkbenchConstants.TAG_FAST_VIEW_DATA);
		if (fastViewBar != null) {
			fastViewBar.saveState(fastViewBarMem);
		}

		memento.putInteger(IWorkbenchConstants.TAG_X, normalBounds.x);
		memento.putInteger(IWorkbenchConstants.TAG_Y, normalBounds.y);
		memento.putInteger(IWorkbenchConstants.TAG_WIDTH, normalBounds.width);
		memento.putInteger(IWorkbenchConstants.TAG_HEIGHT, normalBounds.height);

		IWorkbenchPage activePage = getActivePage();
		if (activePage != null
				&& activePage.findView(IIntroConstants.INTRO_VIEW_ID) != null) {
			IMemento introMem = memento
					.createChild(IWorkbenchConstants.TAG_INTRO);
			boolean isStandby = getWorkbench()
					.getIntroManager()
					.isIntroStandby(getWorkbench().getIntroManager().getIntro());
			introMem.putString(IWorkbenchConstants.TAG_STANDBY, String
					.valueOf(isStandby));
		}

		// save the width of the perspective bar
		IMemento persBarMem = memento
				.createChild(IWorkbenchConstants.TAG_PERSPECTIVE_BAR);
		if (perspectiveSwitcher != null) {
			perspectiveSwitcher.saveState(persBarMem);
		}

		// / Save the order of the cool bar contribution items
        ICoolBarManager2 coolBarMgr = (ICoolBarManager2) getCoolBarManager2();
        if (coolBarMgr != null) {
        	coolBarMgr.refresh();
			IMemento coolBarMem = memento
					.createChild(IWorkbenchConstants.TAG_COOLBAR_LAYOUT);
            if (coolBarMgr.getLockLayout() == true) {
				coolBarMem.putInteger(IWorkbenchConstants.TAG_LOCKED, 1);
			} else {
				coolBarMem.putInteger(IWorkbenchConstants.TAG_LOCKED, 0);
			}
            IContributionItem[] items = coolBarMgr.getItems();
			for (int i = 0; i < items.length; i++) {
				IMemento coolItemMem = coolBarMem
						.createChild(IWorkbenchConstants.TAG_COOLITEM);
				IContributionItem item = items[i];
				// The id of the contribution item
				if (item.getId() != null) {
					coolItemMem.putString(IWorkbenchConstants.TAG_ID, item
							.getId());
				}
				// Write out type and size if applicable
				if (item.isSeparator()) {
					coolItemMem.putString(IWorkbenchConstants.TAG_ITEM_TYPE,
							IWorkbenchConstants.TAG_TYPE_SEPARATOR);
				} else if (item.isGroupMarker() && !item.isSeparator()) {
					coolItemMem.putString(IWorkbenchConstants.TAG_ITEM_TYPE,
							IWorkbenchConstants.TAG_TYPE_GROUPMARKER);
				} else {
					if (item instanceof PlaceholderContributionItem) {
						coolItemMem.putString(
								IWorkbenchConstants.TAG_ITEM_TYPE,
								IWorkbenchConstants.TAG_TYPE_PLACEHOLDER);
					} else {
						// Store the identifier.
						coolItemMem
								.putString(
										IWorkbenchConstants.TAG_ITEM_TYPE,
										IWorkbenchConstants.TAG_TYPE_TOOLBARCONTRIBUTION);
					}

					/*
					 * Retrieve a reasonable approximation of the height and
					 * width, if possible.
					 */
					final int height;
					final int width;
					if (item instanceof IToolBarContributionItem) {
						IToolBarContributionItem toolBarItem = (IToolBarContributionItem) item;
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
					coolItemMem.putInteger(IWorkbenchConstants.TAG_ITEM_X,
							width);
					coolItemMem.putInteger(IWorkbenchConstants.TAG_ITEM_Y,
							height);
				}
			}
		}

		// Save each page.
		Iterator itr = pageList.iterator();
		while (itr.hasNext()) {
			WorkbenchPage page = (WorkbenchPage) itr.next();

			// Save perspective.
			IMemento pageMem = memento
					.createChild(IWorkbenchConstants.TAG_PAGE);
			pageMem.putString(IWorkbenchConstants.TAG_LABEL, page.getLabel());
			result.add(page.saveState(pageMem));

			if (page == getActiveWorkbenchPage()) {
				pageMem.putString(IWorkbenchConstants.TAG_FOCUS, "true"); //$NON-NLS-1$
			}

			// Get the input.
			IAdaptable input = page.getInput();
			if (input != null) {
				IPersistableElement persistable = (IPersistableElement) Util.getAdapter(input,
						IPersistableElement.class);
				if (persistable == null) {
					WorkbenchPlugin
							.log("Unable to save page input: " //$NON-NLS-1$
									+ input
									+ ", because it does not adapt to IPersistableElement"); //$NON-NLS-1$
				} else {
					// Save input.
					IMemento inputMem = pageMem
							.createChild(IWorkbenchConstants.TAG_INPUT);
					inputMem.putString(IWorkbenchConstants.TAG_FACTORY_ID,
							persistable.getFactoryId());
					persistable.saveState(inputMem);
				}
			}
		}

		// Save window advisor state.
		IMemento windowAdvisorState = memento
				.createChild(IWorkbenchConstants.TAG_WORKBENCH_WINDOW_ADVISOR);
		result.add(getWindowAdvisor().saveState(windowAdvisorState));

		// Save actionbar advisor state.
		IMemento actionBarAdvisorState = memento
				.createChild(IWorkbenchConstants.TAG_ACTION_BAR_ADVISOR);
		result.add(getActionBarAdvisor().saveState(actionBarAdvisorState));

		// Only save the trim state if we're using the default layout
		if (defaultLayout != null) {
			IMemento trimState = memento.createChild(IWorkbenchConstants.TAG_TRIM);
			result.add(saveTrimState(trimState));
		}

		return result;
	}

	/**
	 * Save the trim layout trim area and trim ordering.
	 * 
	 * @param memento
	 *            the memento to update
	 * @return the status, OK or not..
	 * @since 3.2
	 */
	private IStatus saveTrimState(IMemento memento) {
		int[] ids = defaultLayout.getAreaIds();
		for (int i = 0; i < ids.length; i++) {
			int id = ids[i];
			List trim = defaultLayout.getAreaTrim(id);
			if (!trim.isEmpty()) {
				IMemento area = memento
						.createChild(IWorkbenchConstants.TAG_TRIM_AREA, Integer
								.toString(id));
				Iterator d = trim.iterator();
				while (d.hasNext()) {
					IWindowTrim item = (IWindowTrim) d.next();
					area.createChild(IWorkbenchConstants.TAG_TRIM_ITEM, item
							.getId());
				}
			}
		}
		return Status.OK_STATUS;
	}

	/**
	 * Restore the trim layout state from the memento.
	 * 
	 * @param memento
	 *            the 'root' Workbench memento to restore
	 * @return the status, OK or not
	 * @since 3.2
	 */
	private IStatus restoreTrimState(IMemento memento) {
		// Determine if we have saved state. If we don't have any 3.2
		// type state we're not done because the FastViewBar maintained
		// its own 'side' state in 3.1 so we'll honor its value
		IMemento trimState = memento.getChild(IWorkbenchConstants.TAG_TRIM);
		if (trimState != null) {
			// first pass sets up ordering for all trim areas
			IMemento[] areas = trimState
					.getChildren(IWorkbenchConstants.TAG_TRIM_AREA);
			
			// We need to remember all the trim that was repositioned
			// here so we can re-site -newly contributed- trim after
			// we're done
			final List knownIds = new ArrayList();
			
			List[] trimOrder = new List[areas.length];
			for (int i = 0; i < areas.length; i++) {
				trimOrder[i] = new ArrayList();
				List preferredLocations = new ArrayList();
				IMemento area = areas[i];
				IMemento[] items = area
						.getChildren(IWorkbenchConstants.TAG_TRIM_ITEM);
				for (int j = 0; j < items.length; j++) {
					IMemento item = items[j];
					String id = item.getID();
					knownIds.add(id);
					preferredLocations.add(id);
					
					IWindowTrim t = defaultLayout.getTrim(id);
					if (t != null) {
						trimOrder[i].add(t);
					}
				}
				
				// Inform the TrimLayout of the preferred location for this area
				String areaIdString = areas[i].getID();
				int areaId = Integer.parseInt(areaIdString);
				defaultLayout.setPreferredLocations(areaId, preferredLocations);
			}
	
			// second pass applies all of the window trim
			for (int i = 0; i < areas.length; i++) {
				IMemento area = areas[i];
				final int id = Integer.parseInt(area.getID());
				final List myTrimOrderList = trimOrder[i];
				StartupThreading.runWithoutExceptions(new StartupRunnable() {

					public void runWithException() throws Throwable {
						defaultLayout.updateAreaTrim(id, myTrimOrderList, false);
					}});
				
			}

			// get the trim manager to re-locate any -newly contributed-
			// trim widgets
			// Legacy (3.2) trim
			if (trimMgr2 != null) {
				StartupThreading.runWithoutExceptions(new StartupRunnable() {

					public void runWithException() throws Throwable {
						trimMgr2.updateLocations(knownIds);
					}});
				
			}
			
			// 3.3 Trim Contributions
			if (trimContributionMgr != null) {
				StartupThreading.runWithoutExceptions(new StartupRunnable() {
					public void runWithException() throws Throwable {
						trimContributionMgr.updateLocations(knownIds);

						// Update the GUI with the new locations
						WorkbenchPage page = getActiveWorkbenchPage();
						if (page != null) {
							Perspective perspective = page.getActivePerspective();
							if (perspective != null) {
								// Ensure that only the upper/right editor stack has
								// min/max buttons
								page.getEditorPresentation().updateStackButtons();
								
								// The perspective's onActivate manipulates the trim under the
								// new min/max story so cause it to refresh...
								perspective.onActivate();
							}
						}
					}});
			}
		}
		else {
			// No 3.2 state...check if the FVB has state
			IMemento fastViewMem = memento
					.getChild(IWorkbenchConstants.TAG_FAST_VIEW_DATA);
			if (fastViewMem != null) {
				if (fastViewBar != null) {
			        final Integer bigInt = fastViewMem.getInteger(IWorkbenchConstants.TAG_FAST_VIEW_SIDE);
			        if (bigInt != null) {
			        	StartupThreading.runWithoutExceptions(new StartupRunnable() {

							public void runWithException() throws Throwable {
								fastViewBar.dock(bigInt.intValue());
					        	getTrimManager().addTrim(bigInt.intValue(), fastViewBar);
							}});
			        	
			        }
				}
			}
		}
		
		return Status.OK_STATUS;
	}

	/**
	 * Sets the active page within the window.
	 * 
	 * @param in
	 *            identifies the new active page, or <code>null</code> for no
	 *            active page
	 */
	public void setActivePage(final IWorkbenchPage in) {
		if (getActiveWorkbenchPage() == in) {
			return;
		}

		// 1FVGTNR: ITPUI:WINNT - busy cursor for switching perspectives
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				// Deactivate old persp.
				WorkbenchPage currentPage = getActiveWorkbenchPage();
				if (currentPage != null) {
					currentPage.onDeactivate();
				}

				// Activate new persp.
				if (in == null || pageList.contains(in)) {
					pageList.setActive(in);
				}
				WorkbenchPage newPage = pageList.getActive();
				Composite parent = getPageComposite();
				StackLayout layout = (StackLayout) parent.getLayout();
				if (newPage != null) {
					layout.topControl = newPage.getClientComposite();
					parent.layout();
					hideEmptyWindowContents();
					newPage.onActivate();
					firePageActivated(newPage);
					if (newPage.getPerspective() != null) {
						firePerspectiveActivated(newPage, newPage
								.getPerspective());
					}
				} else {
					layout.topControl = null;
					parent.layout();
				}

				updateFastViewBar();

				if (isClosing()) {
					return;
				}

				updateDisabled = false;

				// Update action bars ( implicitly calls updateActionBars() )
				updateActionSets();
				submitGlobalActions();

				if (perspectiveSwitcher != null) {
					perspectiveSwitcher.update(false);
				}

				getMenuManager().update(IAction.TEXT);
			}
		});
	}

	/**
	 * Returns whether or not children exist for the Window's toolbar control.
	 * Overridden for coolbar support.
	 * <p>
	 * 
	 * @return boolean true if children exist, false otherwise
	 */
	protected boolean toolBarChildrenExist() {
		CoolBar coolBarControl = (CoolBar) getCoolBarControl();
		return coolBarControl.getItemCount() > 0;
	}

	/**
	 * Hooks a listener to track the activation and deactivation of the window's
	 * shell. Notifies the active part and editor of the change
	 */
	private void trackShellActivation(Shell shell) {
		shell.addShellListener(new ShellAdapter() {
			public void shellActivated(ShellEvent event) {
				shellActivated = true;
				serviceLocator.activate();
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
					getWorkbenchImpl()
							.fireWindowActivated(WorkbenchWindow.this);
				}
			}

			public void shellDeactivated(ShellEvent event) {
				shellActivated = false;
				serviceLocator.deactivate();
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
					getWorkbenchImpl().fireWindowDeactivated(
							WorkbenchWindow.this);
				}
			}
		});
	}

	/**
	 * Hooks a listener to track the resize of the window's shell. Stores the
	 * new bounds if in normal state - that is, not in minimized or maximized
	 * state)
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
				if (shell == null) {
					return;
				}
				if (shell.isDisposed()) {
					return;
				}
				if (shell.getMinimized()) {
					return;
				}
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
		if (updateDisabled || updatesDeferred()) {
			return;
		}
		// updateAll required in order to enable accelerators on pull-down menus
		getMenuBarManager().update(false);

		try {
			getShell().setLayoutDeferred(true);
			getCoolBarManager2().update(false);
		} finally {
			getShell().setLayoutDeferred(false);
		}
		
		getStatusLineManager().update(false);
	}

	/**
	 * Returns true iff we are currently deferring UI processing due to a large
	 * update
	 * 
	 * @return true iff we are deferring UI updates.
	 * @since 3.1
	 */
	private boolean updatesDeferred() {
		return largeUpdates > 0;
	}

	/**
	 * <p>
	 * Indicates the start of a large update within this window. This is used to
	 * disable CPU-intensive, change-sensitive services that were temporarily
	 * disabled in the midst of large changes. This method should always be
	 * called in tandem with <code>largeUpdateEnd</code>, and the event loop
	 * should not be allowed to spin before that method is called.
	 * </p>
	 * <p>
	 * Important: always use with <code>largeUpdateEnd</code>!
	 * </p>
	 * 
	 * @since 3.1
	 */
	public final void largeUpdateStart() {
		largeUpdates++;
	}

	/**
	 * <p>
	 * Indicates the end of a large update within this window. This is used to
	 * re-enable services that were temporarily disabled in the midst of large
	 * changes. This method should always be called in tandem with
	 * <code>largeUpdateStart</code>, and the event loop should not be
	 * allowed to spin before this method is called.
	 * </p>
	 * <p>
	 * Important: always protect this call by using <code>finally</code>!
	 * </p>
	 * 
	 * @since 3.1
	 */
	public final void largeUpdateEnd() {
		if (--largeUpdates == 0) {
			updateActionBars();
		}
	}

	/**
	 * Update the visible action sets. This method is typically called from a
	 * page when the user changes the visible action sets within the
	 * prespective.
	 */
	public void updateActionSets() {
		if (updateDisabled) {
			return;
		}

		WorkbenchPage currentPage = getActiveWorkbenchPage();
		if (currentPage == null) {
			getActionPresentation().clearActionSets();
		} else {
			ICoolBarManager2 coolBarManager = (ICoolBarManager2) getCoolBarManager2();
			if (coolBarManager != null) {
				coolBarManager.refresh();
			}
			getActionPresentation().setActionSets(
					currentPage.getActionSets());
		}
		fireActionSetsChanged();
		updateActionBars();

		// hide the launch menu if it is empty
		String path = IWorkbenchActionConstants.M_WINDOW
				+ IWorkbenchActionConstants.SEP
				+ IWorkbenchActionConstants.M_LAUNCH;
		IMenuManager manager = getMenuBarManager().findMenuUsingPath(path);
		IContributionItem item = getMenuBarManager().findUsingPath(path);

		if (manager == null || item == null) {
			return;
		}
		item.setVisible(manager.getItems().length >= 2);
		// there is a separator for the additions group thus >= 2
	}

	private ListenerList actionSetListeners = null;

	private ListenerList backgroundSaveListeners = new ListenerList(ListenerList.IDENTITY);

	private final void fireActionSetsChanged() {
		if (actionSetListeners != null) {
			final Object[] listeners = actionSetListeners.getListeners();
			for (int i = 0; i < listeners.length; i++) {
				final IActionSetsListener listener = (IActionSetsListener) listeners[i];
				final WorkbenchPage currentPage = getActiveWorkbenchPage();
				final IActionSetDescriptor[] newActionSets;
				if (currentPage == null) {
					newActionSets = null;
				} else {
					newActionSets = currentPage.getActionSets();
				}
				final ActionSetsEvent event = new ActionSetsEvent(newActionSets);
				listener.actionSetsChanged(event);
			}
		}
	}

	final void addActionSetsListener(final IActionSetsListener listener) {
		if (actionSetListeners == null) {
			actionSetListeners = new ListenerList();
		}

		actionSetListeners.add(listener);
	}

	final void removeActionSetsListener(final IActionSetsListener listener) {
		if (actionSetListeners != null) {
			actionSetListeners.remove(listener);
			if (actionSetListeners.isEmpty()) {
				actionSetListeners = null;
			}
		}
	}

	/**
	 * Create the progress indicator for the receiver.
	 * 
	 * @param shell
	 *            the parent shell
	 */
	private void createProgressIndicator(Shell shell) {
		if (getWindowConfigurer().getShowProgressIndicator()) {
			progressRegion = new ProgressRegion();
			progressRegion.createContents(shell, this);
		}

	}

	class PageList {
		// List of pages in the order they were created;
		private List pagesInCreationOrder;

		// List of pages where the top is the last activated.
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
			// It will be moved to top only when activated.
			return true;
		}

		public Iterator iterator() {
			return pagesInCreationOrder.iterator();
		}

		public boolean contains(Object object) {
			return pagesInCreationOrder.contains(object);
		}

		public boolean remove(Object object) {
			if (active == object) {
				active = null;
			}
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
			if (active == page) {
				return;
			}

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
				if (pageInActivationOrder.isEmpty()) {
					return null;
				}

				return (WorkbenchPage) pageInActivationOrder
						.get(pageInActivationOrder.size() - 1);
			}

			if (pageInActivationOrder.size() < 2) {
				return null;
			}

			return (WorkbenchPage) pageInActivationOrder
					.get(pageInActivationOrder.size() - 2);
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
		}
		return windowConfigurer;
	}

	/**
	 * Returns the workbench advisor. Assumes the workbench has been created
	 * already.
	 * <p>
	 * IMPORTANT This method is declared private to prevent regular plug-ins
	 * from downcasting IWorkbenchWindow to WorkbenchWindow and getting hold of
	 * the workbench advisor that would allow them to tamper with the workbench.
	 * The workbench advisor is internal to the application.
	 * </p>
	 */
	private/* private - DO NOT CHANGE */
	WorkbenchAdvisor getAdvisor() {
		return getWorkbenchImpl().getAdvisor();
	}

	/**
	 * Returns the window advisor, creating a new one for this window if needed.
	 * <p>
	 * IMPORTANT This method is declared private to prevent regular plug-ins
	 * from downcasting IWorkbenchWindow to WorkbenchWindow and getting hold of
	 * the window advisor that would allow them to tamper with the window. The
	 * window advisor is internal to the application.
	 * </p>
	 */
	private/* private - DO NOT CHANGE */
	WorkbenchWindowAdvisor getWindowAdvisor() {
		if (windowAdvisor == null) {
			windowAdvisor = getAdvisor().createWorkbenchWindowAdvisor(
					getWindowConfigurer());
			Assert.isNotNull(windowAdvisor);
		}
		return windowAdvisor;
	}

	/**
	 * Returns the action bar advisor, creating a new one for this window if
	 * needed.
	 * <p>
	 * IMPORTANT This method is declared private to prevent regular plug-ins
	 * from downcasting IWorkbenchWindow to WorkbenchWindow and getting hold of
	 * the action bar advisor that would allow them to tamper with the window's
	 * action bars. The action bar advisor is internal to the application.
	 * </p>
	 */
	private/* private - DO NOT CHANGE */
	ActionBarAdvisor getActionBarAdvisor() {
		if (actionBarAdvisor == null) {
			actionBarAdvisor = getWindowAdvisor().createActionBarAdvisor(
					getWindowConfigurer().getActionBarConfigurer());
			Assert.isNotNull(actionBarAdvisor);
		}
		return actionBarAdvisor;
	}

	/*
	 * Returns the IWorkbench implementation.
	 */
	private Workbench getWorkbenchImpl() {
		return Workbench.getInstance();
	}

	/**
	 * Fills the window's real action bars.
	 * 
	 * @param flags
	 *            indicate which bars to fill
	 */
	public void fillActionBars(int flags) {
		Workbench workbench = getWorkbenchImpl();
		workbench.largeUpdateStart();
		try {
			getActionBarAdvisor().fillActionBars(flags);
			//
			// 3.3 start
			final IMenuService menuService = (IMenuService) serviceLocator
					.getService(IMenuService.class);
			menuService.populateContributionManager(
					(ContributionManager) getActionBars().getMenuManager(),
					MenuUtil.MAIN_MENU);
			ICoolBarManager coolbar = getActionBars().getCoolBarManager();
			if (coolbar != null) {
				menuService.populateContributionManager(
						(ContributionManager) coolbar,
						MenuUtil.MAIN_TOOLBAR);
			}
			// 3.3 end
		} finally {
			workbench.largeUpdateEnd();
		}
	}

	/**
	 * Fills the window's proxy action bars.
	 * 
	 * @param proxyBars
	 *            the proxy configurer
	 * @param flags
	 *            indicate which bars to fill
	 */
	public void fillActionBars(IActionBarConfigurer2 proxyBars, int flags) {
		Assert.isNotNull(proxyBars);
		WorkbenchWindowConfigurer.WindowActionBarConfigurer wab = (WorkbenchWindowConfigurer.WindowActionBarConfigurer) getWindowConfigurer()
				.getActionBarConfigurer();
		wab.setProxy(proxyBars);
		try {
			getActionBarAdvisor().fillActionBars(
					flags | ActionBarAdvisor.FILL_PROXY);
		} finally {
			wab.setProxy(null);
		}
	}

	/**
	 * The <code>WorkbenchWindow</code> implementation of this method has the
	 * same logic as <code>Window</code>'s implementation, but without the
	 * resize check. We don't want to skip setting the bounds if the shell has
	 * been resized since a free resize event occurs on Windows when the menubar
	 * is set in configureShell.
	 */
	protected void initializeBounds() {
		Point size = getInitialSize();
		Point location = getInitialLocation(size);
		getShell().setBounds(
				getConstrainedShellBounds(new Rectangle(location.x, location.y,
						size.x, size.y)));
	}

	/*
	 * Unlike dialogs, the position of the workbench window is set by the user
	 * and persisted across sessions. If the user wants to put the window
	 * offscreen or spanning multiple monitors, let them (bug 74762)
	 */
	protected void constrainShellSize() {
		// As long as the shell is visible on some monitor, don't change it.
		Rectangle bounds = getShell().getBounds();
		if (!SwtUtil.intersectsAnyMonitor(Display.getCurrent(), bounds)) {
			super.constrainShellSize();
		}
	}

	/*
	 * Unlike dialogs, the position of the workbench window is set by the user
	 * and persisted across sessions. If the user wants to put the window
	 * offscreen or spanning multiple monitors, let them (bug 74762)
	 */
	protected Point getInitialLocation(Point size) {
		Shell shell = getShell();
		if (shell != null) {
			return shell.getLocation();
		}

		return super.getInitialLocation(size);
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
	 * @param visible
	 *            whether the cool bar should be shown. This is only applicable
	 *            if the window configurer also wishes either the cool bar to be
	 *            visible.
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
	 * @return whether the cool bar should be shown. This is only applicable if
	 *         the window configurer also wishes either the cool bar to be
	 *         visible.
	 * @since 3.0
	 */
	public boolean getCoolBarVisible() {
		return coolBarVisible;
	}

	/**
	 * @param visible
	 *            whether the perspective bar should be shown. This is only
	 *            applicable if the window configurer also wishes either the
	 *            perspective bar to be visible.
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
	 * @return whether the perspective bar should be shown. This is only
	 *         applicable if the window configurer also wishes either the
	 *         perspective bar to be visible.
	 * @since 3.0
	 */
	public boolean getPerspectiveBarVisible() {
		return perspectiveBarVisible;
	}
	
	/**
	 * Tell the workbench window a visible state for the fastview bar. This is
	 * only applicable if the window configurer also wishes the fast view bar to
	 * be visible.
	 * 
	 * @param visible
	 *            <code>true</code> or <code>false</code>
	 * @since 3.2
	 */
	public void setFastViewBarVisible(boolean visible) {
		boolean oldValue = fastViewBarVisible;
		fastViewBarVisible = visible;
		if (oldValue != fastViewBarVisible) {
			updateLayoutDataForContents();
		}
	}
	
	/**
	 * The workbench window take on the fastview bar. This is only applicable if
	 * the window configurer also wishes the fast view bar to be visible.
	 * 
	 * @return <code>true</code> if the workbench window thinks the fastview
	 *         bar should be visible.
	 * @since 3.2
	 */
	public boolean getFastViewBarVisible() {
		return fastViewBarVisible;
	}

	/**
	 * @param visible
	 *            whether the perspective bar should be shown. This is only
	 *            applicable if the window configurer also wishes either the
	 *            perspective bar to be visible.
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
	 * @return whether the perspective bar should be shown. This is only
	 *         applicable if the window configurer also wishes either the
	 *         perspective bar to be visible.
	 * @since 3.0
	 */
	public boolean getStatusLineVisible() {
		return statusLineVisible;
	}

	/**
	 * Note that this will only have an effect if the default implementation of
	 * WorkbenchAdvisor.createWindowContents() has been invoked.
	 * 
	 * called IWorkbench
	 * 
	 * @since 3.0
	 */
	private void updateLayoutDataForContents() {
		if (defaultLayout == null) {
			return;
		}

		// @issue this is not ideal; coolbar and perspective shortcuts should be
		// separately configurable
		if ((getCoolBarVisible() && getWindowConfigurer().getShowCoolBar())
				|| (getPerspectiveBarVisible() && getWindowConfigurer()
						.getShowPerspectiveBar())) {
			if (defaultLayout.getTrim(topBarTrim.getId()) == null) {
				defaultLayout.addTrim(SWT.TOP, topBarTrim);
			}
			topBar.setVisible(true);
		} else {
			defaultLayout.removeTrim(topBarTrim);
			topBar.setVisible(false);
		}

		if (fastViewBar != null) {
			if (getFastViewBarVisible()
					&& getWindowConfigurer().getShowFastViewBars()) {
				int side = fastViewBar.getSide();

				if (defaultLayout.getTrim(fastViewBar.getId()) == null) {
					defaultLayout.addTrim(side, fastViewBar);
				}
				fastViewBar.getControl().setVisible(true);
			} else {
				defaultLayout.removeTrim(fastViewBar);
				fastViewBar.getControl().setVisible(false);
			}
		}

		if (getStatusLineVisible() && getWindowConfigurer().getShowStatusLine()) {
			if (defaultLayout.getTrim(getStatusLineTrim().getId()) == null) {
				defaultLayout.addTrim(SWT.BOTTOM, getStatusLineTrim());
			}
			getStatusLineManager().getControl().setVisible(true);
		} else {
			defaultLayout.removeTrim(getStatusLineTrim());
			getStatusLineManager().getControl().setVisible(false);
		}

		if (heapStatus != null) {
			if (getShowHeapStatus()) {
				if (heapStatus.getLayoutData() == null) {
					heapStatusTrim.setWidthHint(heapStatus.computeSize(
							SWT.DEFAULT, SWT.DEFAULT).x);
					heapStatusTrim
							.setHeightHint(getStatusLineManager().getControl()
									.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
				}

				if (defaultLayout.getTrim(heapStatusTrim.getId()) == null) {
					defaultLayout.addTrim(SWT.BOTTOM, heapStatusTrim);
				}
				heapStatus.setVisible(true);
			} else {

				defaultLayout.removeTrim(heapStatusTrim);
				heapStatus.setVisible(false);
			}
		}

		if (progressRegion != null) {
			if (getWindowConfigurer().getShowProgressIndicator()) {
				if (defaultLayout.getTrim(progressRegion.getId()) == null) {
					defaultLayout.addTrim(SWT.BOTTOM, progressRegion);
				}
				progressRegion.getControl().setVisible(true);
			} else {
				defaultLayout.removeTrim(progressRegion);
				progressRegion.getControl().setVisible(false);
			}
		}
		
		defaultLayout.setCenterControl(getPageComposite());

		// Re-populate the trim elements
		if (trimMgr2 != null)
			trimMgr2.update(true, false, !topBar.getVisible());
		if (trimContributionMgr != null)
			trimContributionMgr.update(true,  !topBar.getVisible());
	}

	public boolean getShowFastViewBars() {
		return getWindowConfigurer().getShowFastViewBars();
	}

	/**
	 * Set the layout data for the contents of the window.
	 */
	private void setLayoutDataForContents() {
		updateLayoutDataForContents();
	}

	/**
	 * Returns the fast view bar.
	 */
	public FastViewBar getFastViewBar() {
		return fastViewBar;
	}

	/**
	 * Returns the perspective bar.
	 * 
	 * @return Returns the perspective bar, or <code>null</code> if it has not
	 *         been initialized.
	 */
	public PerspectiveBarManager getPerspectiveBar() {
		return perspectiveSwitcher == null ? null : perspectiveSwitcher
				.getPerspectiveBar();
	}

    /**
     * Returns the action presentation for dynamic UI
     * @return action presentation
     */
    public ActionPresentation getActionPresentation() {
        if (actionPresentation == null) {
        	actionPresentation = new ActionPresentation(this);
        }
        return actionPresentation;
    }
    
    /**
     * Return the action bar presentation used for creating toolbars.  This
     * is for internal use only, used for consistency with the window.
     * 
     * @return the presentation used.
     */
    public IActionBarPresentationFactory getActionBarPresentationFactory() {
    	// allow replacement of the actionbar presentation
    	IActionBarPresentationFactory actionBarPresentation;        	
    	AbstractPresentationFactory presentationFactory = 
    		getWindowConfigurer().getPresentationFactory();
    	if (presentationFactory instanceof IActionBarPresentationFactory) {
        	actionBarPresentation = ((IActionBarPresentationFactory) presentationFactory);
    	} else {
			actionBarPresentation = new DefaultActionBarPresentationFactory();
		}      
    	
    	return actionBarPresentation;        	
    }
    
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.ApplicationWindow#showTopSeperator()
	 */
	protected boolean showTopSeperator() {
		return false;
	}

	/**
     * Returns a new cool bar manager for the window.
     * <p>
     * Subclasses may override this method to customize the cool bar manager.
     * </p>
     * 
     * @return a cool bar manager
	 * @since 3.2
     */
    protected ICoolBarManager createCoolBarManager2(int style) {
        return getActionBarPresentationFactory().createCoolBarManager();
    }

    /**
     * Returns a new tool bar manager for the window.
     * <p>
     * Subclasses may override this method to customize the tool bar manager.
     * </p>
     * @return a tool bar manager
	 * @since 3.2
     */
    protected IToolBarManager createToolBarManager2(int style) {
        return getActionBarPresentationFactory().createToolBarManager();
    }
    
    /**
	 * Delegate to the presentation factory.
	 * 
	 * @see org.eclipse.jface.window.ApplicationWindow#createStatusLineManager
	 * @since 3.0
	 */
	protected StatusLineManager createStatusLineManager() {
		// @issue ApplicationWindow and WorkbenchWindow should allow full
		// IStatusLineManager
		return (StatusLineManager) getWindowConfigurer()
				.getPresentationFactory().createStatusLineManager();
	}

	/**
	 * Delegate to the presentation factory.
	 * 
	 * @see org.eclipse.jface.window.ApplicationWindow#createStatusLine
	 * @since 3.0
	 */
	protected void createStatusLine(Shell shell) {
		getWindowConfigurer().getPresentationFactory().createStatusLineControl(
				getStatusLineManager(), shell);
	}

	/**
	 * Updates the fast view bar, if present. TODO: The fast view bar should
	 * update itself as necessary. All calls to this should be cleaned up.
	 * 
	 * @since 3.0
	 */
	public void updateFastViewBar() {
		if (getFastViewBar() != null) {
			getFastViewBar().update(true);
		}
	}

	/**
	 * @return Returns the progressRegion.
	 */
	public ProgressRegion getProgressRegion() {
		return progressRegion;
	}

	/**
	 * Adds the given control to the specified side of this window's trim.
	 * 
	 * @param trim
	 *            the bar's IWindowTrim
	 * @param side
	 *            one of <code>SWT.LEFT</code>,<code>SWT.BOTTOM</code>,
	 *            or <code>SWT.RIGHT</code> (only LEFT has been tested)
	 * @since 3.0
	 */
	public void addToTrim(IWindowTrim trim, int side) {
		IWindowTrim reference = null;
		defaultLayout.addTrim(side, trim, reference);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindow#getExtensionTracker()
	 */
	public IExtensionTracker getExtensionTracker() {
		if (tracker == null) {
			tracker = new UIExtensionTracker(getWorkbench().getDisplay());
		}
		return tracker;
	}

	/**
	 * Creates the perspective customization dialog.
	 * 
	 * @param persp
	 *            perspective to customize
	 * 
	 * @return a new perspective customization dialog
	 * @since 3.1
	 */
	public CustomizePerspectiveDialog createCustomizePerspectiveDialog(
			Perspective persp) {
		return new CustomizePerspectiveDialog(getWindowConfigurer(), persp);
	}

	/**
	 * Returns the default page input for workbench pages opened in this window.
	 * 
	 * @return the default page input or <code>null</code> if none
	 * @since 3.1
	 */
	IAdaptable getDefaultPageInput() {
		return getWorkbenchImpl().getDefaultPageInput();
	}

	/**
	 * Add a listener for perspective reordering.
	 * 
	 * @param listener
	 */
	public void addPerspectiveReorderListener(IReorderListener listener) {
		if (perspectiveSwitcher != null) {
			perspectiveSwitcher.addReorderListener(listener);
		}
	}

	/**
	 * Show the heap status
	 * 
	 * @param selection
	 */
	public void showHeapStatus(boolean selection) {
		if (selection) {
			if (heapStatus == null) {
				createHeapStatus(getShell());
				updateLayoutDataForContents();
				getShell().layout();
			}
		} else {
			if (heapStatus != null) {
				heapStatus.dispose();
				heapStatus = null;
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindow#getTrimManager()
	 */
	public ITrimManager getTrimManager() {
		return defaultLayout;
	}

	/**
	 * Initializes all of the default command-based services for the workbench
	 * window.
	 */
	private final void initializeDefaultServices() {
		serviceLocator.registerService(IWorkbenchWindow.class, this);
		
		final ActionCommandMappingService mappingService = new ActionCommandMappingService();
		serviceLocator.registerService(IActionCommandMappingService.class,
				mappingService);

		final LegacyActionPersistence actionPersistence = new LegacyActionPersistence(
				this);
		serviceLocator.registerService(LegacyActionPersistence.class,
				actionPersistence);
		actionPersistence.read();

	}

	public final Object getService(final Class key) {
		return serviceLocator.getService(key);
	}

	public final boolean hasService(final Class key) {
		return serviceLocator.hasService(key);
	}
	
	/**
	 * Toggle the visibility of the coolbar/perspective bar. This method
	 * respects the window configurer and will only toggle visibility if the
	 * item in question was originally declared visible by the window advisor.
	 * 
	 * @since 3.3
	 */
	public void toggleToolbarVisibility() {
		boolean coolbarVisible = getCoolBarVisible();
		boolean perspectivebarVisible = getPerspectiveBarVisible();
		// only toggle the visibility of the components that
		// were on initially
		if (getWindowConfigurer().getShowCoolBar()) {
			setCoolBarVisible(!coolbarVisible);
			firePropertyChanged(PROP_COOLBAR_VISIBLE,
					coolbarVisible ? Boolean.TRUE : Boolean.FALSE,
					!coolbarVisible ? Boolean.TRUE : Boolean.FALSE);
		}
		if (getWindowConfigurer().getShowPerspectiveBar()) {
			setPerspectiveBarVisible(!perspectivebarVisible);
			firePropertyChanged(PROP_PERSPECTIVEBAR_VISIBLE,
					coolbarVisible ? Boolean.TRUE : Boolean.FALSE,
					!coolbarVisible ? Boolean.TRUE : Boolean.FALSE);
		}
		getShell().layout();
	}

	/*package*/ void addBackgroundSaveListener(IBackgroundSaveListener listener) {
		backgroundSaveListeners.add(listener);
	}
	
	/*package*/ void fireBackgroundSaveStarted() {
		Object[] listeners = backgroundSaveListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			IBackgroundSaveListener listener = (IBackgroundSaveListener) listeners[i];
			listener.handleBackgroundSaveStarted();
		}
	}

	/*package*/ void removeBackgroundSaveListener(IBackgroundSaveListener listener) {
		backgroundSaveListeners.remove(listener);
	}
}

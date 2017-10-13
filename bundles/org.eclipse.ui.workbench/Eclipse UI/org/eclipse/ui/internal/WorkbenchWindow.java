/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Zhongwei Zhao - Bug 379495 - Two "Run" on top menu
 *     Patrick Chuong - Bug 391481 - Contributing perspectiveExtension, hiddenMenuItem
 *     								 removes a menu from multiple perspectives
 *     René Brandstetter - Bug 411821 - [QuickAccess] Contribute SearchField
 *                                      through a fragment or other means
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 431446, 433979, 440810, 441184, 472654, 486632
 *     Denis Zygann <d.zygann@web.de> - Bug 457390
 *     Andrey Loskutov <loskutov@gmx.de> - Bug 372799
 *     Axel Richard <axel.richard@obeo.fr> - Bug 354538
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.e4.core.commands.internal.HandlerServiceImpl;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.internal.workbench.OpaqueElementUtil;
import org.eclipse.e4.ui.internal.workbench.PartServiceSaveHandler;
import org.eclipse.e4.ui.internal.workbench.URIHelper;
import org.eclipse.e4.ui.internal.workbench.renderers.swt.IUpdateService;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuSeparator;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.model.internal.Position;
import org.eclipse.e4.ui.model.internal.PositionInfo;
import org.eclipse.e4.ui.services.EContextService;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ISaveHandler;
import org.eclipse.e4.ui.workbench.modeling.IWindowCloseHandler;
import org.eclipse.e4.ui.workbench.renderers.swt.MenuManagerRenderer;
import org.eclipse.e4.ui.workbench.renderers.swt.MenuManagerRendererFilter;
import org.eclipse.e4.ui.workbench.renderers.swt.ToolBarManagerRenderer;
import org.eclipse.e4.ui.workbench.renderers.swt.TrimBarLayout;
import org.eclipse.e4.ui.workbench.renderers.swt.TrimmedPartLayout;
import org.eclipse.e4.ui.workbench.swt.factories.IRendererFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.action.AbstractGroupMarker;
import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManagerOverrides;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.SubContributionItem;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.internal.provisional.action.CoolBarManager2;
import org.eclipse.jface.internal.provisional.action.ICoolBarManager2;
import org.eclipse.jface.internal.provisional.action.IToolBarManager2;
import org.eclipse.jface.internal.provisional.action.ToolBarManager2;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ActiveShellExpression;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPageService;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISaveablesLifecycleListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.ISources;
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
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.StartupThreading.StartupRunnable;
import org.eclipse.ui.internal.actions.CommandAction;
import org.eclipse.ui.internal.commands.SlaveCommandService;
import org.eclipse.ui.internal.contexts.ContextService;
import org.eclipse.ui.internal.dialogs.cpd.CustomizePerspectiveDialog;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityPart;
import org.eclipse.ui.internal.e4.compatibility.ModeledPageLayout;
import org.eclipse.ui.internal.e4.compatibility.SelectionService;
import org.eclipse.ui.internal.handlers.ActionCommandMappingService;
import org.eclipse.ui.internal.handlers.IActionCommandMappingService;
import org.eclipse.ui.internal.handlers.LegacyHandlerService;
import org.eclipse.ui.internal.menus.ActionSet;
import org.eclipse.ui.internal.menus.IActionSetsListener;
import org.eclipse.ui.internal.menus.LegacyActionPersistence;
import org.eclipse.ui.internal.menus.MenuHelper;
import org.eclipse.ui.internal.menus.SlaveMenuService;
import org.eclipse.ui.internal.misc.UIListenerLogging;
import org.eclipse.ui.internal.progress.ProgressRegion;
import org.eclipse.ui.internal.provisional.application.IActionBarConfigurer2;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.UIExtensionTracker;
import org.eclipse.ui.internal.services.EvaluationReference;
import org.eclipse.ui.internal.services.IServiceLocatorCreator;
import org.eclipse.ui.internal.services.IWorkbenchLocationService;
import org.eclipse.ui.internal.services.ServiceLocator;
import org.eclipse.ui.internal.services.WorkbenchLocationService;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.services.IDisposable;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.services.IServiceScopes;
import org.eclipse.ui.views.IViewDescriptor;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
/**
 * A window within the workbench.
 */
public class WorkbenchWindow implements IWorkbenchWindow {

	/**
	 * The 'elementId' of the spacer used to right-align it in the trim
	 */
	public static final String PERSPECTIVE_SPACER_ID = "PerspectiveSpacer"; //$NON-NLS-1$

	public static final String STATUS_LINE_ID = "org.eclipse.ui.StatusLine"; //$NON-NLS-1$

	public static final String TRIM_CONTRIBUTION_URI = "bundleclass://org.eclipse.ui.workbench/org.eclipse.ui.internal.StandardTrim"; //$NON-NLS-1$

	private static final String MAIN_TOOLBAR_ID = ActionSet.MAIN_TOOLBAR;
	private static final String COMMAND_ID_TOGGLE_COOLBAR = "org.eclipse.ui.ToggleCoolbarAction"; //$NON-NLS-1$

	public static final String ACTION_SET_CMD_PREFIX = "AS::"; //$NON-NLS-1$

	private static final String PERSISTED_STATE_RESTORED = "isRestored"; //$NON-NLS-1$

	@Inject
	private IWorkbench workbench;
	@Inject
	private MTrimmedWindow model;
	@Inject
	private IPresentationEngine engine;

	@Inject
	private IRendererFactory rendererFactory;

	@Inject
	private MApplication application;

	@Inject
	EModelService modelService;

	@Inject
	private IEventBroker eventBroker;

	@Inject
	@Optional
	private Logger logger;

	@Inject
	private IExtensionRegistry extensionRegistry;

	private WorkbenchPage page;

	private WorkbenchWindowAdvisor windowAdvisor;

	private ActionBarAdvisor actionBarAdvisor;

	private MenuManagerRenderer renderer;

	private MMenu mainMenu;

	private PageListenerList pageListeners = new PageListenerList();

	private PerspectiveListenerList perspectiveListeners = new PerspectiveListenerList();

	private PartService partService = new WWinPartService();

	private WWinActionBars actionBars;

	private boolean updateDisabled = false;

	private boolean closing = false;

	private boolean shellActivated = false;

	ProgressRegion progressRegion = null;

	private List<MTrimElement> workbenchTrimElements = new ArrayList<>();

	private Map<MToolControl, IConfigurationElement> iceMap = new HashMap<>();

	public IConfigurationElement getICEFor(MToolControl mtc) {
		return iceMap.get(mtc);
	}

	/**
	 * The map of services maintained by the workbench window. These services
	 * are initialized when the workbench window is being constructed by
	 * dependency injection.
	 */
	private ServiceLocator serviceLocator;

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
	private ListenerList<IPropertyChangeListener> genericPropertyListeners = new ListenerList<>();

	private IAdaptable input;

	private IPerspectiveDescriptor perspective;

	private EventHandler windowWidgetHandler = new EventHandler() {
		@Override
		public void handleEvent(Event event) {
			if (event.getProperty(UIEvents.EventTags.ELEMENT) == model
					&& event.getProperty(UIEvents.EventTags.NEW_VALUE) == null) {
				// HandledContributionItem.toolItemUpdater.removeWindowRunnable(menuUpdater);
				manageChanges = false;
				canUpdateMenus = false;
				menuUpdater = null;

				MMenu menu = model.getMainMenu();
				if (menu != null) {
					engine.removeGui(menu);
					model.setMainMenu(null);
				}

				eventBroker.unsubscribe(windowWidgetHandler);
			}
		}
	};

	static final String TEXT_DELIMITERS = TextProcessor.getDefaultDelimiters() + "-"; //$NON-NLS-1$

	// constants for shortcut bar group ids
	static final String GRP_PAGES = "pages"; //$NON-NLS-1$

	static final String GRP_PERSPECTIVES = "perspectives"; //$NON-NLS-1$

	static final String GRP_FAST_VIEWS = "fastViews"; //$NON-NLS-1$

	// static fields for inner classes.
	static final int VGAP = 0;

	static final int CLIENT_INSET = 3;

	static final int BAR_SIZE = 23;

	/** Marks the beginning of a tag which contains positioning information. */
	static final String MOVE_TAG = "move_"; //$NON-NLS-1$

	/**
	 * Ordered list of element IDs which belong to the QuickAccess
	 * {@link MToolControl}s.
	 *
	 * <p>
	 * Element IDs which belong to QuickAccess:
	 * <ul>
	 * <li><code>Spacer Glue</code></li>
	 * <li><code>SearchField</code></li>
	 * <li><code>Search-PS Glue</code></li>
	 * </ul>
	 * </p>
	 */
	private static final List<String> QUICK_ACCESS_ELEMENT_IDS = Collections
			.unmodifiableList(Arrays.asList("Spacer Glue", "SearchField", "Search-PS Glue")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

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
	 * The status line visibility change property. for internal use only.
	 *
	 * @since 3.4
	 */
	public static final String PROP_STATUS_LINE_VISIBLE = "statusLineVisible"; //$NON-NLS-1$

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
	 *            the type of submenu, one of: {@link #NEW_WIZARD_SUBMENU
	 *            NEW_WIZARD_SUBMENU}, {@link #OPEN_PERSPECTIVE_SUBMENU
	 *            OPEN_PERSPECTIVE_SUBMENU}, {@link #SHOW_VIEW_SUBMENU
	 *            SHOW_VIEW_SUBMENU}
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
	 *            the type of submenu, one of: {@link #NEW_WIZARD_SUBMENU
	 *            NEW_WIZARD_SUBMENU}, {@link #OPEN_PERSPECTIVE_SUBMENU
	 *            OPEN_PERSPECTIVE_SUBMENU}, {@link #SHOW_VIEW_SUBMENU
	 *            SHOW_VIEW_SUBMENU}
	 * @return <code>true</code> if window contains submenu, <code>false</code>
	 *         otherwise
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
			| ActionBarAdvisor.FILL_COOL_BAR | ActionBarAdvisor.FILL_STATUS_LINE;

	/**
	 * Creates and initializes a new workbench window.
	 *
	 * @param input
	 *            the input for this workbench window
	 * @param pers
	 *            the perspective to initialize this workbench window with
	 */
	public WorkbenchWindow(IAdaptable input, IPerspectiveDescriptor pers) {
		this.input = input;
		perspective = pers;
	}

	@PostConstruct
	public void setup() {
		try {
			// if workbench window is opened as a result of command execution,
			// the context in which the new workbench window's commands are
			// initialized has to to match the workbench context
			final IEclipseContext windowContext = model.getContext();
			HandlerServiceImpl.push(windowContext.getParent(), null);

			// Initialize a previous 'saved' state if applicable. We no longer
			// update the preference store.
			if (getModel().getPersistedState().containsKey(IPreferenceConstants.COOLBAR_VISIBLE)) {
				this.coolBarVisible = Boolean.parseBoolean(getModel().getPersistedState().get(
						IPreferenceConstants.COOLBAR_VISIBLE));
			} else {
				this.coolBarVisible = PrefUtil.getInternalPreferenceStore().getBoolean(
						IPreferenceConstants.COOLBAR_VISIBLE);
				getModel().getPersistedState().put(IPreferenceConstants.COOLBAR_VISIBLE,
						Boolean.toString(this.coolBarVisible));
			}
			if (getModel().getPersistedState().containsKey(
					IPreferenceConstants.PERSPECTIVEBAR_VISIBLE)) {
				this.perspectiveBarVisible = Boolean.parseBoolean(getModel().getPersistedState()
						.get(IPreferenceConstants.PERSPECTIVEBAR_VISIBLE));
			} else {
				this.perspectiveBarVisible = PrefUtil.getInternalPreferenceStore().getBoolean(
						IPreferenceConstants.PERSPECTIVEBAR_VISIBLE);
				getModel().getPersistedState().put(IPreferenceConstants.PERSPECTIVEBAR_VISIBLE,
						Boolean.toString(this.perspectiveBarVisible));
			}

			IServiceLocatorCreator slc = workbench
					.getService(IServiceLocatorCreator.class);
			this.serviceLocator = (ServiceLocator) slc.createServiceLocator(workbench, null,
					new IDisposable() {
						@Override
						public void dispose() {
							final Shell shell = getShell();
							if (shell != null && !shell.isDisposed()) {
								close();
							}
						}
					}, windowContext);

			windowContext.set(IExtensionTracker.class.getName(), new ContextFunction() {
				@Override
				public Object compute(IEclipseContext context, String contextKey) {
					if (tracker == null) {
						tracker = new UIExtensionTracker(getWorkbench().getDisplay());
					}
					return tracker;
				}
			});

			windowContext.set(IWindowCloseHandler.class.getName(), new IWindowCloseHandler() {
				@Override
				public boolean close(MWindow window) {
					return getWindowAdvisor().preWindowShellClose() && WorkbenchWindow.this.close();
				}
			});

			final ISaveHandler defaultSaveHandler = windowContext.get(ISaveHandler.class);
			final PartServiceSaveHandler localSaveHandler = new PartServiceSaveHandler() {
				@Override
				public Save promptToSave(MPart dirtyPart) {
					Object object = dirtyPart.getObject();
					if (object instanceof CompatibilityPart) {
						IWorkbenchPart part = ((CompatibilityPart) object).getPart();
						ISaveablePart saveable = SaveableHelper.getSaveable(part);
						if (saveable != null) {
							if (!saveable.isSaveOnCloseNeeded()) {
								return Save.NO;
							}
							return SaveableHelper.savePart(saveable, part,
									WorkbenchWindow.this, true) ? Save.NO : Save.CANCEL;
						}
					}
					return defaultSaveHandler.promptToSave(dirtyPart);
				}

				@Override
				public Save[] promptToSave(Collection<MPart> dirtyParts) {
					LabelProvider labelProvider = new LabelProvider() {
						@Override
						public String getText(Object element) {
							return ((MPart) element).getLocalizedLabel();
						}
					};
					List<MPart> parts = new ArrayList<>(dirtyParts);
					ListSelectionDialog dialog = new ListSelectionDialog(getShell(), parts,
							ArrayContentProvider.getInstance(), labelProvider,
							WorkbenchMessages.EditorManager_saveResourcesMessage);
					dialog.setInitialSelections(parts.toArray());
					dialog.setTitle(WorkbenchMessages.EditorManager_saveResourcesTitle);
					if (dialog.open() == IDialogConstants.CANCEL_ID) {
						return new Save[] { Save.CANCEL };
					}

					Object[] toSave = dialog.getResult();
					Save[] retSaves = new Save[parts.size()];
					Arrays.fill(retSaves, Save.NO);
					for (int i = 0; i < retSaves.length; i++) {
						MPart part = parts.get(i);
						for (Object o : toSave) {
							if (o == part) {
								retSaves[i] = Save.YES;
								break;
							}
						}
					}
					return retSaves;
				}

				@Override
				public boolean save(MPart dirtyPart, boolean confirm) {
					Object object = dirtyPart.getObject();
					if (object instanceof CompatibilityPart) {
						IWorkbenchPart workbenchPart = ((CompatibilityPart) object).getPart();
						if (SaveableHelper.isSaveable(workbenchPart)) {
							SaveablesList saveablesList = (SaveablesList) PlatformUI.getWorkbench()
									.getService(ISaveablesLifecycleListener.class);
							Object saveResult = saveablesList.preCloseParts(
									Collections.singletonList(workbenchPart), true,
									WorkbenchWindow.this);
							return saveResult != null;
						}
					}
					return super.save(dirtyPart, confirm);
				}

				@Override
				public boolean saveParts(Collection<MPart> dirtyParts, boolean confirm) {
					ArrayList<IWorkbenchPart> saveableParts = new ArrayList<>();
					for (MPart part : dirtyParts) {
						Object object = part.getObject();
						if (object instanceof CompatibilityPart) {
							IWorkbenchPart workbenchPart = ((CompatibilityPart) object).getPart();
							if (SaveableHelper.isSaveable(workbenchPart)) {
								saveableParts.add(workbenchPart);
							}
						}
					}
					if (saveableParts.isEmpty()) {
						return super.saveParts(dirtyParts, confirm);
					}

					SaveablesList saveablesList = (SaveablesList) PlatformUI.getWorkbench()
							.getService(ISaveablesLifecycleListener.class);
					Object saveResult = saveablesList.preCloseParts(saveableParts, true,
							WorkbenchWindow.this);
					return saveResult != null;
				}
			};
			localSaveHandler.logger = logger;
			windowContext.set(ISaveHandler.class, localSaveHandler);

			windowContext.set(IWorkbenchWindow.class.getName(), this);
			windowContext.set(IPageService.class, this);
			windowContext.set(IPartService.class, partService);

			windowContext.set(ISources.ACTIVE_WORKBENCH_WINDOW_NAME, this);
			windowContext.set(ISources.ACTIVE_WORKBENCH_WINDOW_SHELL_NAME, getShell());
			EContextService cs = windowContext.get(EContextService.class);
			cs.activateContext(IContextService.CONTEXT_ID_WINDOW);
			cs.getActiveContextIds();

			initializeDefaultServices();

			/*
			 * Remove the second QuickAccess control if an older workspace is
			 * opened.
			 *
			 * An older workspace will create an ApplicationModel which already
			 * contains the QuickAccess elements, from the old
			 * "popuolateTopTrimContribution()" method. The new implementation
			 * of this method doesn't add the QuickAccess elements anymore but
			 * an old workbench.xmi still has these entries in it and so they
			 * need to be removed.
			 */
			cleanLegacyQuickAccessContribution();

			// register with the tracker

			fireWindowOpening();
			configureShell(getShell(), windowContext);

			try {
				page = new WorkbenchPage(this, input);
			} catch (WorkbenchException e) {
				WorkbenchPlugin.log(e);
			}

			ContextInjectionFactory.inject(page, model.getContext());
			windowContext.set(IWorkbenchPage.class, page);

			menuManager.setOverrides(menuOverride);
			((CoolBarToTrimManager) getCoolBarManager2()).setOverrides(toolbarOverride);

			// Fill the action bars
			fillActionBars(FILL_ALL_ACTION_BARS);
			firePageOpened();

			populateTopTrimContributions();
			populateBottomTrimContributions();

			// Trim gets populated during rendering (?) so make sure we have al/
			// sides. See bug 383269 for details
			modelService.getTrim(model, SideValue.LEFT);
			modelService.getTrim(model, SideValue.RIGHT);

			// move the QuickAccess ToolControl to the correct position (only if
			// it exists)
			positionQuickAccess();

			Shell shell = (Shell) model.getWidget();
			if (model.getMainMenu() == null) {
				mainMenu = modelService.createModelElement(MMenu.class);
				mainMenu.setElementId(ActionSet.MAIN_MENU);

				renderer = (MenuManagerRenderer) rendererFactory
						.getRenderer(mainMenu, null);
				renderer.linkModelToManager(mainMenu, menuManager);
				fill(renderer, mainMenu, menuManager);
				model.setMainMenu(mainMenu);
				final Menu menu = (Menu) engine.createGui(mainMenu, model.getWidget(),
						model.getContext());
				shell.setMenuBar(menu);

				menuUpdater = new Runnable() {
					@Override
					public void run() {
						try {
							if (model.getMainMenu() == null || model.getWidget() == null
									|| menu.isDisposed() || mainMenu.getWidget() == null) {
								return;
							}
							MenuManagerRendererFilter.updateElementVisibility(mainMenu, renderer,
									menuManager, windowContext.getActiveLeaf(), 1, false);
							menuManager.update(true);
						} finally {
							canUpdateMenus = true;
						}
					}
				};

				RunAndTrack menuChangeManager = new RunAndTrack() {
					@Override
					public boolean changed(IEclipseContext context) {
						ExpressionInfo info = new ExpressionInfo();
						IEclipseContext leafContext = windowContext.getActiveLeaf();
						MenuManagerRendererFilter.collectInfo(info, mainMenu, renderer,
								leafContext, true);
						// if one of these variables change, re-run the RAT
						for (String name : info.getAccessedVariableNames()) {
							leafContext.get(name);
						}
						if (canUpdateMenus && workbench.getDisplay() != null) {
							canUpdateMenus = false;
							workbench.getDisplay().asyncExec(menuUpdater);
						}
						return manageChanges;
					}
				};
				windowContext.runAndTrack(menuChangeManager);
			}

			eventBroker.subscribe(UIEvents.UIElement.TOPIC_WIDGET, windowWidgetHandler);

			boolean newWindow = setupPerspectiveStack(windowContext);
			partService.setPage(page);
			page.setPerspective(perspective);
			firePageActivated();

			if (newWindow) {
				page.fireInitialPartVisibilityEvents();
			} else {
				page.updatePerspectiveActionSets();
			}
			updateActionSets();

			IPreferenceStore preferenceStore = PrefUtil.getAPIPreferenceStore();
			boolean enableAnimations = preferenceStore
					.getBoolean(IWorkbenchPreferenceConstants.ENABLE_ANIMATIONS);
			preferenceStore.setValue(IWorkbenchPreferenceConstants.ENABLE_ANIMATIONS, false);

			// Hack!! don't show the intro if there's more than one open
			// perspective
			List<MPerspective> persps = modelService.findElements(model, null, MPerspective.class,
					null);
			if (persps.size() > 1) {
				PrefUtil.getAPIPreferenceStore().setValue(IWorkbenchPreferenceConstants.SHOW_INTRO,
						false);
				PrefUtil.saveAPIPrefs();
			}

			if (Boolean.valueOf(getModel().getPersistedState().get(PERSISTED_STATE_RESTORED))) {
				SafeRunnable.run(new SafeRunnable() {

					@Override
					public void run() throws Exception {
						getWindowAdvisor().postWindowRestore();
					}
				});
			} else {
				getModel().getPersistedState().put(PERSISTED_STATE_RESTORED, Boolean.TRUE.toString());
			}

			getWindowAdvisor().postWindowCreate();
			getWindowAdvisor().openIntro();

			preferenceStore.setValue(IWorkbenchPreferenceConstants.ENABLE_ANIMATIONS,
					enableAnimations);

			getShell().setData(this);
			trackShellActivation();
		} finally {
			HandlerServiceImpl.pop();
		}
	}

	@PreDestroy
	void preDestroy() {
		if (mainMenu != null) {
			renderer.clearModelToManager(mainMenu, menuManager);
			mainMenu = null;
		}
		renderer = null;
	}

	private void configureShell(Shell shell, IEclipseContext context) {
		String title = getWindowConfigurer().basicGetTitle();
		if (title != null) {
			shell.setText(TextProcessor.process(title, TEXT_DELIMITERS));
		}
		workbench.getHelpSystem().setHelp(shell, IWorkbenchHelpContextIds.WORKBENCH_WINDOW);

		IContextService contextService = context.get(IContextService.class);
		contextService.registerShell(shell, IContextService.TYPE_WINDOW);
		if (model.getContext().get(E4Workbench.NO_SAVED_MODEL_FOUND) != null) {
			Point initialSize = getWindowConfigurer().getInitialSize();
			Rectangle bounds = shell.getBounds();
			// actually set the shell size, so that setting the
			// menuBar on the shell doesn't override the model changes.
			bounds.width = initialSize.x;
			bounds.height = initialSize.y;
			shell.setBounds(bounds);
		}
	}

	private boolean setupPerspectiveStack(IEclipseContext context) {
		IPerspectiveRegistry registry = getWorkbench().getPerspectiveRegistry();
		String forcedPerspectiveId = (String) context.get(E4Workbench.FORCED_PERSPECTIVE_ID);

		if (forcedPerspectiveId != null) {
			perspective = registry.findPerspectiveWithId(forcedPerspectiveId);
		}

		List<MPerspectiveStack> perspStackList = modelService.findElements(model, null,
				MPerspectiveStack.class, null);
		MPerspective selectedPersp = null;

		if (!perspStackList.isEmpty()) {
			selectedPersp = perspStackList.get(0).getSelectedElement();
		}

		if (forcedPerspectiveId == null && selectedPersp != null) {
			perspective = registry.findPerspectiveWithId(selectedPersp.getElementId());
		}

		if (perspective == null) {
			perspective = registry.findPerspectiveWithId(registry.getDefaultPerspective());
		}

		// the perspective stack doesn't have a selected element what means that
		// we have a new window
		return selectedPersp == null;
	}

	private boolean manageChanges = true;
	private boolean canUpdateMenus = true;

	void populateTopTrimContributions() {
		getCoolBarManager2().update(true);
		getCoolBarManager2().add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

		final MTrimBar trimBar = getTopTrim();
		// TODO why aren't these added as trim contributions
		// that would remove everything from this method except the fill(*)
		/*
		 * Reason Why: The setup() method which calls this method also calls the
		 * ActionBarAdvisor to fill the TopTrim-Bar. Both this and the
		 * ActionBarAdvisor fill method will be called after the entire
		 * application model and all its fragments have been build already. This
		 * leads to the effect that all the elements contributed via the
		 * application model would be placed in front of the elements
		 * contributed by the setup() method. (Means all the "Save", "Save All",
		 * and so on, buttons which are normally placed at the beginning of the
		 * trimbar (left) would be moved to the end of it (right).)
		 */
		MToolControl spacerControl = (MToolControl) modelService.find(PERSPECTIVE_SPACER_ID, model);
		if (spacerControl == null) {
			spacerControl = modelService.createModelElement(MToolControl.class);
			spacerControl.setElementId(PERSPECTIVE_SPACER_ID);
			spacerControl
					.setContributionURI("bundleclass://org.eclipse.e4.ui.workbench.renderers.swt/org.eclipse.e4.ui.workbench.renderers.swt.LayoutModifierToolControl"); //$NON-NLS-1$
			spacerControl.getTags().add(TrimBarLayout.SPACER);
			spacerControl.getTags().add("SHOW_RESTORE_MENU"); //$NON-NLS-1$
			trimBar.getChildren().add(spacerControl);
		} else {
			if (!spacerControl.getTags().contains("SHOW_RESTORE_MENU")) { //$NON-NLS-1$
				spacerControl.getTags().add("SHOW_RESTORE_MENU"); //$NON-NLS-1$
			}
		}

		MToolControl switcherControl = (MToolControl) modelService.find(
				"PerspectiveSwitcher", model); //$NON-NLS-1$
		if (switcherControl == null && getWindowConfigurer().getShowPerspectiveBar()) {
			switcherControl = modelService.createModelElement(MToolControl.class);
			switcherControl.setToBeRendered(getWindowConfigurer().getShowPerspectiveBar());
			switcherControl.setElementId("PerspectiveSwitcher"); //$NON-NLS-1$
			switcherControl.getTags().add(IPresentationEngine.DRAGGABLE);
			switcherControl.getTags().add("HIDEABLE"); //$NON-NLS-1$
			switcherControl.getTags().add("SHOW_RESTORE_MENU"); //$NON-NLS-1$
			switcherControl
					.setContributionURI("bundleclass://org.eclipse.ui.workbench/org.eclipse.e4.ui.workbench.addons.perspectiveswitcher.PerspectiveSwitcher"); //$NON-NLS-1$
			trimBar.getChildren().add(switcherControl);
		} else if (switcherControl != null) {
			if (!getWindowConfigurer().getShowPerspectiveBar()) {
				trimBar.getChildren().remove(switcherControl);
			} else {
				List<String> tags = switcherControl.getTags();
				if (!tags.contains("HIDEABLE")) { //$NON-NLS-1$
					tags.add("HIDEABLE"); //$NON-NLS-1$
				}
				if (!tags.contains("SHOW_RESTORE_MENU")) { //$NON-NLS-1$
					tags.add("SHOW_RESTORE_MENU"); //$NON-NLS-1$
				}
			}
		}

		// render now after everything has been added so contributions can be
		// inserted in the right place
		updateLayoutDataForContents();
	}

	/**
	 * Removes the "legacy" QuickAccess related fields from the
	 * ApplicationModel.
	 * <p>
	 * The "legacy" QuickAccess fields exist in the ApplicationModel if an older
	 * workspace is opened which was build before the QuickAccess was
	 * contributed via e4xmi-fragment.
	 * </p>
	 */
	private void cleanLegacyQuickAccessContribution() {
		for (String quickAccessElementId : QUICK_ACCESS_ELEMENT_IDS) {
			MToolControl legacyElement = (MToolControl) modelService.find(quickAccessElementId,
					model);
			if (legacyElement != null) {
				EcoreUtil.remove((EObject) legacyElement);
			}
		}
	}

	/**
	 * Moves the QucickAccess related fields to the wanted position.
	 * <p>
	 * If the elements "Spacer Glue", "SearchField" and "Search-PS Glue" are
	 * available in the model this method will move them to the correct place if
	 * required. The movement can be influenced by a tag which begins with
	 * {@value #MOVE_TAG} followed by the normal positioning information (e.g.:
	 * move_after:PerspectiveSpacer). For more information about positioning
	 * have a look at: {@link PositionInfo#parse(String)}.
	 * </p>
	 */
	private void positionQuickAccess() {
		/*
		 * The QUICK_ACCESS_ELEMENT_IDS array contains the IDs of optional
		 * elements provided via an e4xmi application model fragment. The method
		 * checks if they should be moved to a special position. This behavior
		 * is required because nearly all elements in the legacy workbench are
		 * not provided via e4xmi application model. They are provided
		 * programmatically after the e4xmi application model and the
		 * corresponding fragment models are already processed.
		 */
		for (String quickAccessElementId : QUICK_ACCESS_ELEMENT_IDS) {
			MToolControl quickAccessElement = (MToolControl) modelService.find(
					quickAccessElementId, model);
			if (quickAccessElement != null) {
				moveControl(quickAccessElement.getParent(), quickAccessElement);

				// target the quick access field specifically
				if (QUICK_ACCESS_ID.equals(quickAccessElement.getElementId())) {
					if (model.getTags().contains(QUICK_ACCESS_HIDDEN)) {
						if (!quickAccessElement.getTags().contains(
								IPresentationEngine.HIDDEN_EXPLICITLY)) {
							quickAccessElement.getTags().add(IPresentationEngine.HIDDEN_EXPLICITLY);
						}
					}
				}
			}
		}

	}

	private static final String QUICK_ACCESS_ID = "SearchField"; //$NON-NLS-1$
	private static final String QUICK_ACCESS_HIDDEN = "QUICK_ACCESS_HIDDEN"; //$NON-NLS-1$

	@Inject
	private void hideQuickAccess(
			@Optional @UIEventTopic(UIEvents.ApplicationElement.TOPIC_TAGS) Event event) {
		if (event == null) {
			return;
		}
		Object origin = event.getProperty(UIEvents.EventTags.ELEMENT);
		if (!(origin instanceof MToolControl)) {
			return;
		}
		MToolControl control = (MToolControl) origin;
		if (!QUICK_ACCESS_ID.equals(control.getElementId())) {
			return;
		}
		MWindow myWindow = modelService.getTopLevelWindowFor(control);
		if (myWindow != model) {
			return;
		}
		if (UIEvents.isADD(event)) {
			if (UIEvents.contains(event, UIEvents.EventTags.NEW_VALUE,
					IPresentationEngine.HIDDEN_EXPLICITLY)) {
				if (!model.getTags().contains(QUICK_ACCESS_HIDDEN)) {
					model.getTags().add(QUICK_ACCESS_HIDDEN);
				}
			}
		} else if (UIEvents.isREMOVE(event)) {
			if (UIEvents.contains(event, UIEvents.EventTags.OLD_VALUE,
					IPresentationEngine.HIDDEN_EXPLICITLY)) {
				model.getTags().remove(QUICK_ACCESS_HIDDEN);
			}
		}
	}

	/**
	 * Moves the given element from its current position to the position
	 * mentioned in one of its tags.
	 *
	 * @param elementContainer
	 *            the list of elements in which the element should be moved
	 * @param element
	 *            the element to move
	 */
	private void moveControl(MElementContainer<MUIElement> elementContainer, MUIElement element) {
		if (element == null || elementContainer == null)
			return;

		PositionInfo positionInfo = findMovePositionInfo(element);

		// does the element has a tag with a "move_" position info
		if (positionInfo != null) {
			List<MUIElement> elements = elementContainer.getChildren();

			if (elements.remove(element)) {
				// reposition only if the element was in the list

				switch (positionInfo.getPosition()) {
				case LAST:
					elements.add(element);
					break;

				case FIRST:
					elements.add(0, element);
					break;

				case INDEX:
					int index = positionInfo.getPositionReferenceAsInteger();
					if (index >= 0 && index < elements.size()) {
						elements.add(index, element);
					} else {
						elements.add(element);
					}
					break;

				case BEFORE:
				case AFTER:
					int idx = indexOfElementWithID(elements, positionInfo.getPositionReference());
					if (idx < 0) {
						// element no found
						elements.add(element);
					} else {
						if (positionInfo.getPosition() == Position.AFTER) {
							idx++;
						}

						if (idx < elements.size()) {
							elements.add(idx, element);
						} else {
							elements.add(element);
						}
					}
					break;

				default:
					WorkbenchPlugin.log("Can't position control '" + element.getElementId() //$NON-NLS-1$
							+ "' because of the unknown position type '" //$NON-NLS-1$
							+ positionInfo.getPosition() + "'!"); //$NON-NLS-1$
				}
			}
		}
	}

	/**
	 * Find the element with the given id in the given list of
	 * {@link MUIElement}s.
	 *
	 * @param elements
	 *            the list of {@link MUIElement}s to search
	 * @param id
	 *            the id of the {@link MUIElement} to find
	 * @return the index of the {@link MUIElement} in the given list or -1 if
	 *         element wasn't found
	 */
	private int indexOfElementWithID(List<MUIElement> elements, String id) {
		if (elements == null || id == null)
			return -1;

		int index = 0;
		for (MUIElement element : elements) {
			if (id.equals(element.getElementId())) {
				return index;
			}
			index++;
		}

		return -1;
	}

	/**
	 * Checks if the {@link MUIElement} has a tag starting with
	 * {@value #MOVE_TAG} and if so it will extract the {@link PositionInfo} out
	 * of it.
	 *
	 * @param element
	 *            the element to check
	 * @return the found {@link PositionInfo} on the given {@link MUIElement},
	 *         or <code>null</code> if none was found
	 */
	private PositionInfo findMovePositionInfo(MUIElement element) {
		if (element != null) {
			for (String tag : element.getTags()) {
				if (tag.startsWith(MOVE_TAG)) {
					return PositionInfo.parse(tag.substring(MOVE_TAG.length()));
				}
			}
		}

		return null;
	}

	private void populateStandardTrim(MTrimBar bottomTrim) {
		// StatusLine
		MToolControl slElement = (MToolControl) modelService.find(
STATUS_LINE_ID, model);
		if (slElement == null) {
			slElement = modelService.createModelElement(MToolControl.class);
			slElement.setElementId(STATUS_LINE_ID);
			slElement.setContributionURI(TRIM_CONTRIBUTION_URI);
			bottomTrim.getChildren().add(slElement);
		}
		slElement.setToBeRendered(statusLineVisible);
		slElement.getTags().add(TrimBarLayout.SPACER);

		// Heap Status
		MToolControl hsElement = (MToolControl) modelService.find(
				"org.eclipse.ui.HeapStatus", model); //$NON-NLS-1$
		if (hsElement == null) {
			hsElement = modelService.createModelElement(MToolControl.class);
			hsElement.setElementId("org.eclipse.ui.HeapStatus"); //$NON-NLS-1$
			hsElement.setContributionURI(TRIM_CONTRIBUTION_URI);
			hsElement.getTags().add(IPresentationEngine.DRAGGABLE);
			bottomTrim.getChildren().add(hsElement);
		}
		hsElement.setToBeRendered(getShowHeapStatus());

		// Progress Bar
		MToolControl pbElement = (MToolControl) modelService.find(
				"org.eclipse.ui.ProgressBar", model); //$NON-NLS-1$
		if (pbElement == null) {
			pbElement = modelService.createModelElement(MToolControl.class);
			pbElement.setElementId("org.eclipse.ui.ProgressBar"); //$NON-NLS-1$
			pbElement.getTags().add(IPresentationEngine.DRAGGABLE);
			pbElement.setContributionURI(TRIM_CONTRIBUTION_URI);
			bottomTrim.getChildren().add(pbElement);
		}
		pbElement.setToBeRendered(getWindowConfigurer().getShowProgressIndicator());
	}

	private void populateTrimContributions(MTrimBar bottomTrim) {
		// Part 1: Add groups
		IConfigurationElement[] exts = extensionRegistry
				.getConfigurationElementsFor("org.eclipse.ui.menus"); //$NON-NLS-1$
		List<IConfigurationElement> items = new ArrayList<>();
		for (IConfigurationElement ice : exts) {
			if ("group".equals(ice.getName()) || "widget".equals(ice.getName())) { //$NON-NLS-1$ //$NON-NLS-2$
				items.add(ice);
			}
		}

		if (items.size() == 0)
			return;

		// Iterate over the items until they've all been placed or until
		// an iteration doesn't place anything
		List<IConfigurationElement> handledElements = new ArrayList<>();
		handledElements.add(items.get(0)); // Hack!! startup seeding
		MUIElement createdTrim = null;
		while (items.size() > 0 && handledElements.size() > 0) {
			handledElements.clear();

			for (IConfigurationElement item : items) {
				String id = item.getAttribute("id"); //$NON-NLS-1$
				String classSpec = item.getAttribute("class"); //$NON-NLS-1$
				//boolean sepVisible = "true".equals(item.getAttribute("separatorsVisible")); //$NON-NLS-1$ //$NON-NLS-2$
				IConfigurationElement[] locs = item.getChildren("location"); //$NON-NLS-1$
				for (IConfigurationElement loc : locs) {
					IConfigurationElement[] bars = loc.getChildren("bar"); //$NON-NLS-1$
					if (bars.length > 0) {
						IConfigurationElement bar = bars[0];
						boolean isTrim = "trim".equals(bar.getAttribute("type")); //$NON-NLS-1$//$NON-NLS-2$
						if (isTrim) {
							String path = bar.getAttribute("path"); //$NON-NLS-1$
							if (path != null && path.length() > 0) {
								createdTrim = addTrimElement(bottomTrim, item, id, false, path,
										classSpec);
							} else {
								IConfigurationElement[] orders = loc.getChildren("order"); //$NON-NLS-1$
								if (orders.length > 0) {
									boolean isBefore = "before".equals(orders[0].getAttribute("position")); //$NON-NLS-1$//$NON-NLS-2$
									String relTo = orders[0].getAttribute("relativeTo"); //$NON-NLS-1$
									if ("status".equals(relTo)) //$NON-NLS-1$
										relTo = STATUS_LINE_ID;

									createdTrim = addTrimElement(bottomTrim, item, id, isBefore,
											relTo, classSpec);
								}
							}

							if (createdTrim != null) {
								handledElements.add(item);
							}
						}
					}
				}
			}

			items.removeAll(handledElements);
		}
	}

	private MToolControl addTrimElement(MTrimBar bottomTrim, IConfigurationElement ice, String id,
			boolean isBefore, String relTo, String classSpec) {
		// is it already in the trim ?
		MUIElement existingTrim = modelService.find(id, bottomTrim);
		if (existingTrim != null) {
			iceMap.put((MToolControl) existingTrim, ice);
			return (MToolControl) existingTrim;
		}

		// Ok, create one but only if we can site it correctly
		int insertIndex = bottomTrim.getChildren().size();
		if (relTo != null) {
			MUIElement foundRel = modelService.find(relTo, bottomTrim);
			if (foundRel == null)
				return null;
			insertIndex = bottomTrim.getChildren().indexOf(foundRel);
			if (!isBefore)
				insertIndex++;
		}

		MToolControl newTrimElement = modelService.createModelElement(MToolControl.class);
		newTrimElement.setElementId(id);
		newTrimElement.setToBeRendered(classSpec != null);
		if (classSpec != null) {
			newTrimElement
					.setContributionURI("bundleclass://org.eclipse.ui.workbench/org.eclipse.ui.internal.LegacyTrim"); //$NON-NLS-1$
		}
		newTrimElement.setContributorURI(URIHelper.constructPlatformURI(ice.getContributor()));

		iceMap.put(newTrimElement, ice);
		bottomTrim.getChildren().add(insertIndex, newTrimElement);

		return newTrimElement;
	}

	void populateBottomTrimContributions() {
		MTrimBar bottomTrim = modelService.getTrim(model, SideValue.BOTTOM);

		populateStandardTrim(bottomTrim);
		populateTrimContributions(bottomTrim);
	}

	public MTrimBar getTopTrim() {
		List<MTrimBar> trimBars = model.getTrimBars();
		for (MTrimBar bar : trimBars) {
			if (MAIN_TOOLBAR_ID.equals(bar.getElementId())) {
				return bar;
			}
		}
		return null;
	}

	public void fill(MenuManagerRenderer renderer, MMenu menu, IMenuManager manager) {
		for (IContributionItem item : manager.getItems()) {
			if (item instanceof MenuManager) {
				MenuManager menuManager = (MenuManager) item;
				MMenu subMenu = MenuHelper.createMenu(menuManager);
				if (subMenu != null) {
					renderer.linkModelToContribution(subMenu, item);
					renderer.linkModelToManager(subMenu, menuManager);
					fill(renderer, subMenu, menuManager);
					menu.getChildren().add(subMenu);
				}
			} else if (item instanceof CommandContributionItem) {
				CommandContributionItem cci = (CommandContributionItem) item;
				MMenuItem menuItem = MenuHelper.createItem(application, cci);
				manager.remove(item);
				if (menuItem != null) {
					menu.getChildren().add(menuItem);
				}
			} else if (item instanceof AbstractGroupMarker) {
				MMenuSeparator separator = modelService.createModelElement(MMenuSeparator.class);
				separator.setVisible(item.isVisible());
				separator.setElementId(item.getId());
				if (item instanceof GroupMarker) {
					separator.getTags().add(MenuManagerRenderer.GROUP_MARKER);
				}
				menu.getChildren().add(separator);
				manager.remove(item);
			} else {
				MMenuItem menuItem = OpaqueElementUtil.createOpaqueMenuItem();
				menuItem.setElementId(item.getId());
				menuItem.setVisible(item.isVisible());
				OpaqueElementUtil.setOpaqueItem(menuItem, item);
				menu.getChildren().add(menuItem);
				renderer.linkModelToContribution(menuItem, item);
			}
		}
	}

	public static String getId(IConfigurationElement element) {
		String id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);

		// For sub-menu management -all- items must be id'd so enforce this
		// here (we could optimize by checking the 'name' of the config
		// element == "menu"
		if (id == null || id.length() == 0) {
			id = getCommandId(element);
		}
		if (id == null || id.length() == 0) {
			id = element.toString();
		}

		return id;
	}

	public static String getCommandId(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_COMMAND_ID);
	}

	public static String getActionSetCommandId(IConfigurationElement element) {
		String id = getDefinitionId(element);
		if (id != null) {
			return id;
		}
		id = getId(element);
		String actionSetId = null;
		Object obj = element.getParent();
		while (obj instanceof IConfigurationElement && actionSetId == null) {
			IConfigurationElement parent = (IConfigurationElement) obj;
			if (parent.getName().equals(IWorkbenchRegistryConstants.TAG_ACTION_SET)) {
				actionSetId = getId(parent);
			}
			obj = parent.getParent();
		}
		return ACTION_SET_CMD_PREFIX + actionSetId + '/' + id;
	}

	public static String getDefinitionId(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_DEFINITION_ID);
	}

	public static boolean getRetarget(IConfigurationElement element) {
		String r = element.getAttribute(IWorkbenchRegistryConstants.ATT_RETARGET);
		return Boolean.valueOf(r);
	}

	/**
	 * Return the style bits for the shortcut bar.
	 *
	 * @return int
	 */
	protected int perspectiveBarStyle() {
		return SWT.FLAT | SWT.WRAP | SWT.RIGHT | SWT.HORIZONTAL;
	}

	private boolean coolBarVisible = true;

	private boolean perspectiveBarVisible = true;

	private boolean statusLineVisible = true;

	/**
	 * The handlers for global actions that were last submitted to the workbench
	 * command support. This is a map of command identifiers to
	 * <code>ActionHandler</code>. This map is never <code>null</code>, and is
	 * never empty as long as at least one global action has been registered.
	 */
	private Map<String, ActionHandler> globalActionHandlersByCommandId = new HashMap<>();

	/**
	 * The list of handler submissions submitted to the workbench command
	 * support. This list may be empty, but it is never <code>null</code>.
	 */
	private List<IHandlerActivation> handlerActivations = new ArrayList<>();

	/**
	 * The number of large updates that are currently going on. If this is
	 * number is greater than zero, then UI updateActionBars is a no-op.
	 *
	 * @since 3.1
	 */
	private int largeUpdates = 0;

	private IExtensionTracker tracker;

	private void firePageClosed() {
		pageListeners.firePageClosed(page);
	}

	private void firePageOpened() {
		pageListeners.firePageOpened(page);
	}

	private void firePageActivated() {
		pageListeners.firePageActivated(page);
	}

	void registerGlobalAction(IAction globalAction) {
		String commandId = globalAction.getActionDefinitionId();

		if (commandId != null) {
			final Object value = globalActionHandlersByCommandId.remove(commandId);
			if (value instanceof ActionHandler) {
				// This handler is about to get clobbered, so dispose it.
				final ActionHandler handler = (ActionHandler) value;
				handler.dispose();
			}

			if (globalAction instanceof CommandAction) {
				final String actionId = globalAction.getId();
				if (actionId != null) {
					final IActionCommandMappingService mappingService = serviceLocator
							.getService(IActionCommandMappingService.class);
					mappingService.map(actionId, commandId);
				}
			} else {
				globalActionHandlersByCommandId.put(commandId, new ActionHandler(globalAction));
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
		final IHandlerService handlerService = getService(IHandlerService.class);

		/*
		 * Mash the action sets and global actions together, with global actions
		 * taking priority.
		 */
		Map<String, ActionHandler> handlersByCommandId = new HashMap<>();
		handlersByCommandId.putAll(globalActionHandlersByCommandId);

		List<IHandlerActivation> newHandlers = new ArrayList<>(
				handlersByCommandId.size());

		Iterator<IHandlerActivation> existingIter = handlerActivations.iterator();
		while (existingIter.hasNext()) {
			IHandlerActivation next = existingIter.next();

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
			for (Entry<String, ActionHandler> entry : handlersByCommandId.entrySet()) {
				String commandId = entry.getKey();
				IHandler handler = entry.getValue();
				newHandlers.add(handlerService.activateHandler(commandId, handler, expression));
			}
		}

		handlerActivations = newHandlers;
	}

	/**
	 * Add a generic property listener.
	 *
	 * @param listener
	 *            the listener to add
	 * @since 3.3
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		genericPropertyListeners.add(listener);
	}

	/**
	 * Removes a generic property listener.
	 *
	 * @param listener
	 *            the listener to remove
	 * @since 3.3
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		genericPropertyListeners.remove(listener);
	}

	private void firePropertyChanged(final String property, final Object oldValue,
			final Object newValue) {
		PropertyChangeEvent event = new PropertyChangeEvent(this, property, oldValue, newValue);
		for (Object listener : genericPropertyListeners.getListeners()) {
			IPropertyChangeListener propertyChangeListener = (IPropertyChangeListener) listener;
			propertyChangeListener.propertyChange(event);
		}
	}

	/*
	 * Adds an listener to the part service.
	 */
	@Override
	public void addPageListener(IPageListener l) {
		pageListeners.addPageListener(l);
	}

	/**
	 * @see org.eclipse.ui.IPageService
	 */
	@Override
	public void addPerspectiveListener(org.eclipse.ui.IPerspectiveListener l) {
		perspectiveListeners.addPerspectiveListener(l);
	}

	/**
	 * Close the window.
	 *
	 * Assumes that busy cursor is active.
	 */
	private boolean busyClose(boolean remove) {
		/*
		 * Warning: Intricate flow of control and re-entrant invocations of this
		 * method:
		 *
		 * - busyClose(true) is called from WorkbenchWindow#close() when the
		 * user closes a workbench window.
		 *
		 * - busyClose(false) is called from Workbench#close(int, boolean). This
		 * happens on File > Exit/Restart, [Mac] Quit Eclipse, AND ... tadaa ...
		 * from busyClose(true) when the user closes the last window => [Case A]
		 *
		 * Additional complication: busyClose(true) can also be called again
		 * when someone runs an event loop during the shutdown sequence. In that
		 * case, the nested busyClose(true) should be dropped (bug 381555) =>
		 * [Case B]
		 */
		if (closing) {
			// [Case A] Window is already closing.
			return false;
		}
		if (updateDisabled && remove) {
			// [Case B] User closed this window, which triggered
			// "workbench.close()", during which the user tried to close this
			// window again.
			return false;
		}

		// Whether the window was actually closed or not
		boolean windowClosed = false;

		// Setup internal flags to indicate window is in
		// progress of closing and no update should be done.
		updateDisabled = true;

		try {
			// Tag the currently active part so we can restore focus on startup
			IWorkbenchPage activePage = getActivePage();
			if (activePage != null) {
				WorkbenchPartReference ref = (WorkbenchPartReference) activePage
						.getActivePartReference();
				if (ref != null) {
					ref.getModel().getTags().add(EPartService.ACTIVE_ON_CLOSE_TAG);
				}
			}

			// Only do the check if it is OK to close if we are not closing
			// via the workbench as the workbench will check this itself.
			Workbench workbench = getWorkbenchImpl();
			int count = workbench.getWorkbenchWindowCount();
			// also check for starting - if the first window dies on startup
			// then we'll need to open a default window.
			if (!workbench.isStarting() && !workbench.isClosing() && count <= 1
					&& workbench.getWorkbenchConfigurer().getExitOnLastWindowClose()) {
				windowClosed = workbench.close();
			} else {
				if (okToClose()) {
					closing = true;
					windowClosed = hardClose(remove);
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

	@Override
	public Shell getShell() {
		return (Shell) model.getWidget();
	}

	public boolean close(final boolean remove) {
		final boolean[] ret = new boolean[1];
		BusyIndicator.showWhile(null, new Runnable() {
			@Override
			public void run() {
				ret[0] = busyClose(remove);
			}
		});
		return ret[0];
	}

	/**
	 * @see IWorkbenchWindow
	 */
	@Override
	public boolean close() {
		return close(true);
	}

	protected boolean isClosing() {
		return closing || getWorkbenchImpl().isClosing();
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

	void fireWindowOpened() {
		// notify the advisor
		getWindowAdvisor().postWindowOpen();
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
			@Override
			public void runWithException() throws Throwable {
				getWindowAdvisor().postWindowRestore();
			}
		});
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
	 * Mark contributions dirty for future update.
	 */
	private void allowUpdates(IMenuManager menuManager) {
		menuManager.markDirty();
		for (IContributionItem item : menuManager.getItems()) {
			if (item instanceof IMenuManager) {
				allowUpdates((IMenuManager) item);
			} else if (item instanceof SubContributionItem) {
				final IContributionItem innerItem = ((SubContributionItem) item).getInnerItem();
				if (innerItem instanceof IMenuManager) {
					allowUpdates((IMenuManager) innerItem);
				}
			}
		}
	}

	/**
	 * Fires perspective activated
	 */
	void firePerspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		IMenuManager windowManager = ((WorkbenchPage) page).getActionBars().getMenuManager();
		allowUpdates(windowManager);
		windowManager.update(false);

		UIListenerLogging.logPerspectiveEvent(this, page, perspective,
				UIListenerLogging.PLE_PERSP_ACTIVATED);
		perspectiveListeners.firePerspectiveActivated(page, perspective);
	}

	/**
	 * Fires perspective deactivated.
	 *
	 * @since 3.2
	 */
	void firePerspectivePreDeactivate(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		UIListenerLogging.logPerspectiveEvent(this, page, perspective,
				UIListenerLogging.PLE_PERSP_PRE_DEACTIVATE);
		perspectiveListeners.firePerspectivePreDeactivate(page, perspective);
	}

	/**
	 * Fires perspective deactivated.
	 *
	 * @since 3.1
	 */
	void firePerspectiveDeactivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		UIListenerLogging.logPerspectiveEvent(this, page, perspective,
				UIListenerLogging.PLE_PERSP_DEACTIVATED);
		perspectiveListeners.firePerspectiveDeactivated(page, perspective);
	}

	/**
	 * Fires perspective changed
	 *
	 * @param page
	 * @param perspective
	 * @param changeId
	 */
	public void firePerspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective,
			String changeId) {
		// Some callers call this even when there is no active perspective.
		// Just ignore this case.
		if (perspective != null) {
			UIListenerLogging.logPerspectiveChangedEvent(this, page, perspective, null, changeId);
			perspectiveListeners.firePerspectiveChanged(page, perspective, changeId);
		}
	}

	/**
	 * Fires perspective changed for an affected part
	 *
	 * @param page
	 * @param perspective
	 * @param partRef
	 * @param changeId
	 */
	public void firePerspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective,
			IWorkbenchPartReference partRef, String changeId) {
		// Some callers call this even when there is no active perspective.
		// Just ignore this case.
		if (perspective != null) {
			UIListenerLogging
					.logPerspectiveChangedEvent(this, page, perspective, partRef, changeId);
			perspectiveListeners.firePerspectiveChanged(page, perspective, partRef, changeId);
		}
	}

	/**
	 * Fires perspective closed
	 */
	void firePerspectiveClosed(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		UIListenerLogging.logPerspectiveEvent(this, page, perspective,
				UIListenerLogging.PLE_PERSP_CLOSED);
		perspectiveListeners.firePerspectiveClosed(page, perspective);
	}

	/**
	 * Fires perspective opened
	 */
	void firePerspectiveOpened(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		UIListenerLogging.logPerspectiveEvent(this, page, perspective,
				UIListenerLogging.PLE_PERSP_OPENED);
		perspectiveListeners.firePerspectiveOpened(page, perspective);
	}

	/**
	 * Fires perspective saved as.
	 *
	 * @since 3.1
	 */
	void firePerspectiveSavedAs(IWorkbenchPage page, IPerspectiveDescriptor oldPerspective,
			IPerspectiveDescriptor newPerspective) {
		UIListenerLogging.logPerspectiveSavedAs(this, page, oldPerspective, newPerspective);
		perspectiveListeners.firePerspectiveSavedAs(page, oldPerspective, newPerspective);
	}

	/**
	 * Returns the action bars for this window.
	 *
	 * @return this window's action bars
	 */
	public WWinActionBars getActionBars() {
		if (actionBars == null) {
			actionBars = new WWinActionBars(this);
		}
		return actionBars;
	}

	@Override
	public IWorkbenchPage getActivePage() {
		return page;
	}

	@Override
	public IWorkbenchPage[] getPages() {
		return page == null ? new IWorkbenchPage[0] : new IWorkbenchPage[] { page };
	}

	/**
	 * @see IWorkbenchWindow
	 */
	@Override
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
	@Override
	public ISelectionService getSelectionService() {
		return selectionService;
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
	 * @see IWorkbenchWindow
	 */
	@Override
	public IWorkbench getWorkbench() {
		return PlatformUI.getWorkbench();
	}

	private void hideNonRestorableViews() {
		List<MPart> sharedPartsToRemove = new ArrayList<>();
		List<MPlaceholder> phList = modelService
				.findElements(model, null, MPlaceholder.class, null);
		for (MPlaceholder ph : phList) {
			if (!(ph.getRef() instanceof MPart))
				continue;

			String partId = ph.getElementId();

			// If the id contains a ':' use the part before it as the
			// descriptor id
			int colonIndex = partId.indexOf(':');
			String descId = colonIndex == -1 ? partId : partId.substring(0, colonIndex);
			String secondaryId = colonIndex == -1 ? null : partId.substring(colonIndex + 1);
			IViewDescriptor regEntry = ((Workbench) workbench).getViewRegistry().find(descId);
			if (regEntry != null && !regEntry.isRestorable() && !("*".equals(secondaryId))) { //$NON-NLS-1$
				MElementContainer<MUIElement> phParent = ph.getParent();
				if (colonIndex != -1) {
					// if it's a multi-instance part remove it (and its MPart)
					if (!sharedPartsToRemove.contains(ph.getRef()))
						sharedPartsToRemove.add((MPart) ph.getRef());
					ph.getParent().getChildren().remove(ph);
				} else if (ph.isToBeRendered()) {
					// just hide it (so we remember where to open it again
					ph.setToBeRendered(false);
				}

				// We need to do our own cleanup here...
				int vc = modelService.countRenderableChildren(phParent);
				if (vc == 0) {
					if (!isLastEditorStack(phParent))
						phParent.setToBeRendered(false);
				}
			}
		}

		// Remove the actual shared Parts for any placeholder that was removed
		List<MUIElement> seList = model.getSharedElements();
		for (MPart partToRemove : sharedPartsToRemove) {
			seList.remove(partToRemove);
		}
	}

	private boolean isLastEditorStack(MUIElement element) {
		return modelService.isLastEditorStack(element);
	}

	/**
	 * Unconditionally close this window. Assumes the proper flags have been set
	 * correctly (e.i. closing and updateDisabled)
	 *
	 * @param remove
	 *            <code>true</code> if this window should be removed from the
	 *            application model
	 */
	private boolean hardClose(boolean remove) {
		try {
			if (!remove) {
				// we're in a shutdown case so we need to hide views that should
				// not be restored
				hideNonRestorableViews();
			}

			// clear some lables
			// Remove the handler submissions. Bug 64024.
			final IWorkbench workbench = getWorkbench();
			final IHandlerService handlerService = workbench
					.getService(IHandlerService.class);
			handlerService.deactivateHandlers(handlerActivations);
			final Iterator<IHandlerActivation> activationItr = handlerActivations.iterator();
			while (activationItr.hasNext()) {
				final IHandlerActivation activation = activationItr.next();
				activation.getHandler().dispose();
			}
			handlerActivations.clear();
			globalActionHandlersByCommandId.clear();

			// Remove the enabled submissions. Bug 64024.
			final IContextService contextService = workbench
					.getService(IContextService.class);
			contextService.unregisterShell(getShell());

			// time to wipe our our populate
			// IMenuService menuService = (IMenuService)
			// workbench.getService(IMenuService.class);
			// menuService.releaseContributions(((ContributionManager)
			// getActionBars()
			// .getMenuManager()));
			// ICoolBarManager coolbar = getActionBars().getCoolBarManager();
			// if (coolbar != null) {
			// menuService.releaseContributions(((ContributionManager)
			// coolbar));
			// }

			getActionBarAdvisor().dispose();
			getWindowAdvisor().dispose();
			coolbarToTrim.dispose();

			// Null out the progress region. Bug 64024.
			progressRegion = null;

			// Opposite of setup() and fill()
			MWindow window = model;
			if (window.getMainMenu() != null) {
				MMenu mainMenu = window.getMainMenu();
				final MenuManagerRenderer renderer = (MenuManagerRenderer) rendererFactory.getRenderer(mainMenu, null);
				cleanupMenuManagerRec(renderer, mainMenu);
			}

			engine.removeGui(model);

			MElementContainer<MUIElement> parent = window.getParent();
			if (remove) {
				parent.getChildren().remove(window);

				if (parent.getSelectedElement() == window) {
					if (!parent.getChildren().isEmpty()) {
						parent.setSelectedElement(parent.getChildren().get(0));
					}
				}
			}
			if (getActivePage() != null) {
				firePageClosed();
			}
			fireWindowClosed();
		} finally {

			try {
				// Bring down all of the services ... after the window goes away
				serviceLocator.dispose();
			} catch (Exception ex) {
				WorkbenchPlugin.log(ex);
			}
			menuRestrictions.clear();
		}
		return true;
	}

	private void cleanupMenuManagerRec(MenuManagerRenderer renderer, MMenu m) {
		for (MMenuElement e : m.getChildren()) {
			// renderer.clearModelToContribution(e, null);
			if (e instanceof MMenu) {
				cleanupMenuManagerRec(renderer, (MMenu) e);
			}
		}
		renderer.clearModelToManager(m, null);
	}

	/**
	 * @see IWorkbenchWindow
	 */
	@Override
	public boolean isApplicationMenu(String menuID) {
		// delegate this question to the action bar advisor
		return getActionBarAdvisor().isApplicationMenu(menuID);
	}

	boolean isWorkbenchCoolItemId(String id) {
		return windowConfigurer.containsCoolItem(id);
	}

	/**
	 * Called when this window is about to be closed.
	 */
	private boolean okToClose() {
		// Save all of the editors.
		if (!getWorkbenchImpl().isClosing()) {
			IWorkbenchPage page = getActivePage();
			if (page != null) {
				return ((WorkbenchPage) page).saveAllEditors(true, true, true);
			}
		}
		return true;
	}

	@Override
	public IWorkbenchPage openPage(final String perspectiveId, final IAdaptable input)
			throws WorkbenchException {
		final Object[] result = new Object[1];
		BusyIndicator.showWhile(null, new Runnable() {
			@Override
			public void run() {
				try {
					result[0] = busyOpenPage(perspectiveId, input);
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
			throw new WorkbenchException(WorkbenchMessages.WorkbenchWindow_exceptionMessage);
		}
	}

	private IWorkbenchPage busyOpenPage(String perspectiveId, IAdaptable input)
			throws WorkbenchException {
		IPerspectiveDescriptor descriptor = workbench.getPerspectiveRegistry()
				.findPerspectiveWithId(perspectiveId);
		if (descriptor == null) {
			throw new WorkbenchException(NLS.bind(
					WorkbenchMessages.WorkbenchPage_ErrorCreatingPerspective, perspectiveId));
		}

		if (page == null) {
			page = new WorkbenchPage(this, input);
			model.getContext().set(IWorkbenchPage.class.getName(), page);

			try {
				ContextInjectionFactory.inject(page, model.getContext());
			} catch (InjectionException e) {
				throw new WorkbenchException(e.getMessage(), e);
			}

			firePageOpened();

			partService.setPage(page);
		} else {
			IWorkbenchWindow window = getWorkbench().openWorkbenchWindow(perspectiveId, input);
			return window.getActivePage();
		}

		perspective = descriptor;
		page.setPerspective(perspective);
		firePageActivated();

		return page;
	}

	@Override
	public IWorkbenchPage openPage(IAdaptable input) throws WorkbenchException {
		return openPage(workbench.getPerspectiveRegistry().getDefaultPerspective(), input);
	}

	/*
	 * Removes an listener from the part service.
	 */
	@Override
	public void removePageListener(IPageListener l) {
		pageListeners.removePageListener(l);
	}

	/**
	 * @see org.eclipse.ui.IPageService
	 */
	@Override
	public void removePerspectiveListener(org.eclipse.ui.IPerspectiveListener l) {
		perspectiveListeners.removePerspectiveListener(l);
	}

	private void disableControl(Control ctrl, List<Control> toEnable) {
		if (ctrl != null && !ctrl.isDisposed() && ctrl.isEnabled()) {
			ctrl.setEnabled(false);
			toEnable.add(ctrl);
		}
	}

	@Override
	public void run(final boolean fork, boolean cancelable, final IRunnableWithProgress runnable)
			throws InvocationTargetException, InterruptedException {
		final StatusLineManager manager = getStatusLineManager();

		// Temporary Hack for bug 330106, remove when bug 334093 is fixed
		boolean progressHack = manager.getControl() == null;
		if (manager == null || progressHack) {
			runnable.run(new NullProgressMonitor());
		} else {
			EPartService partService = model.getContext().get(EPartService.class);
			final MPart curActive = partService.getActivePart();
			boolean wasCancelEnabled = manager.isCancelEnabled();
			boolean enableMainMenu = false;

			IBindingService bs = model.getContext().get(IBindingService.class);
			boolean keyFilterEnabled = bs.isKeyFilterEnabled();
			List<Control> toEnable = new ArrayList<>();
			Shell theShell = getShell();
			Display display = theShell.getDisplay();
			Control currentFocus = display.getFocusControl();

			try {
				Menu mainMenu = (Menu) model.getMainMenu().getWidget();
				if (mainMenu != null && !mainMenu.isDisposed() && mainMenu.isEnabled()) {
					mainMenu.setEnabled(false);
					enableMainMenu = true;
				}

				if (keyFilterEnabled)
					bs.setKeyFilterEnabled(false);

				// disable all other shells
				for (Shell childShell : display.getShells()) {
					if (childShell != theShell) {
						disableControl(childShell, toEnable);
					}
				}


				//Disable the presentation (except the bottom trim)
				TrimmedPartLayout tpl = (TrimmedPartLayout) getShell().getLayout();
				disableControl(tpl.clientArea, toEnable);
				disableControl(tpl.top, toEnable);
				disableControl(tpl.left, toEnable);
				disableControl(tpl.right, toEnable);

				// Disable everything in the bottom trim except the status line
				if (tpl.bottom != null && !tpl.bottom.isDisposed() && tpl.bottom.isEnabled()) {
					MUIElement statusLine = modelService.find(STATUS_LINE_ID, model);
					Object slCtrl = statusLine != null ? statusLine.getWidget() : null;
					for (Control bottomCtrl : tpl.bottom.getChildren()) {
						if (bottomCtrl != slCtrl)
							disableControl(bottomCtrl, toEnable);
					}
				}

				manager.setCancelEnabled(cancelable);

				final InvocationTargetException[] ite = new InvocationTargetException[1];
				final InterruptedException[] ie = new InterruptedException[1];

				BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
					@Override
					public void run() {
						try {
							ModalContext.run(runnable, fork, manager.getProgressMonitor(),
									getShell().getDisplay());
						} catch (InvocationTargetException e) {
							ite[0] = e;
						} catch (InterruptedException e) {
							ie[0] = e;
						} finally {
							manager.getProgressMonitor().done();
						}
					}
				});

				if (ite[0] != null) {
					throw ite[0];
				} else if (ie[0] != null) {
					throw ie[0];
				}
			} finally {
				manager.setCancelEnabled(wasCancelEnabled);

				// re-enable the main menu if necessary
				if (enableMainMenu && model != null && model.getMainMenu() != null) {
					Menu mainMenu = (Menu) model.getMainMenu().getWidget();
					mainMenu.setEnabled(true);
				}

				if (keyFilterEnabled)
					bs.setKeyFilterEnabled(true);

				// Re-enable any disabled controls
				for (Control ctrl : toEnable) {
					if (!ctrl.isDisposed() && !ctrl.isEnabled())
						ctrl.setEnabled(true);
				}

				MPart activePart = partService.getActivePart();
				if (curActive != activePart && activePart != null) {
					engine.focusGui(activePart);
				} else if (currentFocus != null && !currentFocus.isDisposed()) {
					// It's necessary to restore focus after reenabling the
					// controls
					// because disabling them causes focus to jump elsewhere.
					// Use forceFocus rather than setFocus to avoid SWT's
					// search for children which can take focus, so focus
					// ends up back on the actual control that previously had
					// it.
					currentFocus.forceFocus();
				}
			}
		}
	}

	@Override
	public void setActivePage(final IWorkbenchPage in) {
		if (getActivePage() != in) {
			if (in == null) {
				firePageClosed();
			}

			page = (WorkbenchPage) in;
			model.getContext().set(IWorkbenchPage.class, page);
			partService.setPage(page);
			updateActionSets();
			// submitGlobalActions();
		}
	}

	private Set<Object> menuRestrictions = new HashSet<>();

	private Boolean valueOf(boolean result) {
		return result ? Boolean.TRUE : Boolean.FALSE;
	}

	public Set<Object> getMenuRestrictions() {
		return menuRestrictions;
	}

	void liftRestrictions() {
		if (menuRestrictions.isEmpty()) {
			return;
		}
		EvaluationReference[] refs = menuRestrictions
				.toArray(new EvaluationReference[menuRestrictions.size()]);
		IEvaluationService es = serviceLocator
				.getService(IEvaluationService.class);
		IEvaluationContext currentState = es.getCurrentState();
		for (EvaluationReference reference : refs) {
			reference.setPostingChanges(true);

			boolean os = reference.evaluate(currentState);
			reference.clearResult();
			boolean ns = reference.evaluate(currentState);
			if (os != ns) {
				reference.getListener().propertyChange(
						new PropertyChangeEvent(reference, reference.getProperty(), valueOf(os),
								valueOf(ns)));
			}
		}
	}

	void imposeRestrictions() {
		Iterator<?> i = menuRestrictions.iterator();
		while (i.hasNext()) {
			EvaluationReference ref = (EvaluationReference) i.next();
			ref.setPostingChanges(false);
		}
	}

	/**
	 * Hooks a listener to track the activation and deactivation of the window's
	 * shell.
	 */
	private void trackShellActivation() {
		getShell().addShellListener(new ShellAdapter() {
			@Override
			public void shellActivated(ShellEvent event) {
				shellActivated = true;
				serviceLocator.activate();
				if (getActivePage() != null) {
					getWorkbenchImpl().fireWindowActivated(WorkbenchWindow.this);
				}
				liftRestrictions();
			}

			@Override
			public void shellDeactivated(ShellEvent event) {
				shellActivated = false;
				imposeRestrictions();
				serviceLocator.deactivate();
				if (getActivePage() != null) {
					getWorkbenchImpl().fireWindowDeactivated(WorkbenchWindow.this);
				}
			}
		});
	}

	/**
	 * update the action bars.
	 */
	public void updateActionBars() {
		if (getShell() == null || getShell().isDisposed() || updateDisabled || updatesDeferred()) {
			return;
		}
		// updateAll required in order to enable accelerators on pull-down menus
		getMenuBarManager().update(false);

		try {
			getShell().setLayoutDeferred(true);
			eventBroker.send(UIEvents.REQUEST_ENABLEMENT_UPDATE_TOPIC, UIEvents.ALL_ELEMENT_ID);
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
	 * <code>largeUpdateStart</code>, and the event loop should not be allowed
	 * to spin before this method is called.
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

		WorkbenchPage currentPage = (WorkbenchPage) getActivePage();
		if (currentPage == null) {
			getActionPresentation().clearActionSets();
		} else {
			ICoolBarManager2 coolBarManager = (ICoolBarManager2) getCoolBarManager2();
			if (coolBarManager != null) {
				coolBarManager.refresh();
			}
			getActionPresentation().setActionSets(currentPage.getActionSets());
		}
		fireActionSetsChanged();
		updateActionBars();

		// hide the launch menu if it is empty
		String path = IWorkbenchActionConstants.M_WINDOW + IWorkbenchActionConstants.SEP
				+ IWorkbenchActionConstants.M_LAUNCH;
		IMenuManager manager = getMenuBarManager().findMenuUsingPath(path);
		IContributionItem item = getMenuBarManager().findUsingPath(path);

		if (manager == null || item == null) {
			return;
		}
		item.setVisible(manager.getItems().length >= 2);
		// there is a separator for the additions group thus >= 2
	}

	private ListenerList<IActionSetsListener> actionSetListeners = null;

	private ListenerList<IBackgroundSaveListener> backgroundSaveListeners = new ListenerList<>(ListenerList.IDENTITY);

	private SelectionService selectionService;

	private ActionPresentation actionPresentation;

	private final void fireActionSetsChanged() {
		if (actionSetListeners != null) {
			for (Object listener : actionSetListeners.getListeners()) {
				final IActionSetsListener actionSetsListener = (IActionSetsListener) listener;
				final WorkbenchPage currentPage = (WorkbenchPage) getActivePage();
				final IActionSetDescriptor[] newActionSets;
				if (currentPage == null) {
					newActionSets = null;
				} else {
					newActionSets = currentPage.getActionSets();
				}
				final ActionSetsEvent event = new ActionSetsEvent(newActionSets);
				actionSetsListener.actionSetsChanged(event);
			}
		}
	}

	final void addActionSetsListener(final IActionSetsListener listener) {
		if (actionSetListeners == null) {
			actionSetListeners = new ListenerList<>();
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
	 * Returns whether the heap status indicator should be shown.
	 *
	 * @return <code>true</code> to show the heap status indicator,
	 *         <code>false</code> otherwise
	 */
	private boolean getShowHeapStatus() {
		return // Show if the preference is set or debug option is on
		PrefUtil.getAPIPreferenceStore().getBoolean(
				IWorkbenchPreferenceConstants.SHOW_MEMORY_MONITOR)
				|| Boolean
						.valueOf(
								Platform.getDebugOption(PlatformUI.PLUGIN_ID
										+ "/perf/showHeapStatus")).booleanValue(); //$NON-NLS-1$
	}

	public void showHeapStatus(boolean show) {
		MUIElement hsElement = modelService.find("org.eclipse.ui.HeapStatus", model); //$NON-NLS-1$
		if (hsElement != null && hsElement.isToBeRendered() != show) {
			hsElement.setToBeRendered(show);
			getShell().layout(null, SWT.ALL | SWT.CHANGED | SWT.DEFER);
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
	 * IMPORTANT This method is declared package private to prevent regular
	 * plug-ins from downcasting IWorkbenchWindow to WorkbenchWindow and getting
	 * hold of the window advisor that would allow them to tamper with the
	 * window. The window advisor is internal to the application.
	 * </p>
	 */
	/* package private - DO NOT CHANGE */
	WorkbenchWindowAdvisor getWindowAdvisor() {
		if (windowAdvisor == null) {
			windowAdvisor = getAdvisor().createWorkbenchWindowAdvisor(getWindowConfigurer());
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
			getActionBarAdvisor().fillActionBars(flags | ActionBarAdvisor.FILL_PROXY);
		} finally {
			wab.setProxy(null);
		}
	}

	/**
	 * Cause the coolbar to be shown or hidden; note that the coolbar may still
	 * be visible depending on the visibility state of other elements. This
	 * method is a lower-level method that affects the visibility but does not
	 * update any associated preference values.
	 *
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
			getModel().getPersistedState().put(IPreferenceConstants.COOLBAR_VISIBLE,
					Boolean.toString(visible));
			updateLayoutDataForContents();
			firePropertyChanged(PROP_COOLBAR_VISIBLE, oldValue ? Boolean.TRUE : Boolean.FALSE,
					coolBarVisible ? Boolean.TRUE : Boolean.FALSE);
		}
	}

	/**
	 * @return whether the cool bar should be shown. This is only applicable if
	 *         the window configurer also wishes either the cool bar to be
	 *         visible.
	 * @since 3.0
	 */
	public boolean getCoolBarVisible() {
		return getWindowConfigurer().getShowCoolBar() && coolBarVisible;
	}

	public ActionPresentation getActionPresentation() {
		if (actionPresentation == null) {
			actionPresentation = new ActionPresentation(this);
		}
		return actionPresentation;
	}

	/**
	 * Cause the perspective bar to be shown or hidden; note that the
	 * perspective bar may still be visible depending on the visibility state of
	 * other elements. This method is a lower-level method that affects the
	 * visibility but does not update any associated preference values.
	 *
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
			getModel().getPersistedState().put(IPreferenceConstants.PERSPECTIVEBAR_VISIBLE,
					Boolean.toString(visible));
			updateLayoutDataForContents();
			firePropertyChanged(PROP_PERSPECTIVEBAR_VISIBLE, oldValue ? Boolean.TRUE
					: Boolean.FALSE, perspectiveBarVisible ? Boolean.TRUE : Boolean.FALSE);
		}
	}

	/**
	 * @return whether the perspective bar should be shown. This is only
	 *         applicable if the window configurer also wishes either the
	 *         perspective bar to be visible.
	 * @since 3.0
	 */
	public boolean getPerspectiveBarVisible() {
		return getWindowConfigurer().getShowPerspectiveBar() && perspectiveBarVisible;
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
			getModel().getPersistedState().put(IPreferenceConstants.PERSPECTIVEBAR_VISIBLE,
					Boolean.toString(visible));
			updateLayoutDataForContents();
			firePropertyChanged(PROP_STATUS_LINE_VISIBLE, oldValue ? Boolean.TRUE : Boolean.FALSE,
					statusLineVisible ? Boolean.TRUE : Boolean.FALSE);
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

	protected boolean showTopSeperator() {
		return false;
	}

	/**
	 * @return Returns the progressRegion.
	 */
	public ProgressRegion getProgressRegion() {
		return progressRegion;
	}

	@Override
	public IExtensionTracker getExtensionTracker() {
		return model.getContext().get(IExtensionTracker.class);
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
	 * Initializes all of the default command-based services for the workbench
	 * window.
	 */
	private final void initializeDefaultServices() {
		IEclipseContext windowContext = model.getContext();
		serviceLocator.registerService(IWorkbenchLocationService.class,
				new WorkbenchLocationService(IServiceScopes.WINDOW_SCOPE, getWorkbench(), this,
						null, null, null, 1));
		// added back for legacy reasons
		serviceLocator.registerService(IWorkbenchWindow.class, this);

		final ActionCommandMappingService mappingService = new ActionCommandMappingService();
		serviceLocator.registerService(IActionCommandMappingService.class, mappingService);

		selectionService = ContextInjectionFactory.make(SelectionService.class, model.getContext());
		serviceLocator.registerService(ISelectionService.class, selectionService);

		LegacyHandlerService hs = new LegacyHandlerService(windowContext);
		windowContext.set(IHandlerService.class.getName(), hs);

		final LegacyActionPersistence actionPersistence = new LegacyActionPersistence(this);
		serviceLocator.registerService(LegacyActionPersistence.class, actionPersistence);
		actionPersistence.read();

		ICommandService cmdService = workbench.getService(ICommandService.class);
		SlaveCommandService slaveCmdService = new SlaveCommandService(cmdService,
				IServiceScopes.WINDOW_SCOPE, this, model.getContext());
		serviceLocator.registerService(ICommandService.class, slaveCmdService);
		serviceLocator.registerService(IUpdateService.class, slaveCmdService);

		IContextService cxs = ContextInjectionFactory
				.make(ContextService.class, model.getContext());
		serviceLocator.registerService(IContextService.class, cxs);

		IMenuService parent = getWorkbench().getService(IMenuService.class);
		IMenuService msvs = new SlaveMenuService(parent, model);
		serviceLocator.registerService(IMenuService.class, msvs);
	}

	@Override
	public final <T> T getService(final Class<T> key) {
		return serviceLocator.getService(key);
	}

	@Override
	public final boolean hasService(final Class<?> key) {
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
		}
		if (getWindowConfigurer().getShowPerspectiveBar()) {
			setPerspectiveBarVisible(!perspectivebarVisible);
		}
		ICommandService commandService = getService(ICommandService.class);
		Map<String, WorkbenchWindow> filter = new HashMap<>();
		filter.put(IServiceScopes.WINDOW_SCOPE, this);
		commandService.refreshElements(COMMAND_ID_TOGGLE_COOLBAR, filter);
	}

	/**
	 * Return true if the toolbar is visible. Note that it may not be possible
	 * to make the toolbar visible (i.e., the window configurer).
	 *
	 * @return true if the toolbar is visible
	 * @since 4.2
	 */
	public boolean isToolbarVisible() {
		return (getCoolBarVisible() && getWindowConfigurer().getShowCoolBar())
				|| (getPerspectiveBarVisible() && getWindowConfigurer().getShowPerspectiveBar());
	}

	private void updateLayoutDataForContents() {
		MTrimBar topTrim = getTopTrim();
		if (topTrim != null) {
			topTrim.setVisible(isToolbarVisible());
			getShell().layout();
		}
	}

	/* package */void addBackgroundSaveListener(IBackgroundSaveListener listener) {
		backgroundSaveListeners.add(listener);
	}

	/* package */void fireBackgroundSaveStarted() {
		for (Object listener : backgroundSaveListeners.getListeners()) {
			IBackgroundSaveListener backgroundSaveListener = (IBackgroundSaveListener) listener;
			backgroundSaveListener.handleBackgroundSaveStarted();
		}
	}

	/* package */void removeBackgroundSaveListener(IBackgroundSaveListener listener) {
		backgroundSaveListeners.remove(listener);
	}

	public MWindow getModel() {
		return model;
	}

	StatusLineManager statusLineManager = null;

	public StatusLineManager getStatusLineManager() {
		if (statusLineManager == null) {
			statusLineManager = new StatusLineManager();
		}
		return statusLineManager;
	}

	private CoolBarManager2 oldCBM = new CoolBarManager2();
	private CoolBarToTrimManager coolbarToTrim;

	public ICoolBarManager getCoolBarManager2() {
		if (coolbarToTrim == null) {
			coolbarToTrim = new CoolBarToTrimManager(application, model, workbenchTrimElements,
					rendererFactory);
		}
		return coolbarToTrim;
	}

	public CoolBarManager getCoolBarManager() {
		WorkbenchPlugin.log(new Exception("Bad call to getCoolBarManager()")); //$NON-NLS-1$
		return oldCBM;
	}

	private IContributionManagerOverrides toolbarOverride = new IContributionManagerOverrides() {

		@Override
		public Integer getAccelerator(IContributionItem item) {
			return null;
		}

		@Override
		public String getAcceleratorText(IContributionItem item) {
			return null;
		}

		@Override
		public Boolean getEnabled(IContributionItem item) {
			return null;
		}

		@Override
		public String getText(IContributionItem item) {
			return null;
		}

		@Override
		public Boolean getVisible(IContributionItem item) {
			if (page == null)
				return null;

			MPerspective curPersp = page.getCurrentPerspective();
			if (curPersp == null)
				return null;

			// Find the command ID
			String id = CustomizePerspectiveDialog.getIDFromIContributionItem(item);
			if (id == null)
				return null;

			String hiddenToolItems = page.getHiddenItems();
			if (hiddenToolItems.contains(ModeledPageLayout.HIDDEN_TOOLBAR_PREFIX + id + ",")) { //$NON-NLS-1$
				return Boolean.FALSE;
			}
			return null;
		}
	};

	MenuManager menuManager = new MenuManager("MenuBar", ActionSet.MAIN_MENU); //$NON-NLS-1$

	public MenuManager getMenuManager() {
		return menuManager;
	}

	public IMenuManager getMenuBarManager() {
		return menuManager;
	}

	private IContributionManagerOverrides menuOverride = new IContributionManagerOverrides() {

		@Override
		public Integer getAccelerator(IContributionItem item) {
			return null;
		}

		@Override
		public String getAcceleratorText(IContributionItem item) {
			return null;
		}

		@Override
		public Boolean getEnabled(IContributionItem item) {
			return null;
		}

		@Override
		public String getText(IContributionItem item) {
			return null;
		}

		@Override
		public Boolean getVisible(IContributionItem item) {
			if (page == null)
				return null;

			MPerspective curPersp = page.getCurrentPerspective();
			if (curPersp == null)
				return null;

			// Find the command ID
			String id = CustomizePerspectiveDialog.getIDFromIContributionItem(item);
			if (id == null)
				return null;

			String hiddenToolItems = page.getHiddenItems();
			if (hiddenToolItems.contains(ModeledPageLayout.HIDDEN_MENU_PREFIX + id + ",")) { //$NON-NLS-1$
				return Boolean.FALSE;
			}
			return null;
		}
	};

	ToolBarManager2 toolBarManager = new ToolBarManager2();

	private Runnable menuUpdater;

	@Inject
	@Optional
	private ToolBarManagerRenderer toolBarManagerRenderer;

	public IToolBarManager2 getToolBarManager2() {
		return toolBarManager;
	}

	public IToolBarManager getToolBarManager() {
		return getToolBarManager2();
	}

	public CustomizePerspectiveDialog createCustomizePerspectiveDialog(Perspective persp,
			IEclipseContext context) {
		return new CustomizePerspectiveDialog(getWindowConfigurer(), persp, context);
	}

	private class WWinPartService extends PartService {

		@Override
		public void partActivated(IWorkbenchPart part) {
			super.partActivated(part);
			selectionService.notifyListeners(part);
		}

	}
}

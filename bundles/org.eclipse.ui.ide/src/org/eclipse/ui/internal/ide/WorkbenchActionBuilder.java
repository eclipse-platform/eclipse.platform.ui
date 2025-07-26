/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Andreas Buchen <andreas.buchen@sap.com> - Bug 206584
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 440810, 440975, 431862
 *     Andrey Loskutov <loskutov@gmx.de> - Bug 445538
 *     Patrik Suzzi <psuzzi@itemis.com> - Bug 487570, 494289, 529885
 *     Kaloyan Raev <kaloyan.r@zend.com> - Bug 322002
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import org.eclipse.core.commands.Command;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.jface.internal.provisional.action.IToolBarContributionItem;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.actions.BuildAction;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.NewWizardMenu;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.ide.IIDEActionConstants;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.handlers.IActionCommandMappingService;
import org.eclipse.ui.internal.ide.actions.BuildSetMenu;
import org.eclipse.ui.internal.ide.actions.BuildUtilities;
import org.eclipse.ui.internal.ide.actions.QuickMenuAction;
import org.eclipse.ui.internal.ide.actions.RetargetActionWithDefault;
import org.eclipse.ui.internal.provisional.application.IActionBarConfigurer2;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

/**
 * Adds actions to a workbench window.
 */
public class WorkbenchActionBuilder extends ActionBarAdvisor {

	private final IWorkbenchWindow workbenchWindow;

	// generic actions
	private IWorkbenchAction closeAction;

	private IWorkbenchAction closeAllAction;

	private IWorkbenchAction closeOthersAction;

	private IWorkbenchAction closeAllSavedAction;

	private IWorkbenchAction saveAction;

	private IWorkbenchAction saveAllAction;

	private IWorkbenchAction newWindowAction;

	private IWorkbenchAction helpContentsAction;

	private IWorkbenchAction helpSearchAction;

	private IWorkbenchAction dynamicHelpAction;

	private IWorkbenchAction aboutAction;

	private IWorkbenchAction openPreferencesAction;

	private IWorkbenchAction saveAsAction;

	private IWorkbenchAction hideShowEditorAction;

	private IWorkbenchAction editActionSetAction;

	private IWorkbenchAction closePerspAction;

	private IWorkbenchAction lockToolBarAction;

	private IWorkbenchAction closeAllPerspsAction;

	private IWorkbenchAction showViewMenuAction;

	private IWorkbenchAction showPartPaneMenuAction;

	private IWorkbenchAction nextPartAction;

	private IWorkbenchAction prevPartAction;

	private IWorkbenchAction nextEditorAction;

	private IWorkbenchAction prevEditorAction;

	private IWorkbenchAction nextPerspectiveAction;

	private IWorkbenchAction prevPerspectiveAction;

	private IWorkbenchAction activateEditorAction;

	private IWorkbenchAction switchToEditorAction;

	private IWorkbenchAction workbookEditorsAction;

	private IWorkbenchAction quickAccessAction;

	private IWorkbenchAction backwardHistoryAction;

	private IWorkbenchAction forwardHistoryAction;

	// generic retarget actions
	private IWorkbenchAction quitAction;

	private IWorkbenchAction goIntoAction;

	private IWorkbenchAction backAction;

	private IWorkbenchAction forwardAction;

	private IWorkbenchAction upAction;

	private IWorkbenchAction nextAction;

	private IWorkbenchAction previousAction;

	// IDE-specific actions
	private IWorkbenchAction openWorkspaceAction;

	private IWorkbenchAction projectPropertyDialogAction;

	private IWorkbenchAction newWizardAction;

	private IWorkbenchAction newWizardDropDownAction;

	private IWorkbenchAction importResourcesAction;

	private IWorkbenchAction exportResourcesAction;

	private IWorkbenchAction buildAllAction; // Incremental workspace build

	private IWorkbenchAction cleanAction;

	private IWorkbenchAction toggleAutoBuildAction;

	private MenuManager buildWorkingSetMenu;

	private IWorkbenchAction quickStartAction;

	private IWorkbenchAction tipsAndTricksAction;

	private QuickMenuAction showInQuickMenu;

	private QuickMenuAction newQuickMenu;

	private IWorkbenchAction introAction;

	// IDE-specific retarget actions
	private IWorkbenchAction buildProjectAction;

	private CommandContributionItem minimizeItem;

	private CommandContributionItem zoomItem;

	private CommandContributionItem fullscreenItem;

	private CommandContributionItem arrangeWindowsItem;

	// contribution items
	// @issue should obtain from ContributionItemFactory
	private NewWizardMenu newWizardMenu;

	// @issue class is workbench internal
	private StatusLineContributionItem statusLineItem;

	private Preferences.IPropertyChangeListener prefListener;

	// listener for the "close editors automatically"
	// preference change
	private IPropertyChangeListener propPrefListener;

	private IPageListener pageListener;
	private IPartListener partListener;

	private IResourceChangeListener resourceListener;

	/**
	 * Indicates if the action builder has been disposed
	 */
	private boolean isDisposed = false;

	/**
	 * Constructs a new action builder which contributes actions
	 * to the given window.
	 *
	 * @param configurer the action bar configurer for the window
	 */
	public WorkbenchActionBuilder(IActionBarConfigurer configurer) {
		super(configurer);
		workbenchWindow = configurer.getWindowConfigurer().getWindow();
	}

	/**
	 * Returns the window to which this action builder is contributing.
	 */
	private IWorkbenchWindow getWindow() {
		return workbenchWindow;
	}

	/**
	 * Hooks listeners on the preference store and the window's page, perspective and selection services.
	 */
	private void hookListeners() {

		pageListener = new IPageListener() {
			@Override
			public void pageActivated(IWorkbenchPage page) {
				// do nothing
			}

			@Override
			public void pageClosed(IWorkbenchPage page) {
				// do nothing
			}

			@Override
			public void pageOpened(IWorkbenchPage page) {
				// set default build handler -- can't be done until the shell is available
				IAction buildHandler = new BuildAction(page.getWorkbenchWindow(), IncrementalProjectBuilder.INCREMENTAL_BUILD);
				((RetargetActionWithDefault)buildProjectAction).setDefaultHandler(buildHandler);
			}
		};
		getWindow().addPageListener(pageListener);

		partListener = new IPartListener() {

			@Override
			public void partOpened(IWorkbenchPart part) {
			}

			@Override
			public void partDeactivated(IWorkbenchPart part) {
			}

			@Override
			public void partClosed(IWorkbenchPart part) {
			}

			@Override
			public void partActivated(IWorkbenchPart part) {
				if (!(part instanceof IEditorPart)) {
					return;
				}
				// update the "toggled" state based on the current editor
				ICommandService commandService = workbenchWindow.getService(ICommandService.class);
				commandService.refreshElements(IWorkbenchCommandConstants.WINDOW_PIN_EDITOR, null);
			}

			@Override
			public void partBroughtToTop(IWorkbenchPart part) {
			}

		};
		getWindow().getPartService().addPartListener(partListener);

		prefListener = event -> {
			if (event.getProperty().equals(
					ResourcesPlugin.PREF_AUTO_BUILDING)) {
				updateBuildActions(false);
			}
		};
		ResourcesPlugin.getPlugin().getPluginPreferences()
				.addPropertyChangeListener(prefListener);

		// listener for the "close editors automatically"
		// preference change
		propPrefListener = event -> {
			if (event.getProperty().equals(
					IPreferenceConstants.REUSE_EDITORS_BOOLEAN)) {
				if (workbenchWindow.getShell() != null
						&& !workbenchWindow.getShell().isDisposed()) {
					// this property change notification could be from a non-ui thread
					workbenchWindow.getShell().getDisplay().asyncExec(() -> {
						if (workbenchWindow.getShell() != null && !workbenchWindow.getShell().isDisposed()) {
							updatePinActionToolbar();
						}
					});
				}
			}
		};
		/*
		 * In order to ensure that the pin action toolbar sets its size
		 * correctly, the pin action should set its visibility before we call updatePinActionToolbar().
		 *
		 * In other words we always want the PinActionContributionItem to be notified before the
		 * WorkbenchActionBuilder.
		 */
		WorkbenchPlugin.getDefault().getPreferenceStore()
				.addPropertyChangeListener(propPrefListener);
		//listen for project description changes, which can affect enablement of build actions
		resourceListener = event -> {
			IResourceDelta delta = event.getDelta();
			if (delta == null) {
				return;
			}
			for (IResourceDelta projectDelta : delta.getAffectedChildren()) {
				int kind = projectDelta.getKind();
				//affected by projects being opened/closed or description changes
				boolean changed = (projectDelta.getFlags() & (IResourceDelta.DESCRIPTION | IResourceDelta.OPEN)) != 0;
				if (kind != IResourceDelta.CHANGED || changed) {
					updateBuildActions(false);
					return;
				}
			}
		};
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener, IResourceChangeEvent.POST_CHANGE);
	}

	@Override
	public void fillActionBars(int flags) {
		super.fillActionBars(flags);
		updateBuildActions(true);
		if ((flags & FILL_PROXY) == 0) {
			hookListeners();
		}
	}

	/**
	 * Fills the coolbar with the workbench actions.
	 */
	@Override
	protected void fillCoolBar(ICoolBarManager coolBar) {

		IActionBarConfigurer2 actionBarConfigurer = (IActionBarConfigurer2) getActionBarConfigurer();

		// File Group
		coolBar.add(new GroupMarker(IIDEActionConstants.GROUP_FILE));
		IToolBarManager fileToolBar = actionBarConfigurer.createToolBarManager();
		fileToolBar.add(new Separator(IWorkbenchActionConstants.NEW_GROUP));
		fileToolBar.add(newWizardDropDownAction);
		fileToolBar.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
		fileToolBar.add(new GroupMarker(
				IWorkbenchActionConstants.SAVE_GROUP));
		fileToolBar.add(saveAction);
		fileToolBar.add(saveAllAction);
		fileToolBar
				.add(new GroupMarker(IWorkbenchActionConstants.SAVE_EXT));
		fileToolBar.add(getPrintItem());
		fileToolBar
				.add(new GroupMarker(IWorkbenchActionConstants.PRINT_EXT));

		fileToolBar
				.add(new Separator(IWorkbenchActionConstants.BUILD_GROUP));
		fileToolBar
				.add(new GroupMarker(IWorkbenchActionConstants.BUILD_EXT));
		fileToolBar.add(new Separator(
				IWorkbenchActionConstants.MB_ADDITIONS));

		// Add to the cool bar manager
		coolBar.add(actionBarConfigurer.createToolBarContributionItem(fileToolBar,
				IWorkbenchActionConstants.TOOLBAR_FILE));

		// Edit group
		coolBar.add(new GroupMarker(IIDEActionConstants.GROUP_EDIT));
		IToolBarManager editToolBar = actionBarConfigurer.createToolBarManager();
		editToolBar.add(new Separator(IWorkbenchActionConstants.EDIT_GROUP));
		editToolBar.add(getUndoItem());
		editToolBar.add(getRedoItem());

		// Add to the cool bar manager
		coolBar.add(actionBarConfigurer.createToolBarContributionItem(editToolBar,
				IWorkbenchActionConstants.TOOLBAR_EDIT));

		coolBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

		// Navigate group
		coolBar.add(new GroupMarker(IIDEActionConstants.GROUP_NAV));
		IToolBarManager navToolBar = actionBarConfigurer.createToolBarManager();
		navToolBar.add(new Separator(
				IWorkbenchActionConstants.HISTORY_GROUP));
		navToolBar
				.add(new GroupMarker(IWorkbenchActionConstants.GROUP_APP));
		navToolBar.add(backwardHistoryAction);
		navToolBar.add(forwardHistoryAction);
		navToolBar.add(new Separator(IWorkbenchActionConstants.PIN_GROUP));
		navToolBar.add(getPinEditorItem());

		// Add to the cool bar manager
		coolBar.add(actionBarConfigurer.createToolBarContributionItem(navToolBar,
				IWorkbenchActionConstants.TOOLBAR_NAVIGATE));

		coolBar.add(new GroupMarker(IWorkbenchActionConstants.GROUP_EDITOR));

		coolBar.add(new GroupMarker(IWorkbenchActionConstants.GROUP_HELP));

		// Help group
		IToolBarManager helpToolBar = actionBarConfigurer.createToolBarManager();
		helpToolBar.add(new Separator(IWorkbenchActionConstants.GROUP_HELP));
		// Add the group for applications to contribute
		helpToolBar.add(new GroupMarker(IWorkbenchActionConstants.GROUP_APP));
		// Add to the cool bar manager
		coolBar.add(actionBarConfigurer.createToolBarContributionItem(helpToolBar,
				IWorkbenchActionConstants.TOOLBAR_HELP));

	}

	/**
	 * Fills the menu bar with the workbench actions.
	 */
	@Override
	protected void fillMenuBar(IMenuManager menuBar) {
		menuBar.add(createFileMenu());
		menuBar.add(createEditMenu());
		menuBar.add(createNavigateMenu());
		menuBar.add(createProjectMenu());
		menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		menuBar.add(createWindowMenu());
		menuBar.add(createHelpMenu());
	}

	/**
	 * Creates and returns the File menu.
	 */
	private MenuManager createFileMenu() {
		MenuManager menu = new MenuManager(IDEWorkbenchMessages.Workbench_file, IWorkbenchActionConstants.M_FILE);
		menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_START));
		// create the New submenu, using the same id for it as the New action
		String newText = IDEWorkbenchMessages.Workbench_new;
		String newId = ActionFactory.NEW.getId();
		MenuManager newMenu = new MenuManager(newText, newId);
		newMenu.setActionDefinitionId("org.eclipse.ui.file.newQuickMenu"); //$NON-NLS-1$
		newMenu.add(new Separator(newId));
		this.newWizardMenu = new NewWizardMenu(getWindow());
		newMenu.add(this.newWizardMenu);
		newMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(newMenu);

		menu.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));

		MenuManager recent = new MenuManager(WorkbenchMessages.OpenRecentDocuments_text);
		recent.add(ContributionItemFactory.REOPEN_EDITORS.create(getWindow()));
		recent.add(new GroupMarker(IWorkbenchActionConstants.MRU));
		menu.add(recent);

		menu.add(new Separator());

		menu.add(closeAction);
		menu.add(closeAllAction);
		//		menu.add(closeAllSavedAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.CLOSE_EXT));
		menu.add(new Separator());
		menu.add(saveAction);
		menu.add(saveAsAction);
		menu.add(saveAllAction);
		menu.add(getRevertItem());
		menu.add(new Separator());
		menu.add(getMoveItem());
		menu.add(getRenameItem());
		menu.add(getRefreshItem());

		menu.add(new GroupMarker(IWorkbenchActionConstants.SAVE_EXT));
		menu.add(new Separator());
		menu.add(getPrintItem());
		menu.add(new GroupMarker(IWorkbenchActionConstants.PRINT_EXT));
		menu.add(new Separator());
		menu.add(new GroupMarker(IWorkbenchActionConstants.OPEN_EXT));
		menu.add(new Separator());
		menu.add(importResourcesAction);
		menu.add(exportResourcesAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.IMPORT_EXT));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		menu.add(new Separator());
		menu.add(getPropertiesItem());

		menu.add(new Separator());

		menu.add(openWorkspaceAction);

		// If we're on OS X we shouldn't show this command in the File menu. It
		// should be invisible to the user. However, we should not remove it -
		// the carbon UI code will do a search through our menu structure
		// looking for it when Cmd-Q is invoked (or Quit is chosen from the
		// application menu.
		ActionContributionItem quitItem = new ActionContributionItem(quitAction);
		quitItem.setVisible(!Util.isMac());
		menu.add(quitItem);
		menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_END));
		return menu;
	}

	/**
	 * Creates and returns the Edit menu.
	 */
	private MenuManager createEditMenu() {
		MenuManager menu = new MenuManager(IDEWorkbenchMessages.Workbench_edit, IWorkbenchActionConstants.M_EDIT);
		menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_START));

		menu.add(getUndoItem());
		menu.add(getRedoItem());
		menu.add(new GroupMarker(IWorkbenchActionConstants.UNDO_EXT));
		menu.add(new Separator());

		menu.add(getCutItem());
		menu.add(getCopyItem());
		menu.add(getPasteItem());
		menu.add(new GroupMarker(IWorkbenchActionConstants.CUT_EXT));
		menu.add(new Separator());

		menu.add(getDeleteItem());
		menu.add(getSelectAllItem());
		menu.add(new Separator());

		menu.add(getFindItem());
		menu.add(new GroupMarker(IWorkbenchActionConstants.FIND_EXT));
		menu.add(new Separator());

		menu.add(getBookmarkItem());
		menu.add(getTaskItem());
		menu.add(new GroupMarker(IWorkbenchActionConstants.ADD_EXT));

		menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_END));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		return menu;
	}

	/**
	 * Creates and returns the Navigate menu.
	 */
	private MenuManager createNavigateMenu() {
		MenuManager menu = new MenuManager(
				IDEWorkbenchMessages.Workbench_navigate, IWorkbenchActionConstants.M_NAVIGATE);
		menu.add(new GroupMarker(IWorkbenchActionConstants.NAV_START));
		menu.add(goIntoAction);

		MenuManager goToSubMenu = new MenuManager(IDEWorkbenchMessages.Workbench_goTo, IWorkbenchActionConstants.GO_TO);
		menu.add(goToSubMenu);
		goToSubMenu.add(backAction);
		goToSubMenu.add(forwardAction);
		goToSubMenu.add(upAction);
		goToSubMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		menu.add(new Separator(IWorkbenchActionConstants.OPEN_EXT));
		for (int i = 2; i < 5; ++i) {
			menu.add(new Separator(IWorkbenchActionConstants.OPEN_EXT + i));
		}
		menu.add(new Separator(IWorkbenchActionConstants.SHOW_EXT));
		MenuManager showInSubMenu = new MenuManager(
				IDEWorkbenchMessages.Workbench_showIn, "showIn"); //$NON-NLS-1$
		showInSubMenu.setActionDefinitionId(showInQuickMenu
				.getActionDefinitionId());
		showInSubMenu.add(ContributionItemFactory.VIEWS_SHOW_IN
				.create(getWindow()));
		menu.add(showInSubMenu);
		for (int i = 2; i < 5; ++i) {
			menu.add(new Separator(IWorkbenchActionConstants.SHOW_EXT + i));
		}
		menu.add(new Separator());
		menu.add(nextAction);
		menu.add(previousAction);
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(new GroupMarker(IWorkbenchActionConstants.NAV_END));

		//TBD: Location of this actions
		menu.add(new Separator());
		menu.add(backwardHistoryAction);
		menu.add(forwardHistoryAction);
		return menu;
	}

	/**
	 * Creates and returns the Project menu.
	 */
	private MenuManager createProjectMenu() {
		MenuManager menu = new MenuManager(
				IDEWorkbenchMessages.Workbench_project, IWorkbenchActionConstants.M_PROJECT);
		menu.add(new Separator(IWorkbenchActionConstants.PROJ_START));

		menu.add(getOpenProjectItem());
		menu.add(getCloseProjectItem());
		menu.add(new GroupMarker(IWorkbenchActionConstants.OPEN_EXT));
		menu.add(new Separator());
		menu.add(buildAllAction);
		menu.add(buildProjectAction);
		addWorkingSetBuildActions(menu);
		menu.add(cleanAction);
		menu.add(toggleAutoBuildAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.BUILD_EXT));
		menu.add(new Separator());

		menu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(new GroupMarker(IWorkbenchActionConstants.PROJ_END));
		menu.add(new Separator());
		menu.add(projectPropertyDialogAction);
		return menu;
	}

	/**
	 * Creates and returns the Window menu.
	 */
	private MenuManager createWindowMenu() {
		MenuManager menu = new MenuManager(
				IDEWorkbenchMessages.Workbench_window, IWorkbenchActionConstants.M_WINDOW);

		addMacWindowMenuItems(menu);

		menu.add(newWindowAction);

		menu.add(new Separator());

		menu.add(addShowView());
		menu.add(addPerspectiveActions());
		menu.add(new Separator());
		addKeyboardShortcuts(menu);
		Separator sep = new Separator(IWorkbenchActionConstants.MB_ADDITIONS);
		sep.setVisible(!Util.isMac());
		menu.add(sep);

		if(Util.isCocoa())
			menu.add(arrangeWindowsItem);

		// See the comment for quit in createFileMenu
		ActionContributionItem openPreferencesItem = new ActionContributionItem(openPreferencesAction);
		openPreferencesItem.setVisible(!Util.isMac());
		menu.add(openPreferencesItem);

		menu.add(ContributionItemFactory.OPEN_WINDOWS.create(getWindow()));
		return menu;
	}

	private void addMacWindowMenuItems(MenuManager windowMenu) {

		if (!Util.isCocoa())
			return;

		windowMenu.add(minimizeItem);
		windowMenu.add(zoomItem);
		windowMenu.add(fullscreenItem);
		windowMenu.add(new Separator());
	}

	/** Creates and returns the Show View menu
	 */
	private MenuManager addShowView() {
		// 'Show View' menu entry
		MenuManager showViewMenuMgr = new MenuManager(IDEWorkbenchMessages.Workbench_showView, "showView"); //$NON-NLS-1$
		IContributionItem showViewMenu = ContributionItemFactory.VIEWS_SHORTLIST.create(getWindow());
		showViewMenuMgr.add(showViewMenu);
		return showViewMenuMgr;
	}

/**
	 * Adds the perspective actions to the specified menu.
	 */
	private MenuManager addPerspectiveActions() {
		MenuManager menu = new MenuManager(IDEWorkbenchMessages.Workbench_perspective,
				IWorkbenchActionConstants.M_PERSPECTIVE);
		menu.add(new GroupMarker(IWorkbenchActionConstants.PERSPECTIVE_START));

		// 'Open Perspective' menu entry
		String openText = IDEWorkbenchMessages.Workbench_openPerspective;
		MenuManager changePerspMenuMgr = new MenuManager(openText,
				WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_ETOOL_NEW_PAGE), "openPerspective"); //$NON-NLS-1$
		IContributionItem changePerspMenuItem = ContributionItemFactory.PERSPECTIVES_SHORTLIST.create(getWindow());
		changePerspMenuMgr.add(changePerspMenuItem);
		menu.add(changePerspMenuMgr);

		menu.add(new Separator());
		menu.add(editActionSetAction);
		menu.add(getSavePerspectiveItem());
		menu.add(getResetPerspectiveItem());
		menu.add(closePerspAction);
		menu.add(closeAllPerspsAction);

		return menu;
	}

	/**
	 * Adds the keyboard navigation submenu to the specified menu.
	 */
	private void addWorkingSetBuildActions(MenuManager menu) {
		buildWorkingSetMenu = new MenuManager(IDEWorkbenchMessages.Workbench_buildSet);
		IContributionItem workingSetBuilds = new BuildSetMenu(workbenchWindow,
				getActionBarConfigurer());
		buildWorkingSetMenu.add(workingSetBuilds);
		menu.add(buildWorkingSetMenu);
	}

	/**
	 * Adds the keyboard navigation submenu to the specified menu.
	 */
	private void addKeyboardShortcuts(MenuManager menu) {
		MenuManager subMenu = new MenuManager(IDEWorkbenchMessages.Workbench_shortcuts, "shortcuts"); //$NON-NLS-1$
		menu.add(subMenu);
		subMenu.add(showPartPaneMenuAction);
		subMenu.add(showViewMenuAction);
		subMenu.add(quickAccessAction);
		subMenu.add(new Separator());
		subMenu.add(activateEditorAction);
		subMenu.add(nextEditorAction);
		subMenu.add(prevEditorAction);
		subMenu.add(switchToEditorAction);
		subMenu.add(new Separator());
		subMenu.add(nextPartAction);
		subMenu.add(prevPartAction);
		subMenu.add(new Separator());
		subMenu.add(nextPerspectiveAction);
		subMenu.add(prevPerspectiveAction);
	}

	/**
	 * Creates and returns the Help menu.
	 */
	private MenuManager createHelpMenu() {
		MenuManager menu = new MenuManager(IDEWorkbenchMessages.Workbench_help, IWorkbenchActionConstants.M_HELP);
		addSeparatorOrGroupMarker(menu, "group.intro"); //$NON-NLS-1$
		// See if a welcome or intro page is specified
		if (introAction != null) {
			menu.add(introAction);
		} else if (quickStartAction != null) {
			menu.add(quickStartAction);
		}
		menu.add(new GroupMarker("group.intro.ext")); //$NON-NLS-1$
		addSeparatorOrGroupMarker(menu, "group.main"); //$NON-NLS-1$
		menu.add(helpContentsAction);
		menu.add(helpSearchAction);
		menu.add(dynamicHelpAction);
		addSeparatorOrGroupMarker(menu, "group.assist"); //$NON-NLS-1$
		// See if a tips and tricks page is specified
		if (tipsAndTricksAction != null) {
			menu.add(tipsAndTricksAction);
		}
		// HELP_START should really be the first item, but it was after
		// quickStartAction and tipsAndTricksAction in 2.1.
		menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_START));
		menu.add(new GroupMarker("group.main.ext")); //$NON-NLS-1$
		addSeparatorOrGroupMarker(menu, "group.tutorials"); //$NON-NLS-1$
		addSeparatorOrGroupMarker(menu, "group.tools"); //$NON-NLS-1$
		addSeparatorOrGroupMarker(menu, "group.updates"); //$NON-NLS-1$
		menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_END));
		addSeparatorOrGroupMarker(menu, IWorkbenchActionConstants.MB_ADDITIONS);
		// about should always be at the bottom
		menu.add(new Separator("group.about")); //$NON-NLS-1$

		ActionContributionItem aboutItem = new ActionContributionItem(aboutAction);
		aboutItem.setVisible(!Util.isMac());
		menu.add(aboutItem);
		menu.add(new GroupMarker("group.about.ext")); //$NON-NLS-1$
		return menu;
	}

	/**
	 * Adds a <code>GroupMarker</code> or <code>Separator</code> to a menu.
	 * The test for whether a separator should be added is done by checking for
	 * the existence of a preference matching the string
	 * useSeparator.MENUID.GROUPID that is set to <code>true</code>.
	 *
	 * @param menu
	 *            the menu to add to
	 * @param groupId
	 *            the group id for the added separator or group marker
	 */
	private void addSeparatorOrGroupMarker(MenuManager menu, String groupId) {
		String prefId = "useSeparator." + menu.getId() + "." + groupId; //$NON-NLS-1$ //$NON-NLS-2$
		boolean addExtraSeparators = IDEWorkbenchPlugin.getDefault()
				.getPreferenceStore().getBoolean(prefId);
		if (addExtraSeparators) {
			menu.add(new Separator(groupId));
		} else {
			menu.add(new GroupMarker(groupId));
		}
	}

	/**
	 * Disposes any resources and unhooks any listeners that are no longer needed.
	 * Called when the window is closed.
	 */
	@Override
	public void dispose() {
		if (isDisposed) {
			return;
		}
		isDisposed = true;

		getActionBarConfigurer().getStatusLineManager().remove(statusLineItem);
		if (pageListener != null) {
			workbenchWindow.removePageListener(pageListener);
			pageListener = null;
		}
		if (partListener != null) {
			workbenchWindow.getPartService().removePartListener(partListener);
			partListener = null;
		}
		if (prefListener != null) {
			ResourcesPlugin.getPlugin().getPluginPreferences()
					.removePropertyChangeListener(prefListener);
			prefListener = null;
		}
		if (propPrefListener != null) {
			WorkbenchPlugin.getDefault().getPreferenceStore()
					.removePropertyChangeListener(propPrefListener);
			propPrefListener = null;
		}
		if (resourceListener != null) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener);
			resourceListener = null;
		}

		showInQuickMenu.dispose();
		newQuickMenu.dispose();


		// null out actions to make leak debugging easier
		closeAction = null;
		closeAllAction = null;
		closeAllSavedAction = null;
		closeOthersAction = null;
		saveAction = null;
		saveAllAction = null;
		newWindowAction = null;
		helpContentsAction = null;
		helpSearchAction = null;
		dynamicHelpAction = null;
		aboutAction = null;
		openPreferencesAction = null;
		saveAsAction = null;
		hideShowEditorAction = null;
		editActionSetAction = null;
		closePerspAction = null;
		lockToolBarAction = null;
		closeAllPerspsAction = null;
		showViewMenuAction = null;
		showPartPaneMenuAction = null;
		nextPartAction = null;
		prevPartAction = null;
		nextEditorAction = null;
		prevEditorAction = null;
		nextPerspectiveAction = null;
		prevPerspectiveAction = null;
		activateEditorAction = null;
		switchToEditorAction = null;
		quickAccessAction.dispose();
		quickAccessAction = null;
		backwardHistoryAction = null;
		forwardHistoryAction = null;
		quitAction = null;
		goIntoAction = null;
		backAction = null;
		forwardAction = null;
		upAction = null;
		nextAction = null;
		previousAction = null;
		openWorkspaceAction = null;
		projectPropertyDialogAction = null;
		newWizardAction = null;
		newWizardDropDownAction = null;
		importResourcesAction = null;
		exportResourcesAction = null;
		buildAllAction = null;
		cleanAction = null;
		toggleAutoBuildAction = null;
		buildWorkingSetMenu = null;
		quickStartAction = null;
		tipsAndTricksAction = null;
		showInQuickMenu = null;
		newQuickMenu = null;
		buildProjectAction = null;
		if (newWizardMenu != null) {
			newWizardMenu.dispose();
			newWizardMenu = null;
		}
		statusLineItem = null;
		prefListener = null;
		propPrefListener = null;
		introAction = null;
		minimizeItem = null;
		zoomItem = null;
		fullscreenItem = null;
		arrangeWindowsItem = null;
		super.dispose();
	}

	void updateModeLine(final String text) {
		statusLineItem.setText(text);
	}

	/**
	 * Returns true if the menu with the given ID should
	 * be considered as an OLE container menu. Container menus
	 * are preserved in OLE menu merging.
	 */
	@Override
	public boolean isApplicationMenu(String menuId) {
		if (menuId.equals(IWorkbenchActionConstants.M_FILE)) {
			return true;
		}
		if (menuId.equals(IWorkbenchActionConstants.M_WINDOW)) {
			return true;
		}
		return false;
	}

	/**
	 * Return whether or not given id matches the id of the coolitems that
	 * the workbench creates.
	 */
	public boolean isWorkbenchCoolItemId(String id) {
		if (IWorkbenchActionConstants.TOOLBAR_FILE.equalsIgnoreCase(id)) {
			return true;
		}
		if (IWorkbenchActionConstants.TOOLBAR_NAVIGATE.equalsIgnoreCase(id)) {
			return true;
		}
		return false;
	}

	/**
	 * Fills the status line with the workbench contribution items.
	 */
	@Override
	protected void fillStatusLine(IStatusLineManager statusLine) {
		statusLine.add(statusLineItem);
	}

	/**
	 * Creates actions (and contribution items) for the menu bar, toolbar and status line.
	 */
	@Override
	protected void makeActions(final IWorkbenchWindow window) {
		// @issue should obtain from ConfigurationItemFactory
		statusLineItem = new StatusLineContributionItem("ModeContributionItem"); //$NON-NLS-1$

		newWizardAction = ActionFactory.NEW.create(window);
		register(newWizardAction);

		newWizardDropDownAction = IDEActionFactory.NEW_WIZARD_DROP_DOWN
				.create(window);
		register(newWizardDropDownAction);

		importResourcesAction = ActionFactory.IMPORT.create(window);
		register(importResourcesAction);

		exportResourcesAction = ActionFactory.EXPORT.create(window);
		register(exportResourcesAction);

		buildAllAction = IDEActionFactory.BUILD.create(window);
		register(buildAllAction);

		cleanAction = IDEActionFactory.BUILD_CLEAN.create(window);
		register(cleanAction);

		toggleAutoBuildAction = IDEActionFactory.BUILD_AUTOMATICALLY
				.create(window);
		register(toggleAutoBuildAction);

		saveAction = ActionFactory.SAVE.create(window);
		register(saveAction);

		saveAsAction = ActionFactory.SAVE_AS.create(window);
		register(saveAsAction);

		saveAllAction = ActionFactory.SAVE_ALL.create(window);
		register(saveAllAction);

		newWindowAction = ActionFactory.OPEN_NEW_WINDOW.create(getWindow());
		newWindowAction.setText(IDEWorkbenchMessages.Workbench_openNewWindow);
		register(newWindowAction);

		closeAction = ActionFactory.CLOSE.create(window);
		register(closeAction);

		closeAllAction = ActionFactory.CLOSE_ALL.create(window);
		register(closeAllAction);

		closeOthersAction = ActionFactory.CLOSE_OTHERS.create(window);
		register(closeOthersAction);

		closeAllSavedAction = ActionFactory.CLOSE_ALL_SAVED.create(window);
		register(closeAllSavedAction);

		helpContentsAction = ActionFactory.HELP_CONTENTS.create(window);
		register(helpContentsAction);

		helpSearchAction = ActionFactory.HELP_SEARCH.create(window);
		register(helpSearchAction);

		dynamicHelpAction = ActionFactory.DYNAMIC_HELP.create(window);
		register(dynamicHelpAction);

		aboutAction = ActionFactory.ABOUT.create(window);
		aboutAction
				.setImageDescriptor(IDEInternalWorkbenchImages
						.getImageDescriptor(IDEInternalWorkbenchImages.IMG_OBJS_DEFAULT_PROD));
		register(aboutAction);

		openPreferencesAction = ActionFactory.PREFERENCES.create(window);
		register(openPreferencesAction);





		makeFeatureDependentActions(window);

		// Actions for invisible accelerators
		showViewMenuAction = ActionFactory.SHOW_VIEW_MENU.create(window);
		register(showViewMenuAction);

		showPartPaneMenuAction = ActionFactory.SHOW_PART_PANE_MENU
				.create(window);
		register(showPartPaneMenuAction);

		nextEditorAction = ActionFactory.NEXT_EDITOR.create(window);
		register(nextEditorAction);
		prevEditorAction = ActionFactory.PREVIOUS_EDITOR.create(window);
		register(prevEditorAction);
		ActionFactory.linkCycleActionPair(nextEditorAction, prevEditorAction);

		nextPartAction = ActionFactory.NEXT_PART.create(window);
		register(nextPartAction);
		prevPartAction = ActionFactory.PREVIOUS_PART.create(window);
		register(prevPartAction);
		ActionFactory.linkCycleActionPair(nextPartAction, prevPartAction);

		nextPerspectiveAction = ActionFactory.NEXT_PERSPECTIVE
				.create(window);
		register(nextPerspectiveAction);
		prevPerspectiveAction = ActionFactory.PREVIOUS_PERSPECTIVE
				.create(window);
		register(prevPerspectiveAction);
		ActionFactory.linkCycleActionPair(nextPerspectiveAction,
				prevPerspectiveAction);

		activateEditorAction = ActionFactory.ACTIVATE_EDITOR
				.create(window);
		register(activateEditorAction);

		switchToEditorAction = ActionFactory.SHOW_OPEN_EDITORS
				.create(window);
		register(switchToEditorAction);

		workbookEditorsAction = ActionFactory.SHOW_WORKBOOK_EDITORS
				.create(window);
		register(workbookEditorsAction);

		quickAccessAction = ActionFactory.SHOW_QUICK_ACCESS
			.create(window);

		hideShowEditorAction = ActionFactory.SHOW_EDITOR.create(window);
		register(hideShowEditorAction);
		editActionSetAction = ActionFactory.EDIT_ACTION_SETS
				.create(window);
		register(editActionSetAction);
		lockToolBarAction = ActionFactory.LOCK_TOOL_BAR.create(window);
		register(lockToolBarAction);
		closePerspAction = ActionFactory.CLOSE_PERSPECTIVE.create(window);
		register(closePerspAction);
		closeAllPerspsAction = ActionFactory.CLOSE_ALL_PERSPECTIVES
				.create(window);
		register(closeAllPerspsAction);

		forwardHistoryAction = ActionFactory.FORWARD_HISTORY
				.create(window);
		register(forwardHistoryAction);

		backwardHistoryAction = ActionFactory.BACKWARD_HISTORY
				.create(window);
		register(backwardHistoryAction);




		quitAction = ActionFactory.QUIT.create(window);
		register(quitAction);



		goIntoAction = ActionFactory.GO_INTO.create(window);
		register(goIntoAction);

		backAction = ActionFactory.BACK.create(window);
		register(backAction);

		forwardAction = ActionFactory.FORWARD.create(window);
		register(forwardAction);

		upAction = ActionFactory.UP.create(window);
		register(upAction);

		nextAction = ActionFactory.NEXT.create(window);
		nextAction
				.setImageDescriptor(IDEInternalWorkbenchImages
						.getImageDescriptor(IDEInternalWorkbenchImages.IMG_ETOOL_NEXT_NAV));
		register(nextAction);

		previousAction = ActionFactory.PREVIOUS.create(window);
		previousAction
				.setImageDescriptor(IDEInternalWorkbenchImages
						.getImageDescriptor(IDEInternalWorkbenchImages.IMG_ETOOL_PREVIOUS_NAV));
		register(previousAction);

		buildProjectAction = IDEActionFactory.BUILD_PROJECT.create(window);
		register(buildProjectAction);

		openWorkspaceAction = makeOpenWorkspaceAction(window);
		register(openWorkspaceAction);

		projectPropertyDialogAction = IDEActionFactory.OPEN_PROJECT_PROPERTIES
				.create(window);
		register(projectPropertyDialogAction);

		if (window.getWorkbench().getIntroManager().hasIntro()) {
			introAction = ActionFactory.INTRO.create(window);
			register(introAction);
		}

		String showInQuickMenuId = IWorkbenchCommandConstants.NAVIGATE_SHOW_IN_QUICK_MENU;
		showInQuickMenu = new QuickMenuAction(showInQuickMenuId) {
			@Override
			protected void fillMenu(IMenuManager menu) {
				menu.add(ContributionItemFactory.VIEWS_SHOW_IN
						.create(window));
			}
		};
		register(showInQuickMenu);

		final String newQuickMenuId = "org.eclipse.ui.file.newQuickMenu"; //$NON-NLS-1$
		newQuickMenu = new QuickMenuAction(newQuickMenuId) {
			@Override
			protected void fillMenu(IMenuManager menu) {
				menu.add(new NewWizardMenu(window));
			}
		};
		register(newQuickMenu);


		if (Util.isCocoa()) {

			CommandContributionItemParameter minimizeParam = new CommandContributionItemParameter(window, null,
					"org.eclipse.ui.cocoa.minimizeWindow", CommandContributionItem.STYLE_PUSH); //$NON-NLS-1$
			minimizeItem = new CommandContributionItem(minimizeParam);
			CommandContributionItemParameter zoomParam = new CommandContributionItemParameter(window, null,
					"org.eclipse.ui.cocoa.zoomWindow", CommandContributionItem.STYLE_PUSH); //$NON-NLS-1$
			zoomItem = new CommandContributionItem(zoomParam);
			CommandContributionItemParameter fullscreenParam = new CommandContributionItemParameter(
					window, null, "org.eclipse.ui.window.fullscreenmode", CommandContributionItem.STYLE_PUSH); //$NON-NLS-1$
			fullscreenItem = new CommandContributionItem(fullscreenParam);
			CommandContributionItemParameter arrangeWindowsParam = new CommandContributionItemParameter(window, null,
					"org.eclipse.ui.cocoa.arrangeWindowsInFront", CommandContributionItem.STYLE_PUSH); //$NON-NLS-1$
			arrangeWindowsItem = new CommandContributionItem(arrangeWindowsParam);
		}

	}

	/**
	 * @param window the workbench window
	 * @return Creates a new open workspace action for the given workbench window.
	 */
	protected IWorkbenchAction makeOpenWorkspaceAction(final IWorkbenchWindow window) {
		return IDEActionFactory.OPEN_WORKSPACE.create(window);
	}

	/**
	 * Creates the feature-dependent actions for the menu bar.
	 */
	private void makeFeatureDependentActions(IWorkbenchWindow window) {
		AboutInfo[] infos = null;

		IPreferenceStore prefs = IDEWorkbenchPlugin.getDefault().getPreferenceStore();

		// Optimization: avoid obtaining the about infos if the platform state is
		// unchanged from last time.  See bug 75130 for details.
		String stateKey = "platformState"; //$NON-NLS-1$
		String prevState = prefs.getString(stateKey);
		String currentState = String.valueOf(Platform.getStateStamp());
		boolean sameState = currentState.equals(prevState);
		if (!sameState) {
			prefs.putValue(stateKey, currentState);
		}

		// See if a welcome page is specified.
		// Optimization: if welcome pages were found on a previous run, then just add the action.
		String quickStartKey = IDEActionFactory.QUICK_START.getId();
		String showQuickStart = prefs.getString(quickStartKey);
		if (sameState && "true".equals(showQuickStart)) { //$NON-NLS-1$
			quickStartAction = IDEActionFactory.QUICK_START.create(window);
			register(quickStartAction);
		}
		else if (sameState && "false".equals(showQuickStart)) { //$NON-NLS-1$
			// do nothing
		}
		else {
			// do the work
			infos = IDEWorkbenchPlugin.getDefault().getFeatureInfos();
			boolean found = hasWelcomePage(infos);
			prefs.setValue(quickStartKey, String.valueOf(found));
			if (found) {
				quickStartAction = IDEActionFactory.QUICK_START.create(window);
				register(quickStartAction);
			}
		}

		// See if a tips and tricks page is specified.
		// Optimization: if tips and tricks were found on a previous run, then just add the action.
		String tipsAndTricksKey = IDEActionFactory.TIPS_AND_TRICKS.getId();
		String showTipsAndTricks = prefs.getString(tipsAndTricksKey);
		if (sameState && "true".equals(showTipsAndTricks)) { //$NON-NLS-1$
			tipsAndTricksAction = IDEActionFactory.TIPS_AND_TRICKS
					.create(window);
			register(tipsAndTricksAction);
		}
		else if (sameState && "false".equals(showTipsAndTricks)) { //$NON-NLS-1$
			// do nothing
		}
		else {
			// do the work
			if (infos == null) {
				infos = IDEWorkbenchPlugin.getDefault().getFeatureInfos();
			}
			boolean found = hasTipsAndTricks(infos);
			prefs.setValue(tipsAndTricksKey, String.valueOf(found));
			if (found) {
				tipsAndTricksAction = IDEActionFactory.TIPS_AND_TRICKS
						.create(window);
				register(tipsAndTricksAction);
			}
		}
	}

	/**
	 * Returns whether any of the given infos have a welcome page.
	 *
	 * @param infos the infos
	 * @return <code>true</code> if a welcome page was found, <code>false</code> if not
	 */
	private boolean hasWelcomePage(AboutInfo[] infos) {
		for (AboutInfo info : infos) {
			if (info.getWelcomePageURL() != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns whether any of the given infos have tips and tricks.
	 *
	 * @param infos the infos
	 * @return <code>true</code> if tips and tricks were found, <code>false</code> if not
	 */
	private boolean hasTipsAndTricks(AboutInfo[] infos) {
		for (AboutInfo info : infos) {
			if (info.getTipsAndTricksHref() != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Update the build actions on the toolbar and menu bar based on the current
	 * state of autobuild. This method can be called from any thread.
	 *
	 * @param immediately
	 *            <code>true</code> to update the actions immediately,
	 *            <code>false</code> to queue the update to be run in the
	 *            event loop
	 */
	void updateBuildActions(boolean immediately) {
		// this can be triggered by property or resource change notifications
		Runnable update = new Runnable() {
			@Override
			public void run() {
				if (isDisposed) {
					return;
				}
				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				IProject[] projects = workspace.getRoot().getProjects();
				boolean enabled = BuildUtilities.isEnabled(projects, IncrementalProjectBuilder.INCREMENTAL_BUILD);
				//update menu bar actions in project menu
				updateCommandEnablement(buildAllAction.getActionDefinitionId());
				buildProjectAction.setEnabled(enabled);
				toggleAutoBuildAction.setChecked(workspace.isAutoBuilding());
				cleanAction.setEnabled(BuildUtilities.isEnabled(projects, IncrementalProjectBuilder.CLEAN_BUILD));

				//update the cool bar build button
				ICoolBarManager coolBarManager = getActionBarConfigurer()
						.getCoolBarManager();
				IContributionItem cbItem = coolBarManager
						.find(IWorkbenchActionConstants.TOOLBAR_FILE);
				if (!(cbItem instanceof IToolBarContributionItem)) {
					// This should not happen
					IDEWorkbenchPlugin.log("File toolbar contribution item is missing"); //$NON-NLS-1$
					return;
				}
				IToolBarContributionItem toolBarItem = (IToolBarContributionItem) cbItem;
				IToolBarManager toolBarManager = toolBarItem.getToolBarManager();
				if (toolBarManager == null) {
					// error if this happens, file toolbar assumed to always exist
					IDEWorkbenchPlugin.log("File toolbar is missing"); //$NON-NLS-1$
					return;
				}
				//add the build button if build actions are enabled, and remove it otherwise
				boolean found = toolBarManager.find(buildAllAction.getId()) != null;
				if (enabled && !found) {
					toolBarManager.appendToGroup(IWorkbenchActionConstants.BUILD_GROUP,
							buildAllAction);
					toolBarManager.update(false);
					toolBarItem.update(ICoolBarManager.SIZE);
				} else if (buildAllAction != null && found && !enabled) {
					toolBarManager.remove(buildAllAction.getId());
					toolBarManager.update(false);
					toolBarItem.update(ICoolBarManager.SIZE);
				}
			}

			private void updateCommandEnablement(String commandId) {
				IHandlerService handlerService = workbenchWindow.getService(IHandlerService.class);
				ICommandService commandService = workbenchWindow.getService(ICommandService.class);
				if (handlerService != null && commandService != null) {
					Command buildAllCmd = commandService.getCommand(commandId);
					buildAllCmd.setEnabled(handlerService.getCurrentState());
				}
			}
		};
		if (immediately) {
			update.run();
		}
		else {
			// Dispatch the update to be run later in the UI thread.
			// This helps to reduce flicker if autobuild is being temporarily disabled programmatically.
			Shell shell = workbenchWindow.getShell();
			if (shell != null && !shell.isDisposed()) {
				shell.getDisplay().asyncExec(update);
			}
		}
	}

	/**
	 * Update the pin action's tool bar
	 */
	void updatePinActionToolbar() {

		ICoolBarManager coolBarManager = getActionBarConfigurer()
				.getCoolBarManager();
		IContributionItem cbItem = coolBarManager
				.find(IWorkbenchActionConstants.TOOLBAR_NAVIGATE);
		if (!(cbItem instanceof IToolBarContributionItem)) {
			// This should not happen
			IDEWorkbenchPlugin
					.log("Navigation toolbar contribution item is missing"); //$NON-NLS-1$
			return;
		}
		IToolBarContributionItem toolBarItem = (IToolBarContributionItem) cbItem;
		IToolBarManager toolBarManager = toolBarItem.getToolBarManager();
		if (toolBarManager == null) {
			// error if this happens, navigation toolbar assumed to always exist
			IDEWorkbenchPlugin.log("Navigate toolbar is missing"); //$NON-NLS-1$
			return;
		}
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		boolean reuseEditors = store.getBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN);
		IContributionItem pinItem = toolBarManager.find(IWorkbenchCommandConstants.WINDOW_PIN_EDITOR);
		if (pinItem != null) {
			pinItem.setVisible(reuseEditors);
		}
		toolBarManager.markDirty();
		toolBarManager.update(false);
		toolBarItem.update(ICoolBarManager.SIZE);
		workbenchWindow.getShell().getDisplay().asyncExec(() -> {
			if (workbenchWindow.getShell() != null && !workbenchWindow.getShell().isDisposed()) {
				ICommandService commandService = workbenchWindow.getService(ICommandService.class);
				commandService.refreshElements(IWorkbenchCommandConstants.WINDOW_PIN_EDITOR, null);
			}
		});
	}

	private IContributionItem getPinEditorItem() {
		return ContributionItemFactory.PIN_EDITOR.create(workbenchWindow);
	}

	private IContributionItem getUndoItem() {
		return getItem(ActionFactory.UNDO.getId(), ActionFactory.UNDO.getCommandId(), ISharedImages.IMG_TOOL_UNDO,
				ISharedImages.IMG_TOOL_UNDO_DISABLED, WorkbenchMessages.Workbench_undo,
				WorkbenchMessages.Workbench_undoToolTip);
	}

	private IContributionItem getRedoItem() {
		return getItem(ActionFactory.REDO.getId(), ActionFactory.REDO.getCommandId(), ISharedImages.IMG_TOOL_REDO,
				ISharedImages.IMG_TOOL_REDO_DISABLED, WorkbenchMessages.Workbench_redo,
				WorkbenchMessages.Workbench_redoToolTip);
	}
	private IContributionItem getCutItem() {
		return getItem(ActionFactory.CUT.getId(), ActionFactory.CUT.getCommandId(), ISharedImages.IMG_TOOL_CUT,
				ISharedImages.IMG_TOOL_CUT_DISABLED, WorkbenchMessages.Workbench_cut,
				WorkbenchMessages.Workbench_cutToolTip);
	}

	private IContributionItem getCopyItem() {
		return getItem(ActionFactory.COPY.getId(), ActionFactory.COPY.getCommandId(), ISharedImages.IMG_TOOL_COPY,
				ISharedImages.IMG_TOOL_COPY_DISABLED, WorkbenchMessages.Workbench_copy,
				WorkbenchMessages.Workbench_copyToolTip);
	}

	private IContributionItem getResetPerspectiveItem() {
		return getItem(ActionFactory.RESET_PERSPECTIVE.getId(), ActionFactory.RESET_PERSPECTIVE.getCommandId(), null,
				null, WorkbenchMessages.ResetPerspective_text, WorkbenchMessages.ResetPerspective_toolTip);
	}

	private IContributionItem getSavePerspectiveItem() {
		return getItem(ActionFactory.SAVE_PERSPECTIVE.getId(), ActionFactory.SAVE_PERSPECTIVE.getCommandId(), null,
				null, WorkbenchMessages.SavePerspective_text, WorkbenchMessages.SavePerspective_toolTip);
	}

	private IContributionItem getPasteItem() {
		return getItem(ActionFactory.PASTE.getId(), ActionFactory.PASTE.getCommandId(), ISharedImages.IMG_TOOL_PASTE,
				ISharedImages.IMG_TOOL_PASTE_DISABLED, WorkbenchMessages.Workbench_paste,
				WorkbenchMessages.Workbench_pasteToolTip);
	}

	private IContributionItem getPrintItem() {
		return getItem(ActionFactory.PRINT.getId(), ActionFactory.PRINT.getCommandId(),
				ISharedImages.IMG_ETOOL_PRINT_EDIT, ISharedImages.IMG_ETOOL_PRINT_EDIT_DISABLED,
				WorkbenchMessages.Workbench_print, WorkbenchMessages.Workbench_printToolTip);
	}

	private IContributionItem getSelectAllItem() {
		return getItem(ActionFactory.SELECT_ALL.getId(), ActionFactory.SELECT_ALL.getCommandId(), null, null,
				WorkbenchMessages.Workbench_selectAll, WorkbenchMessages.Workbench_selectAllToolTip);
	}

	private IContributionItem getFindItem() {
		return getItem(ActionFactory.FIND.getId(), ActionFactory.FIND.getCommandId(), null, null,
				WorkbenchMessages.Workbench_findReplace, WorkbenchMessages.Workbench_findReplaceToolTip);
	}

	private IContributionItem getBookmarkItem() {
		return getItem(IDEActionFactory.BOOKMARK.getId(), IDEActionFactory.BOOKMARK.getCommandId(), null, null,
				IDEWorkbenchMessages.Workbench_addBookmark, IDEWorkbenchMessages.Workbench_addBookmarkToolTip);
	}

	private IContributionItem getTaskItem() {
		return getItem(IDEActionFactory.ADD_TASK.getId(), IDEActionFactory.ADD_TASK.getCommandId(), null, null,
				IDEWorkbenchMessages.Workbench_addTask, IDEWorkbenchMessages.Workbench_addTaskToolTip);
	}

	private IContributionItem getDeleteItem() {
		return getItem(ActionFactory.DELETE.getId(), ActionFactory.DELETE.getCommandId(), ISharedImages.IMG_TOOL_DELETE,
				ISharedImages.IMG_TOOL_DELETE_DISABLED, WorkbenchMessages.Workbench_delete,
				WorkbenchMessages.Workbench_deleteToolTip);
	}

	private IContributionItem getRevertItem() {
		return getItem(ActionFactory.REVERT.getId(), ActionFactory.REVERT.getCommandId(), null, null,
				WorkbenchMessages.Workbench_revert, WorkbenchMessages.Workbench_revertToolTip);
	}

	private IContributionItem getRefreshItem() {
		return getItem(ActionFactory.REFRESH.getId(), ActionFactory.REFRESH.getCommandId(), null, null,
				WorkbenchMessages.Workbench_refresh, WorkbenchMessages.Workbench_refreshToolTip);
	}

	private IContributionItem getPropertiesItem() {
		return getItem(ActionFactory.PROPERTIES.getId(), ActionFactory.PROPERTIES.getCommandId(), null, null,
				WorkbenchMessages.Workbench_properties, WorkbenchMessages.Workbench_propertiesToolTip);
	}

	private IContributionItem getMoveItem() {
		return getItem(ActionFactory.MOVE.getId(), ActionFactory.MOVE.getCommandId(), null, null,
				WorkbenchMessages.Workbench_move, WorkbenchMessages.Workbench_moveToolTip);
	}

	private IContributionItem getRenameItem() {
		return getItem(ActionFactory.RENAME.getId(), ActionFactory.RENAME.getCommandId(), null, null,
				WorkbenchMessages.Workbench_rename, WorkbenchMessages.Workbench_renameToolTip);
	}

	private IContributionItem getOpenProjectItem() {
		return getItem(IDEActionFactory.OPEN_PROJECT.getId(), IDEActionFactory.OPEN_PROJECT.getCommandId(), null, null,
				IDEWorkbenchMessages.OpenResourceAction_text, IDEWorkbenchMessages.OpenResourceAction_toolTip);
	}

	private IContributionItem getCloseProjectItem() {
		return getItem(IDEActionFactory.CLOSE_PROJECT.getId(), IDEActionFactory.CLOSE_PROJECT.getCommandId(), null,
				null, IDEWorkbenchMessages.CloseResourceAction_text, IDEWorkbenchMessages.CloseResourceAction_toolTip);
	}

	private IContributionItem getItem(String actionId, String commandId,
			String image, String disabledImage, String label, String tooltip) {
		ISharedImages sharedImages = getWindow().getWorkbench()
				.getSharedImages();

		IActionCommandMappingService acms = getWindow()
				.getService(IActionCommandMappingService.class);
		acms.map(actionId, commandId);

		CommandContributionItemParameter commandParm = new CommandContributionItemParameter(
				getWindow(), actionId, commandId, null, sharedImages
						.getImageDescriptor(image), sharedImages
						.getImageDescriptor(disabledImage), null, label, null,
				tooltip, CommandContributionItem.STYLE_PUSH, null, false);
		return new CommandContributionItem(commandParm);
	}
}

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

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.AboutInfo;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.NewWizardMenu;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * Adds actions to a workbench window.
 * 
 * @issue move WorkbenchActionBuilder to package org.eclipse.ui.internal.ide
 */
public final class WorkbenchActionBuilder {

	private IWorkbenchWindowConfigurer windowConfigurer;

	private final IPropertyChangeListener propertyChangeListener =
		new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			handlePropertyChange(event);
		}
	};

	// generic actions
	private IWorkbenchAction closeAction;
	private IWorkbenchAction closeAllAction;
	private IWorkbenchAction closeAllSavedAction;
	private IWorkbenchAction saveAction;
	private IWorkbenchAction saveAllAction;
	private IWorkbenchAction aboutAction;
	private IWorkbenchAction openPreferencesAction;
	private IWorkbenchAction saveAsAction;
	private IWorkbenchAction hideShowEditorAction;
	private IWorkbenchAction savePerspectiveAction;
	private IWorkbenchAction resetPerspectiveAction;
	private IWorkbenchAction editActionSetAction;
	private IWorkbenchAction closePerspAction;
	private IWorkbenchAction lockToolBarAction;
	private IWorkbenchAction closeAllPerspsAction;
	private IWorkbenchAction pinEditorAction;
	private IWorkbenchAction showViewMenuAction;
	private IWorkbenchAction showPartPaneMenuAction;
	private IWorkbenchAction nextPartAction;
	private IWorkbenchAction prevPartAction;
	private IWorkbenchAction nextEditorAction;
	private IWorkbenchAction prevEditorAction;
	private IWorkbenchAction nextPerspectiveAction;
	private IWorkbenchAction prevPerspectiveAction;
	private IWorkbenchAction activateEditorAction;
	private IWorkbenchAction maximizePartAction;
	private IWorkbenchAction workbenchEditorsAction;
	private IWorkbenchAction backwardHistoryAction;
	private IWorkbenchAction forwardHistoryAction;

	// generic retarget actions
	private IWorkbenchAction undoAction;
	private IWorkbenchAction redoAction;
	private IWorkbenchAction cutAction;
	private IWorkbenchAction copyAction;
	private IWorkbenchAction pasteAction;
	private IWorkbenchAction deleteAction;
	private IWorkbenchAction selectAllAction;
	private IWorkbenchAction findAction;
	private IWorkbenchAction printAction;
	private IWorkbenchAction revertAction;
	private IWorkbenchAction refreshAction;
	private IWorkbenchAction propertiesAction;
	private IWorkbenchAction moveAction;
	private IWorkbenchAction renameAction;
	private IWorkbenchAction goIntoAction;
	private IWorkbenchAction backAction;
	private IWorkbenchAction forwardAction;
	private IWorkbenchAction upAction;
	private IWorkbenchAction nextAction;
	private IWorkbenchAction previousAction;

	// IDE-specific actions
	private IWorkbenchAction projectPropertyDialogAction;
	private IWorkbenchAction newWizardAction;
	private IWorkbenchAction newWizardDropDownAction;
	private IWorkbenchAction importResourcesAction;
	private IWorkbenchAction exportResourcesAction;

	private IWorkbenchAction rebuildAllAction; // Full build
	private IWorkbenchAction buildAllAction; // Incremental build
	private IWorkbenchAction quickStartAction;
	private IWorkbenchAction tipsAndTricksAction;

	// IDE-specific retarget actions
	private IWorkbenchAction addBookmarkAction;
	private IWorkbenchAction addTaskAction;
	private IWorkbenchAction buildProjectAction;
	private IWorkbenchAction rebuildProjectAction;
	private IWorkbenchAction openProjectAction;
	private IWorkbenchAction closeProjectAction;

	private NewWizardMenu newWizardMenu;

	/**
	 * Constructs a new action builder which contributes actions
	 * to the given window configurer.
	 * 
	 * @windowConfigurer the window configurer
	 */
	public WorkbenchActionBuilder(IWorkbenchWindowConfigurer windowConfigurer) {
		super();
		this.windowConfigurer = windowConfigurer;
	}

	/**
	 * Returns the window to which this action builder is contributing.
	 */
	private IWorkbenchWindow getWindow() {
		return windowConfigurer.getWindow();
	}
	
	/**
	 * Builds the actions and contributes them to the given window.
	 */
	public void buildActions() {
		makeActions();
		fillActionBars();
		hookListeners();
	}

	/**
	 * Hooks listeners on the preference store and the window's page, perspective and selection services.
	 */
	private void hookListeners() {

		// Listen for preference property changes to
		// update the menubar and toolbar
		IPreferenceStore store =
			IDEWorkbenchPlugin.getDefault().getPreferenceStore();
		store.addPropertyChangeListener(propertyChangeListener);

		// Listen to workbench page lifecycle methods to enable
		// and disable the perspective menu items as needed.
		getWindow().addPageListener(new IPageListener() {
			public void pageActivated(IWorkbenchPage page) {
				enableActions(page.getPerspective() != null);
			}
			public void pageClosed(IWorkbenchPage page) {
				IWorkbenchPage pg = getWindow().getActivePage();
				enableActions(pg != null && pg.getPerspective() != null);
			}
			public void pageOpened(IWorkbenchPage page) {
			}
		});

		// Listen to workbench perspective lifecycle methods to enable
		// and disable the perspective menu items as needed.
		getWindow().addPageListener(new IPageListener() {
			public void pageActivated(IWorkbenchPage page) {
			}
			public void pageClosed(IWorkbenchPage page) {
				enableActions(false);
			}
			public void pageOpened(IWorkbenchPage page) {
			}
		});
		getWindow().addPerspectiveListener(new IPerspectiveListener() {
			public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
				enableActions(true);
			}
			public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
			}
		});
	}

	/**
	 * Enables the menu items dependent on an active
	 * page and perspective.
	 * Note, the show view action already does its own 
	 * listening so no need to do it here.
	 */
	private void enableActions(boolean value) {
		hideShowEditorAction.setEnabled(value);
		savePerspectiveAction.setEnabled(value);
		lockToolBarAction.setEnabled(value);
		resetPerspectiveAction.setEnabled(value);
		editActionSetAction.setEnabled(value);
		closePerspAction.setEnabled(value);
		closeAllPerspsAction.setEnabled(value);
		newWizardMenu.setEnabled(value);
		newWizardDropDownAction.setEnabled(value);
		importResourcesAction.setEnabled(value);
		exportResourcesAction.setEnabled(value);
	}
	/**
	 * Fills the menu bar and coolbar with the workbench actions.
	 */
	private void fillActionBars() {
		fillActionBars(windowConfigurer);
	}
	/**
	 * Fills the given action bars with the workbench actions.
	 */
	private void fillActionBars(IWorkbenchWindowConfigurer configurer) {
		fillMenuBar(configurer.getMenuManager());
		fillCoolBar(configurer);
		if (!ResourcesPlugin.getWorkspace().isAutoBuilding()) {
			// Only add the manual incremental build if auto build off.
			// Only update the coolbar at this point.
			addManualIncrementalBuildToolAction(configurer);
		}

		IPreferenceStore store = IDEWorkbenchPlugin.getDefault().getPreferenceStore();
		// @issue ref to internal generic workbench constant
		if (store.getBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN)) {
			addPinEditorAction(configurer);
		}
	}
	/**
	 * Fills the coolbar with the workbench actions.
	 */
	private void fillCoolBar(IWorkbenchWindowConfigurer configurer) {
		configurer.addToToolBarMenu(new ActionContributionItem(lockToolBarAction));
		configurer.addToToolBarMenu(new ActionContributionItem(editActionSetAction));

		IToolBarManager tBarMgr = configurer.addToolBar(IWorkbenchActionConstants.TOOLBAR_FILE);
		configurer.addToolbarGroup(tBarMgr, IWorkbenchActionConstants.NEW_GROUP, true);
		tBarMgr.add(newWizardDropDownAction);
		configurer.addToolbarGroup(tBarMgr, IWorkbenchActionConstants.NEW_EXT, false);
		configurer.addToolbarGroup(tBarMgr, IWorkbenchActionConstants.SAVE_GROUP, false);
		tBarMgr.add(saveAction);
		tBarMgr.add(saveAsAction);
		configurer.addToolbarGroup(tBarMgr, IWorkbenchActionConstants.SAVE_EXT, false);
		tBarMgr.add(printAction);
		configurer.addToolbarGroup(tBarMgr, IWorkbenchActionConstants.PRINT_EXT, false);
		configurer.addToolbarGroup(tBarMgr, IWorkbenchActionConstants.BUILD_GROUP, true);
		configurer.addToolbarGroup(tBarMgr, IWorkbenchActionConstants.BUILD_EXT, false);
		configurer.addToolbarGroup(tBarMgr, IWorkbenchActionConstants.MB_ADDITIONS, true);

		tBarMgr = configurer.addToolBar(IWorkbenchActionConstants.TOOLBAR_NAVIGATE);
		configurer.addToolbarGroup(tBarMgr, IWorkbenchActionConstants.HISTORY_GROUP, true);
		tBarMgr.add(backwardHistoryAction);
		tBarMgr.add(forwardHistoryAction);
		configurer.addToolbarGroup(tBarMgr, IWorkbenchActionConstants.PIN_GROUP, true);
	}
	/**
	 * Fills the menu bar with the workbench actions.
	 */
	private void fillMenuBar(IMenuManager menubar) {
		menubar.add(createFileMenu());
		menubar.add(createEditMenu());
		menubar.add(createNavigateMenu());
		menubar.add(createProjectMenu());
		menubar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		menubar.add(createWindowMenu());
		menubar.add(createHelpMenu());
	}
	/**
	 * Creates and returns the File menu.
	 */
	private MenuManager createFileMenu() {
		MenuManager menu = new MenuManager(IDEWorkbenchMessages.getString("Workbench.file"), IWorkbenchActionConstants.M_FILE); //$NON-NLS-1$
		menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_START));
		{
			this.newWizardMenu = new NewWizardMenu(getWindow());
			MenuManager newMenu = new MenuManager(IDEWorkbenchMessages.getString("Workbench.new")); //$NON-NLS-1$
			newMenu.add(this.newWizardMenu);
			menu.add(newMenu);
		}

		menu.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
		menu.add(new Separator());

		menu.add(closeAction);
		menu.add(closeAllAction);
		//		menu.add(closeAllSavedAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.CLOSE_EXT));
		menu.add(new Separator());
		menu.add(saveAction);
		menu.add(saveAsAction);
		menu.add(saveAllAction);

		menu.add(revertAction);
		menu.add(new Separator());
		menu.add(moveAction);
		menu.add(renameAction);
		menu.add(new Separator());
		menu.add(refreshAction);

		menu.add(new GroupMarker(IWorkbenchActionConstants.SAVE_EXT));
		menu.add(new Separator());
		menu.add(printAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.PRINT_EXT));
		menu.add(new Separator());
		menu.add(importResourcesAction);
		menu.add(exportResourcesAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.IMPORT_EXT));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		menu.add(new Separator());
		menu.add(propertiesAction);

		menu.add(ContributionItemFactory.REOPEN_EDITORS.create(getWindow()));
		menu.add(new GroupMarker(IWorkbenchActionConstants.MRU));
		menu.add(new Separator());
		menu.add(ActionFactory.QUIT.create(getWindow()));
		menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_END));
		return menu;
	}

	/**
	 * Creates and returns the Edit menu.
	 */
	private MenuManager createEditMenu() {
		MenuManager menu = new MenuManager(IDEWorkbenchMessages.getString("Workbench.edit"), IWorkbenchActionConstants.M_EDIT); //$NON-NLS-1$
		menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_START));

		menu.add(undoAction);
		menu.add(redoAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.UNDO_EXT));
		menu.add(new Separator());

		menu.add(cutAction);
		menu.add(copyAction);
		menu.add(pasteAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.CUT_EXT));
		menu.add(new Separator());

		menu.add(deleteAction);
		menu.add(selectAllAction);
		menu.add(new Separator());

		menu.add(findAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.FIND_EXT));
		menu.add(new Separator());

		menu.add(addBookmarkAction);
		menu.add(addTaskAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.ADD_EXT));

		menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_END));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		return menu;
	}

	/**
	 * Creates and returns the Navigate menu.
	 */
	private MenuManager createNavigateMenu() {
		MenuManager menu = new MenuManager(IDEWorkbenchMessages.getString("Workbench.navigate"), IWorkbenchActionConstants.M_NAVIGATE); //$NON-NLS-1$
		menu.add(new GroupMarker(IWorkbenchActionConstants.NAV_START));
		menu.add(goIntoAction);

		MenuManager goToSubMenu = new MenuManager(IDEWorkbenchMessages.getString("Workbench.goTo"), IWorkbenchActionConstants.GO_TO); //$NON-NLS-1$
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
		{
			MenuManager showInSubMenu = new MenuManager(IDEWorkbenchMessages.getString("Workbench.showIn")); //$NON-NLS-1$
			// @issue ref to internal generic workbench class - should be on contributionitemfactory
			showInSubMenu.add(new ShowInMenu(getWindow()));
			menu.add(showInSubMenu);
		}
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
		boolean autoBuild = ResourcesPlugin.getWorkspace().isAutoBuilding();
		MenuManager menu = new MenuManager(IDEWorkbenchMessages.getString("Workbench.project"), IWorkbenchActionConstants.M_PROJECT); //$NON-NLS-1$
		menu.add(new Separator(IWorkbenchActionConstants.PROJ_START));

		menu.add(openProjectAction);
		menu.add(closeProjectAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.OPEN_EXT));
		menu.add(new Separator());

		// Only add the manual incremental build if auto build off
		if (!autoBuild)
			menu.add(buildProjectAction);
		menu.add(rebuildProjectAction);
		if (!autoBuild) {
			menu.add(buildAllAction);
		}
		menu.add(rebuildAllAction);
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
		MenuManager menu = new MenuManager(IDEWorkbenchMessages.getString("Workbench.window"), IWorkbenchActionConstants.M_WINDOW); //$NON-NLS-1$

		IWorkbenchAction action = ActionFactory.OPEN_NEW_WINDOW.create(getWindow());
		action.setText(IDEWorkbenchMessages.getString("Workbench.openNewWindow")); //$NON-NLS-1$
		menu.add(action);
		menu.add(new Separator());
		addPerspectiveActions(menu);
		menu.add(new Separator());
		addKeyboardShortcuts(menu);
		menu.add(new Separator());
		menu.add(workbenchEditorsAction);
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "end")); //$NON-NLS-1$
		menu.add(openPreferencesAction);
		menu.add(ContributionItemFactory.OPEN_WINDOWS.create(getWindow()));
		return menu;
	}

	/**
	 * Adds the perspective actions to the specified menu.
	 */
	private void addPerspectiveActions(MenuManager menu) {
		{
			String openText = IDEWorkbenchMessages.getString("Workbench.openPerspective"); //$NON-NLS-1$
			MenuManager changePerspMenuMgr = new MenuManager(openText);
			IContributionItem changePerspMenuItem = 
				ContributionItemFactory.PERSPECTIVES_SHORTLIST.create(getWindow());
			changePerspMenuMgr.add(changePerspMenuItem);
			menu.add(changePerspMenuMgr);
		}
		{
			MenuManager showViewMenuMgr = new MenuManager(IDEWorkbenchMessages.getString("Workbench.showView")); //$NON-NLS-1$
			IContributionItem showViewMenu = ContributionItemFactory.VIEWS_SHORTLIST.create(getWindow());
			showViewMenuMgr.add(showViewMenu);
			menu.add(showViewMenuMgr);
		}
		menu.add(hideShowEditorAction);
		menu.add(lockToolBarAction);
		menu.add(new Separator());
		menu.add(editActionSetAction);
		menu.add(savePerspectiveAction);
		menu.add(resetPerspectiveAction);
		menu.add(closePerspAction);
		menu.add(closeAllPerspsAction);
	}

	/**
	 * Adds the keyboard navigation submenu to the specified menu.
	 */
	private void addKeyboardShortcuts(MenuManager menu) {
		MenuManager subMenu = new MenuManager(IDEWorkbenchMessages.getString("Workbench.shortcuts")); //$NON-NLS-1$
		menu.add(subMenu);
		subMenu.add(showPartPaneMenuAction);
		subMenu.add(showViewMenuAction);
		subMenu.add(new Separator());
		subMenu.add(maximizePartAction);
		subMenu.add(new Separator());
		subMenu.add(activateEditorAction);
		subMenu.add(nextEditorAction);
		subMenu.add(prevEditorAction);
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
		MenuManager menu = new MenuManager(IDEWorkbenchMessages.getString("Workbench.help"), IWorkbenchActionConstants.M_HELP); //$NON-NLS-1$
		// See if a welcome page is specified
		if (quickStartAction != null)
			menu.add(quickStartAction);
		// See if a tips and tricks page is specified
		if (tipsAndTricksAction != null)
			menu.add(tipsAndTricksAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_START));
		menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_END));
		menu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		// about should always be at the bottom
		menu.add(new Separator());
		menu.add(aboutAction);
		return menu;
	}

	/**
	 * Disposes any resources and unhooks any listeners that are no longer needed.
	 * Called when the window is closed.
	 */
	public void dispose() {
		// Listen for preference property changes to
		// update the menubar and toolbar
		IPreferenceStore store =
			IDEWorkbenchPlugin.getDefault().getPreferenceStore();
		store.removePropertyChangeListener(propertyChangeListener);
	}

	/**
	 * Returns true if the menu with the given ID should
	 * be considered as an OLE container menu. Container menus
	 * are preserved in OLE menu merging.
	 */
	public boolean isContainerMenu(String menuId) {
		if (menuId.equals(IWorkbenchActionConstants.M_FILE))
			return true;
		if (menuId.equals(IWorkbenchActionConstants.M_VIEW))
			return true;
		if (menuId.equals(IWorkbenchActionConstants.M_WORKBENCH))
			return true;
		if (menuId.equals(IWorkbenchActionConstants.M_WINDOW))
			return true;
		return false;
	}
	/**
	 * Return whether or not given id matches the id of the coolitems that
	 * the workbench creates.
	 */
	public boolean isWorkbenchCoolItemId(String id) {
		if (IWorkbenchActionConstants.TOOLBAR_FILE.equalsIgnoreCase(id)) return true;
		if (IWorkbenchActionConstants.TOOLBAR_NAVIGATE.equalsIgnoreCase(id)) return true;
		return false;
	}

	/**
	 * Create actions for the menu bar and toolbar
	 */
	private void makeActions() {

		// The actions in jface do not have menu vs. enable, vs. disable vs. color
		// There are actions in here being passed the workbench - problem 
		newWizardAction = IDEActionFactory.NEW.create(getWindow());
		registerGlobalAction(newWizardAction);

		newWizardDropDownAction = IDEActionFactory.NEW_WIZARD_DROP_DOWN.create(getWindow());

		importResourcesAction = IDEActionFactory.IMPORT.create(getWindow());

		exportResourcesAction = IDEActionFactory.EXPORT.create(getWindow());

		rebuildAllAction = IDEActionFactory.REBUILD_ALL.create(getWindow());
		registerGlobalAction(rebuildAllAction);

		buildAllAction = IDEActionFactory.BUILD.create(getWindow());
		registerGlobalAction(buildAllAction);

		saveAction = ActionFactory.SAVE.create(getWindow());
		registerGlobalAction(saveAction);

		saveAsAction = ActionFactory.SAVE_AS.create(getWindow());

		saveAllAction = ActionFactory.SAVE_ALL.create(getWindow());
		registerGlobalAction(saveAllAction);
		
		undoAction = ActionFactory.UNDO.create(getWindow());
		registerGlobalAction(undoAction);

		redoAction = ActionFactory.REDO.create(getWindow());
		registerGlobalAction(redoAction);

		cutAction = ActionFactory.CUT.create(getWindow());
		registerGlobalAction(cutAction);

		copyAction = ActionFactory.COPY.create(getWindow());
		registerGlobalAction(redoAction);

		pasteAction = ActionFactory.PASTE.create(getWindow());
		registerGlobalAction(pasteAction);

		printAction = ActionFactory.PRINT.create(getWindow());
		registerGlobalAction(printAction);

		selectAllAction = ActionFactory.SELECT_ALL.create(getWindow());
		registerGlobalAction(selectAllAction);
		
		findAction = ActionFactory.FIND.create(getWindow());
		registerGlobalAction(findAction);

		closeAction = ActionFactory.CLOSE.create(getWindow());
		registerGlobalAction(closeAction);

		closeAllAction = ActionFactory.CLOSE_ALL.create(getWindow());
		registerGlobalAction(closeAllAction);

		closeAllSavedAction = ActionFactory.CLOSE_ALL_SAVED.create(getWindow());
		registerGlobalAction(closeAllSavedAction);

		pinEditorAction = ActionFactory.PIN_EDITOR.create(getWindow());

		try {
			aboutAction = IDEActionFactory.ABOUT.create(getWindow());
			AboutInfo aboutInfo = windowConfigurer.getWorkbenchConfigurer().getPrimaryFeatureAboutInfo();
			String productName = aboutInfo.getProductName();
			if (productName == null) {
				productName = ""; //$NON-NLS-1$
			}
			aboutAction.setText(IDEWorkbenchMessages.format("AboutAction.text", new Object[] { productName })); //$NON-NLS-1$
			aboutAction.setToolTipText(IDEWorkbenchMessages.format("AboutAction.toolTip", new Object[] { productName})); //$NON-NLS-1$
			aboutAction.setImageDescriptor(
				IDEInternalWorkbenchImages.getImageDescriptor(
					IDEInternalWorkbenchImages.IMG_OBJS_DEFAULT_PROD));
			registerGlobalAction(aboutAction);
		} catch (WorkbenchException e) {
			// do nothing
		}

		openPreferencesAction = ActionFactory.PREFERENCES.create(getWindow());

		addBookmarkAction = IDEActionFactory.BOOKMARK.create(getWindow());
		registerGlobalAction(addBookmarkAction);

		addTaskAction = IDEActionFactory.ADD_TASK.create(getWindow());
		registerGlobalAction(addTaskAction);

		deleteAction = ActionFactory.DELETE.create(getWindow());
		// don't register the delete action with the key binding service.
		// doing so would break cell editors that listen for keyPressed SWT 
		// events.
		// registerGlobalAction(deleteAction);

		try {
			AboutInfo[] infos = windowConfigurer.getWorkbenchConfigurer().getAllFeaturesAboutInfo();
			// See if a welcome page is specified
			for (int i = 0; i < infos.length; i++) {
				if (infos[i].getWelcomePageURL() != null) {
					quickStartAction = IDEActionFactory.QUICK_START.create(getWindow());
					registerGlobalAction(quickStartAction);
					break;
				}
			}
			// See if a tips and tricks page is specified
			for (int i = 0; i < infos.length; i++) {
				if (infos[i].getTipsAndTricksHref() != null) {
					tipsAndTricksAction = IDEActionFactory.TIPS_AND_TRICKS.create(getWindow());
					registerGlobalAction(tipsAndTricksAction);
					break;
				}
			}
			
		} catch (WorkbenchException e) {
			IDEWorkbenchPlugin.log("Failed to read about info for all installed features.", e.getStatus()); //$NON-NLS-1$
		}

		// Actions for invisible accelerators
		showViewMenuAction = ActionFactory.SHOW_VIEW_MENU.create(getWindow());
		registerGlobalAction(showViewMenuAction);

		showPartPaneMenuAction = ActionFactory.SHOW_PART_PANE_MENU.create(getWindow());
		registerGlobalAction(showPartPaneMenuAction);

		nextEditorAction = ActionFactory.NEXT_EDITOR.create(getWindow());
		prevEditorAction = ActionFactory.PREVIOUS_EDITOR.create(getWindow());
		ActionFactory.linkCycleActionPair(nextEditorAction, prevEditorAction);
		registerGlobalAction(nextEditorAction);
		registerGlobalAction(prevEditorAction);

		nextPartAction = ActionFactory.NEXT_PART.create(getWindow());
		prevPartAction = ActionFactory.PREVIOUS_PART.create(getWindow());
		ActionFactory.linkCycleActionPair(nextPartAction, prevPartAction);
		registerGlobalAction(nextPartAction);
		registerGlobalAction(prevPartAction);

		nextPerspectiveAction = ActionFactory.NEXT_PERSPECTIVE.create(getWindow());
		prevPerspectiveAction = ActionFactory.PREVIOUS_PERSPECTIVE.create(getWindow());
		ActionFactory.linkCycleActionPair(nextPerspectiveAction, prevPerspectiveAction);
		registerGlobalAction(nextPerspectiveAction);
		registerGlobalAction(prevPerspectiveAction);

		activateEditorAction = ActionFactory.ACTIVATE_EDITOR.create(getWindow());
		registerGlobalAction(activateEditorAction);

		maximizePartAction = ActionFactory.MAXIMIZE.create(getWindow());
		registerGlobalAction(maximizePartAction);
		
		workbenchEditorsAction = ActionFactory.SHOW_OPEN_EDITORS.create(getWindow());
		registerGlobalAction(workbenchEditorsAction);

		hideShowEditorAction = ActionFactory.SHOW_EDITOR.create(getWindow());
		savePerspectiveAction = ActionFactory.SAVE_PERSPECTIVE.create(getWindow());
		editActionSetAction = ActionFactory.EDIT_ACTION_SETS.create(getWindow());
		lockToolBarAction = ActionFactory.LOCK_TOOL_BAR.create(getWindow());
		resetPerspectiveAction = ActionFactory.RESET_PERSPECTIVE.create(getWindow());
		closePerspAction = ActionFactory.CLOSE_PERSPECTIVE.create(getWindow());
		closeAllPerspsAction = ActionFactory.CLOSE_ALL_PERSPECTIVES.create(getWindow());

		forwardHistoryAction = ActionFactory.FORWARD_HISTORY.create(getWindow());
		registerGlobalAction(forwardHistoryAction);

		backwardHistoryAction = ActionFactory.BACKWARD_HISTORY.create(getWindow());
		registerGlobalAction(backwardHistoryAction);

		revertAction = ActionFactory.REVERT.create(getWindow());
		registerGlobalAction(revertAction);

		refreshAction = ActionFactory.REFRESH.create(getWindow());
		registerGlobalAction(refreshAction);

		propertiesAction = ActionFactory.PROPERTIES.create(getWindow());
		registerGlobalAction(propertiesAction);

		moveAction = ActionFactory.MOVE.create(getWindow());
		registerGlobalAction(moveAction);

		renameAction = ActionFactory.RENAME.create(getWindow());
		registerGlobalAction(renameAction);

		goIntoAction = ActionFactory.GO_INTO.create(getWindow());
		registerGlobalAction(goIntoAction);

		backAction = ActionFactory.BACK.create(getWindow());
		registerGlobalAction(backAction);

		forwardAction = ActionFactory.FORWARD.create(getWindow());
		registerGlobalAction(forwardAction);

		upAction = ActionFactory.UP.create(getWindow());
		registerGlobalAction(upAction);

		nextAction = ActionFactory.NEXT.create(getWindow());
		nextAction.setImageDescriptor(
			IDEInternalWorkbenchImages.getImageDescriptor(
				IDEInternalWorkbenchImages.IMG_CTOOL_NEXT_NAV));
		registerGlobalAction(nextAction);

		previousAction = ActionFactory.PREVIOUS.create(getWindow());
		previousAction.setImageDescriptor(
			IDEInternalWorkbenchImages.getImageDescriptor(
				IDEInternalWorkbenchImages.IMG_CTOOL_PREVIOUS_NAV));
		registerGlobalAction(previousAction);

				
		buildProjectAction = IDEActionFactory.BUILD_PROJECT.create(getWindow());
		registerGlobalAction(buildProjectAction);

		rebuildProjectAction = IDEActionFactory.REBUILD_PROJECT.create(getWindow());
		registerGlobalAction(rebuildProjectAction);

		openProjectAction = IDEActionFactory.OPEN_PROJECT.create(getWindow());
		registerGlobalAction(openProjectAction);

		closeProjectAction = IDEActionFactory.CLOSE_PROJECT.create(getWindow());
		registerGlobalAction(closeProjectAction);
		
		projectPropertyDialogAction = IDEActionFactory.OPEN_PROJECT_PROPERTIES.create(getWindow());
		registerGlobalAction(projectPropertyDialogAction);
	}

	/**
	 * Updates the menubar and toolbar when changes are made to the preferences.
	 */
	private void handlePropertyChange(PropertyChangeEvent event) {
		IPreferenceStore store =
			IDEWorkbenchPlugin.getDefault().getPreferenceStore();
		// @issue ref to internal generic workbench constant
		if (event.getProperty().equals(IPreferenceConstants.REUSE_EDITORS_BOOLEAN)) {
			if (store.getBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN))
				addPinEditorAction(windowConfigurer);
			else
				removePinEditorAction(windowConfigurer);
		} else if (event.getProperty().equals(IPreferenceConstants.REUSE_EDITORS)) {
			// @issue ref to internal generic workbench constant
			// @issue idiosyncratic semantics of pinEditor
//			pinEditorAction.updateState();
		} else if (event.getProperty().equals(IPreferenceConstants.RECENT_FILES)) {
			// @issue ref to internal generic workbench constant
			// @issue this should be moved to the contribution item (open recent editor)
			Workbench wb = (Workbench) (Workbench) getWindow().getWorkbench();
			int newValue = store.getInt(IPreferenceConstants.RECENT_FILES);
			wb.getEditorHistory().reset(newValue);
			if (newValue == 0) {
				// the open recent menu item can go from enabled to disabled
				windowConfigurer.getMenuManager().updateAll(false);
			}
		}
	}
	/**
	 * Adds the pin action to the toolbar.  Add it to the navigate toolbar.
	 */
	private void addPinEditorAction(IWorkbenchWindowConfigurer configurer) {
		IToolBarManager tBarMgr = configurer.getToolBar(IWorkbenchActionConstants.TOOLBAR_NAVIGATE);
		if (tBarMgr == null) {
			// This tool bar should exist. Bail out!
			IDEWorkbenchPlugin.log("Navigate toolbar is missing"); //$NON-NLS-1$
		} else {
			tBarMgr.appendToGroup(IWorkbenchActionConstants.PIN_GROUP, pinEditorAction);
			tBarMgr.update(true);
		}
	}
	
	/**
	 * Removes the pin action from the toolbar.
	 */
	private void removePinEditorAction(IWorkbenchWindowConfigurer configurer) {
		// Flag the action so it is hidden in the editor menu.
		// @issue idiosyncratic semantics of pinEditor
//		pinEditorAction.setVisible(false);
		
		IToolBarManager tBarMgr = configurer.getToolBar(IWorkbenchActionConstants.TOOLBAR_NAVIGATE);
		if (tBarMgr == null) {
			// This tool bar should exist. Bail out!
			IDEWorkbenchPlugin.log("Navigate toolbar is missing"); //$NON-NLS-1$
		} else {
			try {
				tBarMgr.remove(pinEditorAction.getId());
				tBarMgr.update(true);
			} catch (IllegalArgumentException e) {
				// Action was not in tool bar
			}
		}
	}

	/**
	 * Add the manual incremental build action
	 * to both the menu bar and the tool bar.
	 */
	public void addManualIncrementalBuildAction() {
		IMenuManager menubar = windowConfigurer.getMenuManager();
		IMenuManager manager =
			menubar.findMenuUsingPath(IWorkbenchActionConstants.M_PROJECT);
		if (manager != null) {
			try {
				manager.insertBefore(
					IDEActionFactory.REBUILD_PROJECT.getId(),
					buildProjectAction);
				manager.insertBefore(
					IDEActionFactory.REBUILD_ALL.getId(),
					buildAllAction);
			} catch (IllegalArgumentException e) {
				// action not found!
			}
		}
		addManualIncrementalBuildToolAction(windowConfigurer);
	}
	private void addManualIncrementalBuildToolAction(IWorkbenchWindowConfigurer configurer) {
		IToolBarManager tBarMgr = configurer.getToolBar(IWorkbenchActionConstants.TOOLBAR_FILE);
		if (tBarMgr == null) {
			// This tool bar should exist. Bail out!
			IDEWorkbenchPlugin.log("File toolbar is missing"); //$NON-NLS-1$
		} else {
			tBarMgr.appendToGroup(IWorkbenchActionConstants.BUILD_GROUP, buildAllAction);
			tBarMgr.update(true);
		}
	}

	/**
	 * Remove the manual incremental build action
	 * from both the menu bar and the tool bar.
	 */
	public void removeManualIncrementalBuildAction() {
		IMenuManager menubar = windowConfigurer.getMenuManager();
		IMenuManager manager =
			menubar.findMenuUsingPath(IWorkbenchActionConstants.M_PROJECT);
		if (manager != null) {
			try {
				manager.remove(IDEActionFactory.BUILD.getId());
				manager.remove(IDEActionFactory.BUILD_PROJECT.getId());
			} catch (IllegalArgumentException e) {
				// action was not in menu
			}
		}
		removeManualIncrementalBuildToolAction(windowConfigurer);
	}
	private void removeManualIncrementalBuildToolAction(IWorkbenchWindowConfigurer configurer) {
		IToolBarManager tBarMgr = configurer.getToolBar(IWorkbenchActionConstants.TOOLBAR_FILE);
		if (tBarMgr == null) {
			// This tool bar should exist. Bail out!
			IDEWorkbenchPlugin.log("File toolbar is missing"); //$NON-NLS-1$
		} else {
			try {
				tBarMgr.remove(IDEActionFactory.BUILD.getId());
				tBarMgr.update(true);
			} catch (IllegalArgumentException e) {
				// Action was not in tool bar
			}
		}
	}
	
	private void registerGlobalAction(IAction action) {
		windowConfigurer.registerGlobalAction(action);
	}
}

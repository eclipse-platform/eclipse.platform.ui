/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ExportResourcesAction;
import org.eclipse.ui.actions.GlobalBuildAction;
import org.eclipse.ui.actions.ImportResourcesAction;
import org.eclipse.ui.actions.LabelRetargetAction;
import org.eclipse.ui.actions.NewWizardAction;
import org.eclipse.ui.actions.NewWizardMenu;
import org.eclipse.ui.actions.OpenInNewWindowAction;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Adds actions to a workbench window.
 */
public class WorkbenchActionBuilder {

	private static final String saveActionDefId = "org.eclipse.ui.file.save"; //$NON-NLS-1$
	private static final String saveAllActionDefId = "org.eclipse.ui.file.saveAll"; //$NON-NLS-1$
	private static final String closeActionDefId = "org.eclipse.ui.file.close"; //$NON-NLS-1$
	private static final String closeAllActionDefId = "org.eclipse.ui.file.closeAll"; //$NON-NLS-1$
	private static final String closeAllSavedActionDefId = "org.eclipse.ui.file.closeAllSaved"; //$NON-NLS-1$
	private static final String deleteActionDefId = "org.eclipse.ui.edit.delete"; //$NON-NLS-1$
	private static final String showViewMenuActionDefId = "org.eclipse.ui.window.showViewMenu"; //$NON-NLS-1$
	private static final String showPartPaneMenuActionDefId = "org.eclipse.ui.window.showSystemMenu"; //$NON-NLS-1$
	private static final String nextEditorActionDefId = "org.eclipse.ui.window.nextEditor"; //$NON-NLS-1$
	private static final String prevEditorActionDefId = "org.eclipse.ui.window.previousEditor"; //$NON-NLS-1$
	private static final String nextPartActionDefId = "org.eclipse.ui.window.nextView"; //$NON-NLS-1$
	private static final String prevPartActionDefId = "org.eclipse.ui.window.previousView"; //$NON-NLS-1$
	private static final String nextPerspectiveActionDefId = "org.eclipse.ui.window.nextPerspective"; //$NON-NLS-1$
	private static final String prevPerspectiveActionDefId = "org.eclipse.ui.window.previousPerspective"; //$NON-NLS-1$
	private static final String activateEditorActionDefId = "org.eclipse.ui.window.activateEditor"; //$NON-NLS-1$
	private static final String workbenchEditorsActionDefId = "org.eclipse.ui.window.switchToEditor"; //$NON-NLS-1$
	private static final String buildAllActionDefId = "org.eclipse.ui.project.buildAll"; //$NON-NLS-1$
	private static final String rebuildAllActionDefId = "org.eclipse.ui.project.rebuildAll"; //$NON-NLS-1$
	private static final String backwardHistoryActionDefId = "org.eclipse.ui.navigate.backwardHistory"; //$NON-NLS-1$
	private static final String forwardHistoryActionDefId = "org.eclipse.ui.navigate.forwardHistory"; //$NON-NLS-1$
	private static final String showInActionDefId = "org.eclipse.ui.navigate.showIn"; //$NON-NLS-1$

	private static final String workbenchToolGroupId = "org.eclipse.ui.internal"; //$NON-NLS-1$

	//pin editor group in the toolbar
	private static final String pinEditorGroup = "pinEditorGroup"; //$NON-NLS-1$

	//history group in the toolbar
	private static final String historyGroup = "historyGroup"; //$NON-NLS-1$

	/**
	 * The window to which this is contributing.
	 */
	private final WorkbenchWindow window;

	private final IPropertyChangeListener propertyChangeListener =
		new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			handlePropertyChange(event);
		}
	};

	// actions
	private NewWizardAction newWizardAction;
	private NewWizardDropDownAction newWizardDropDownAction;
	private NewWizardMenu newWizardMenu;
	private CloseEditorAction closeAction;
	private CloseAllAction closeAllAction;
	private CloseAllSavedAction closeAllSavedAction;
	private ImportResourcesAction importResourcesAction;
	private ExportResourcesAction exportResourcesAction;
	private GlobalBuildAction rebuildAllAction; // Full build
	private GlobalBuildAction buildAllAction; // Incremental build
	private SaveAction saveAction;
	private SaveAllAction saveAllAction;
	private AboutAction aboutAction;
	private OpenPreferencesAction openPreferencesAction;
	private QuickStartAction quickStartAction;
	private SaveAsAction saveAsAction;
	private ToggleEditorsVisibilityAction hideShowEditorAction;
	private SavePerspectiveAction savePerspectiveAction;
	private ResetPerspectiveAction resetPerspectiveAction;
	private EditActionSetsAction editActionSetAction;
	private ClosePerspectiveAction closePerspAction;
	private LockToolBarAction lockToolBarAction;
	private CloseAllPerspectivesAction closeAllPerspsAction;
	private PinEditorAction pinEditorAction;

	private ShowViewMenuAction showViewMenuAction;
	private ShowPartPaneMenuAction showPartPaneMenuAction;
	private CyclePartAction nextPartAction;
	private CyclePartAction prevPartAction;
	private CycleEditorAction nextEditorAction;
	private CycleEditorAction prevEditorAction;
	private CyclePerspectiveAction nextPerspectiveAction;
	private CyclePerspectiveAction prevPerspectiveAction;
	private ActivateEditorAction activateEditorAction;

	private WorkbenchEditorsAction workbenchEditorsAction;
	private WorkbenchShowInAction showInAction;

	// retarget actions.
	private RetargetAction undoAction;
	private RetargetAction redoAction;
	private RetargetAction cutAction;
	private RetargetAction copyAction;
	private RetargetAction pasteAction;
	private RetargetAction deleteAction;
	private RetargetAction selectAllAction;
	private RetargetAction findAction;
	private RetargetAction addBookmarkAction;
	private RetargetAction addTaskAction;
	private RetargetAction printAction;

	private RetargetAction revertAction;
	private RetargetAction refreshAction;
	private RetargetAction propertiesAction;
	private RetargetAction moveAction;
	private RetargetAction renameAction;
	private RetargetAction goIntoAction;
	private RetargetAction backAction;
	private RetargetAction forwardAction;
	private RetargetAction upAction;
	private RetargetAction nextAction;
	private RetargetAction previousAction;

	private RetargetAction buildProjectAction;
	private RetargetAction rebuildProjectAction;
	private RetargetAction openProjectAction;
	private RetargetAction closeProjectAction;

	private NavigationHistoryAction backwardHistoryAction;
	private NavigationHistoryAction forwardHistoryAction;

	/**
	 * Constructs a new action builder which contributes actions
	 * to the given window.
	 * 
	 * @window the window
	 */
	public WorkbenchActionBuilder(IWorkbenchWindow window) {
		this.window = (WorkbenchWindow) window;
	}

	/**
	 * Returns the window to which this action builder is contributing.
	 */
	protected WorkbenchWindow getWindow() {
		return window;
	}

	/**
	 * Builds the actions and contributes them to the given window.
	 */
	public void buildActions() {
		makeActions();
		fillMenuBar();
		fillToolBar();
		fillShortcutBar();
		hookListeners();
	}

	/**
	 * Hooks listeners on the preference store and the window's page, perspective and selection services.
	 */
	private void hookListeners() {

		// Listen for preference property changes to
		// update the menubar and toolbar
		IPreferenceStore store =
			WorkbenchPlugin.getDefault().getPreferenceStore();
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
		getWindow()
			.getPerspectiveService()
			.addPerspectiveListener(new IInternalPerspectiveListener() {
			public void perspectiveClosed(
				IWorkbenchPage page,
				IPerspectiveDescriptor perspective) {
				enableActions(page.getPerspective() != null);
			}
			public void perspectiveOpened(
				IWorkbenchPage page,
				IPerspectiveDescriptor perspective) {
			}
			public void perspectiveActivated(
				IWorkbenchPage page,
				IPerspectiveDescriptor perspective) {
				enableActions(true);
			}
			public void perspectiveChanged(
				IWorkbenchPage page,
				IPerspectiveDescriptor perspective,
				String changeId) {
			}
		});

		//Listen for the selection changing and update the
		//actions that are interested
		getWindow()
			.getSelectionService()
			.addSelectionListener(new ISelectionListener() {
			public void selectionChanged(
				IWorkbenchPart part,
				ISelection selection) {
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection structured =
						(IStructuredSelection) selection;
					importResourcesAction.selectionChanged(structured);
					exportResourcesAction.selectionChanged(structured);
				}
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
	 * Fills the menu bar with the workbench actions.
	 */
	private void fillMenuBar() {
		MenuManager menubar = getWindow().getMenuBarManager();
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
		MenuManager menu = new MenuManager(WorkbenchMessages.getString("Workbench.file"), IWorkbenchActionConstants.M_FILE); //$NON-NLS-1$
		menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_START));
		{
			this.newWizardMenu = new NewWizardMenu(window);
			MenuManager newMenu = new MenuManager(WorkbenchMessages.getString("Workbench.new")); //$NON-NLS-1$
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
		menu.add(new Separator());
		menu.add(importResourcesAction);
		menu.add(exportResourcesAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.IMPORT_EXT));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		menu.add(new Separator());
		menu.add(propertiesAction);

		menu.add(
			new ReopenEditorMenu(
				window,
				((Workbench) getWindow().getWorkbench()).getEditorHistory(),
				true));
		menu.add(new GroupMarker(IWorkbenchActionConstants.MRU));
		menu.add(new Separator());
		menu.add(new QuitAction(getWindow().getWorkbench()));
		menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_END));
		return menu;
	}

	/**
	 * Creates and returns the Edit menu.
	 */
	private MenuManager createEditMenu() {
		MenuManager menu = new MenuManager(WorkbenchMessages.getString("Workbench.edit"), IWorkbenchActionConstants.M_EDIT); //$NON-NLS-1$
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
		MenuManager menu = new MenuManager(WorkbenchMessages.getString("Workbench.navigate"), IWorkbenchActionConstants.M_NAVIGATE); //$NON-NLS-1$
		menu.add(new GroupMarker(IWorkbenchActionConstants.NAV_START));
		menu.add(goIntoAction);

		MenuManager goToSubMenu = new MenuManager(WorkbenchMessages.getString("Workbench.goTo"), IWorkbenchActionConstants.GO_TO); //$NON-NLS-1$
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
		menu.add(showInAction);
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
		MenuManager menu = new MenuManager(WorkbenchMessages.getString("Workbench.project"), IWorkbenchActionConstants.M_PROJECT); //$NON-NLS-1$
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
		return menu;
	}

	/**
	 * Creates and returns the Window menu.
	 */
	private MenuManager createWindowMenu() {
		MenuManager menu = new MenuManager(WorkbenchMessages.getString("Workbench.window"), IWorkbenchActionConstants.M_WINDOW); //$NON-NLS-1$

		OpenInNewWindowAction action = new OpenInNewWindowAction(window);
		action.setText(WorkbenchMessages.getString("Workbench.openNewWindow")); //$NON-NLS-1$
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
		menu.add(new SwitchToWindowMenu(window, true));
		return menu;
	}

	/**
	 * Adds the perspective actions to the specified menu.
	 */
	private void addPerspectiveActions(MenuManager menu) {
		{
			String openText = WorkbenchMessages.getString("Workbench.openPerspective"); //$NON-NLS-1$
			MenuManager changePerspMenuMgr = new MenuManager(openText); //$NON-NLS-1$
			ChangeToPerspectiveMenu changePerspMenuItem =
				new ChangeToPerspectiveMenu(window);
			changePerspMenuMgr.add(changePerspMenuItem);
			menu.add(changePerspMenuMgr);
		}
		{
			MenuManager showViewMenuMgr = new MenuManager(WorkbenchMessages.getString("Workbench.showView")); //$NON-NLS-1$
			ShowViewMenu showViewMenu = new ShowViewMenu(window);
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
		MenuManager subMenu = new MenuManager(WorkbenchMessages.getString("Workbench.shortcuts")); //$NON-NLS-1$
		menu.add(subMenu);
		subMenu.add(showPartPaneMenuAction);
		subMenu.add(showViewMenuAction);
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
		MenuManager menu = new MenuManager(WorkbenchMessages.getString("Workbench.help"), IWorkbenchActionConstants.M_HELP); //$NON-NLS-1$
		// See if a welcome page is specified
		if (quickStartAction != null)
			menu.add(quickStartAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_START));
		menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_END));
		menu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		// about should always be at the bottom
		menu.add(new Separator());
		menu.add(aboutAction);
		return menu;
	}

	/**
	 * Fills the tool bar by merging all the individual viewers' contributions
	 * and invariant (static) menus and menu items, as defined in MenuConstants interface.
	 */
	private void fillToolBar() {
		// Create a CoolBar item for the workbench
		CoolBarManager cBarMgr = getWindow().getCoolBarManager();
		CoolBarContributionItem coolBarItem = new CoolBarContributionItem(cBarMgr, workbenchToolGroupId); //$NON-NLS-1$
		cBarMgr.add(coolBarItem);
		coolBarItem.setVisible(true);
		IContributionManager toolsManager =
			(IContributionManager) coolBarItem.getToolBarManager();
		cBarMgr.addToMenu(new ActionContributionItem(lockToolBarAction));
		cBarMgr.addToMenu(new ActionContributionItem(editActionSetAction));

		toolsManager.add(newWizardDropDownAction);
		toolsManager.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
		toolsManager.add(new Separator());
		toolsManager.add(saveAction);
		toolsManager.add(saveAsAction);
		toolsManager.add(new GroupMarker(IWorkbenchActionConstants.SAVE_EXT));
		toolsManager.add(printAction);
		toolsManager.add(new GroupMarker(IWorkbenchActionConstants.BUILD_EXT));
		toolsManager.prependToGroup(
			IWorkbenchActionConstants.BUILD_EXT,
			new Separator());
		toolsManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		// Only add the manual incremental build if auto build off
		if (!ResourcesPlugin.getWorkspace().isAutoBuilding()) {
			toolsManager.appendToGroup(
				IWorkbenchActionConstants.BUILD_EXT,
				buildAllAction);
		}
		addHistoryActions();
		IPreferenceStore store =
			WorkbenchPlugin.getDefault().getPreferenceStore();
		if (store.getBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN)) {
			addPinEditorAction();
		}
	}

	/**
	 * Fills the shortcut bar
	 */
	private void fillShortcutBar() {
		ToolBarManager shortcutBar = getWindow().getShortcutBar();
		shortcutBar.add(new PerspectiveContributionItem(window));
		shortcutBar.add(new Separator(WorkbenchWindow.GRP_PAGES));
		shortcutBar.add(new Separator(WorkbenchWindow.GRP_PERSPECTIVES));
		shortcutBar.add(new Separator(WorkbenchWindow.GRP_FAST_VIEWS));
		shortcutBar.add(new ShowFastViewContribution(window));
	}

	/**
	 * Disposes any resources and unhooks any listeners that are no longer needed.
	 * Called when the window is closed.
	 */
	public void dispose() {
		// Listen for preference property changes to
		// update the menubar and toolbar
		IPreferenceStore store =
			WorkbenchPlugin.getDefault().getPreferenceStore();
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
	 * Create actions for the menu bar and toolbar
	 */
	private void makeActions() {

		// The actions in jface do not have menu vs. enable, vs. disable vs. color
		// There are actions in here being passed the workbench - problem 

		// Get services for notification.
		IPartService partService = getWindow().getPartService();
		WWinKeyBindingService keyBindingService =
			getWindow().getKeyBindingService();

		// Many actions need the workbench.
		IWorkbench workbench = getWindow().getWorkbench();

		newWizardAction = new NewWizardAction();
		// images for this action are set in its constructor
		keyBindingService.registerGlobalAction(newWizardAction);

		newWizardDropDownAction =
			new NewWizardDropDownAction(workbench, newWizardAction);
		newWizardDropDownAction.setImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_NEW_WIZ));
		newWizardDropDownAction.setHoverImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_NEW_WIZ_HOVER));
		newWizardDropDownAction.setDisabledImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_NEW_WIZ_DISABLED));

		importResourcesAction = new ImportResourcesAction(workbench);
		importResourcesAction.setImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_IMPORT_WIZ));
		importResourcesAction.setHoverImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_IMPORT_WIZ_HOVER));
		importResourcesAction.setDisabledImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_IMPORT_WIZ_DISABLED));

		exportResourcesAction = new ExportResourcesAction(workbench, WorkbenchMessages.getString("ExportResourcesAction.fileMenuText")); //$NON-NLS-1$
		exportResourcesAction.setImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_EXPORT_WIZ));
		exportResourcesAction.setHoverImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_EXPORT_WIZ_HOVER));
		exportResourcesAction.setDisabledImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_EXPORT_WIZ_DISABLED));

		rebuildAllAction =
			new GlobalBuildAction(window, IncrementalProjectBuilder.FULL_BUILD);
		rebuildAllAction.setActionDefinitionId(rebuildAllActionDefId);
		keyBindingService.registerGlobalAction(rebuildAllAction);

		buildAllAction =
			new GlobalBuildAction(
				window,
				IncrementalProjectBuilder.INCREMENTAL_BUILD);
		buildAllAction.setImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_BUILD_EXEC));
		buildAllAction.setHoverImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_BUILD_EXEC_HOVER));
		buildAllAction.setDisabledImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_BUILD_EXEC_DISABLED));
		buildAllAction.setActionDefinitionId(buildAllActionDefId);
		keyBindingService.registerGlobalAction(buildAllAction);

		saveAction = new SaveAction(window);
		saveAction.setImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_SAVE_EDIT));
		saveAction.setHoverImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_SAVE_EDIT_HOVER));
		saveAction.setDisabledImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_SAVE_EDIT_DISABLED));
		partService.addPartListener(saveAction);
		saveAction.setActionDefinitionId(saveActionDefId);
		keyBindingService.registerGlobalAction(saveAction);

		saveAsAction = new SaveAsAction(window);
		saveAsAction.setImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_SAVEAS_EDIT));
		saveAsAction.setHoverImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_SAVEAS_EDIT_HOVER));
		saveAsAction.setDisabledImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_SAVEAS_EDIT_DISABLED));
		partService.addPartListener(saveAsAction);

		saveAllAction = new SaveAllAction(window);
		saveAllAction.setImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_SAVEALL_EDIT));
		saveAllAction.setHoverImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_SAVEALL_EDIT_HOVER));
		saveAllAction.setDisabledImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_SAVEALL_EDIT_DISABLED));
		partService.addPartListener(saveAllAction);
		saveAllAction.setActionDefinitionId(saveAllActionDefId);
		keyBindingService.registerGlobalAction(saveAllAction);

		undoAction = createGlobalAction(IWorkbenchActionConstants.UNDO, "edit", true); //$NON-NLS-1$
		undoAction.setImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_UNDO_EDIT));
		undoAction.setHoverImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_UNDO_EDIT_HOVER));
		undoAction.setDisabledImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_UNDO_EDIT_DISABLED));

		redoAction = createGlobalAction(IWorkbenchActionConstants.REDO, "edit", true); //$NON-NLS-1$
		redoAction.setImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_REDO_EDIT));
		redoAction.setHoverImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_REDO_EDIT_HOVER));
		redoAction.setDisabledImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_REDO_EDIT_DISABLED));

		cutAction = createGlobalAction(IWorkbenchActionConstants.CUT, "edit", false); //$NON-NLS-1$
		cutAction.setImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_CUT_EDIT));
		cutAction.setHoverImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_CUT_EDIT_HOVER));
		cutAction.setDisabledImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_CUT_EDIT_DISABLED));

		copyAction = createGlobalAction(IWorkbenchActionConstants.COPY, "edit", false); //$NON-NLS-1$
		copyAction.setImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_COPY_EDIT));
		copyAction.setHoverImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_COPY_EDIT_HOVER));
		copyAction.setDisabledImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_COPY_EDIT_DISABLED));

		pasteAction = createGlobalAction(IWorkbenchActionConstants.PASTE, "edit", false); //$NON-NLS-1$
		pasteAction.setImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_PASTE_EDIT));
		pasteAction.setHoverImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_PASTE_EDIT_HOVER));
		pasteAction.setDisabledImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_PASTE_EDIT_DISABLED));

		printAction = createGlobalAction(IWorkbenchActionConstants.PRINT, "file", false); //$NON-NLS-1$
		printAction.setImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_PRINT_EDIT));
		printAction.setHoverImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_PRINT_EDIT_HOVER));
		printAction.setDisabledImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_PRINT_EDIT_DISABLED));

		selectAllAction = createGlobalAction(IWorkbenchActionConstants.SELECT_ALL, "edit", false); //$NON-NLS-1$

		findAction = createGlobalAction(IWorkbenchActionConstants.FIND, "findReplace", "edit", "findReplace", false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		// Find's images are commented out due to a conflict with Search.
		// See bug 16412.
		//		findAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_SEARCH_SRC));
		//		findAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_SEARCH_SRC_HOVER));
		//		findAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_SEARCH_SRC_DISABLED));

		closeAction = new CloseEditorAction(window);
		partService.addPartListener(closeAction);
		closeAction.setActionDefinitionId(closeActionDefId);
		keyBindingService.registerGlobalAction(closeAction);

		closeAllAction = new CloseAllAction(window);
		partService.addPartListener(closeAllAction);
		closeAllAction.setActionDefinitionId(closeAllActionDefId);
		keyBindingService.registerGlobalAction(closeAllAction);

		closeAllSavedAction = new CloseAllSavedAction(window);
		partService.addPartListener(closeAllSavedAction);
		closeAllSavedAction.setActionDefinitionId(closeAllSavedActionDefId);
		keyBindingService.registerGlobalAction(closeAllSavedAction);

		pinEditorAction = new PinEditorAction(window);
		partService.addPartListener(pinEditorAction);
		pinEditorAction.setImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_PIN_EDITOR));
		pinEditorAction.setHoverImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_PIN_EDITOR_HOVER));
		pinEditorAction.setDisabledImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_PIN_EDITOR_DISABLED));

		aboutAction = new AboutAction(window);
		aboutAction.setImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_OBJS_DEFAULT_PROD));

		openPreferencesAction = new OpenPreferencesAction(window);

		addBookmarkAction = createGlobalAction(IWorkbenchActionConstants.BOOKMARK, "addBookMark", "edit", "addBookmark", false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
		addTaskAction = createGlobalAction(IWorkbenchActionConstants.ADD_TASK, "edit", false); //$NON-NLS-1$

		showInAction = new WorkbenchShowInAction(window);
		partService.addPartListener(showInAction);
		showInAction.setActionDefinitionId(showInActionDefId);
		keyBindingService.registerGlobalAction(showInAction);

		// can't use createGlobalAction convenience since deleteAction is not registered with the key binding service
		deleteAction = new RetargetAction(IWorkbenchActionConstants.DELETE, WorkbenchMessages.getString("Workbench.delete")); //$NON-NLS-1$
		deleteAction.setToolTipText(WorkbenchMessages.getString("Workbench.deleteToolTip")); //$NON-NLS-1$
		deleteAction.setImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_DELETE_EDIT));
		deleteAction.setHoverImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_DELETE_EDIT_HOVER));
		deleteAction.setDisabledImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_DELETE_EDIT_DISABLED));
		deleteAction.enableAccelerator(false);
		WorkbenchHelp.setHelp(
			deleteAction,
			IHelpContextIds.DELETE_RETARGET_ACTION);
		partService.addPartListener(deleteAction);
		deleteAction.setActionDefinitionId(deleteActionDefId);
		// don't register the delete action with the key binding service.
		// doing so would break cell editors that listen for keyPressed SWT 
		// events.
		//keyBindingService.registerGlobalAction(deleteAction);

		// See if a welcome page is specified
		AboutInfo[] infos =
			((Workbench) workbench).getConfigurationInfo().getFeaturesInfo();
		for (int i = 0; i < infos.length; i++) {
			if (infos[i].getWelcomePageURL() != null) {
				quickStartAction = new QuickStartAction(workbench);
				break;
			}
		}

		// Actions for invisible accelerators
		showViewMenuAction = new ShowViewMenuAction(window);
		showViewMenuAction.setActionDefinitionId(showViewMenuActionDefId);
		keyBindingService.registerGlobalAction(showViewMenuAction);

		showPartPaneMenuAction = new ShowPartPaneMenuAction(window);
		showPartPaneMenuAction.setActionDefinitionId(
			showPartPaneMenuActionDefId);
		keyBindingService.registerGlobalAction(showPartPaneMenuAction);

		nextEditorAction = new CycleEditorAction(window, true);
		nextEditorAction.setActionDefinitionId(nextEditorActionDefId);
		keyBindingService.registerGlobalAction(nextEditorAction);

		prevEditorAction = new CycleEditorAction(window, false);
		prevEditorAction.setActionDefinitionId(prevEditorActionDefId);
		keyBindingService.registerGlobalAction(prevEditorAction);

		nextPartAction = new CyclePartAction(window, true);
		nextPartAction.setActionDefinitionId(nextPartActionDefId);
		keyBindingService.registerGlobalAction(nextPartAction);

		prevPartAction = new CyclePartAction(window, false);
		prevPartAction.setActionDefinitionId(prevPartActionDefId);
		keyBindingService.registerGlobalAction(prevPartAction);

		nextPerspectiveAction = new CyclePerspectiveAction(window, true);
		nextPerspectiveAction.setActionDefinitionId(nextPerspectiveActionDefId);
		keyBindingService.registerGlobalAction(nextPerspectiveAction);

		prevPerspectiveAction = new CyclePerspectiveAction(window, false);
		prevPerspectiveAction.setActionDefinitionId(prevPerspectiveActionDefId);
		keyBindingService.registerGlobalAction(prevPerspectiveAction);

		activateEditorAction = new ActivateEditorAction(window);
		activateEditorAction.setActionDefinitionId(activateEditorActionDefId);
		keyBindingService.registerGlobalAction(activateEditorAction);

		workbenchEditorsAction = new WorkbenchEditorsAction(window);
		workbenchEditorsAction.setActionDefinitionId(
			workbenchEditorsActionDefId);
		keyBindingService.registerGlobalAction(workbenchEditorsAction);

		hideShowEditorAction = new ToggleEditorsVisibilityAction(window);
		savePerspectiveAction = new SavePerspectiveAction(window);
		editActionSetAction = new EditActionSetsAction(window);
		lockToolBarAction = new LockToolBarAction(window);
		resetPerspectiveAction = new ResetPerspectiveAction(window);
		closePerspAction = new ClosePerspectiveAction(window);
		closeAllPerspsAction = new CloseAllPerspectivesAction(window);

		//Must change/copy the image. It is using a local toolbar image and colored.
		backwardHistoryAction = new NavigationHistoryAction(window, false);
		backwardHistoryAction.setImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_BACKWARD_NAV));
		backwardHistoryAction.setActionDefinitionId(backwardHistoryActionDefId);
		keyBindingService.registerGlobalAction(backwardHistoryAction);

		forwardHistoryAction = new NavigationHistoryAction(window, true);
		forwardHistoryAction.setImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_FORWARD_NAV));
		forwardHistoryAction.setActionDefinitionId(forwardHistoryActionDefId);
		keyBindingService.registerGlobalAction(forwardHistoryAction);

		revertAction = createGlobalAction(IWorkbenchActionConstants.REVERT, "file", false); //$NON-NLS-1$
		refreshAction = createGlobalAction(IWorkbenchActionConstants.REFRESH, "file", false); //$NON-NLS-1$
		propertiesAction = createGlobalAction(IWorkbenchActionConstants.PROPERTIES, "file", false); //$NON-NLS-1$
		moveAction = createGlobalAction(IWorkbenchActionConstants.MOVE, "edit", false); //$NON-NLS-1$
		renameAction = createGlobalAction(IWorkbenchActionConstants.RENAME, "edit", false); //$NON-NLS-1$
		goIntoAction = createGlobalAction(IWorkbenchActionConstants.GO_INTO, "navigate", false); //$NON-NLS-1$
		backAction = createGlobalAction(IWorkbenchActionConstants.BACK, "navigate", true); //$NON-NLS-1$
		forwardAction = createGlobalAction(IWorkbenchActionConstants.FORWARD, "navigate", true); //$NON-NLS-1$
		upAction = createGlobalAction(IWorkbenchActionConstants.UP, "navigate", true); //$NON-NLS-1$
		nextAction = createGlobalAction(IWorkbenchActionConstants.NEXT, "navigate", true); //$NON-NLS-1$
		nextAction.setImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_NEXT_NAV));
		previousAction = createGlobalAction(IWorkbenchActionConstants.PREVIOUS, "navigate", true); //$NON-NLS-1$
		previousAction.setImageDescriptor(
			WorkbenchImages.getImageDescriptor(
				IWorkbenchGraphicConstants.IMG_CTOOL_PREVIOUS_NAV));
		buildProjectAction = createGlobalAction(IWorkbenchActionConstants.BUILD_PROJECT, "project", false); //$NON-NLS-1$
		rebuildProjectAction = createGlobalAction(IWorkbenchActionConstants.REBUILD_PROJECT, "project", false); //$NON-NLS-1$
		openProjectAction = createGlobalAction(IWorkbenchActionConstants.OPEN_PROJECT, "project", false); //$NON-NLS-1$
		closeProjectAction = createGlobalAction(IWorkbenchActionConstants.CLOSE_PROJECT, "project", false); //$NON-NLS-1$
	}

	/**
	 * Creates a global (retargetable) action.
	 * 
	 * @param id the global action id (also used as the id to use when looking up messages, and the suffix to use
	 *   in the action definition id)
	 * @param actionDefPrefix the prefix to use in the action definition id
	 * @param labelRetarget <code>true</code> if the action's label is retargetable, <code>false</code> if not
	 * @return the action
	 */
	private RetargetAction createGlobalAction(
		String id,
		String actionDefPrefix,
		boolean labelRetarget) {
		return createGlobalAction(id, id, actionDefPrefix, id, labelRetarget);
	}

	/**
	 * Creates a global (retargetable) action.
	 * 
	 * @param id the global action id
	 * @param messageId the id to use when looking up messages
	 * @param actionDefPrefix the prefix to use in the action definition id
	 * @param actionDefSuffix the suffix to use in the action definition id
	 * @param labelRetarget <code>true</code> if the action's label is retargetable, <code>false</code> if not
	 * @return the action
	 */
	private RetargetAction createGlobalAction(
		String id,
		String messageId,
		String actionDefPrefix,
		String actionDefSuffix,
		boolean labelRetarget) {
		String text = WorkbenchMessages.getString("Workbench." + messageId); //$NON-NLS-1$
		String toolTipText = WorkbenchMessages.getString("Workbench." + messageId + "ToolTip"); //$NON-NLS-1$ //$NON-NLS-2$
		RetargetAction action;
		if (labelRetarget) {
			action = new LabelRetargetAction(id, text);
		} else {
			action = new RetargetAction(id, text);
		}
		action.setToolTipText(toolTipText);
		getWindow().getPartService().addPartListener(action);
		action.setActionDefinitionId(PlatformUI.PLUGIN_ID + "." + actionDefPrefix + "." + actionDefSuffix); //$NON-NLS-1$ //$NON-NLS-2$
		getWindow().getKeyBindingService().registerGlobalAction(action);
		return action;
	}

	/**
	 * Updates the menubar and toolbar when changes are made to the preferences.
	 */
	private void handlePropertyChange(PropertyChangeEvent event) {
		// Allow manual incremental build only if the
		// auto build setting is off.
		IPreferenceStore store =
			WorkbenchPlugin.getDefault().getPreferenceStore();
		if (event.getProperty().equals(IPreferenceConstants.AUTO_BUILD)) {
			// Auto build is stored in core. It is also in the preference store for use by import/export.
			IWorkspaceDescription description =
				ResourcesPlugin.getWorkspace().getDescription();
			boolean autoBuildSetting = description.isAutoBuilding();
			boolean newAutoBuildSetting =
				store.getBoolean(IPreferenceConstants.AUTO_BUILD);

			if (autoBuildSetting != newAutoBuildSetting) {
				description.setAutoBuilding(newAutoBuildSetting);
				autoBuildSetting = newAutoBuildSetting;
				try {
					ResourcesPlugin.getWorkspace().setDescription(description);
				} catch (CoreException e) {
					WorkbenchPlugin.log("Error changing autobuild pref", e.getStatus()); //$NON-NLS-1$
				}

				// If auto build is turned on, then do a global incremental
				// build on all the projects.
				if (newAutoBuildSetting) {
					GlobalBuildAction action =
						new GlobalBuildAction(
							window,
							IncrementalProjectBuilder.INCREMENTAL_BUILD);
					action.doBuild();
				}
			}

			if (autoBuildSetting)
				removeManualIncrementalBuildAction();
			else
				addManualIncrementalBuildAction();

		} else if (
			event.getProperty().equals(
				IPreferenceConstants.REUSE_EDITORS_BOOLEAN)) {
			if (store.getBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN))
				addPinEditorAction();
			else
				removePinEditorAction();
		} else if (
			event.getProperty().equals(IPreferenceConstants.REUSE_EDITORS)) {
			pinEditorAction.updateState();
		} else if (
			event.getProperty().equals(IPreferenceConstants.RECENT_FILES)) {
			Workbench wb = (Workbench) (Workbench) getWindow().getWorkbench();
			int newValue = store.getInt(IPreferenceConstants.RECENT_FILES);
			wb.getEditorHistory().reset(newValue);
			if (newValue == 0) {
				// the open recent menu item can go from enabled to disabled
				getWindow().updateActionBars();
			}
		}
	}

	/**
	 * Adds the editor history navigation actions to the toolbar.
	 */
	private void addHistoryActions() {
		CoolBarManager cBarMgr = getWindow().getCoolBarManager();
		CoolBarContributionItem coolBarItem = new CoolBarContributionItem(cBarMgr, historyGroup); //$NON-NLS-1$
		// we want to add the history cool item before the editor cool item (if it exists)
		IContributionItem refItem =
			cBarMgr.findSubId(IWorkbenchActionConstants.GROUP_EDITOR);
		if (refItem == null) {
			cBarMgr.add(coolBarItem);
		} else {
			cBarMgr.insertBefore(refItem.getId(), coolBarItem);
		}
		coolBarItem.setVisible(true);
		IToolBarManager tBarMgr =
			(IToolBarManager) coolBarItem.getToolBarManager();
		tBarMgr.add(new GroupMarker(historyGroup));
		tBarMgr.insertAfter(historyGroup, forwardHistoryAction);
		tBarMgr.insertAfter(historyGroup, backwardHistoryAction);
		cBarMgr.update(true);
	}

	/**
	 * Adds the pin action to the toolbar.
	 */
	private void addPinEditorAction() {
		CoolBarManager cBarMgr = getWindow().getCoolBarManager();
		CoolBarContributionItem coolBarItem = new CoolBarContributionItem(cBarMgr, pinEditorGroup); //$NON-NLS-1$
		// we want to add the pin editor cool item before the editor cool item (if it exists)
		IContributionItem refItem =
			cBarMgr.findSubId(IWorkbenchActionConstants.GROUP_EDITOR);
		if (refItem == null) {
			cBarMgr.add(coolBarItem);
		} else {
			cBarMgr.insertBefore(refItem.getId(), coolBarItem);
		}
		coolBarItem.setVisible(true);
		IToolBarManager tBarMgr =
			(IToolBarManager) coolBarItem.getToolBarManager();
		tBarMgr.add(new GroupMarker(pinEditorGroup));
		pinEditorAction.setVisible(true);
		tBarMgr.insertAfter(pinEditorGroup, pinEditorAction);
		cBarMgr.update(true);
	}

	/**
	 * Removes the pin action from the toolbar.
	 */
	private void removePinEditorAction() {
		CoolBarManager cBarMgr = getWindow().getCoolBarManager();
		CoolBarContributionItem coolBarItem = (CoolBarContributionItem) cBarMgr.find(pinEditorGroup); //$NON-NLS-1$
		if (coolBarItem != null)
			coolBarItem.dispose();
		cBarMgr.update(true);
	}

	/**
	 * Add the manual incremental build action
	 * to both the menu bar and the tool bar.
	 */
	private void addManualIncrementalBuildAction() {
		MenuManager menubar = getWindow().getMenuBarManager();
		IMenuManager manager =
			menubar.findMenuUsingPath(IWorkbenchActionConstants.M_PROJECT);
		if (manager != null) {
			try {
				manager.insertBefore(
					IWorkbenchActionConstants.REBUILD_PROJECT,
					buildProjectAction);
				manager.insertBefore(
					IWorkbenchActionConstants.REBUILD_ALL,
					buildAllAction);
			} catch (IllegalArgumentException e) {
				// action not found!
			}
		}
		IContributionManager cBarMgr = getWindow().getCoolBarManager();
		CoolBarContributionItem groupItem =
			(CoolBarContributionItem) cBarMgr.find(workbenchToolGroupId);
		IContributionManager tBarMgr = groupItem.getToolBarManager();
		tBarMgr.appendToGroup(
			IWorkbenchActionConstants.BUILD_EXT,
			buildAllAction);
		tBarMgr.update(true);
	}

	/**
	 * Remove the manual incremental build action
	 * from both the menu bar and the tool bar.
	 */
	protected void removeManualIncrementalBuildAction() {
		MenuManager menubar = getWindow().getMenuBarManager();
		IMenuManager manager =
			menubar.findMenuUsingPath(IWorkbenchActionConstants.M_PROJECT);
		if (manager != null) {
			try {
				manager.remove(IWorkbenchActionConstants.BUILD);
				manager.remove(IWorkbenchActionConstants.BUILD_PROJECT);
			} catch (IllegalArgumentException e) {
				// action was not in menu
			}
		}
		CoolBarManager cBarMgr = getWindow().getCoolBarManager();
		CoolBarContributionItem groupItem =
			(CoolBarContributionItem) cBarMgr.find(workbenchToolGroupId);
		if (groupItem != null) {
			IContributionManager tBarMgr = groupItem.getToolBarManager();
			try {
				tBarMgr.remove(IWorkbenchActionConstants.BUILD);
				tBarMgr.update(true);
			} catch (IllegalArgumentException e) {
				// action was not in toolbar
			}
		}
	}
}
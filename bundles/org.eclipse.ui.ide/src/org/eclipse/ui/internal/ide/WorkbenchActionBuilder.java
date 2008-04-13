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
package org.eclipse.ui.internal.ide;

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
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.BuildAction;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.NewWizardMenu;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.ide.IIDEActionConstants;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
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
import org.eclipse.ui.menus.IMenuService;

/**
 * Adds actions to a workbench window.
 */
public final class WorkbenchActionBuilder extends ActionBarAdvisor {
    private final IWorkbenchWindow window;

    // generic actions
    private IWorkbenchAction closeAction;

    private IWorkbenchAction closeAllAction;
    
    private IWorkbenchAction closeOthersAction;

    private IWorkbenchAction closeAllSavedAction;

    private IWorkbenchAction saveAction;

    private IWorkbenchAction saveAllAction;

    private IWorkbenchAction newWindowAction;
    
    private IWorkbenchAction newEditorAction;

    private IWorkbenchAction helpContentsAction;

    private IWorkbenchAction helpSearchAction;
	
    private IWorkbenchAction dynamicHelpAction;
    
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
    
    private IWorkbenchAction minimizePartAction;

    private IWorkbenchAction switchToEditorAction;

	private IWorkbenchAction workbookEditorsAction;

    private IWorkbenchAction quickAccessAction;

    private IWorkbenchAction backwardHistoryAction;

    private IWorkbenchAction forwardHistoryAction;

    // generic retarget actions
    private IWorkbenchAction undoAction;

    private IWorkbenchAction redoAction;

    private CommandContributionItem cutItem;

    private CommandContributionItem copyItem;

    private CommandContributionItem pasteItem;

    private CommandContributionItem deleteItem;

    private CommandContributionItem selectAllItem;

    private CommandContributionItem findItem;

    private CommandContributionItem printMenuItem;
    private CommandContributionItem printToolItem;

    private CommandContributionItem revertItem;

    private CommandContributionItem refreshItem;

    private CommandContributionItem propertiesItem;

    private IWorkbenchAction quitAction;

    private CommandContributionItem moveItem;

    private CommandContributionItem renameItem;

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

    IWorkbenchAction buildAllAction; // Incremental workspace build

    private IWorkbenchAction cleanAction;

    private IWorkbenchAction toggleAutoBuildAction;

    MenuManager buildWorkingSetMenu;

    private IWorkbenchAction quickStartAction;

    private IWorkbenchAction tipsAndTricksAction;

    private QuickMenuAction showInQuickMenu;

    private QuickMenuAction newQuickMenu;

    private IWorkbenchAction introAction;

    // IDE-specific retarget actions
    private CommandContributionItem addBookmarkItem;

    private CommandContributionItem addTaskItem;

    IWorkbenchAction buildProjectAction;

    private CommandContributionItem openProjectItem;

    private CommandContributionItem closeProjectItem;

    // contribution items
    // @issue should obtain from ContributionItemFactory
    private NewWizardMenu newWizardMenu;

    private IContributionItem pinEditorContributionItem;

//    private IContributionItem searchComboItem;
    
    // @issue class is workbench internal
    private StatusLineContributionItem statusLineItem;

    private Preferences.IPropertyChangeListener prefListener;

    // listener for the "close editors automatically"
    // preference change
    private IPropertyChangeListener propPrefListener;

    private IPageListener pageListener;

    private IResourceChangeListener resourceListener;
    
    /**
     * Indicates if the action builder has been disposed
     */
    private boolean isDisposed = false;

    /**
     * The coolbar context menu manager.
     * @since 3.3
     */
	private MenuManager coolbarPopupMenuManager;

    /**
     * Constructs a new action builder which contributes actions
     * to the given window.
     * 
     * @param configurer the action bar configurer for the window
     */
    public WorkbenchActionBuilder(IActionBarConfigurer configurer) {
        super(configurer);
        window = configurer.getWindowConfigurer().getWindow();
    }

    /**
     * Returns the window to which this action builder is contributing.
     */
    private IWorkbenchWindow getWindow() {
        return window;
    }

    /**
     * Hooks listeners on the preference store and the window's page, perspective and selection services.
     */
    private void hookListeners() {

        pageListener = new IPageListener() {
            public void pageActivated(IWorkbenchPage page) {
                // do nothing
            }

            public void pageClosed(IWorkbenchPage page) {
                // do nothing
            }

            public void pageOpened(IWorkbenchPage page) {
                // set default build handler -- can't be done until the shell is available
                IAction buildHandler = new BuildAction(page.getWorkbenchWindow(), IncrementalProjectBuilder.INCREMENTAL_BUILD);
            	((RetargetActionWithDefault)buildProjectAction).setDefaultHandler(buildHandler);
            }
        };
        getWindow().addPageListener(pageListener);

        prefListener = new Preferences.IPropertyChangeListener() {
            public void propertyChange(Preferences.PropertyChangeEvent event) {
                if (event.getProperty().equals(
                        ResourcesPlugin.PREF_AUTO_BUILDING)) {
                   	updateBuildActions(false);
                }
            }
        };
        ResourcesPlugin.getPlugin().getPluginPreferences()
                .addPropertyChangeListener(prefListener);

        // listener for the "close editors automatically"
        // preference change
        propPrefListener = new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if (event.getProperty().equals(
						IPreferenceConstants.REUSE_EDITORS_BOOLEAN)) {
                    if (window.getShell() != null
                            && !window.getShell().isDisposed()) {
                        // this property change notification could be from a non-ui thread
                        window.getShell().getDisplay().syncExec(new Runnable() {
                            public void run() {
                                updatePinActionToolbar();
                            }
                        });
                    }
                }
            }
        };
        /*
         * In order to ensure that the pin action toolbar sets its size 
         * correctly, the pin action should set its visiblity before we call updatePinActionToolbar().
         * 
         * In other words we always want the PinActionContributionItem to be notified before the 
         * WorkbenchActionBuilder.
         */
        WorkbenchPlugin.getDefault().getPreferenceStore()
                .addPropertyChangeListener(propPrefListener);
        //listen for project description changes, which can affect enablement of build actions
        resourceListener = new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				IResourceDelta delta = event.getDelta();
				if (delta == null) {
					return;
				}
				IResourceDelta[] projectDeltas = delta.getAffectedChildren();
				for (int i = 0; i < projectDeltas.length; i++) {
					int kind = projectDeltas[i].getKind();
					//affected by projects being opened/closed or description changes
					boolean changed = (projectDeltas[i].getFlags() & (IResourceDelta.DESCRIPTION | IResourceDelta.OPEN)) != 0;
					if (kind != IResourceDelta.CHANGED || changed) {
						updateBuildActions(false);
						return;
					}
				}
			}
		};
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener, IResourceChangeEvent.POST_CHANGE);
    }

    public void fillActionBars(int flags) {
        super.fillActionBars(flags);
        if ((flags & FILL_PROXY) == 0) {
            updateBuildActions(true);
            hookListeners();
        }
    }

    /**
     * Fills the coolbar with the workbench actions.
     */
    protected void fillCoolBar(ICoolBarManager coolBar) {

    	IActionBarConfigurer2 actionBarConfigurer = (IActionBarConfigurer2) getActionBarConfigurer();
        { // Set up the context Menu
            coolbarPopupMenuManager = new MenuManager();
			coolbarPopupMenuManager.add(new ActionContributionItem(lockToolBarAction));
            coolbarPopupMenuManager.add(new ActionContributionItem(editActionSetAction));
            coolBar.setContextMenuManager(coolbarPopupMenuManager);
            IMenuService menuService = (IMenuService) window.getService(IMenuService.class);
            menuService.populateContributionManager(coolbarPopupMenuManager, "popup:windowCoolbarContextMenu"); //$NON-NLS-1$
        }
        coolBar.add(new GroupMarker(IIDEActionConstants.GROUP_FILE));
        { // File Group
            IToolBarManager fileToolBar = actionBarConfigurer.createToolBarManager();
            fileToolBar.add(new Separator(IWorkbenchActionConstants.NEW_GROUP));
            fileToolBar.add(newWizardDropDownAction);
            fileToolBar.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
            fileToolBar.add(new GroupMarker(
                    IWorkbenchActionConstants.SAVE_GROUP));
            fileToolBar.add(saveAction);
            fileToolBar
                    .add(new GroupMarker(IWorkbenchActionConstants.SAVE_EXT));
            fileToolBar.add(printToolItem);
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
        }

        coolBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

        coolBar.add(new GroupMarker(IIDEActionConstants.GROUP_NAV));
        { // Navigate group
            IToolBarManager navToolBar = actionBarConfigurer.createToolBarManager();
            navToolBar.add(new Separator(
                    IWorkbenchActionConstants.HISTORY_GROUP));
            navToolBar
                    .add(new GroupMarker(IWorkbenchActionConstants.GROUP_APP));
            navToolBar.add(backwardHistoryAction);
            navToolBar.add(forwardHistoryAction);
            navToolBar.add(new Separator(IWorkbenchActionConstants.PIN_GROUP));
            navToolBar.add(pinEditorContributionItem);

            // Add to the cool bar manager
            coolBar.add(actionBarConfigurer.createToolBarContributionItem(navToolBar,
                    IWorkbenchActionConstants.TOOLBAR_NAVIGATE));
        }

        coolBar.add(new GroupMarker(IWorkbenchActionConstants.GROUP_EDITOR));
     
        coolBar.add(new GroupMarker(IWorkbenchActionConstants.GROUP_HELP));
        
        { // Help group
            IToolBarManager helpToolBar = actionBarConfigurer.createToolBarManager();
            helpToolBar.add(new Separator(IWorkbenchActionConstants.GROUP_HELP));
//            helpToolBar.add(searchComboItem);
              // Add the group for applications to contribute
            helpToolBar.add(new GroupMarker(IWorkbenchActionConstants.GROUP_APP));              
            // Add to the cool bar manager
            coolBar.add(actionBarConfigurer.createToolBarContributionItem(helpToolBar,
                    IWorkbenchActionConstants.TOOLBAR_HELP));
        }        

    }

    /**
     * Fills the menu bar with the workbench actions.
     */
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
        {
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
        menu.add(revertItem);
        menu.add(new Separator());
        menu.add(moveItem);
        menu.add(renameItem);
        menu.add(refreshItem);

        menu.add(new GroupMarker(IWorkbenchActionConstants.SAVE_EXT));
        menu.add(new Separator());
        menu.add(printMenuItem);
        menu.add(new GroupMarker(IWorkbenchActionConstants.PRINT_EXT));
        menu.add(new Separator());
        menu.add(openWorkspaceAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.OPEN_EXT));
        menu.add(new Separator());
        menu.add(importResourcesAction);
        menu.add(exportResourcesAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.IMPORT_EXT));
        menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        menu.add(new Separator());
        menu.add(propertiesItem);

        menu.add(ContributionItemFactory.REOPEN_EDITORS.create(getWindow()));
        menu.add(new GroupMarker(IWorkbenchActionConstants.MRU));
        menu.add(new Separator());
        
        // If we're on OS X we shouldn't show this command in the File menu. It
		// should be invisible to the user. However, we should not remove it -
		// the carbon UI code will do a search through our menu structure
		// looking for it when Cmd-Q is invoked (or Quit is chosen from the
		// application menu.
		ActionContributionItem quitItem = new ActionContributionItem(quitAction);
		quitItem.setVisible(!"carbon".equals(SWT.getPlatform())); //$NON-NLS-1$
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

        menu.add(undoAction);
        menu.add(redoAction);
        menu.add(new GroupMarker(IWorkbenchActionConstants.UNDO_EXT));
        menu.add(new Separator());

        menu.add(cutItem);
        menu.add(copyItem);
        menu.add(pasteItem);
        menu.add(new GroupMarker(IWorkbenchActionConstants.CUT_EXT));
        menu.add(new Separator());

        menu.add(deleteItem);
        menu.add(selectAllItem);
        menu.add(new Separator());

        menu.add(findItem);
        menu.add(new GroupMarker(IWorkbenchActionConstants.FIND_EXT));
        menu.add(new Separator());

        menu.add(addBookmarkItem);
        menu.add(addTaskItem);
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
        {
			MenuManager showInSubMenu = new MenuManager(
					IDEWorkbenchMessages.Workbench_showIn, "showIn"); //$NON-NLS-1$
			showInSubMenu.setActionDefinitionId(showInQuickMenu
					.getActionDefinitionId());
			showInSubMenu.add(ContributionItemFactory.VIEWS_SHOW_IN
					.create(getWindow()));
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
        MenuManager menu = new MenuManager(
                IDEWorkbenchMessages.Workbench_project, IWorkbenchActionConstants.M_PROJECT);
        menu.add(new Separator(IWorkbenchActionConstants.PROJ_START));

        menu.add(openProjectItem);
        menu.add(closeProjectItem);
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

        menu.add(newWindowAction);
		menu.add(newEditorAction);
		
        menu.add(new Separator());
        addPerspectiveActions(menu);
        menu.add(new Separator());
        addKeyboardShortcuts(menu);
        Separator sep = new Separator(IWorkbenchActionConstants.MB_ADDITIONS);
		sep.setVisible(!"carbon".equals(SWT.getPlatform())); //$NON-NLS-1$
		menu.add(sep);
        
        // See the comment for quit in createFileMenu
        ActionContributionItem openPreferencesItem = new ActionContributionItem(openPreferencesAction);
        openPreferencesItem.setVisible(!"carbon".equals(SWT.getPlatform())); //$NON-NLS-1$
        menu.add(openPreferencesItem);

        menu.add(ContributionItemFactory.OPEN_WINDOWS.create(getWindow()));
        return menu;
    }

    /**
     * Adds the perspective actions to the specified menu.
     */
    private void addPerspectiveActions(MenuManager menu) {
        {
            String openText = IDEWorkbenchMessages.Workbench_openPerspective;
            MenuManager changePerspMenuMgr = new MenuManager(openText,
                    "openPerspective"); //$NON-NLS-1$
            IContributionItem changePerspMenuItem = ContributionItemFactory.PERSPECTIVES_SHORTLIST
                    .create(getWindow());
            changePerspMenuMgr.add(changePerspMenuItem);
            menu.add(changePerspMenuMgr);
        }
        {
            MenuManager showViewMenuMgr = new MenuManager(IDEWorkbenchMessages.Workbench_showView, "showView"); //$NON-NLS-1$
            IContributionItem showViewMenu = ContributionItemFactory.VIEWS_SHORTLIST
                    .create(getWindow());
            showViewMenuMgr.add(showViewMenu);
            menu.add(showViewMenuMgr);
        }
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
    private void addWorkingSetBuildActions(MenuManager menu) {
        buildWorkingSetMenu = new MenuManager(IDEWorkbenchMessages.Workbench_buildSet);
        IContributionItem workingSetBuilds = new BuildSetMenu(window,
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
        subMenu.add(maximizePartAction);
        subMenu.add(minimizePartAction);
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
		aboutItem.setVisible(!"carbon".equals(SWT.getPlatform())); //$NON-NLS-1$
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
    public void dispose() {
        if (isDisposed) {
			return;
		}
    	isDisposed = true;
    	IMenuService menuService = (IMenuService) window.getService(IMenuService.class);
        menuService.releaseContributions(coolbarPopupMenuManager);
        coolbarPopupMenuManager.dispose();
        
        getActionBarConfigurer().getStatusLineManager().remove(statusLineItem);
        if (pageListener != null) {
            window.removePageListener(pageListener);
            pageListener = null;
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

        pinEditorContributionItem.dispose();
        showInQuickMenu.dispose();
        newQuickMenu.dispose();
//        searchComboItem.dispose();
        
        // null out actions to make leak debugging easier
        closeAction = null;
        closeAllAction = null;
        closeAllSavedAction = null;
        closeOthersAction = null;
        saveAction = null;
        saveAllAction = null;
        newWindowAction = null;
		newEditorAction = null;
        helpContentsAction = null;
        helpSearchAction = null;
		dynamicHelpAction = null;
        aboutAction = null;
        openPreferencesAction = null;
        saveAsAction = null;
        hideShowEditorAction = null;
        savePerspectiveAction = null;
        resetPerspectiveAction = null;
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
        maximizePartAction = null;
        minimizePartAction = null;
        switchToEditorAction = null;
        quickAccessAction = null;
        backwardHistoryAction = null;
        forwardHistoryAction = null;
        undoAction = null;
        redoAction = null;
        cutItem = null;
        copyItem = null;
        pasteItem = null;
        deleteItem = null;
        selectAllItem = null;
        findItem = null;
        printMenuItem = null;
        printToolItem = null;
        revertItem = null;
        refreshItem = null;
        propertiesItem = null;
        quitAction = null;
        moveItem = null;
        renameItem = null;
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
        addBookmarkItem = null;
        addTaskItem = null;
        buildProjectAction = null;
        openProjectItem = null;
        closeProjectItem = null;
        newWizardMenu = null;
        pinEditorContributionItem = null;
//        searchComboItem = null;
        statusLineItem = null;
        prefListener = null;
        propPrefListener = null;
        introAction = null;
        
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
    protected void fillStatusLine(IStatusLineManager statusLine) {
        statusLine.add(statusLineItem);
    }

    /**
     * Creates actions (and contribution items) for the menu bar, toolbar and status line.
     */
    protected void makeActions(final IWorkbenchWindow window) {
    	ISharedImages sharedImages = window.getWorkbench().getSharedImages();
    	IActionCommandMappingService acms = (IActionCommandMappingService) window
				.getService(IActionCommandMappingService.class);

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

		newEditorAction = ActionFactory.NEW_EDITOR.create(window);
		register(newEditorAction);

        undoAction = ActionFactory.UNDO.create(window);
        register(undoAction);

        redoAction = ActionFactory.REDO.create(window);
        register(redoAction);

        String cutId = "org.eclipse.ui.edit.cut"; //$NON-NLS-1$
		CommandContributionItemParameter cutParm = new CommandContributionItemParameter(
				window,
				ActionFactory.CUT.getId(),
				cutId,
				null,
				sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_CUT),
				sharedImages
						.getImageDescriptor(ISharedImages.IMG_TOOL_CUT_DISABLED),
				null, WorkbenchMessages.Workbench_cut, null,
				WorkbenchMessages.Workbench_cutToolTip,
				CommandContributionItem.STYLE_PUSH, null, false);
        cutItem = new CommandContributionItem(cutParm);
        acms.map(ActionFactory.CUT.getId(), cutId);

        String copyId = "org.eclipse.ui.edit.copy"; //$NON-NLS-1$
        CommandContributionItemParameter copyParm = new CommandContributionItemParameter(
				window,
				ActionFactory.COPY.getId(),
				copyId,
				null,
				sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY),
				sharedImages
						.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED),
				null, WorkbenchMessages.Workbench_copy, null,
				WorkbenchMessages.Workbench_copyToolTip,
				CommandContributionItem.STYLE_PUSH, null, false);
		copyItem = new CommandContributionItem(copyParm);
		acms.map(ActionFactory.COPY.getId(), copyId);

		String pasteId = "org.eclipse.ui.edit.paste"; //$NON-NLS-1$
		CommandContributionItemParameter pasteParm = new CommandContributionItemParameter(
				window,
				ActionFactory.PASTE.getId(),
				pasteId,
				null,
				sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE),
				sharedImages
						.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED),
				null, WorkbenchMessages.Workbench_paste, null,
				WorkbenchMessages.Workbench_pasteToolTip,
				CommandContributionItem.STYLE_PUSH, null, false);
        pasteItem = new CommandContributionItem(pasteParm);
        acms.map(ActionFactory.PASTE.getId(), pasteId);

        String printId = "org.eclipse.ui.file.print"; //$NON-NLS-1$
		CommandContributionItemParameter printParm = new CommandContributionItemParameter(
				window,
				ActionFactory.PRINT.getId(),
				printId,
				null,
				sharedImages.getImageDescriptor(ISharedImages.IMG_ETOOL_PRINT_EDIT),
				sharedImages
						.getImageDescriptor(ISharedImages.IMG_ETOOL_PRINT_EDIT_DISABLED),
				null, WorkbenchMessages.Workbench_print, null,
				WorkbenchMessages.Workbench_printToolTip,
				CommandContributionItem.STYLE_PUSH, null, false);
        printMenuItem = new CommandContributionItem(printParm);
        printToolItem = new CommandContributionItem(printParm);
        acms.map(ActionFactory.PRINT.getId(), printId);

        String selectId = "org.eclipse.ui.edit.selectAll"; //$NON-NLS-1$
		CommandContributionItemParameter selectAllParm = new CommandContributionItemParameter(
				window,
				ActionFactory.SELECT_ALL.getId(),
				selectId,
				null,
				null,
				null,
				null, WorkbenchMessages.Workbench_selectAll, null,
				WorkbenchMessages.Workbench_selectAllToolTip,
				CommandContributionItem.STYLE_PUSH, null, false);
        selectAllItem = new CommandContributionItem(selectAllParm);
        acms.map(ActionFactory.SELECT_ALL.getId(), selectId);

        String findId = "org.eclipse.ui.edit.findReplace"; //$NON-NLS-1$
		CommandContributionItemParameter findParm = new CommandContributionItemParameter(
				window,
				ActionFactory.FIND.getId(),
				findId,
				null,
				null,
				null,
				null, WorkbenchMessages.Workbench_findReplace, null,
				WorkbenchMessages.Workbench_findReplaceToolTip,
				CommandContributionItem.STYLE_PUSH, null, false);
        findItem = new CommandContributionItem(findParm);
        acms.map(ActionFactory.FIND.getId(), findId);

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

        String bookmarkId = "org.eclipse.ui.edit.addBookmark"; //$NON-NLS-1$
		CommandContributionItemParameter bookmarParm = new CommandContributionItemParameter(
				window,
				IDEActionFactory.BOOKMARK.getId(),
				bookmarkId,
				null,
				null,
				null,
				null, IDEWorkbenchMessages.Workbench_addBookmark, null,
				IDEWorkbenchMessages.Workbench_addBookmarkToolTip,
				CommandContributionItem.STYLE_PUSH, null, false);
		addBookmarkItem = new CommandContributionItem(bookmarParm);
        acms.map(IDEActionFactory.BOOKMARK.getId(), bookmarkId);
        

        String addTaskId = "org.eclipse.ui.edit.addTask"; //$NON-NLS-1$
		CommandContributionItemParameter addTaskParm = new CommandContributionItemParameter(
				window,
				IDEActionFactory.ADD_TASK.getId(),
				addTaskId,
				null,
				null,
				null,
				null, IDEWorkbenchMessages.Workbench_addTask, null,
				IDEWorkbenchMessages.Workbench_addTaskToolTip,
				CommandContributionItem.STYLE_PUSH, null, false);
        addTaskItem = new CommandContributionItem(addTaskParm);
        acms.map(IDEActionFactory.ADD_TASK.getId(), addTaskId);

        String deleteId = "org.eclipse.ui.edit.delete"; //$NON-NLS-1$
		CommandContributionItemParameter deleteParm = new CommandContributionItemParameter(
				window,
				ActionFactory.DELETE.getId(),
				deleteId,
				null,
				sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE),
				sharedImages
						.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE_DISABLED),
				null, WorkbenchMessages.Workbench_delete, null,
				WorkbenchMessages.Workbench_deleteToolTip,
				CommandContributionItem.STYLE_PUSH, 
				IWorkbenchHelpContextIds.DELETE_RETARGET_ACTION, false);
        deleteItem = new CommandContributionItem(deleteParm);
        acms.map(ActionFactory.DELETE.getId(), deleteId);

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

        maximizePartAction = ActionFactory.MAXIMIZE.create(window);
        register(maximizePartAction);

		minimizePartAction = ActionFactory.MINIMIZE.create(window);
		register(minimizePartAction);
        
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
        savePerspectiveAction = ActionFactory.SAVE_PERSPECTIVE
                .create(window);
        register(savePerspectiveAction);
        editActionSetAction = ActionFactory.EDIT_ACTION_SETS
                .create(window);
        register(editActionSetAction);
        lockToolBarAction = ActionFactory.LOCK_TOOL_BAR.create(window);
        register(lockToolBarAction);
        resetPerspectiveAction = ActionFactory.RESET_PERSPECTIVE
                .create(window);
        register(resetPerspectiveAction);
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

        String revertId = "org.eclipse.ui.file.revert"; //$NON-NLS-1$
		CommandContributionItemParameter revertParm = new CommandContributionItemParameter(
				window,
				ActionFactory.REVERT.getId(),
				revertId,
				null,
				null,
				null,
				null, WorkbenchMessages.Workbench_revert, null,
				WorkbenchMessages.Workbench_revertToolTip,
				CommandContributionItem.STYLE_PUSH, null, false);
        revertItem = new CommandContributionItem(revertParm);
        acms.map(ActionFactory.REVERT.getId(), revertId);

        String refreshId = "org.eclipse.ui.file.refresh"; //$NON-NLS-1$
		CommandContributionItemParameter refreshParm = new CommandContributionItemParameter(
				window,
				ActionFactory.REFRESH.getId(),
				refreshId,
				null,
				null,
				null,
				null, WorkbenchMessages.Workbench_refresh, null,
				WorkbenchMessages.Workbench_refreshToolTip,
				CommandContributionItem.STYLE_PUSH, null, false);
        refreshItem = new CommandContributionItem(refreshParm);
        acms.map(ActionFactory.REFRESH.getId(), refreshId);

        String propId = "org.eclipse.ui.file.properties"; //$NON-NLS-1$
		CommandContributionItemParameter propertiesParm = new CommandContributionItemParameter(
				window,
				ActionFactory.PROPERTIES.getId(),
				propId,
				null,
				null,
				null,
				null, WorkbenchMessages.Workbench_properties, null,
				WorkbenchMessages.Workbench_propertiesToolTip,
				CommandContributionItem.STYLE_PUSH, null, false);
        propertiesItem = new CommandContributionItem(propertiesParm);
        acms.map(ActionFactory.PROPERTIES.getId(), propId);

        quitAction = ActionFactory.QUIT.create(window);
        register(quitAction);

        String moveId = "org.eclipse.ui.edit.move"; //$NON-NLS-1$
		CommandContributionItemParameter moveParm = new CommandContributionItemParameter(
				window,
				ActionFactory.MOVE.getId(),
				moveId,
				null,
				null,
				null,
				null, WorkbenchMessages.Workbench_move, null,
				WorkbenchMessages.Workbench_moveToolTip,
				CommandContributionItem.STYLE_PUSH, null, false);
        moveItem = new CommandContributionItem(moveParm);
        acms.map(ActionFactory.MOVE.getId(), moveId);

        String renameId = "org.eclipse.ui.edit.rename"; //$NON-NLS-1$
		CommandContributionItemParameter renameParm = new CommandContributionItemParameter(
				window,
				ActionFactory.RENAME.getId(),
				renameId,
				null,
				null,
				null,
				null, WorkbenchMessages.Workbench_rename, null,
				WorkbenchMessages.Workbench_renameToolTip,
				CommandContributionItem.STYLE_PUSH, null, false);
        renameItem = new CommandContributionItem(renameParm);
        acms.map(ActionFactory.RENAME.getId(), renameId);

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

        String openProjectId = "org.eclipse.ui.project.openProject"; //$NON-NLS-1$
		CommandContributionItemParameter openProjectParm = new CommandContributionItemParameter(
				window,
				IDEActionFactory.OPEN_PROJECT.getId(),
				openProjectId,
				null,
				null,
				null,
				null, IDEWorkbenchMessages.OpenResourceAction_text, null,
				IDEWorkbenchMessages.OpenResourceAction_toolTip,
				CommandContributionItem.STYLE_PUSH, null, false);
		openProjectItem = new CommandContributionItem(openProjectParm);
        acms.map(IDEActionFactory.OPEN_PROJECT.getId(), openProjectId);

        String closeProjectId = "org.eclipse.ui.project.closeProject"; //$NON-NLS-1$
		CommandContributionItemParameter closeProjectParm = new CommandContributionItemParameter(
				window,
				IDEActionFactory.CLOSE_PROJECT.getId(),
				closeProjectId,
				null,
				null,
				null,
				null, IDEWorkbenchMessages.CloseResourceAction_text, null,
				IDEWorkbenchMessages.CloseResourceAction_text,
				CommandContributionItem.STYLE_PUSH, null, false);
		closeProjectItem = new CommandContributionItem(closeProjectParm);
        acms.map(IDEActionFactory.CLOSE_PROJECT.getId(), closeProjectId);

        openWorkspaceAction = IDEActionFactory.OPEN_WORKSPACE
                .create(window);
        register(openWorkspaceAction);

        projectPropertyDialogAction = IDEActionFactory.OPEN_PROJECT_PROPERTIES
                .create(window);
        register(projectPropertyDialogAction);

        if (window.getWorkbench().getIntroManager().hasIntro()) {
            introAction = ActionFactory.INTRO.create(window);
            register(introAction);
        }

        String showInQuickMenuId = "org.eclipse.ui.navigate.showInQuickMenu"; //$NON-NLS-1$
        showInQuickMenu = new QuickMenuAction(showInQuickMenuId) {
            protected void fillMenu(IMenuManager menu) {
                menu.add(ContributionItemFactory.VIEWS_SHOW_IN
                        .create(window));
            }
        };
        register(showInQuickMenu);

        final String newQuickMenuId = "org.eclipse.ui.file.newQuickMenu"; //$NON-NLS-1$
        newQuickMenu = new QuickMenuAction(newQuickMenuId) {
            protected void fillMenu(IMenuManager menu) {
                menu.add(new NewWizardMenu(window));
            }
        };
        register(newQuickMenu);

        pinEditorContributionItem = ContributionItemFactory.PIN_EDITOR
                .create(window);
        
//        searchComboItem = ContributionItemFactory.HELP_SEARCH.create(window);
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
            prefs.setValue(quickStartKey, found);
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
	    	prefs.setValue(tipsAndTricksKey, found);
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
        for (int i = 0; i < infos.length; i++) {
            if (infos[i].getWelcomePageURL() != null) {
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
        for (int i = 0; i < infos.length; i++) {
            if (infos[i].getTipsAndTricksHref() != null) {
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
            public void run() {
                if (isDisposed) {
					return;
				}
		    	IWorkspace workspace = ResourcesPlugin.getWorkspace();
				IProject[] projects = workspace.getRoot().getProjects();
		    	boolean enabled = BuildUtilities.isEnabled(projects, IncrementalProjectBuilder.INCREMENTAL_BUILD);
		        //update menu bar actions in project menu
		        buildAllAction.setEnabled(enabled);
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
        };
        if (immediately) {
        	update.run();
        }
        else {
	        // Dispatch the update to be run later in the UI thread.
	        // This helps to reduce flicker if autobuild is being temporarily disabled programmatically.
	        Shell shell = window.getShell();
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

        toolBarManager.update(false);
        toolBarItem.update(ICoolBarManager.SIZE);
    }
}

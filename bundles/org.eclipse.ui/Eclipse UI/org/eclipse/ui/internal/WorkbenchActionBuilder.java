package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.*;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.ui.internal.misc.*;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.*;
import org.eclipse.ui.actions.NewWizardAction;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.jface.action.*;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.util.*;
import java.util.*;

//debug
import org.eclipse.ui.internal.misc.UIHackFinder;

/**
 * This is used to add actions to the workbench.
 */
public class WorkbenchActionBuilder implements IPropertyChangeListener {
	// target
	private WorkbenchWindow window;
	
	// actions
	private NewWizardAction newWizardAction;
	private NewWizardDropDownAction newWizardDropDownAction;
	private NewWizardMenu newWizardMenu;
	private CloseEditorAction closeAction;
	private CloseAllAction closeAllAction;
	private ImportResourcesAction importResourcesAction;
	private ExportResourcesAction exportResourcesAction;
	private GlobalBuildAction rebuildAllAction;	// Full build
	private GlobalBuildAction buildAction;			// Incremental build
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
	private ClosePageAction closePageAction;
	private CloseAllPagesAction closeAllPagesAction;

	// menus
	private OpenPerspectiveMenu openPerspMenu;
	
	// retarget actions.
	private RetargetAction undoAction;
	private RetargetAction redoAction;
	private RetargetAction cutAction;
	private RetargetAction copyAction;
	private RetargetAction pasteAction;
	private RetargetAction deleteAction;
	private RetargetAction	 selectAllAction;
	private RetargetAction findAction;
	private RetargetAction addBookmarkAction;
/**
 * WorkbenchActionBuilder constructor comment.
 */
public WorkbenchActionBuilder() {
	super();
}
/**
 * Add the manual incremental build action
 * to both the menu bar and the tool bar.
 */
protected void addManualIncrementalBuildAction() {
	MenuManager menubar = window.getMenuBarManager();
	IMenuManager manager = menubar.findMenuUsingPath(IWorkbenchActionConstants.M_WORKBENCH);
	if (manager != null) {
		try {
			manager.insertBefore(IWorkbenchActionConstants.REBUILD_ALL, buildAction);
		}
		catch (IllegalArgumentException e) {
			// action not found!
		}
	}
		
	ToolBarManager toolbar = window.getToolBarManager();
	try {
		toolbar.prependToGroup(IWorkbenchActionConstants.BUILD_EXT, buildAction);
		toolbar.prependToGroup(IWorkbenchActionConstants.BUILD_EXT, new Separator());
		toolbar.update(true);
	}
	catch (IllegalArgumentException e) {
		// group not found
	}
}
/**
 * build the workbench menu
 */
public void buildActions(WorkbenchWindow win) {
	window = win;
	makeActions();
	createMenuBar();
	createToolBar();
	createShortcutBar();

	// Listen for preference property changes to
	// update the menubar and toolbar
	IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
	store.addPropertyChangeListener(this);

	// Listen to workbench page lifecycle methods to enable
	// and disable the perspective menu items as needed.
	// Note, the show view action already does its own
	// listening so no need to do it here.
	window.addPageListener(new IPageListener() {
		public void pageActivated(IWorkbenchPage page) {
			openPerspMenu.setReplaceEnabled(true);
			hideShowEditorAction.setEnabled(true);
			savePerspectiveAction.setEnabled(true);
			resetPerspectiveAction.setEnabled(true);
			editActionSetAction.setEnabled(true);
			closePageAction.setEnabled(true);
			closeAllPagesAction.setEnabled(true);
			newWizardMenu.setEnabled(true);
		}
		public void pageClosed(IWorkbenchPage page) {
			openPerspMenu.setReplaceEnabled(false);
			hideShowEditorAction.setEnabled(false);
			savePerspectiveAction.setEnabled(false);
			resetPerspectiveAction.setEnabled(false);
			editActionSetAction.setEnabled(false);
			closePageAction.setEnabled(false);
			closeAllPagesAction.setEnabled(false);
			newWizardMenu.setEnabled(false);
		}
		public void pageOpened(IWorkbenchPage page) {
		}
	});
}
/**
 * Create the menu bar.
 */
private void createMenuBar() {
	// Get main menu.
	MenuManager menubar = window.getMenuBarManager();

	// Create the file menu.
	MenuManager popup = new MenuManager("&File", IWorkbenchActionConstants.M_FILE);
	menubar.add(popup);
	popup.add(new GroupMarker(IWorkbenchActionConstants.FILE_START));
	{
		MenuManager newMenu = new MenuManager("&New");
		popup.add(newMenu);
		this.newWizardMenu = new NewWizardMenu(newMenu, window, true);
	}
	popup.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
	popup.add(new Separator());
	popup.add(closeAction);
	popup.add(closeAllAction);
	popup.add(new GroupMarker(IWorkbenchActionConstants.CLOSE_EXT));
	popup.add(new Separator());
	popup.add(saveAction);
	popup.add(saveAsAction);
	popup.add(saveAllAction);
	popup.add(new GroupMarker(IWorkbenchActionConstants.SAVE_EXT));
	popup.add(new Separator());
	popup.add(importResourcesAction);
	popup.add(exportResourcesAction);
	popup.add(new GroupMarker(IWorkbenchActionConstants.IMPORT_EXT));
	popup.add(
		new ReopenEditorMenu(
			window,
			((Workbench) (window.getWorkbench())).getEditorHistory(),
			true));
	popup.add(new GroupMarker(IWorkbenchActionConstants.MRU));
	popup.add(new Separator());
	popup.add(new QuitAction(window.getWorkbench()));
	popup.add(new GroupMarker(IWorkbenchActionConstants.FILE_END));

	// Edit menu.
	popup = new MenuManager("&Edit", IWorkbenchActionConstants.M_EDIT);
	menubar.add(popup);
	popup.add(new GroupMarker(IWorkbenchActionConstants.EDIT_START));
	popup.add(undoAction);
	popup.add(redoAction);
	popup.add(new GroupMarker(IWorkbenchActionConstants.UNDO_EXT));
	popup.add(new Separator());
	popup.add(cutAction);
	popup.add(copyAction);
	popup.add(pasteAction);
	popup.add(new GroupMarker(IWorkbenchActionConstants.CUT_EXT));
	popup.add(new Separator());
	popup.add(deleteAction);
	popup.add(selectAllAction);
	popup.add(new Separator());
	popup.add(findAction);
	popup.add(new Separator());
	popup.add(addBookmarkAction);
	popup.add(new GroupMarker(IWorkbenchActionConstants.EDIT_END));

	// View menu.
	popup = new MenuManager("&Perspective", IWorkbenchActionConstants.M_VIEW);
	menubar.add(popup);
	{
		MenuManager openInSameWindow = new MenuManager("&Open");
		openPerspMenu = 
			new OpenPerspectiveMenu(window, ResourcesPlugin.getWorkspace().getRoot());
		openInSameWindow.add(openPerspMenu);
		popup.add(openInSameWindow);
	}
	{
		MenuManager subMenu = new MenuManager("&Show View");
		popup.add(subMenu);
		new ShowViewMenu(subMenu, window, true);
	}
	popup.add(hideShowEditorAction = new ToggleEditorsVisibilityAction(window));
	popup.add(new Separator());
	popup.add(savePerspectiveAction = new SavePerspectiveAction(window));
	popup.add(editActionSetAction = new EditActionSetsAction(window));
	popup.add(resetPerspectiveAction = new ResetPerspectiveAction(window));
	popup.add(new Separator());
	popup.add(new NextPageAction("Previous@ALT+UP", -1, window));
	popup.add(new NextPageAction("Next@ALT+DOWN", 1, window));
	popup.add(new Separator());
	popup.add(closePageAction = new ClosePageAction(window));
	popup.add(closeAllPagesAction = new CloseAllPagesAction(window));
	popup.add(new Separator(IWorkbenchActionConstants.VIEW_EXT));

	// Workbench menu
	popup = new MenuManager("P&roject", IWorkbenchActionConstants.M_WORKBENCH);
	menubar.add(popup);
	popup.add(new GroupMarker(IWorkbenchActionConstants.WB_START));
	// Only add the manual incremental build if auto build off
	if (!ResourcesPlugin.getWorkspace().isAutoBuilding())
		popup.add(buildAction);
	popup.add(rebuildAllAction);
	popup.add(new GroupMarker(IWorkbenchActionConstants.WB_END));

	// Define section for additions.
	menubar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

	// Window menu.
	popup = new MenuManager("&Window", IWorkbenchActionConstants.M_WINDOW);
	menubar.add(popup);
	MenuManager launchWindowMenu =
		new MenuManager("&Launch", IWorkbenchActionConstants.M_LAUNCH);
	launchWindowMenu.add(new GroupMarker(IWorkbenchActionConstants.LAUNCH_EXT));
	popup.add(launchWindowMenu);
	popup.add(new Separator(IWorkbenchActionConstants.WINDOW_EXT));
	popup.add(new Separator());
	popup.add(openPreferencesAction);
	popup.add(new SwitchToWindowMenu(window, true));

	// Help menu.
	popup = new MenuManager("&Help", IWorkbenchActionConstants.M_HELP);
	menubar.add(popup);
	popup.add(new GroupMarker(IWorkbenchActionConstants.HELP_START));
	popup.add(new GroupMarker(IWorkbenchActionConstants.HELP_END));
	// about should always be at the bottom
	popup.add(aboutAction);
}
/**
 * Fills the shortcut bar
 */
private void createShortcutBar() {
	ToolBarManager shortcutBar = window.getShortcutBar();
	shortcutBar.add(new PerspectiveContributionItem(window, new OpenNewAction(window)));
	shortcutBar.add(new Separator(window.GRP_PAGES));
	shortcutBar.add(new Separator(window.GRP_FAST_VIEWS));
	shortcutBar.add(new ShowFastViewContribution(window));
}
/**
 * Fills the menu bar by merging all the individual viewers' contributions
 * and invariant (static) menus and menu items, as defined in MenuConstants interface.
 */   
private void createToolBar() {
	ToolBarManager toolbar = window.getToolBarManager();
	toolbar.add(newWizardDropDownAction);
	toolbar.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
	toolbar.add(new Separator());
	toolbar.add(saveAction);
	toolbar.add(saveAsAction);
	toolbar.add(new GroupMarker(IWorkbenchActionConstants.SAVE_EXT));
	// Only add the manual incremental build if auto build off
	if (!ResourcesPlugin.getWorkspace().isAutoBuilding()) {
		toolbar.add(new Separator());
		toolbar.add(buildAction);
	}
	toolbar.add(new GroupMarker(IWorkbenchActionConstants.BUILD_EXT));
}
/**
 * Remove the property change listener
 */
public void dispose() { 
	// Listen for preference property changes to
	// update the menubar and toolbar
	IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
	store.removePropertyChangeListener(this);
}
/**
 * Returns true if the menu with the given ID should
 * be considered OLE container menu. Container menus
 * are preserved in OLE menu merging.
 */
public static boolean isContainerMenu(String menuId) {
	if (menuId.equals(IWorkbenchActionConstants.M_FILE)) return true;
	if (menuId.equals(IWorkbenchActionConstants.M_VIEW)) return true;
	if (menuId.equals(IWorkbenchActionConstants.M_WORKBENCH)) return true;
	if (menuId.equals(IWorkbenchActionConstants.M_WINDOW)) return true;
	return false;
}
/**
 * Create actions for the menu bar and toolbar
 */
private void makeActions() {

	UIHackFinder.fixUI();
	// The actions in jface do not have menu vs. enable, vs. disable vs. color
	// There are actions in here being passed the workbench - problem 

	// Get services for notification.
	IPartService partService = window.getPartService();
	
	// Many actions need the workbench.
	IWorkbench workbench = window.getWorkbench();
	
	newWizardAction = new NewWizardAction();
	// images for this action are set in its constructor
	
	newWizardDropDownAction = new NewWizardDropDownAction(workbench, newWizardAction);
	newWizardDropDownAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_NEW_WIZ));
	newWizardDropDownAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_NEW_WIZ_HOVER));
	newWizardDropDownAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_NEW_WIZ_DISABLED));
	
	importResourcesAction = new ImportResourcesAction(workbench);
	importResourcesAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_IMPORT_WIZ));
	importResourcesAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_IMPORT_WIZ_HOVER));
	importResourcesAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_IMPORT_WIZ_DISABLED));
	
	exportResourcesAction = new ExportResourcesAction(workbench);
	exportResourcesAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_EXPORT_WIZ));
	exportResourcesAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_EXPORT_WIZ_HOVER));
	exportResourcesAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_EXPORT_WIZ_DISABLED));
	
	rebuildAllAction = new GlobalBuildAction(workbench, IncrementalProjectBuilder.FULL_BUILD);
	UIHackFinder.fixPR();
	// 1G82IWC - a new icon is needed for Rebuild All or Build
	//	rebuildAllAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_BUILD_EXEC));
	//	rebuildAllAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_BUILD_EXEC_HOVER));
	//	rebuildAllAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_BUILD_EXEC_DISABLED));

	buildAction = new GlobalBuildAction(workbench, IncrementalProjectBuilder.INCREMENTAL_BUILD);
	buildAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_BUILD_EXEC));
	buildAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_BUILD_EXEC_HOVER));
	buildAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_BUILD_EXEC_DISABLED));
	
	saveAction = new SaveAction(window);
	saveAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_SAVE_EDIT));
	saveAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_SAVE_EDIT_HOVER));
	saveAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_SAVE_EDIT_DISABLED));
	partService.addPartListener(saveAction);

	saveAsAction = new SaveAsAction(window);
	saveAsAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_SAVEAS_EDIT));
	saveAsAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_SAVEAS_EDIT_HOVER));
	saveAsAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_SAVEAS_EDIT_DISABLED));
	partService.addPartListener(saveAsAction);
	
	saveAllAction = new SaveAllAction(window);
	saveAllAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_SAVEALL_EDIT));
	saveAllAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_SAVEALL_EDIT_HOVER));
	saveAllAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_SAVEALL_EDIT_DISABLED));
	partService.addPartListener(saveAllAction);
	
	undoAction = new LabelRetargetAction(IWorkbenchActionConstants.UNDO, "&Undo@Ctrl+Z");
	undoAction.setToolTipText("Undo the last action");
	undoAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_UNDO_EDIT));
	undoAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_UNDO_EDIT_HOVER));
	undoAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_UNDO_EDIT_DISABLED));
	partService.addPartListener(undoAction);

	redoAction = new LabelRetargetAction(IWorkbenchActionConstants.REDO, "&Redo@Ctrl+Y");
	redoAction.setToolTipText("Redo the last action");
	redoAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_REDO_EDIT));
	redoAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_REDO_EDIT_HOVER));
	redoAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_REDO_EDIT_DISABLED));
	partService.addPartListener(redoAction);

	cutAction = new RetargetAction(IWorkbenchActionConstants.CUT, "Cu&t@Ctrl+X");
	cutAction.setToolTipText("Cut the selection to the clipboard");
	cutAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_CUT_EDIT));
	cutAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_CUT_EDIT_HOVER));
	cutAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_CUT_EDIT_DISABLED));
	partService.addPartListener(cutAction);

	copyAction = new RetargetAction(IWorkbenchActionConstants.COPY, "&Copy@Ctrl+C");
	copyAction.setToolTipText("Copy the selection to the clipboard");
	copyAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_COPY_EDIT));
	copyAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_COPY_EDIT_HOVER));
	copyAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_COPY_EDIT_DISABLED));
	partService.addPartListener(copyAction);

	pasteAction = new RetargetAction(IWorkbenchActionConstants.PASTE, "&Paste@Ctrl+V");
	pasteAction.setToolTipText("Paste from the clipboard");
	pasteAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_PASTE_EDIT));
	pasteAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_PASTE_EDIT_HOVER));
	pasteAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_PASTE_EDIT_DISABLED));
	partService.addPartListener(pasteAction);

	selectAllAction = new RetargetAction(IWorkbenchActionConstants.SELECT_ALL, "Select &All@Ctrl+A");
	selectAllAction.setToolTipText("Select the entire contents");
	partService.addPartListener(selectAllAction);

	findAction = new RetargetAction(IWorkbenchActionConstants.FIND, "&Find/Replace...@Ctrl+F");
	findAction.setToolTipText("Open a find and replace dialog");
	findAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_SEARCH_SRC));
	findAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_SEARCH_SRC_HOVER));
	findAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_SEARCH_SRC_DISABLED));
	partService.addPartListener(findAction);
	
	closeAction = new CloseEditorAction(window);
	partService.addPartListener(closeAction);

	closeAllAction = new CloseAllAction(window);
	partService.addPartListener(closeAllAction);

	aboutAction = new AboutAction(window);
	aboutAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJS_DEFAULT_PROD));

	openPreferencesAction = new OpenPreferencesAction(window);

	addBookmarkAction = new RetargetAction(IWorkbenchActionConstants.BOOKMARK, "Add Bookmar&k");
	addBookmarkAction.setToolTipText("Bookmark the selection");
	partService.addPartListener(addBookmarkAction);
	
	deleteAction = new RetargetAction(IWorkbenchActionConstants.DELETE, "&Delete@Delete");
	deleteAction.setToolTipText("Delete the selection");
	deleteAction.enableAccelerator(false);
	WorkbenchHelp.setHelp(deleteAction, new Object[] {IHelpContextIds.DELETE_RETARGET_ACTION});
	partService.addPartListener(deleteAction);

	// 1FVKH62: ITPUI:WINNT - quick start should be available on file menu
	quickStartAction = new QuickStartAction(workbench);
}
/**
 * Update the menubar and toolbar when
 * changes to the preferences are done.
 */
public void propertyChange(PropertyChangeEvent event) {
	// Allow manual incremental build only if the
	// auto build setting is off.
	if (event.getProperty() == IWorkbenchPreferenceConstants.AUTO_BUILD) {
		boolean autoBuildOn = ((Boolean) event.getNewValue()).booleanValue();
		if (autoBuildOn)
			removeManualIncrementalBuildAction();
		else
			addManualIncrementalBuildAction();
	}
}
/**
 * Remove the manual incremental build action
 * from both the menu bar and the tool bar.
 */
protected void removeManualIncrementalBuildAction() {
	MenuManager menubar = window.getMenuBarManager();
	IMenuManager manager = menubar.findMenuUsingPath(IWorkbenchActionConstants.M_WORKBENCH);
	if (manager != null) {
		try {
			manager.remove(IWorkbenchActionConstants.BUILD);
		}
		catch (IllegalArgumentException e) {
			// action was not in menu
		}
	}
		
	ToolBarManager toolbar = window.getToolBarManager();
	try {
		toolbar.remove(IWorkbenchActionConstants.BUILD);
		toolbar.update(true);
	}
	catch (IllegalArgumentException e) {
		// action was not in toolbar
	}
}
}

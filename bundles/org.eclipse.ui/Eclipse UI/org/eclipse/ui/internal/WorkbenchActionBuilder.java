package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.ui.internal.*;
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
import java.net.*;


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
	private PinEditorAction pinEditorAction;
	private ShowMenuAction showMenuAction;
	private CyclePartAction nextPartAction;
	private CyclePartAction prevPartAction;
	private CycleEditorAction nextEditorAction;
	private CycleEditorAction prevEditorAction;
	private ActivateEditorAction activateEditorAction;
	private WorkbenchEditorsAction workbenchEditorsAction;

	// menus
	private OpenPerspectiveMenu openPerspMenu;
	
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
	private RetargetAction printAction;
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
			enableActions(true);
		}
		public void pageClosed(IWorkbenchPage page) {
			enableActions(window.getActivePage() != null);
		}
		public void pageOpened(IWorkbenchPage page) {
		}
		private void enableActions(boolean value) {
			openPerspMenu.setReplaceEnabled(value);
			hideShowEditorAction.setEnabled(value);
			savePerspectiveAction.setEnabled(value);
			resetPerspectiveAction.setEnabled(value);
			editActionSetAction.setEnabled(value);
			closePageAction.setEnabled(value);
			closeAllPagesAction.setEnabled(value);
			newWizardMenu.setEnabled(value);
			newWizardDropDownAction.setEnabled(value);
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
	MenuManager popup = new MenuManager(WorkbenchMessages.getString("Workbench.file"), IWorkbenchActionConstants.M_FILE); //$NON-NLS-1$
	menubar.add(popup);
	popup.add(new GroupMarker(IWorkbenchActionConstants.FILE_START));
	{
		MenuManager newMenu = new MenuManager(WorkbenchMessages.getString("Workbench.new")); //$NON-NLS-1$
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
	popup.add(printAction);
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
	popup.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	popup.add(new Separator());
	popup.add(new QuitAction(window.getWorkbench()));
	popup.add(new GroupMarker(IWorkbenchActionConstants.FILE_END));

	// Edit menu.
	popup = new MenuManager(WorkbenchMessages.getString("Workbench.edit"), IWorkbenchActionConstants.M_EDIT); //$NON-NLS-1$
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
	popup.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

	// Perspective menu.
	popup = new MenuManager(WorkbenchMessages.getString("Workbench.perspective"), IWorkbenchActionConstants.M_VIEW); //$NON-NLS-1$
	menubar.add(popup);
	{
		MenuManager openInSameWindow = new MenuManager(WorkbenchMessages.getString("Workbench.open")); //$NON-NLS-1$
		openPerspMenu = 
			new WindowPerspectiveMenu(window, ResourcesPlugin.getWorkspace().getRoot());
		openInSameWindow.add(openPerspMenu);
		popup.add(openInSameWindow);
	}
	{
		MenuManager subMenu = new MenuManager(WorkbenchMessages.getString("Workbench.showView")); //$NON-NLS-1$
		popup.add(subMenu);
		new ShowViewMenu(subMenu, window, true);
	}
	popup.add(hideShowEditorAction = new ToggleEditorsVisibilityAction(window));
	popup.add(new Separator());
	popup.add(savePerspectiveAction = new SavePerspectiveAction(window));
	popup.add(editActionSetAction = new EditActionSetsAction(window));
	popup.add(resetPerspectiveAction = new ResetPerspectiveAction(window));
	popup.add(new Separator());
	popup.add(closePageAction = new ClosePageAction(window));
	popup.add(closeAllPagesAction = new CloseAllPagesAction(window));
	popup.add(new Separator(IWorkbenchActionConstants.VIEW_EXT));
	popup.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
	popup.add(new Separator());
	popup.add(new OpenPagesMenu(window, false));

	// Workbench menu
	popup = new MenuManager(WorkbenchMessages.getString("Workbench.project"), IWorkbenchActionConstants.M_WORKBENCH); //$NON-NLS-1$
	menubar.add(popup);
	popup.add(new GroupMarker(IWorkbenchActionConstants.WB_START));
	// Only add the manual incremental build if auto build off
	if (!ResourcesPlugin.getWorkspace().isAutoBuilding())
		popup.add(buildAction);
	popup.add(rebuildAllAction);
	popup.add(new GroupMarker(IWorkbenchActionConstants.WB_END));
	popup.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

	// Define section for additions.
	menubar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

	// Window menu.
	popup = new MenuManager(WorkbenchMessages.getString("Workbench.window"), IWorkbenchActionConstants.M_WINDOW); //$NON-NLS-1$
	menubar.add(popup);
	popup.add(new OpenWorkbenchAction(window));
	MenuManager launchWindowMenu =
		new MenuManager(WorkbenchMessages.getString("Workbench.launch"), IWorkbenchActionConstants.M_LAUNCH); //$NON-NLS-1$
	launchWindowMenu.add(new GroupMarker(IWorkbenchActionConstants.LAUNCH_EXT));
	popup.add(launchWindowMenu);
	popup.add(activateEditorAction);
	popup.add(showMenuAction);
	popup.add(nextEditorAction);
	popup.add(prevEditorAction);
	popup.add(nextPartAction);
	popup.add(prevPartAction);
	popup.add(new Separator(IWorkbenchActionConstants.WINDOW_EXT));
	popup.add(workbenchEditorsAction = new WorkbenchEditorsAction(window));
	popup.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
	popup.add(new Separator());
	popup.add(openPreferencesAction);
	popup.add(new SwitchToWindowMenu(window, true));

	// Help menu.
	popup = new MenuManager(WorkbenchMessages.getString("Workbench.help"), IWorkbenchActionConstants.M_HELP); //$NON-NLS-1$
	menubar.add(popup);
	// See if a welcome page is specified
	if (quickStartAction != null)
		popup.add(quickStartAction);
	popup.add(new GroupMarker(IWorkbenchActionConstants.HELP_START));
	popup.add(new GroupMarker(IWorkbenchActionConstants.HELP_END));
	popup.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
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
	toolbar.add(printAction);
	// Only add the manual incremental build if auto build off
	if (!ResourcesPlugin.getWorkspace().isAutoBuilding()) {
		toolbar.add(new Separator());
		toolbar.add(buildAction);
	}
	toolbar.add(pinEditorAction);
	toolbar.add(new PerspectiveComboBox(window));
	toolbar.add(new GroupMarker(IWorkbenchActionConstants.BUILD_EXT));
	toolbar.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
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
	
	undoAction = new LabelRetargetAction(IWorkbenchActionConstants.UNDO, WorkbenchMessages.getString("Workbench.undo")); //$NON-NLS-1$
	undoAction.setToolTipText(WorkbenchMessages.getString("Workbench.undoToolTip")); //$NON-NLS-1$
	undoAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_UNDO_EDIT));
	undoAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_UNDO_EDIT_HOVER));
	undoAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_UNDO_EDIT_DISABLED));
	partService.addPartListener(undoAction);

	redoAction = new LabelRetargetAction(IWorkbenchActionConstants.REDO, WorkbenchMessages.getString("Workbench.redo")); //$NON-NLS-1$
	redoAction.setToolTipText(WorkbenchMessages.getString("Workbench.redoToolTip")); //$NON-NLS-1$
	redoAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_REDO_EDIT));
	redoAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_REDO_EDIT_HOVER));
	redoAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_REDO_EDIT_DISABLED));
	partService.addPartListener(redoAction);

	cutAction = new RetargetAction(IWorkbenchActionConstants.CUT, WorkbenchMessages.getString("Workbench.cut")); //$NON-NLS-1$
	cutAction.setToolTipText(WorkbenchMessages.getString("Workbench.cutToolTip")); //$NON-NLS-1$
	cutAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_CUT_EDIT));
	cutAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_CUT_EDIT_HOVER));
	cutAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_CUT_EDIT_DISABLED));
	partService.addPartListener(cutAction);

	copyAction = new RetargetAction(IWorkbenchActionConstants.COPY, WorkbenchMessages.getString("Workbench.copy")); //$NON-NLS-1$
	copyAction.setToolTipText(WorkbenchMessages.getString("Workbench.copyToolTip")); //$NON-NLS-1$
	copyAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_COPY_EDIT));
	copyAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_COPY_EDIT_HOVER));
	copyAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_COPY_EDIT_DISABLED));
	partService.addPartListener(copyAction);

	pasteAction = new RetargetAction(IWorkbenchActionConstants.PASTE, WorkbenchMessages.getString("Workbench.paste")); //$NON-NLS-1$
	pasteAction.setToolTipText(WorkbenchMessages.getString("Workbench.pasteToolTip")); //$NON-NLS-1$
	pasteAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_PASTE_EDIT));
	pasteAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_PASTE_EDIT_HOVER));
	pasteAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_PASTE_EDIT_DISABLED));
	partService.addPartListener(pasteAction);

	printAction = new RetargetAction(IWorkbenchActionConstants.PRINT,WorkbenchMessages.getString("Workbench.print")); //$NON-NLS-1$
	printAction.setToolTipText(WorkbenchMessages.getString("Workbench.printToolTip")); //$NON-NLS-1$
	printAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_PRINT_EDIT));
	printAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_PRINT_EDIT_HOVER));
	printAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_PRINT_EDIT_DISABLED));
	partService.addPartListener(printAction);
	
	selectAllAction = new RetargetAction(IWorkbenchActionConstants.SELECT_ALL, WorkbenchMessages.getString("Workbench.selectAll")); //$NON-NLS-1$
	selectAllAction.setToolTipText(WorkbenchMessages.getString("Workbench.selectAllToolTip")); //$NON-NLS-1$
	partService.addPartListener(selectAllAction);

	findAction = new RetargetAction(IWorkbenchActionConstants.FIND, WorkbenchMessages.getString("Workbench.findReplace")); //$NON-NLS-1$
	findAction.setToolTipText(WorkbenchMessages.getString("Workbench.findReplaceToolTip")); //$NON-NLS-1$
	findAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_SEARCH_SRC));
	findAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_SEARCH_SRC_HOVER));
	findAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_SEARCH_SRC_DISABLED));
	partService.addPartListener(findAction);
	
	closeAction = new CloseEditorAction(window);
	partService.addPartListener(closeAction);

	closeAllAction = new CloseAllAction(window);
	partService.addPartListener(closeAllAction);
		
	pinEditorAction = new PinEditorAction(window);
	partService.addPartListener(pinEditorAction);
	pinEditorAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_PIN_EDITOR));
	pinEditorAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_PIN_EDITOR_HOVER));
	pinEditorAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_PIN_EDITOR_DISABLED));	

	aboutAction = new AboutAction(window);
	aboutAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJS_DEFAULT_PROD));

	openPreferencesAction = new OpenPreferencesAction(window);

	addBookmarkAction = new RetargetAction(IWorkbenchActionConstants.BOOKMARK, WorkbenchMessages.getString("Workbench.addBookMark")); //$NON-NLS-1$
	addBookmarkAction.setToolTipText(WorkbenchMessages.getString("Workbench.addBookMarkToolTip")); //$NON-NLS-1$
	partService.addPartListener(addBookmarkAction);
	
	deleteAction = new RetargetAction(IWorkbenchActionConstants.DELETE, WorkbenchMessages.getString("Workbench.delete")); //$NON-NLS-1$
	deleteAction.setToolTipText(WorkbenchMessages.getString("Workbench.deleteToolTip")); //$NON-NLS-1$
	deleteAction.setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_DELETE_EDIT));
	deleteAction.setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_DELETE_EDIT_HOVER));
	deleteAction.setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_DELETE_EDIT_DISABLED));
	deleteAction.enableAccelerator(false);
	WorkbenchHelp.setHelp(deleteAction, new Object[] {IHelpContextIds.DELETE_RETARGET_ACTION});
	partService.addPartListener(deleteAction);

	// See if a welcome page is specified
	if (((Workbench)PlatformUI.getWorkbench()).getProductInfo().getWelcomePageURL() != null)
		quickStartAction = new QuickStartAction(workbench);
	
	// Actions for invisible accelerators
	showMenuAction = new ShowMenuAction(window);
	nextEditorAction = new CycleEditorAction(window, true);
	prevEditorAction = new CycleEditorAction(window, false);
	nextPartAction = new CyclePartAction(window, true);
	prevPartAction = new CyclePartAction(window, false);
	activateEditorAction = new ActivateEditorAction(window);
}
/**
 * Update the menubar and toolbar when
 * changes to the preferences are done.
 */
public void propertyChange(PropertyChangeEvent event) {
	// Allow manual incremental build only if the
	// auto build setting is off.
	if (event.getProperty() == IPreferenceConstants.AUTO_BUILD) {
		boolean autoBuildOn = ((Boolean) event.getNewValue()).booleanValue();
		if (autoBuildOn)
			removeManualIncrementalBuildAction();
		else
			addManualIncrementalBuildAction();
	} else if(event.getProperty() == IPreferenceConstants.REUSE_EDITORS) {
		pinEditorAction.updateState();
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

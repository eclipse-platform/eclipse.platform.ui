package org.eclipse.ui.views.navigator;

import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.resources.IResource;

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.*;
import org.eclipse.ui.dialogs.PropertyDialogAction;

/**
 * This is the action group for all the resource navigator actions.
 * It delegates to several subgroups for most of the actions.
 * 
 * @see GotoActionGroup
 * @see OpenActionGroup
 * @see RefactorActionGroup
 * @see SortAndFilterActionGroup
 * @see WorkspaceActionGroup
 * 
 * @since 2.0
 */
public class ResourceNavigatorActionGroup extends ActionGroup {

	private IResourceNavigatorPart navigator;
	
	private AddBookmarkAction addBookmarkAction;
	private NewWizardAction newWizardAction;
	private PropertyDialogAction propertyDialogAction;
	private ImportResourcesAction importAction;
	private ExportResourcesAction exportAction;
	
	private GotoActionGroup gotoGroup;
	private OpenActionGroup openGroup;
	private RefactorActionGroup refactorGroup;
	private SortAndFilterActionGroup sortAndFilterGroup;
	private WorkspaceActionGroup workspaceGroup;

	public ResourceNavigatorActionGroup(IResourceNavigatorPart navigator) {
		this.navigator = navigator;
		makeActions();
		makeSubGroups();
	}

	private void makeActions() {
		Shell shell = navigator.getSite().getShell();
		IWorkbench workbench = navigator.getSite().getWorkbenchWindow().getWorkbench();
		addBookmarkAction = new AddBookmarkAction(shell);
		newWizardAction = new NewWizardAction();
		propertyDialogAction =
			new PropertyDialogAction(shell, navigator.getResourceViewer());
		importAction = new ImportResourcesAction(workbench);
		exportAction = new ExportResourcesAction(workbench);
	}
	
	private void makeSubGroups() {
		gotoGroup = new GotoActionGroup(navigator);
		openGroup = new OpenActionGroup(navigator);
		refactorGroup = new RefactorActionGroup(navigator);
		sortAndFilterGroup = new SortAndFilterActionGroup(navigator);
		workspaceGroup = new WorkspaceActionGroup(navigator);
	}
	
	/**
	 * Extends the superclass implementation to set the context in the subgroups.
	 */
	public void setContext(ActionContext context) {
		super.setContext(context);
		gotoGroup.setContext(context);
		openGroup.setContext(context);
		refactorGroup.setContext(context);
		sortAndFilterGroup.setContext(context);
		workspaceGroup.setContext(context);
	}
	
	public void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection =
			(IStructuredSelection) getContext().getSelection();
		boolean onlyFilesSelected =
			!selection.isEmpty()
				&& ResourceSelectionUtil.allResourcesAreOfType(selection, IResource.FILE);
		

		MenuManager newMenu =
			new MenuManager(ResourceNavigatorMessages.getString("ResourceNavigator.new")); //$NON-NLS-1$
		menu.add(newMenu);
		new NewWizardMenu(newMenu, navigator.getSite().getWorkbenchWindow(), false);
		
		gotoGroup.fillContextMenu(menu);
		openGroup.fillContextMenu(menu);
		menu.add(new Separator());
		
		refactorGroup.fillContextMenu(menu);
		menu.add(new Separator());
		
		menu.add(importAction);
		menu.add(exportAction);
		importAction.setSelection(selection);
		exportAction.setSelection(selection);
		menu.add(new Separator());
				
		if (onlyFilesSelected) {
			addBookmarkAction.selectionChanged(selection);
			menu.add(addBookmarkAction);
		}
		menu.add(new Separator());
		
		workspaceGroup.fillContextMenu(menu);
		
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "-end")); //$NON-NLS-1$
		menu.add(new Separator());

		if (propertyDialogAction.isApplicableForSelection(selection)) {
			propertyDialogAction.selectionChanged(selection);
			menu.add(propertyDialogAction);
		}
	}
	
	/**
	 * Overrides the super implementation to delegate to the subgroups.
	 */
	public void fillActionBars(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(
			IWorkbenchActionConstants.PROPERTIES,
			propertyDialogAction);
		actionBars.setGlobalActionHandler(
			IWorkbenchActionConstants.BOOKMARK,
			addBookmarkAction);
			
		gotoGroup.fillActionBars(actionBars);
		openGroup.fillActionBars(actionBars);
		refactorGroup.fillActionBars(actionBars);
		sortAndFilterGroup.fillActionBars(actionBars);
		workspaceGroup.fillActionBars(actionBars);
	}
	
	/**
	 * Overrides the super implementation to delegate to the subgroups.
	 */
	public void updateActionBars() {
		IStructuredSelection selection =
			(IStructuredSelection) getContext().getSelection();
		propertyDialogAction.setEnabled(
			propertyDialogAction.isApplicableForSelection(selection));
		addBookmarkAction.selectionChanged(selection);
		
		gotoGroup.updateActionBars();
		openGroup.updateActionBars();
		refactorGroup.updateActionBars();
		sortAndFilterGroup.updateActionBars();
		workspaceGroup.updateActionBars();
	} 
	
	/**
	 * Runs the default action (open file).
	 */
	public void runDefaultAction(IStructuredSelection selection) {
		openGroup.runDefaultAction(selection);
	}
	

	/**
 	 * Handles a key pressed event by invoking the appropriate action,
 	 * delegating to the subgroups as necessary.
 	 */
	public void handleKeyPressed(KeyEvent event) {
		refactorGroup.handleKeyPressed(event);
		workspaceGroup.handleKeyPressed(event);
	}
	
	/**
	 * Extends the superclass implementation to dispose the subgroups.
	 */
	public void dispose() {
		super.dispose();
		gotoGroup.dispose();
		openGroup.dispose();
		refactorGroup.dispose();
		sortAndFilterGroup.dispose();
		workspaceGroup.dispose();
	}
}
